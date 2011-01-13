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

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.imagejdev.imagine.spi.effects.Effect;
import org.imagejdev.imagine.spi.effects.Effect.Applicator;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Composite which converts an image to monochrome
 *
 * @author Timothy Boudreau
 */

@ServiceProvider(service=Effect.class)

public class InvertEffect implements Effect, Effect.Applicator, Composite {

    public String getName() {
	return NbBundle.getMessage(MonochromeEffect.class,
		"NAME_Invert"); //NOI18N

    }

    public Applicator getApplicator() {
	return this;
    }

    public JPanel getCustomizer() {
	return null;
    }

    public Composite getComposite() {
	return this;
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

    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
	return new Ctx (srcColorModel, dstColorModel);
    }

    public String toString() {
        return NbBundle.getMessage (getClass(), "LBL_Invert");
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

	public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
	    Rectangle r = dstOut.getBounds();
	    int[] arr = new int[4];
	    for (int x=r.x; x < r.x + r.width; x++) {
		for (int y=r.y; y < r.y + r.height; y++) {
		    src.getPixel(x, y, arr);
		    arr[0] = 255-arr[0];
		    arr[1] = 255-arr[1];
		    arr[2] = 255-arr[2];
		    dstOut.setPixel(x, y, arr);
		}
	    }
	}
    }
}
