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
package org.netbeans.modules.j2ee.appengine.config;

import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentFactory;
import org.netbeans.modules.j2ee.appengine.util.Utils;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * @author Michal Mocnak
 */
public class AppEngineModuleConfiguration implements ModuleConfiguration, ContextRootConfiguration {

    private final J2eeModule module;
    private final File appengineXmlFile;
    private final String name;

    public AppEngineModuleConfiguration(J2eeModule module) {
        this.module = module;
        appengineXmlFile = module.getDeploymentConfigurationFile("WEB-INF/appengine-web.xml");
        Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(module.getDeploymentConfigurationFile("WEB-INF")));
        //this.name = appengineXmlFile.getParentFile().getParentFile().getParentFile().getName();
        name = p.getProjectDirectory().getNameExt();
        createAppEngineXml();
    }

    @Override
    synchronized public Lookup getLookup() {
        register();        
        return Lookups.fixed(this);
    }
    /**
     * Registers the last call to the {@link #getLookup() } method.
     * 
     */
    synchronized public void register() {
        long dt = System.currentTimeMillis();
        Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(appengineXmlFile));
        AppEngineDeploymentFactory.getInstance().usedModuleChanged(p, dt);
    }

    @Override
    public J2eeModule getJ2eeModule() {
        return module;
    }

    @Override
    public void dispose() {
    }

    private void createAppEngineXml() {
        if (!appengineXmlFile.exists()) {
            try {
                String lineSep = System.getProperty("line.separator"); //NOI18N
                StringBuilder content = new StringBuilder();
                content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(lineSep);
                content.append("<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\"").append(lineSep).append("xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'").append(lineSep).append("xsi:schemaLocation='http://kenai.com/projects/nbappengine/downloads/download/schema/appengine-web.xsd appengine-web.xsd'>").append(lineSep);
                content.append("    <application>").append(name).append("</application>").append(lineSep);
                content.append("    <version>1</version>").append(lineSep);

                content.append("    <!--");
                content.append("    <ssl-enabled>false</ssl-enabled>").append(lineSep);
                content.append("    <sessions-enabled>true</sessions-enabled>").append(lineSep);
                content.append("    -->").append(lineSep);
                content.append("    <threadsafe>false</threadsafe>").append(lineSep);;

                content.append("</appengine-web-app>").append(lineSep);
                createFile(appengineXmlFile, content.toString(), "UTF-8");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void createFile(File target, String content, String encoding) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target), encoding));

        try {
            bw.write(content);
        } finally {
            bw.close();
        }
    }

    @Override
    public String getContextRoot() throws ConfigurationException {
        return "/";
    }

    @Override
    public void setContextRoot(String contextRoot) throws ConfigurationException {
        String m = module == null ? "NULL" : module.getUrl();
    }

}
