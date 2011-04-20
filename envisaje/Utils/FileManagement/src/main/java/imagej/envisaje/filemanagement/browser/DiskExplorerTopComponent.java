/*
Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
Microsystems, Inc. All Rights Reserved.
 */
package imagej.envisaje.filemanagement.browser;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.windows.OutputWriter;
import org.openide.windows.IOProvider;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** 
 * A simple explorer component.
 *
 * @author  Rich Unger
 */
public class DiskExplorerTopComponent extends CloneableTopComponent implements ExplorerManager.Provider {

    private static final String MODE = "explorer"; // NOI18N
    /** A hint to the window system for generating a unique id */
    private static final String PREFERRED_ID = "disk_explorer"; // NOI18N
    private transient final ExplorerManager m_manager;
    private transient Node m_rootNode;
    private transient final BeanTreeView m_btv;

    public static DiskExplorerTopComponent getInstance() {
        // look for an open instance
        Iterator opened = TopComponent.getRegistry().getOpened().iterator();
        while (opened.hasNext()) {
            Object tc = opened.next();
            if (tc instanceof DiskExplorerTopComponent) {
                return (DiskExplorerTopComponent) tc;
            }
        }

        // none found, make a new one
        return new DiskExplorerTopComponent();
    }

    public DiskExplorerTopComponent() {
        super();
		
        m_manager = new ExplorerManager();
        m_btv = new BeanTreeView();
        m_btv.setDragSource(true);
        m_btv.setRootVisible(false);

        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(m_manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(m_manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(m_manager));
        map.put("delete", ExplorerUtils.actionDelete(m_manager, true)); // NOI18N

        setLayout(new BorderLayout());
        add(m_btv, BorderLayout.CENTER);

        associateLookup(ExplorerUtils.createLookup(m_manager, map));

        m_rootNode = new RootNode();
        m_manager.setRootContext(m_rootNode);
		setName("DiskExplorer");
    }

    public Image getIcon() {
        return Utilities.loadImage("imagej/envisaje/filemanagement/resources/folder.png");
    }

    public void open() {
        Mode m = WindowManager.getDefault().findMode(MODE);
        m.dockInto(this);
        super.open();
    }

    public ExplorerManager getExplorerManager() {
        return m_manager;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    protected String preferredID() {
        return PREFERRED_ID;
    }

    /** To enable cloning of the top component */
    protected CloneableTopComponent createClonedObject() {
        return new DiskExplorerTopComponent();
    }

    protected void componentActivated() {
        ExplorerUtils.activateActions(m_manager, true);
    }

    protected void componentDeactivated() {
        ExplorerUtils.activateActions(m_manager, false);
    }

    private void log(String str) {
        OutputWriter out = IOProvider.getDefault().getStdOut();
        out.println(str);
        out.flush();
    }

    private static class RootNode extends AbstractNode {

        public RootNode() {
            super(new RootChildren());
        }

        @Override
        public Action getPreferredAction() {
            return new OpenAction();
        }
    }

    private static class OpenAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class RootChildren extends Children.Array {

        public RootChildren() {
            super();
        }

        @Override
        protected java.util.Collection initCollection() {
            File[] driveRoots = File.listRoots();
            ArrayList result = new ArrayList(driveRoots.length);

            for (int i = 0; i < driveRoots.length; i++) {
                File f = FileUtil.normalizeFile(driveRoots[i]);
                FileObject fo = FileUtil.toFileObject(f);
                if (fo != null) {
                    try {
                        DataObject dobj = DataObject.find(fo);
                        result.add(dobj.getNodeDelegate());
                    } catch (DataObjectNotFoundException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            }

            return result;
        }
    }
    private static final long serialVersionUID = -4406860213735839129L;
}
