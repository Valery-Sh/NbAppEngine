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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Jindrich Sedek
 */
public class DeployUtils {

    private static final String PROPERTY_APPENGINE_LOCATION = "appengineLocation"; //NOI18N
    private static final Logger LOG = Logger.getLogger(DeployUtils.class.getName());
    public static final String URI_PREFIX = "deployer:appengine";
    
    public static void out(String msg) {
        InputOutput io = IOProvider.getDefault().getIO("ShowMessage", false);
        io.getOut().println(msg);
        io.getOut().close();
    }

    static boolean isAppEngineProjectOLD(Project project) {
        //return getAppEngineFile(project) != null;
        return false;
    }
    public static boolean isAppEngineProject(Project p) {
        J2eeModuleProvider mp = p.getLookup().lookup(J2eeModuleProvider.class);
        if ( mp == null ) {
            return false;
        }
        InstanceProperties ip = mp.getInstanceProperties();
        if ( ip == null ) {
            return false;
        }
        String uri = ip.getProperty("url");
        if ( uri == null || ! uri.startsWith(URI_PREFIX)) {
            return false;
        }
        return true;

    }

    static void deploy(final Project project) {
        deploy(project, false);
    }
/*    static FileObject getAppEngineFile(Project project) {
        FileObject directory = project.getProjectDirectory();
        FileObject result = directory.getFileObject("web/WEB-INF/appengine-web.xml");
        if (result == null) { // try maven project layout
            result = directory.getFileObject("src/main/webapp/WEB-INF/appengine-web.xml");
        }
        return result;
    }
*/
    static FileObject getAppEngineFileOLD(Project project) {
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

    static FileObject getAppEngineLocation(Project project) throws IOException {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        InstanceProperties props = j2eeModuleProvider.getInstanceProperties();
        if (props == null) {
            LOG.fine("No J2ee module found");
            return null;
        }
        String serverPath = props.getProperty(PROPERTY_APPENGINE_LOCATION);
        if (serverPath == null) {
            return null;
        }
        return FileUtil.toFileObject(new File(serverPath));

    }

    static FileObject getAppCFG(Project project) throws IOException {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        InstanceProperties props = j2eeModuleProvider.getInstanceProperties();
        if (props == null) {
            LOG.fine("No J2ee module found");
            return null;
        }
        String serverPath = props.getProperty(PROPERTY_APPENGINE_LOCATION);
        if (serverPath == null) {
            LOG.fine("Server path property is missing - probably not an appengine project");
            return null;
        }
        FileObject appEngineDir = FileUtil.createData(new File(serverPath));
        FileObject appcfg = null;
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            appcfg = appEngineDir.getFileObject("bin/appcfg.cmd");
        } else {
            appcfg = appEngineDir.getFileObject("bin/appcfg.sh");
        }
        if (appcfg == null) {
            String message = NbBundle.getMessage(DeployUtils.class, "AppCFG_not_found");
            NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(desc);
        }
        return appcfg;
    }

    private static void buildProject(Project project) {
        /*        FileObject buildXML = project.getProjectDirectory().getFileObject("build.xml");
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

         */
        if ( ! isServerRunning(project)) {
            CommandActionProgress.invokeActionAndWait(project, ActionProvider.COMMAND_REBUILD);
        }
        
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        FileObject tempFo = FileUtil.toFileObject(tempDir);
        FileObject buildXml = null;
        try {
            buildXml = FileUtil.createData(tempFo, "build.xml");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        InputStream inStream = DeployUtils.class.getResourceAsStream("deploy-build.xml");
        OutputStream outStream = null;
        try {
            outStream = buildXml.getOutputStream();
            FileUtil.copy(inStream, outStream);
        } catch (FileAlreadyLockedException ex) {
            Exceptions.printStackTrace(ex);
            return;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                outStream.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        Properties props = new Properties();
        FileObject loc = null;
        try {
            loc = getAppEngineLocation(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        if (loc == null) {
            return;
        }
        props.setProperty("appengine.location", loc.getPath());
DeployUtils.out("** 1.");
        FileObject webDir = getWebDir(project);
DeployUtils.out("** 2.");        
        props.setProperty("build.web.dir", webDir.getPath());
        props.setProperty("appengine.jpa2", Boolean.toString(isV2Enhancer(project)));

        try {
            ActionUtils.runTarget(buildXml, new String[]{"enhance"}, props).waitFinished();
DeployUtils.out("** 3.");            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }

        DeployUtils.out("buildProject time=" + new Date());
        //TODO build maven project
    }

    static boolean isV2Enhancer(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        InstanceProperties props = j2eeModuleProvider.getInstanceProperties();
        if (props == null) {
            LOG.fine("No J2ee module found");
            return false;
        }
        return "v2".equals(props.getProperty("enhancerVersion"));

    }

    static boolean isServerRunning(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        InstanceProperties props = j2eeModuleProvider.getInstanceProperties();
        if ( props.getProperty("deployedProject") == null ) {
            return false;
        }
        FileObject fo = FileUtil.toFileObject(new File(props.getProperty("deployedProject")));
        if ( ! project.getProjectDirectory().equals(fo)) {
            return false;
        }
        
        String uri = j2eeModuleProvider.getServerInstanceID();
        ServerInstance si = Deployment.getDefault().getServerInstance(uri);
        
        boolean result;
        try {
            result = si.isRunning();
        } catch (InstanceRemovedException ex) {
            result = false;
            Exceptions.printStackTrace(ex);
        }
        return result;

    }
    
    
///////////////////////////////////////////////////
    

    public static boolean isMavenProject(Project project) {
        return project.getProjectDirectory().getFileObject("pom.xml") != null;
    }
    
    public static FileObject getWebDir(Project webApp) {
        FileObject webDir;
        if ( isMavenProject(webApp)) {
            webDir = FileUtil.toFileObject(new File(getMavenBuildDir(webApp)));
        } else {
            webDir = webApp.getProjectDirectory().getFileObject("build/web");
        }
        return webDir;
    }
    public static String getMavenBuildDir(Project webApp) {
        String projDir = webApp.getProjectDirectory().getPath();
        Path target = Paths.get(projDir + "/target");
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".war");
            }
            
        };
        
        File[] list = target.toFile().listFiles(filter);
        if ( list == null || list.length == 0) {
            return null;
        }
        String result = null;
        String targetDir = target.toString();
        for ( File f : list) {
            if ( f.isDirectory() ) {
                continue;
            }
            String nm = f.getName();
            String nmNoExt = nm.substring(0,nm.length()-4);
            File webInf = new File(targetDir + "/" + nmNoExt + "/WEB-INF");
            if ( webInf.exists() && webInf.isDirectory() ) {
                result = nmNoExt;
                break;
            }
        }//for
        
        if ( result != null ) {
            result = targetDir + "/"  + result;
        }
        return result;
        
    }
    
///////////////////////////////////////////////////
    // TODO FIX 17.12 V.Sh
    static FileObject getWebDirOLD(Project project) {
        FileObject appDir = null;
        FileObject projectDir = project.getProjectDirectory();
        appDir = projectDir.getFileObject("build/web");
        if (appDir == null || appDir.getChildren().length == 0) {// try maven layout
            appDir = projectDir.getFileObject("target/" + projectDir.getName());
        }
        return appDir;
    }
}
