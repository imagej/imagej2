/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.sandbox.other.bordello;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

    /**
     * Indicates the default implementation for a service.
     */
    @Documented
    @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME)
    public @interface Implementor {
      Class<?> value();
    }