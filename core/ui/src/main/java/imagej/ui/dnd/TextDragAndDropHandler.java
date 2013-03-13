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

import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.TextDisplay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.scijava.plugin.Plugin;

/**
 * @author Barry DeZonia
 */
@Plugin(type = DragAndDropHandler.class)
public class TextDragAndDropHandler extends AbstractDragAndDropHandler {

	// -- constants --

	public static final String MIME_TYPE =
		"text/plain; class=java.lang.String; charset=Unicode";

	// -- DragAndDropHandler methods --

	@Override
	public boolean isCompatible(Display<?> display, Object data) {
		if ((display != null) && !(display instanceof TextDisplay)) return false;
		if (data instanceof String) {
			return true;
		}
		if (data instanceof File) {
			// TODO - accept anything and make handler very low priority. Then after
			// trying every other handler first this handler will assume it's a text
			// file and open it.
			File file = (File) data;
			return file.getAbsolutePath().toLowerCase().endsWith(".txt");
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
		String str = getString(data);
		if (str == null) return false;
		if (display == null) {
			DisplayService dispSrv = getContext().getService(DisplayService.class);
			final Display<?> d = dispSrv.createDisplay(str);
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
		if (!(display instanceof TextDisplay)) return false;
		TextDisplay txtDisp = (TextDisplay) display;
		txtDisp.append(str); // TODO - do a paste rather than an append
		return true;
	}

	// -- helpers --

	private String getString(Object data) {
		if (data instanceof String) {
			return (String) data;
		}
		if (data instanceof File) {
			File file = (File) data;
			try {
				StringBuilder builder = new StringBuilder();
				BufferedReader in = new BufferedReader(new FileReader(file));
				String line;
				while ((line = in.readLine()) != null) {
					builder.append(line);
					builder.append("\n");
				}
				in.close();
				return builder.toString();
			}
			catch (Exception e) {
				return null;
			}
		}
		if (data instanceof DragAndDropData) {
			DragAndDropData dndData = (DragAndDropData) data;
			return (String) dndData.getData(MIME_TYPE);
		}
		return null;
	}

}
