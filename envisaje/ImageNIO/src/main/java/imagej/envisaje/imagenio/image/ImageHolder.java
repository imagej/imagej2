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

package imagej.envisaje.imagenio.image;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Object which represents a cached, non-heap storage image.  It is passed a
 * normal BufferedImage in its construtor;  it immediately makes a non-heap
 * copy of the image in its state at the time of construction (changes to
 * the original image afterward will not affect the stored image).
 * <p>
 * On demand, a call to <code>getImage()</code> will return a normal, 
 * acceleratable BufferedImage.
 * <p>
 * General purpose:  For making images that are saved as undo information;  the
 * saved information should be stored off the java heap and need not have 
 * lightning fast painting times.
 *
 * @author Timothy Boudreau
 */
public class ImageHolder {
    private WeakReference realImage = null;
    private BufferedImage cached;
    /** Creates a new instance of NIOImageHolder */
    public ImageHolder(BufferedImage img, Rectangle r) {
	try {
	    createCache (img, r);
	} catch (IOException e) {
	    IllegalStateException ise =
		    new IllegalStateException(e);
	    throw ise;
	}
    }
    
    public ImageHolder(BufferedImage img) {
	try {
	    createCache (img, null);
	} catch (IOException ioe) {
	    IllegalStateException ise = 
		    new IllegalStateException (ioe);
	    throw ise;
	}
    }

    protected boolean canCache() {
	return true; //XXX if serious IOError in cacheManager, set false
    }

    protected void createCache(BufferedImage img, Rectangle r) throws IOException {
	if (!(img instanceof ByteNIOBufferedImage)) {
	    cached = r == null ? new ByteNIOBufferedImage (img) :
		new ByteNIOBufferedImage (img, r);
	} else {
	    cached = img;
	}
    }
    
    public BufferedImage getImage ( boolean acceleratable) {
	BufferedImage result = null;
        if (!acceleratable) {
            result = (BufferedImage) (realImage != null ? realImage.get()
                                   : null);
            if (result == null) {
                result = cached;
            }
        } else {
            if (realImage != null) {
                result = (BufferedImage) realImage.get();
            }
            if (result == null) {
                int type;
                if (System.getProperty("mrj.version") != null) {
                    type = BufferedImage.TYPE_INT_ARGB;
                } else {
                    type = BufferedImage.TYPE_INT_ARGB;
                }
                result = new BufferedImage (cached.getWidth(), cached.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = result.createGraphics();
                g.drawRenderedImage(cached, AffineTransform.getTranslateInstance(
                        0,0));
                g.dispose();
                realImage = new WeakReference (result);
            }
        }
        return result;
    }
    
    public void dispose() {
	((DisposableImage) cached).dispose();
    }
}
