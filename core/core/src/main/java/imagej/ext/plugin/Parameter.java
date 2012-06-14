/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ext.plugin;

import imagej.ext.module.ItemIO;
import imagej.ext.module.ItemVisibility;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ui.WidgetStyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for indicating a field is an input or output parameter. This
 * annotation is a useful way for plugins to declare their inputs and outputs
 * simply.
 * 
 * @author Johannes Schindelin
 * @author Grant Harris
 * @author Curtis Rueden
 * @see PluginModuleInfo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {

	/** Defines a label for the parameter. */
	String label() default "";

	/** Defines a description for the parameter. */
	String description() default "";

	/**
	 * Defines the input/output type of the parameter.
	 * <p>
	 * Choices are:
	 * </p>
	 * <ul>
	 * <li>INPUT: parameter is an input for the plugin.</li>
	 * <li>OUTPUT: parameter is an output for the plugin.</li>
	 * <li>BOTH: parameter is both and input and an output for the plugin. This
	 * type is used to indicate an object that is mutated somehow during
	 * execution.</li>
	 * </ul>
	 */
	// NB: We use the fully qualified name to work around a javac bug:
	// http://bugs.sun.com/view_bug.do?bug_id=6512707
	// See:
	// http://groups.google.com/group/project-lombok/browse_thread/thread/c5568eb659cab203
	ItemIO type() default imagej.ext.module.ItemIO.INPUT;

	/**
	 * Defines the "visibility" of the parameter.
	 * <p>
	 * Choices are:
	 * </p>
	 * <ul>
	 * <li>NORMAL: parameter is included in the history for purposes of data
	 * provenance, and included as a parameter when recording scripts.</li>
	 * <li>TRANSIENT: parameter is excluded from the history for the purposes of
	 * data provenance, but still included as a parameter when recording scripts.</li>
	 * <li>INVISIBLE: parameter is excluded from the history for the purposes of
	 * data provenance, and also excluded as a parameter when recording scripts.
	 * This option should only be used for parameters with no effect on the final
	 * output, such as a "verbose" flag.</li>
	 * <li>MESSAGE: parameter value is intended as a message only, not editable by
	 * the user nor included as an input or output parameter.</li>
	 * </ul>
	 */
	// NB: We use the fully qualified name to work around a javac bug:
	// http://bugs.sun.com/view_bug.do?bug_id=6512707
	// See:
	// http://groups.google.com/group/project-lombok/browse_thread/thread/c5568eb659cab203
	ItemVisibility visibility() default imagej.ext.module.ItemVisibility.NORMAL;

	/**
	 * Defines whether the parameter value should be auto-filled, if possible,
	 * during the preprocessing chain. Some preprocessors can populate a parameter
	 * value based on certain criteria. For example, an
	 * {@link imagej.ext.display.ActiveDisplayPreprocessor} will populate a single
	 * {@link imagej.ext.display.Display} parameter to match the currently active
	 * display, if any. Setting this flag to false suppresses this behavior.
	 */
	boolean autoFill() default true;

	/** Defines whether the parameter value must be non-null. */
	boolean required() default true;

	/** Defines whether to remember the most recent value of the parameter. */
	boolean persist() default true;

	/** Defines a key to use for saving the value persistently. */
	String persistKey() default "";

	/**
	 * Defines a function that is called during preprocessing to initialize the
	 * parameter value. This initializer is called after the global
	 * {@link Plugin#initializer()} (i.e., {@link ModuleInfo#getInitializer()})
	 * method.
	 * 
	 * @see InitPreprocessor
	 */
	String initializer() default "";

	/**
	 * Defines a function that is called whenever this parameter changes.
	 * <p>
	 * This mechanism enables interdependent parameters of various types. For
	 * example, two int parameters "width" and "height" could update each other
	 * when another boolean "Preserve aspect ratio" flag is set.
	 * </p>
	 */
	String callback() default "";

	/** Defines the preferred widget style. */
	// NB: We use the fully qualified name to work around a javac bug:
	// http://bugs.sun.com/view_bug.do?bug_id=6512707
	// See:
	// http://groups.google.com/group/project-lombok/browse_thread/thread/c5568eb659cab203
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
