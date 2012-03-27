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
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;
import imagej.updater.core.FilesCollection;
import imagej.updater.core.FilesCollection.UpdateSite;
import imagej.updater.util.Util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

/*
 * This class' role is to be in charge of how the Table should be displayed
 */
@SuppressWarnings("serial")
public class FileTable extends JTable {

	protected UpdaterFrame updaterFrame;
	protected FilesCollection files;
	protected List<FileObject> row2file;
	private FileTableModel fileTableModel;
	protected Font plain, bold;

	public FileTable(final UpdaterFrame updaterFrame) {
		this.updaterFrame = updaterFrame;
		files = updaterFrame.files;
		row2file = new ArrayList<FileObject>();
		for (final FileObject file : files) {
			row2file.add(file);
		}

		// Set appearance of table
		setShowGrid(false);
		setIntercellSpacing(new Dimension(0, 0));
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setRequestFocusEnabled(false);

		// set up the table properties and other settings
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setCellSelectionEnabled(true);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(true);

		fileTableModel = new FileTableModel(files);
		setModel(fileTableModel);
		getModel().addTableModelListener(this);
		setColumnWidths(250, 100);

		setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(final JTable table,
				final Object value, final boolean isSelected, final boolean hasFocus,
				final int row, final int column)
			{
				final Component comp =
					super.getTableCellRendererComponent(table, value, isSelected,
						hasFocus, row, column);
				setStyle(comp, row, column);
				return comp;
			}
		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(final MouseEvent e) {
				maybeShowPopupMenu(e);
			}
		});
	}

	/*
	 * This sets the font to bold when the user selected an action for
	 * this file, or when it is locally modified.
	 *
	 * It also warns loudly when the file is obsolete, but locally
	 * modified.
	 */
	protected void
		setStyle(final Component comp, final int row, final int column)
	{
		if (plain == null) {
			plain = comp.getFont();
			bold = plain.deriveFont(Font.BOLD);
		}
		final FileObject file = getFile(row);
		if (file == null) return;
		comp.setFont(file.actionSpecified() || file.isLocallyModified() ? bold
			: plain);
		comp.setForeground(file.getStatus() == Status.OBSOLETE_MODIFIED ? Color.red
			: Color.black);
	}

	private void setColumnWidths(final int col1Width, final int col2Width) {
		final TableColumn col1 = getColumnModel().getColumn(0);
		final TableColumn col2 = getColumnModel().getColumn(1);

		col1.setPreferredWidth(col1Width);
		col1.setMinWidth(col1Width);
		col1.setResizable(false);
		col2.setPreferredWidth(col2Width);
		col2.setMinWidth(col2Width);
		col2.setResizable(true);
	}

	public FilesCollection getAllFiles() {
		return files;
	}

	public void setFiles(final Iterable<FileObject> files) {
		fileTableModel.setFiles(files);
	}

	@Override
	public TableCellEditor getCellEditor(final int row, final int col) {
		final FileObject file = getFile(row);

		// As we follow FileTableModel, 1st column is filename
		if (col == 0) return super.getCellEditor(row, col);
		final Action[] actions = files.getActions(file);
		return new DefaultCellEditor(new JComboBox(actions));
	}

	public void maybeShowPopupMenu(final MouseEvent e) {
		if (!e.isPopupTrigger() &&
			(Util.isMacOSX() || (e.getModifiers() & InputEvent.META_MASK) == 0)) return;
		final Iterable<FileObject> selected =
			getSelectedFiles(e.getY() / getRowHeight());
		if (!selected.iterator().hasNext()) return;
		final JPopupMenu menu = new JPopupMenu();
		int count = 0;
		for (final FileObject.Action action : files.getActions(selected)) {
			final JMenuItem item = new JMenuItem(action.toString());
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					for (final FileObject file : selected)
						setFileAction(file, action);
				}
			});
			menu.add(item);
			count++;
		}
		if (count == 0) {
			final JMenuItem noActions = new JMenuItem("<No common actions>");
			noActions.setEnabled(false);
			menu.add(noActions);
		}
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	public FileObject getFile(final int row) {
		final FileObject.LabeledFile file =
			(FileObject.LabeledFile) getValueAt(row, 0);
		return file == null ? null : file.getFile();
	}

	public Iterable<FileObject> getSelectedFiles() {
		return getSelectedFiles(-1);
	}

	public Iterable<FileObject> getSelectedFiles(final int fallbackRow) {
		int[] rows = getSelectedRows();
		if (fallbackRow >= 0 && getFile(fallbackRow) != null &&
			(rows.length == 0 || indexOf(rows, fallbackRow) < 0)) rows =
			new int[] { fallbackRow };
		final FileObject[] result = new FileObject[rows.length];
		for (int i = 0; i < rows.length; i++)
			result[i] = getFile(rows[i]);
		return Arrays.asList(result);
	}

	protected int indexOf(final int[] array, final int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	public String[] getUpdateSitesWithUploads(final FilesCollection files) {
		final Set<String> sites = new HashSet<String>();
		for (final FileObject file : files.toUpload())
			sites.add(file.updateSite);
		return sites.toArray(new String[sites.size()]);
	}

	public boolean areAllSelectedFilesUploadable() {
		if (getSelectedRows().length == 0) return false;
		for (final FileObject file : getSelectedFiles())
			if (!file.isUploadable(updaterFrame.files)) return false;
		return true;
	}

	public boolean chooseUpdateSite(final FilesCollection files,
		final FileObject file)
	{
		final List<String> list = new ArrayList<String>();
		for (final String name : files.getUpdateSiteNames()) {
			final UpdateSite site = files.getUpdateSite(name);
			if (site.isUploadable()) list.add(name);
		}
		if (list.size() == 0) {
			error("No upload site available");
			return false;
		}
		if (list.size() == 1 &&
			list.get(0).equals(FilesCollection.DEFAULT_UPDATE_SITE))
		{
			file.updateSite = FilesCollection.DEFAULT_UPDATE_SITE;
			return true;
		}
		final String updateSite =
			SwingTools.getChoice(updaterFrame, list,
				"To which upload site do you want to upload " + file.filename + "?",
				"Upload site");
		if (updateSite == null) return false;
		file.updateSite = updateSite;
		return true;
	}

	protected void setFileAction(final FileObject file, final Action action) {
		if (!file.getStatus().isValid(action)) return;
		if (action == Action.UPLOAD) {
			final String[] sitesWithUploads =
				getUpdateSitesWithUploads(updaterFrame.files);
			if (sitesWithUploads.length > 1) {
				error("Internal error: multiple upload sites selected");
				return;
			}
			final boolean isNew = file.getStatus() == Status.LOCAL_ONLY;
			if (sitesWithUploads.length == 0) {
				if (isNew && !chooseUpdateSite(updaterFrame.files, file)) return;
			}
			else {
				final String siteName = sitesWithUploads[0];
				if (isNew) file.updateSite = siteName;
				else if (!file.updateSite.equals(siteName)) {
					error("Already have uploads for site '" + siteName +
						"', cannot upload to '" + file.updateSite + "', too!");
					return;
				}
			}
		}
		file.setAction(updaterFrame.files, action);
		fireFileChanged(file);
	}

	protected class FileTableModel extends AbstractTableModel {

		private FilesCollection files;
		protected Map<FileObject, Integer> fileToRow;
		protected List<FileObject> rowToFile;

		public FileTableModel(final FilesCollection files) {
			this.files = files;
		}

		public void setFiles(final Iterable<FileObject> files) {
			setFiles(this.files.clone(files));
		}

		public void setFiles(final FilesCollection files) {
			this.files = files;
			fileToRow = null;
			rowToFile = null;
			fireTableChanged(new TableModelEvent(fileTableModel));
		}

		@Override
		public int getColumnCount() {
			return 2; // Name of file, status
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			switch (columnIndex) {
				case 0:
					return String.class; // filename
				case 1:
					return String.class; // status/action
				default:
					return Object.class;
			}
		}

		@Override
		public String getColumnName(final int column) {
			switch (column) {
				case 0:
					return "Name";
				case 1:
					return "Status/Action";
				default:
					throw new Error("Column out of range");
			}
		}

		public FileObject getEntry(final int rowIndex) {
			return rowToFile.get(rowIndex);
		}

		@Override
		public int getRowCount() {
			return files.size();
		}

		@Override
		public Object getValueAt(final int row, final int column) {
			updateMappings();
			if (row < 0 || row >= files.size()) return null;
			return rowToFile.get(row).getLabeledFile(column);
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return columnIndex == 1;
		}

		@Override
		public void setValueAt(final Object value, final int row, final int column)
		{
			if (column == 1) {
				final Action action = (Action) value;
				final FileObject file = getFile(row);
				setFileAction(file, action);
			}
		}

		public void fireRowChanged(final int row) {
			fireTableRowsUpdated(row, row);
		}

		public void fireFileChanged(final FileObject file) {
			updateMappings();
			final Integer row = fileToRow.get(file);
			if (row != null) fireRowChanged(row.intValue());
		}

		protected void updateMappings() {
			if (fileToRow != null) return;
			fileToRow = new HashMap<FileObject, Integer>();
			rowToFile = new ArrayList<FileObject>();
			// the table may be sorted, and we need the model's row
			int i = 0;
			for (final FileObject f : files) {
				fileToRow.put(f, new Integer(i++));
				rowToFile.add(f);
			}
		}
	}

	public void fireFileChanged(final FileObject file) {
		fileTableModel.fireFileChanged(file);
	}

	protected void error(final String message) {
		SwingTools.showMessageBox(updaterFrame, message, JOptionPane.ERROR_MESSAGE);
	}
}
