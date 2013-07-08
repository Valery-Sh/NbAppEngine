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

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentFactory;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * @author Petr Blaha
 */
public class AppEngineInstantiatingIterator implements WizardDescriptor.InstantiatingIterator, ChangeListener {
    
    private transient AddServerLocationPanel locationPanel = null;
    private transient AddServerPropertiesPanel propertiesPanel = null;
    
    private WizardDescriptor wizard;
    private transient int index = 0;
    private transient WizardDescriptor.Panel[] panels = null;
        
    private final Set <ChangeListener> listeners = new HashSet<ChangeListener>(1);
    
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
    public void uninitialize(WizardDescriptor wizard) {
    }
    
    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }
    
    @Override
    public void previousPanel() {
        index--;
    }
    
    @Override
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    @Override
    public String name() {
        return "Google App Engine Server Iterator";  // NOI18N
    }
    
    public static void showInformation(final String msg,  final String title){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                d.setTitle(title);
                DialogDisplayer.getDefault().notify(d);
            }
        });
        
    }
    
    @Override
    public Set instantiate() throws IOException {
        Set <InstanceProperties> result = new HashSet<InstanceProperties>();
        String displayName =  (String)wizard.getProperty("ServInstWizard_displayName"); // NOI18N
        int httpPort = (Integer) wizard.getProperty(InstanceProperties.HTTP_PORT_NUMBER);
        int debugPort = (Integer) wizard.getProperty(AppEnginePluginProperties.DEBUG_PORT_NUMBER);
        String host = (String) wizard.getProperty(AppEnginePluginProperties.PROPERTY_HOST);
        String url = AppEngineDeploymentFactory.URI_PREFIX + ":" + host + ":" + httpPort + "/" + displayName.hashCode();    // NOI18N
        String appengineLocation = (String) wizard.getProperty(AppEnginePluginProperties.PROPERTY_APPENGINE_LOCATION);
        
        String enhancer = (String) wizard.getProperty(AppEnginePluginProperties.PROPERTY_DATANUCLEUS_ENHANCER);
        try {
            InstanceProperties ip = InstanceProperties.createInstanceProperties(url, null, null, displayName);
            ip.setProperty(AppEnginePluginProperties.PROPERTY_APPENGINE_LOCATION, appengineLocation);
            ip.setProperty(InstanceProperties.HTTP_PORT_NUMBER, Integer.toString(httpPort));
            ip.setProperty(AppEnginePluginProperties.DEBUG_PORT_NUMBER, Integer.toString(debugPort));
            ip.setProperty(AppEnginePluginProperties.PROPERTY_HOST, host);
            ip.setProperty(AppEnginePluginProperties.PROPERTY_DATANUCLEUS_ENHANCER, enhancer);
            
            result.add(ip);                        
        } catch (InstanceCreationException e){
            showInformation(e.getLocalizedMessage(), NbBundle.getMessage(AddServerLocationPanel.class, "MSG_INSTANCE_REGISTRATION_FAILED"));
            Logger.getLogger("global").log(Level.SEVERE, e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }
    
    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(AppEngineInstantiatingIterator.class, "STEP_ServerLocation"),
            NbBundle.getMessage(AppEngineInstantiatingIterator.class, "STEP_Properties")
        };
    }
    
    protected final String[] getSteps() {
        if (steps == null) {
            steps = createSteps();
        }
        return steps;
    }
    
    protected final WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = createPanels();
        }
        return panels;
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        if (locationPanel == null) {
            locationPanel = new AddServerLocationPanel();
            locationPanel.addChangeListener(this);
        }

        if (propertiesPanel == null) {
            propertiesPanel = new AddServerPropertiesPanel();
            propertiesPanel.addChangeListener(this);
        }
        
        return new WizardDescriptor.Panel[] {
            (WizardDescriptor.Panel)locationPanel,
            (WizardDescriptor.Panel)propertiesPanel
        };
    }
    
    private transient String[] steps = null;
    
    protected final int getIndex() {
        return index;
    }
    
    @Override
    public WizardDescriptor.Panel current() {
        WizardDescriptor.Panel result = getPanels()[index];
        JComponent component = (JComponent)result.getComponent();
        component.putClientProperty("WizardPanel_contentData", getSteps());  // NOI18N
        component.putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(getIndex()));// NOI18N
        return result;
    }
    
    @Override
    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        fireChangeEvent();
    }
    
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
}