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

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;

/**
 * Google App Engine instance customizer which is accessible from server manager.
 *
 * @author  Michal Mocnak
 */
public class AppEngineCustomizer extends JTabbedPane {
    
    private final AppEngineCustomizerDataSupport support;
    
    public AppEngineCustomizer(AppEngineCustomizerDataSupport support) {
        this.support = support;

        // Initialization
        initComponents();
    }
    
    private void initComponents() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(AppEngineCustomizer.class,"ACS_Customizer"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AppEngineCustomizer.class,"ACS_Customizer"));
        // set help ID according to selected tab
        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                String helpID = null;
                switch (getSelectedIndex()) {
                    case 0 : helpID = "appengine_customizer_classes";   // NOI18N
                        break;
                    case 1 : helpID = "appengine_customizer_javadoc";   // NOI18N
                        break;
                }
                putClientProperty("HelpID", helpID); // NOI18N
            }
        });
        addTab(NbBundle.getMessage(AppEngineCustomizer.class,"TXT_Tab_Classes"),
                AppEngineCustomizerSupport.createClassesCustomizer(support.getClassModel()));
        addTab(NbBundle.getMessage(AppEngineCustomizer.class,"TXT_Tab_Javadoc"),
                AppEngineCustomizerSupport.createJavadocCustomizer(support.getJavadocsModel(), null));
    }
}