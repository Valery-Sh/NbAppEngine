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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jindrich Sedek
 */
public class Utils {

    private static final String PROPERTY_APPENGINE_LOCATION = "appengineLocation"; //NOI18N
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());
    
    static boolean isAppEngineProject(Project project) {
        return getAppEngineFile(project) != null;
    }

    static void deploy(final Project project) {
        deploy(project, false);
    }

    static FileObject getAppEngineFile(Project project) {
        FileObject directory = project.getProjectDirectory();
        FileObject result = directory.getFileObject("web/WEB-INF/appengine-web.xml");
        if (result == null) { // try maven project layout
            result = directory.getFileObject("src/main/webapp/WEB-INF/appengine-web.xml");
        }
        return result;
    }

    private static void deploy(final Project project, boolean markPasswordIncorrect) {
        assert isAppEngineProject(project);
        buildProject(project);
        new Deployer(project, markPasswordIncorrect).deploy();
    }

    static boolean appCFGAvailable(Project project) {
        try {
            FileObject fo = getAppCFG(project);
            if (fo == null) {
                return false;
            }
            return fo.isData() && fo.canRead();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    static FileObject getAppCFG(Project project) throws IOException {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        InstanceProperties props = j2eeModuleProvider.getInstanceProperties();
        if (props == null) {
            LOG.fine("No J2ee module found");
            return null;
        }
        String serverPath = props.getProperty(PROPERTY_APPENGINE_LOCATION);
        if (serverPath == null){
            LOG.fine("Server path property is missing - probably not an appengine project");
            return null;
        }
        FileObject appEngineDir = FileUtil.createData(new File(serverPath));
        FileObject appcfg = null;
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")){
            appcfg = appEngineDir.getFileObject("bin/appcfg.cmd");
        }else{
            appcfg = appEngineDir.getFileObject("bin/appcfg.sh");
        }
        if (appcfg == null) {
            String message = NbBundle.getMessage(Utils.class, "AppCFG_not_found");
            NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(desc);
        }
        return appcfg;
    }

    private static void buildProject(Project project) {
        FileObject buildXML = project.getProjectDirectory().getFileObject("build.xml");
        if (buildXML != null) {
            try {
                ActionUtils.runTarget(buildXML, new String[]{"datanucleusenhance"}, null).waitFinished();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
            return;
        }
        //TODO build maven project
    }

    static FileObject getAppDir(Project project) {
        FileObject appDir = null;
        FileObject projectDir = project.getProjectDirectory();
        appDir = projectDir.getFileObject("build/web");
        if (appDir == null || appDir.getChildren().length == 0) {// try maven layout
            appDir = projectDir.getFileObject("target/" + projectDir.getName());
        }
        return appDir;
    }
}
