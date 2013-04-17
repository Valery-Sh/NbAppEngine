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

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.openide.util.NbBundle;

/**
 * @author Michal Mocnak
 */
public class AppEngineProgressObject implements ProgressObject {

    private AppEngineModule module;
    private boolean failed;

    public AppEngineProgressObject(AppEngineModule module, boolean failed) {
        this.module = module;
        this.failed = failed;
    }

    @Override
    public DeploymentStatus getDeploymentStatus() {
        return new AppEngineDeploymentStatus(ActionType.EXECUTE, CommandType.START, failed ? StateType.FAILED : StateType.COMPLETED,
                failed ? NbBundle.getMessage(AppEngineProgressObject.class, "MSG_Failed") : null);
    }

    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
MyLOG.log("AppEngineProgressObject.getResultTargetModuleIDs moduleID" + module.getModuleID());
        return new TargetModuleID[] {module};
    }

    @Override
    public ClientConfiguration getClientConfiguration(TargetModuleID arg0) {
        return null;
    }

    @Override
    public boolean isCancelSupported() {
        return false;
    }

    @Override
    public void cancel() throws OperationUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStopSupported() {
        return false;
    }

    @Override
    public void stop() throws OperationUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addProgressListener(ProgressListener arg0) {
    }

    @Override
    public void removeProgressListener(ProgressListener arg0) {
    }
}