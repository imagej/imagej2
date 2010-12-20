package imagej.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Menu {

	String label();
	int weight() default 0;
	char mnemonic() default '\0';
	String icon() default "";

}
