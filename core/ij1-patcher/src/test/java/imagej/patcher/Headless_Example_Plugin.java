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

package imagej.patcher;

import ij.gui.GenericDialog;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;

/**
 * A very simple plugin to test the legacy headless support.
 * 
 * This plugin is used by {@link LegacyHeadlessTest}.
 * 
 * @author Johannes Schindelin
 */
public class Headless_Example_Plugin implements PlugIn {

	private String value;

	@Override
	public void run(String arg) {
		if ("SaveDialog".equals(arg)) {
			final SaveDialog dialog = new SaveDialog("File to save to", "default", "txt");
			final String fileName = dialog.getFileName();
			final String directory = dialog.getDirectory();
			if (!"README.txt".equals(fileName)) {
				throw new RuntimeException("Unexpected file name/directory: " + directory + "/" + fileName);
			}
			value = "true";
			return;
		}

		final GenericDialog dialog = new GenericDialog("Example");
		dialog.addStringField("prefix", "(prefix)");
		dialog.showDialog();
		if (dialog.wasCanceled()) return;

		final String prefix = dialog.getNextString();
		value = prefix + arg;
	}

	@Override
	public String toString() {
		return value;
	}

}
