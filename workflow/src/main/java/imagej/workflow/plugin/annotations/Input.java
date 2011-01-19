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
 *
 * @author aivar
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
    Img[] value() default { };
}
