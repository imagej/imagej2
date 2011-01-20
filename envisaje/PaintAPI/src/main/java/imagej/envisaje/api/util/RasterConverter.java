/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.api.util;

import imagej.envisaje.api.editing.LayerFactory;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.api.image.Picture;
import imagej.envisaje.spi.tools.Tool;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Utility class for converting 
 *
 * @author Tim Boudreau
 */
public abstract class RasterConverter {
    public static RasterConverter getDefault() {
        return Lookup.getDefault().lookup(RasterConverter.class);
    }
    
    protected abstract Layer convertToRasterLayer(Layer layer, Picture picture);
    protected abstract LayerFactory getRasterLayerFactory();
    
    public static LayerFactory getLayerFactory() {
        RasterConverter r = getDefault();
        if (r != null) {
            return r.getRasterLayerFactory();
        }
        return null;
    }
    
    public final Layer convert (Layer layer) {
        Picture picture = Utilities.actionsGlobalContext().lookup(Picture.class);
        return convert (layer, picture);
    }
        
    public final Layer convert (Layer layer, Picture picture) {
        if (picture == null) {
            throw new IllegalStateException ("No picture");
        }
        int ix = picture.getLayers().indexOf(layer);
        if (ix == -1) {
            throw new IllegalStateException ("Wrong picture");
        }
        Layer nue = convertToRasterLayer(layer, picture);
        picture.delete(layer);
        picture.add(ix, nue);
        return nue;
    }
    
    public static Layer askUserToConvert (Layer layer, Picture picture, Tool tool) {
        RasterConverter conv = getDefault();
        if (conv == null) {
            return null;
        }
        if (picture == null) {
            picture = Utilities.actionsGlobalContext().lookup(Picture.class);
        }
        String msg = NbBundle.getMessage (RasterConverter.class, "MSG_CONVERT", 
                tool.getName(), layer.getName());
        String ttl = NbBundle.getMessage (RasterConverter.class, "TTL_CONVERT");
        DialogDescriptor dd = new DialogDescriptor (msg, ttl);
        if (DialogDescriptor.OK_OPTION.equals (DialogDisplayer.getDefault().notify(dd))) {
            return conv.convert (layer, picture);
        }
        return null;
    }
}
