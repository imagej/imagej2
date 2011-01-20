/*
 * LayerFactory.java
 *
 * Created on October 23, 2006, 4:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.editing;

import java.awt.Dimension;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.api.util.RasterConverter;
import imagej.envisaje.spi.image.LayerImplementation;
import imagej.envisaje.spi.image.RepaintHandle;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Factory for types of layers which can be composed into an image.
 * These are placed in the default Lookup (create a flat file in
 * META-INF/services in your jar, named with the fully qualified name
 * of this class, and include in it one line for each LayerFactory you
 * are registering; each line should be the fully qualified name of
 * the class.
 *
 * @author Tim Boudreau
 */
public abstract class LayerFactory {
    public static final String TYPE_RASTER = "raster"; //NOI18N
    public static final String TYPE_TEXT = "text"; //NOI18N
    public static final String TYPE_VECTOR = "vector"; //NOI18N
    private final String name;
    private final String displayName;
    protected LayerFactory (String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }
    
    /**
     * A localized name for this layer type.
     */
    public final String getDisplayName() {
        return displayName;
    }

    public static LayerFactory getDefault() {
        LayerFactory result = RasterConverter.getLayerFactory();
        if (result == null) {
            result = Lookup.getDefault().lookup (LayerFactory.class);
        }
        return result;
    }

    /**
     * A programmatic name for this layer type.  A layer type may register
     * specific tools against this code name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Create an empty layer.
     * @param handle An object that can be notified when a change is made in
     * the layer that should trigger a repaint.
     */
    public abstract LayerImplementation createLayer(String name, RepaintHandle handle,
            Dimension size);
    /**
     * Determine if the passed Layer object can be converted into a layer of
     * this type - for example, rasterizing a vector shape layer.
     * @param other A layer, which may use a different internal storage format,
     *  which might be able to be converted into a layer of this type.
     * @return true if the layer can be converted into this type
     */
    public abstract boolean canConvert (Layer other);
    /**
     * Create a new LayerImplmentation representing the passed layer, if
     * canConvert() returns true for the passed layer.  The new layer shall
     * not be affected by changes in the old layer.
     * @return a new LayerImplementation of this factory's type, which contains
     *  image data derived from the original layer
     */
    public abstract LayerImplementation convert (Layer other);
    
    public abstract String getToolRegistryPath();

    /**
     * Get the display name for converting another layer to this type -
     * e.g. "Rasterize Layer".  The default implementation returns, in
     * English, "Convert to [display name]".
     */
    public String getConversionActionDisplayName() {
        return NbBundle.getMessage(LayerFactory.class, "LBL_Convert", //NOI18N
                getDisplayName());
    }
}
