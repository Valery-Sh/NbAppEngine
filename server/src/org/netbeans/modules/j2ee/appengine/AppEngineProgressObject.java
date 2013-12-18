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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.appengine.util.Utils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.openide.util.NbBundle;

/**
 * @author Michal Mocnak
 */
public class AppEngineProgressObject implements ProgressObject {
    List<ProgressListener> listeners = new ArrayList<ProgressListener>();
    private AppEngineTargetModuleID module;
    private boolean failed;
    //private final AppEngineServerMode mode;
    private final Deployment.Mode mode;
    private CommandType commantType;
    
    public AppEngineProgressObject(AppEngineTargetModuleID module, boolean failed,Deployment.Mode mode ) {
        this.module = module;
        this.failed = failed;
        this.mode = mode;
        commantType = CommandType.START;
    }
    public AppEngineProgressObject(AppEngineTargetModuleID module, boolean failed,Deployment.Mode mode, CommandType ct ) {
        this.module = module;
        this.failed = failed;
        this.mode = mode;
        commantType = ct;        
    }
    @Override
    public DeploymentStatus getDeploymentStatus() {
        return new AppEngineDeploymentStatus(ActionType.EXECUTE, commantType, failed ? StateType.FAILED : StateType.COMPLETED,
                failed ? NbBundle.getMessage(AppEngineProgressObject.class, "MSG_Failed") : null);
    }

    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[] {module};
    }

    @Override
    public ClientConfiguration getClientConfiguration(TargetModuleID arg0) {
        return null;
    }

    @Override
    public boolean isCancelSupported() {
        return true;
    }

    @Override
    public void cancel() throws OperationUnsupportedException {
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
        listeners.add(arg0);
    }

    @Override
    public void removeProgressListener(ProgressListener arg0) {
        listeners.remove(arg0);
    }

    public Deployment.Mode getMode() {
        return mode;
    }
    
    
}