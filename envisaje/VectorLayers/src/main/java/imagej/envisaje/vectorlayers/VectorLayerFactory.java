/*
 * VectorLayerFactory.java
 *
 * Created on October 25, 2006, 10:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.vectorlayers;

import imagej.envisaje.api.editing.LayerFactory;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.spi.image.LayerImplementation;
import imagej.envisaje.spi.image.RepaintHandle;
import java.awt.Dimension;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=LayerFactory.class)
/**
 * Factory for shape-based vector data layers.
 *
 * @author Tim Boudreau
 */
public final class VectorLayerFactory extends LayerFactory {

    public VectorLayerFactory () {
        super ("vector", NbBundle.getMessage(VectorLayerFactory.class, //NOI18N
                "LBL_VectorLayerFactory")); //NOI18N
    }

    public LayerImplementation createLayer(String name, RepaintHandle handle,
                                           Dimension size) {
        VLayerImpl result = new VLayerImpl(this, handle, name, size);
        result.setName(name);
        return result;
    }
        
    public boolean canConvert(Layer other) {
        return false;
    }

    public LayerImplementation convert(Layer other) {
        throw new UnsupportedOperationException();
    }

    public String getToolRegistryPath() {
        return "layers/vector/";
    }
}
