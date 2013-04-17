/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.appengine.wizards;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentFactory;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author pblaha
 */
public class AddServerLocationVisualPanel extends javax.swing.JPanel {
    private final Set <ChangeListener> listeners = new HashSet<ChangeListener>();
    private static JFileChooser chooser = null;

    /** Creates new form AddServerLocationVisualPanel */
    public AddServerLocationVisualPanel() {
        initComponents();
        setName(NbBundle.getMessage(AddServerLocationVisualPanel.class, "TITLE_ServerLocation"));
        oc4jHomeTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
            public void insertUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
            public void removeUpdate(DocumentEvent e) {
                fireChangeEvent();
            }                    
        });
        
        initData();
    }
    
    private void initData() {
        String root = NbPreferences.forModule(AppEngineDeploymentFactory.class).get(AppEngineDeploymentFactory.PROP_SERVER_ROOT, "");
        
        if (root.length() != 0)
            oc4jHomeTextField.setText(root);
    }
    
    public String getOC4JHomeLocation() {
        return oc4jHomeTextField.getText();
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l ) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged (ev);
        }
    }
    
    private String browseOC4JHomeLocation(){
        String oc4jLocation = null;
        JFileChooser ch = getJFileChooser();
        int returnValue = ch.showDialog(this, NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooseButton")); //NOI18N
        
        if(returnValue == JFileChooser.APPROVE_OPTION){
            oc4jLocation = ch.getSelectedFile().getAbsolutePath();
        }
        return oc4jLocation;
    }
    
    private JFileChooser getJFileChooser(){
        
        if (chooser == null) {        
            chooser = new JFileChooser();
            chooser.setDialogTitle(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName")); //NOI18N
            chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);

            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setApproveButtonMnemonic("Choose_Button_Mnemonic".charAt(0)); //NOI18N
            chooser.setMultiSelectionEnabled(false);
            chooser.addChoosableFileFilter(new DirFilter());
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setApproveButtonToolTipText(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName")); //NOI18N

            chooser.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName")); //NOI18N
            chooser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName")); //NOI18N
        }

        // set the current directory
        File currentLocation = new File(oc4jHomeTextField.getText());
        if (currentLocation.exists() && currentLocation.isDirectory()) {
            chooser.setCurrentDirectory(currentLocation.getParentFile());
            chooser.setSelectedFile(currentLocation);
        }
        
        
        return chooser;
    }
    
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        oc4jHomeLbl = new javax.swing.JLabel();
        oc4jHomeTextField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        oc4jHomeLbl.setLabelFor(oc4jHomeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(oc4jHomeLbl,
                NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_InstallLocation"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(oc4jHomeLbl, gridBagConstraints);
        oc4jHomeTextField.setColumns(15);
        oc4jHomeTextField.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_InstallLocation"));
        oc4jHomeTextField.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_InstallLocation"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(oc4jHomeTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_BrowseButton"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(jButton1, gridBagConstraints);
        jButton1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_BrowseButton"));
        jButton1.getAccessibleContext().setAccessibleDescription("ACSD_Browse_Button_InstallLoc");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
        jPanel1.getAccessibleContext().setAccessibleName("TITLE_AddServerLocationPanel");
        jPanel1.getAccessibleContext().setAccessibleDescription("AddServerLocationPanel_Desc");        
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        String newLoc = browseOC4JHomeLocation();
        if ((newLoc!=null)&&(!newLoc.equals("")))
        oc4jHomeTextField.setText(newLoc);
    }

    private static class DirFilter extends javax.swing.filechooser.FileFilter {
        
        public boolean accept(File f) {
            if(!f.exists() || !f.canRead() || !f.isDirectory() ) {
                return false;
            }else{
                return true;
            }
        }
        
        public String getDescription() {
            return NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_DirType");
        }
        
    }
    
    // Variables declaration - do not modify
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel oc4jHomeLbl;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField oc4jHomeTextField;
    // End of variables declaration   
}