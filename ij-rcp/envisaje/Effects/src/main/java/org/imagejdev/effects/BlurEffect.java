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

package org.imagejdev.effects;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.imagejdev.imagine.api.util.GraphicsUtils;
import org.imagejdev.imagine.spi.effects.Effect;
import org.imagejdev.imagine.spi.effects.Effect.Applicator;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Composite which implements a poor-man's blur effect (rubber stamp the
 * image around itself with varying degrees of opacity).
 *
 * @author Timothy Boudreau
 */

@ServiceProvider(service=Effect.class)

public class BlurEffect implements Effect, Effect.Applicator {

    public String getName() {
	return NbBundle.getMessage(BlurEffect.class,
		"NAME_Blur"); //NOI18N
    }

    public Applicator getApplicator() {
	return this;
    }

    public JPanel getCustomizer() {
	return null;
    }

    public Composite getComposite() {
	return new BlurComposite();
    }

    public void addChangeListener(ChangeListener cl) {
	//do nothing
    }

    public void removeChangeListener(ChangeListener cl) {
	//do nothing
    }

    public boolean canPreview() {
	return false;
    }

    public boolean canApply() {
	return true;
    }

    private static class BlurComposite implements Composite {
	public CompositeContext createContext(ColorModel srcColorModel,
					      ColorModel dstColorModel,
				              RenderingHints hints) {
	    return new Ctx (srcColorModel, dstColorModel);
	}

        public String toString() {
            return NbBundle.getMessage (getClass(), "LBL_Blur");
        }
    }

    static final class Ctx implements CompositeContext {
	private ColorModel src;
	private ColorModel dst;
	Ctx (ColorModel src, ColorModel dst) {
	    this.src = src;
	    this.dst = dst;
	}

	public void dispose() {
	    src = null;
	    dst = null;
	}

	int depth = 8;
	int alphaFactor = 3;
	boolean skipCenter = false;
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
	    WritableRaster x = Raster.createWritableRaster(src.getSampleModel(), new Point (src.getMinX(), src.getMinY()));
	    BufferedImage a = new BufferedImage (src.getWidth(), src.getHeight(), 
                    GraphicsUtils.DEFAULT_BUFFERED_IMAGE_TYPE);
	    a.getRaster().setDataElements(0, 0, src);

	    BufferedImage out = new BufferedImage (this.dst, dstOut, false, new Hashtable());

	    AffineTransform at = AffineTransform.getTranslateInstance(0, -0);
	    int ct = depth;
	    Graphics2D outGr = (Graphics2D) out.createGraphics();
	    for (int i=ct; i >= 0; i--) {
		float alpha = 1f / (float) ((i + 1) * alphaFactor);
		AlphaComposite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		if (skipCenter && alpha > 0.7) {
		    continue;
		}
		outGr.setComposite(comp);
		at.setToTranslation(-i, -i);
		outGr.drawRenderedImage (a, at);
		at.setToTranslation(-i, 0);
		outGr.drawRenderedImage (a, at);
		at.setToTranslation(0, -i);
		outGr.drawRenderedImage (a, at);
		at.setToTranslation(i, i);
		outGr.drawRenderedImage (a, at);
		at.setToTranslation(i, 0);
		outGr.drawRenderedImage (a, at);
		at.setToTranslation(0, i);
		outGr.drawRenderedImage (a, at);
	    }
	    outGr.dispose();
	}
    }
}
