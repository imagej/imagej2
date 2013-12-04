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

package imagej.data.lut;

import imagej.command.Command;
import imagej.command.CommandService;
import imagej.command.DynamicCommand;
import imagej.data.display.ColorTables;
import imagej.data.display.ImageDisplay;
import imagej.menu.MenuConstants;
import imagej.module.MutableModuleItem;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import net.imglib2.display.ColorTable;

import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

// FIXME: This command belongs in ij-commands, not ij-data.

/**
 * LUTSelectorPlugin is a {@link Command} that allows one to choose to apply a
 * {@link ColorTable} to an {@link ImageDisplay}. The applicable ColorTables are
 * discovered at runtime.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, label = "LUT Selector", initializer = "init",
	menu = {
		@Menu(label = MenuConstants.IMAGE_LABEL,
			weight = MenuConstants.IMAGE_WEIGHT,
			mnemonic = MenuConstants.IMAGE_MNEMONIC),
		@Menu(label = "Lookup Tables", mnemonic = 'l'),
		@Menu(label = "Select...", weight = 0, mnemonic = 's') })
public class LUTSelectorPlugin extends DynamicCommand {

	// -- Parameters --

	@Parameter
	private LUTService lutService;

	@Parameter
	private CommandService cmdService;

	@Parameter(required = false)
	private ImageDisplay display;

	@Parameter(label = "LUT name", persist = false, callback = "nameChanged")
	private String choice = "Gray";

	@Parameter(required = false, label = "LUT", persist = false)
	private ColorTable table = ColorTables.GRAYS;

	// -- other fields --

	private Map<String, URL> luts = null;

	// -- Command methods --

	@Override
	public void run() {
		// set LUT for current channel of current ImageDisplay
		if (display != null) {
			// CTR FIXME: Avoid circular dependency between ij-data and ij-commands.
			cmdService.run("imagej.plugins.commands.misc.ApplyLookupTable", true,
				new Object[] { "display", display, "tableURL", luts.get(choice) });
		}
	}

	// -- initializers --

	protected void init() {
		LUTFinder finder = new LUTFinder();
		luts = finder.findLUTs();
		ArrayList<String> choices = new ArrayList<String>();
		for (Map.Entry<String, URL> entry : luts.entrySet()) {
			choices.add(entry.getKey());
		}
		Collections.sort(choices);
		MutableModuleItem<String> input =
			getInfo().getMutableInput("choice", String.class);
		input.setChoices(choices);
		input.setValue(this, choices.get(0));
		nameChanged();
	}

	// -- callbacks --

	protected void nameChanged() {
		try {
			table = lutService.loadLUT(luts.get(choice));
		}
		catch (Exception e) {
			// nada
		}
	}

}
