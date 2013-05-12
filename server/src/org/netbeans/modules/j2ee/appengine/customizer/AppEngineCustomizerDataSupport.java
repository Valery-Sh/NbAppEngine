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
package org.netbeans.modules.j2ee.appengine.customizer;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

/**
 * Customizer data support keeps models for all the customizer components,
 * initializes them, tracks model changes and performs save.
 *
 * @author Michal Mocnak
 */
public class AppEngineCustomizerDataSupport {

    private AppEngineCustomizerSupport.PathModel classModel;
    private AppEngineCustomizerSupport.PathModel javadocModel;
    private boolean javadocModelFlag;
    private AppEnginePluginProperties properties;

    /**
     * Creates a new instance of CustomizerDataSupport
     */
    public AppEngineCustomizerDataSupport(AppEngineDeploymentManager manager) {
        this.properties = manager.getProperties();

        // Initialization
        init();
    }

    /**
     * Initialize the customizer models
     */
    private void init() {
        // classModel
        classModel = new AppEngineCustomizerSupport.PathModel(properties.getClasses());

        // javadocModel
        javadocModel = new AppEngineCustomizerSupport.PathModel(properties.getJavadocs());
        javadocModel.addListDataListener(new ModelChangeAdapter() {

            @Override
            public void modelChanged() {
                javadocModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
    }

    public InstanceProperties getInstanceProperties() {
        return properties.getInstanceProperties();
    }

    public AppEngineCustomizerSupport.PathModel getClassModel() {
        return classModel;
    }

    public AppEngineCustomizerSupport.PathModel getJavadocsModel() {
        return javadocModel;
    }

    /**
     * Save all changes
     */
    private void store() {
        if (javadocModelFlag) {
            properties.setJavadocs(javadocModel.getData());
            javadocModelFlag = false;
        }
    }

    /**
     * Adapter that implements several listeners, which is useful for dirty model
     * monitoring.
     */
    private abstract class ModelChangeAdapter implements ListDataListener,
            DocumentListener, ItemListener, ChangeListener {

        public abstract void modelChanged();

        @Override
        public void contentsChanged(ListDataEvent e) {
            modelChanged();
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            modelChanged();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            modelChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            modelChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            modelChanged();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            modelChanged();
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            modelChanged();
        }

        @Override
        public void stateChanged(javax.swing.event.ChangeEvent e) {
            modelChanged();
        }
    }
}
