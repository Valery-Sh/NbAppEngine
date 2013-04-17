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
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.AppEngineProgressObject;
import org.netbeans.modules.j2ee.appengine.MyLOG;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;

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
        MyLOG.log("AppEngineStartServer CONSTRUCTOR");
        this.manager = manager;
        manager.setSelected(null);
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

    public ProgressObject getCurrentProgressObject() {
        return current;
    }

    public AppEngineServerMode getMode() {
        return mode;
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
        MyLOG.log("AppEngineStartServer.startDeploymentManager() : AppEngineServerMode.NORMAL");
        return start(mode = AppEngineServerMode.NORMAL);
    }

    public ProgressObject startDeploymentManager(Project selected) {
        MyLOG.log("AppEngineStartServer.startDeploymentManager(selected) : AppEngineServerMode.NORMAL selected" + selected.getProjectDirectory().getName());
        return start(mode = AppEngineServerMode.NORMAL, selected);
    }

    @Override
    public ProgressObject startProfiling(Target target) {
        return start(mode = AppEngineServerMode.PROFILE);
    }

    @Override
    public ProgressObject stopDeploymentManager() {
        Process process = manager.getProcess();
        MyLOG.log("AppStartServer.stopDeploymentManager() : process=" + (process == null ? "NULL" : "NOT NULL"));
        ExecutorService executor = manager.getExecutor();

        // Kill process
        if (null != process) {

            MyLOG.log("AppStartServer.stopDeploymentManager() : call destroy");
            // Kill process
            ExternalProcessSupport.destroy(process, new HashMap<String, String>());
            /*            try {
             process.waitFor();
             } catch (InterruptedException ex) {
             MyLOG.log("AppStartServer.stopDeploymentManager() : waitFor interrupted");                                        
             Exceptions.printStackTrace(ex);
             }
             MyLOG.log("AppStartServer.stopDeploymentManager() : DESTROYED");                                                    
             */
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

        return (current = new AppEngineProgressObject(manager.getModule(), false));
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
//Logger.getLogger(getClass().getName()).log(Level.WARNING, "+++++ isRunning = false --------------------------------");                
                MyLOG.log("-- AppEngineStartServer.isRunning=false; process==null");
                return false;
            }
        } catch (IllegalThreadStateException ex) {
            // Nothing to do
        }
        MyLOG.log("-- AppEngineStartServer.isRunning=true; process != null");
        return true;
    }

    @Override
    public boolean isDebuggable(Target target) {
        // It's not in debug mode
        if (!isRunning() || null == mode || mode.equals(AppEngineServerMode.NORMAL)) {
            return false;
        }

        // It's in debug mode
        return true;
    }

    @Override
    public ProgressObject startDebugging(Target target) {
        return start(mode = AppEngineServerMode.DEBUG);
    }

    @Override
    public ServerDebugInfo getDebugInfo(Target target) {
        return new ServerDebugInfo(
                manager.getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.PROPERTY_HOST),
                Integer.parseInt(manager.getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.DEBUG_PORT_NUMBER)));
    }
    /*        Collection<? extends ModuleConfiguration> cc = Lookup.getDefault().lookupAll(AppEngineModuleConfiguration.class);        
     for ( ModuleConfiguration mc : cc) {
     try {
     MyLOG.log("CCOLLECTION  " + mc.getJ2eeModule().getArchive());
     } catch (IOException ex) {
     Exceptions.printStackTrace(ex);
     }
     }
     */

    /*    private ProgressObject start(AppEngineServerMode mode) {
     if (manager.getSelected() == null && !isRunning()) {
     return (current = new AppEngineProgressObject(manager.getModule(), false));
     }
     Project selected = manager.getSelected();
     MyLOG.log("FOUND SELECTED ");
     current = AppEngineDeployer.getInstance(manager, mode, selected);
     ((AppEngineDeployer) current).deploy();

     //return (current = new AppEngineDeployer(manager, mode, selected));
     return current;

     }
     */
    /**
     * Called from the {@code StartDeploymentManager}
     *
     * @param mode
     * @return
     */
    private ProgressObject start(AppEngineServerMode mode) {
        //Myif (manager.getSelected() == null && !isRunning()) {
        if (!isRunning()) {
            MyLOG.log("AppEngineStartServer.start() STUB NOT RUNNING ");
            //return (current = new AppEngineProgressObject(manager.createModule(), false));
            return (current = new AppEngineProgressObject(null, false));
        }

        MyLOG.log("FOUND SELECTED 0");
        if (manager.getSelected() != null) {
            MyLOG.log("FOUND SELECTED 1");
            current = AppEngineDeployer.getInstance(manager, mode, manager.getSelected());
            ((AppEngineDeployer) current).deploy();
            return current;
        }

        MyLOG.log("AppEngineStartServer.start() start THE VERY END");
        return (current = new AppEngineProgressObject(manager.getModule(), false));
    }

    private ProgressObject start(AppEngineServerMode mode, Project selected) {
        MyLOG.log("AppEngineStartServer.start(mode,selected) selected=" + selected.getProjectDirectory().getName());
        
        current = AppEngineDeployer.getInstance(manager, mode, selected);
        ((AppEngineDeployer) current).deploy();
        //return (current = new AppEngineDeployer(manager, mode, selected));
        return current;
    }
}