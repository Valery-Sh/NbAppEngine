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
package org.netbeans.modules.j2ee.appengine.nodes;

import java.awt.Component;
import java.util.Collection;
import javax.swing.Action;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.MyLOG;
import org.netbeans.modules.j2ee.appengine.customizer.AppEngineCustomizer;
import org.netbeans.modules.j2ee.appengine.customizer.AppEngineCustomizerDataSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * @author Michal Mocnak
 */
public class AppEngineManagerNode extends AbstractNode {

    private final static String ICON_BASE = "org/netbeans/modules/j2ee/appengine/resources/16x16.png";

    private final Lookup lookup;

    public AppEngineManagerNode(Lookup lookup) {
        super(Children.LEAF);

        // Set default lookup
        this.lookup = lookup;
        // Set icon
        setIconBaseWithExtension(ICON_BASE);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getBundle("org.netbeans.modules.j2ee.appengine.resources.Bundle").getString("appengine.name");
    }

    @Override
    public SystemAction[] getActions() {
        return new SystemAction[] {};
    }

    @Override
    public Action[] getActions(boolean context) {
        //return null;
        return new Action[] {};
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public Component getCustomizer() {
        return new AppEngineCustomizer(new AppEngineCustomizerDataSupport(getDeploymentManager()));
    }

    public AppEngineDeploymentManager getDeploymentManager() {
        return lookup.lookup(AppEngineDeploymentManager.class);
    }
}