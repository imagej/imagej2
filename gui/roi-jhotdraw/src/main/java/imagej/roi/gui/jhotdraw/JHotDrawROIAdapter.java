package imagej.roi.gui.jhotdraw;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

/**
 * @author leek
 *
 * Add this annotation to any ImageJHotDrawROIAdapter and
 * sezpos will make it available to ImageJ and let people
 * use JHotDraw to edit it.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Indexable(type=IJHotDrawROIAdapter.class)

public @interface JHotDrawROIAdapter {

}
