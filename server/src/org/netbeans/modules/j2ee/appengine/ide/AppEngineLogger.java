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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.openide.windows.InputOutput;

/**
 * @author  Michal Mocnak
 */
public class AppEngineLogger implements InputProcessor {

    /**
     * Singleton model pattern
     */
    private static Map<String, AppEngineLogger> instances = new HashMap<String, AppEngineLogger>();

    /**
     * Buffer
     */
    private StringBuffer buffer;

    /**
     * InputOutput Object
     */
    private InputOutput io;

    /**
     * Server URI
     */
    private String uri;

    /**
     * Creates and starts a new instance of AppEngineLogger
     *
     * @param uri the uri of the server
     */
    private AppEngineLogger(String uri) {
        // Create string buffer
        this.buffer = new StringBuffer();
        // Set uri
        this.uri = uri;
    }
    
    /**
     * Returns uri specific instance of AppEngineLogger
     *
     * @param uri the uri of the server
     * @return uri specific instamce of AppEngineLogger
     */
    public static AppEngineLogger getInstance(String uri) {
        if (!instances.containsKey(uri)) {
            instances.put(uri, new AppEngineLogger(uri));
        }
        
        return instances.get(uri);
    }

    public synchronized boolean contains(String text) {
        return buffer.toString().contains(text);
    }

    public synchronized void processInput(char[] chars) throws IOException {
        // Select io
        getIO().select();
        // Write into buffer
        buffer.append(chars);
        // Write into io
        getIO().getOut().write(chars);
    }

    public synchronized void reset() throws IOException {
        buffer = new StringBuffer();
    }

    public synchronized void close() throws IOException {
        getIO().closeInputOutput();
        io = null;
    }

    private synchronized InputOutput getIO() {
        if (null == io) {
            io = UISupport.getServerIO(uri);
        }

        return io;
    }
    
    public String getText() {
        return buffer.toString();
    }
}