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

package imagej.ui.swing;

import imagej.core.options.OptionsAppearance;
import imagej.options.OptionsService;
import imagej.ui.ApplicationFrame;
import imagej.ui.UIService;
import imagej.ui.swing.sdi.SwingUI;

import java.awt.Dialog.ModalityType;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default implementation of {@link SwingService}.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultSwingService extends AbstractService implements
	SwingService
{

	@Parameter
	private UIService uiService;
	
	@Parameter
	private OptionsService optionsService;

	@Override
	public SwingWindow createWindow(final String title) {
		return createWindow(null, title);
	}

	@Override
	public SwingWindow createWindow(final Window parent, final String title) {
		final boolean useDialogs =
			optionsService.getOptions(OptionsAppearance.class).isUseDialogs();

		final Window owner;
		if (useDialogs && parent == null) {
			// NB: Use the Swing UI's main application frame by default.
			final ApplicationFrame appFrame =
				uiService.getUI(SwingUI.NAME).getApplicationFrame();
			owner = (SwingApplicationFrame) appFrame;
		}
		else owner = parent;

		if (useDialogs) {
			final JDialog dialog = new JDialog(owner, title, ModalityType.MODELESS);
			return new SwingWindow(dialog);
		}
		return new SwingWindow(new JFrame(title));
	}

}
