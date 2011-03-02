package imagej.plugin;

import imagej.plugin.gui.WidgetStyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO
 *
 * @author Johannes Schindelin
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {

	/** Defines if the parameter is an output. */
	boolean output() default false;

	/** Defines a label for the parameter. */
	String label() default "";

	/** Defines whether the parameter is required (i.e., no default). */
	boolean required() default false;

	/** Defines a key to use for saving the value persistently. */
	String persist() default "";

	/** Defines the preferred widget style. */
	WidgetStyle widgetStyle() default WidgetStyle.DEFAULT;

	/** Defines the minimum allowed value (numeric parameters only). */
	String min() default "";

	/** Defines the maximum allowed value (numeric parameters only). */
	String max() default "";

	/** Defines the step size to use (numeric parameters only). */
	String stepSize() default "";

	/**
	 * Defines the width of the input field in characters
	 * (text field parameters only).
	 */
	int columns() default 6;

	/** Defines the list of possible values (multiple choice text fields only). */
	String[] choices() default {};

}
