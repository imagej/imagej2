//
// ResolveDependencies.java
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

import imagej.updater.core.Dependency;
import imagej.updater.core.FileObject;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FilesCollection;
import imagej.updater.core.FilesCollection.DependencyMap;
import imagej.updater.util.Util;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

@SuppressWarnings("serial")
public class ResolveDependencies extends JDialog implements ActionListener {

	UpdaterFrame updaterFrame;
	JPanel rootPanel;
	public JTextPane panel; // this is public for debugging purposes
	SimpleAttributeSet bold, indented, italic, normal, red;
	JButton ok, cancel;

	FilesCollection plugins;
	DependencyMap toInstall, obsoleted;
	Collection<FileObject> automatic, ignore;
	int conflicts;
	boolean forUpload, wasCanceled;

	public ResolveDependencies(final UpdaterFrame owner,
		final FilesCollection plugins)
	{
		this(owner, plugins, false);
	}

	public ResolveDependencies(final UpdaterFrame owner,
		final FilesCollection plugins, final boolean forUpload)
	{
		super(owner, "Resolve dependencies");

		updaterFrame = owner;
		this.forUpload = forUpload;
		this.plugins = plugins;

		rootPanel = SwingTools.verticalPanel();
		setContentPane(rootPanel);

		panel = new JTextPane();
		panel.setEditable(false);

		bold = new SimpleAttributeSet();
		StyleConstants.setBold(bold, true);
		StyleConstants.setFontSize(bold, 16);
		indented = new SimpleAttributeSet();
		StyleConstants.setLeftIndent(indented, 40);
		italic = new SimpleAttributeSet();
		StyleConstants.setItalic(italic, true);
		normal = new SimpleAttributeSet();
		red = new SimpleAttributeSet();
		StyleConstants.setForeground(red, Color.RED);

		SwingTools.scrollPane(panel, 450, 350, rootPanel);

		final JPanel buttons = new JPanel();
		ok = SwingTools.button("OK", "OK", this, buttons);
		cancel = SwingTools.button("Cancel", "Cancel", this, buttons);
		rootPanel.add(buttons);

		// do not show, right now
		pack();
		setModal(true);
		setLocationRelativeTo(owner);

		final int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		SwingTools.addAccelerator(cancel, rootPanel, this, KeyEvent.VK_ESCAPE, 0);
		SwingTools.addAccelerator(cancel, rootPanel, this, KeyEvent.VK_W, ctrl);
		SwingTools.addAccelerator(ok, rootPanel, this, KeyEvent.VK_ENTER, 0);

		ignore = new HashSet<FileObject>();
	}

	@Override
	public void setVisible(final boolean visible) {
		if (updaterFrame == null || !updaterFrame.hidden) super.setVisible(visible);
	}

	@Override
	public void dispose() {
		if (updaterFrame != null && updaterFrame.hidden) synchronized (this) {
			notifyAll();
		}
		super.dispose();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == cancel) {
			wasCanceled = true;
			dispose();
		}
		else if (e.getSource() == ok) {
			if (!ok.isEnabled()) return;
			for (final FileObject plugin : automatic)
				plugin.setFirstValidAction(plugins, new Action[] { Action.INSTALL,
					Action.UPDATE, Action.UNINSTALL });
			dispose();
		}
	}

	void enableOKIfValid() {
		ok.setEnabled(conflicts == 0);
		if (ok.isEnabled()) ok.requestFocus();
	}

	public boolean resolve() {
		listIssues();

		if (panel.getDocument().getLength() > 0) setVisible(true);
		return !wasCanceled;
	}

	void listIssues() {
		conflicts = 0;
		panel.setText("");

		automatic = new ArrayList<FileObject>();
		if (forUpload) listUploadIssues();
		else listUpdateIssues();

		enableOKIfValid();
		if (isShowing()) {
			if (panel.getStyledDocument().getLength() == 0) addText(
				"No more issues to be resolved!", italic);
			panel.setCaretPosition(0);
			panel.repaint();
		}
	}

	void listUpdateIssues() {
		toInstall = plugins.getDependencies(false);
		obsoleted = plugins.getDependencies(true);

		for (final FileObject plugin : toInstall.keySet())
			if (obsoleted.get(plugin) != null) bothInstallAndUninstall(plugin);
			else if (!plugin.willBeUpToDate()) {
				if (plugin.isLocallyModified()) locallyModified(plugin);
				else automatic.add(plugin);
			}

		for (final FileObject plugin : obsoleted.keySet())
			if (toInstall.get(plugin) != null && // handled above
				!plugin.willNotBeInstalled()) needUninstall(plugin);

		if (automatic.size() > 0) {
			maybeAddSeparator();
			newText("These components will be updated/"
				+ "installed automatically: \n\n");
			addList(automatic);
		}
	}

	void bothInstallAndUninstall(final FileObject plugin) {
		maybeAddSeparator();
		final FilesCollection reasons =
			FilesCollection.clone(toInstall.get(plugin));
		reasons.addAll(obsoleted.get(plugin));
		newText("Conflict: ", red);
		addText(plugin.getFilename(), bold);
		addText(" is required by\n\n");
		addList(toInstall.get(plugin));
		addText("\nbut made obsolete by\n\n");
		addList(obsoleted.get(plugin));
		addText("\n    ");
		addIgnoreButton("Ignore this issue", plugin);
		addText("    ");
		addButton("Do not update " + reasons, reasons, null);
	}

	void needUninstall(final FileObject plugin) {
		maybeAddSeparator();
		final FilesCollection reasons = obsoleted.get(plugin);
		newText("Conflict: ", red);
		addText(plugin.getFilename(), bold);
		addText(" is locally modified but made obsolete by\n\n");
		addList(reasons);
		addText("\n    ");
		addButton("Uninstall " + plugin, plugin, Action.UNINSTALL);
		addText("    ");
		addButton("Do not update " + reasons, reasons, null);
	}

	void locallyModified(final FileObject plugin) {
		if (ignore.contains(plugin)) return;
		maybeAddSeparator();
		newText("Warning: ");
		addText(plugin.getFilename(), bold);
		addText(" is locally modified and the Updater cannot determine its "
			+ "status. A newer version might be required by\n\n");
		addList(toInstall.get(plugin));
		addText("\n    ");
		addIgnoreButton("Keep the local version", plugin);
		addText("    ");
		final boolean toInstall = plugin.getStatus().isValid(Action.INSTALL);
		addButton((toInstall ? "Install" : "Update") + " " + plugin, plugin,
			toInstall ? Action.INSTALL : Action.UPDATE);
	}

	void listUploadIssues() {
		toInstall = new FilesCollection.DependencyMap();
		for (final FileObject plugin : plugins.toUpload())
			for (final Dependency dependency : plugin.getDependencies()) {
				final FileObject dep = plugins.getPlugin(dependency.filename);
				if (dep == null || ignore.contains(dep)) continue;
				if (dep.isInstallable() ||
					(!dep.isFiji() && dep.getAction() != Action.UPLOAD) ||
					dep.isObsolete() ||
					(dep.getStatus().isValid(Action.UPLOAD) && dep.getAction() != Action.UPLOAD)) toInstall
					.add(dep, plugin);
			}
		for (final FileObject plugin : toInstall.keySet())
			needUpload(plugin);

		// Replace dependencies on to-be-removed plugins
		for (final FileObject plugin : plugins.fijiPlugins()) {
			if (plugin.getAction() == Action.REMOVE) continue;
			for (final Dependency dependency : plugin.getDependencies())
				if (plugins.getPlugin(dependency.filename) == null) dependencyNotUploaded(
					plugin, dependency.filename);
				else if (plugins.getPlugin(dependency.filename).getAction() == Action.REMOVE) dependencyRemoved(
					plugin, dependency.filename);
		}
	}

	void needUpload(final FileObject plugin) {
		final boolean notFiji = !plugin.isFiji();
		final boolean notInstalled = plugin.isInstallable();
		final boolean obsolete = plugin.isObsolete();
		final FilesCollection reasons = toInstall.get(plugin);
		maybeAddSeparator();
		newText("Warning: ", notFiji || obsolete ? red : normal);
		addText(plugin.getFilename(), bold);
		addText((notFiji ? "was not uploaded yet" : "is " +
			(notInstalled ? "not installed" : (obsolete ? "marked obsolete"
				: "locally modified"))) +
			" but a dependency of\n\n");
		addList(reasons);
		addText("\n    ");
		if (!notFiji && !obsolete) {
			addIgnoreButton("Do not upload " + plugin, plugin);
			addText("    ");
		}
		if (!notInstalled) {
			addButton("Upload " + plugin + (obsolete ? " again" : ""), plugin,
				Action.UPLOAD);
			addText("    ");
		}
		addButton("Break the dependency", new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				for (final FileObject other : reasons)
					other.removeDependency(plugin.getFilename());
				listIssues();
			}
		});
	}

	void dependencyNotUploaded(final FileObject plugin, final String dependency) {
		maybeAddSeparator();
		newText("Error: ", normal);
		addText(plugin.getFilename(), bold);
		addText(" depends on " + dependency + " which was not uploaded.\n\n");
		addDependencyButton("Break the dependency", plugin, dependency, null);
	}

	void dependencyRemoved(final FileObject plugin, final String dependency) {
		maybeAddSeparator();
		newText("Warning: ", normal);
		addText(plugin.getFilename(), bold);
		addText(" depends on " + dependency + " which is about to be removed.\n\n");
		addDependencyButton("Break the dependency", plugin, dependency, null);
		for (final FileObject toUpload : plugins.toUpload()) {
			if (plugin.hasDependency(toUpload.getFilename())) continue;
			addText("    ");
			addDependencyButton("Replace with dependency to " + toUpload, plugin,
				dependency, toUpload.getFilename());
			addText("    ");
			addDependencyButton("Replace all dependencies on " + dependency +
				" with dependencies to " + toUpload, null, dependency, toUpload
				.getFilename());
		}
	}

	void addIgnoreButton(final String label, final FileObject plugin) {
		addButton(label, new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				ignore.add(plugin);
				listIssues();
			}
		});
	}

	void addDependencyButton(final String label, final FileObject plugin,
		final String removeDependency, final String addDependency)
	{
		addButton(label, new ActionListener() {

			void replaceDependency(final FileObject plugin) {
				plugin.removeDependency(removeDependency);
				if (addDependency != null) plugin.addDependency(addDependency);
			}

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (plugin != null) replaceDependency(plugin);
				else for (final FileObject plugin : plugins)
					if (plugin.hasDependency(removeDependency)) replaceDependency(plugin);
				listIssues();
			}
		});
	}

	void addButton(final String label, final FileObject plugin,
		final Action action)
	{
		final Collection<FileObject> one = new ArrayList<FileObject>();
		one.add(plugin);
		addButton(label, one, action);
	}

	void addButton(final String label, final Collection<FileObject> plugins,
		final Action action)
	{
		addButton(label, new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				for (final FileObject plugin : plugins)
					if (action == null) plugin.setNoAction();
					else plugin.setAction(ResolveDependencies.this.plugins, action);
				listIssues();
			}
		});
	}

	void addButton(final String label, final ActionListener listener) {
		conflicts++;
		final JButton button = SwingTools.button(label, null, listener, null);
		selectEnd();
		panel.insertComponent(button);
	}

	void selectEnd() {
		final int end = panel.getStyledDocument().getLength();
		panel.select(end, end);
	}

	void newText(final String message) {
		newText(message, normal);
	}

	void newText(final String message, final SimpleAttributeSet style) {
		if (panel.getStyledDocument().getLength() > 0) addText("\n\n");
		addText(message, style);
	}

	void addText(final String message) {
		addText(message, normal);
	}

	void addText(final String message, final SimpleAttributeSet style) {
		final int end = panel.getStyledDocument().getLength();
		try {
			panel.getStyledDocument().insertString(end, message, style);
		}
		catch (final BadLocationException e) {
			e.printStackTrace();
		}
	}

	void addList(final Collection<FileObject> list) {
		addText(Util.join(", ", list) + "\n", bold);
		final int end = panel.getStyledDocument().getLength();
		panel.select(end - 1, end - 1);
		panel.setParagraphAttributes(indented, true);
	}

	protected void maybeAddSeparator() {
		if (panel.getText().equals("") && panel.getComponents().length == 0) return;
		addText("\n");
		selectEnd();
		panel.insertComponent(new JSeparator());
	}

	private static FileObject fakePlugin(final String label) {
		final Random random = new Random();
		String fakeChecksum = "";
		for (int i = 0; i < 20; i++)
			fakeChecksum +=
				Integer.toHexString(random.nextInt() & 0xf) +
					Integer.toHexString(random.nextInt() & 0xf);
		final long fakeTimestamp =
			19700000000000l + (random.nextLong() % 400000000000l);
		return new FileObject("", label, fakeChecksum, fakeTimestamp,
			FileObject.Status.NOT_INSTALLED);
	}

	public static void main(final String[] args) {
		final FilesCollection plugins = new FilesCollection();
		final ResolveDependencies frame = new ResolveDependencies(null, plugins);

		FileObject plugin = fakePlugin("Install_.jar");
		plugin.addDependency("Obsoleted_.jar");
		plugin.addDependency("Locally_Modified.jar");
		plugin.setStatus(FileObject.Status.NOT_INSTALLED);
		plugin.setAction(plugins, FileObject.Action.INSTALL);
		plugins.add(plugin);
		plugin = fakePlugin("Obsoleting_.jar");
		plugin.addDependency("Obsoleted_.jar");
		plugin.setStatus(FileObject.Status.NOT_INSTALLED);
		plugin.setAction(plugins, FileObject.Action.INSTALL);
		plugins.add(plugin);
		plugin = fakePlugin("Locally_Modified.jar");
		plugin.setStatus(FileObject.Status.MODIFIED);
		plugin.setAction(plugins, FileObject.Action.MODIFIED);
		plugins.add(plugin);
		plugin = fakePlugin("Obsoleted_.jar");
		plugin.setStatus(FileObject.Status.OBSOLETE);
		plugin.setAction(plugins, FileObject.Action.OBSOLETE);
		plugins.add(plugin);

		System.err.println("resolved: " + frame.resolve());
	}
}
