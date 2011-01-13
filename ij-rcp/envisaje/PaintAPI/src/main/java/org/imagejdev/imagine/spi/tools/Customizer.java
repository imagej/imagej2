/*
 * Customizer.java
 *
 * Created on September 29, 2006, 5:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.imagine.spi.tools;

import javax.swing.JComponent;

/**
 * A customizer for a property of some type (for example, a color).  Provides
 * a set of "component groups" which should be laid out together.  A single
 * component group is typically a widget of some sort and a label for it.
 * Customizers are provided by instances of CustomizerProvider, an interface
 * which may be found in the lookup of some Tools.
 * @see Tool
 * @see org.openide.util.Lookup
 * @see CustomizerProvider
 * @author Tim Boudreau
 */
public interface Customizer <T> {
    public JComponent getComponent();
    public String getName();
    public T get();
}
