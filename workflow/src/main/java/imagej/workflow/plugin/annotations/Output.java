/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

import imagej.workflow.plugin.IPlugin;

/**
 * Annotation for a plugin output.  Contains a list of @Items.
 *
 * <p>
 * Examples:
 * <p>
 *   @Output
 *     Annotation with empty list of items.  By default this will be treated as
 *     a single output to the current image.
 * <p>
 *   @Output({
 *     @Item(
 *       name = "Scale factor",
 *       type = Item.FLOATING;
 *       floating = 1.0;
 *     ),
 *     @Item(
 *       name = Output image",
 *       type = Item.IMAGE
 *     )
 *   })
 *
 * @author Aivar Grislis
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME) //SOURCE) //TODO was RUNTIME, sezPoz wants SOURCE:
// "warning: should be marked @Retention(RetentionPolicy.SOURCE)"
@Indexable(type=IPlugin.class)
public @interface Output {
    public final String DEFAULT = "OUTPUT";
    // see Input
    //Img[] value() default { @Img };

    //TODO Work in progress, WAS:
    //Img[] value() default {};

    Item[] value() default {};
}
