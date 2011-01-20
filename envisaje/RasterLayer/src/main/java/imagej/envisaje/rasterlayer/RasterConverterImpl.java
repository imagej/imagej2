/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.rasterlayer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import imagej.envisaje.api.editing.LayerFactory;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.api.image.Picture;
import imagej.envisaje.api.util.GraphicsUtils;
import imagej.envisaje.api.util.RasterConverter;
import imagej.envisaje.spi.image.RepaintHandle;

import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */

@ServiceProvider(service=imagej.envisaje.api.util.RasterConverter.class)

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
