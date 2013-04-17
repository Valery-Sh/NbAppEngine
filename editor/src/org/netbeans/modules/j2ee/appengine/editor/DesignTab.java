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
import java.io.Serializable;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.openide.awt.UndoRedo;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jindrich Sedek
 */
class DesignTab extends DesignMultiViewDesc implements MultiViewElement, Serializable, ChangeListener {

    static final long serialVersionUID = -3640713597058983397L;
    private transient DesignTabPanel designTabPanel = null;
    private boolean ignoreStateChanges = false;

    public DesignTab(AppEngineDataObject dataObj) {
        super(dataObj, "Design");
    }

    @Override
    public MultiViewElement createElement() {
        return this;
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("org/netbeans/modules/j2ee/appengine/editor/engine.png");
    }

    @Override
    public String preferredID() {
        return "Google App Engine Design Tab";
    }

    //--------------//
    public JComponent getVisualRepresentation() {
        if (designTabPanel == null) {
            designTabPanel = new DesignTabPanel();
            getModel().setChangeListener(this);
        }
        return designTabPanel;
    }

    public JComponent getToolbarRepresentation() {
        return new JPanel();
    }

    public Action[] getActions() {
        return getDataObject().getNodeDelegate().getActions(true);
    }

    public Lookup getLookup() {
        return new ProxyLookup(new org.openide.util.Lookup[]{
                    getDataObject().getNodeDelegate().getLookup()
                });
    }

    public void componentOpened() {
        ((AppEngineDataObject) getDataObject()).updateDisplayName();
    }

    public void componentClosed() {
    }

    public void componentShowing() {
        getVisualRepresentation();
        designTabPanel.setModel(getModel());
    }

    public void componentHidden() {
        designTabPanel.stopEditing();
    }

    private AppEngineDataModel getModel() {
        AppEngineDataObject dobj = (AppEngineDataObject) getDataObject();
        return dobj.getSynchronizer().getModel();
    }

    public void componentActivated() {
    }

    public void componentDeactivated() {
    }

    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    public void setMultiViewCallback(MultiViewElementCallback arg0) {
    }

    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    public void stateChanged(ChangeEvent e) {
        if (ignoreStateChanges) {
            return;
        }
        AppEngineDataObject dobj = (AppEngineDataObject) getDataObject();
        dobj.getSynchronizer().requestUpdateData();
        dobj.setModified(true);
    }
}
