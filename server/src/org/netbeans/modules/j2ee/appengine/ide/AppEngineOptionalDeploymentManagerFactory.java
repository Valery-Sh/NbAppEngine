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

import org.netbeans.modules.j2ee.appengine.*;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.appengine.wizards.AppEngineInstantiatingIterator;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 * @author Michal Mocnak
 */
public class AppEngineOptionalDeploymentManagerFactory extends OptionalDeploymentManagerFactory {

    @Override
    public StartServer getStartServer(DeploymentManager manager) {
MyLOG.log("AppEngineOptionalDeploymentManagerFactory.getStartServer(manager)");
        return AppEngineStartServer.getInstance((AppEngineDeploymentManager) manager);
    }

    @Override
    public InstantiatingIterator getAddInstanceIterator() {
        return new AppEngineInstantiatingIterator();
    }

    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager manager) {
        return new AppEngineFindJSPServlet((AppEngineDeploymentManager) manager);
    }

    @Override
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager manager) {
        return null;
    }

    @Override
    public AntDeploymentProvider getAntDeploymentProvider(DeploymentManager manager) {
        return new AppEngineAntDeploymentProvider((AppEngineDeploymentManager) manager);
    }
}