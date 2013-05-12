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
package org.netbeans.modules.j2ee.appengine.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.tools.ant.module.AntSettings;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * @author Michal Mocnak
 */
public class AppEnginePluginUtils {

    private static Collection<String> fileRequired = new java.util.ArrayList<String>();

    static {
        fileRequired.add("bin/dev_appserver.sh");        // NOI18N
        fileRequired.add("bin/dev_appserver.cmd");        // NOI18N
        fileRequired.add("lib/appengine-tools-api.jar");     // NOI18N
    }

    public static boolean isGoodAppEngineLocation(File candidate) {
        AppEngineDebug.log("org.netbeans.modules.j2ee.appengine.util.AppEnginePluginUtils", "Check location for: " + candidate);

        if (null == candidate
                || !candidate.exists()
                || !candidate.canRead()
                || !candidate.isDirectory()
                || !hasRequiredChildren(candidate, fileRequired)) {
            return false;
        }

        AppEngineDebug.log("org.netbeans.modules.j2ee.appengine.util.AppEnginePluginUtils", "Location is OK");

        return true;
    }


    public static String getProperty(Project project, String key) {
        String s = null;
        Properties props = getProperties(project);
        s = props.getProperty("antsrc.cp");
        return s;
    }

    public static Properties getProperties(Project project) {
        Properties props = new Properties();

        FileObject fo = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        if (fo == null) {
            return props;
        }
        try {
            props.load(new FileInputStream(fo.getPath()));
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return props;
    }

    private static boolean hasRequiredChildren(File candidate, Collection<String> requiredChildren) {
        if (null == candidate) {
            return false;
        }

        String[] children = candidate.list();

        if (null == children) {
            return false;
        }

        if (null == requiredChildren) {
            return true;
        }

        Iterator iter = requiredChildren.iterator();

        while (iter.hasNext()) {
            String next = (String) iter.next();
            File test = new File(candidate.getPath() + File.separator + next);
            if (!test.exists()) {
                return false;
            }
        }

        return true;
    }

    public static boolean isAppEngineProject(Project project, String uri) {

        if (project == null) {
            return false;
        }

        Properties ep = new Properties();
        FileObject fo = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        //FileUtil.
        if (fo == null) {
            return false;
        }
        try {
            //ep.load(fo.getInputStream());
            ep.load(new FileInputStream(fo.getPath()));
            String p = ep.getProperty("j2ee.server.instance");

            boolean r = false;
            if (p != null && p.equals(uri)) {
                r = true;
            }
            return r;
        } catch (IOException ioe) {
            return false;
        }
    }

    public static Project getProject(File file) {

        return FileOwnerQuery.getOwner(FileUtil.toFileObject(file));
    }

    public static boolean isAppEngineProject(File file, String uri) {
        return isAppEngineProject(FileOwnerQuery.getOwner(FileUtil.toFileObject(file)), uri);
    }

    public static FileObject getAppEngineFile(Project project) {
        FileObject directory = project.getProjectDirectory();
        FileObject result = directory.getFileObject("web/WEB-INF/appengine-web.xml");

        if (result == null) { // try maven project layout
            result = directory.getFileObject("src/main/webapp/WEB-INF/appengine-web.xml");
        }

        return result;
    }

    public static Project[] getAppEngineProjects(String uri) {
        Set<Project> projects = new HashSet<Project>();

        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            if (isAppEngineProject(project, uri)) {
                projects.add(project);
            }
        }

        return projects.toArray(new Project[]{});
    }

    public static String getWarDirectory(Project project) {
        FileObject web = project.getProjectDirectory().getFileObject("build/web");

        if (null == web) {
            web = project.getProjectDirectory().getFileObject("target/" + ProjectUtils.getInformation(project).getName());
        }

        return null == web ? null : web.getPath();
    }

    public static String getWarChecksum(Project project) {
        // null check
        if (null == project) {
            return null;
        }

        // Hash code for ant project
        for (FileObject war : project.getProjectDirectory().getFileObject("dist").getChildren()) {
            if (null != war && war.getExt().equals("war")) {
                return getMD5Checksum(new File(war.getPath()));
            }
        }

        // Hash code for maven project
        for (FileObject war : project.getProjectDirectory().getFileObject("target").getChildren()) {
            if (null != war && war.getExt().equals("war")) {
                return getMD5Checksum(new File(war.getPath()));
            }
        }

        // War doesn't exist
        return null;
    }

    public static String getMD5Checksum(File file) {
        String result = "";

        try {
            byte[] b = createChecksum(file.getAbsolutePath());

            for (int i = 0; i < b.length; i++) {
                result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    public static Process runAntTarget(Project project, String target, Properties properties) {

        FileObject buildXML = project.getProjectDirectory().getFileObject("build.xml");
        //FileObject buildXML = null;
        if (buildXML != null) {
            //if ( true )
            try {
                // Create process builder
                String ant = Utilities.isWindows() ? "ant.bat" : "ant";
                ExternalProcessBuilder builder = new ExternalProcessBuilder(new File(new File(AntSettings.getAntHome(), "bin"), ant).getAbsolutePath());

                // Add arguments
                builder = builder.addArgument("-f");
                builder = builder.addArgument(buildXML.getPath());
                builder = builder.addArgument(target);
                // Add properties
                for (Object key : properties.keySet()) {
                    builder = builder.addArgument("-D" + key + "=" + properties.getProperty((String) key));
                }

                // Redirect error stream
                builder = builder.redirectErrorStream(true);

                // Perform action
                return builder.call();

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        //TODO build maven project
        return null;
    }

    private static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();

        return complete.digest();
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
}
