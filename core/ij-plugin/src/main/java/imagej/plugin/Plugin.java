package imagej.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Indexable(type=IPlugin.class)
public @interface Plugin {

	String toolbarIcon() default "";

	String menuPath() default "";

	String menubar() default "main";

	Menu[] menu() default {};

}
