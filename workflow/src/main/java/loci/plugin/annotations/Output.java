/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

import loci.workflow.plugin.IPlugin;

/**
 *
 * @author aivar
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME) //SOURCE) //TODO was RUNTIME, sezPoz wants SOURCE:
// "warning: should be marked @Retention(RetentionPolicy.SOURCE)"
@Indexable(type=IPlugin.class)
public @interface Output {
    public final String DEFAULT = "OUTPUT";
    // see Input
    //Img[] value() default { @Img };
    Img[] value() default {};
}
