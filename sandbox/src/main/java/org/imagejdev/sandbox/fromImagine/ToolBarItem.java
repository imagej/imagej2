package org.imagejdev.sandbox.fromImagine;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)


public @interface ToolBarItem {

  String toolBar() default "";

  String icon();

  int position() default Integer.MAX_VALUE; // at end
}
