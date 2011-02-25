package imagej.plugin.finder;

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
@Indexable(type=IPluginFinder.class)
public @interface PluginFinder {
	// indexable annotation interface for use with SezPoz
}
