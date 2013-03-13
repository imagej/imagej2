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

package imagej.ui.dnd;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.lut.LutService;
import imagej.display.Display;
import imagej.display.DisplayService;

import java.io.File;

import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.scijava.plugin.Plugin;

/**
 * @author Barry DeZonia
 */
@Plugin(type = DragAndDropHandler.class)
public class LutDragAndDropHandler extends AbstractDragAndDropHandler {

	// -- constants --

	private static final int WIDTH = 256;
	private static final int HEIGHT = 32;

	public static final String MIME_TYPE = "application/imagej-lut";

	// -- DragAndDropHandler methods --

	@Override
	public boolean isCompatible(Display<?> display, Object data) {
		if ((display != null) && !(display instanceof ImageDisplay)) return false;
		if (data instanceof ColorTable) {
			return true;
		}
		if (data instanceof File) {
			File f = (File) data;
			return f.getAbsolutePath().toLowerCase().endsWith(".lut");
		}
		if (data instanceof DragAndDropData) {
			DragAndDropData dndData = (DragAndDropData) data;
			for (final String mimeType : dndData.getMimeTypes()) {
				if (MIME_TYPE.equals(mimeType)) return true;
			}
		}
		return false;
	}

	@Override
	public boolean drop(Display<?> display, Object data) {
		ColorTable colorTable = getColorTable(data);
		if (colorTable == null) return false;
		if (display == null) {
			DatasetService dsSrv = getContext().getService(DatasetService.class);
			String name = getName(data);
			Dataset dataset =
				dsSrv.create(new UnsignedByteType(), new long[] { WIDTH, HEIGHT },
					name, new AxisType[] { Axes.X, Axes.Y });
			rampFill(dataset);
			// TODO - is this papering over a bug in the dataset/imgplus code?
			if (dataset.getColorTableCount() == 0) dataset.initializeColorTables(1);
			dataset.setColorTable(colorTable, 0);
			DisplayService dispSrv = getContext().getService(DisplayService.class);
			final Display<?> d = dispSrv.createDisplay(dataset);
			// HACK: update display a bit later - else data is not drawn correctly
			new Thread() {

				@Override
				public void run() {
					try {
						Thread.sleep(50);
					}
					catch (Exception e) {/**/}
					d.update();
				}
			}.start();
			return true;
		}
		if (!(display instanceof ImageDisplay)) return false;
		ImageDisplayService imgSrv =
			getContext().getService(ImageDisplayService.class);
		DatasetView view = imgSrv.getActiveDatasetView((ImageDisplay) display);
		if (view == null) return false;
		int channel = view.getIntPosition(Axes.CHANNEL);
		view.setColorTable(colorTable, channel);
		return true;
	}

	// -- helpers --

	private ColorTable getColorTable(Object data) {
		if (data instanceof ColorTable) {
			return (ColorTable) data; // TODO: data.clone()?
		}
		if (data instanceof File) {
			File file = (File) data;
			LutService lutService = getContext().getService(LutService.class);
			return lutService.loadLut(file);
		}
		if (data instanceof DragAndDropData) {
			DragAndDropData dndData = (DragAndDropData) data;
			return (ColorTable) dndData.getData(MIME_TYPE);
		}
		return null;
	}

	private String getName(Object data) {
		if (data instanceof File) {
			File file = (File) data;
			String filename = file.getName();
			if (filename != null && filename.length() > 0) return filename;
		}
		return "Lookup table";
	}

	private void rampFill(Dataset dataset) {
		RandomAccess<? extends RealType<?>> accessor =
			dataset.getImgPlus().randomAccess();
		for (int x = 0; x < WIDTH; x++) {
			accessor.setPosition(x, 0);
			for (int y = 0; y < HEIGHT; y++) {
				accessor.setPosition(y, 1);
				accessor.get().setReal(x);
			}
		}
	}

}
