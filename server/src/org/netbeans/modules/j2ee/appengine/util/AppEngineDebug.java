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

/**
 * @author Petr Blaha
 */
public class AppEngineDebug {

    private static final String PROPERTY = "serverplugins.appengine.debug";
    private static boolean isEnabled = System.getProperty(PROPERTY) != null;   // NOI18N
    
    public static boolean isEnabled() {
        return isEnabled;
    }
    
    public static void log(String className, String msg) {
       if(isEnabled()) {
           System.out.println("serverplugins.appengine.debug: Class " + className + ", Message: " + msg);
       }
    }
}