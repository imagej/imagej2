/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.misccomponents.explorer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Tim Boudreau
 */
final class CustomizeFolderPanel extends JPanel implements ExplorerManager.Provider {
    private final ExplorerManager mgr = new ExplorerManager();
    private final JPanel buttonPanel;
    private final PCL pcl = new PCL();
    private final BeanTreeView btv;
    private String fldName;
    public CustomizeFolderPanel (String folder) {
        try {
            setLayout(new BorderLayout());
            add(btv = new BeanTreeView(), BorderLayout.CENTER);
            btv.setRootVisible(false);
            btv.setPopupAllowed(false);
            buttonPanel = new JPanel();
            buttonPanel.setBorder (BorderFactory.createEmptyBorder(5,5,5,5));
            buttonPanel.setLayout(new LM());
            mgr.addPropertyChangeListener(pcl);
            add(buttonPanel, BorderLayout.LINE_END);
            FileObject fld = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject(folder);
            if (fld == null) {
                throw new IllegalStateException(fld + " does not exist");
            }
            DataObject dob = DataObject.find(fld);
            fldName = dob.getNodeDelegate().getDisplayName();
            mgr.setRootContext(new FilterNode (dob.getNodeDelegate(), new NameFilterNodeChildren(dob.getNodeDelegate())));
            btv.expandAll();
        } catch (DataObjectNotFoundException ex) {
            throw new IllegalStateException (ex);
        }
    }
    
    public static void showDialog(String folder) {
        String closeString = NbBundle.getMessage(CustomizeFolderPanel.class, "CLOSE"); //NOI18N
        CustomizeFolderPanel pnl = new CustomizeFolderPanel (folder);
        DialogDescriptor dlg = new DialogDescriptor (pnl, pnl.fldName, false, 
                new Object[] { closeString }, closeString, DialogDescriptor.PLAIN_MESSAGE,
                HelpCtx.DEFAULT_HELP,
                null);
        DialogDisplayer.getDefault().notify(dlg);
        pnl.btv.requestFocus();
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }
    
    private final class PCL implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] n = mgr.getSelectedNodes();
                buttonPanel.removeAll();
                if (n.length == 1) {
                    DeleteAction a = SystemAction.get(DeleteAction.class);
                    Action action = a.createContextAwareInstance(n[0].getLookup());
                    buttonPanel.add (new JButton(action));
                    RenameAction a1 = SystemAction.get(RenameAction.class);
                    action = a1.createContextAwareInstance(n[0].getLookup());
                    buttonPanel.add (new JButton(action));
                    MoveUpAction a2 = SystemAction.get(MoveUpAction.class);
                    buttonPanel.add (new JButton(a2.createContextAwareInstance(n[0].getLookup())));
                    MoveDownAction a3 = SystemAction.get(MoveDownAction.class);
                    buttonPanel.add (new JButton(a3.createContextAwareInstance(n[0].getLookup())));
                }
                
//                if (n.length == 1) {
//                    Action[] a = n[0].getActions(false);
//                    for (int i=0; i < a.length; i++) {
//                        if (!skip (a[i])) {
//                            JButton jb = new JButton();
//                            if (a[i] instanceof ContextAwareAction) {
//                                a[i] = ((ContextAwareAction) a[i]).createContextAwareInstance(n[0].getLookup());
//                            }
//                            Actions.connect(jb, a[i]);
//                            buttonPanel.add (jb);
//                        }
//                    }
//                }
                buttonPanel.invalidate();
                buttonPanel.revalidate();
                buttonPanel.repaint();
            }
        }
    }
    
    private boolean skip(Action a) {
//        if (a == null) {
//            return true;
//        }
//        if (a instanceof CutAction || a instanceof CopyAction || 
//            a instanceof PasteAction || a instanceof DeleteAction || 
//            a instanceof RenameAction) {
//            return false;
//        }
//        return true;
        return a == null;
    }
    
    private class LM implements LayoutManager {
        public void addLayoutComponent(String name, Component comp) {}
        public void removeLayoutComponent(Component comp) {}

        public Dimension preferredLayoutSize(Container parent) {
            return layoutSize (parent, false);
        }

        private static final int GAP = 5;
        public Dimension minimumLayoutSize(Container parent) {
            return layoutSize (parent, true);
        }
        
        private Dimension layoutSize (Container parent, boolean isMin) {
            Component[] comps = parent.getComponents();
            if (comps.length == 0) {
                return new Dimension (150, 40);
            }
            int y = 0;
            int minW = Integer.MIN_VALUE;
            for (Component c : comps) {
                Dimension d = isMin ? c.getMinimumSize() : c.getPreferredSize();
                minW = Math.max (d.width, minW);
                y += d.height + GAP;
            }
            Insets ins = parent.getInsets();
            minW += ins.left + ins.right;
            y += ins.top + ins.bottom;
            return new Dimension (minW, y);
            
        }

        public void layoutContainer(Container parent) {
            Component[] comps = parent.getComponents();
            Insets ins = parent.getInsets();
            int w = parent.getWidth() - (ins.left + ins.right);
            int top = ins.top;
            int h = parent.getHeight() - (ins.top + ins.bottom);
            int y = top;
            for (Component c : comps) {
                int ht = c.getPreferredSize().height;
                c.setBounds (ins.left, y, w, ht);
                y += ht + GAP;
            }
        }
        
    }
}
