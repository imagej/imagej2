
package org.imagejdev.annotations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ActionRegistration {
    String category();
    String id() default "";
    String displayName();
    String iconBase() default "";
    boolean iconInMenu() default true;
    String key() default "";
}