/*
 * Effect.java
 *
 * Created on October 17, 2005, 9:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.imagine.spi.effects;

import java.awt.Composite;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

/**
 * Represents an effect that can be performed over a Layer.
 *
 * @author Timothy Boudreau
 */
public interface Effect {
    /** Get the name to display in the menu */
    public String getName();
    /** Get an object that can supply a customization panel to configure an
     * effect and supply a composite to apply to a layer */
    public Applicator getApplicator();
    
    public interface Applicator {
        /** Get a panel the user will interact with to configure settings */
        public JPanel getCustomizer();
        /** Get the composite that should be applied to the layer to achieve the
         * effect.  Note that image rasters will be using
         * BufferedImage.TYPE_INT_ARGB_PRE, so effect implementations may 
         * optimize by not attempting to support other color models */
        public Composite getComposite();
        /** Add a change listener which will detect changes in settings as the
         * user adjusts controls on the customizer panel, to indicate the 
         * preview should be re-rendered */
        public void addChangeListener (ChangeListener cl);
        /** Remove a change listener */
        public void removeChangeListener (ChangeListener cl);
        /** Determine if this effect can be previewed.  Very slow effects
         *  may want to return false.  */
        public boolean canPreview();
        /** Determine if the settings in the customizer are valid */
        public boolean canApply();
    }
    
}
