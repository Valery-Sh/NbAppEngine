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

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentFactory;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

/**
 * @author Petr Blaha
 */
public class AddServerPropertiesPanel implements WizardDescriptor.Panel, ChangeListener {   
    
    private AddServerPropertiesVisualPanel component;
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
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new AddServerPropertiesVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }
    
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(AppEngineDeploymentFactory.class); //NOI18N
    }
    
    @Override
    public boolean isValid() {
        wizard.putProperty(InstanceProperties.HTTP_PORT_NUMBER, ((AddServerPropertiesVisualPanel) getComponent()).getHttpPort());
        wizard.putProperty(AppEnginePluginProperties.DEBUG_PORT_NUMBER, ((AddServerPropertiesVisualPanel) getComponent()).getDebugPort());
        wizard.putProperty(AppEnginePluginProperties.PROPERTY_HOST, ((AddServerPropertiesVisualPanel) getComponent()).getHost());
        wizard.putProperty(AppEnginePluginProperties.PROPERTY_DATANUCLEUS_ENHANCER, ((AddServerPropertiesVisualPanel) getComponent()).getDataNucleusEnhancerVersion());
        return true;
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    @Override
    public void readSettings(Object settings) {
        if (wizard == null) {
            wizard = (WizardDescriptor)settings;
        }
    }
    
    @Override
    public void storeSettings(Object settings) {
    }
}