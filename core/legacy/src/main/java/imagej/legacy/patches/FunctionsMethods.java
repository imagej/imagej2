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

package imagej.legacy.patches;

import ij.ImagePlus;
import ij.macro.Functions;

/**
 * Overrides {@link Functions} methods.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class FunctionsMethods {

	// TODO - this class was written to get around bug #554. The reliance on a
	// static here is troubling. Rather than track calls we could replace the
	// IJ1 code completely with a method that does the roi code but not the
	// drawing code. Not sure if that would cause bad side effects. Waiting
	// until we verify this implementation is a problem. On 10-19-11 BDZ
	// found that not tracking this at all seems to be okay as #554 does not
	// happen.

	public static int InsideBatchDrawing = 0;

	private FunctionsMethods() {
		// prevent instantiation of utility class
	}

	/** Prepends {@link ij.macro.Functions#displayBatchModeImage(ImagePlus)}. */
	public static void displayBatchModeImageBefore(
		@SuppressWarnings("unused") final ImagePlus imp2)
	{
		// NOTE - BDZ - removing for now - see if any problems rear their head.
		// Was for bug #554
		// InsideBatchDrawing++;
	}

	/** Appends {@link ij.macro.Functions#displayBatchModeImage(ImagePlus)}. */
	public static void displayBatchModeImageAfter(
		@SuppressWarnings("unused") final ImagePlus imp2)
	{
		// NOTE - BDZ - removing for now - see if any problems rear their head.
		// Was for bug #554
		// InsideBatchDrawing--;
	}

}
