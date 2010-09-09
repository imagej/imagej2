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
    // attributes...
	String label() default "";        // label to display in input widget
    int digits() default 2;           // number of digits to right of decimal point
	  int columns() default 6;          // width of field in characters
    String units() default "";        // a string displayed to the right of the field
    boolean required() default false; //++ gbh
    String widget() default "";       // widget to use for input
    //
	  boolean output() default false;
}

/*
How to set default value of parameter
Persistence?
 */