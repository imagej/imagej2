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
import imagej.display.Display;
import imagej.io.StatusDispatcher;
import imagej.io.event.FileSavedEvent;
import imagej.menu.MenuConstants;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.Result;
import imagej.ui.UIService;
import imagej.widget.FileWidget;

import java.io.File;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgPlus;

import ome.scifio.io.img.ImgIOException;
import ome.scifio.io.img.ImgSaver;

import org.scijava.ItemIO;
import org.scijava.app.StatusService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Saves the current {@link Dataset} to disk using a user-specified file name.
 * 
 * @author Mark Hiner
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.FILE_LABEL, weight = MenuConstants.FILE_WEIGHT,
		mnemonic = MenuConstants.FILE_MNEMONIC),
	@Menu(label = "Save As...", weight = 21) })
public class SaveAsImage extends ContextCommand {

	@Parameter
	private LogService log;

	@Parameter
	private EventService eventService;

	@Parameter
	private StatusService statusService;

	@Parameter
	private UIService uiService;

	@Parameter(label = "File to save", style = FileWidget.SAVE_STYLE,
		initializer = "initOutputFile", persist = false)
	private File outputFile;

	@Parameter
	private Dataset dataset;

	@Parameter(type = ItemIO.BOTH)
	private Display<?> display;

	public void initOutputFile() {
		outputFile = new File(dataset.getImgPlus().getSource());
	}

	@Override
	public void run() {
		@SuppressWarnings("rawtypes")
		final ImgPlus img = dataset.getImgPlus();
		boolean overwrite = true;
		Result result = null;

		// TODO prompts the user if the file is dirty or being saved to a new
		// location. Could remove the isDirty check to always overwrite the current
		// file
		if (outputFile.exists() &&
			(dataset.isDirty() || !outputFile.getAbsolutePath().equals(
				img.getSource())))
		{
			result =
				uiService.showDialog("\"" + outputFile.getName() +
					"\" already exists. Do you want to replace it?", "Save [IJ2]",
					DialogPrompt.MessageType.WARNING_MESSAGE,
					DialogPrompt.OptionType.YES_NO_OPTION);
			overwrite = result == DialogPrompt.Result.YES_OPTION;
		}

		if (overwrite) {
			final ImgSaver imageSaver = new ImgSaver();
			boolean saveImage = true;
			try {
				imageSaver.addStatusListener(new StatusDispatcher(statusService));

				if (imageSaver.isCompressible(img)) {
					result =
					uiService.showDialog("Your image contains axes other than XYZCT.\n"
						+ "When saving, these may be compressed to the "
						+ "Channel axis (or the save process may simply fail).\n"
						+ "Would you like to continue?", "Save [IJ2]",
						DialogPrompt.MessageType.WARNING_MESSAGE,
						DialogPrompt.OptionType.YES_NO_OPTION);
				}

				saveImage = result == DialogPrompt.Result.YES_OPTION;
				imageSaver.saveImg(outputFile.getAbsolutePath(), img);
				eventService.publish(new FileSavedEvent(img.getSource()));
			}
			catch (final ImgIOException e) {
				log.error(e);
				uiService.showDialog(e.getMessage(), "IJ2: Save Error",
					DialogPrompt.MessageType.ERROR_MESSAGE);
				return;
			}
			catch (final IncompatibleTypeException e) {
				log.error(e);
				uiService.showDialog(e.getMessage(), "IJ2: Save Error",
					DialogPrompt.MessageType.ERROR_MESSAGE);
				return;
			}

			if (saveImage) {
				dataset.setName(outputFile.getName());
				dataset.setDirty(false);

				display.setName(outputFile.getName());
				// NB - removed when display became ItemIO.Both.
				//  Restore later if necessary.
				//display.update();
			}
		}
	}

}
