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

package imagej.plugins.commands.io;

import java.io.IOException;

import net.imagej.Dataset;
import net.imagej.DatasetService;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.log.LogService;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/**
 * Resets the current {@link Dataset} to its original state.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.FILE_LABEL, weight = MenuConstants.FILE_WEIGHT,
		mnemonic = MenuConstants.FILE_MNEMONIC),
	@Menu(label = "Revert", weight = 20, mnemonic = 'v', accelerator = "^R") })
public class RevertImage extends ContextCommand {

	@Parameter
	private LogService log;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private UIService uiService;

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	@Override
	public void run() {
		try {
			datasetService.revert(dataset);
		}
		catch (final IOException exc) {
			log.error(exc);
			uiService.showDialog(exc.getMessage(),
				DialogPrompt.MessageType.ERROR_MESSAGE);
		}
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
	}

}
