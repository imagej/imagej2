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

package imagej.core.commands.io;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.io.IOService;
import imagej.menu.MenuConstants;
import imagej.text.TextService;
import imagej.ui.DialogPrompt;
import imagej.ui.UIService;

import java.io.File;
import java.io.IOException;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.io.ImgIOException;

import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Opens the selected file.
 * 
 * @author Curtis Rueden
 * @author Mark Hiner
 */
@Plugin(type = Command.class, iconPath = "/icons/commands/folder_picture.png",
	menu = {
		@Menu(label = MenuConstants.FILE_LABEL,
			weight = MenuConstants.FILE_WEIGHT,
			mnemonic = MenuConstants.FILE_MNEMONIC),
		@Menu(label = "Open...", weight = 1, mnemonic = 'o',
			accelerator = "control O") })
public class OpenFile extends ContextCommand {

	@Parameter
	private LogService log;

	@Parameter
	private IOService ioService;

	@Parameter
	private TextService textService;

	@Parameter
	private UIService uiService;

	@Parameter(label = "File to open")
	private File inputFile;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset dataset;

	@Parameter(type = ItemIO.OUTPUT, label = "Text")
	private String html;

	@Override
	public void run() {
		final String source = inputFile.getAbsolutePath();
		if (ioService.isImageData(source)) {
			openImage(source);
		}
		else if (textService.isText(inputFile)) {
			openText(inputFile);
		}
		else {
			uiService.showDialog("The file is not in a supported format\n\n" +
				inputFile.getPath(),
				DialogPrompt.MessageType.ERROR_MESSAGE);
		}
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(final File inputFile) {
		this.inputFile = inputFile;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
	}

	public String getHTML() {
		return html;
	}

	public void setHTML(final String html) {
		this.html = html;
	}

	// -- Helper methods --

	private void openImage(String source) {
		try {
			dataset = ioService.loadDataset(source);
		}
		catch (final ImgIOException e) {
			log.error(e);
			error(e.getMessage());
		}
		catch (final IncompatibleTypeException e) {
			log.error(e);
			error(e.getMessage());
		}
	}

	private void openText(File file) {
		try {
			html = textService.asHTML(file);
		}
		catch (final IOException e) {
			log.error(e);
			error(e.getMessage());
		}
	}

	private void error(String message) {
		uiService.showDialog(message, DialogPrompt.MessageType.ERROR_MESSAGE);
	}

}
