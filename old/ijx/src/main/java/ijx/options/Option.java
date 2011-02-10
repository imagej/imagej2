
package ijx.options;


import org.openide.util.lookup.ServiceProvider;


/**
 *  Options annotation
 * @author GBH <imagejdev.org>
 */

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
//@ServiceProvider(service=Option.class)

public @interface Option {

    // annotate fields that are "options", loaded from and saved to Prefs.
    /// @Option()
    // prefs key: "Classname.fieldname"
}
