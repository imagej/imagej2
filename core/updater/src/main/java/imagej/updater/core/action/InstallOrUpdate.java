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

package imagej.updater.core.action;

import imagej.updater.core.FileObject;
import imagej.updater.core.FilesCollection;
import imagej.updater.core.GroupAction;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;

/**
 * The <i>update</i> action.
 * 
 * <p>
 * This class determines whether a bunch of files can be updated (or installed),
 * how to mark them for update, and what to call this action in the GUI.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class InstallOrUpdate implements GroupAction {

	@Override
	public boolean isValid(FilesCollection files, FileObject file) {
		final Status status = file.getStatus();
		return status.isValid(Action.INSTALL) || status.isValid(Action.UPDATE);
	}

	@Override
	public void setAction(FilesCollection files, FileObject file) {
		file.setFirstValidAction(files, Action.INSTALL, Action.UPDATE);
	}

	@Override
	public String getLabel(FilesCollection files, Iterable<FileObject> selected) {
		boolean install = false, update = false;
		for (final FileObject file : selected) {
			final Status status = file.getStatus();
			install = install || status.isValid(Action.INSTALL);
			update = update || status.isValid(Action.UPDATE);
		}
		if (!(install ^ update)) return "Install / Update";
		return update ? "Update" : "Install";
	}

	@Override
	public String toString() {
		return "Install / Update";
	}

}
