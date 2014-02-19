/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.legacy.patches;

import ij.IJ;
import ij.ImagePlus;

/**
 * The base {@link LegacyHooks} to be used in the patched ImageJ 1.x.
 * <p>
 * This is the minimal implementation of {@link LegacyHooks} and will be
 * installed by default after patching in the extension points into ImageJ 1.x.
 * On its own, it does not allow to override the extension points (such as the
 * editor) with different implementations; one needs to install different hooks using
 * the {@link imagej.legacy.CodeHacker#installHooks(LegacyHooks)} method.
 * </p>
 * </p>
 * This class is also the perfect base class for all implementations of the
 * {@link LegacyHooks} interface, e.g. to offer "real" extension mechanisms such
 * as the SciJava-common plugin framework.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class EssentialLegacyHooks implements LegacyHooks {

	/** @inherit */
	@Override
	public boolean isLegacyMode() {
		return true;
	}

	/** @inherit */
	@Override
	public Object getContext() {
		return null;
	}

	/** @inherit */
	@Override
	public boolean quit() {
		return true;
	}

	/** @inherit */
	@Override
	public void installed() {
		// ignore
	}

	/** @inherit */
	@Override
	public void dispose() {
		// ignore
	}

	/** @inherit */
	@Override
	public void showProgress(double progress) {
	}

	/** @inherit */
	@Override
	public void showProgress(int currentIndex, int finalIndex) {
	}

	/** @inherit */
	@Override
	public void showStatus(String status) {
	}

	/** @inherit */
	@Override
	public void log(String message) {
	}

	/** @inherit */
	@Override
	public void registerLegacyImage(final ImagePlus image) {
	}

	/** @inherit */
	@Override
	public void unregisterLegacyImage(final ImagePlus image) {
	}

	/** @inherit */
	@Override
	public void debug(String string) {
		System.err.println(string);
	}

	/** @inherit */
	@Override
	public void error(Throwable t) {
		IJ.handleException(t);
	}

}
