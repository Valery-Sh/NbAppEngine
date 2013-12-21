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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.extexecution.ExternalProcessSupport;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentFactory;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.AppEngineProgressObject;
import org.netbeans.modules.j2ee.appengine.ui.AppEngineProjectChooser;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginUtils;
import org.netbeans.modules.j2ee.appengine.util.Utils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
//import org.netbeans.modules.profiler.actions.StopAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

//import org.openide.actions.
/**
 * @author Michal Mocnak
 */
public class AppEngineStartServer extends StartServer {

    // Instances map
    private static final Map<AppEngineDeploymentManager, AppEngineStartServer> instances = new HashMap<AppEngineDeploymentManager, AppEngineStartServer>();
    // Instance of deployment manager
    private final AppEngineDeploymentManager manager;
    private ProgressObject current;
    private Deployment.Mode mode;
    //private AppEngineServerMode extendedMode; // null after distribute
    private boolean serverNeedsRestart;
    
    private AppEngineStartServer(AppEngineDeploymentManager manager) {
        this.manager = manager;
        serverNeedsRestart = true;
    }

    public synchronized static AppEngineStartServer getInstance(AppEngineDeploymentManager manager) {
        AppEngineStartServer start = instances.get(manager);

        if (null == start) {
            // Create a new one
            start = new AppEngineStartServer(manager);

            // Cache it
            instances.put(manager, start);
        }

        return start;
    }

    public String pname() {
        return manager.getSelected() == null ? " NULL " : manager.getSelected().getProjectDirectory().getName();
    }

    public boolean isServerNeedsRestart() {
        return serverNeedsRestart;
    }

    public void setServerNeedsRestart(boolean serverNeedsRestart) {
        this.serverNeedsRestart = serverNeedsRestart;
    }

    @Override
    public boolean needsRestart(Target target) {
        return isServerNeedsRestart();
    }

    public ProgressObject getCurrentProgressObject() {
        return current;
    }

    public Deployment.Mode getMode() {
        return mode;
    }

    public void setMode(Deployment.Mode mode) {
        this.mode = mode;
    }

    @Override
    public boolean isAlsoTargetServer(Target arg0) {
        return true;
    }

    @Override
    public boolean supportsStartDeploymentManager() {
        return true;
    }

    @Override
    public boolean supportsStartDebugging(Target target) {
        return true;
    }

    @Override
    public boolean supportsStartProfiling(Target target) {
        return true;
    }

    public void resetOnStart() {
        manager.setSelected(null);
    }

    public void startDummyServer() {

    }

    public Project findSelected() {
        if ( manager.isRestartedAction() && manager.getSelected() != null ) {
            return manager.getSelected();
        }
        Project result = null;
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        if ( projects == null ) {
            return result;
        }
        int count = 0;
        Project main = OpenProjects.getDefault().getMainProject();
        if ( main != null && ! Utils.isAppEngineProject(main)) {
            main = null;
        }
        for ( Project p : projects) {
            if ( Utils.isAppEngineProject(p)) {
                count++;
                result = p;
            }
        }
        if ( count == 1 ) {
            return result;
        } else if ( count == 0 ) {
            return main;
        }
        //
        // Ther are more than one web project registered on Gae
        //
        AppEngineDeploymentFactory.LastState selectedState = AppEngineDeploymentFactory.getInstance().getLastStateSelected(manager.getUri());
        AppEngineDeploymentFactory.LastState usedState = AppEngineDeploymentFactory.getInstance().getLastStateUsedModule(manager.getUri());        
        
        FileObject fo =  AppEngineDeploymentFactory.getInstance().getLastSelectedProject(manager.getUri());
        
        result = null;
        if ( fo != null) {
            result =  FileOwnerQuery.getOwner(fo);
        } else {
            result = main;
        }
        
        if ( result != null && usedState != null ) {
            long st = selectedState.getTime();
            long us = usedState.getTime();
            if ( Math.abs(usedState.getTime() - selectedState.getTime()) < 60000  ) {
                result = FileOwnerQuery.getOwner(usedState.getProjDir());
            }
        }
        
        return result;
    }

    @Override
    public ProgressObject startDeploymentManager() {
        Utils.out("--- STARTSERVER: startDeploymentManager() " + " --- " + pname());
        //
        // May be in some cases there is a way to determine a project 
        //
        Project p = findSelected();
        if (p != null) {
            return startDeploymentManager(p);
        }
        resetOnStart(); // set manager.setSelected(null)
        mode = Deployment.Mode.RUN;
        return new AppEngineProgressObject(null,false,Deployment.Mode.RUN );
    }

    public ProgressObject startDeploymentManager(Project project) {
        manager.setSelected(project);        
        Utils.out("--- STARTSERVER: startDeploymentManager(Project) " + " --- " + project.getProjectDirectory().getName());
        serverNeedsRestart = false;
//        extendedMode = AppEngineServerMode.NORMAL;
        return start(mode = Deployment.Mode.RUN);
    }


    @Override
    public ProgressObject startDebugging(Target target) {
        Utils.out("--- STARTSERVER startDebugging(Target) ------------");
        Project p = findSelected();
        if (p != null) {
            return startDebugging(target,p);
        }
        return new AppEngineProgressObject(null, true, mode);
    }

    public ProgressObject startDebugging(Target target, Project project) {
        Utils.out("--- STARTSERVER BEFORE START startDebugging(Target,Project) " + " --- " + pname());
        manager.setSelected(project);
        serverNeedsRestart = false;
        return start(mode = Deployment.Mode.DEBUG);
    }

    public ProgressObject startProfiling(Target target, Project project) {
        Utils.out("--- STARTSERVER BEFORE START startProfiling(Project) " + " --- " + pname());
        manager.setSelected(project);
        serverNeedsRestart = false;
        return start(mode = Deployment.Mode.PROFILE);
    }

    @Override
    public ProgressObject startProfiling(Target target) {
        Project p = findSelected();
        return startProfiling(target,p);
    }

    @Override
    public ProgressObject stopDeploymentManager() {
        Utils.out("--- STARTSERVER stopDeploymentManager" + " --- " + pname());

        Process process = manager.getProcess();
        ExecutorService executor = manager.getExecutor();

        // Kill process
        if (null != process) {
            // Kill process
            ExternalProcessSupport.destroy(process, new HashMap<String, String>());
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            // Set to null
            manager.setProcess(null);
        }

        // Shutdown executor
        if (null != executor) {
            // Shutdown
            executor.shutdownNow();
            // Set to null
            manager.setExecutor(null);
        }

        mode = Deployment.Mode.RUN;
        return (current = new AppEngineProgressObject(manager.getTargetModuleID(), false, mode));
    }

    @Override
    public boolean needsStartForConfigure() {
        return false;
    }

    @Override
    public boolean needsStartForTargetList() {
        return false;
    }

    @Override
    public boolean needsStartForAdminConfig() {
        return false;
    }

    @Override
    public boolean isRunning() {

        Process process = manager.getProcess();

        // Wait due to performance
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            // Nothing to do
        }

        try {
            if (null == process) {
                return false;
            }
        } catch (IllegalThreadStateException ex) {
            // Nothing to do
        }
        return true;
    }

    @Override
    public boolean isDebuggable(Target target) {
        String s = null;
        // It's not in debug mode
        if (!isRunning() || null == mode || mode == Deployment.Mode.RUN || mode == Deployment.Mode.PROFILE) {
            return false;
        }
        // It's in debug mode
        return true;
    }

    @Override
    public ServerDebugInfo getDebugInfo(Target target) {
        return new ServerDebugInfo(
                manager.getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.PROPERTY_HOST),
                Integer.parseInt(manager.getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.DEBUG_PORT_NUMBER)));
    }

    /**
     * Called from the {@code StartDeploymentManager}
     *
     * @param mode
     * @return
     */
    private ProgressObject start(Deployment.Mode mode) {
        Utils.out("--- STARTSERVER start" + " --- ");
        current = AppEngineDeployer.getInstance(manager, mode, manager.getSelected());
        ((AppEngineDeployer) current).deploy();
        return current;

    }

    private Project requestSelected(Deployment.Mode mode) {
        Project[] projects = AppEnginePluginUtils.getAppEngineProjects(manager.getUri());
        if (projects.length == 0) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(AppEngineStartServer.class, "MSG_NoProjectWarning")));
        } else if (projects.length == 1) {
            return projects[0];
        } else if (projects.length > 1) {
            // Get main project
            Project main = OpenProjects.getDefault().getMainProject();

            // If there is a main app engine project deploy it
            for (Project project : projects) {
                if (project.equals(main)) {
                    return project;
                }
            }

            // Create dialog
            Object result = DialogDisplayer.getDefault().notify(new DialogDescriptor(new AppEngineProjectChooser(manager),
                    NbBundle.getMessage(AppEngineStartServer.class, "MSG_Chooser")));

            // Result
            if (NotifyDescriptor.OK_OPTION.equals(result)) {
                return manager.getSelected();
            }
        }
        return null;
    }
}
