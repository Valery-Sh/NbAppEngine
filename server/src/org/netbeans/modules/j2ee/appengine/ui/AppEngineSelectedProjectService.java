package org.netbeans.modules.j2ee.appengine.ui;

import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author V. Shyshkin
 */
@ServiceProvider(service=AppEngineSelectedProject.class)
public class AppEngineSelectedProjectService implements AppEngineSelectedProject{
    
    private FileObject projectDirectory;
    private FileObject deployedProjectDirectory;

    public AppEngineSelectedProjectService() {
        projectDirectory = null;
    }
    
    
    @Override
    public FileObject getProjectDirectory() {
        return this.projectDirectory;
    }

    @Override
    public void setProjectDirectory(FileObject projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public FileObject getDeployedProjectDirectory() {
        return deployedProjectDirectory;
    }

    @Override
    public void setDeployedProjectDirectory(FileObject deployedProjectDirectory) {
        this.deployedProjectDirectory = deployedProjectDirectory;
    }

    
}
