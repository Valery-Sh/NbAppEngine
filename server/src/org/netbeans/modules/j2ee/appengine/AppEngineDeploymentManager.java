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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.appengine.ide.AppEngineDeployer;
import org.netbeans.modules.j2ee.appengine.ide.AppEngineJ2eePlatformImpl;
import org.netbeans.modules.j2ee.appengine.ide.AppEngineLogger;
import org.netbeans.modules.j2ee.appengine.ide.AppEngineServerMode;
import org.netbeans.modules.j2ee.appengine.ide.AppEngineStartServer;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginUtils;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentManager2;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * @author Michal Mocnak
 */
public class AppEngineDeploymentManager implements DeploymentManager {

    private final String uri;
    private final AppEngineTarget target;
    private final AppEngineLogger logger;
    private AppEngineModule module;
    private ProgressObject progress;
    private AppEnginePluginProperties properties;
    private AppEngineJ2eePlatformImpl platform;
    private Process process;
    private ExecutorService executor;
    private Project selected;

    public AppEngineDeploymentManager(String uri) {
        this.uri = uri;
        this.properties = new AppEnginePluginProperties(this);
        this.target = new AppEngineTarget(getProperties().getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR));
        this.logger = AppEngineLogger.getInstance(uri);
    }

    public String getUri() {
        return uri;
    }

    /*    public AppEngineModule createModule() {
     return new AppEngineModule(getTarget(),
     getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.PROPERTY_HOST),
     Integer.valueOf(getProperties().getInstanceProperties().getProperty(InstanceProperties.HTTP_PORT_NUMBER)),
     "NULL_NULL_123987");

     }
     */
    public AppEngineModule getModule() {
//My        if (null == module) {
        String c = "null";
        if (selected != null) {
            c = selected.getProjectDirectory().getName();
        } else {
            return null;
        }
        module = new AppEngineModule(getTarget(),
                getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.PROPERTY_HOST),
                Integer.valueOf(getProperties().getInstanceProperties().getProperty(InstanceProperties.HTTP_PORT_NUMBER)),
                c);

        return module;
    }

    public ProgressObject getProgress() {
        if (null == progress) {
            progress = new AppEngineProgressObject(getModule(), false, AppEngineServerMode.NORMAL);
        }

        return progress;
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
        MyLOG.log("AppEngineDeploymentManager.getRunningModules");
        return new TargetModuleID[]{getModule()};
    }

    @Override
    public TargetModuleID[] getNonRunningModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
        MyLOG.log("AppEngineDeploymentManager.getNonRunningModules");
        return new TargetModuleID[]{getModule()};
    }

    @Override
    public TargetModuleID[] getAvailableModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
        AppEngineModule m = getModule();
        String s = m == null ? "NULL" : m.getModuleID();
        MyLOG.log("AppEngineDeploymentManager.getAvailableModules moduleID=" + s);
        //return null;
        if (m == null) {
            return null;
        }
        //this.ge
        return new TargetModuleID[]{m};
        //My RESTORE !!! return new TargetModuleID[]{getModule()};
    }

    @Override
    public DeploymentConfiguration createConfiguration(DeployableObject arg0) throws InvalidModuleException {
        MyLOG.log("AppEngineDeploymentManager.createConfiguration");
        return null;
    }

    private ProgressObject runServer(File file) {
        String fn = file == null ? "NULL" : file.getName(); //My to delete
        MyLOG.log("AppEngineDeploymentManager.runServer aFILE=" + fn);

        AppEngineStartServer startServer = AppEngineStartServer.getInstance(this);

        Project newSelected = null;
        if (file != null) {
            FileUtil.toFileObject(file);
            newSelected = FileOwnerQuery.getOwner(FileUtil.toFileObject(file));
        } else if (selected != null) {
            newSelected = selected;
        } else {
            // Try a DIALOG
        }


        this.selected = newSelected;

        return startServer.startDeploymentManager(selected);

    }

    /**
     * 15.04.2013 the copy before NbAppEngine-15-04-2013.zip
     *
     * @param target
     * @param file
     * @param plan
     * @return
     * @throws IllegalStateException
     */
    @Override
    public ProgressObject distribute(Target[] target, File file, File plan) throws IllegalStateException {
        String fn = file == null ? "NULL" : file.getName(); //My to delete
        MyLOG.log("AppEngineDeploymentManager.distribute aFILE=" + fn);
        AppEngineStartServer startServer = AppEngineStartServer.getInstance(this);
        if (startServer.getMode() == AppEngineServerMode.DEBUG && !startServer.isRunning() ) {
            return runServer(file);
/*            runServer(file);
            while ((!logger.contains("Dev App Server is now running"))
                    && (!logger.contains("The server is running"))
                    && !logger.contains("Address already in use")) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
*/
        }

        if (!startServer.isRunning()) {
            return runServer(file);
        }
        //MyLOG.log("AppEngineDeploymentManager distribute FILE FILE");                
        // Wait to Google App Engine will be initialized
        while ((!logger.contains("Dev App Server is now running"))
                && (!logger.contains("The server is running"))
                && !logger.contains("Address already in use")) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // Get start server object
        // Get progress object
        ProgressObject p = startServer.getCurrentProgressObject();
        // If the deployer wasn't invoked return
        if (!(p instanceof AppEngineDeployer)) {
            MyLOG.log("AppEngineDeploymentManager distribute NOT instanceof AppEngineDeployer");
            return p;
        }
//Project pp = null;        
//ActionProvider ap = pp.getLookup().lookup(ActionProvider.class);
//ap.
        // Get checksums
/* //My        String checksum = AppEnginePluginUtils.getMD5Checksum(file);
         String checksumToCompare = ((AppEngineDeployer) p).getWarChecksum();
         // If the same as deployed project return
         if (null != checksum && null != checksumToCompare
         && checksum.equals(checksumToCompare)
         || !startServer.getMode().equals(AppEngineServerMode.NORMAL)) {
         MyLOG.log("AppEngineDeploymentManager.distribute the same as deployed project return");
         return p;
         }
         */

        FileUtil.toFileObject(file);
        Project newSelected = FileOwnerQuery.getOwner(FileUtil.toFileObject(file));

        if (newSelected.equals(selected)) {
            MyLOG.log("AppEngineDeploymentManager.distribute the same as deployed project return");
            return p;
        }
        MyLOG.log("AppEngineDeploymentManager.distribute NOT the same as deployed project return.  startServer.isRunning=" + startServer);
        // If not stop server
        if (startServer.isRunning()) {
            MyLOG.log("AppEngineDeploymentManager.distribute stopDeploymentManager");
            //My setSelected(null);
            startServer.stopDeploymentManager();
        }

        

        this.selected = newSelected;
        if (startServer.getMode() == AppEngineServerMode.DEBUG) {
MyLOG.log("AppEngineDeploymentManager.distribute startServer.startDebugging(selected)");            
            return startServer.startDebugging(selected);
        }
MyLOG.log("AppEngineDeploymentManager.distribute startDeploymentManager(selected) before return");
        return startServer.startDeploymentManager(selected);
    }

    /*    @Override
     public ProgressObject distribute(Target[] target, File file, File plan) throws IllegalStateException {
     String fn = file == null ? "NULL" : file.getName();
     MyLOG.log("AppEngineDeploymentManager.distribute aFILE=" + fn);
     //MyLOG.log("AppEngineDeploymentManager distribute FILE FILE");                
     // Wait to Google App Engine will be initialized
     AppEngineStartServer startServer = AppEngineStartServer.getInstance(this);
     if (startServer.isRunning()) {
     while ((!logger.contains("Dev App Server is now running"))
     && (!logger.contains("The server is running"))
     && !logger.contains("Address already in use")) {
     try {
     Thread.sleep(1000);
     } catch (InterruptedException ex) {
     Exceptions.printStackTrace(ex);
     }
     }
     }

     // Get start server object
     //        AppEngineStartServer startServer = AppEngineStartServer.getInstance(this);

     // Get progress object
     ProgressObject p = startServer.getCurrentProgressObject();

     // If the deployer wasn't invoked return
     //        if (!(p instanceof AppEngineDeployer)) {
     //            MyLOG.log("AppEngineDeploymentManager distribute NOT instanceof AppEngineDeployer");
     //            return p;
     //        }

     // Get checksums
     //        String checksum = AppEnginePluginUtils.getMD5Checksum(file);
     //        String checksumToCompare = ((AppEngineDeployer) p).getWarChecksum();

     // If the same as deployed project return
     //        if (null != checksum && null != checksumToCompare && checksum.equals(checksumToCompare)
     //                || !startServer.getMode().equals(AppEngineServerMode.NORMAL)) {
     //            MyLOG.log("AppEngineDeploymentManager.distribute the same as deployed project return");
     //            return p;
     //        }
     MyLOG.log("AppEngineDeploymentManager.distribute NOT the same as deployed project return.  startServer.isRunning=" + startServer);
     // If not stop server
     if (startServer.isRunning()) {
     MyLOG.log("AppEngineDeploymentManager.distribute stopDeploymentManager");

     startServer.stopDeploymentManager();
     }
     MyLOG.log("AppEngineDeploymentManager.distribute startDeploymentManager before return");
     //this.selected = 
     // Redeploy in normal mode
     FileUtil.toFileObject(file);
     Project p1 = FileOwnerQuery.getOwner(FileUtil.toFileObject(file));	        
     this.selected = p1;
     return startServer.startDeploymentManager();
     }
     */
    @Override
    public ProgressObject distribute(Target[] arg0, InputStream arg1, InputStream arg2) throws IllegalStateException {
        MyLOG.log("AppEngineDeploymentManager.distribute STREAM STREAM");
        return getProgress();
    }

    @Override
    public ProgressObject distribute(Target[] arg0, ModuleType arg1, InputStream arg2, InputStream arg3) throws IllegalStateException {
        MyLOG.log("AppEngineDeploymentManager.distribute MODULE ");
        return getProgress();
    }

    @Override
    public ProgressObject start(TargetModuleID[] arg0) throws IllegalStateException {
        MyLOG.log("AppEngineDeploymentManager.start(arg0) moduleID="
                + arg0[0].getModuleID() + "; webURL" + arg0[0].getWebURL());
        return getProgress();
    }

    @Override
    public ProgressObject stop(TargetModuleID[] arg0) throws IllegalStateException {
        MyLOG.log("AppEngineDeploymentManager.stop ");
        return getProgress();
    }

    @Override
    public ProgressObject undeploy(TargetModuleID[] arg0) throws IllegalStateException {
        MyLOG.log("AppEngineDeploymentManager.undeploy ");
        return getProgress();
    }

    @Override
    public boolean isRedeploySupported() {
        return false;
        //My return false;
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] arg0, File arg1, File arg2) throws UnsupportedOperationException, IllegalStateException {
        MyLOG.log("AppEngineDeploymentManager.redeploy 0 ");
        return getProgress();
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] arg0, InputStream arg1, InputStream arg2) throws UnsupportedOperationException, IllegalStateException {
        MyLOG.log("AppEngineDeploymentManager.redeploy 1");
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    @Override
    public ProgressObject distribute(Target[] targets, DeploymentContext deployment) {
        MyLOG.log("&&&&& AppEngineDeploymentManager2.distribute");
        return null;
    }
}
