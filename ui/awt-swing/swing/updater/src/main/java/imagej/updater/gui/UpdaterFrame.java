//
// UpdaterFrame.java
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

import imagej.updater.core.Checksummer;
import imagej.updater.core.Installer;
import imagej.updater.core.PluginCollection;
import imagej.updater.core.PluginCollection.DependencyMap;
import imagej.updater.core.PluginObject;
import imagej.updater.core.PluginObject.Action;
import imagej.updater.core.PluginObject.Status;
import imagej.updater.core.PluginUploader;
import imagej.updater.util.Canceled;
import imagej.updater.util.Progress;
import imagej.updater.util.StderrProgress;
import imagej.updater.util.UserInterface;
import imagej.updater.util.Util;
import imagej.util.Log;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

@SuppressWarnings("serial")
public class UpdaterFrame extends JFrame implements TableModelListener,
	ListSelectionListener
{

	protected PluginCollection plugins;

	protected JTextField txtSearch;
	protected ViewOptions viewOptions;
	protected PluginTable table;
	protected JLabel lblPluginSummary;
	protected PluginDetails pluginDetails;
	protected JButton apply, cancel, easy, updateSites;
	protected boolean easyMode;

	// For developers
	protected JButton upload;
	boolean canUpload;
	protected boolean hidden;

	public UpdaterFrame(final PluginCollection plugins) {
		this(plugins, false);
	}

	public UpdaterFrame(final PluginCollection plugins, final boolean hidden) {
		super("ImageJ Updater");
		setPreferredSize(new Dimension(780, 560));

		this.plugins = plugins;
		this.hidden = hidden;

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				quit();
			}
		});

		// ======== Start: LEFT PANEL ========
		final JPanel leftPanel = new JPanel();
		final GridBagLayout gb = new GridBagLayout();
		leftPanel.setLayout(gb);
		final GridBagConstraints c = new GridBagConstraints(0, 0, // x, y
			9, 1, // rows, cols
			0, 0, // weightx, weighty
			GridBagConstraints.NORTHWEST, // anchor
			GridBagConstraints.HORIZONTAL, // fill
			new Insets(0, 0, 0, 0), 0, 0); // ipadx, ipady

		txtSearch = new JTextField();
		txtSearch.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(final DocumentEvent e) {
				updatePluginsTable();
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				updatePluginsTable();
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				updatePluginsTable();
			}
		});
		final JPanel searchPanel =
			SwingTools.labelComponentRigid("Search:", txtSearch);
		gb.setConstraints(searchPanel, c);
		leftPanel.add(searchPanel);

		Component box = Box.createRigidArea(new Dimension(0, 10));
		c.gridy = 1;
		gb.setConstraints(box, c);
		leftPanel.add(box);

		viewOptions = new ViewOptions();
		viewOptions.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				updatePluginsTable();
			}
		});

		final JPanel viewOptionsPanel =
			SwingTools.labelComponentRigid("View Options:", viewOptions);
		c.gridy = 2;
		gb.setConstraints(viewOptionsPanel, c);
		leftPanel.add(viewOptionsPanel);

		box = Box.createRigidArea(new Dimension(0, 10));
		c.gridy = 3;
		gb.setConstraints(box, c);
		leftPanel.add(box);

		// Create labels to annotate table
		final JPanel chooseLabel =
			SwingTools.label("Please choose what you want to install/uninstall:",
				null);
		c.gridy = 4;
		gb.setConstraints(chooseLabel, c);
		leftPanel.add(chooseLabel);

		box = Box.createRigidArea(new Dimension(0, 5));
		c.gridy = 5;
		gb.setConstraints(box, c);
		leftPanel.add(box);

		// Label text for plugin summaries
		lblPluginSummary = new JLabel();
		final JPanel summaryPanel = SwingTools.horizontalPanel();
		summaryPanel.add(lblPluginSummary);
		summaryPanel.add(Box.createHorizontalGlue());

		// Create the plugin table and set up its scrollpane
		table = new PluginTable(this);
		table.getSelectionModel().addListSelectionListener(this);
		final JScrollPane pluginListScrollpane = new JScrollPane(table);
		pluginListScrollpane.getViewport().setBackground(table.getBackground());

		c.gridy = 6;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		gb.setConstraints(pluginListScrollpane, c);
		leftPanel.add(pluginListScrollpane);

		box = Box.createRigidArea(new Dimension(0, 5));
		c.gridy = 7;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		gb.setConstraints(box, c);
		leftPanel.add(box);

		// ======== End: LEFT PANEL ========

		// ======== Start: RIGHT PANEL ========
		final JPanel rightPanel = SwingTools.verticalPanel();

		rightPanel.add(Box.createVerticalGlue());

		pluginDetails = new PluginDetails(this);
		SwingTools.tab(pluginDetails, "Details", "Individual Plugin information",
			350, 315, rightPanel);
		// TODO: put this into SwingTools, too
		rightPanel.add(Box.createRigidArea(new Dimension(0, 25)));
		// ======== End: RIGHT PANEL ========

		// ======== Start: TOP PANEL (LEFT + RIGHT) ========
		final JPanel topPanel = SwingTools.horizontalPanel();
		topPanel.add(leftPanel);
		topPanel.add(Box.createRigidArea(new Dimension(15, 0)));
		topPanel.add(rightPanel);
		topPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 5, 15));
		// ======== End: TOP PANEL (LEFT + RIGHT) ========

		// ======== Start: BOTTOM PANEL ========
		final JPanel bottomPanel2 = SwingTools.horizontalPanel();
		final JPanel bottomPanel = SwingTools.horizontalPanel();
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
		bottomPanel.add(new PluginAction("Keep as-is", null));
		bottomPanel.add(Box.createRigidArea(new Dimension(15, 0)));
		bottomPanel.add(new PluginAction("Install", Action.INSTALL, "Update",
			Action.UPDATE));
		bottomPanel.add(Box.createRigidArea(new Dimension(15, 0)));
		bottomPanel.add(new PluginAction("Uninstall", Action.UNINSTALL));

		bottomPanel.add(Box.createHorizontalGlue());

		// Button to start actions
		apply =
			SwingTools.button("Apply changes",
				"Start installing/uninstalling plugins", new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent e) {
						applyChanges();
					}
				}, bottomPanel);
		apply.setEnabled(false);

		// Manage update sites
		updateSites =
			SwingTools.button("Manage update sites",
				"Manage multiple update sites for updating and uploading",
				new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent e) {
						new SitesDialog(UpdaterFrame.this, UpdaterFrame.this.plugins,
							UpdaterFrame.this.plugins.hasUploadableSites()).setVisible(true);
					}
				}, bottomPanel2);

		// includes button to upload to server if is a Developer using
		bottomPanel2.add(Box.createRigidArea(new Dimension(15, 0)));
		upload =
			SwingTools.button("Upload to server",
				"Upload selected plugins to server", new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent e) {
						new Thread() {

							@Override
							public void run() {
								try {
									upload();
								}
								catch (final InstantiationException e) {
									Log.error(e);
									error("Could not upload (possibly unknown protocol)");
								}
							}
						}.start();
					}
				}, bottomPanel2);
		upload.setEnabled(false);
		upload.setVisible(plugins.hasUploadableSites());

		if (Util.isDeveloper) try {
			final IJ1Plugin pluginChanges =
				IJ1Plugin.discover("fiji.scripting.ShowPluginChanges");
			if (pluginChanges != null &&
				new File(System.getProperty("ij.dir"), ".git").isDirectory())
			{
				bottomPanel2.add(Box.createRigidArea(new Dimension(15, 0)));
				@SuppressWarnings("unused")
				final JButton showChanges =
					SwingTools.button("Show changes",
						"Show the changes in Git since the last upload",
						new ActionListener() {

							@Override
							public void actionPerformed(final ActionEvent e) {
								new Thread() {

									@Override
									public void run() {
										for (final PluginObject plugin : table.getSelectedPlugins())
											pluginChanges.run(plugin.filename);
									}
								}.start();
							}
						}, bottomPanel2);
			}
			final IJ1Plugin rebuild =
				IJ1Plugin.discover("fiji.scripting.RunFijiBuild");
			if (rebuild != null &&
				new File(System.getProperty("ij.dir"), ".git").isDirectory())
			{
				bottomPanel2.add(Box.createRigidArea(new Dimension(15, 0)));
				@SuppressWarnings("unused")
				final JButton rebuildButton =
					SwingTools.button("Rebuild", "Rebuild using Fiji Build",
						new ActionListener() {

							@Override
							public void actionPerformed(final ActionEvent e) {
								new Thread() {

									@Override
									public void run() {
										String list = "";
										final List<String> files = new ArrayList<String>();
										for (final PluginObject plugin : table.getSelectedPlugins())
										{
											list +=
												("".equals(list) ? "" : " ") + plugin.filename +
													"-rebuild";
											files.add(plugin.filename);
										}
										if (!"".equals(list)) rebuild.run(list);
										final Checksummer checksummer =
											new Checksummer(plugins,
												getProgress("Checksumming rebuilt plugins"));
										checksummer.updateFromLocal(files);
										pluginsChanged();
										updatePluginsTable();
									}
								}.start();
							}
						}, bottomPanel2);
			}
		}
		catch (final Exception e) { /* ignore */}

		bottomPanel2.add(Box.createHorizontalGlue());

		bottomPanel.add(Box.createRigidArea(new Dimension(15, 0)));
		easy =
			SwingTools.button("Toggle easy mode",
				"Toggle between easy and verbose mode", new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent e) {
						toggleEasyMode();
					}
				}, bottomPanel);

		bottomPanel.add(Box.createRigidArea(new Dimension(15, 0)));
		cancel =
			SwingTools.button("Close", "Exit Plugin Manager", new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					quit();
				}
			}, bottomPanel);
		// ======== End: BOTTOM PANEL ========

		getContentPane().setLayout(
			new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(topPanel);
		getContentPane().add(summaryPanel);
		getContentPane().add(bottomPanel);
		getContentPane().add(bottomPanel2);

		getRootPane().setDefaultButton(apply);

		table.getModel().addTableModelListener(this);

		pack();

		SwingTools.addAccelerator(cancel, (JComponent) getContentPane(), cancel
			.getActionListeners()[0], KeyEvent.VK_ESCAPE, 0);

		addCustomViewOptions();
	}

	protected static class IJ1Plugin {

		protected Object plugin;
		protected Method run;

		protected void run(final String arg) {
			try {
				run.invoke(plugin, arg);
			}
			catch (final Exception e) {
				UserInterface.get().handleException(e);
			}
		}

		protected static IJ1Plugin discover(final String className) {
			try {
				final IJ1Plugin instance = new IJ1Plugin();
				final Class<?> clazz =
					IJ1Plugin.class.getClassLoader().loadClass(className);
				instance.plugin = clazz.newInstance();
				instance.run =
					instance.plugin.getClass().getMethod("run", String.class);
				return instance;
			}
			catch (final Exception e) {
				return null;
			}
		}
	}

	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible && !hidden);
		if (visible) {
			UserInterface.get().addWindow(this);
			apply.requestFocusInWindow();
		}
	}

	@Override
	public void dispose() {
		UserInterface.get().removeWindow(this);
		super.dispose();
	}

	public Progress getProgress(final String title) {
		if (hidden) return new StderrProgress();
		return new ProgressDialog(this, title);
	}

	@Override
	public void valueChanged(final ListSelectionEvent event) {
		pluginsChanged();
	}

	List<PluginAction> pluginActions = new ArrayList<PluginAction>();

	class PluginAction extends JButton implements ActionListener {

		String label, otherLabel;
		Action action, otherAction;

		PluginAction(final String label, final Action action) {
			this(label, action, null, null);
		}

		PluginAction(final String label, final Action action,
			final String otherLabel, final Action otherAction)
		{
			super(label);
			this.label = label;
			this.action = action;
			this.otherLabel = otherLabel;
			this.otherAction = otherAction;
			addActionListener(this);
			pluginActions.add(this);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (table.isEditing()) table.editingCanceled(null);
			for (final PluginObject plugin : table.getSelectedPlugins()) {
				if (action == null) plugin.setNoAction();
				else if (!setAction(plugin)) continue;
				table.firePluginChanged(plugin);
			}
			pluginsChanged();
		}

		protected boolean setAction(final PluginObject plugin) {
			return plugin.setFirstValidAction(plugins, new Action[] { action,
				otherAction });
		}

		public void enableIfValid() {
			boolean enable = false, enableOther = false;

			for (final PluginObject plugin : table.getSelectedPlugins()) {
				final Status status = plugin.getStatus();
				if (action == null) enable = true;
				else if (status.isValid(action)) enable = true;
				else if (status.isValid(otherAction)) enableOther = true;
				if (enable && enableOther) break;
			}
			setText(!enableOther ? label : (enable ? label + "/" : "") + otherLabel);
			setEnabled(enable || enableOther);
		}
	}

	public void addCustomViewOptions() {
		viewOptions.clearCustomOptions();

		final Collection<String> names = plugins.getUpdateSiteNames();
		if (names.size() > 1) for (final String name : names)
			viewOptions.addCustomOption("View files of the '" + name + "' site",
				plugins.forUpdateSite(name));
	}

	public void setViewOption(final ViewOptions.Option option) {
		viewOptions.setSelectedItem(option);
		updatePluginsTable();
	}

	public void updatePluginsTable() {
		Iterable<PluginObject> view = viewOptions.getView(table);
		final Set<PluginObject> selected = new HashSet<PluginObject>();
		for (final PluginObject plugin : table.getSelectedPlugins())
			selected.add(plugin);
		table.clearSelection();

		final String search = txtSearch.getText().trim();
		if (!search.equals("")) view = PluginCollection.filter(search, view);

		// Directly update the table for display
		table.setPlugins(view);
		for (int i = 0; i < table.getRowCount(); i++)
			if (selected.contains(table.getPlugin(i))) table.addRowSelectionInterval(
				i, i);
	}

	public void applyChanges() {
		final ResolveDependencies resolver = new ResolveDependencies(this, plugins);
		if (!resolver.resolve()) return;
		new Thread() {

			@Override
			public void run() {
				install();
			}
		}.start();
	}

	private void quit() {
		if (plugins.hasChanges()) {
			if (!SwingTools.showQuestion(hidden, this, "Quit?",
				"You have specified changes. Are you sure you want to quit?")) return;
		}
		else try {
			plugins.write();
		}
		catch (final Exception e) {
			error("There was an error writing the local metadata cache: " + e);
		}
		dispose();
	}

	void setEasyMode(final Container container) {
		for (final Component child : container.getComponents()) {
			if ((child instanceof Container) &&
				child != table.getParent().getParent()) setEasyMode((Container) child);
			if (child == upload && !easyMode && !plugins.hasUploadableSites()) child
				.setVisible(false);
			else child.setVisible(!easyMode);
		}
	}

	public void setEasyMode(final boolean easyMode) {
		this.easyMode = easyMode;
		setEasyMode(getContentPane());
		final Component[] exempt = { table, easy, apply, cancel };
		for (Component child : exempt)
			for (; child != getContentPane(); child = child.getParent())
				child.setVisible(true);
		easy.setText(easyMode ? "Advanced mode" : "Easy mode");
		if (isVisible()) repaint();
	}

	public void toggleEasyMode() {
		setEasyMode(!easyMode);
	}

	public void install() {
		final Installer installer =
			new Installer(plugins, getProgress("Installing..."));
		try {
			final PluginCollection uninstalled =
				PluginCollection.clone(plugins.toUninstall());
			installer.start();
			for (final PluginObject plugin : uninstalled)
				if (!plugin.isFiji()) plugins.remove(plugin);
				else plugin.setStatus(plugin.isObsolete() ? Status.OBSOLETE_UNINSTALLED
					: Status.NOT_INSTALLED);
			updatePluginsTable();
			pluginsChanged();
			plugins.write();
			info("Updated successfully.  Please restart ImageJ!");
			dispose();
		}
		catch (final Canceled e) {
			// TODO: remove "update/" directory
			error("Canceled");
			installer.done();
		}
		catch (final Exception e) {
			e.printStackTrace();
			// TODO: remove "update/" directory
			error("Installer failed: " + e);
			installer.done();
		}
	}

	public void updateTheUpdater() {
		final PluginCollection.Filter filter = new PluginCollection.Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				if (plugin.filename.equals("plugins/Fiji_Updater.jar")) {
					plugin.setAction(plugins, Action.UPDATE);
					return true;
				}
				return false;
			}
		};
		final PluginCollection justTheUpdater =
			PluginCollection.clone(plugins.filter(filter));
		final Installer installer =
			new Installer(justTheUpdater, getProgress("Installing the updater..."));
		try {
			installer.start();
		}
		catch (final Canceled e) {
			// TODO: remove "update/" directory
			error("Canceled");
			installer.done();
		}
		catch (final IOException e) {
			// TODO: remove "update/" directory
			error("Installer failed: " + e);
			installer.done();
		}
	}

	private Thread pluginsChangedWorker;

	public synchronized void pluginsChanged() {
		if (pluginsChangedWorker != null) return;

		pluginsChangedWorker = new Thread() {

			@Override
			public void run() {
				pluginsChangedWorker();
				synchronized (UpdaterFrame.this) {
					pluginsChangedWorker = null;
				}
			}
		};
		SwingUtilities.invokeLater(pluginsChangedWorker);
	}

	private void pluginsChangedWorker() {
		// TODO: once this is editable, make sure changes are committed
		pluginDetails.reset();
		for (final PluginObject plugin : table.getSelectedPlugins())
			pluginDetails.showPluginDetails(plugin);
		if (pluginDetails.getDocument().getLength() > 0 &&
			table.areAllSelectedPluginsUploadable()) pluginDetails
			.setEditableForDevelopers();

		for (final PluginAction button : pluginActions)
			button.enableIfValid();

		apply.setEnabled(plugins.hasChanges());
		cancel.setText(plugins.hasChanges() ? "Cancel" : "Close");

		if (plugins.hasUploadableSites()) enableUploadOrNot();

		int install = 0, uninstall = 0, upload = 0;
		long bytesToDownload = 0, bytesToUpload = 0;

		for (final PluginObject plugin : plugins)
			switch (plugin.getAction()) {
				case INSTALL:
				case UPDATE:
					install++;
					bytesToDownload += plugin.filesize;
					break;
				case UNINSTALL:
					uninstall++;
					break;
				case UPLOAD:
					upload++;
					bytesToUpload += plugin.filesize;
					break;
			}
		int implicated = 0;
		final DependencyMap map = plugins.getDependencies(true);
		for (final PluginObject plugin : map.keySet()) {
			implicated++;
			bytesToUpload += plugin.filesize;
		}
		String text = "";
		if (install > 0) text +=
			" install/update: " + install + (implicated > 0 ? "+" + implicated : "") +
				" (" + sizeToString(bytesToDownload) + ")";
		if (uninstall > 0) text += " uninstall: " + uninstall;
		if (plugins.hasUploadableSites() && upload > 0) text +=
			" upload: " + upload + " (" + sizeToString(bytesToUpload) + ")";
		lblPluginSummary.setText(text);

	}

	protected final static String[] units = { "B", "kB", "MB", "GB", "TB" };

	public static String sizeToString(long size) {
		int i;
		for (i = 1; i < units.length && size >= 1l << (10 * i); i++); // do nothing
		if (--i == 0) return "" + size + units[i];
		// round
		size *= 100;
		size >>= (10 * i);
		size += 5;
		size /= 10;
		return "" + (size / 10) + "." + (size % 10) + units[i];
	}

	@Override
	public void tableChanged(final TableModelEvent e) {
		pluginsChanged();
	}

	// checkWritable() is guaranteed to be called after Checksummer ran
	public void checkWritable() {
		String list = null;
		for (final PluginObject plugin : plugins) {
			final File file = new File(Util.prefix(plugin.getFilename()));
			if (!file.exists() || file.canWrite()) continue;
			if (list == null) list = plugin.getFilename();
			else list += ", " + plugin.getFilename();
		}
		if (list != null) UserInterface.get().info(
			"WARNING: The following plugin files " + "are set to read-only: '" +
				list + "'", "Read-only Plugins");
	}

	void markUploadable() {
		canUpload = true;
		enableUploadOrNot();
	}

	void enableUploadOrNot() {
		upload.setVisible(!easyMode && plugins.hasUploadableSites());
		upload.setEnabled(canUpload || plugins.hasUploadOrRemove());
	}

	protected void upload() throws InstantiationException {
		final ResolveDependencies resolver =
			new ResolveDependencies(this, plugins, true);
		if (!resolver.resolve()) return;

		final String errors = plugins.checkConsistency();
		if (errors != null) {
			error(errors);
			return;
		}

		final List<String> possibleSites =
			new ArrayList<String>(plugins.getSiteNamesToUpload());
		if (possibleSites.size() == 0) {
			error("Huh? No upload site?");
			return;
		}
		String updateSiteName;
		if (possibleSites.size() == 1) updateSiteName = possibleSites.get(0);
		else {
			updateSiteName =
				SwingTools.getChoice(hidden, this, possibleSites,
					"Which site do you want to upload to?", "Update site");
			if (updateSiteName == null) return;
		}
		final PluginUploader uploader = new PluginUploader(plugins, updateSiteName);

		Progress progress = null;
		try {
			if (!uploader.login()) return;
			progress = getProgress("Uploading...");
			uploader.upload(progress);
			for (final PluginObject plugin : plugins.toUploadOrRemove())
				if (plugin.getAction() == Action.UPLOAD) {
					plugin.markUploaded();
					plugin.setStatus(Status.INSTALLED);
				}
				else {
					plugin.markRemoved();
					plugin.setStatus(Status.OBSOLETE_UNINSTALLED);
				}
			updatePluginsTable();
			canUpload = false;
			plugins.write();
			info("Uploaded successfully.");
			enableUploadOrNot();
			dispose();
		}
		catch (final Canceled e) {
			// TODO: teach uploader to remove the lock file
			error("Canceled");
			if (progress != null) progress.done();
		}
		catch (final Throwable e) {
			UserInterface.get().handleException(e);
			error("Upload failed: " + e);
			if (progress != null) progress.done();
		}
	}

	protected boolean initializeUpdateSite(final String url,
		final String sshHost, final String uploadDirectory)
		throws InstantiationException
	{
		final String updateSiteName = "Dummy";
		final PluginCollection plugins = new PluginCollection();
		plugins.addUpdateSite(updateSiteName, url, sshHost, uploadDirectory, Long
			.parseLong(Util.timestamp(-1)));
		final PluginUploader uploader = new PluginUploader(plugins, updateSiteName);
		Progress progress = null;
		try {
			if (!uploader.login()) return false;
			progress = getProgress("Initializing Update Site...");
			uploader.upload(progress);
			// JSch needs some time to finalize the SSH connection
			try {
				Thread.sleep(1000);
			}
			catch (final InterruptedException e) { /* ignore */}
			return true;
		}
		catch (final Canceled e) {
			if (progress != null) progress.done();
		}
		catch (final Throwable e) {
			UserInterface.get().handleException(e);
			if (progress != null) progress.done();
		}
		return false;
	}

	public void error(final String message) {
		SwingTools.showMessageBox(hidden, this, message, JOptionPane.ERROR_MESSAGE);
	}

	public void warn(final String message) {
		SwingTools.showMessageBox(hidden, this, message,
			JOptionPane.WARNING_MESSAGE);
	}

	public void info(final String message) {
		SwingTools.showMessageBox(hidden, this, message,
			JOptionPane.INFORMATION_MESSAGE);
	}
}
