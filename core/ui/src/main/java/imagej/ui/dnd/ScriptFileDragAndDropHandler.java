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
import imagej.script.ScriptService;

import java.io.File;

import org.scijava.plugin.Plugin;

/**
 * Drag-and-drop handler for script files.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = DragAndDropHandler.class)
public class ScriptFileDragAndDropHandler extends
	AbstractDragAndDropHandler<File>
{

	// -- DragAndDropHandler methods --

	@Override
	public Class<File> getType() {
		return File.class;
	}

	@Override
	public boolean isCompatible(final File file) {
		if (file == null) return true; // trivial case

		// verify that the file is a script
		final ScriptService scriptService =
			getContext().getService(ScriptService.class);
		if (scriptService == null) return false;
		return scriptService.canHandleFile(file);
	}

	@Override
	public boolean drop(final File file, final Display<?> display) {
		check(file, display);
		if (file == null) return true; // trivial case

		final ScriptService scriptService =
			getContext().getService(ScriptService.class);
		if (scriptService == null) return false;

		// TODO: Use the script service to open the file in the script editor.
		// We may also want to be context sensitive about which type of display
		// received the drop:
		// - If it's the main window, open a new Script Editor window.
		// - If it's an existing Script Editor window, open a new tab.
		// -- This will require the Script Editor windows to be Displays.
		// - Should a drop onto any other displays do anything?

		return true;
	}

}
