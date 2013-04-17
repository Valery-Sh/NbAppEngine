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
package org.netbeans.modules.j2ee.appengine.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Jindrich Sedek
 */
class OutputProcessor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(OutputProcessor.class.getName());
    private final InputStream inputStream;
    private final Writer writer;
    private final String filterString;
    private final FilterCallback callback;
    private StringBuffer sb = new StringBuffer(4 * 1024);

    public OutputProcessor(Writer writer, InputStream inputStream, String filterString, FilterCallback callback) {
        this.inputStream = inputStream;
        this.writer = writer;
        this.filterString = filterString;
        this.callback = callback;
    }

    public void run() {
        try {
            InputStreamReader reader = new InputStreamReader(inputStream);

            char[] chars = new char[1024];
            int result;
            do {
                result = reader.read(chars);
                if (result != -1) {
                    writer.write(chars, 0, result);
                    sb.append(chars, 0, result);
                }
            } while (result != -1);
            if (sb.toString().contains(filterString)) {
                callback.textFound();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, null, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, null, e);
            }
            try {
                writer.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static Waitable readProcessOutput(Process process, String windowTitle, String filterString, FilterCallback filterCallback) {
        InputOutput io = IOProvider.getDefault().getIO(windowTitle, false);
        io.select();
        final Task t1 = RequestProcessor.getDefault().post(new OutputProcessor(io.getOut(), process.getInputStream(), filterString, filterCallback));
        final Task t2 = RequestProcessor.getDefault().post(new OutputProcessor(io.getErr(), process.getErrorStream(), filterString, filterCallback));
        return new Waitable() {

            public void waitFinished() {
                t1.waitFinished();
                t2.waitFinished();
            }
        };
    }

    static interface FilterCallback {

        void textFound();
    }

    static interface Waitable {

        void waitFinished();
    }
}
