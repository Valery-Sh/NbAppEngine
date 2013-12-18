/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.j2ee.appengine.ide;

import java.io.File;
import java.util.Date;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
//import org.myant.AppEngineHelper;
//import org.myant.AppEngineInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.appengine.util.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

//@ServiceProvider(service = AntLogger.class, position = 100)
/**
 * @deprecated used only for debug purpose
 * @author Valery
 */
public class AppEngineAntLogger extends AntLogger {

    /**
     * Default constructor for lookup
     */
    public AppEngineAntLogger() {
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
        //return new String[] {"-profile-check"};
        //return new String[] {"-run-deploy-nb","debug","-profile-check"};
        //return AntLogger.ALL_TARGETS;
        return AntLogger.NO_TARGETS;
    }

    @Override
    public String[] interestedInTasks(AntSession session) {
        
        return AntLogger.NO_TASKS;
        //return AntLogger.ALL_TASKS;
    }

    @Override
    public void targetStarted(AntEvent event) {
        //TODO delete after testing. Only those targets defined in interestedInTargets
        if ( event.getScriptLocation() == null ) {
            return;
        }
        
        String ll = event.getScriptLocation() == null ? "NULL NULL " : event.getScriptLocation().getPath();
        FileObject projFo = FileUtil.toFileObject(new File(ll));
        if ( projFo == null ) {
            return;
        }
        
        Project proj = FileOwnerQuery.getOwner(projFo);
        if ( proj == null ) {
            return;
        }
        
        String pname = proj.getProjectDirectory().getName();
        //Utils.out("SCRIPT LOCATION: " + ll); 
        Utils.out("--- target: " + event.getTargetName() + " --- " + pname + "; time=" + new Date()); 
        
        if ( !( "debug".equals(event.getTargetName()) 
                || "-run-deploy-nb".equals(event.getTargetName())
                //|| "-do-profile".equals(event.getTargetName()) ) ) {
                || "-profile-check".equals(event.getTargetName()) ) ) {
            return;
        }
        
        File buildXml = event.getScriptLocation();
        
        if (!"build-impl.xml".equals(buildXml.getName())) {
            return;
        }
        if ( event.getProperty("appengine.location") == null ) {
            //if not appengine project then return
            return;
        }
/*        Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(buildXml));
        try {
            AppEngineDeploymentManager dm = (AppEngineDeploymentManager) AppEngineDeploymentFactory.getInstance().getDeploymentManager(event.getProperty("appengine.manager.uri"), null, null);
            Project currentSelected = dm.getSelected();
            if (event.getProperty("ignore.ant.logger") != null) {
                // When target is called by ActionUtils.runTarget from our code
                return;
            }
            if (event.getProperty("is.debugged") != null) {
                //dm.setDebuggedSet(true);
            } else {
                //dm.setDebuggedSet(false);
            }
            //dm.setProfilingNeedsStop(false);
            //if ( ! "-do-profile".equals(event.getTargetName()) ) {
            if ( ! "-profile-check".equals(event.getTargetName()) ) {
                if ( dm.getServerMode() == AppEngineServerMode.PROFILE ) {
                    //dm.setProfilingNeedsStop(true);
                }
            }
            //====================================================
            //dm.setServerNeedsRestart(false);            
            if ( ! dm.isServerRunning() ) {
      //13.06          dm.setServerNeedsRestart(true);
                //dm.setSelected(p);
                return;
            }
            //
            // server is allready running
            //
            if (!p.equals(currentSelected)) {
                //dm.setServerNeedsRestart(true);
                //dm.setSelected(p);
                return;
            }          
            //
            // server is allready running and same selected
            //
            AppEngineServerMode currentMode = dm.getServerMode();
            AppEngineServerMode newMode = AppEngineServerMode.NORMAL;
            if ( "debug".equals(event.getTargetName()) ) {
                newMode = AppEngineServerMode.DEBUG;
            } else if ( "-profile-check".equals(event.getTargetName()) ) {
                newMode = AppEngineServerMode.PROFILE;
            }
            
            if ( currentMode != newMode || newMode == AppEngineServerMode.PROFILE) {
                dm.setServerNeedsRestart(true);
            } 
            
        } catch (DeploymentManagerCreationException dmce) {
        }
*/        
    }

    @Override
    public void taskStarted(AntEvent event) {
        if ( event.getScriptLocation() == null ) {
            return;
        }
        String ll = event.getScriptLocation() == null ? "NULL NULL " : event.getScriptLocation().getPath();
        FileObject projFo = FileUtil.toFileObject(new File(ll));
        if ( projFo == null ) {
            return;
        }
        Project proj = FileOwnerQuery.getOwner(projFo);
        if ( proj == null ) {
            return;
        }
        String pname = proj.getProjectDirectory().getName();
        
        Utils.out("------ task: " + event.getTaskName() + " --- " + pname+ "; time=" + new Date()); 
    }

}
