/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.appengine.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import static org.netbeans.modules.j2ee.appengine.AppEngineDeploymentFactory.URI_PREFIX;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
//import org.netbeans.modules.maven.api.Constants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Valery
 */
public class Utils {
    
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.j2ee.appengine"); // NOI18N
    
    
    public static void out(String msg) {
        InputOutput io = IOProvider.getDefault().getIO("ShowMessage", false);
        io.getOut().println(msg);
        io.getOut().close();
    }
    public static void out1(String msg) {
        InputOutput io = IOProvider.getDefault().getIO("ShowMessage", false);
        //io.getOut().println(msg + " MAVEN CONSTANTS " + Constants.POM_MIME_TYPE);
        io.getOut().close();
    }
    
    public static Project projectByName(String projName) {
        Project[] projs = projects();
        Project r = null;
        for ( Project p : projs) {
            if ( p.getProjectDirectory().getName().equals(projName) ) {
                r = p;
                break;
            }
        }
        return r;
    }
    public static Project[] projects() {
        return OpenProjects.getDefault().getOpenProjects();
    }
    
    public static boolean isAppEngineProject(Project p) {
        //out("isAppEngineProject p=" + p);        
        J2eeModuleProvider mp = p.getLookup().lookup(J2eeModuleProvider.class);
        //out("isAppEngineProject 1." );        
        
        if ( mp == null ) {
        //out("isAppEngineProject 2." );        
            
            return false;
        }
        //out("isAppEngineProject 3." );        
        
        InstanceProperties ip = mp.getInstanceProperties();
        if ( ip == null ) {
            return false;
        }
        //out("isAppEngineProject 4." );        
        
        String uri = ip.getProperty("url");
        //out("URL PROP=" + uri);
        
        if ( uri == null || ! uri.startsWith(URI_PREFIX)) {
            return false;
        }
        out("isAppEngineProject == TRUE" );        
        return true;

    }

    /**
     * Returns a file object which represents a web application configuration
     * file named {@literal embedded-context.properties} for a given directory.
     * If a web project is registered in an embedded server then it must have
     * such file in the {@literal META-INF} directory.
     *
     * @param webProjectDir a directory to look for
     * @return {@literal null} if the specified file doesn't exist. An instance
     * of (@code FileObject} otherwise
     */
    public static FileObject getAppEngineConfigFile(FileObject webProjectDir) {
        FileObject fo = null;
        File cf = getJ2eeModule(FileOwnerQuery.getOwner(webProjectDir)).getDeploymentConfigurationFile("WEB-INF/appengine-web.xml");
        if (cf != null) {
            fo = FileUtil.toFileObject(cf);
        }
        return fo;
    }

    /**
     * Return an instance of {@literal J2eeModule} for the specified web
     * project.
     *
     * @param webProject the web project whose {@literal J2eeModule} is required
     * @return an instance of {@literal J2eeModule} for the specified web
     * project
     */
    public static J2eeModule getJ2eeModule(Project webProject) {
        return getJ2eeModuleProvider(webProject).getJ2eeModule();
    }
    public static J2eeModuleProvider getJ2eeModuleProvider(Project webProject) {
        return webProject.getLookup().lookup(J2eeModuleProvider.class);
    }

    public static void writeDeploymentScript(AppEngineDeploymentManager manager,OutputStream os) throws IOException {
        String xml = convertStreamToString(AppEngineDeploymentManager.class.getResourceAsStream("resources/deploy-build.xml"));
        // Set sdk.path property
        xml = xml.replace("#appengine.location", manager.getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.PROPERTY_APPENGINE_LOCATION));
        xml = xml.replace("#appengine.http.port", manager.getProperties().getInstanceProperties().getProperty(InstanceProperties.HTTP_PORT_NUMBER));
        xml = xml.replace("#appengine.debug.port", manager.getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.DEBUG_PORT_NUMBER));
        //String webDir = Utils.getAppEngineConfigFile(manager.getSelected().getProjectDirectory()).getParent().getParent().getPath();
        String webDir = Utils.getWebDir(manager.getSelected()).getPath();
        xml = xml.replace("#build.web.dir", webDir);
Utils.out("xml: appengine.location = " + manager.getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.PROPERTY_APPENGINE_LOCATION));
Utils.out("xml: appengine.http.port = " + manager.getProperties().getInstanceProperties().getProperty(InstanceProperties.HTTP_PORT_NUMBER));
Utils.out("xml: appengine.debug.port = " + manager.getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.DEBUG_PORT_NUMBER));
Utils.out("xml: build.web.dir = " + webDir);

        String p = manager.getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.PROPERTY_DATANUCLEUS_ENHANCER);        
        if ( "v2".equals(p)) {
            p = "true";
        } else {
            p = "false";
        }
        xml = xml.replace("#appengine.jpa2", p);
        
        // Create input stream
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        if (is == null) {
            // this should never happen, but better make sure
            LOGGER.severe("Missing resource resources/appengine-ant-deploy.xml."); // NOI18N
            return;
        }
        try {
            FileUtil.copy(is, os);
        } finally {
            is.close();
        }
        
        
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
    /**
     * (@code Development mode} only.
     * @param projDir
     * @return 
     */
    public static boolean isMavenProject(String projDir) {
        return new File(projDir + "/pom.xml").exists();
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
    /**
     * (@code Development mode} only.
     * @param projDir
     * @return 
     */
    public static boolean isMavenProject(Project project) {
        return project.getProjectDirectory().getFileObject("pom.xml") != null;
    }
    
    /**{@literal Development mode} only.
     * @param projDir
     * @return 
     */
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
    
}
