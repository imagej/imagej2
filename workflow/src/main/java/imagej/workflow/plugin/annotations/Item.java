/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow.plugin.annotations;

import java.lang.annotation.*;

/**
 * Annotation for a plugin input or output item.
 * <p>
 * Example:
 *   @Item(name="First Name", type=Item.STRING)
 *     uses default value
 *   @Item(name="Last Name", type=Item.STRING, string="Jones")
 *     supplies default value of "Jones"
 *
 * @author Aivar Grislis
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Item {
    public enum Type {
        STRING, INTEGER, FLOATING, URL, IMAGE, ITEM
    }
    public String CURRENT_IMAGE = "<current image>";

    //TODO the following would allow us to define an item of Item.Type.ITEM using
    // @Item("Name of item")
    // which might be useful.
    //   String value();
    //   Type type() default Type.ITEM;
    // However we get the spurious "incompatible types" message,
    //   "found: imagej.workflow.plugin.annotations.Item.Type
    //    required: imagej.workflow.plugin.annotations.Item.Type"

    String name();
    Type type();
    String string() default "";
    int integer() default 1;
    double floating() default 1.0;
    String url() default "";
    String image() default CURRENT_IMAGE;
}

/*
 * @Input( {
 *     @Item(
 *         name = Thingy.BLUE,
 *         type = Item.STRING,
 *         string = ""),
 *
 */