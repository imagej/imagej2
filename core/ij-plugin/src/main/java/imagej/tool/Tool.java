package imagej.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Indexable(type=ITool.class)
public @interface Tool
{
        String name() default "";
	String label() default "";
	String description() default "";
	String iconPath() default "";
}
