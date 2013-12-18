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
package org.netbeans.modules.j2ee.appengine;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.extexecution.ExternalProcessSupport;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

import org.netbeans.modules.j2ee.appengine.ide.AppEngineJ2eePlatformImpl;
import org.netbeans.modules.j2ee.appengine.ide.AppEngineLogger;

import org.netbeans.modules.j2ee.appengine.ide.AppEngineStartServer;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.appengine.util.Utils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * @author Michal Mocnak
 */
public class AppEngineDeploymentManager implements DeploymentManager {

    private final String uri;
    private final AppEngineTarget target;
    private final AppEngineLogger logger;
    private AppEngineTargetModuleID module;
    private ProgressObject progress;
    private final AppEnginePluginProperties properties;
    private AppEngineJ2eePlatformImpl platform;
    private Process process;
    private ExecutorService executor;
    private Project selected;
    public boolean restartedAction;

    //private boolean knownWebApp;

    public String pname() {
        return getSelected() == null ? " NULL " : getSelected().getProjectDirectory().getName();
    }

    public AppEngineDeploymentManager(String uri) {
        this.uri = uri;
        this.properties = new AppEnginePluginProperties(this);
        this.target = new AppEngineTarget(getProperties().getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR));
        this.logger = AppEngineLogger.getInstance(uri);
    }


    public String getUri() {
        return uri;
    }

    public boolean isRestartedAction() {
        return restartedAction;
    }

    public AppEngineTargetModuleID getTargetModuleID() {
        String c = "null";
        if (selected != null) {
            c = selected.getProjectDirectory().getName();
        } else {
            return null;
        }
        module = new AppEngineTargetModuleID(getTarget(),
                getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.PROPERTY_HOST),
                Integer.valueOf(getProperties().getInstanceProperties().getProperty(InstanceProperties.HTTP_PORT_NUMBER)),
                c);

        return module;
    }

    /**
     *
     * @return
     */
    public ProgressObject getProgress() {
        AppEngineStartServer startServer = AppEngineStartServer.getInstance(this);
        if (startServer.getMode() == Deployment.Mode.DEBUG && startServer.isRunning()) {
            progress = new AppEngineProgressObject(getTargetModuleID(), false, Deployment.Mode.DEBUG);
        } else if (null == progress) {
            progress = new AppEngineProgressObject(getTargetModuleID(), false, Deployment.Mode.RUN);
        }

        return progress;
    }

    public Deployment.Mode getServerMode() {
        AppEngineStartServer startServer = AppEngineStartServer.getInstance(this);
        return startServer.getMode();
    }

    public boolean isServerRunning() {
        AppEngineStartServer startServer = AppEngineStartServer.getInstance(this);
        return startServer.isRunning();
    }

    public AppEngineTarget getTarget() {
        return target;
    }

    public AppEnginePluginProperties getProperties() {
        return properties;
    }

    public AppEngineJ2eePlatformImpl getPlatform() {
        if (null == platform) {
            platform = new AppEngineJ2eePlatformImpl(this);
        }

        return platform;
    }

    public Project getSelected() {
        return selected;
    }

    public void setSelected(Project selected) {
        this.selected = selected;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public Target[] getTargets() throws IllegalStateException {
        return new Target[]{target};
    }

    @Override
    public TargetModuleID[] getRunningModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
        Utils.out("--- DEPLOYMENTMANAGER: getRunningModules" + " --- " + pname());

        return new TargetModuleID[]{getTargetModuleID()};
    }

    @Override
    public TargetModuleID[] getNonRunningModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
        Utils.out("--- DEPLOYMENTMANAGER: getNonRunningModules" + " --- " + pname());

        return new TargetModuleID[]{};
    }

    @Override
    public TargetModuleID[] getAvailableModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
        Utils.out("--- DEPLOYMENTMANAGER: getAvailableModules" + " --- " + pname());

        return new TargetModuleID[]{};
    }

    @Override
    public DeploymentConfiguration createConfiguration(DeployableObject arg0) throws InvalidModuleException {
        Utils.out("--- DEPLOYMENTMANAGER: createConfiguration" + " --- " + pname());

        return null;
    }

    protected ProgressObject restartNormalAction() {

        Utils.out("--- DEPLOYMENTMANAGER restartNormalAction 1");
        new RequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                stopServer();
                ActionProvider ap = selected.getLookup().lookup(ActionProvider.class);
                restartedAction = true;
                Utils.out("--- DEPLOYMENTMANAGER distributer invoke Run action");
                ap.invokeAction(ActionProvider.COMMAND_RUN, selected.getLookup());
            }
        });
        return new AppEngineProgressObject(getTargetModuleID(), true, Deployment.Mode.RUN, CommandType.DISTRIBUTE);
    }
    
    protected ProgressObject restartDebugAction() {

        //AppEngineStartServer startServer = AppEngineStartServer.getInstance(this);


        Utils.out("--- DEPLOYMENTMANAGER restartDebugAction 1");
        RequestProcessor rp = new RequestProcessor();
        rp.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                stopServer();
                Session ses = DebuggerManager.getDebuggerManager().getCurrentSession();
                Utils.out("--- DEPLOYMENTMANAGER debugger session = " + ses);
                //ses.kill();
                ActionProvider ap = selected.getLookup().lookup(ActionProvider.class);
                restartedAction = true;
                Utils.out("--- DEPLOYMENTMANAGER distributer invoke debug action");
                ap.invokeAction(ActionProvider.COMMAND_DEBUG, selected.getLookup());
            }
        });
        return new AppEngineProgressObject(getTargetModuleID(), true, Deployment.Mode.DEBUG, CommandType.DISTRIBUTE);
    }

    protected ProgressObject restartProfileAction() {


        Utils.out("--- DEPLOYMENTMANAGER restartProfileAction 1");
        RequestProcessor rp = new RequestProcessor();
        rp.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                stopServer();
                ActionProvider ap = selected.getLookup().lookup(ActionProvider.class);
                restartedAction = true;
                Utils.out("--- DEPLOYMENTMANAGER distributer invoke Profile action");
                ap.invokeAction(ActionProvider.COMMAND_PROFILE, selected.getLookup());
            }
        });
        return new AppEngineProgressObject(getTargetModuleID(), true, Deployment.Mode.PROFILE, CommandType.DISTRIBUTE);
    }
    
    @Override
    public ProgressObject distribute(Target[] target, File file, File plan) throws IllegalStateException {
        Utils.out("--- DEPLOYMENTMANAGER DISTRIBUTE time=" + new Date());
        AppEngineStartServer startServer = AppEngineStartServer.getInstance(this);
        startServer.setServerNeedsRestart(true);
        if (restartedAction) {
            Utils.out("--- DEPLOYMENTMANAGER DISTRIBUTE redeploy == true time=" + new Date());
            restartedAction = false;
//            startServer.setExtendedMode(null);
            getProperties().getInstanceProperties().setProperty("deployedProject", selected.getProjectDirectory().getPath());
           return getProgress();
        }

        FileObject fo = FileUtil.toFileObject(file);

        Project distrProject = FileOwnerQuery.getOwner(fo);
        if (distrProject.equals(selected)) {
            getProperties().getInstanceProperties().setProperty("deployedProject", distrProject.getProjectDirectory().getPath());
            return new AppEngineProgressObject(getTargetModuleID(), false, startServer.getMode(), CommandType.DISTRIBUTE);            
        }
        //
        // The server started with invalid web project. We must restart it.
        //
        if (Deployment.Mode.RUN.equals(startServer.getMode())) {
            Utils.out("--- DEPLOYMENTMANAGER DISTRIBUTE. 1");
            
//            ProgressObject po = new AppEngineProgressObject(getTargetModuleID(), false, AppEngineServerMode.NORMAL, CommandType.DISTRIBUTE);
            selected = distrProject;
            
//            if (selected.equals(distrProject)) {
                // web app is correctly selected
//                startServer.setExtendedMode(null);
//            } else {
                // we must restart the whole action 
//                selected = distrProject;
//                startServer.setExtendedMode(null);
                restartedAction = true;
                return restartNormalAction();

//            }
//            return po;
            
        } else if (Deployment.Mode.DEBUG.equals(startServer.getMode())) {
            //
            // startDebugging has alredy been issued just before distribute.
            // We must check whether we guess the web app used to start the server.
            // 
//            ProgressObject po = new AppEngineProgressObject(getTargetModuleID(), false, AppEngineServerMode.DEBUG, CommandType.DISTRIBUTE);
//            if (selected.equals(distrProject)) {
                // web app is correctly selected
//                selected = distrProject;
//                startServer.setExtendedMode(null);
//            } else {
                // we must restart the whole action 
                selected = distrProject;
//                startServer.setExtendedMode(null);
                restartedAction = true;
                return restartDebugAction();

//            }
//            return po;
//            selected = distrProject;
//            return startInNomalMode();
        } else if (Deployment.Mode.PROFILE.equals(startServer.getMode())) {
            //
            // startDebugging has alredy been issued just before distribute.
            // We must check whether we guess the web app used to start the server.
            // 
//            ProgressObject po = new AppEngineProgressObject(getTargetModuleID(), false, AppEngineServerMode.DEBUG, CommandType.DISTRIBUTE);
//            if (selected.equals(distrProject)) {
                // web app is correctly selected
//                selected = distrProject;
//                startServer.setExtendedMode(null);
//            } else {
                // we must restart the whole action 
                selected = distrProject;
//                startServer.setExtendedMode(null);
                restartedAction = true;
                return restartProfileAction();

//            }
//            return po;
//            selected = distrProject;
//            return startInNomalMode();
        }
        return null;
    }

    @Override
    public ProgressObject distribute(Target[] arg0, InputStream arg1, InputStream arg2) throws IllegalStateException {
        return getProgress();
    }

    @Override
    public ProgressObject distribute(Target[] arg0, ModuleType arg1, InputStream arg2, InputStream arg3) throws IllegalStateException {
        return getProgress();
    }

    @Override
    public ProgressObject start(TargetModuleID[] arg0) throws IllegalStateException {
        Utils.out("--- DEPLOYMENTMANAGER: start" + " --- " + pname());

        return getProgress();
    }

    @Override
    public ProgressObject stop(TargetModuleID[] arg0) throws IllegalStateException {
        Utils.out("--- DEPLOYMENTMANAGER: stop" + " --- " + pname());

        return getProgress();
    }

    public void stopServer() {
        Utils.out("--- DEPLOYMENTMANAGER stopServer" + " --- " + pname());

        Process process = getProcess();
        ExecutorService executor = getExecutor();

        // Kill process
        if (null != process) {
            //Utils.out("--- DEPLOYMENTMANAGER stopServer process NOT null " + " --- " + pname());

            // Kill process
            ExternalProcessSupport.destroy(process, new HashMap<String, String>());
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            // Set to null
            setProcess(null);
        }

        // Shutdown executor
        if (null != executor) {
            //Utils.out("--- DEPLOYMENTMANAGER stopServer executor NOT null " + " --- " + pname());

            // Shutdown
            executor.shutdownNow();
            // Set to null
            setExecutor(null);
        }
        AppEngineStartServer startServer = AppEngineStartServer.getInstance(this);
        while (startServer.isRunning()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Utils.out("--- DEPLOYMENTMANAGER stopServer() isRunning()=" + startServer.isRunning());

        startServer.setMode(Deployment.Mode.RUN);
//        startServer.setExtendedMode(AppEngineServerMode.NORMAL);
        //selected = null;

        //return null;
        //return (current = new AppEngineProgressObject(manager.getTargetModuleID(), false, mode));
    }

    @Override
    public ProgressObject undeploy(TargetModuleID[] arg0) throws IllegalStateException {
        return getProgress();
    }

    @Override
    public boolean isRedeploySupported() {
        return false;
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] arg0, File arg1, File arg2) throws UnsupportedOperationException, IllegalStateException {
        return getProgress();
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] arg0, InputStream arg1, InputStream arg2) throws UnsupportedOperationException, IllegalStateException {
        return getProgress();
    }

    @Override
    public void release() {
    }

    @Override
    public Locale getDefaultLocale() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Locale getCurrentLocale() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLocale(Locale arg0) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Locale[] getSupportedLocales() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLocaleSupported(Locale arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DConfigBeanVersionType getDConfigBeanVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType arg0) {
        return false;
    }

    @Override
    public void setDConfigBeanVersion(DConfigBeanVersionType arg0) throws DConfigBeanVersionUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, DeploymentContext deployment) {
        Utils.out("--- DEPLOYMENTMANAGER: redeploy 2" + " --- " + pname());
        //return null;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public class GaeProgressListener implements ProgressListener {

        @Override
        public void handleProgressEvent(ProgressEvent pe) {
            Utils.out("GaeProgressListener " + pe.toString());
        }

    }
}
