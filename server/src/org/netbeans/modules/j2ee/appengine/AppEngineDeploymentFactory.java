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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Michal Mocnak
 */
public class AppEngineDeploymentFactory implements DeploymentFactory, LookupListener {

    protected Lookup.Result<Project> lookupResults;
    private final Map<String,LastState> selectedMap = new HashMap<String,LastState>();
    private final Map<String,LastState> usedMap = new HashMap<String,LastState>();

    public static final String URI_PREFIX = "deployer:appengine";
    public static final String PROP_SERVER_ROOT = "appengine_server_root"; // NOI18N
    private final HashMap<String, DeploymentManager> managers = new HashMap<String, DeploymentManager>();

    private static AppEngineDeploymentFactory instance = null;

    private AppEngineDeploymentFactory() {
    }

    public synchronized static AppEngineDeploymentFactory getInstance() {
        if (null == instance) {
            instance = new AppEngineDeploymentFactory();
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
            instance.lookupResults = Utilities.actionsGlobalContext().lookupResult(Project.class);
            instance.lookupResults.addLookupListener(instance);

        }
        return instance;
    }

    @Override
    public boolean handlesURI(String uri) {
        return uri != null && uri.startsWith(URI_PREFIX);
    }

    @Override
    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI:" + uri);
        }
        // Trying to fetch from cache
        DeploymentManager manager = managers.get(uri);

        if (null == manager) {
            // Create a new instance
            manager = new AppEngineDeploymentManager(uri);
            // Insert into cache
            managers.put(uri, manager);
        }

        return manager;
    }

    @Override
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        return getDeploymentManager(uri, null, null);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getBundle("org.netbeans.modules.j2ee.appengine.resources.Bundle").getString("appengine.name");
    }

    @Override
    public String getProductVersion() {
        return NbBundle.getBundle("org.netbeans.modules.j2ee.appengine.resources.Bundle").getString("appengine.version");
    }
    //
    // --------------  LookupListener Implementation ----------------------
    //
    
    @Override
    public void resultChanged(LookupEvent ev) {
        long dt = System.currentTimeMillis();
        Project project = null;
        Collection<? extends Project> projects = lookupResults.allInstances();
        if (projects.size() == 1) {
            project = projects.iterator().next();
        }
        if ( project == null ) {
            return;
        }
        
        J2eeModuleProvider mp = project.getLookup().lookup(J2eeModuleProvider.class);
        if ( mp == null ) {
            return;
        }
        InstanceProperties ip = mp.getInstanceProperties();
        String uri = ip.getProperty("url");
        if ( ! uri.startsWith(URI_PREFIX)) {
            return;
        }
        LastState state = new LastState(project.getProjectDirectory(), dt);
        selectedMap.put(uri, state);
    }

    public void usedModuleChanged(Project project, long time) {
        if ( project == null ) {
            return;
        }
        
        J2eeModuleProvider mp = project.getLookup().lookup(J2eeModuleProvider.class);
        if ( mp == null ) {
            return;
        }
        InstanceProperties ip = mp.getInstanceProperties();
        if ( ip == null || ip.getProperty("url") == null || !ip.getProperty("url").startsWith(URI_PREFIX)) {
            return;
        }
        
        LastState state = new LastState(project.getProjectDirectory(), time);
        usedMap.put(ip.getProperty("url"), state);
    }
    
    public FileObject getLastSelectedProject(String uri) {
        LastState ls = getLastStateSelected(uri);
        if ( ls == null ) {
            return null;
        }
        return ls.getProjDir();
    }
    public LastState getLastStateSelected(String uri) {
        return selectedMap.get(uri);
    }
    public LastState getLastStateUsedModule(String uri) {
        return usedMap.get(uri);
    }

    public static class LastState {
        private final FileObject projDir;
        private final long time;
        
        public LastState(FileObject projDir, long time) {
            this.projDir = projDir;
            this.time = time;
        }

        public FileObject getProjDir() {
            return projDir;
        }

        public long getTime() {
            return time;
        }
        
    }
}
