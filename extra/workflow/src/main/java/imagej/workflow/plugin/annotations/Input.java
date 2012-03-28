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

package imagej.workflow.plugin.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

import imagej.workflow.plugin.IPlugin;

/**
 * Annotation for a plugin input.  Contains a list of @Items.
 *
 * <p>
 * Examples:
 * <p>
 *   @Input
 *     Annotation with empty list of items.  By default this will be treated as
 *     a single input of the current image.
 * <p>
 *   @Input({
 *     @Item(
 *       name = "Scale factor",
 *       type = Item.FLOATING;
 *       floating = 1.0;
 *     ),
 *     @Item(
 *       name = Input image",
 *       type = Item.IMAGE
 *     )
 *   })
 *
 * @author Aivar Grislis
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME) //SOURCE) //TODO was RUNTIME, sezPoz wants SOURCE:
// "warning: should be marked @Retention(RetentionPolicy.SOURCE)"
@Indexable(type=IPlugin.class)
public @interface Input {
    public final String DEFAULT = "INPUT"; //TODO this is hardcoded elsewhere in Workflow/Workflow Pipes
    //Bug ID: 6954300
    // Annotation with generics causes javac to fail when annotation processor present
    // State: 3-Accepted, bug Priority: 4-Low
    // Submit Date: 20-MAY-2010
    //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6954300
    //Img[] value() default { @Img };
    //Img[] value() default { @Img("DEFAULT") };

    //TODO try this, does an annotation extend Object?
    // WAS Img[] value() default { };
    //NO: Object[] value() default { }; doesn't compile
    //neither does Annotation[] value default { }; !!!

    Item[] value() default { };
}
