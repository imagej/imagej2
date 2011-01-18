package imagej.plugin.ij2;

public @interface Menu {

	static final double DEFAULT_WEIGHT = Double.POSITIVE_INFINITY;

	String label();
	double weight() default DEFAULT_WEIGHT;
	char mnemonic() default '\0';
	String accelerator() default "";
	String icon() default "";

}
