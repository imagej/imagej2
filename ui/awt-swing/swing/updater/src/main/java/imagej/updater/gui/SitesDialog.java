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

import imagej.updater.core.Checksummer;
import imagej.updater.core.FileObject;
import imagej.updater.core.FilesCollection;
import imagej.updater.core.FilesCollection.UpdateSite;
import imagej.updater.core.XMLFileReader;
import imagej.updater.util.Util;
import imagej.util.Log;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
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
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

@SuppressWarnings("serial")
public class SitesDialog extends JDialog implements ActionListener,
	ItemListener
{

	protected UpdaterFrame updaterFrame;
	protected FilesCollection files;
	protected List<String> names;

	protected DataModel tableModel;
	protected JTable table;
	protected JButton add, edit, remove, close;
	protected JCheckBox forUpload;

	public SitesDialog(final UpdaterFrame owner, final FilesCollection files,
		final boolean forUpload)
	{
		super(owner, "Manage update sites");
		updaterFrame = owner;
		this.files = files;
		names = new ArrayList<String>(files.getUpdateSiteNames());
		names.set(0, FilesCollection.DEFAULT_UPDATE_SITE);

		final Container contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		this.forUpload = new JCheckBox("For Uploading", forUpload);
		this.forUpload.addItemListener(this);

		tableModel = new DataModel();
		table = new JTable(tableModel) {

			@Override
			public void valueChanged(final ListSelectionEvent e) {
				super.valueChanged(e);
				edit.setEnabled(getSelectedRow() >= 0);
				remove.setEnabled(getSelectedRow() > 0);
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
		buttons.add(this.forUpload);
		add = SwingTools.button("Add", "Add", this, buttons);
		edit = SwingTools.button("Edit", "Edit", this, buttons);
		edit.setEnabled(false);
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

	protected UpdateSite getUpdateSite(final String name) {
		return files.getUpdateSite(name);
	}

	protected void add() {
		final SiteDialog dialog = new SiteDialog();
		dialog.setVisible(true);
	}

	protected void edit(final int row) {
		final String name = names.get(row);
		final UpdateSite updateSite = getUpdateSite(name);
		final SiteDialog dialog =
			new SiteDialog(name, updateSite.url, updateSite.sshHost,
				updateSite.uploadDirectory, row);
		dialog.setVisible(true);
	}

	protected void delete(final int row) {
		final String name = names.get(row);
		final List<FileObject> list = new ArrayList<FileObject>();
		int count = 0;
		for (final FileObject file : files.forUpdateSite(name))
			switch (file.getStatus()) {
				case NEW:
				case NOT_INSTALLED:
				case OBSOLETE_UNINSTALLED:
					count--;
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
		else if (source == edit) edit(table.getSelectedRow());
		else if (source == remove) delete(table.getSelectedRow());
		else if (source == close) {
			dispose();
		}
	}

	@Override
	public void itemStateChanged(final ItemEvent e) {
		tableModel.fireTableStructureChanged();
		tableModel.setColumnWidths();
	}

	protected class DataModel extends AbstractTableModel {

		protected int tableWidth;
		protected int[] widths = { 100, 300, 150, 150 };
		protected String[] headers = { "Name", "URL", "SSH Host",
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
			return forUpload.isSelected() ? 4 : 2;
		}

		@Override
		public String getColumnName(final int column) {
			return headers[column];
		}

		@Override
		public int getRowCount() {
			return names.size();
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			final String name = names.get(row);
			if (col == 0) return name;
			final UpdateSite site = getUpdateSite(name);
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

	protected class SiteDialog extends JDialog implements ActionListener {

		protected int row;
		protected JTextField name, url, sshHost, uploadDirectory;
		protected JButton ok, cancel;

		public SiteDialog() {
			this("", "", "", "", -1);
		}

		public SiteDialog(final String name, final String url,
			final String sshHost, final String uploadDirectory, final int row)
		{
			super(SitesDialog.this, "Add update site");
			this.row = row;

			setPreferredSize(new Dimension(400, 150));
			final Container contentPane = getContentPane();
			contentPane.setLayout(new GridBagLayout());
			final GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = c.gridheight = 1;
			c.weightx = c.weighty = 0;
			c.gridx = c.gridy = 0;

			this.name = new JTextField(name, tableModel.widths[0]);
			this.url = new JTextField(url, tableModel.widths[1]);
			this.sshHost = new JTextField(sshHost, tableModel.widths[2]);
			this.uploadDirectory =
				new JTextField(uploadDirectory, tableModel.widths[3]);
			contentPane.add(new JLabel("Name:"), c);
			c.weightx = 1;
			c.gridx++;
			contentPane.add(this.name, c);
			c.weightx = 0;
			c.gridx = 0;
			c.gridy++;
			contentPane.add(new JLabel("URL:"), c);
			c.weightx = 1;
			c.gridx++;
			contentPane.add(this.url, c);
			if (forUpload.isSelected()) {
				c.weightx = 0;
				c.gridx = 0;
				c.gridy++;
				contentPane.add(new JLabel("SSH host:"), c);
				c.weightx = 1;
				c.gridx++;
				contentPane.add(this.sshHost, c);
				c.weightx = 0;
				c.gridx = 0;
				c.gridy++;
				contentPane.add(new JLabel("Upload directory:"), c);
				c.weightx = 1;
				c.gridx++;
				contentPane.add(this.uploadDirectory, c);
			}
			final JPanel buttons = new JPanel();
			ok = new JButton("OK");
			ok.addActionListener(this);
			buttons.add(ok);
			cancel = new JButton("Cancel");
			cancel.addActionListener(this);
			buttons.add(cancel);
			c.weightx = 0;
			c.gridx = 0;
			c.gridwidth = 2;
			c.gridy++;
			contentPane.add(buttons, c);

			getRootPane().setDefaultButton(ok);
			pack();
			escapeCancels(this);
			setLocationRelativeTo(SitesDialog.this);
		}

		protected boolean validURL(String url) {
			if (!url.endsWith("/")) url += "/";
			return Util.getLastModified(url + Util.XML_COMPRESSED) != -1;
		}

		protected boolean readFromSite(final String name) {
			try {
				new XMLFileReader(files).read(name);
				final List<String> filesFromSite = new ArrayList<String>();
				for (final FileObject file : files.forUpdateSite(name))
					filesFromSite.add(file.filename);
				final Checksummer checksummer =
					new Checksummer(files, updaterFrame.getProgress("Czechsummer"));
				checksummer.updateFromLocal(filesFromSite);
			}
			catch (final Exception e) {
				error("Not a valid URL: " + url.getText());
				return false;
			}
			return true;
		}

		protected boolean initializeUpdateSite(final String siteName, String url,
			final String host, String uploadDirectory)
		{
			if (!url.endsWith("/")) url += "/";
			if (!uploadDirectory.endsWith("/")) uploadDirectory += "/";
			boolean result;
			try {
				result =
					updaterFrame.initializeUpdateSite(url, host, uploadDirectory) &&
						validURL(url);
			}
			catch (final InstantiationException e) {
				Log.error(e);
				result = false;
			}
			if (result) info("Initialized update site '" + siteName + "'");
			else error("Could not initialize update site '" + siteName + "'");
			return result;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final Object source = e.getSource();
			if (source == ok) {
				if (name.getText().equals("")) {
					error("Need a name");
					return;
				}
				if (!validURL(url.getText())) {
					try {
						new URL(url.getText());
					}
					catch (final MalformedURLException e2) {
						error("Not a valid URL: " + url.getText());
						return;
					}
					if (!uploadDirectory.getText().equals("")) {
						if (!showYesNoQuestion("Initialize upload site?",
							"It appears that the URL is not (yet) valid.\n" +
								"Do you want to upload an empty db.xml.gz to " +
								sshHost.getText() + ":" + uploadDirectory.getText() + "?")) return;
						String host = sshHost.getText();
						if (host.equals("")) host = null; // Try the file system
						if (!initializeUpdateSite(name.getText(), url.getText(), host,
							uploadDirectory.getText())) return;
					}
					else {
						error("URL does not refer to an update site: " + url.getText());
						return;
					}
				}
				if (row < 0) {
					if (names.contains(name.getText())) {
						error("Site '" + name.getText() + "' exists already!");
						return;
					}
					row = names.size();
					files.addUpdateSite(name.getText(), url.getText(), sshHost.getText(),
						uploadDirectory.getText(), 0l);
					names.add(name.getText());
				}
				else {
					final String originalName = names.get(row);
					final UpdateSite updateSite = getUpdateSite(originalName);
					final String name = this.name.getText();
					final boolean nameChanged = !name.equals(originalName);
					if (nameChanged) {
						if (names.contains(name)) {
							error("Site '" + name + "' exists already!");
							return;
						}
						files.renameUpdateSite(originalName, name);
						names.set(row, name);
					}
					updateSite.url = url.getText();
					updateSite.sshHost = sshHost.getText();
					updateSite.uploadDirectory = uploadDirectory.getText();
				}
				readFromSite(name.getText());
				tableModel.rowChanged(row);
				table.setRowSelectionInterval(row, row);
				updaterFrame.enableUploadOrNot();
			}
			dispose();
		}
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
}
