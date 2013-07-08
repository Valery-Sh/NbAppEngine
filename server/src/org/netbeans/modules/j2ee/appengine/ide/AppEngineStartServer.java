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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.AppEngineProgressObject;
import org.netbeans.modules.j2ee.appengine.MyLOG;
import org.netbeans.modules.j2ee.appengine.ui.AppEngineProjectChooser;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginUtils;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
//import org.netbeans.modules.profiler.actions.StopAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
    private AppEngineServerMode mode;

    private AppEngineStartServer(AppEngineDeploymentManager manager) {
        this.manager = manager;

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

    @Override
    public boolean needsRestart(Target target) {
    //    return false;
        return manager.isServerNeedsRestart();
    }

    public ProgressObject getCurrentProgressObject() {
        //ProgressObject pp;

        return current;
    }

    public AppEngineServerMode getMode() {
        return mode;
    }

    public void setMode(AppEngineServerMode mode) {
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

    @Override
    public ProgressObject startDeploymentManager() {
        return start(mode = AppEngineServerMode.NORMAL);
    }

    @Override
    public ProgressObject startDebugging(Target target) {
        return start(mode = AppEngineServerMode.DEBUG);
    }

    @Override
    public ProgressObject startProfiling(Target target) {
        return start(mode = AppEngineServerMode.PROFILE);
    }

    @Override
    public ProgressObject stopDeploymentManager() {
MyLOG.log("PRRRRRRRRRRRRRRRRRRR stopDeploymentManager isProfilingNeedsStop:" + manager.isProfilingNeedsStop());
        if (manager.isProfilingNeedsStop()) {
//            StopAction action = StopAction.getInstance();
//            if (action.isEnabled()) {
MyLOG.log("PRRRRRRRRRRRRRRRRRRR stopDeploymentManager isProfilingNeedsStop==TRUU performAction");
                
               // action.performAction();
//            }
        }
        Process process = manager.getProcess();
        ExecutorService executor = manager.getExecutor();
MyLOG.log("PRRRRRRRRRRRRRRRRRRR KILL PROCESS ");

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
        
        if ( ! manager.isServerNeedsRestart() ) {
            manager.setSelected(null);
        }
        manager.setServerNeedsRestart(false);
        this.mode = AppEngineServerMode.NORMAL;
        return (current = new AppEngineProgressObject(manager.getModule(), false, mode));
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
        if (!isRunning() || null == mode || mode == AppEngineServerMode.NORMAL || mode == AppEngineServerMode.PROFILE) {
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
    private ProgressObject start(AppEngineServerMode mode) {
        if ( manager.getSelected() == null ) {
            Project p = requestSelected(AppEngineServerMode.NORMAL);
            manager.setSelected(p);
        }
        if ( manager.getSelected() == null ) {
            //Fail
            return new AppEngineProgressObject(manager.getModule(), true, mode);
        }
        
        current = AppEngineDeployer.getInstance(manager, mode, manager.getSelected());
        ((AppEngineDeployer) current).deploy();
        return current;

    }

    private Project requestSelected(AppEngineServerMode mode) {
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
    
    
