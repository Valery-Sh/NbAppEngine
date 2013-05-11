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
//import org.myant.AppEngineHelper;
//import org.myant.AppEngineInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentFactory;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.MyLOG;
import org.openide.filesystems.FileUtil;
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
    }

    @Override
    public boolean interestedInSession(AntSession session) {
        return true;
    }

    @Override
    public String[] interestedInTargets(AntSession session) {
        return new String[] {"-run-deploy-nb","debug","-do-profile"};
//        return AntLogger.ALL_TARGETS;
    }

    @Override
    public String[] interestedInTasks(AntSession session) {
        //this.targetStarted(null);
        return AntLogger.NO_TASKS;
    }

    @Override
    public void targetStarted(AntEvent event) {
        MyLOG.log("%%% targetStarted targetName=" + event.getTargetName());
        //TODO delete after testing. Only those targets defined in interestedInTargets
        if ( !( "debug".equals(event.getTargetName()) 
                || "-run-deploy-nb".equals(event.getTargetName())
                || "-do-profile".equals(event.getTargetName()) ) ) {
            return;
        }
        
        File buildXml = event.getScriptLocation();
        MyLOG.log("%%% buildStarted buildXml=" + buildXml.getPath() + "; name=" + buildXml.getName());
        if (!"build-impl.xml".equals(buildXml.getName())) {
            MyLOG.log("%%% buildStarted RETURN is NOT build.xml ");
            return;
        }
        if ( event.getProperty("appengine.location") == null ) {
            MyLOG.log("%%% buildStarted IS NOT AppEngine");
            //if not appengine project then return
            return;
        }
/*        if (!AppEnginePluginUtils.isAppEngineProject(buildXml)) {
            return;
        }
*/ 
        Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(buildXml));
/*        Collection c = p.getLookup().lookupAll(Object.class);
        for ( Object o : c ) {
            MyLOG.log("=== LOOKUP Obj=" + o.getClass().getSimpleName());
        }
*/ 
        try {
            AppEngineDeploymentManager dm = (AppEngineDeploymentManager) AppEngineDeploymentFactory.getInstance().getDeploymentManager(event.getProperty("appengine.manager.uri"), null, null);
            Project currentSelected = dm.getSelected();
            if (currentSelected != null) {
                MyLOG.log("%%% buildStarted  dm.getSelected()=" + currentSelected.getProjectDirectory().getName());
            } else {
                MyLOG.log("%%% buildStarted  dm.getSelected() = NULL");
            }
            if (event.getProperty("ignore.ant.logger") != null) {
                // When target is called by ActionUtils.runTarget from our code
                MyLOG.log("%%% buildStarted  ignore.ant.logger NOT NULL");
                return;
            }
            if (event.getProperty("is.debugged") != null) {
                MyLOG.log("%%% buildStarted  is.debugged NOT NULL we do dm.setDebugedSet(true)");
                dm.setDebuggedSet(true);
            } else {
                MyLOG.log("%%% buildStarted  is.debugged NULL we do dm.setDebugedSet(false)");
                dm.setDebuggedSet(false);
            }
            dm.setProfilingNeedsStop(false);
            if ( ! "-do-profile".equals(event.getTargetName()) ) {
                if ( dm.getServerMode() == AppEngineServerMode.PROFILE ) {
                MyLOG.log("%%% buildStarted SET NEEDS PROFILE STOP");
                    dm.setProfilingNeedsStop(true);
                }
            }
            //====================================================
            dm.setServerNeedsRestart(false);            
            
            if ( ! dm.isServerRunning() ) {
                dm.setServerNeedsRestart(true);
                dm.setSelected(p);
                return;
            }
            //
            // server is allready running
            //
            if (!p.equals(currentSelected)) {
                MyLOG.log("%%% buildStarted  selected != oldSelected and new selected=" + p);

                dm.setServerNeedsRestart(true);
                //dm.setOldSelected(dm.getSelected());
                dm.setSelected(p);
                return;
            }          
            //
            // server is allready running and same selected
            //
            AppEngineServerMode currentMode = dm.getServerMode();
            AppEngineServerMode newMode = AppEngineServerMode.NORMAL;
            if ( "debug".equals(event.getTargetName()) ) {
                newMode = AppEngineServerMode.DEBUG;
            } else if ( "-do-profile".equals(event.getTargetName()) ) {
                newMode = AppEngineServerMode.PROFILE;
            }
            
            if ( currentMode != newMode || newMode == AppEngineServerMode.PROFILE) {
                dm.setServerNeedsRestart(true);
            } 
            
/*            if ( currentMode == AppEngineServerMode.NORMAL ) {
                
            } else if ( currentMode == AppEngineServerMode.DEBUG ) {
                
            } else if ( currentMode == AppEngineServerMode.PROFILE ) {
                
            }
            //====================================================
            if (!p.equals(currentSelected)) {
                MyLOG.log("%%% buildStarted  selected != oldSelected and new selected=" + p);

                dm.setServerNeedsRestart(true);
                //dm.setOldSelected(dm.getSelected());
                dm.setSelected(p);
            } else {
                
                MyLOG.log("%%% buildStarted  selected == oldSelected=" + p);
                MyLOG.log("%%% buildStarted  targetName=" + event.getTargetName());
                if ( "debug".equals(event.getTargetName()) && dm.isDebuggedSet() ) {
                MyLOG.log("%%% ____  needsRestart=FALSE");
                    
                    dm.setServerNeedsRestart(false);
                    //dm.setOldSelected(dm.getSelected());
                } else if ( ! dm.isDebuggedSet() ) {
                MyLOG.log("1) %%% ____  needsRestart=TRUE");
                    
                    dm.setServerNeedsRestart(true);
                    //dm.setOldSelected(dm.getSelected());
                } else if ( "-run-deploy-nb".equals(event.getTargetName()) && dm.isDebuggedSet() ) {
                MyLOG.log("2) %%% ____  needsRestart=TRUE");
                    dm.setServerNeedsRestart(true);
                    //dm.setOldSelected(dm.getSelected());
                }
            }
*/
        } catch (DeploymentManagerCreationException dmce) {
            MyLOG.log("%%% buildStarted EXCEPTION");

        }
    }

    @Override
    public void taskStarted(AntEvent event) {

        File buildXml = event.getScriptLocation();
        MyLOG.log("%%% taskStarted buildXml=" + buildXml.getPath()
                + "; target=" + event.getTargetName() + "; task=" + event.getTaskName());
    }

/*    @Override
    public void buildStarted(AntEvent event) {
    }
    */ 
}
