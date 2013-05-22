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

package imagej.updater.gui;

import imagej.updater.core.FileObject;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FilesCollection;
import imagej.updater.core.FilesCollection.UpdateSite;
import imagej.updater.core.UploaderService;
import imagej.updater.util.Util;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sourceforge.jwbf.core.actions.util.ActionException;
import net.sourceforge.jwbf.core.actions.util.ProcessException;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

/**
 * The dialog in which the user can choose which update sites to follow.
 * 
 * @author Johannes Schindelin
 */
@SuppressWarnings("serial")
public class SitesDialog extends JDialog implements ActionListener {

	protected UpdaterFrame updaterFrame;
	protected FilesCollection files;
	protected List<String> names;
	protected String newUpdateSiteName;
	protected UpdateSite newUpdateSite;

	protected DataModel tableModel;
	protected JTable table;
	protected JButton add, remove, close;

	public SitesDialog(final UpdaterFrame owner, final FilesCollection files)
	{
		super(owner, "Manage update sites");
		updaterFrame = owner;
		this.files = files;
		names = new ArrayList<String>(files.getUpdateSiteNames());
		names.set(0, FilesCollection.DEFAULT_UPDATE_SITE);

		final Container contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		tableModel = new DataModel();
		table = new JTable(tableModel) {

			@Override
			public void valueChanged(final ListSelectionEvent e) {
				super.valueChanged(e);
				remove.setEnabled(getSelectedRow() > 0);
			}

			@Override
			public boolean isCellEditable(final int row, final int column) {
				return column >= 0 && column < getColumnCount() && row >= 0 && row < getRowCount();
			}

			@Override
			public TableCellEditor getCellEditor(final int row, final int column) {
				final JTextField field = new JTextField();
				return new DefaultCellEditor(field) {
					@Override
					public boolean stopCellEditing() {
						String value = field.getText();
						if ((column == 1 || column == 3) && !value.equals("") && !value.endsWith("/")) {
							value += "/";
						}
						if (column == 0) {
							if (value.equals(getUpdateSiteName(row))) return super.stopCellEditing();
							if (files.getUpdateSite(value) != null) {
								error("Update site '" + value + "' exists already!");
								return false;
							}
						} else if (column == 1) {
							if ("/".equals(value)) value = "";
							final UpdateSite site = getUpdateSite(row);
							if (value.equals(site.url)) return super.stopCellEditing();
							if (!validURL(value)) {
								if (site == null || site.sshHost == null || site.sshHost.equals("")) {
									error("URL does not refer to an update site: " + value + "\n"
										+ "If you want to initialize that site, you need to provide upload information first.");
									return false;
								} else {
									if (!showYesNoQuestion("Initialize upload site?",
											"It appears that the URL\n"
											+ "\t" + value + "\n"
											+ "is not (yet) valid. "
											+ "Do you want to initialize it (host: "
											+ site.sshHost + "; directory: "
											+ site.uploadDirectory + ")?"))
										return false;
									if (!initializeUpdateSite((String)getValueAt(row, 0),
											value, site.sshHost, site.uploadDirectory))
										return false;
								}
							}
							if (row == names.size()) {
								files.addUpdateSite(newUpdateSiteName, value, site.sshHost, site.uploadDirectory, 0l);
								names.add(newUpdateSiteName);
								newUpdateSiteName = null;
								newUpdateSite = null;
							}
						} else if (column == 2) {
							final UpdateSite site = getUpdateSite(row);
							if (value.equals(site.sshHost)) return super.stopCellEditing();
							final int colon = value.indexOf(':');
							if (colon > 0) {
								final String protocol = value.substring(0, colon);
								final UploaderService uploaderService = updaterFrame.getUploaderService();
								if (null == uploaderService.installUploader(protocol, files, updaterFrame.getProgress(null))) {
									error("Unknown upload protocol: " + protocol);
									return false;
								}
							}
						} else if (column == 3) {
							final UpdateSite site = getUpdateSite(row);
							if (value.equals(site.uploadDirectory)) return super.stopCellEditing();
						}
						updaterFrame.enableUploadOrNot();
						return super.stopCellEditing();
					}
				};
			}

			@Override
			public void setValueAt(final Object value, final int row, final int column)
			{
				final String string = (String)value;
				final UpdateSite site = getUpdateSite(row);
				// if the name changed, or if we auto-fill the name from the URL
				switch (column) {
				case 0:
					final String name = getUpdateSiteName(row);
					if (name.equals(string)) return;
					files.renameUpdateSite(name, string);
					names.set(row, string);
					break;
				case 1:
					site.url = string;
					break;
				case 2:
					site.sshHost = string;
					break;
				case 3:
					site.uploadDirectory = string;
					break;
				default:
					updaterFrame.log.error("Whoa! Column " + column + " is not handled!");
				}
				if (!readFromSite(row)) {
					error("There were problems reading from the site '"
						+ getUpdateSiteName(row) + "' (URL: " + getUpdateSite(row).url + ")");
				}
			}

		};
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableModel.setColumnWidths();
		final JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(tableModel.tableWidth, 100));
		contentPane.add(scrollpane);

		final JPanel buttons = new JPanel();
		add = SwingTools.button("Add", "Add", this, buttons);
		remove = SwingTools.button("Remove", "Remove", this, buttons);
		remove.setEnabled(false);
		close = SwingTools.button("Close", "Close", this, buttons);
		contentPane.add(buttons);

		getRootPane().setDefaultButton(close);
		escapeCancels(this);
		pack();
		add.requestFocusInWindow();
		setLocationRelativeTo(owner);
	}

	protected String getUpdateSiteName(int row) {
		if (row == names.size()) return newUpdateSiteName;
		return names.get(row);
	}

	protected UpdateSite getUpdateSite(int row) {
		if (row == names.size()) return newUpdateSite;
		return files.getUpdateSite(names.get(row));
	}

	protected void add() {
		if (newUpdateSite != null) {
			error("Let's fill out the values of the previously added site first, okay?");
			return;
		}
		final int row = names.size();
		newUpdateSiteName = makeUniqueSiteName("New");
		newUpdateSite = new UpdateSite("", "", "", 0l);
		tableModel.rowsChanged();
		tableModel.rowChanged(row);
		table.setRowSelectionInterval(row, row);
	}

	private String makeUniqueSiteName(final String prefix) {
		if (files.getUpdateSite(prefix) == null) return prefix;
		for (int i = 2; ; i++) {
			if (files.getUpdateSite(prefix + "-" + i) == null) return prefix + "-" + i;
		}
	}

	protected void delete(final int row) {
		final String name = names.get(row);
		if (!showYesNoQuestion("Remove " + name + "?",
				"Do you really want to remove the site '" + name + "' from the list?\n"
				+ "URL: " + getUpdateSite(row).url))
			return;
		final List<FileObject> list = new ArrayList<FileObject>();
		int count = 0;
		for (final FileObject file : files.forUpdateSite(name))
			switch (file.getStatus()) {
				case NEW:
				case NOT_INSTALLED:
				case OBSOLETE_UNINSTALLED:
					count--;
					break;
				default:
					count++;
					list.add(file);
			}
		if (count > 0) info("" +
			count +
			" files are installed from the site '" +
			name +
			"'\n" +
			"These files will not be deleted automatically.\n" +
			"Note: even if marked as 'Local-only', they might be available from other sites.");
		for (final FileObject file : list) {
			file.updateSite = null;
			file.setStatus(FileObject.Status.LOCAL_ONLY);
		}
		files.removeUpdateSite(name);
		names.remove(row);
		tableModel.rowChanged(row);
		updaterFrame.updateFilesTable();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Object source = e.getSource();
		if (source == add) add();
		else if (source == remove) delete(table.getSelectedRow());
		else if (source == close) {
			dispose();
		}
	}

	protected class DataModel extends AbstractTableModel {

		protected int tableWidth;
		protected int[] widths = { 100, 300, 150, 150 };
		protected String[] headers = { "Name", "URL", "Host",
			"Directory on Host" };

		public void setColumnWidths() {
			final TableColumnModel columnModel = table.getColumnModel();
			for (int i = 0; i < tableModel.widths.length && i < getColumnCount(); i++)
			{
				final TableColumn column = columnModel.getColumn(i);
				column.setPreferredWidth(tableModel.widths[i]);
				column.setMinWidth(tableModel.widths[i]);
				tableWidth += tableModel.widths[i];
			}
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(final int column) {
			return headers[column];
		}

		@Override
		public int getRowCount() {
			return names.size() + (newUpdateSite == null ? 0 : 1);
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			if (col == 0) return getUpdateSiteName(row);
			final UpdateSite site = getUpdateSite(row);
			if (col == 1) return site.url;
			if (col == 2) return site.sshHost;
			if (col == 3) return site.uploadDirectory;
			return null;
		}

		public void rowChanged(final int row) {
			rowsChanged(row, row + 1);
		}

		public void rowsChanged() {
			rowsChanged(0, names.size());
		}

		public void rowsChanged(final int firstRow, final int lastRow) {
			// fireTableChanged(new TableModelEvent(this, firstRow, lastRow));
			fireTableChanged(new TableModelEvent(this));
		}
	}

	protected boolean validURL(String url) {
		if (!url.endsWith("/"))
			url += "/";
		try {
			return files.util.getLastModified(new URL(url
					+ Util.XML_COMPRESSED)) != -1;
		} catch (MalformedURLException e) {
			updaterFrame.log.error(e);
			return false;
		}
	}

	protected boolean readFromSite(final int row) {
		try {
			final String updateSite = names.get(row);
			files.reReadUpdateSite(updateSite, updaterFrame.getProgress(null));
			markForUpdate(updateSite, false);
		} catch (final Exception e) {
			error("Not a valid URL: " + getUpdateSite(row).url);
			return false;
		}
		return true;
	}

	private void markForUpdate(final String updateSite, final boolean evenForcedUpdates) {
		for (final FileObject file : files.forUpdateSite(updateSite)) {
			if (file.isUpdateable(evenForcedUpdates) && file.isUpdateablePlatform(files)) {
				file.setFirstValidAction(files, Action.UPDATE,
					Action.UNINSTALL, Action.INSTALL);
			}
		}
	}

	protected boolean initializeUpdateSite(final String siteName,
			String url, final String host, String uploadDirectory) {
		if (!url.endsWith("/"))
			url += "/";
		if (!uploadDirectory.endsWith("/"))
			uploadDirectory += "/";
		boolean result;
		try {
			result = updaterFrame.initializeUpdateSite(url, host,
					uploadDirectory) && validURL(url);
		} catch (final InstantiationException e) {
			updaterFrame.log.error(e);
			result = false;
		}
		if (result)
			info("Initialized update site '" + siteName + "'");
		else
			error("Could not initialize update site '" + siteName + "'");
		return result;
	}

	@Override
	public void dispose() {
		super.dispose();
		updaterFrame.updateFilesTable();
		updaterFrame.enableUploadOrNot();
		updaterFrame.addCustomViewOptions();
	}

	public void info(final String message) {
		SwingTools.showMessageBox(this, message, JOptionPane.INFORMATION_MESSAGE);
	}

	public void error(final String message) {
		SwingTools.showMessageBox(this, message, JOptionPane.ERROR_MESSAGE);
	}

	public boolean showYesNoQuestion(final String title, final String message) {
		return SwingTools.showYesNoQuestion(this, title, message);
	}

	public static void escapeCancels(final JDialog dialog) {
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
			KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
		dialog.getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			}
		});
	}

	private static String stripWikiMarkup(final String string) {
		return string.replaceAll("'''", "").replaceAll("\\[http://[^ ]*([^\\]]*)\\]", "\\1");
	}

	private static class UpdateSiteMetadata {
		public String name, url, description, maintainer;
	}

	private static List<UpdateSiteMetadata> getAvailableSites() throws ActionException, ProcessException, MalformedURLException {
		final MediaWikiBot b = new MediaWikiBot("http://fiji.sc/");
		final Article article = b.readContent("List of update sites");
		final String text = article.getText();

		final int start = text.indexOf("\n{| class=\"wikitable\"\n");
		final int end = text.indexOf("\n|}\n", start);
		if (start < 0 || end < 0) {
			throw new ProcessException("Could not find table");
		}
		final String[] table = text.substring(start + 1, end).split("\n\\|-");

		final List<UpdateSiteMetadata> result = new ArrayList<UpdateSiteMetadata>();
		for (final String row : table) {
			if (row.matches("(?s)(\\{\\||[\\|!](style=\"vertical-align|colspan=\"4\")).*")) continue;
			final String[] columns = row.split("\n[\\|!]");
			if (columns.length == 5 && !columns[1].endsWith("|'''Name'''")) {
				final UpdateSiteMetadata metadata = new UpdateSiteMetadata();
				metadata.name = stripWikiMarkup(columns[1]);
				metadata.url = columns[2];
				metadata.description = columns[3];
				metadata.maintainer = columns[4];
				result.add(metadata);
			}
		}
		if (result.size() < 2) throw new ProcessException("Invalid page: " + article);
		final UpdateSiteMetadata ij = result.get(0);
		if (!ij.name.equals("ImageJ") || !ij.url.equals("http://update.imagej.net/")) {
			throw new ProcessException("Invalid page: " + article);
		}
		final UpdateSiteMetadata fiji = result.get(1);
		if (!fiji.name.equals("Fiji") || !fiji.url.equals("http://fiji.sc/update/")) {
			throw new ProcessException("Invalid page: " + article);
		}
		return result;
	}

}
