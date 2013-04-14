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

import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link DragAndDropData}, which provides a
 * UI-agnostic way to bundle an object together with its MIME type.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class DefaultDragAndDropData extends AbstractDragAndDropData {

	// -- Fields --

	private final String mime;
	private final Object data;

	// -- Constructor --

	public DefaultDragAndDropData(final String mimeType, final Object data) {
		this.mime = createMIMEType(mimeType, data);
		this.data = data;
	}

	// -- DragAndDropData methods --

	@Override
	public boolean isSupported(final String mimeType) {
		return mime.equals(mimeType) || mime.startsWith(mimeType + ";");
	}

	@Override
	public Object getData(final String mimeType) {
		return isSupported(mimeType) ? data : null;
	}

	@Override
	public List<String> getMIMETypes() {
		return Collections.singletonList(mime);
	}

	// -- Helper methods --

	/**
	 * Ensures the MIME type includes the corresponding fully qualified Java class
	 * name.
	 */
	private String createMIMEType(final String mimeType, final Object o) {
		final String classFragment = "; class=" + o.getClass().getName();
		if (mimeType.endsWith(classFragment)) return mimeType;
		if (mimeType.contains(classFragment + ";")) return mimeType;
		return mimeType + classFragment;
	}

}
