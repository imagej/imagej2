package imagej.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * @author Johannes Schindelin johannes.schindelin at imagejdev.org
 * @author Grant Harris gharris at mbl.edu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {

	/** Defines if the parameter is an output. */
	boolean output() default false;

	/** @return label to display in input widget */
	String label() default "";

	/** @return whether the parameter is required (i.e., no default) */
	boolean required() default false;

	/** Defines a key to use for saving the value persistently. */
	String persist() default "";

	/** @return minimum allowed value (numeric parameters only) */
	String min() default "";

	/** @return maximum allowed value (numeric parameters only) */
	String max() default "";

	/** @return step size to use (numeric parameters only) */
	String stepSize() default "";

	/** @return width of field in characters (text field parameters only) */
	int columns() default 6;

	/** @return the list of possible values (multiple choice text fields only) */
	String[] choices() default {};

}
