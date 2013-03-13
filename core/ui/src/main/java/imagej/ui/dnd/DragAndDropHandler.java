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
import imagej.plugin.ImageJPlugin;

import org.scijava.Contextual;
import org.scijava.Prioritized;
import org.scijava.plugin.Plugin;

/**
 * Interface for drag-and-drop handlers. A drag-and-drop handler defines
 * behavior for drag-and-drop operations with respect to a specific sort of
 * objects.
 * <p>
 * Drag-and-drop handlers discoverable at runtime must implement this interface
 * and be annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link DragAndDropHandler}.class. While it possible to create a drag-and-drop
 * handler merely by implementing this interface, it is encouraged to instead
 * extend {@link AbstractDragAndDropHandler}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see DragAndDropService
 */
public interface DragAndDropHandler extends ImageJPlugin, Contextual,
	Prioritized
{

	/**
	 * Gets whether this handler supports the given display and data Object.
	 */
	boolean isCompatible(final Display<?> display, final Object data);

	/**
	 * Performs a drop operation with the given data Object in the specified
	 * display.
	 * 
	 * @throws IllegalArgumentException if the handler is not compatible with the
	 *           given display or drag-and-drop data.
	 */
	boolean drop(final Display<?> display, final Object data);

}
