/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.j2ee.appengine;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.appengine.ui.AppEngineSelectedProject;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Valery
 */
public class MyLOG {
    public static void log(String msg) {
        AppEngineSelectedProject selected = Lookup.getDefault().lookup(AppEngineSelectedProject.class);
        FileObject dir = selected.getProjectDirectory();
        FileObject deployDir = selected.getDeployedProjectDirectory();
        String dirName = dir == null ? null : dir.getName();
        String depName = deployDir == null ? null : deployDir.getName();
        Logger.getLogger(MyLOG.class.getName()).log(Level.WARNING, "+++++ " + msg );                        
    }
}
