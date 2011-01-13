package org.imagejdev.imagefiletypes.file;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.swing.Action;

import org.openide.actions.*;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.actions.SystemAction;

/**
 * <p>
 * This class subclasses the DataNode to provide a Node implementation for the
 * ImageFile data object. The node is the visual representation that is displayed by
 * an ExplorerManager such as the project view or DiskExplorer component. This
 * controls such thing as the icon or properties associated with the file type,
 * or what actions are available (and to some degree, which are enabled) in the
 * context menu when you right-click on the node. The Node is a relatively
 * &quot;dumb&quot; class. Most of the important code is actually in the
 * DataObject and Cookie implementation classes.
 * </p>
 * 
 * @author Tom Wheeler
 */
public class ImageFileNode extends DataNode implements PropertyChangeListener {

    private static final String IMAGE_ICON_BASE = "/org/imagejdev/imagefiletypes/resources/ImageFileicon";

    private ImageFileDataObject ddo;

    public ImageFileNode(ImageFileDataObject obj) {
        super(obj, Children.LEAF);
        setIconBase(IMAGE_ICON_BASE);
        this.ddo = obj;
        addPropertyChangeListener((PropertyChangeListener) this);
    }

    /* (non-Javadoc)
     * @see org.openide.nodes.Node#getPreferredAction()
     */
    public Action getPreferredAction() {
        // makes editing the default on double-click.  If you don't provide
        // an implementation of this method (and if the superclass doesn't
        // either), then nothing will happen when you double-click the node.
        return SystemAction.get(EditAction.class);
    }

    /* (non-Javadoc)
     * @see org.openide.nodes.Node#getActions(boolean)
     */
    public Action[] getActions(boolean context) {
        // these represent the items that show up in the context menu for 
        // the node.  You usually want to call the superclass implementation
        // and add those to the array first before adding your own actions, 
        // unless your intention is to override (not provide) those actions.
        
        List allActions = null;
        // a null item here is rendered as an item separator in the menu
        Action[] parentActions = super.getActions();
        Action[] myActions = new Action[]{null,
                SystemAction.get(PropertiesAction.class),
                SystemAction.get(EditAction.class),
                SystemAction.get(ViewAction.class)};

        allActions = new ArrayList();
        if (parentActions.length > 0) {
            allActions.addAll(Arrays.asList(parentActions));
        }
        allActions.addAll(Arrays.asList(myActions));
        return (Action[]) allActions.toArray(new Action[0]);
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        ddo.setModified(true);
    }

    /**
     * <p>
     * Creates a property sheet. This is used to show specific properties, for
     * example, metadata, on the NetBeans property viewer (press ctrl-shift-7 
     * to see this).  By default, nodes tied to files show information about the
     * file similar to what is shown by the operating system (name, last modification 
     * time, etc.).  Properties can be read-write or read-only. 
     * </p>
     */
    protected Sheet createSheet() {
        // we're not adding any properties in this example; it just uses
        // the defaults.
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        if (ss == null) {
            ss = Sheet.createPropertiesSet();
            s.put(ss);
        }

        return s;
    }

}
