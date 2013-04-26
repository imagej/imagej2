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

import ij.IJ;
import ij.Menus;
import ij.plugin.PlugIn;

import java.awt.Menu;
import java.awt.MenuItem;
import java.util.Hashtable;

import javax.swing.SwingUtilities;

import org.scijava.Context;

/**
 * An ImageJ 1.x plugin to switch back from the legacy mode.
 * 
 * @author Johannes Schindelin
 */
public class SwitchToModernMode implements PlugIn {

	@Override
	public void run(String arg) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					// Make sure that we have a valid context.
					IJ.runPlugIn(Context.class.getName(), null);
					/*
					 * The LegacyService which has the ImageJ context.
					 * 
					 * Since ImageJ 1.x had no context, we have to set this variable just before
					 * switching to the legacy mode.
					 */
					final LegacyService legacyService = (LegacyService)
						IJ.runPlugIn(LegacyService.class.getName(), null);
					if (legacyService == null) {
						IJ.error("No LegacyService available!");
						return;
					}
					legacyService.toggleLegacyMode(false);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Register this ImageJ 1.x plugin.
	 * 
	 * @param service the legacy service holding the ImageJ context
	 */
	static void registerMenuItem(final LegacyService service) {
		// inject Help>Switch to Modern Mode
		final String menuLabel = "Switch to Modern Mode";
		@SuppressWarnings("unchecked")
		final Hashtable<String, String> commands = Menus.getCommands();
		if (!commands.containsKey(menuLabel)) {
			final Menu helpMenu = Menus.getMenuBar().getHelpMenu();
			final MenuItem item = new MenuItem(menuLabel);
			item.addActionListener(IJ.getInstance());
			helpMenu.add(item);

			commands.put(menuLabel, SwitchToModernMode.class.getName());
		}
	}
}
