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

import java.io.IOException;
import javax.xml.transform.Source;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.TransformableSupport;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.text.DataEditorSupport;
import org.xml.sax.InputSource;

/**
 *
 * @author Jindrich Sedek
 */
public class AppEngineDataObject extends XmlMultiViewDataObject {

    private final ModelSynchronizer synchronizer;

    public AppEngineDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        InputSource is = DataObjectAdapters.inputSource(this);
        Source source = DataObjectAdapters.source(this);

        cookies.add(new CheckXMLSupport(is));
        cookies.add(new ValidateXMLSupport(is));
        cookies.add(new TransformableSupport(source));
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        synchronizer = new ModelSynchronizer(this);
    }

    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    @Override
    protected DesignMultiViewDesc[] getMultiViewDesc() {
        return new DesignMultiViewDesc[]{new DesignTab(this)};
    }

    @Override
    protected String getPrefixMark() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    ModelSynchronizer getSynchronizer() {
        return synchronizer;
    }

    @Override
    public void setModified(boolean modif) {
        super.setModified(modif);
        updateDisplayName();
    }

    void updateDisplayName() {
        XmlMultiViewEditorSupport es = getLookup().lookup(XmlMultiViewEditorSupport.class);
        es.updateDisplayName();
    }
}
