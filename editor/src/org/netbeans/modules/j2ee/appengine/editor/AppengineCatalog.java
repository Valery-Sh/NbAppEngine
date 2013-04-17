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
package org.netbeans.modules.j2ee.appengine.editor;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 *@author  Jindrich Sedek
 */
public class AppengineCatalog implements CatalogReader, CatalogDescriptor, EntityResolver {

    private static final String APPENGINE_WEB_XSD = "appengine-web.xsd";
    private static final String APPENGINE_WEB = "http://kenai.com/projects/nbappengine/downloads/download/schema/appengine-web.xsd";
    private static final String WEB_LOCAL_URL = "nbres:/org/netbeans/modules/j2ee/appengine/editor/appengine-web.xsd";
    private static final String WEB_PUBLIC_ID = "SCHEMA:" + APPENGINE_WEB;

    private static final String APPENGINE_CRON_XSD = "cron.xsd";
    private static final String APPENGINE_CRON = "http://kenai.com/projects/nbappengine/downloads/download/schema/cron.xsd";
    private static final String CRON_LOCAL_URL = "nbres:/org/netbeans/modules/j2ee/appengine/editor/cron.xsd";
    private static final String CRON_PUBLIC_ID = "SCHEMA:" + APPENGINE_CRON;

    private static final String DATASTORE_WEB_XSD = "datastore-indexes.xsd";
    private static final String DATASTORE = "http://kenai.com/projects/nbappengine/downloads/download/schema/datastore-indexes.xsd";
    private static final String DATASTORE_LOCAL_URL = "nbres:/org/netbeans/modules/j2ee/appengine/editor/datastore-indexes.xsd";
    private static final String DATASTORE_PUBLIC_ID = "SCHEMA:" + DATASTORE;


    /** Creates a new instance of RegisterCatalog */
    public AppengineCatalog() {
    }

    public Iterator getPublicIDs() {
        List<String> list = new ArrayList<String>();
        list.add(WEB_PUBLIC_ID);
        list.add(CRON_PUBLIC_ID);
        list.add(DATASTORE_PUBLIC_ID);
        
        return list.listIterator();
    }

    public void refresh() {
    }

    public String getSystemID(String publicId) {
        if(publicId.equals(WEB_PUBLIC_ID)) {
            return WEB_LOCAL_URL;
        } else if(publicId.equals(CRON_PUBLIC_ID)) {
            return CRON_LOCAL_URL;
        } else if(publicId.equals(DATASTORE_PUBLIC_ID)) {
            return DATASTORE_LOCAL_URL;
        } else {
            return null;
        }
    }

    public String resolveURI(String string) {
        return null;
    }

    public String resolvePublic(String string) {
        return null;
    }

    public void addCatalogListener(CatalogListener catalogListener) {
    }

    public void removeCatalogListener(CatalogListener catalogListener) {
    }

    public Image getIcon(int i) {
        return ImageUtilities.loadImage("org/netbeans/modules/j2ee/appengine/editor/engine.png");
    }

    public String getDisplayName() {
        return NbBundle.getBundle(AppengineCatalog.class).getString("APPENGINE_CATALOG");
    }

    public String getShortDescription() {
        return NbBundle.getBundle(AppengineCatalog.class).getString("XML_CATALOG_FOR_GOOGLE_APPENGINE");
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (APPENGINE_WEB.equals(systemId)){
            return new org.xml.sax.InputSource(WEB_LOCAL_URL);
        }
        if (APPENGINE_CRON.equals(systemId)){
            return new org.xml.sax.InputSource(CRON_LOCAL_URL);
        }
        if (DATASTORE.equals(systemId)){
            return new org.xml.sax.InputSource(DATASTORE_LOCAL_URL);
        }
        if (systemId != null && systemId.endsWith(APPENGINE_WEB_XSD)){
            return new org.xml.sax.InputSource(WEB_LOCAL_URL);
        }
        if (systemId != null && systemId.endsWith(APPENGINE_CRON_XSD)){
            return new org.xml.sax.InputSource(CRON_LOCAL_URL);
        }
        if (systemId != null && systemId.endsWith(DATASTORE_WEB_XSD)){
            return new org.xml.sax.InputSource(DATASTORE_LOCAL_URL);
        }
        return null;
    }
}
