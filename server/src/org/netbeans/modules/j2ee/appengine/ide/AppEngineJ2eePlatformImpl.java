/**
 *  This file is part of Google App Engine suppport in NetBeans IDE.
 *
 *  Google App Engine suppport in NetBeans IDE is free software: you can
 *  redistribute it and/or modify it under the terms of the GNU General
 *  Public License as published by the Free Software Foundation, either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  Google App Engine suppport in NetBeans IDE is distributed in the hope
 *  that it will be useful, but WITHOUT ANY WARRANTY; without even the
 *  implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Google App Engine suppport in NetBeans IDE.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.j2ee.appengine.ide;

import java.awt.Image;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.MyLOG;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * @author Michal Mocnak
 */
public class AppEngineJ2eePlatformImpl extends J2eePlatformImpl {

    private LibraryImplementation[] libraries;
    private AppEnginePluginProperties properties;

    public AppEngineJ2eePlatformImpl(AppEngineDeploymentManager manager) {
        this.properties = manager.getProperties();

        // Load libraries
        loadLibraries();
    }

    @Override
    public LibraryImplementation[] getLibraries() {
//MyLOG.log("AppEngineJ2eePlatformImpl.getLibraries");                
        return libraries.clone();
    }

    @Override
    public String getDisplayName() {
//MyLOG.log("AppEngineJ2eePlatformImpl.getDisplayName");                        
        return NbBundle.getBundle("org.netbeans.modules.j2ee.appengine.resources.Bundle").getString("appengine.platform.name");
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("org/netbeans/modules/j2ee/myserver/resources/server.gif");
    }

    @Override
    public File[] getPlatformRoots() {
//MyLOG.log("AppEngineJ2eePlatformImpl.getPlatformRoots");                                
        return null;
    }

    @Override
    public File[] getToolClasspathEntries(String arg0) {
//MyLOG.log("AppEngineJ2eePlatformImpl.getToolClasspathEntries");                                        
        return new File[] {};
    }

    @Override
    public boolean isToolSupported(String arg0) {
//MyLOG.log("AppEngineJ2eePlatformImpl.isToolSupported");        
        if(arg0.equals("org.datanucleus.store.appengine.jpa.DatastorePersistenceProvider")) { // NOI18N
            return true;
        } else if(arg0.equals("dataNucleusPersistenceProviderIsDefault")) { // NOI18N
            return true;
        }
        return false;
    }

    @Override
    public Set getSupportedSpecVersions() {
//MyLOG.log("AppEngineJ2eePlatformImpl.getSupportedSpecVersions");        
        Set <String> result = new HashSet<String>();
        //result.add(J2eeModule.JAVA_EE_5);
        result.add("1.5");
        return result;
    }

    @Override
    public Set getSupportedModuleTypes() {
//MyLOG.log("AppEngineJ2eePlatformImpl.getSupportedModuleTypes");                
        Set<Object> result = new HashSet<Object>();
        result.add(J2eeModule.Type.WAR);
        return result;
    }

    @Override
    public Set getSupportedJavaPlatformVersions() {
//MyLOG.log("AppEngineJ2eePlatformImpl.getSupportedJavaPlatformVersions");         
        return null;
    }

    @Override
    public JavaPlatform getJavaPlatform() {
//MyLOG.log("AppEngineJ2eePlatformImpl.getJavaPlatform");                 
        return null;
    }

    public void notifyLibrariesChanged() {
//MyLOG.log("AppEngineJ2eePlatformImpl.notifyLibrariesChanged");                         
        // Reload libraries
        loadLibraries();
        // Fire changes
        firePropertyChange(PROP_LIBRARIES, null, libraries.clone());
    }

    private void loadLibraries() {
//MyLOG.log("AppEngineJ2eePlatformImpl.loadLibraries");                                 
        // Create library
        LibraryImplementation library = new J2eeLibraryTypeProvider().createLibrary();
        // Set name
        library.setName(NbBundle.getBundle("org.netbeans.modules.j2ee.appengine.resources.Bundle").getString("appengine.name"));
        // Set content
        library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, properties.getClasses());
        // Store it
        libraries = new LibraryImplementation[]{library};
    }

/* "Это для J2eePlatformImpl2
  //@Override
    public File getServerHome() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //@Override
    public File getDomainHome() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //@Override
    public File getMiddlewareHome() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
*/    
    
}