package org.netbeans.modules.j2ee.appengine.ui;

import org.openide.filesystems.FileObject;

/**
 *
 * @author Valery
 */
public interface AppEngineSelectedProject {
    FileObject getProjectDirectory();
    void setProjectDirectory(FileObject projectDirectory);
    FileObject getDeployedProjectDirectory();
    void setDeployedProjectDirectory(FileObject deployedProjectDirectory);
    
}
