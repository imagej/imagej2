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
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.imagejdev.imagine.spi.effects.Effect;
import org.imagejdev.imagine.spi.effects.Effect.Applicator;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Timothy Boudreau
 */
@ServiceProvider(service=Effect.class)

public class DropShadowEffect implements Effect, Effect.Applicator {

    public String getName() {
	return NbBundle.getMessage(MonochromeEffect.class,
		"NAME_DropShadow"); //NOI18N
    }

    public Applicator getApplicator() {
	return this;
    }

    public JPanel getCustomizer() {
	return null;
    }

    public Composite getComposite() {
	return new DropShadowComposite();
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

    private static class DropShadowComposite implements Composite {
	public CompositeContext createContext(ColorModel srcColorModel,
		ColorModel dstColorModel, RenderingHints hints) {

	    return new Ctx (srcColorModel, dstColorModel);
	}

        public String toString() {
            return NbBundle.getMessage (getClass(), "LBL_DropShadow");
        }
    }

    private static final class Ctx implements CompositeContext {
	private final ColorModel srcMdl;
	private final ColorModel dstMdl;
	private final MonochromeEffect.Ctx mono;
	private final BlurEffect.Ctx blur;
	private final CompositeContext alpha;

	Ctx (ColorModel src, ColorModel dst) {
	    srcMdl = src;
	    dstMdl = dst;
	    mono = new MonochromeEffect.Ctx (srcMdl, dstMdl);
	    blur = new BlurEffect.Ctx (srcMdl, dstMdl);
	    blur.alphaFactor = 2;
	    blur.skipCenter = true;
	    mono.inc = 64;

	    HashMap <RenderingHints.Key, Object> hints =
                    new HashMap<RenderingHints.Key, Object>();

	    hints.put (RenderingHints.KEY_ALPHA_INTERPOLATION,
		    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	    hints.put (RenderingHints.KEY_RENDERING,
		    RenderingHints.VALUE_RENDER_QUALITY);

	    alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
		    1.0f).createContext(srcMdl,
		    dstMdl, new RenderingHints(hints));
	}

	public void dispose() {
	    mono.dispose();
	    blur.dispose();
	    alpha.dispose();
	}

	private static final int SHADOW_OFFSET = 3;
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
	    WritableRaster xr = src.createWritableRaster(src.getSampleModel(),
		    new Point (src.getMinX(), src.getMinY()));

	    int x = src.getMinX();
	    int y = src.getMinY();
	    int w = src.getWidth();
	    int h = src.getHeight();

	    mono.compose (src, dstIn, xr);
	    int[] arr = new int[4];
	    for (int i=(w + x) - (SHADOW_OFFSET+1); i >= x + SHADOW_OFFSET; i--) {
		for (int j=(y + h) - (SHADOW_OFFSET+1); j >= y + SHADOW_OFFSET; j--) {
		    xr.getPixel(i, j, arr);
		    xr.setPixel(i + SHADOW_OFFSET, j + SHADOW_OFFSET, arr);
		}
	    }
	    blur.compose (xr, dstIn, dstOut);
	    alpha.compose (src, dstIn, dstOut);
	}
    }


}
