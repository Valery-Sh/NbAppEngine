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
import org.myant.AppEngineHelper;
import org.myant.AppEngineInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentFactory;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.MyLOG;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = AntLogger.class, position = 100)
public class AppEngineAntLogger extends AntLogger {

    /**
     * Default constructor for lookup
     */
    public AppEngineAntLogger() {
        MyLOG.log("%%% CREATE ANT LOGGER");
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
        return AntLogger.NO_TARGETS;
    }

    @Override
    public String[] interestedInTasks(AntSession session) {
        //this.targetStarted(null);
        return AntLogger.NO_TASKS;
    }

    @Override
    public void targetStarted(AntEvent event) {
        if (true) {
            return;
        }
        File buildXml = event.getScriptLocation();
        MyLOG.log("%%% targetStarted target=" + event.getTargetName()
                + "; buildXml= " + buildXml.getPath());
    }

    @Override
    public void taskStarted(AntEvent event) {

        File buildXml = event.getScriptLocation();
        MyLOG.log("%%% taskStarted buildXml=" + buildXml.getPath()
                + "; target=" + event.getTargetName() + "; task=" + event.getTaskName());
    }

    @Override
    public void buildStarted(AntEvent event) {

/*for (String nm : event.getPropertyNames()) {
        MyLOG.log("!!! %%% buildStarted pname==" + nm);
}
*/ 
        //event.getSession().
        File buildXml = event.getScriptLocation();
        MyLOG.log("%%% buildStarted buildXml=" + buildXml.getPath() + "; name=" + buildXml.getName());
        if (!"build.xml".equals(buildXml.getName())) {
            MyLOG.log("%%% buildStarted RETURN is NOT build.xml ");                

            return;
        }
        if (!AppEnginePluginUtils.isAppEngineProject(buildXml)) {
            MyLOG.log("%%% buildStarted IS NOT AppEngine");
            return;
        }
        Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(buildXml));
        MyLOG.log("%%% buildStarted antsrc.cp=" + AppEnginePluginUtils.getProperty(p, "antsrc.cp"));
        MyLOG.log("%%% buildStarted antsrc.cp=" + AppEnginePluginUtils.getProperty(p, "antsrc.cp"));
        for ( String s :event.getPropertyNames() ) {
            MyLOG.log("%%% buildStarted prop key=" + s + "; val=" + event.getProperty(s));
        }
        try {
            AppEngineDeploymentManager dm = (AppEngineDeploymentManager) AppEngineDeploymentFactory.getInstance().getDeploymentManager("deployer:appengine:localhost:8181/-1198750328", null, null);
            Project dp = dm.getSelected();
MyLOG.log("%%% buildStarted  dm.prj=" + dp);            
            AppEngineInfo inst = Lookup.getDefault().lookup(AppEngineInfo.class);
            if (!p.equals(dp)) {
MyLOG.log("%%% buildStarted  call selectedChanging prj=" + p);
                
            //AppEngineHelper.getInstance().setSameProject(false);
            
            //boolean same = AppEngineHelper.getInstance().isSameProject();
            inst.setSameProject(false);
                
/*                dm.selectedChanging(p);
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
*/
MyLOG.log("%%% buildStarted  AFTER call selectedChanging prj=" + p);                
                dm.setSelected(p);
            } else {
                //AppEngineHelper.getInstance().setSameProject(true);
                inst.setSameProject(true);
            }
MyLOG.log("%%% buildStarted  isSameProject=" + inst.isSameProject());                
            
        } catch (DeploymentManagerCreationException dmce) {
            MyLOG.log("%%% buildStarted EXCEPTION");

        }
        //      MyLOG.log("%%% buildStarted buildXml=" + buildXml.getPath()
//                + "; target=" + event.getTargetName() + "; task=" + event.getTaskName());
    }
}
