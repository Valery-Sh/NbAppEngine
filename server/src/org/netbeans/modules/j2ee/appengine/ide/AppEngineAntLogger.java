/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.j2ee.appengine.ide;

import java.io.File;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentFactory;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.MyLOG;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = AntLogger.class, position = 10)
public class AppEngineAntLogger extends AntLogger {

    /**
     * Default constructor for lookup
     */
    public AppEngineAntLogger() {
        MyLOG.log("%%% CREATE ANT LOGGER");
    }
    
    protected boolean isAppEngineProject(File file) {
        return AppEnginePluginUtils.isAppEngineProject(file);
        //return true;
    }
    
    @Override
    public boolean interestedInScript(File script, AntSession session) {
        return true;
/*        if (!script.getName().equals("build-impl.xml")) {
            MyLOG.log("%%% AntLogger.interestedInScript=FALSE 0 " + script.getPath());
            return false;
        }
        File parent = script.getParentFile();
        if (parent != null && parent.getName().equals("nbproject")) { // NOI18N
            File parent2 = parent.getParentFile();
            if (parent2 != null) {
                return isAppEngineProject(parent2);
            }
        }
        return false;
        */ 
    }
    
    @Override
    public boolean interestedInSession(AntSession session) {
        return true;
    }
    
    @Override
    public String[] interestedInTargets(AntSession session) {
        //this.targetStarted(null);
        return AntLogger.NO_TASKS;
    }

    @Override
    public String[] interestedInTasks(AntSession session) {
        //this.targetStarted(null);
        return AntLogger.NO_TASKS;
    }
    
    @Override
    public void taskStarted(AntEvent event) {
        
        File buildXml = event.getScriptLocation();
        MyLOG.log("%%% taskStarted buildXml=" + buildXml.getPath()
                + "; target=" + event.getTargetName() + "; task=" + event.getTaskName());
    }
    
    @Override
    public void buildStarted(AntEvent event) {

        File buildXml = event.getScriptLocation();
        MyLOG.log("%%% buildStarted buildXml=" + buildXml.getPath() + "; name=" + buildXml.getName());                
        if ( ! "build.xml".equals(buildXml.getName())) {
        MyLOG.log("%%% buildStarted RETURN is NOT build.xml ");                
            
            return;
        }
        if ( ! isAppEngineProject(buildXml)) {
            return;
        }
        Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(buildXml));
        try {
            AppEngineDeploymentManager dm = (AppEngineDeploymentManager) AppEngineDeploymentFactory.getInstance().getDeploymentManager("deployer:appengine:localhost:8181/-1198750328", null, null);
            Project dp = dm.getSelected();
MyLOG.log("%%% buildStarted  dm.prj=" + dp);            
            if ( ! p.equals(dp) ) {
MyLOG.log("%%% buildStarted  (! p.equals(dp)) == TRUE; prj=" + p);
                
                dm.setSelected(p);
            }
        } catch (DeploymentManagerCreationException dmce) {
        MyLOG.log("%%% buildStarted EXCEPTION");
            
        }        
        MyLOG.log("%%% buildStarted buildXml=" + buildXml.getPath()
                + "; target=" + event.getTargetName() + "; task=" + event.getTaskName());
    }
}
