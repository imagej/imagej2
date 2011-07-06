//
// OpenImage.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.core.plugins.io;

import imagej.data.Dataset;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.Log;

import java.io.File;

import loci.common.StatusListener;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgPlus;
import net.imglib2.io.ImgIOException;
import net.imglib2.io.ImgOpener;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Opens the selected file as a {@link Dataset}.
 * 
 * @author Curtis Rueden
 */
@Plugin(iconPath = "/icons/plugins/folder_picture.png", menu = {
	@Menu(label = "File", mnemonic = 'f'),
	@Menu(label = "Open [IJ2]...", weight = 1, mnemonic = 'o',
		accelerator = "control O") })
public class OpenImage<T extends RealType<T> & NativeType<T>> implements
	ImageJPlugin, StatusListener
{

	@Parameter(label = "File to open")
	private File inputFile;

	@Parameter(output = true)
	private Dataset dataset;

	@Override
	public void run() {
		final String id = inputFile.getAbsolutePath();

		// open image
		final ImgOpener imageOpener = new ImgOpener();
		try {
			imageOpener.addStatusListener(this);
			final ImgPlus<T> imgPlus = imageOpener.openImg(id);
			dataset = new Dataset(imgPlus);
		}
		catch (final ImgIOException e) {
			Log.error(e);
		}
		catch (final IncompatibleTypeException e) {
			Log.error(e);
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

	private long lastTime;

	@Override
	public void statusUpdated(loci.common.StatusEvent e) {
		long time = System.currentTimeMillis();
		final int progress = e.getProgressValue();
		final int maximum = e.getProgressMaximum();
		final String message = e.getStatusMessage();
		final boolean warn = e.isWarning();

		// don't update more than 20 times/sec
		if (time - lastTime < 50 && progress > 0 && progress < maximum && !warn) {
			return;
		}
		lastTime = time;

		Events.publish(new StatusEvent(progress, maximum, message, warn));
	}

}
