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

package imagej.io.plugins;

import imagej.data.Dataset;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.io.IOService;
import imagej.ui.DialogPrompt;
import imagej.ui.UIService;
import imagej.util.Log;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.io.ImgIOException;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Resets the current {@link Dataset} to its original state.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.FILE_LABEL, weight = MenuConstants.FILE_WEIGHT,
		mnemonic = MenuConstants.FILE_MNEMONIC),
	@Menu(label = "Revert", weight = 20, mnemonic = 'v',
		accelerator = "control R") })
public class RevertImage<T extends RealType<T> & NativeType<T>> implements
	ImageJPlugin
{

	@Parameter(persist = false)
	private IOService ioService;

	@Parameter(persist = false)
	private UIService uiService;

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	@Override
	public void run() {
		final String source = dataset.getSource();
		if (source == null) {
			uiService.showDialog("Cannot revert an image with no source");
			return;
		}

		try {
			ioService.revertDataset(dataset);
		}
		catch (final ImgIOException e) {
			Log.error(e);
			uiService.showDialog(e.getMessage(), "ImageJ",
				DialogPrompt.MessageType.ERROR_MESSAGE);
		}
		catch (final IncompatibleTypeException e) {
			Log.error(e);
			uiService.showDialog(e.getMessage(), "ImageJ",
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
