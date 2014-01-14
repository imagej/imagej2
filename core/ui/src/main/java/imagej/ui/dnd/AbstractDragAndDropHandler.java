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

package imagej.ui.dnd;

import imagej.display.Display;

import org.scijava.plugin.AbstractHandlerPlugin;

/**
 * Abstract superclass for {@link DragAndDropHandler}s.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractDragAndDropHandler<D> extends
	AbstractHandlerPlugin<D> implements DragAndDropHandler<D>
{

	// -- DragAndDropHandler methods --

	@Override
	public boolean supports(final D dataObject, final Display<?> display) {
		return supportsDisplay(display) && supports(dataObject);
	}

	@Override
	public boolean supportsData(final DragAndDropData data) {
		return data.isSupported(getType()) && supports(convertDataUnchecked(data));
	}

	@Override
	public boolean supportsData(final DragAndDropData data,
		final Display<?> display)
	{
		return supportsDisplay(display) && supportsData(data);
	}

	@Override
	public boolean supportsObject(final Object object) {
		return object != null && getType().isAssignableFrom(object.getClass()) &&
			supports(convertObjectUnchecked(object));
	}

	@Override
	public boolean supportsObject(final Object object, final Display<?> display) {
		return supportsDisplay(display) && supportsObject(object);
	}

	@Override
	public boolean supportsDisplay(final Display<?> display) {
		return true;
	}

	@Override
	public D convertData(final DragAndDropData data) {
		if (!supportsData(data)) {
			throw new IllegalArgumentException("Incompatible data object");
		}
		return convertDataUnchecked(data);
	}

	@Override
	public D convertObject(final Object object) {
		if (!supportsObject(object)) {
			throw new IllegalArgumentException("Incompatible data object");
		}
		return convertObjectUnchecked(object);
	}

	@Override
	public boolean dropData(final DragAndDropData data, final Display<?> display)
	{
		return drop(convertData(data), display);
	}

	@Override
	public boolean dropObject(final Object object, final Display<?> display) {
		return drop(convertObject(object), display);
	}

	// -- Internal methods --

	/**
	 * Throws {@link IllegalArgumentException} if (data, display) pair is
	 * incompatible.
	 */
	protected void check(final D dataObject, final Display<?> display) {
		// NB: First check compatibility of data and display individually.
		if (!supports(dataObject)) {
			throw new IllegalArgumentException("Incompatible data object");
		}
		if (!supportsDisplay(display)) {
			throw new IllegalArgumentException("Incompatible display");
		}
		// NB: The data and display are individually compatible,
		// but are they compatible with one another?
		if (!supports(dataObject, display)) {
			throw new IllegalArgumentException(
				"Data object and display are incompatible");
		}
	}

	/**
	 * Converts the given data to this handler's native data type, without
	 * verifying compatibility first.
	 */
	protected D convertDataUnchecked(final DragAndDropData data) {
		return data.getData(getType());
	}

	/**
	 * Converts the given object to this handler's native data type, without
	 * verifying compatibility first.
	 */
	protected D convertObjectUnchecked(final Object object) {
		@SuppressWarnings("unchecked")
		final D dataObject = (D) object;
		return dataObject;
	}

}
