/**
 * This file is part of Google App Engine suppport in NetBeans IDE.
 *
 * Google App Engine suppport in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Google App Engine suppport in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Google App Engine suppport in NetBeans IDE. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.j2ee.appengine.deployment;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jindrich Sedek
 */
public class Deployer implements Cancellable, OutputProcessor.FilterCallback {

    private static final String OLD_EMAIL_PROPERTY = "email";
    private static final String OLD_PASSWD_PROPERTY = "passwd";
    private static final String EMAIL_KEY = "org.netbeans.modules.j2ee.appengine.email";
    private static final String PASSWD_KEY = "org.netbeans.modules.j2ee.appengine.passwd";
    private static final Preferences preferences = NbPreferences.forModule(DeployUtils.class);
    private final String processTitle = NbBundle.getMessage(DeployUtils.class, "DeploymentTitle");
    private final Project project;
    private ProgressHandle progressHandle;
    private Process runningProcess;
    boolean canFinish;

    Deployer(Project project, boolean markPasswordIncorrect) {
        this.project = project;
    }

    void deploy() {
        progressHandle = ProgressHandleFactory.createHandle(processTitle, this);
        progressHandle.start();
        deploy(false);
    }

    private void deploy(boolean markPasswordIncorrect) {
        Writer wr = null;
        OutputProcessor.Waitable waitable = null;
        try {
            FileObject appcfg = DeployUtils.getAppCFG(project);

            if (appcfg == null) {
                cancel();
                return;
            }

            String email = getEmail(markPasswordIncorrect);

            if (email == null) {
                cancel();
                return;
            }
            FileObject appDir = DeployUtils.getWebDir(project);
            if (appDir == null || appDir.getChildren().length == 0) {
                String message = NbBundle.getMessage(DeployUtils.class, "BUILD_NEEDED_MESSAGE");
                NotifyDescriptor desc = new DialogDescriptor.Message(new JLabel(message), DialogDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(desc);
                cancel();
                return;
            }
            ProcessBuilder process = new ProcessBuilder(appcfg.getPath(), "--email=" + email, "--passin", "update", appDir.getPath());
            runningProcess = process.start();
            waitable = OutputProcessor.readProcessOutput(runningProcess, processTitle, "password do not match", this);
            canFinish = true;
            wr = new OutputStreamWriter(runningProcess.getOutputStream());
            wr.write(Keyring.read(PASSWD_KEY));
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } finally {
            if (wr != null) {
                try {
                    wr.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (waitable != null) {
                waitable.waitFinished();
            }
            if (canFinish) {
                progressHandle.finish();
            }
        }
    }

    public void textFound() {
        if (runningProcess != null) {
            runningProcess.destroy();
        }
        canFinish = false;
        deploy(true);
    }

    public boolean cancel() {
        if (runningProcess != null) {
            runningProcess.destroy();
        }
        progressHandle.finish();
        return true;
    }

    private static String loadEmail() {
        String email = preferences.get(OLD_EMAIL_PROPERTY, null);
        if (email != null) {
            preferences.remove(OLD_EMAIL_PROPERTY);
            Keyring.save(EMAIL_KEY, email.toCharArray(), /*XXX I18N*/ "Google App Engine email");
            return email;
        } else {
            char[] e = Keyring.read(EMAIL_KEY);
            return e != null ? new String(e) : null;
        }
    }

    public static String changeEmail() {
        String result = loadEmail();

        String message = null;

        boolean dialogResult = showDialog(null, null, message);
        if (!dialogResult) {
            return null;
        }
        return loadEmail();
    }

    private static String getEmail(boolean markPasswordIncorrect) {
        String result = loadEmail();

        if (markPasswordIncorrect || result == null) {

            String message = null;
            if (markPasswordIncorrect) {
                message = NbBundle.getMessage(DeployUtils.class, "ValueIncorrect");
            }

            boolean dialogResult = showDialog(null, null, message);
            if (!dialogResult) {
                return null;
            }
        }
        return loadEmail();
    }

    private static boolean showDialog(String email, char[] passwd, String message) {
        if (email == null) {
            email = loadEmail();
            if (email == null) {
                email = "";
            }
        }
        if (passwd == null) {
            String old = preferences.get(OLD_PASSWD_PROPERTY, null);
            if (old != null) {
                preferences.remove(OLD_PASSWD_PROPERTY);
                passwd = old.toCharArray();
                Keyring.save(PASSWD_KEY, passwd, /*XXX I18N*/ "Google App Engine password for " + email);
            } else {
                passwd = Keyring.read(PASSWD_KEY);
            }
        }

        EmailPanel panel = new EmailPanel();
        panel.setEmail(email);
        panel.setPasswd(passwd);
        
        panel.setPasswdText(passwd);

        
        if (message != null) {
            panel.setMessage(message);
        }
        String dialogTitle = NbBundle.getMessage(DeployUtils.class, "PasswordTitle");

        DialogDescriptor desriptor = new DialogDescriptor(panel, dialogTitle, true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);

        Object resultOption = DialogDisplayer.getDefault().notify(desriptor);

        if (DialogDescriptor.OK_OPTION.equals(resultOption)) {
            email = panel.getEmail();
            passwd = panel.getPasswd();
            if ((email.length() == 0) || (passwd.length == 0)) {
                return showDialog(email, passwd, NbBundle.getMessage(DeployUtils.class, "ValueNotSet"));
            }
            Keyring.save(EMAIL_KEY, email.toCharArray(), /*XXX I18N*/ "Google App Engine email");
            Keyring.save(PASSWD_KEY, passwd, /*XXX I18N*/ "Google App Engine password for " + email);
        } else {
            return false;
        }
        return true;
    }
}
