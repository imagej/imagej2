/*
 *
 * SelectAndCustomizePanel.java
 *
 * Created on October 15, 2005, 6:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.imagejdev.misccomponents.explorer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import org.imagejdev.misccomponents.IconButtonComboBoxUI;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ChoiceView;
import org.openide.explorer.view.NodeListModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 * A general panel which can be pointed at a folder in the system filesystem
 * and will show a ChoiceView (combobox) of its contents with a custom icon-only
 * UI.
 *
 * @author Timothy Boudreau
 */
public final class SelectAndCustomizePanel extends JPanel implements ExplorerManager.Provider,
                                                                     PropertyChangeListener,
                                                                     LayoutManager {
    private final ChoiceView choice = new ChoiceView();
    private final ExplorerManager mgr = new ExplorerManager();
    private final JPanel customizerContainer = new JPanel();
    private String folder;
    private JLabel lbl;
    private JPanel top;
    public SelectAndCustomizePanel(String folder, boolean useIconDropDown) {
        customizerContainer.setLayout(new BorderLayout());
        mgr.addPropertyChangeListener(this);
        choice.setMinimumSize(new Dimension(80, 16));
        lbl = new JLabel();
        setLayout(new BorderLayout());
        top = new JPanel();

        top.setLayout(this);
        top.add(lbl);
        top.add(choice);
        top.add(new JSeparator());
        add(top, BorderLayout.NORTH);
        add(customizerContainer, BorderLayout.CENTER);
        if (useIconDropDown) {
            choice.setUI(new IconButtonComboBoxUI());
        }
        this.folder = folder;
    }

    public void initialize() {
        try {
            FileSystem systemFileSystem = Repository.getDefault().getDefaultFileSystem();
            FileObject fld = systemFileSystem.getRoot().getFileObject(folder);
            DataFolder dfldr = DataFolder.findFolder(fld);
            Node root = new FilterNode (dfldr.getNodeDelegate(), new NameFilterNodeChildren(dfldr.getNodeDelegate()));

            lbl.setText(root.getDisplayName());
            mgr.setRootContext(root);
            mgr.setExploredContext(root);
            String last = (String)fld.getAttribute("lastSelection");
            Node[] n = mgr.getRootContext().getChildren().getNodes(true);

            if (n.length > 0) {
                Node sel = n[0];

                if (last != null) {
                    Node lsel = mgr.getRootContext().getChildren().findChild(last);

                    if (lsel != null) {
                        sel = lsel;
                    }
                }
                choice.setShowExploredContext(false);
                // set an initial selection, so the folder isn't selected
                mgr.setSelectedNodes(new Node[] {sel});
                // XXX shouldn't need to do this:
                NodeListModel mdl = (NodeListModel)choice.getModel();

                mdl.setSelectedItem(sel);
                choice.setToolTipText(sel.getDisplayName());
            }
            if (n.length == 1) {
                //XXX - should listen to children changes on the node, in case a module
                //is loaded that adds children
                top.remove (lbl);
                top.remove (choice);
            }
        }
        catch (PropertyVetoException pve) {
            // won't actually happen
            Exceptions.printStackTrace(pve);
        }
        
    }

    @Override
    public void addNotify() {
        super.addNotify();
        EventQueue.invokeLater(new Runnable() {

                                   public void run() {
                                       initialize();
                                   }
                               });
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null ||
            ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            if (mgr.getSelectedNodes().length > 0) {
                Node n = mgr.getSelectedNodes()[0];

                choice.setToolTipText(n.getDisplayName());
                if (n.hasCustomizer()) {
                    setSelection(null);
                    setInnerComponent(n.getCustomizer());
                }
                else if (n.getCookie(InstanceCookie.class) != null) {
                    InstanceCookie ic = (InstanceCookie)n.getCookie(InstanceCookie.class);

                    try {
                        if (Customizable.class.isAssignableFrom(ic.instanceClass())) {
                            setSelection((Customizable)ic.instanceCreate());
                        }
                        else {
                            setSelection(null);
                        }
                    }
                    catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                        setSelection(null);
                    }
                    catch (ClassNotFoundException cnfe) {
                        ErrorManager.getDefault().notify(cnfe);
                        setSelection(null);
                    }
                } else {
                    System.err.println("No instance cookie found in " + n);
                    
                    setSelection(null);
                }
                FileSystem systemFileSystem = Repository.getDefault().getDefaultFileSystem();
                FileObject fld = systemFileSystem.getRoot().getFileObject(folder);

                try {
                    fld.setAttribute("lastSelection", n.getName());
                }
                catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
    }
    private Customizable selection;

    public Customizable getSelection() {
        return selection;
    }

    private void setSelection(Customizable sel) {
        if (this.selection != sel) {
            this.selection = sel;
            setInnerComponent(sel == null ? null : sel.getCustomizer());
        }
    }

    public void setInnerComponent(Component c) {
        customizerContainer.removeAll();
        if (c == null) {
            JLabel jl = new JLabel();

            jl.setEnabled(false);
            jl.setHorizontalAlignment(SwingConstants.CENTER);
            jl.setHorizontalTextPosition(SwingConstants.CENTER);
            jl.setText(org.openide.util.NbBundle.getMessage(SelectAndCustomizePanel.class,
                                                            "LBL_NoCustomizer"));
            c = jl;
        }
        customizerContainer.add(c, BorderLayout.CENTER);
        customizerContainer.invalidate();
        customizerContainer.revalidate();
        customizerContainer.repaint();
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Component[] c = parent.getComponents();
        Insets ins = parent.getInsets();
        int w = ins.left + ins.right;
        int h = 0;

        for (int i = 0; i < c.length; i++) {
            if (!(c[i] instanceof JSeparator)) {
                Dimension d = c[i].getPreferredSize();

                if (i == 0) {
                    d.width = Math.max(d.width, MIN_LBL_WIDTH);
                }
                w += d.width;
                h = Math.max(h, d.height);
            }
        }
        h += ins.top + ins.bottom;
        return new Dimension(w, h);
    }

    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }
    private static final int MIN_LBL_WIDTH = 60;
    private static final int GAP = 5;

    public void layoutContainer(Container parent) {
        Component[] c = parent.getComponents();
        Insets ins = parent.getInsets();
        int x = ins.left;

        for (int i = 0; i < c.length; i++) {
            Dimension d = c[i].getPreferredSize();
            int y = Math.max(0, (parent.getHeight()/2) - (d.height/2));
            int w = i == c.length - 1 ? (parent.getWidth() -
                                         (ins.left + ins.right)) - x
                                      : d.width;

            if (i == 0) {
                w = Math.max(w, MIN_LBL_WIDTH);
            }
            c[i].setBounds(x, y, w, d.height);
            x += w + GAP;
        }
    }
}
