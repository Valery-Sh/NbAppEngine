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

import java.io.File;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.util.AppEngineJspNameUtil;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;

/**
 * @author Michal Mocnak
 */
public class AppEngineFindJSPServlet implements FindJSPServlet {

    private final String host;
    private final int port;

    public AppEngineFindJSPServlet(AppEngineDeploymentManager manager) {
        this.host = manager.getProperties().getInstanceProperties().getProperty(AppEnginePluginProperties.PROPERTY_HOST);
        this.port = Integer.valueOf(manager.getProperties().getInstanceProperties().getProperty(InstanceProperties.HTTP_PORT_NUMBER));
    }

    @Override
    public File getServletTempDirectory(String moduleContextPath) {
        File temp = new File(System.getProperty("java.io.tmpdir"));

        //My if ((temp == null) || !temp.exists()) {
        if (!temp.exists()) {
            return null;
        }

        for (File folder : temp.listFiles()) {
            if (folder.getName().startsWith("Jetty_" + host + "_" + port)) {
                return new File(folder, "jsp");
            }
        }

        return null;
    }

    @Override
    public String getServletResourcePath(String moduleContextPath, String jspResourcePath) {
        return getServletPackageName(jspResourcePath).replace('.', '/') + '/' +
                getServletClassName(jspResourcePath) + ".java";
    }

    @Override
    public String getServletEncoding(String arg0, String arg1) {
        return "UTF8";
    }

    private String getServletPackageName(String jspUri) {
        String dPackageName = getDerivedPackageName(jspUri);

        if (dPackageName.length() == 0) {
            return AppEngineJspNameUtil.JSP_PACKAGE_NAME;
        }
        
        return AppEngineJspNameUtil.JSP_PACKAGE_NAME + '.' + getDerivedPackageName(jspUri);
    }

    private String getDerivedPackageName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/');
        return (iSep > 0) ? AppEngineJspNameUtil.makeJavaPackage(jspUri.substring(0, iSep)) : "";
    }

    private String getServletClassName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/') + 1;
        return AppEngineJspNameUtil.makeJavaIdentifier(jspUri.substring(iSep));
    }
}