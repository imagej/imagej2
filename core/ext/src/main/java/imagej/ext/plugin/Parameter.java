//
// Parameter.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.ext.plugin;

import imagej.ext.module.ItemVisibility;
import imagej.ext.module.ui.WidgetStyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {

	/** Defines a label for the parameter. */
	String label() default "";

	/** Defines a description for the parameter. */
	String description() default "";

	/** Defines if the parameter is an output. */
	boolean output() default false;

	/**
	 * Defines the "visibility" of the parameter.
	 * <p>
	 * Choices are:
	 * <ul>
	 * <li>NORMAL: parameter is included in the history for purposes of data
	 * provenance, and included as a parameter when recording scripts.</li>
	 * <li>TRANSIENT: parameter is excluded from the history for the purposes of
	 * data provenance, but still included as a parameter when recording
	 * scripts.</li>
	 * <li>INVISIBLE: parameter is excluded from the history for the purposes of
	 * data provenance, and also excluded as a parameter when recording scripts.
	 * This option should only be used for parameters with no effect on the final
	 * output, such as a "verbose" flag.</li>
	 * <li>MESSAGE: parameter value is intended as a message only, not editable
	 * by the user nor included as an input or output parameter.</li>
	 * </ul>
	 */
	// NB: We use the fully qualified name to work around a javac bug:
	// http://bugs.sun.com/view_bug.do?bug_id=6512707
	// See: http://groups.google.com/group/project-lombok/browse_thread/thread/c5568eb659cab203
	ItemVisibility visibility() default imagej.ext.module.ItemVisibility.NORMAL;

	/** Defines whether the parameter value must be specified (i.e., no default). */
	boolean required() default false;

	/** Defines whether to remember the most recent value of the parameter. */
	boolean persist() default true;

	/** Defines a key to use for saving the value persistently. */
	String persistKey() default "";

	/**
	 * Defines a function that is called whenever this parameter changes.
	 * <p>
	 * This mechanism enables interdependent parameters of various types.
	 * For example, two int parameters "width" and "height" could update each
	 * other when another boolean "Preserve aspect ratio" flag is set.
	 * </p>
	 */
	String callback() default "";

	/** Defines the preferred widget style. */
	// NB: We use the fully qualified name to work around a javac bug:
	// http://bugs.sun.com/view_bug.do?bug_id=6512707
	// See: http://groups.google.com/group/project-lombok/browse_thread/thread/c5568eb659cab203
	WidgetStyle style() default imagej.ext.module.ui.WidgetStyle.DEFAULT;

	/** Defines the minimum allowed value (numeric parameters only). */
	String min() default "";

	/** Defines the maximum allowed value (numeric parameters only). */
	String max() default "";

	/** Defines the step size to use (numeric parameters only). */
	String stepSize() default "";

	/**
	 * Defines the width of the input field in characters (text field parameters
	 * only).
	 */
	int columns() default 6;

	/** Defines the list of possible values (multiple choice text fields only). */
	String[] choices() default {};

}
