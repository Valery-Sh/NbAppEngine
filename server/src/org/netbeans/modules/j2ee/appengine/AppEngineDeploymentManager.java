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
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.appengine.ide.AppEngineDeployer;
import org.netbeans.modules.j2ee.appengine.ide.AppEngineJ2eePlatformImpl;
import org.netbeans.modules.j2ee.appengine.ide.AppEngineLogger;
import org.netbeans.modules.j2ee.appengine.ide.AppEngineServerMode;
import org.netbeans.modules.j2ee.appengine.ide.AppEngineStartServer;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
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
    //private Project selectedByAntLogger;
    private boolean debuggedSet;
    private boolean serverNeedsRestart;    
    private boolean profilingNeedsStop;    
    

    public AppEngineDeploymentManager(String uri) {
        this.uri = uri;
        this.properties = new AppEnginePluginProperties(this);
        this.target = new AppEngineTarget(getProperties().getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR));
        this.logger = AppEngineLogger.getInstance(uri);
    }

    public String getUri() {
        return uri;
    }

    public AppEngineModule getModule() {
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

    /**
     *
     * @return
     */
    public ProgressObject getProgress() {
        AppEngineStartServer startServer = AppEngineStartServer.getInstance(this);
        if (startServer.getMode() == AppEngineServerMode.DEBUG && startServer.isRunning() ) {
            progress = new AppEngineProgressObject(getModule(), false, AppEngineServerMode.DEBUG);            
        } else
        if (null == progress) {
            progress = new AppEngineProgressObject(getModule(), false, AppEngineServerMode.NORMAL);
        }

        return progress;
    }
    
    public AppEngineServerMode getServerMode() {
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

    public boolean isDebuggedSet() {
        return debuggedSet;
    }

    public void setDebuggedSet(boolean debuggedSet) {
        this.debuggedSet = debuggedSet;
    }

    public boolean isProfilingNeedsStop() {
        return profilingNeedsStop;
    }

    public void setProfilingNeedsStop(boolean profilingNeedsStop) {
        this.profilingNeedsStop = profilingNeedsStop;
    }
    
    public boolean isServerNeedsRestart() {
        return serverNeedsRestart;
    }

    public void setServerNeedsRestart(boolean needsRestart) {
        this.serverNeedsRestart = needsRestart;
    }

/*    public Project getOldSelected() {
        return oldSelected;
    }

    public void setOldSelected(Project oldSelected) {
        this.oldSelected = oldSelected;
    }
*/
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
        return new TargetModuleID[]{getModule()};
    }

    @Override
    public TargetModuleID[] getNonRunningModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
        return new TargetModuleID[]{getModule()};
    }

    @Override
    public TargetModuleID[] getAvailableModules(ModuleType arg0, Target[] arg1) throws TargetException, IllegalStateException {
        AppEngineModule m = getModule();
        String s = m == null ? "NULL" : m.getModuleID();
        if (m == null) {
            return null;
        }
        return new TargetModuleID[]{m};
    }

    @Override
    public DeploymentConfiguration createConfiguration(DeployableObject arg0) throws InvalidModuleException {
        return null;
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
        MyLOG.log("AppEngineDeploymentManager.DISTRIBUTE aFILE=" + fn);
        AppEngineStartServer startServer = AppEngineStartServer.getInstance(this);
     /*   if (startServer.getMode() == AppEngineServerMode.DEBUG && startServer.isRunning() ) {
        MyLOG.log("AppEngineDeploymentManager.DISTRIBUTE before return startServer.getCurrentProgressObject(); =" + startServer.getCurrentProgressObject().getClass());
            return getProgress();
        }
     */ 
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

        // Get progress object
        ProgressObject p = startServer.getCurrentProgressObject();
        // If the deployer wasn't invoked return
        if (!(p instanceof AppEngineDeployer)) {
            MyLOG.log("AppEngineDeploymentManager distribute NOT instanceof AppEngineDeployer");
            return p;
        }
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

/*        FileUtil.toFileObject(file);
        Project newSelected = FileOwnerQuery.getOwner(FileUtil.toFileObject(file));
        if (selected.equals(oldSelected)) {
//        if (newSelected.equals(selected)) {
            MyLOG.log("AppEngineDeploymentManager.distribute the same as deployed project return");
            return p;
        }
        MyLOG.log("AppEngineDeploymentManager.distribute NOT the same as deployed project return.  startServer.isRunning=" + startServer);
*/  
        return getProgress();
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
        MyLOG.log("AppEngineDeploymentManager.START(arg0) moduleID="
                + arg0[0].getModuleID() + "; webURL" + arg0[0].getWebURL());
        return getProgress();
    }

    @Override
    public ProgressObject stop(TargetModuleID[] arg0) throws IllegalStateException {
        MyLOG.log("AppEngineDeploymentManager.STOP ");
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

}
