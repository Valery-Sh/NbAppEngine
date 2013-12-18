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

/**
 *
 * @author Jindrich Sedek
 */
public class EmailPanel extends javax.swing.JPanel {

    /** Creates new form EmailPanel */
    public EmailPanel() {
        initComponents();
        message.setVisible(false);
        psw_TextField.setVisible(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();
        message = new javax.swing.JLabel();
        passwdField = new javax.swing.JPasswordField();
        psw_TextField = new javax.swing.JTextField();

        jLabel1.setLabelFor(emailField);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(EmailPanel.class, "EmailPanel.jLabel1.text")); // NOI18N

        jLabel2.setLabelFor(passwdField);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(EmailPanel.class, "EmailPanel.jLabel2.text")); // NOI18N

        emailField.setText(org.openide.util.NbBundle.getMessage(EmailPanel.class, "EmailPanel.emailField.text")); // NOI18N
        emailField.setToolTipText(org.openide.util.NbBundle.getMessage(EmailPanel.class, "EmailPanel.emailField.toolTipText")); // NOI18N

        message.setForeground(new java.awt.Color(255, 0, 0));
        message.setText(org.openide.util.NbBundle.getMessage(EmailPanel.class, "EmailPanel.message.text")); // NOI18N

        passwdField.setText(org.openide.util.NbBundle.getMessage(EmailPanel.class, "EmailPanel.passwdField.text")); // NOI18N
        passwdField.setToolTipText(org.openide.util.NbBundle.getMessage(EmailPanel.class, "EmailPanel.passwdField.toolTipText")); // NOI18N

        psw_TextField.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(passwdField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                            .add(emailField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(message)
                            .add(psw_TextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 292, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(emailField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwdField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(message)
                .add(18, 18, 18)
                .add(psw_TextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 31, Short.MAX_VALUE))
        );

        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EmailPanel.class, "EmailPanel.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N
        emailField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EmailPanel.class, "EmailPanel.emailField.AccessibleContext.accessibleDescription")); // NOI18N
        passwdField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EmailPanel.class, "EmailPanel.passwdField.AccessibleContext.accessibleName")); // NOI18N
        passwdField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EmailPanel.class, "EmailPanel.passwdField.AccessibleContext.accessibleDescription_1")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField emailField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel message;
    private javax.swing.JPasswordField passwdField;
    private javax.swing.JTextField psw_TextField;
    // End of variables declaration//GEN-END:variables

    String getEmail() {
        return emailField.getText();
    }

    char[] getPasswd() {
        return passwdField.getPassword();
    }

    void setEmail(String email) {
        emailField.setText(email);
    }

    void setPasswd(char[] passwd) {
        passwdField.setText(passwd != null ? new String(passwd) : null);
    }
    void setPasswdText(char[] passwd) {
        psw_TextField.setText(passwd != null ? new String(passwd) : null);
    }
    String getPasswdText() {
        return psw_TextField.getText();
    }

    void setMessage(String msg) {
        message.setText(msg);
        message.setVisible(true);
    }
}
