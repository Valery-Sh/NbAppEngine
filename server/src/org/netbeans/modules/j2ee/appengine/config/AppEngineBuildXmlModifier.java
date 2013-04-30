/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.j2ee.appengine.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author V.Shyshkin
 */
public class AppEngineBuildXmlModifier implements FileSystem.AtomicAction {

    /**
     * From constructor
     */
    private Project project;
    private String originalBuildXml;
    private String action;

    public AppEngineBuildXmlModifier(Project project) {
        this.project = project;
    }

    public void replaceBuildXml() throws IOException {
        this.action = "replace";
        run();
    }

    public void restoreBuildXml() throws IOException {
        this.action = "restore";
        run();
    }

    /**
     * Reads
     * <code>build.Xml</code> and if it doesn't contain the target with a
     * name='connect-debugger' than saves it as a string.
     */
    protected void runRestore() throws IOException {
        String b;
        b = readBuildXmlAsString();
        if (!b.contains("connect-debugger")) {
            return;
        }

        StringBuilder sb = new StringBuilder(b);
        int start = 0;
        int end;
        String s = b;

        while (true) {
            start = s.indexOf("<target", start);
            if (start < 0) {
                break;
            }
            end = s.indexOf("</target>", start);
            if (end < 0) { // possible comment
                s = s.substring(start);
                continue;
            }
            s = s.substring(start, end);
            if (s.contains("connect-debugger")) {
                //delete substring
                sb.delete(start, end + "</target>".length());
                replace(sb.toString());
                break;
            }
            s = b;
            start = end;
            // start = end;
        }
    }

    /**
     * Reads
     * <code>build.Xml</code> and if it doesn't contain the target with a
     * name='connect-debugger' than saves it as a string.
     */
    protected void runReplace() throws IOException {
        String b;
        b = readBuildXmlAsString();
        if (b.contains("connect-debugger")) {
            return;
        }

        originalBuildXml = b;

        StringBuilder sb = new StringBuilder(b);
        int insertPos = sb.indexOf("</project>") - 1;
        String r = null;
        r = readAppEngineBuildXml();
        if (r == null) {
            return;
        }
        sb.insert(insertPos, r);
        replace(sb.toString());
    }

    protected void replace(String withContent) throws IOException {
        FileObject target = project.getProjectDirectory().getFileObject("build.xml");
        FileLock lock = target.lock();
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(target.getOutputStream(lock), "UTF-8"));
            bw.write(withContent);
            bw.close();

        } finally {
            lock.releaseLock();
        }
    }

    protected String readAppEngineBuildXml() throws IOException {
        InputStream input = AppEngineDeploymentManager.class.getResourceAsStream("resources/appengine-build.xml");
        StringBuilder sb = new StringBuilder();
        String lineSep = System.getProperty("line.separator");//NOI18N

        BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }

    protected String readBuildXmlAsString() throws IOException {
        InputStream input = project.getProjectDirectory().getFileObject("build.xml").getInputStream();
        StringBuilder sb = new StringBuilder();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }

    public String getOriginalBuildXml() {
        return originalBuildXml;
    }

    @Override
    public void run() throws IOException {
        if ("replace".equals(action)) {
            runReplace();
        } else if ("restore".equals(action)) {
            runRestore();
        }

    }
}
