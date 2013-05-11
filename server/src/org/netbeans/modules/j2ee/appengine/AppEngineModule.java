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

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.openide.util.NbBundle;

/**
 * @author Michal Mocnak
 */
public class AppEngineModule implements TargetModuleID {

    private AppEngineTarget target;
    private String hostname;
    private int port;
    private String contextRoot;

    public AppEngineModule(AppEngineTarget target, String hostname, int port, String contextRoot) {
        this.target = target;
        this.hostname = hostname;
        this.port = port;
        this.contextRoot = contextRoot;
//MyLOG.log("AppEngineModule costr contextRoot=" + contextRoot + "; target="+target+"; host=" + hostname + "; port="+ port);        
    }

    @Override
    public Target getTarget() {
        return target;
    }

    @Override
    public String getModuleID() {
//MyLOG.log("AppEngineModule.getModuleID() context="+ contextRoot);    
        return getWebURL() + "/" + contextRoot;
//        return NbBundle.getMessage(AppEngineModule.class, "TITLE_Application") + "/" + contextRoot;
    }

    @Override
    public String getWebURL() {
//MyLOG.log("AppEngineModule.getWebURL()");        
        //return target.getServerUri () + "/" + contextRoot;
        return "http://" + hostname + ":" + port;
    }

    @Override
    public TargetModuleID getParentTargetModuleID() {
        return null;
    }

    @Override
    public TargetModuleID[] getChildTargetModuleID() {
        return new TargetModuleID[] {};
    }
}