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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.openide.filesystems.FileLock;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.xml.sax.SAXException;

/**
 *
 * @author Jindrich Sedek
 */
class ModelSynchronizer extends XmlMultiViewDataSynchronizer {

    private AppEngineDataObject dataObject;
    private AppEngineDataModel model;
    private boolean modelIsValid;

    public ModelSynchronizer(AppEngineDataObject dataObj) {
        super(dataObj, 500);
        this.dataObject = dataObj;
    }

    @Override
    protected boolean mayUpdateData(boolean allowDialog) {
        return true;
    }

    @Override
    protected void updateDataFromModel(Object modelParam, FileLock lock, boolean modify) {
        try {
            AppEngineDataModel appModel = (AppEngineDataModel) modelParam;
            OutputStream wr = new ByteArrayOutputStream();
            try {
                XMLUtil.write(appModel.getRoot(), wr, "UTF-8");
            } finally {
                wr.close();
            }
            dataObject.getDataCache().setData(lock, wr.toString(), modify);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected AppEngineDataModel getModel() {
        if (model == null) {
            InputStream is = dataObject.getDataCache().createInputStream();
            try {
                model = AppEngineDataModel.createModel(is);
                modelIsValid = true;
            } catch (ParserConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SAXException ex) {
                modelIsValid = false;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return model;
    }

    @Override
    protected void reloadModelFromData() {
        if (model == null) {
            return;
        }
        InputStream is = dataObject.getDataCache().createInputStream();
        try {
            model.reload(is);
            modelIsValid = true;
        } catch (SAXException ex) {
            modelIsValid = false;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
