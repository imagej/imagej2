/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow.plugin.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

import imagej.workflow.plugin.IPlugin;

/**
 * Annotation for a plugin input.  Contains a list of @Items.
 *
 * <p>
 * Examples:
 * <p>
 *   @Input
 *     Annotation with empty list of items.  By default this will be treated as
 *     a single input of the current image.
 * <p>
 *   @Input({
 *     @Item(
 *       name = "Scale factor",
 *       type = Item.FLOATING;
 *       floating = 1.0;
 *     ),
 *     @Item(
 *       name = Input image",
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
public @interface Input {
    public final String DEFAULT = "INPUT";
    //Bug ID: 6954300
    // Annotation with generics causes javac to fail when annotation processor present
    // State: 3-Accepted, bug Priority: 4-Low
    // Submit Date: 20-MAY-2010
    //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6954300
    //Img[] value() default { @Img };
    //Img[] value() default { @Img("DEFAULT") };

    //TODO try this, does an annotation extend Object?
    // WAS Img[] value() default { };
    //NO: Object[] value() default { }; doesn't compile
    //neither does Annotation[] value default { }; !!!

    Item[] value() default { };
}
