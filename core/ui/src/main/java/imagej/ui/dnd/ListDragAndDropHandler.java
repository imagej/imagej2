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

import java.util.List;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Drag-and-drop handler for lists of objects.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = DragAndDropHandler.class)
public class ListDragAndDropHandler extends AbstractDragAndDropHandler<List<?>>
{

	@Parameter(required = false)
	private DragAndDropService dragAndDropService;

	// -- DragAndDropHandler methods --

	@Override
	public boolean supports(final List<?> list, final Display<?> display) {
		if (dragAndDropService == null) return false;
		if (!super.supports(list, display)) return false;

		// empty lists are trivially compatible
		if (list.size() == 0) return true;

		// the list is deemed compatible if at least one item is compatible
		for (final Object item : list) {
			if (dragAndDropService.supports(item, display)) return true;
		}
		return false;
	}

	@Override
	public boolean drop(final List<?> list, final Display<?> display) {
		if (dragAndDropService == null) return false;
		check(list, display);
		if (list == null) return true; // trivial case

		// dropping an empty list trivially succeeds
		if (list.size() == 0) return true;

		// use the drag-and-drop service to handle each item separately
		boolean success = false;
		for (final Object item : list) {
			if (dragAndDropService.supports(item, display)) {
				final boolean result = dragAndDropService.drop(item, display);
				if (result) success = true;
			}
		}
		return success;
	}

	// -- Typed methods --

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Class<List<?>> getType() {
		return (Class) List.class;
	}

}
