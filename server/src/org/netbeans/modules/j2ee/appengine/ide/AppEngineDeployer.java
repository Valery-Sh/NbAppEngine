/**
 * This file is part of Google App Engine suppport in NetBeans IDE.
 *
 * Google App Engine suppport in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Google App Engine suppport in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Google App Engine suppport in NetBeans IDE. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.j2ee.appengine.ide;

import java.io.IOException;
import java.nio.charset.Charset;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.extexecution.input.InputReaderTask;
import org.netbeans.api.extexecution.input.InputReaders;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.project.Project;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginUtils;
import org.netbeans.modules.j2ee.deployment.plugins.api.CommonServerBridge;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * @author Michal Mocnak
 */
public class AppEngineDeployer implements Runnable, ProgressObject {
//My

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.j2ee.appengine.ide.AppEngineDeployer");
//END MY
    private final AppEngineDeploymentManager manager;
    private final AppEngineServerMode mode;
    private final AppEngineLogger logger;
    private final InstanceProperties properties;
    private final String name;
    private final Project project;
    private final List<ProgressListener> listeners = new ArrayList<ProgressListener>();
    private DeploymentStatus status = new AppEngineDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, null);
    private String warChecksum;

    private AppEngineDeployer(AppEngineDeploymentManager manager, AppEngineServerMode mode, Project project) {
        this.manager = manager;
        
        this.properties = manager.getProperties().getInstanceProperties();
        this.name = properties.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        this.project = project;
        this.mode = mode;
        this.logger = AppEngineLogger.getInstance(manager.getUri());


//org.netbeans.modules.j2ee.deployment.impl.ServerInstance si;        
// Start deployer
//My cut see getInstance() deploy()        RequestProcessor.getDefault().post(this);
    }

    public static AppEngineDeployer getInstance(AppEngineDeploymentManager manager, AppEngineServerMode mode, Project project) {
        return new AppEngineDeployer(manager, mode, project);
    }

    public void deploy() {
        // Start deployer
        RequestProcessor.getDefault().post(this);
    }

    public Project getProject() {
        return project;
    }

    public String getWarChecksum() {
        return warChecksum;
    }

    @Override
    public void run() {
        // Get executor
        ExecutorService executor = manager.getExecutor();
        // If not null shutdown
        if (null != executor) {
            executor.shutdownNow();
        }

        // Reset logger
        try {
            logger.reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        // Ant runtime properties
        Properties props = new Properties();

        // Set server uri
        props.setProperty(InstanceProperties.URL_ATTR, manager.getUri());

        // Execute
        StartupExtender.StartMode startMode;
        String target;
        if (mode == AppEngineServerMode.NORMAL) {
            target = "runserver";
            startMode = StartupExtender.StartMode.NORMAL;
        } else if (mode == AppEngineServerMode.DEBUG) {
            target = "runserver-debug";
            startMode = StartupExtender.StartMode.DEBUG;
        } else if (mode == AppEngineServerMode.PROFILE) {
            target = "runserver-profile";
            startMode = StartupExtender.StartMode.PROFILE;
        } else {
            // issue #174297 - server process is null
            String message = NbBundle.getMessage(AppEngineDeployer.class, "no_server_process");
            NotifyDescriptor dd = new DialogDescriptor.Message(message, DialogDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(dd);
            return;
        }

        ServerInstance instance = CommonServerBridge.getCommonInstance(manager.getUri());
        
        StringBuilder jvmargs = new StringBuilder();
        List<StartupExtender> l = StartupExtender.getExtenders(Lookups.singleton(instance), startMode);
        for (StartupExtender args : StartupExtender.getExtenders(Lookups.singleton(instance), startMode)) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "args.length=" + args.getArguments().size());

            for (String arg : args.getArguments()) {
                jvmargs.append(" --jvm_flag=").append(arg);
            }
        }
        if (jvmargs.toString().trim().length() != 0) {
            props.setProperty("jvmargs", jvmargs.toString());
        }

        // Executor task object
        Process serverProcess = AppEnginePluginUtils.runAntTarget(project, target, props);
        //serverProcess.exitValue();
        // Create new executor
        executor = Executors.newSingleThreadExecutor();

        // Set it to the manager
        manager.setExecutor(executor);

        // Start logging normal
        executor.submit(InputReaderTask.newTask(
                InputReaders.forStream(serverProcess.getInputStream(),
                Charset.defaultCharset()), logger));

        // Wait for dist
        while (null == project.getProjectDirectory().getFileObject("dist")
                || project.getProjectDirectory().getFileObject("dist").getChildren().length == 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // Generate checksum
        warChecksum = AppEnginePluginUtils.getWarChecksum(project);

        // Store process
        manager.setProcess(serverProcess);
        // Fire changes
        fireStartProgressEvent(StateType.RUNNING, createProgressMessage("MSG_START_SERVER_IN_PROGRESS"));

        // read from the input stream
        while (!logger.contains("Dev App Server is now running")
                && !logger.contains("The server is running")
                && !logger.contains("Listening for transport dt_socket at address")
                && !logger.contains("Waiting for connection on port")) {
            if (logger.contains("Address already in use") || logger.contains("Error occurred")
                    || logger.contains("BUILD FAILED")) {
                // Fire changes
                fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED"));
                // Clear process
                manager.setProcess(null);
                return;
            }

            // when the stream is empty - sleep for a while
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        fireStartProgressEvent(StateType.COMPLETED, createProgressMessage("MSG_SERVER_STARTED"));

        if ("runserver-debug".equals(target) && manager.isDebuggedSet()) {
            FileObject fo = manager.getSelected().getProjectDirectory().getFileObject("build.xml");
            try {
                Properties actionProps = new Properties();
                String host = properties.getProperty(AppEnginePluginProperties.PROPERTY_HOST);                
                String debug_port = properties.getProperty(AppEnginePluginProperties.DEBUG_PORT_NUMBER);                                
                actionProps.put("ignore.ant.logger", "true");
                actionProps.put("jpda.address", debug_port);
                actionProps.put("jpda.host", host);
                actionProps.put("do.debug.server", "true");
                actionProps.put("j2ee.compile.on.save", "true");
                actionProps.put("jpda.transport", "dt_socket");

                ActionUtils.runTarget(fo, new String[]{"init", "-init-cos", "connect-debugger"}, actionProps).waitFinished();

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    private String createProgressMessage(final String resName) {
        return createProgressMessage(resName, null);
    }

    private String createProgressMessage(final String resName, final String param) {
        return NbBundle.getMessage(AppEngineDeployer.class, resName, name, param);
    }

    private void fireStartProgressEvent(StateType stateType, String msg) {
        status = new AppEngineDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, stateType, msg);

        // Fire changes into ProgressObject
        fireHandleProgressEvent();
    }

    @Override
    public DeploymentStatus getDeploymentStatus() {
        return status;
    }

    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[]{manager.getModule()};
    }

    @Override
    public ClientConfiguration getClientConfiguration(TargetModuleID arg0) {
        return null;
    }

    @Override
    public boolean isCancelSupported() {
        return false;
    }

    @Override
    public void cancel() throws OperationUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStopSupported() {
        return false;
    }

    @Override
    public void stop() throws OperationUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addProgressListener(ProgressListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeProgressListener(ProgressListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void fireHandleProgressEvent() {
        ProgressEvent evt = new ProgressEvent(this, null, getDeploymentStatus());

        ProgressListener[] targets;

        synchronized (listeners) {
            targets = listeners.toArray(new ProgressListener[]{});
        }
        for (ProgressListener listener : targets) {
            listener.handleProgressEvent(evt);
        }
    }
}