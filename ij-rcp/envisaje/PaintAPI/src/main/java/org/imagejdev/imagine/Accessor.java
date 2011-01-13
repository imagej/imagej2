/*
 * Accessor.java
 *
 * Created on October 24, 2006, 12:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.imagine;

import org.imagejdev.imagine.api.image.Layer;
import org.imagejdev.imagine.api.image.Picture;
import org.imagejdev.imagine.api.image.Surface;
import org.imagejdev.imagine.spi.image.LayerImplementation;
import org.imagejdev.imagine.spi.image.PictureImplementation;
import org.imagejdev.imagine.spi.image.SurfaceImplementation;


/**
 * Jarda's wild and crazy API lockdown technique.  This class sits in a 
 * non-public package, and is used to bidirectionally map SPI and API instances.
 * The result is that clients only see the API classes, which can be
 * strictly compatible and implemented only in this module;  the 
 * implementation classes have slightly looser requirements.
 *
 * @author Tim Boudreau
 */
public abstract class Accessor {
    public static Accessor DEFAULT = null;
    public static InverseAccessor INVERSE = null;
    public abstract Surface createSurface (SurfaceImplementation impl);
    public abstract Layer createLayer (LayerImplementation impl);
    public abstract Picture createPicture (PictureImplementation impl);
    public abstract PictureImplementation getImpl (Picture picture);
    public abstract LayerImplementation getImpl (Layer layer);
    public abstract SurfaceImplementation getSurface (Surface surface);

    public static final Layer layerFor (LayerImplementation impl){
        return INVERSE.layerFor(impl);
    }

    public static final Picture pictureFor (PictureImplementation impl){
        return INVERSE.pictureFor(impl);
    }

    public static final Surface surfaceFor (SurfaceImplementation impl){
        return INVERSE.surfaceFor(impl);
    }

    public static abstract class InverseAccessor {
        public abstract Layer layerFor (LayerImplementation impl);
        public abstract Picture pictureFor (PictureImplementation impl);
        public abstract Surface surfaceFor (SurfaceImplementation impl);
    }
}
