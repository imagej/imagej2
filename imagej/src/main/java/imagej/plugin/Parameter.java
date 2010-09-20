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
    
    /**
     *
     * @return  label to display in input widget
     */
    String label() default "";        //
    /**
     * number of digits to right of decimal point
     * @return
     */
    int digits() default 2;

    /**
     * width of field in characters
     * @return
     */
    int columns() default 6;
    /**
     * 
     * @return a string displayed to the right of the field
     */
    String units() default "";
    /**
     *
     * @return widget to use for input
     */
    
    String widget() default "";
    /**
     *
     * @return is this parameter required (i.e. no default
     */
    boolean required() default false;

    /**
     * Defines a key to use for saving the value to Prefs...
     *
     * @return
     */
    String persist() default "";

    /**
     *
     * @return
     */
    boolean output() default false;
}

/*
How to set default value of parameter
Persistence?
 */
