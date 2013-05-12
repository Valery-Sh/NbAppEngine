/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.j2ee.appengine;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Valery
 */
public class MyLOG {
    public static void log(String msg) {
        Logger.getLogger(MyLOG.class.getName()).log(Level.WARNING, "+++++ " + msg );                        
    }
}
