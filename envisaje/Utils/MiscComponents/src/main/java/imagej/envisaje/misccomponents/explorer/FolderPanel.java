/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.envisaje.misccomponents.explorer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.UIManager;
import imagej.envisaje.misccomponents.TitledPanel;
import org.openide.cookies.InstanceCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ChoiceView;
import org.openide.explorer.view.ListView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Tim Boudreau
 */
public class FolderPanel<T> extends TitledPanel implements ExplorerManager.Provider {
    private final ExplorerManager mgr = new ExplorerManager();
    private final Class<T> selectionType;
    private final PCL pcl = new PCL();
    private final JPanel inner = new JPanel();
    private final ListView lv = new ListView();
    private final ChoiceView cv = new ChoiceView();
    public FolderPanel(String folder, Class<T> selectionType) {
        super(folder);
        setBorder (BorderFactory.createEmptyBorder());
        this.selectionType = selectionType;
        inner.setLayout (new BorderLayout());
        lv.setPopupAllowed(false);
        inner.add (lv, BorderLayout.CENTER);
        lv.setBorder(BorderFactory.createLineBorder(UIManager.getColor("controlShadow")));
        boolean expanded = false;
        try {
            setCenterComponent(cv);
            FileObject fld = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject(folder);
            Boolean exp = (Boolean) fld.getAttribute("expanded"); //NOI18N
            expanded = exp == null ? false : exp.booleanValue();
            if (fld == null) {
                throw new IllegalStateException(fld + " does not exist");
            }
            DataObject dob = DataObject.find(fld);
            mgr.setRootContext(new FilterNode (dob.getNodeDelegate(), new NameFilterNodeChildren(dob.getNodeDelegate())));
            String last = (String) fld.getAttribute("lastSelection"); //NOI18N
            Node[] n = mgr.getRootContext().getChildren().getNodes(true);
            if (n.length > 0) {
                Node sel = n[0];
                if (last != null) {
                    Node lsel = mgr.getRootContext().getChildren().findChild(last);
                    if (lsel != null) {
                        sel = lsel;
                    }
                }
                // set an initial selection, so the folder isn't selected
                mgr.setSelectedNodes(new Node[]{sel});
                pcl.propertyChange(null);
            }
            setTitle (dob.getNodeDelegate().getDisplayName());
        } catch (PropertyVetoException ex) {
            throw new IllegalStateException(ex);
        } catch (DataObjectNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        mgr.addPropertyChangeListener(pcl);
        if (expanded) {
            doSetExpanded(true);
        }
    }

    @Override
    public Component setExpanded(boolean val) {
        boolean old = super.isExpanded();
        Component result = super.setExpanded(val);
        if (val != old) {
            try {
                if (val) {
                    result = inner;
                } else {
                    result = cv;
                }
                DataObject dob = mgr.getRootContext().getLookup().lookup(DataObject.class);
                FileObject fob = dob.getPrimaryFile();
                fob.setAttribute("expanded", val ? Boolean.TRUE : Boolean.FALSE);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        T old = selection;
        selection = null;
        setSelection (old);
    }

    @Override
    public void removeNotify() {
        if (oldCustomizer != null) {
            inner.remove (oldCustomizer);
        }
        super.removeNotify();
    }

    @Override
    protected void onCustomize() {
        String path = mgr.getRootContext().getLookup().lookup(DataObject.class).getPrimaryFile().getPath();
        CustomizeFolderPanel.showDialog(path);
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }
    private T selection;

    private Component oldCustomizer;
    private void setSelection(T t) {
        T old = selection;
        if (selection == t) {
            return;
        }
        selection = t;
        if (!isDisplayable()) {
            return;
        }
        if (t instanceof Customizable) {
            Customizable c = (Customizable) t;
            Component comp = c.getCustomizer();
            boolean changed = false;
            if (oldCustomizer != null) {
                inner.remove(oldCustomizer);
                changed = true;
            }
            if (comp != null) {
                oldCustomizer = comp;
                inner.add (comp, BorderLayout.SOUTH);
                changed = true;
            }
            if (changed) {
                inner.invalidate();
                inner.revalidate();
                inner.repaint();
            }
        }
        onSelectionChanged (old, selection);
    }

    protected void onSelectionChanged (T old, T nue) {
        firePropertyChange("selection", old, nue);
    }

    public T getSelection() {
        return selection;
    }

    private class PCL implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt == null ||
                    ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                if (mgr.getSelectedNodes().length > 0) {
                    Node n = mgr.getSelectedNodes()[0];
                    InstanceCookie ic = (InstanceCookie) n.getCookie(InstanceCookie.class);
                    try {
                        if (ic != null && selectionType.isAssignableFrom(ic.instanceClass())) {
                            setSelection((T) ic.instanceCreate());
                        } else {
                            setSelection(null);
                        }
                        FileObject fld = mgr.getRootContext().getLookup().lookup(DataObject.class).getPrimaryFile();
                        fld.setAttribute("lastSelection", n.getName()); //NOI18N
                    } catch (Exception cnfe) {
                        throw new IllegalStateException(cnfe);
                    }
                }
            }
        }
    }
}
