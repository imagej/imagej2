/*
 * RasterLayerFactory.java
 *
 * Created on October 25, 2006, 3:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.rasterlayer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.imagejdev.imagine.api.editing.LayerFactory;
import org.imagejdev.imagine.api.image.Layer;
import org.imagejdev.imagine.api.util.GraphicsUtils;
import org.imagejdev.imagine.spi.image.LayerImplementation;
import org.imagejdev.imagine.spi.image.RepaintHandle;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */

@ServiceProvider(service=LayerFactory.class, position=-1)

public final class RasterLayerFactory extends LayerFactory {
    public RasterLayerFactory () {
        super ("raster", NbBundle.getMessage(RasterLayerFactory.class, //NOI18N
                "LBL_RasterFactory")); //NOI18N
    }

    public LayerImplementation createLayer(String name, RepaintHandle handle,
                                           Dimension size) {
        RasterLayerImpl result = new RasterLayerImpl (this, handle, size); //XXX cast
        if (name != null) {
            result.setName (name);
        }
        return result;
    }

    public boolean canConvert(Layer other) {
        return true;
    }

    public LayerImplementation convert(Layer other) {
        Rectangle r = other.getBounds();
        BufferedImage img = new BufferedImage (r.width, r.height,
                GraphicsUtils.DEFAULT_BUFFERED_IMAGE_TYPE);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        other.paint(g, null,true);
        g.dispose();
        RasterLayerImpl result = new RasterLayerImpl (this, null, img);
        result.setName (other.getName());
        result.setOpacity(other.getOpacity());
        return result;
    }

    @Override
    public String getConversionActionDisplayName() {
        return NbBundle.getMessage(RasterLayerFactory.class,
                "LBL_ConvertToRaster"); //NOI18N
    }

    public String getToolRegistryPath() {
        return "layers/raster/";
    }
}
