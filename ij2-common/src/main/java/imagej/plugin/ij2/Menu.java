package imagej.plugin.ij2;

public @interface Menu {

	String label();
	int weight() default 0;
	char mnemonic() default '\0';
	String icon() default "";

}
