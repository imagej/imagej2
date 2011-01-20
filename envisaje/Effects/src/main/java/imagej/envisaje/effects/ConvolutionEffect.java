/*
 * ConvolutionEffect.java
 *
 * Created on August 3, 2006, 10:13 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.effects;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.ImagingOpException;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import imagej.envisaje.spi.effects.Effect;
import imagej.envisaje.spi.effects.Effect.Applicator;

/**
 *
 * @author Tim Boudreau
 */
abstract class ConvolutionEffect implements Effect, Effect.Applicator, Composite, CompositeContext {
    private final String name;

    protected ConvolutionEffect(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Applicator getApplicator() {
        return this;
    }

    public final Composite getComposite() {
        return this;
    }

    protected abstract Kernel createKernel(int width, int height);

    private final List <ChangeListener> listeners = 
            Collections.synchronizedList(new LinkedList <ChangeListener> ());
    
    public void addChangeListener(ChangeListener cl) {
        listeners.add (cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        listeners.remove (cl);
    }

    protected void fire() {
        ChangeListener[] l= listeners.toArray(new ChangeListener[0]);
        for (int i = 0; i < l.length; i++) {
            l[i].stateChanged(new ChangeEvent(this));
        }
    }

    public boolean canPreview() {
        return true;
    }

    public boolean canApply() {
        return true;
    }

    public final CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return this;
    }

    public void dispose() {
    }

    public final void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
        try {
            ConvolveOp op = new ConvolveOp (createKernel(src.getWidth(), src.getHeight()));
            op.filter(src, dstOut);
        } catch (ImagingOpException e) {
            e.printStackTrace();
        }
    }
}
