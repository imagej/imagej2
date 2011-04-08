package imagej.envisaje.spi.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.util.List;
import javax.swing.event.ChangeListener;
import imagej.envisaje.Accessor;
import imagej.envisaje.api.image.Hibernator;
import imagej.envisaje.api.image.Picture;


/**
 * An ordered stack of one or more images which compose an image.  This 
 * class is implemented by editors to provide editing facilities for
 * pictures.
 * <p/>
 * The only code that should touch this interface directly is the 
 * Picture API class;  modules providing Tools or other facilities should
 * get an instance of Picture from the Utilities.actionsGlobalContext()
 * lookup;  unless you are writing the editor portion of an application,
 * if you are coding to this class you are probably doing something wrong.
 *
 * @author Timothy Boudreau
 */
public abstract class PictureImplementation implements Hibernator {
    public static final int POSITION_BOTTOM = Picture.POSITION_BOTTOM;
    public static final int POSITION_TOP = Picture.POSITION_TOP;
    protected final Picture picture;
    private final RepaintHandleProxySupport proxy =
            new RepaintHandleProxySupport(toString());

    static {
        LayerImplementation.init();
    }

    protected PictureImplementation() {
        this.picture = Accessor.DEFAULT.createPicture(this);
    }

    public abstract Transferable copy(Clipboard clipboard, boolean allLayers);
    public abstract Transferable cut(Clipboard clipboard, boolean allLayers);
    public abstract boolean paste(Clipboard clipboard);

    public abstract Dimension getSize();
    public abstract List<LayerImplementation> getLayers();
    public abstract void move (LayerImplementation layer, int pos);
    public abstract void delete (LayerImplementation layer);
    public abstract LayerImplementation add (int index);
    public abstract LayerImplementation duplicate (LayerImplementation toClone);
    public abstract void addChangeListener (ChangeListener cl);
    public abstract void removeChangeListener (ChangeListener cl);
    public abstract void setActiveLayer (LayerImplementation layer);
    public abstract LayerImplementation getActiveLayer();
    public abstract void flatten();
    public abstract void add (int ix, LayerImplementation l);

    public final Picture getPicture() {
        return picture;
    }

    public abstract boolean paint(Graphics2D g, Rectangle bounds, boolean showSelection);

    public void hibernate() {
        //do nothing
    }

    public void wakeup(boolean immediately) {
        //do nothing
    }

    /**
     * Add a repaint handle that wants to be notified when there is
     * a change in the image.  Subclasses can call repaintArea() on
     * the return value of getMasterRepaintHandle() to update
     * all registered repaint handles.
     * <p>
     * The passed repaint handles are referenced weakly.
     */ 
    public final void addRepaintHandle (RepaintHandle handle) {
        proxy.add (handle);
    }

    public final void removeRepaintHandle (RepaintHandle handle) {
        proxy.remove (handle);
    }

    public final RepaintHandle getMasterRepaintHandle() {
        return proxy;
    }
}
