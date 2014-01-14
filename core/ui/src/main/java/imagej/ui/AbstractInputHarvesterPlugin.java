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

package imagej.ui;

import imagej.module.Module;
import imagej.module.ModuleException;
import imagej.module.process.PreprocessorPlugin;
import imagej.widget.AbstractInputHarvester;
import imagej.widget.InputHarvester;

import org.scijava.plugin.Parameter;

/**
 * AbstractInputHarvesterPlugin is an {@link InputHarvester} that implements the
 * {@link PreprocessorPlugin} interface. It is intended to be extended by
 * UI-specific implementations such as {@code SwingInputHarvester}.
 * <p>
 * The input harvester will first check whether the default UI matches that of
 * its implementation; for example, the Swing-based input harvester plugin will
 * only harvest inputs if the Swing UI is currently the default one.
 * </p>
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @param <P> The type of UI component housing the input panel itself.
 * @param <W> The type of UI component housing each input widget.
 */
public abstract class AbstractInputHarvesterPlugin<P, W> extends
	AbstractInputHarvester<P, W> implements PreprocessorPlugin
{

	@Parameter(required = false)
	private UIService uiService;

	private boolean canceled;
	private String cancelReason;

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		if (uiService == null) return; // no UI service means no input harvesting!

		// do not harvest if the UI is inactive!
		if (!uiService.isVisible(getUI())) return;

		// proceed with input harvesting
		try {
			harvest(module);
		}
		catch (final ModuleException e) {
			canceled = true;
			cancelReason = e.getMessage();
		}
	}

	// -- Cancelable methods --

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public String getCancelReason() {
		return cancelReason;
	}

	// -- Internal methods --

	/** Gets the name (or class name) of the input harvester's affiliated UI. */
	protected abstract String getUI();

}
