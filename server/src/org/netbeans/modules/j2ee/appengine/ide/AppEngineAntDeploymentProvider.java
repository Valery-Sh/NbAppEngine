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
package org.netbeans.modules.j2ee.appengine.ide;

import org.netbeans.modules.j2ee.appengine.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 * @author Michal Mocnak
 */
public class AppEngineAntDeploymentProvider implements AntDeploymentProvider {

    private final AppEngineDeploymentManager manager;
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.j2ee.appengine"); // NOI18N

    public AppEngineAntDeploymentProvider(AppEngineDeploymentManager manager) {
        this.manager = manager;
    }

    @Override
    public void writeDeploymentScript(OutputStream os, Object moduleType) throws IOException {
        MyLOG.log("AppEngineAntDeploymentProvider.write os.class="+os.getClass());
        String xml = convertStreamToString(AppEngineDeploymentManager.class.getResourceAsStream("resources/appengine-ant-deploy.xml"));
        // Set sdk.path property
        xml = xml.replace("#appengine.location", manager.getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.PROPERTY_APPENGINE_LOCATION));
        xml = xml.replace("#appengine.http.port", manager.getProperties().getInstanceProperties().getProperty(InstanceProperties.HTTP_PORT_NUMBER));
        xml = xml.replace("#appengine.debug.port", manager.getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.DEBUG_PORT_NUMBER));
        xml = xml.replace("#appengine.manager.uri", manager.getUri());
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

    @Override
    public File getDeploymentPropertiesFile() {
        return null;
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
