package org.imagejdev.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Action {

    int position() default Integer.MAX_VALUE;

    String displayName();

    /**
     * The path to the folder where this action will be registered.
     * The menu item and/or toolbar button will be registered
     * in the same folder. For example, if "Edit" is returned,
     * the action will be registered in (at least) "Actions/Edit",
     * as well as, optionally, "Menu/Edit" and "Toolbars/Edit".
     * @return String (default is "File")
     */
    String path() default "File";

    String iconBase() default "";

    boolean menuBar() default false;

    boolean toolBar() default false;

   // could be added from Tulach's ActionRegistration...
//    String category();
//    String id() default "";
//    boolean iconInMenu() default true;
//    String key() default "";

}