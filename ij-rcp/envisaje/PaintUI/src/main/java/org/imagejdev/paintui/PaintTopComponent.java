/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.imagejdev.paintui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.imagejdev.imagine.api.editing.UndoManager;
import org.imagejdev.imagine.api.editor.IO;
import org.imagejdev.imagine.api.image.Layer;
import org.imagejdev.imagine.api.image.Picture;
import org.imagejdev.imagine.api.image.Surface;
import org.imagejdev.imagine.api.selection.Selection;
import org.imagejdev.imagine.api.selection.Selection.Op;
import org.imagejdev.imagine.spi.image.LayerImplementation;
import org.imagejdev.imagine.spi.image.PictureImplementation;
import org.imagejdev.imagine.spi.image.SurfaceImplementation;
import org.imagejdev.imagine.spi.tools.Tool;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author Timothy Boudreau
 */
public class PaintTopComponent extends TopComponent implements ChangeListener, LookupListener, IO {

    private final PaintCanvas canvas; //The component the user draws on
    private static int ct = 0; //A counter we use to provide names for new images
    private File file;
    public static DataFlavor LAYER_DATA_FLAVOR =
            new DataFlavor(Layer.class, NbBundle.getMessage(AppPicture.class, "LAYER_CLIPBOARD_NAME")); //NOI18N

    public PaintTopComponent() {
        this(new PaintCanvas());
        init();
    }

    public static PaintTopComponent tcFor(Picture p) {
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        for (TopComponent tc : tcs) {
            if (tc instanceof PaintTopComponent) {
                PaintTopComponent ptc = (PaintTopComponent) tc;
                if (ptc.canvas.getPicture().getPicture() == p) {
                    return ptc;
                }
            }
        }
        return null;
    }

    public void pictureResized(int width, int height) {
        canvas.pictureResized(width, height);
        doLayout();
    }

    // from a file...
    public PaintTopComponent(BufferedImage img, File origin) throws IOException {
        this(new PaintCanvas(img));
        this.file = file;
        updateActivatedNode(origin);
        init();
        setDisplayName(origin.getName());
    }

    public PaintTopComponent(Dimension dim) {
        this(new PaintCanvas(dim));
        init();
    }

    public PaintTopComponent(Dimension dim, boolean xpar) {
        this(new PaintCanvas(dim, xpar));
        init();
    }

    private void init() {
        undoManager.discardAllEdits(); //XXX because we paint it white
        setOpaque(true);
        setBackground(Color.WHITE);
    }

    private void updateActivatedNode(File origin) throws IOException {
        FileObject fob = FileUtil.toFileObject(FileUtil.normalizeFile(origin));
        if (fob != null) {
            DataObject dob = DataObject.find(fob);
            setActivatedNodes(new Node[]{dob.getNodeDelegate()});
        }
    }

    private PaintTopComponent(PaintCanvas canvas) {
        this.canvas = canvas;
        String displayName = NbBundle.getMessage(
                PaintTopComponent.class,
                "UnsavedImageNameFormat", //NOI18N
                new Object[]{new Integer(ct++)});
        setDisplayName(displayName);
        PictureImplementation picture = canvas.getPicture();
        picture.addChangeListener(this);
        stateChanged(new ChangeEvent(picture));
        setPreferredSize(new Dimension(500, 500));

        setLayout(new BorderLayout());
        JScrollPane pane = new JScrollPane(canvas);
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.setViewportBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                UIManager.getColor("controlShadow"))); //NOI18N
        add(pane, BorderLayout.CENTER);
        undoManager.setLimit(UNDO_LIMIT);
    }
    private static final int UNDO_LIMIT = 15;

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    @Override
    public String preferredID() {
        return this.file == null ? "Image" : this.file.getName(); //NOI18N
    }

    public void selectAll() {
        PictureImplementation l = canvas.getPicture();
        if (l.getActiveLayer() == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        LayerImplementation layer = l.getActiveLayer();
        Selection s = layer.getLookup().lookup(Selection.class);
        s.add(new Rectangle(l.getSize()), Op.REPLACE);
        repaint();
    }

    public void clearSelection() {
        PictureImplementation l = canvas.getPicture();
        if (l.getActiveLayer() == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        LayerImplementation layer = l.getActiveLayer();
        Selection s = layer.getLookup().lookup(Selection.class);
        if (s != null) {
            s.clear();
            repaint();
        }
    }

    public void invertSelection() {
        PictureImplementation l = canvas.getPicture();
        if (l.getActiveLayer() == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        LayerImplementation layer = l.getActiveLayer();
        Selection s = layer.getLookup().lookup(Selection.class);
        if (s != null) {
            Dimension d = l.getSize();
            s.invert(new Rectangle(0, 0, d.width, d.height));
            repaint();
        }
    }
    private final UndoRedo.Manager undoManager = new UndoMgr();

    public static final class UndoMgr extends UndoRedo.Manager implements UndoManager {

        public List getEdits() {
            return new ArrayList(super.edits);
        }

        @Override
        public synchronized boolean addEdit(UndoableEdit anEdit) {
            return super.addEdit(anEdit);
        }

        @Override
        public synchronized void redo() throws CannotRedoException {
            super.redo();
        }

        @Override
        public synchronized void undo() throws CannotUndoException {
            super.undo();
        }
    }

    @Override
    public UndoRedo getUndoRedo() {
        return undoManager;
    }
    private LayerImplementation lastActiveLayer = null;

    public void stateChanged(ChangeEvent e) {
        PictureImplementation picture = canvas.getPicture();
        LayerImplementation layerImpl = picture.getActiveLayer();
        if (layerImpl != lastActiveLayer && lastActiveLayer != null) {
            SurfaceImplementation lastSurface = lastActiveLayer.getSurface();
            if (lastSurface != null && lastActiveLayer != null) {
                //Force it to dispose its undo data
                lastActiveLayer.getSurface().setTool(null);
            }
        }
        //What the heck was this?
        /*
        Collection <? extends TopComponent> tcs = layerImpl.getLookup().lookupAll (TopComponent.class);
        for (TopComponent tc : tcs) {
        tc.open();
        tc.requestVisible();
        }
        layerTopComponents.removeAll(tcs);
        for (TopComponent old : layerTopComponents) {
        old.close();
        }
        layerTopComponents.clear();
        for (TopComponent nue : tcs) {
        layerTopComponents.add (nue);
        }
         */

        List l = new ArrayList(10);
        l.add(this);
        l.addAll(Arrays.asList(this, canvas.zoomImpl,
                getUndoRedo(), picture.getPicture()));
        if (layerImpl != null) {
            l.add(picture.getPicture().getLayers());
            Layer layer = layerImpl.getLookup().lookup(Layer.class);
            if (layer != null) {
                l.add(layer);
            }
            Selection sel = layerImpl.getLookup().lookup(Selection.class);
            if (sel != null) {
                l.add(sel);
            }
            Surface surf = layerImpl.getLookup().lookup(Surface.class);
            if (surf != null) {
                l.add(surf);
            }
        }
        System.err.println("Lookup contents set to " + l);
        UIContextLookupProvider.set(l);
        updateActiveTool();
    }

    public void save() throws IOException {
        if (this.file != null) {
            doSave(file);
        } else {
            saveAs();
        }
    }

    public void saveAs() throws IOException {
        JFileChooser ch = FileChooserUtils.getFileChooser("image");
        if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION
                && ch.getSelectedFile() != null) {

            File f = ch.getSelectedFile();
            if (!f.getPath().endsWith(".png")) { //NOI18N
                f = new File(f.getPath() + ".png"); //NOI18N
            }
            if (!f.exists()) {
                if (!f.createNewFile()) {
                    String failMsg = NbBundle.getMessage(
                            PaintTopComponent.class,
                            "MSG_SaveFailed", new Object[]{f.getPath()} //NOI18N
                            );
                    JOptionPane.showMessageDialog(this, failMsg);
                    return;
                }
            } else {
                String overwriteMsg = NbBundle.getMessage(
                        PaintTopComponent.class,
                        "MSG_Overwrite", new Object[]{f.getPath()} //NOI18N
                        );
                if (JOptionPane.showConfirmDialog(this, overwriteMsg)
                        != JOptionPane.OK_OPTION) {

                    return;
                }
            }
            doSave(f);
        }
    }

    private void doSave(File f) throws IOException {
        BufferedImage img = canvas.getImage();
        ImageIO.write(img, "png", f);
        this.file = f;
        String statusMsg = NbBundle.getMessage(PaintTopComponent.class,
                "MSG_Saved", new Object[]{f.getPath()}); //NOI18N
        StatusDisplayer.getDefault().setStatusText(statusMsg);
        setDisplayName(f.getName());
        updateActivatedNode(f);
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(PaintTopComponent.class,
                "MSG_SAVED", f.getPath())); //NOI18N
    }

    @Override
    public void open() {
        //Rare case where we *do* want to do this
        super.open();
        requestActive();
    }

    @Override
    protected void componentActivated() {
        startListening();
        updateActiveTool();
        stateChanged(null);
        canvas.requestFocus();
//        for (TopComponent tc : layerTopComponents) {
//            tc.open();
//        }
    }

    @Override
    protected void componentDeactivated() {
        stopListening();
        setActiveTool(null);
//        for (TopComponent tc : layerTopComponents) {
//            tc.close();
//        }
    }
    boolean firstTime = true;

    @Override
    protected void componentShowing() {
        AppPicture p = canvas.getPicture();
        final int layerCount = p.getLayers().size();
        p.setHibernated(false, false, new Runnable() {

            int ct = 0;
            ProgressHandle h;

            public void run() {
                if (EventQueue.isDispatchThread()) {
                    repaint();
                    return;
                }
                ct++;
                if (ct == 1) {
                    h = ProgressHandleFactory.createHandle(NbBundle.getMessage(PaintTopComponent.class,
                            "MSG_UNHIBERNATING")); //NOI18N
                    h.start();
                    h.switchToDeterminate(layerCount);
                }
                if (h != null) {
                    h.progress(ct);
                }
                if (ct == layerCount - 1) {
                    if (h != null) {
                        h.finish();
                    }
                    EventQueue.invokeLater(this);
                }
            }
        });
    }

    @Override
    protected void componentHidden() {
        canvas.getPicture().setHibernated(true, false, null);
    }

    public void resultChanged(LookupEvent lookupEvent) {
        updateActiveTool();
    }
    private Tool tool = null;

    private void setActiveTool(Tool tool) {
        if (tool != this.tool) {
            this.tool = tool;
            canvas.setActiveTool(tool);
        }
    }
    private Lookup.Result tools = null;

    private void startListening() {
        tools = Utilities.actionsGlobalContext().lookup(new Lookup.Template(Tool.class));
        tools.addLookupListener(this);
    }

    private void stopListening() {
        tools.removeLookupListener(this);
        tools = null;
    }

    private void updateActiveTool() {
        if (TopComponent.getRegistry().getActivated() == this) {
            Collection c = tools.allInstances();
            setActiveTool(c.size() == 0 ? null : (Tool) c.iterator().next());
        }
    }

    @Override
    protected void componentClosed() {
        if (UIContextLookupProvider.lookup(PaintTopComponent.class) == this) {
            UIContextLookupProvider.set(new Object[0]);
            if (lastActiveLayer != null) {
                lastActiveLayer.getSurface().setTool(null);
            }
        }

        super.componentClosed();
        undoManager.discardAllEdits();
    }

    public void reload() throws IOException {
    }

    public boolean canReload() {
        Node[] n = getActivatedNodes();
        if (n.length == 1) {
            DataObject dob = (DataObject) n[0].getCookie(DataObject.class);
            return dob.getPrimaryFile().isValid();
        }
        return false;
    }

    boolean isActive() {
        return TopComponent.getRegistry().getActivated() == this;
    }
}
