//
// ViewOptions.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
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
			LOCALLY_MODIFIED("locally modified files only"), FIJI(
				"Downloaded files only"), OTHERS("Non-downloaded files only"), CHANGES(
				"changes"), SELECTED("selected");

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
			FilesCollection.clone(table.getAllFiles().notHidden());
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
			case FIJI:
				return files.fijiFiles();
			case OTHERS:
				return files.nonFiji();
			case CHANGES:
				return files.changes();
			case SELECTED:
				return table.getSelectedFiles();
			default:
				return files;
		}
	}
}
