/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.rasterlayer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.imagejdev.imagine.api.editing.LayerFactory;
import org.imagejdev.imagine.api.image.Layer;
import org.imagejdev.imagine.api.image.Picture;
import org.imagejdev.imagine.api.util.GraphicsUtils;
import org.imagejdev.imagine.api.util.RasterConverter;
import org.imagejdev.imagine.spi.image.RepaintHandle;

import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */

@ServiceProvider(service=org.imagejdev.imagine.api.util.RasterConverter.class)

public class RasterConverterImpl extends RasterConverter {

    @Override
    protected Layer convertToRasterLayer(Layer layer, Picture picture) {
        RepaintHandle handle = picture == null ? null : picture.getRepaintHandle();
        RasterLayerFactory factory = Lookup.getDefault().lookup(RasterLayerFactory.class);
        BufferedImage img = new BufferedImage (layer.getBounds().width, layer.getBounds().height, 
                GraphicsUtils.DEFAULT_BUFFERED_IMAGE_TYPE);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        layer.paint(g, null,true);
        g.dispose();
        RasterLayerImpl result = new RasterLayerImpl (factory, handle, img);
        return result.getLayer();
    }

    @Override
    protected LayerFactory getRasterLayerFactory() {
        return Lookup.getDefault().lookup (RasterLayerFactory.class);
    }
}
