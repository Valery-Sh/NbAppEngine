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
package org.netbeans.modules.j2ee.appengine;

import java.util.HashMap;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.util.NbBundle;

/**
 * @author Michal Mocnak
 */
public class AppEngineDeploymentFactory implements DeploymentFactory {

    public static final String URI_PREFIX = "deployer:appengine";
    public static final String PROP_SERVER_ROOT = "appengine_server_root"; // NOI18N
    private final HashMap<String, DeploymentManager> managers = new HashMap<String, DeploymentManager>();
    
    private static AppEngineDeploymentFactory instance = null;

    private AppEngineDeploymentFactory() {
//MyLOG.log("APPENG: GGGGGGGGG AppEngineDeploymentFactory CONSTR ");            
        
    }

    public synchronized static AppEngineDeploymentFactory getInstance() {
        if (null == instance) {
            instance = new AppEngineDeploymentFactory();
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
            
        }
//MyLOG.log("APPENG: GGGGGGGGG AppEngineDeploymentFactory created allready ");            

        return instance;
    }


    @Override
    public boolean handlesURI(String uri) {
        return uri != null && uri.startsWith(URI_PREFIX) ;
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
}