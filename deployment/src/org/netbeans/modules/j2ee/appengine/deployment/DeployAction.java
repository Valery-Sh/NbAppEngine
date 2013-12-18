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
package org.netbeans.modules.j2ee.appengine.deployment;

import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Jindrich Sedek
 */
public final class DeployAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        assert activatedNodes.length == 1;
        Project p = getProject(activatedNodes);
        DeployUtils.out("Project path=" + p.getProjectDirectory().getPath());        
        
        DeployUtils.deploy(p);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DeployAction.class, "CTL_DeployAction");
    }

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/j2ee/appengine/editor/engine.png";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes.length != 1) {
            return false;
        }
        Project proj = getProject(nodes);
        if (proj == null){
            return false;
        }
       // DeployUtils.out("Project path=" + proj.getProjectDirectory().getPath());        
        return DeployUtils.isAppEngineProject(proj) && DeployUtils.appCFGAvailable(proj);
    }

    private Project getProject(Node[] nodes) {
        Node n = nodes[0];
        //DeployUtils.out("getProject path=" +n.getLookup().lookup(Project.class).getProjectDirectory().getPath());
        return n.getLookup().lookup(Project.class);
    }
}

