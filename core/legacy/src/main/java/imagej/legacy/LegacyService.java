/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.legacy;

import ij.ImagePlus;
import imagej.data.display.ImageDisplay;
import imagej.service.IJService;

import org.scijava.app.StatusService;
import org.scijava.log.LogService;

/**
 * Interface for services that work with legacy ImageJ 1.x.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public interface LegacyService extends IJService { 
	/** Gets the LogService associated with this LegacyService. */
	LogService log();

	/** Gets the StatusService associated with this LegacyService. */
	StatusService status();

	/** Gets the LegacyImageMap associated with this LegacyService. */
	LegacyImageMap getImageMap();

	/** Gets the OptionsSynchronizer associated with this LegacyService. */
	OptionsSynchronizer getOptionsSynchronizer();

	/**
	 * Runs a legacy command programmatically.
	 * 
	 * @param ij1ClassName The name of the plugin class you want to run e.g.
	 *          "ij.plugin.Clipboard"
	 * @param argument The argument string to pass to the plugin e.g. "copy"
	 */
	void runLegacyCommand(String ij1ClassName, String argument);

	/**
	 * Ensures that the currently active {@link ImagePlus} matches the currently
	 * active {@link ImageDisplay}. Does not perform any harmonization.
	 */
	void syncActiveImage();

	/**
	 * Returns true if this LegacyService has been initialized already and false
	 * if not.
	 */
	boolean isInitialized();

	/**
	 * Sets the foreground and background colors in ImageJ 1.x from the current
	 * view using the current channel values.
	 */
	void syncColors();

	/**
	 * States whether we're running in legacy ImageJ 1.x mode.
	 * 
	 * To support work flows which are incompatible with ImageJ2, we want to allow
	 * users to run in legacy ImageJ 1.x mode, where the ImageJ2 GUI is hidden and
	 * the ImageJ 1.x GUI is shown. During this time, no synchronization should take
	 * place.
	 */
	public boolean isLegacyMode();

	/**
	 * Switch to/from running legacy ImageJ 1.x mode.
	 */
	void toggleLegacyMode(boolean toggle);

	/** Gets the version of ImageJ 1.x being used. */
	String getLegacyVersion();

}
