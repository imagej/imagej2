package imagej.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Indexable(type=BasePlugin.class)
public @interface Plugin {

	Class<?> type() default ImageJPlugin.class;
	
	String name() default "";

	String label() default "";

	String description() default "";

	String iconPath() default "";

	/**
	 * The plugin index returns plugins sorted by priority.
	 *
	 * This is useful for @{PluginPreprocessor}s, e.g., to
	 * control the order of their execution.
	 */
	int priority() default Integer.MAX_VALUE;

	String menuPath() default "";

	Menu[] menu() default {};

}
