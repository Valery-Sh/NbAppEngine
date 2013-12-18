/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.j2ee.appengine.ide;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import org.netbeans.api.extexecution.input.InputReaderTask;
import org.netbeans.api.extexecution.input.InputReaders;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.project.Project;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentStatus;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginUtils;
import org.netbeans.modules.j2ee.appengine.util.Utils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.CommonServerBridge;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

public class AppEngineDeployer implements Runnable, ProgressObject {

    private ExecutorTask waitTask;
    private final AppEngineDeploymentManager manager;
    protected final Deployment.Mode mode;
    protected final AppEngineLogger logger;
    private final InstanceProperties properties;
    private final String name;
    private final Project project;
    private final List<ProgressListener> listeners = new ArrayList<ProgressListener>();
    private DeploymentStatus status = new AppEngineDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, null);
    private String warChecksum;

    protected AppEngineDeployer(AppEngineDeploymentManager manager, Deployment.Mode mode, Project project) {
        this.manager = manager;

        this.properties = manager.getProperties().getInstanceProperties();
        this.name = properties.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        this.project = project;
        this.mode = mode;
        this.logger = AppEngineLogger.getInstance(manager.getUri());
    }

    public static AppEngineDeployer getInstance(AppEngineDeploymentManager manager, Deployment.Mode mode, Project project) {
        return new AppEngineDeployer(manager, mode, project);
    }

    public AppEngineDeploymentManager getManager() {
        return manager;
    }

    public InstanceProperties getInstanceProperties() {
        return properties;
    }

    public void deploy() {
        // Start deployer
        RequestProcessor.getDefault().post(this);
    }

    public void deploy(ExecutorTask waitTask) {
        this.waitTask = waitTask;
        Utils.out("AppEngineDeployer deploy");
        // Start deployer
        RequestProcessor.getDefault().post(this);
    }

    public Project getProject() {
        return project;
    }

    public String getWarChecksum() {
        return warChecksum;
    }

    public Properties prepare() {
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
        if (mode == Deployment.Mode.RUN) {
            target = "runserver";
            startMode = StartupExtender.StartMode.NORMAL;
        } else if (mode == Deployment.Mode.DEBUG) {
            target = "runserver-debug";
            startMode = StartupExtender.StartMode.DEBUG;
        } else if (mode == Deployment.Mode.PROFILE) {
            target = "runserver-profile";
            startMode = StartupExtender.StartMode.PROFILE;
        } else {
            // issue #174297 - server process is null
            String message = NbBundle.getMessage(AppEngineDeployer.class, "no_server_process");
            NotifyDescriptor dd = new DialogDescriptor.Message(message, DialogDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(dd);
            return null;
        }

        ServerInstance instance = CommonServerBridge.getCommonInstance(manager.getUri());
        Collection c = instance.getLookup().lookupAll(Object.class);
        for (Object o : c) {
            Utils.out("CLASS: " + o.getClass().getName());
        }
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
        // Create new executor
        executor = Executors.newSingleThreadExecutor();

        // Set it to the manager
        manager.setExecutor(executor);

        return props;
    }

    
    protected String getTarget() {
        String target = null;
        if (mode == Deployment.Mode.RUN) {
            target = "runserver";
        } else if (mode == Deployment.Mode.DEBUG) {
            target = "runserver-debug";
        } else if (mode == Deployment.Mode.PROFILE) {
            target = "runserver-profile";
        }
        Utils.out(getClass().getSimpleName() + " getTarget=" + target);
        return target;
    }

    public ExecutorTask getWaitTask() {
        return waitTask;
    }

    public AppEngineStartServer getStartServer() {
        return AppEngineStartServer.getInstance(manager);
    }
    protected Process runAntTarget(String target, Properties props) {
        return AppEnginePluginUtils.runAntTarget(manager,target,props);
    }
    
    @Override
    public void run() {
        Utils.out("AppEngineDeployer.run() 1 time=" + new Date());
        if (getStartServer().isRunning()) {
            fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_ALLREADY_RUNNING"));
            return;
        }
        Properties props = prepare();

        String target = getTarget();

        // Executor task object
        Process serverProcess = runAntTarget(target, props);
        manager.getExecutor().submit(InputReaderTask.newTask(
                InputReaders.forStream(serverProcess.getInputStream(),
                        Charset.defaultCharset()), logger));
        manager.setProcess(serverProcess);
        // Fire changes
        fireStartProgressEvent(StateType.RUNNING, createProgressMessage("MSG_START_SERVER_IN_PROGRESS"));
        if (waitFinished() < 0) {
            return;
        }

        Utils.out("========= AppEngineDeployer.run() COMPLETED isRunning=" + getStartServer().isRunning() + "; time=" + new Date());
        //properties.refreshServerInstance();
        fireStartProgressEvent(StateType.COMPLETED, createProgressMessage("MSG_SERVER_STARTED"));

        accomplish();

    }

    /**
     *
     * @return 0 -continue; 1 - success; -1 failed
     */
    protected int waitFinished() {
        //while ( ) 
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
//        if ( true ) return 1;
        Utils.out("AppEngineDeployer.waitFinished()");
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
                return -1;
            }

            // when the stream is empty - sleep for a while
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // do nothing
            }

        }

        return 1;
    }

    protected void accomplish() {
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
        return new TargetModuleID[]{manager.getTargetModuleID()};
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
