package imagej.plugin.ij2;

public @interface Menu {

	String label();
	double weight() default Double.POSITIVE_INFINITY;
	char mnemonic() default '\0';
	String accelerator() default "";
	String icon() default "";

}
