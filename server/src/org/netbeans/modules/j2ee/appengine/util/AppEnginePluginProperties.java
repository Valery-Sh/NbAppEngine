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
package org.netbeans.modules.j2ee.appengine.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.customizer.AppEngineCustomizerSupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 * @author Michal Mocnak
 */
public class AppEnginePluginProperties {

    public static final String PROPERTY_DISPLAY_NAME = InstanceProperties.DISPLAY_NAME_ATTR;
    public static final String PROPERTY_APPENGINE_LOCATION = "appengineLocation"; //NOI18N
    public static final String PROPERTY_HOST = "host"; //NOI18N
    public static final String DEBUG_PORT_NUMBER = "debug_port"; //NOI18N
    public static final String PROP_JAVA_PLATFORM = "java_platform"; //NOI18N
    public static final String PROP_JAVADOCS = "javadocs";        // NOI18N
    public static final String PLAT_PROP_ANT_NAME = "platform.ant.name"; //NOI18N

    private InstanceProperties properties;
    private AppEngineDeploymentManager manager;

    public AppEnginePluginProperties(AppEngineDeploymentManager manager) {
        this.manager = manager;
        this.properties = InstanceProperties.getInstanceProperties(this.manager.getUri());
    }

    public String getAppEngineLocation() {
        return properties.getProperty(PROPERTY_APPENGINE_LOCATION);
    }

    public JavaPlatform getJavaPlatform() {
        String currentJvm = properties.getProperty(PROP_JAVA_PLATFORM);
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatform[] installedPlatforms = jpm.getPlatforms(null, new Specification("J2SE", null)); // NOI18N
        for (int i = 0; i < installedPlatforms.length; i++) {
            String platformName = (String) installedPlatforms[i].getProperties().get(PLAT_PROP_ANT_NAME);
            if (platformName != null && platformName.equals(currentJvm)) {
                return installedPlatforms[i];
            }
        }
        // return default platform if none was set
        return jpm.getDefaultPlatform();
    }

    public InstanceProperties getInstanceProperties() {
        return properties;
    }

    public List<URL> getClasses() {
        List<URL> list = new ArrayList<URL>();
        List<String> names = new ArrayList<String>();
        File serverDir = new File(getAppEngineLocation());

        // Add all jars
        addJars(new File(serverDir, "lib"), list, names);
        addJars(new File(serverDir, "lib/impl"), list, names);
        addJars(new File(serverDir, "lib/impl/agent"), list, names);
        addJars(new File(serverDir, "lib/shared"), list, names);
        addJars(new File(serverDir, "lib/shared/jsp"), list, names);
        addJars(new File(serverDir, "lib/tools/jsp"), list, names);
        addJars(new File(serverDir, "lib/tools/orm"), list, names);
        addJars(new File(serverDir, "lib/user"), list, names);
        addJars(new File(serverDir, "lib/user/orm"), list, names);

        return list;
    }

    public List<URL> getJavadocs() {
        String path = properties.getProperty(PROP_JAVADOCS);
        if (path == null) {
            ArrayList<URL> list = new ArrayList<URL>();
            File j2eeDoc = InstalledFileLocator.getDefault().locate("docs/javadoc", null, false); // NOI18N
            if (j2eeDoc != null) {
                list.add(FileUtil.urlForArchiveOrDir(j2eeDoc));
            }
            return list;
        }
        return AppEngineCustomizerSupport.tokenizePath(path);
    }

    public void setJavadocs(List<URL> path) {
        properties.setProperty(PROP_JAVADOCS, AppEngineCustomizerSupport.buildPath(path));
        manager.getPlatform().notifyLibrariesChanged();
    }

    private void addJars(File root, List<URL> urls, List<String> names) {
        // null check
        if (null == root || !root.exists() || !root.isDirectory()) {
            return;
        }

        for (File file : root.listFiles()) {
            try {
                if (!names.contains(file.getName()) && FileUtil.isArchiveFile(file.toURI().toURL())) {
                    urls.add(FileUtil.urlForArchiveOrDir(file));
                    names.add(file.getName());
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex);
            }
        }
    }
}
