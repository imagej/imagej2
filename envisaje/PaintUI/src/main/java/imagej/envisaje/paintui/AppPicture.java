/*
 *
 * Sun Public License Notice
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
package imagej.envisaje.paintui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import imagej.envisaje.api.editing.LayerFactory;
import imagej.envisaje.api.image.Hibernator;
import imagej.envisaje.api.selection.Selection;
import imagej.envisaje.api.util.ChangeListenerSupport;
import imagej.envisaje.api.util.GraphicsUtils;
import imagej.envisaje.api.util.RasterConverter;
import imagej.envisaje.spi.image.LayerImplementation;
import imagej.envisaje.spi.image.PictureImplementation;
import imagej.envisaje.spi.image.RepaintHandle;
import imagej.envisaje.spi.image.SurfaceImplementation;

import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * Implementation of the Layers interface.  Things worth noting:
 * All operations are automatically undoable.  State is kept in a
 * LayersState object which is cloned when an operation is started,
 * and if redo is called, restored.
 * <p>
 * Mainly this class is a container for a collection of LayerImpl
 * instances representing the actual images.
 *
 * @author Timothy Boudreau
 */
class AppPicture extends PictureImplementation {

    private LayersState state;

    public AppPicture(RepaintHandle handle, Dimension size) {
        addRepaintHandle(handle);
        state = new LayersState(this);
        state.size.setSize(size);
        LayerImplementation initial = createInitialLayer(size);
        assert initial != null;
        state.layers.add(initial);
        state.activeLayer = initial;
    }

    public AppPicture(RepaintHandle handle, BufferedImage img) {
        addRepaintHandle(handle);
        state = new LayersState(this);
        state.size.width = img.getWidth();
        state.size.height = img.getHeight();

        Dimension d = new Dimension(img.getWidth(), img.getHeight());
        LayerFactory lf = RasterConverter.getLayerFactory();
        if (lf == null) {
            lf = LayerFactory.getDefault();
        }
        LayerImplementation initial =
                lf.createLayer(getDefaultLayerName(1), handle, d);
        Graphics2D g = initial.getSurface().getGraphics();
        try {
            g.drawRenderedImage(img, AffineTransform.getTranslateInstance(0, 0));
        } catch (NullPointerException e) {
            //XXX do nothing for now - ordering issue
        } finally {
            g.dispose();
        }
        state.layers.add(initial);
        state.activeLayer = initial;
    }

    void resized(int width, int height) {
        state.size = new Dimension(width, height);
    }

    private LayerImplementation createInitialLayer(Dimension d) {
        LayerImplementation impl =
                LayerFactory.getDefault().createLayer(getDefaultLayerName(1), getMasterRepaintHandle(), d);
        return impl;
    }

    private static String getDefaultLayerName(int ix) {
        return NbBundle.getMessage(AppPicture.class, "LAYER_NAME", "" + ix);
    }

    public void add(int ix, LayerImplementation l) {
        if (l == null) {
            throw new NullPointerException("Null layer"); //NOI18N
        }
        if (ix == POSITION_BOTTOM) {
            ix = 0;
        } else if (ix == POSITION_TOP) {
            ix = state.layers.size() - 1;
        }
        l.addRepaintHandle(getMasterRepaintHandle());
        state.layers.add(ix, l);
        //XXX what is 1000 here?
        getMasterRepaintHandle().repaintArea(0, 0, 1000, 1000);
        setActiveLayer(l);
    }

    public RepaintHandle getRepaintHandle() {
        return getMasterRepaintHandle();
    }

    public boolean paint(Graphics2D g, Rectangle r, boolean showSelection) {
        if (hibernated) {
            return false;
        }
        boolean result = false;
        for (Iterator i = state.layers.iterator(); i.hasNext();) {
            LayerImplementation l = (LayerImplementation) i.next();
            result |= l.paint(g, r, showSelection);
        }
        return result;
    }

    public List<LayerImplementation> getLayers() {
        return Collections.unmodifiableList(state.layers);
    }

    public void move(LayerImplementation layer, int pos) {
        int oldPos = state.layers.indexOf(layer);

        if (oldPos == -1) {
            throw new IllegalArgumentException();
        }
        if (oldPos == pos) {
            return;
        }
        beginUndoableOperation(false,
                NbBundle.getMessage(AppPicture.class, "MSG_MOVE_LAYER",
                layer.getName(), "" + pos));
        try {
            state.layers.remove(oldPos);
            if (pos > oldPos) {
                pos--;
            }
            state.layers.add(pos, layer);
        } catch (RuntimeException re) {
            cancelUndoableOperation();
            throw re;
        }
        endUndoableOperation();
        fire();
    }

    public void delete(LayerImplementation layer) {
        beginUndoableOperation(false, NbBundle.getMessage(AppPicture.class,
                "MSG_DELETE_LAYER", layer.getName()));
        try {
            int ix = state.layers.indexOf(layer);

            if (ix == -1) {
                throw new IllegalArgumentException();
            }
            state.layers.remove(layer);
            layer.removeRepaintHandle(getMasterRepaintHandle());
            if (state.activeLayer == layer) {
                if (ix != 0) {
                    state.activeLayer = getLayer(ix - 1);
                } else {
                    if (state.layers.size() > 0) {
                        state.activeLayer = getLayer(0);
                    } else {
                        state.activeLayer = null;
                    }
                }
            }
        } catch (RuntimeException re) {
            cancelUndoableOperation();
            throw re;
        }
        endUndoableOperation();
        fire();
    }

    public LayerImplementation getActiveLayer() {
        return state.activeLayer;
    }

    LayerImplementation activeLayer() {
        return getActiveLayer();
    }
    Selection storedSelection;

    public void setActiveLayer(LayerImplementation l) {
        if (l != state.activeLayer) {
            beginUndoableOperation(false, l == null ? NbBundle.getMessage(AppPicture.class,
                    "MSG_CLEAR_ACTIVE_LAYER") : NbBundle.getMessage(AppPicture.class,
                    "MSG_ACTIVATE_LAYER", l.getName()));
            /*
            try {
            assert l == null || state.layers.contains(l);
            //XXX shouldn't we fire after we set the field?
            //                fire();
            }
            catch (RuntimeException re) {
            cancelUndoableOperation();
            throw re;
            }
             */
            LayerImplementation old = state.activeLayer;
            state.activeLayer = l;
            Selection oldSelection = old == null ? storedSelection : old.getLookup().lookup(Selection.class);
            if (oldSelection != null) {
                Selection newSelection = l == null ? null : l.getLookup().lookup(Selection.class);
                if (newSelection != null) {
                    newSelection.translateFrom(oldSelection);
                    oldSelection.clearNoUndo();
                    storedSelection = null;
                } else {
                    storedSelection = oldSelection;
                }
            }
            endUndoableOperation();
            fire();
        }
    }

    public LayerImplementation add(int index) {
        beginUndoableOperation(false, NbBundle.getMessage(AppPicture.class,
                "MSG_ADD_NEW_LAYER"));
        LayerFactory factory;
        if (state.layers.isEmpty()) {
            factory = LayerFactory.getDefault();
        } else {
            factory = (state.layers.get(state.layers.size() - 1)).getLookup().lookup(LayerFactory.class);
        }

        LayerImplementation result;

        try {
            result = factory.createLayer("foo", getMasterRepaintHandle(), //XXX
                    state.size);
            if (index == POSITION_TOP) {
                state.layers.add(result);
            } else if (index == POSITION_BOTTOM) {
                state.layers.add(0, result);
            } else {
                state.layers.add(index, result);
            }
            setActiveLayer(result);
        } catch (RuntimeException re) {
            cancelUndoableOperation();
            throw re;
        }
        endUndoableOperation();
        return result;
    }

    private LayerImplementation getLayer(int ix) {
        return (LayerImplementation) state.layers.get(ix);
    }

    public LayerImplementation duplicate(LayerImplementation toClone) {
        beginUndoableOperation(false, NbBundle.getMessage(AppPicture.class,
                "MSG_DUPLICATE_LAYER", toClone.getName()));
        LayerImplementation nue;

        try {
            int ix = state.layers.indexOf(toClone);
            if (ix == -1) {
                throw new IllegalArgumentException();
            }
            nue = getLayer(ix).clone(true, true);
            state.layers.add(ix, nue);
            setActiveLayer(nue);
        } catch (RuntimeException re) {
            cancelUndoableOperation();
            throw re;
        }
        endUndoableOperation();
        return nue;
    }
    private ChangeListenerSupport changes = new ChangeListenerSupport(this);
    private boolean pendingChange = false;

    private void fire() {
        // We suspend firing until the end of an undoable operation
        if (undoEntryCount <= 0) {
            changes.fire();
            pendingChange = false;
        } else {
            pendingChange = true;
        }
    }

    public void addChangeListener(ChangeListener cl) {
        changes.add(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        changes.remove(cl);
    }

    public Dimension getSize() {
        return new Dimension(state.size);
    }

    public void repaintArea(int x, int y, int w, int h) {
        getMasterRepaintHandle().repaintArea(x, y, w, h);
    }

    public void flatten() {
        beginUndoableOperation(false, NbBundle.getMessage(AppPicture.class,
                "MSG_FLATTEN_LAYERS"));
        try {
            LayerImplementation nue = LayerFactory.getDefault().createLayer("foo", //XXX
                    getMasterRepaintHandle(), getSize());
            SurfaceImplementation surface = nue.getSurface();
            if (surface != null) {
                this.paint(surface.getGraphics(), null, true);
            } else {
                Logger.getLogger("global").log(Level.SEVERE,
                        "Tried to flatten image but default layer factory"
                        + "provides a layer instance with no surface to "
                        + "paint into");
            }
            state.layers.clear();
            state.layers.add(nue);
            state.activeLayer = nue;
            fire();
        } catch (RuntimeException re) {
            cancelUndoableOperation();
            throw re;
        }
        endUndoableOperation();
    }
    private LayersUndoableOperation currentOp = null;
    private int undoEntryCount = 0;

    private void beginUndoableOperation(boolean needDeepCopy, String what) {
        UndoManager undo = (UndoManager) Utilities.actionsGlobalContext().lookup(UndoManager.class);

        if (undo != null) {
            undoEntryCount++;
            if (currentOp == null) {
                currentOp = new LayersUndoableOperation(state, needDeepCopy);
            } else if (needDeepCopy != currentOp.isDeepCopy()) {
                currentOp.becomeDeepCopy();
            }
            currentOp.opName = what;
        }
    }

    private void cancelUndoableOperation() {
        if (currentOp != null) {
            currentOp.undo();
        }
        currentOp = null;
        undoEntryCount = 0;
        pendingChange = false;
    }

    private void endUndoableOperation() {
        undoEntryCount--;
        if (undoEntryCount == 0) {
            UndoManager undo = (UndoManager) Utilities.actionsGlobalContext().lookup(UndoManager.class);

            if (undo != null) {
                assert undo != null;
                assert currentOp != null;
                undo.undoableEditHappened(new UndoableEditEvent(this, currentOp));
                // Thread.dumpStack();
                currentOp = null;
            }
            if (pendingChange) {
                fire();
            }
        }
    }
    private boolean hibernated = false;

    @Override
    public void hibernate() {
        setHibernated(true, false, null);
    }

    @Override
    public void wakeup(boolean immediate, Runnable run) {
        setHibernated(false, immediate, run);
    }

    synchronized void setHibernated(boolean val, boolean immediate, Runnable run) {
        if (hibernated == val) {
            return;
        }
        Dimension d = getSize();
        hibernated = val;
        LayerImplementation[] layers = (LayerImplementation[]) state.layers.toArray(new LayerImplementation[state.layers.size()]);

        for (int i = 0; i < layers.length; i++) {
            Hibernator hibernator = layers[i].getLookup().lookup(Hibernator.class);
            if (hibernator != null) {
                if (val) {
                    hibernator.hibernate();
                } else {
                    hibernator.wakeup(immediate, run);
                }
            }
        }
    }

    /**
     *
     * Replaceable object which holds all stateful fields for a
     * Layers instance.  It is copied whenever an operation is performed
     * which may need to be undone layer.  Can create a deep copy of
     * all LayerImpls associated with it if needed.
     */
    private static final class LayersState {

        public Dimension size = new Dimension();
        public List<LayerImplementation> layers =
                new LinkedList<LayerImplementation>();
        public LayerImplementation activeLayer = null;
        private final AppPicture owner;

        LayersState(LayersState other, boolean deepCopy) {
            this.owner = other.owner;
            this.size = new Dimension(other.size);
            this.activeLayer = other.activeLayer;
            if (deepCopy) {
                deepCopy(other.layers);
            } else {
                layers.addAll(other.layers);
            }
        }

        LayersState(AppPicture owner) {
            this.owner = owner;
        }

        void restore() {
            owner.state = this;
            owner.fire();
        }

        void becomeDeepCopy() {
            deepCopy(this.layers);
        }

        void deepCopy(java.util.List layers) {
            java.util.List newLayers = new LinkedList();

            for (Iterator i = layers.iterator(); i.hasNext();) {
                LayerImplementation layer = (LayerImplementation) i.next();
                LayerImplementation nue = layer.clone(false, true);

                if (activeLayer == layer) {
                    activeLayer = nue;
                }
                newLayers.add(nue);
            }
            this.layers = newLayers;
        }
    }

    private static class LayersUndoableOperation implements UndoableEdit {

        private LayersState state;
        private boolean deepCopy;
        String opName = NbBundle.getMessage(AppPicture.class,
                "LBL_UNKNOWN_UNDOABLE_OP");

        LayersUndoableOperation(LayersState state, boolean deepCopy) {
            this.state = new LayersState(state, deepCopy);
            this.deepCopy = deepCopy;
        }

        boolean isDeepCopy() {
            return deepCopy;
        }
        LayersState redoState = null;

        public void undo() throws CannotUndoException {
            redoState = new LayersState(state.owner.state, deepCopy);
            state.restore();
            isRedo = true;
        }

        void becomeDeepCopy() {
            state.becomeDeepCopy();
            deepCopy = true;
        }

        public boolean canUndo() {
            return !isRedo;
        }

        public void redo() throws CannotRedoException {
            redoState.restore();
            isRedo = false;
        }
        boolean isRedo = false;

        public boolean canRedo() {
            return isRedo;
        }

        public void die() {
            state = null;
            redoState = null;
        }

        public boolean addEdit(UndoableEdit anEdit) {
            return false;
        }

        public boolean replaceEdit(UndoableEdit anEdit) {
            return false;
        }

        public boolean isSignificant() {
            return true;
        }

        public String getPresentationName() {
            return opName;
        }

        public String getUndoPresentationName() {
            return opName;
        }

        public String getRedoPresentationName() {
            return opName;
        }
    }

    @Override
    public Transferable copy(Clipboard clipboard, boolean allLayers) {
        LayerSelection sel = new LayerSelection(allLayers, false);
        return sel;
    }

    @Override
    public Transferable cut(Clipboard clipboard, boolean allLayers) {
        LayerSelection sel = new LayerSelection(allLayers, true);
        return sel;
    }

    @Override
    public boolean paste(Clipboard clipboard) {
        if (clipboard.getContents(this) != null) {
            for (DataFlavor flavor : clipboard.getAvailableDataFlavors()) {
                if (DataFlavor.imageFlavor.equals(flavor)) {
                    try {
                        Image img = (Image) clipboard.getData(flavor);
                        if (!(img instanceof BufferedImage)) {
                            int w = img.getWidth(null);
                            int h = img.getHeight(null);
                            LayerImplementation current = getActiveLayer();
                            Selection sel = current == null ? null
                                    : getActiveLayer().getLookup().lookup(Selection.class);
                            Shape clip = sel == null ? null : sel.asShape();
                            if (clip != null && current != null) {
                                Point loc = current.getBounds().getLocation();
                                if (loc.x != 0 || loc.y != 0) {
                                    AffineTransform xform =
                                            AffineTransform.getTranslateInstance(-loc.x, -loc.y);
                                    clip = xform.createTransformedShape(clip);
                                }
                            }
                            LayerFactory lf = Lookup.getDefault().lookup(LayerFactory.class);
                            String name = NbBundle.getMessage(AppPicture.class,
                                    "LBL_PASTED_LAYER"); //NOI18N
                            LayerImplementation newLayer = RasterConverter.getLayerFactory().createLayer(name,
                                    getMasterRepaintHandle(), new Dimension(w, h));
                            SurfaceImplementation surf = newLayer.getSurface();
                            Graphics2D g = surf.getGraphics();
                            try {
                                if (clip != null) {
                                    g.setClip(clip);
                                }
                                GraphicsUtils.setHighQualityRenderingHints(g);
                                g.drawImage(img,
                                        AffineTransform.getTranslateInstance(0,
                                        0), null);
                            } finally {
                                g.dispose();
                            }
                            add(0, newLayer);
                        }
                        return true;
                    } catch (UnsupportedFlavorException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return false;
    }

    private class LayerSelection implements Transferable, ClipboardOwner {

        private final boolean allLayers;
        private final boolean isCut;

        LayerSelection(boolean allLayers, boolean isCut) {
            this.allLayers = allLayers;
            this.isCut = isCut;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor, PaintTopComponent.LAYER_DATA_FLAVOR};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(DataFlavor.imageFlavor)
                    || flavor.equals(PaintTopComponent.LAYER_DATA_FLAVOR);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(DataFlavor.imageFlavor)) {
                Dimension d = AppPicture.this.getSize();
                LayerImplementation layer = getActiveLayer();
                if (!allLayers && layer == null) {
                    return null;
                }
                Selection sel = layer == null ? null : layer.getLookup().lookup(
                        Selection.class);
                Shape clip = sel == null ? null : sel.asShape();

                BufferedImage nue = new BufferedImage(d.width, d.height,
                        GraphicsUtils.DEFAULT_BUFFERED_IMAGE_TYPE);
                Graphics2D g = nue.createGraphics();
                GraphicsUtils.setHighQualityRenderingHints(g);
                if (clip != null) {
                    g.setClip(clip);
                }
                if (allLayers) {
                    AppPicture.this.paint(g, null, false);
                } else {
                    layer.paint(g, null, false);
                }
                g.dispose();
                if (!isCut) {
                    Iterable<LayerImplementation> toCutFrom = allLayers ? getLayers()
                            : Collections.singleton(layer);
                    for (LayerImplementation l : toCutFrom) {
                        if (!l.isVisible()) {
                            continue;
                        }
                        SurfaceImplementation impl = l.getSurface();
                        impl.beginUndoableOperation(NbBundle.getMessage(AppPicture.class,
                                "CUT", l.getName())); //NOI18N
                        Graphics2D gg = impl.getGraphics();
                        try {
                            Shape cl = clip;
                            if (cl == null) {
                                cl = layer.getBounds();
                            }
                            if (sel != null) {
                                gg.setColor(new Color(0, 0, 0, 0));
                            }
                            gg.setClip(clip);
                            Rectangle bds = clip.getBounds();
                            gg.setBackground(new Color(0, 0, 0, 0));
                            gg.clearRect(bds.x, bds.y, bds.width, bds.height);
                        } finally {
                            gg.dispose();
                            impl.endUndoableOperation();
                        }
                    }
                }
                if (clip != null) {
                    Rectangle r = clip.getBounds();
                    if (r.width != 0 && r.height != 0) {
                        nue = nue.getSubimage(r.x, r.y, Math.min(nue.getWidth(),
                                r.width), Math.min(nue.getHeight(),
                                r.height));
                    }
                }
                return nue;
            } else if (flavor.equals(PaintTopComponent.LAYER_DATA_FLAVOR)) {
                //XXX pending
                return null;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            //do nothing
        }
    }
}
