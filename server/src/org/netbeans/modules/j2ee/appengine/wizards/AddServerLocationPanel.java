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
package org.netbeans.modules.j2ee.appengine.wizards;

import java.io.File;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import java.awt.Component;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginUtils;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.util.NbBundle;

/**
 * @author Petr Blaha
 */
public class AddServerLocationPanel implements WizardDescriptor.Panel, ChangeListener {

    private final static String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18   
    private AddServerLocationVisualPanel component;
    private WizardDescriptor wizard;
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

    @Override
    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }

    private void fireChangeEvent(ChangeEvent ev) {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new AddServerLocationVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_server_oc4j_location"); //NOI18N
    }

    @Override
    public boolean isValid() {
        String locationStr = ((AddServerLocationVisualPanel) getComponent()).getOC4JHomeLocation();
        if (!AppEnginePluginUtils.isGoodAppEngineLocation(new File(locationStr))) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(AddServerLocationPanel.class, "MSG_InvalidServerLocation")); // NOI18N
            return false;
        } else {
            //check if this is not a duplicate instance
            for (String url : InstanceProperties.getInstanceList()) {
                InstanceProperties props = InstanceProperties.getInstanceProperties(url);
                if (props == null) {
                    // probably removed
                    continue;
                }

                String property = null;
                try {
                    property = props.getProperty(AppEnginePluginProperties.PROPERTY_APPENGINE_LOCATION);
                } catch (IllegalStateException ex) {
                    // instance removed
                }

                if (property == null) {
                    continue;
                }

                try {
                    String root = new File(property).getCanonicalPath();

                    if (root.equals(new File(locationStr).getCanonicalPath())) {
                        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_InstanceExists"));  //NOI18N
                        return false;
                    }
                } catch (IOException ex) {
                    // It's normal behaviour when instance is something else then appengine instance
                    continue;
                }
            }

            wizard.putProperty(PROP_ERROR_MESSAGE, null);
            wizard.putProperty(AppEnginePluginProperties.PROPERTY_APPENGINE_LOCATION, locationStr);
            return true;
        }
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void readSettings(Object settings) {
        if (wizard == null) {
            wizard = (WizardDescriptor) settings;
        }
    }

    public void storeSettings(Object settings) {
    }
}