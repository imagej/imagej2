package imagej.ext.plugin;


public @interface ParameterGroup {
	
	String id() default "";
	String label() default "";

	boolean collapsed() default false;
}
