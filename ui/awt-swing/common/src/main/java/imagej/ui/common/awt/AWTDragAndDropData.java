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

package imagej.ui.common.awt;

import imagej.ui.dnd.AbstractDragAndDropData;
import imagej.ui.dnd.DragAndDropData;
import imagej.ui.dnd.MIMEType;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.IOException;
import java.util.ArrayList;

import org.scijava.Context;
import org.scijava.log.LogService;

/**
 * AWT implementation of {@link DragAndDropData}.
 * 
 * @author Curtis Rueden
 */
public class AWTDragAndDropData extends AbstractDragAndDropData {

	private final Transferable t;

	public AWTDragAndDropData(final Context context, final Transferable t) {
		setContext(context);
		this.t = t;
	}

	// -- DragAndDrop methods --

	@Override
	public boolean isSupported(final MIMEType mimeType) {
		for (final DataFlavor flavor : t.getTransferDataFlavors()) {
			if (mimeType.isCompatible(new MIMEType(flavor.getMimeType()))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getData(final MIMEType mimeType) {
		for (final DataFlavor flavor : t.getTransferDataFlavors()) {
			if (mimeType.isCompatible(new MIMEType(flavor.getMimeType()))) {
				try {
					return t.getTransferData(flavor);
				}
				catch (final UnsupportedFlavorException exc) {
					throw new IllegalArgumentException("Unsupported MIME type: " +
						mimeType, exc);
				}
				catch (final IOException exc) {
					final LogService log = getContext().getService(LogService.class);
					if (log != null) log.error("Drag-and-drop error", exc);
				}
				catch (final InvalidDnDOperationException exc) {
					// NB: This exception is thrown when the data is requested at an
					// inappropriate time, typically too early in the drag-and-drop
					// process such as during a dragEnter event. In that case, we simply
					// return null for now.
					//
					// ImageJ's drag-and-drop layer does its best to deal with it.
					return null;
				}
			}
		}
		throw new IllegalArgumentException("Unsupported MIME type: " + mimeType);
	}

	@Override
	public ArrayList<MIMEType> getMIMETypes() {
		final ArrayList<MIMEType> mimeTypes = new ArrayList<MIMEType>();
		for (final DataFlavor flavor : t.getTransferDataFlavors()) {
			mimeTypes.add(new MIMEType(flavor.getMimeType()));
		}
		return mimeTypes;
	}

}
