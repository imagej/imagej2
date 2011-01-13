/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.imagine.api.toolcustomizers;

import org.imagejdev.imagine.spi.tools.Customizer;


/**
 * Factory for customizers which can be registered in the default lookup, to
 * enable Customizer.getCustomizer() to instantiate customizers for types not
 * known to this module.
 * <p/>
 * Customizers are expected to be used repeatedly;  for a given type and name,
 * the getCustomizer() method will only be called one time.
 * <p/>
 * Customizers that are returned here are expected to automatically store
 * their data using NbPreferences, so that a future session will return a 
 * Customizer already set to the last user choice of settings.
 *
 * @author Tim Boudreau
 */
public interface CustomizerFactory {
    public <T> Customizer<T> getCustomizer (Class<T> type, String name, Object... args);
    public boolean supportsType (Class type);
}
