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

package imagej.envisaje.effects;

import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import imagej.envisaje.spi.effects.Effect;
import imagej.envisaje.spi.effects.Effect.Applicator;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Hue/Saturation/Brightness effect
 *
 * @author Timothy Boudreau
 */
@ServiceProvider(service=Effect.class)

public class HSVEffect implements Effect {

    public String getName() {
	return NbBundle.getMessage(HSVEffect.class,
		"NAME_HSV"); //NOI18N
    }

    public Applicator getApplicator() {
	return new HSVApplicator();
    }

    static final class HSVApplicator implements Applicator, Composite {
	private final List <ChangeListener> listeners = 
                Collections.synchronizedList(new LinkedList <ChangeListener> ());
	public JPanel getCustomizer() {
	    return new HSVCustomizer (this);
	}

	public Composite getComposite() {
	    return this;
	}

	public void addChangeListener(ChangeListener cl) {
	    listeners.add (cl);
	}

	public void removeChangeListener(ChangeListener cl) {
	    listeners.remove (cl);
	}

	private void fire() {
	    ChangeListener[] l = listeners.toArray (
		    new ChangeListener[0]);

	    for (int i=0; i < l.length; i++) {
		l[i].stateChanged(new ChangeEvent(this));
	    }
	}

	public boolean canPreview() {
	    return true;
	}

	public boolean canApply() {
	    return hue != 0.0f || sat != 0.0f || bri != 0.0f;
	}

	private float hue, sat, bri = 0.0f;
	public void setParameters (float hue, float sat, float bri) {
	    this.hue = hue;
	    this.sat = sat;
	    this.bri = bri;
	    fire();
	}

	public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
	    return new Ctx(srcColorModel, dstColorModel, hue, sat, bri);
	}

        @Override
        public String toString() {
            return NbBundle.getMessage (getClass(), "LBL_HueSatBri");
        }
    }


    private static final class Ctx implements CompositeContext {
	private ColorModel src;
	private ColorModel dst;
	private final float hue, sat, bri;
	Ctx (ColorModel src, ColorModel dst, float hue, float sat, float bri) {
	    this.src = src;
	    this.dst = dst;
	    this.hue = hue;
	    this.sat = sat;
	    this.bri = bri;
	}

	public void dispose() {
	    src = null;
	    dst = null;
	}

	private float constrain (float val) {
	    return Math.max (-1f, Math.min (1f, val));
	}

	private float rotate (float val) {
	    if (val > 1.0f) {
		val = -1.0f + (val - 1.0f);
	    } else if (val < -1.0f) {
		val = 1.0f + (val + (-1.0f));
	    }
	    return val;
	}

	public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
	    Rectangle r = dstOut.getBounds();
	    int[] arr = new int[4];
	    float[] hsb = new float[3];
	    for (int x=r.x; x < r.x + r.width; x++) {
		for (int y=r.y; y < r.y + r.height; y++) {
		    src.getPixel(x, y, arr);

		    Color.RGBtoHSB(arr[0], arr[1], arr[2], hsb);
		    hsb[0] += hue;
		    hsb[1] += sat;
		    hsb[2] += bri;

		    int outColor = Color.HSBtoRGB(
			    rotate(hsb[0]),
			    constrain(hsb[1]),
			    constrain(hsb[2]));

		    Color c = new Color (outColor);
		    arr[0] = c.getRed();
		    arr[1] = c.getGreen();
		    arr[2] = c.getBlue();

		    dstOut.setPixel(x, y, arr);
		}
	    }
	}
    }
}
