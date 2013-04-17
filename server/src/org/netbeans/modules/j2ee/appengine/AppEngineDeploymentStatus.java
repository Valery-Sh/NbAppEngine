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
import javax.enterprise.deploy.spi.status.DeploymentStatus;

/**
 * An implementation of the DeploymentStatus interface used to track the
 * server start/stop progress.
 * 
 * @author  Petr Blaha
 */
public class AppEngineDeploymentStatus implements DeploymentStatus {
    
    private ActionType action;
    private CommandType command;
    private StateType state;
    
    private String message;
    
    public AppEngineDeploymentStatus(ActionType action, CommandType command, StateType state, String message) {
        
        this.action = action;
        this.command = command;
        this.state = state;
        
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public StateType getState() {
        return state;
    }

    @Override
    public CommandType getCommand() {
        return command;
    }

    @Override
    public ActionType getAction() {
        return action;
    }
    
    @Override
    public boolean isRunning() {
        return StateType.RUNNING.equals(state);
    }

    @Override
    public boolean isFailed() {
        return StateType.FAILED.equals(state);
    }

    @Override
    public boolean isCompleted() {
        return StateType.COMPLETED.equals(state);
    }
}