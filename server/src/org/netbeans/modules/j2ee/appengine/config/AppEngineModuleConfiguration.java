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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.appengine.MyLOG;
import org.netbeans.modules.j2ee.appengine.util.AppEnginePluginUtils;
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
    private AppEngineBuildXmlModifier buildXmlModifier;

    public AppEngineModuleConfiguration(J2eeModule module) {
        this.module = module;
        this.appengineXmlFile = module.getDeploymentConfigurationFile("WEB-INF/appengine-web.xml");
        this.name = appengineXmlFile.getParentFile().getParentFile().getParentFile().getName();
        checkAppEngineXml();

        /*FileObject fo = FileUtil.toFileObject(appengineXmlFile);
        if (fo != null) {
            //replaceBuildXml();
            MyLOG.log(" --- AppEngineModuleConfiguration EEE originalBuildXml=" + buildXmlModifier.getOriginalBuildXml());
        } else {
            MyLOG.log(" --- AppEngineModuleConfiguration EEE FILEOBJECT=NULL");
        }
*/
    }

    public void restoreBuildXml() {
        try {
            buildXmlModifier.restoreBuildXml();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    protected void replaceBuildXml() {

        Project p = AppEnginePluginUtils.getProject(appengineXmlFile);
        buildXmlModifier = new AppEngineBuildXmlModifier(p);

        try {
            buildXmlModifier.replaceBuildXml();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    @Override
    public Lookup getLookup() {
        String m = module == null ? "NULL" : module.getUrl();
        /*MyLOG.log("AppEngineModuleConfiguration.getLookup module=" + m); 
         Lookup r = Lookups.fixed(this);
         File f = new File("d:/VnsTestApps/WebDebug1");
        
         Collection c = r.lookupAll(Object.class);
         int sz = c.size();
         MyLOG.log("AppEngineModuleConfiguration.getLookup SIZE()=" + sz);
         for ( Object o : c) {
         MyLOG.log(" --- AppEngineModuleConfiguration.getLookup LLCLASS=" + o.getClass());
         }
        
         return r;
         */
        return Lookups.fixed(this);
    }

    @Override
    public J2eeModule getJ2eeModule() {

        String m = module == null ? "NULL" : module.getUrl();
//        MyLOG.log("AppEngineModuleConfiguration.getJ2eeModule module=" + m);
        return module;
    }

    @Override
    public void dispose() {
        Project p = AppEnginePluginUtils.getProject(appengineXmlFile);
//        MyLOG.log(" #????? AppEngineModuleConfiguration DISPOSE project=" + p.getProjectDirectory().getName());
        //restoreBuildXml();
        /*        if (!AppEnginePluginUtils.isAppEngineProject(p)) {
         MyLOG.log(" #????? AppEngineModuleConfiguration DISPOSE NOT AppEngine");
         restoreBuildXml();
         } else {
         MyLOG.log("#????? AppEngineModuleConfiguration DISPOSE isAppEngine");
         }
         */
    }

    private void checkAppEngineXml() {
        if (!appengineXmlFile.exists()) {
            try {
                String lineSep = System.getProperty("line.separator"); //NOI18N
                StringBuilder content = new StringBuilder();
                content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(lineSep);
                content.append("<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\"").append(lineSep).append("xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'").append(lineSep).append("xsi:schemaLocation='http://kenai.com/projects/nbappengine/downloads/download/schema/appengine-web.xsd appengine-web.xsd'>").append(lineSep);
                content.append("    <application>").append(name).append("</application>").append(lineSep);
                content.append("    <version>1</version>").append(lineSep);

                content.append("    <!--");
                content.append("    <ssl-enabled>false</ssl-enabled>");
                content.append("    <sessions-enabled>true</sessions-enabled>");
                content.append("    -->");
                content.append("    <threadsafe>false</threadsafe>");

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
        String m = module == null ? "NULL" : module.getUrl();
//        MyLOG.log("AppEngineModuleConfiguration.getContextRoot module=" + m + "; name=" + name);

        return "/";
        //return "/" + name;
    }

    @Override
    public void setContextRoot(String contextRoot) throws ConfigurationException {
        // Nothing to do
        String m = module == null ? "NULL" : module.getUrl();
//        MyLOG.log("AppEngineModuleConfiguration.setContextRoot module=" + m);

    }

    /*My    @Override
     public void resultChanged(LookupEvent le) {
     AppEngineSelectedProject selected = Lookup.getDefault().lookup(AppEngineSelectedProject.class);
     Collection<? extends Project> projects = lookupResults.allInstances();
     FileObject oldDir = selected.getProjectDirectory();
     selected.setProjectDirectory(null);
        
     if (projects.size() == 1) {
     Project project = projects.iterator().next();
     FileObject newDir = project.getProjectDirectory();
     FileObject deployedDir = selected.getDeployedProjectDirectory();
            
     selected.setProjectDirectory(project.getProjectDirectory());
     if ( (! newDir.equals(oldDir)) && (! newDir.equals(deployedDir)) ) {
     FileObject dist = newDir.getFileObject("dist");
     if ( dist != null ) {
 
     }
     }
     */
}