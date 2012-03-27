/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.updater.gui;

import imagej.updater.core.FileObject;
import imagej.updater.core.FilesCollection;

import javax.swing.JComboBox;

@SuppressWarnings("serial")
public class ViewOptions extends JComboBox {

	public static enum Option {
		ALL("all files"), INSTALLED("installed files only"), UNINSTALLED(
			"uninstalled files only"), UPTODATE("only up-to-date files"), UPDATEABLE(
			"updateable files only"),
			LOCALLY_MODIFIED("locally modified files only"), MANAGED(
				"downloaded files only"), OTHERS("local-only files"),
			CHANGES("changes"), SELECTED("selected");

		String label;

		Option(final String label) {
			this.label = "View " + label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	protected final int customOptionStart;

	public ViewOptions() {
		super(Option.values());

		customOptionStart = getItemCount();

		setMaximumRowCount(15);
	}

	public void clearCustomOptions() {
		while (getItemCount() > customOptionStart)
			removeItemAt(customOptionStart);
	}

	protected interface CustomOption {

		Iterable<FileObject> getIterable();
	}

	public void addCustomOption(final String title,
		final Iterable<FileObject> iterable)
	{
		addItem(new CustomOption() {

			@Override
			public String toString() {
				return title;
			}

			@Override
			public Iterable<FileObject> getIterable() {
				return iterable;
			}
		});
	}

	public Iterable<FileObject> getView(final FileTable table) {
		if (getSelectedIndex() >= customOptionStart) return ((CustomOption) getSelectedItem())
			.getIterable();

		final FilesCollection files =
			table.files.clone(table.getAllFiles().notHidden());
		files.sort();
		switch ((Option) getSelectedItem()) {
			case INSTALLED:
				return files.installed();
			case UNINSTALLED:
				return files.uninstalled();
			case UPTODATE:
				return files.upToDate();
			case UPDATEABLE:
				return files.shownByDefault();
			case LOCALLY_MODIFIED:
				return files.locallyModified();
			case MANAGED:
				return files.managedFiles();
			case OTHERS:
				return files.localOnly();
			case CHANGES:
				return files.changes();
			case SELECTED:
				return table.getSelectedFiles();
			default:
				return files;
		}
	}
}
