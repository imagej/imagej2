//
// CommandFinderPanel.java
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

package imagej.ui.swing;

import imagej.ImageJ;
import imagej.ext.AbstractUIDetails;
import imagej.ext.MenuEntry;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleService;
import imagej.ext.module.ui.menu.ShadowMenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

/**
 * A panel that allows the user to search for ImageJ commands. Based on the
 * original Command Finder plugin by Mark Longair and Johannes Schindelin.
 * 
 * @author Curtis Rueden
 */
public class CommandFinderPanel extends JPanel implements ActionListener,
	DocumentListener
{

	private List<Command> commands;

	private final JTextField searchField;
	private final JList commandsList;
	private final JCheckBox showFullCheckbox;

	public CommandFinderPanel() {
		buildCommands();

		searchField = new JTextField(12);
		commandsList = new JList();
		commandsList.setVisibleRowCount(20);
		showFullCheckbox = new JCheckBox("Show full information");

		searchField.getDocument().addDocumentListener(this);
		showFullCheckbox.addActionListener(this);

		updateCommands();

		final String layout = "fillx,wrap 2";
		final String cols = "[pref|fill,grow]";
		final String rows = "[pref|fill,grow|pref]";
		setLayout(new MigLayout(layout, cols, rows));
		add(new JLabel("Type part of a command:"));
		add(searchField);
		add(new JScrollPane(commandsList), "grow,span 2");
		add(showFullCheckbox, "center, span 2");
	}

	// -- CommandFinderPanel methods --

	/** Gets the currently selected command. */
	public ModuleInfo getCommand() {
		Command command = (Command) commandsList.getSelectedValue();
		if (command == null) {
			command = (Command) commandsList.getModel().getElementAt(0);
		}
		return command == null ? null : command.getModuleInfo();
	}

	/** Gets the {@link JTextField} component for specifying the search string. */
	public JTextField getSearchField() {
		return searchField;
	}

	public String getRegex() {
		return ".*" + searchField.getText().toLowerCase() + ".*";
	}

	/** Gets whether the "Show full information" option is checked. */
	public boolean isShowFull() {
		return showFullCheckbox.isSelected();
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		updateCommands();
	}

	// -- DocumentListener methods --

	@Override
	public void changedUpdate(final DocumentEvent arg0) {
		filterUpdated();
	}

	@Override
	public void insertUpdate(final DocumentEvent arg0) {
		filterUpdated();
	}

	@Override
	public void removeUpdate(final DocumentEvent arg0) {
		filterUpdated();
	}

	// -- Helper methods --

	/** Builds the master list of available commands. */
	private void buildCommands() {
		commands = new ArrayList<Command>();

		final ModuleService moduleService = ImageJ.get(ModuleService.class);
		final List<ModuleInfo> modules = moduleService.getModules();
		for (final ModuleInfo info : modules) {
			commands.add(new Command(info));
		}

		Collections.sort(commands);
	}

	/** Updates the list of visible commands. */
	private void updateCommands() {
		final String regex = getRegex();
		final DefaultListModel matches = new DefaultListModel();
		for (final Command command : commands) {
			if (!command.matches(regex)) continue; // no match
			matches.addElement(command);
		}
		commandsList.setModel(matches);
	}

	/** Called when the search filter text field changes. */
	private void filterUpdated() {
		updateCommands();
	}

	// -- Helper classes --

	private class Command implements Comparable<Command> {

		private final ModuleInfo info;
		private final String menuLabel;

		public Command(final ModuleInfo info) {
			this.info = info;
			menuLabel = ShadowMenu.getMenuLabel(info);
		}

		public boolean matches(final String regex) {
			return menuLabel.toLowerCase().matches(regex);
		}

		public ModuleInfo getModuleInfo() {
			return info;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder(menuLabel);
			if (isShowFull()) {
				final List<MenuEntry> menuPath = info.getMenuPath();
				final String menuString =
					AbstractUIDetails.getMenuString(menuPath, false);
				if (!menuString.isEmpty()) sb.append(" (in " + menuString + ")");
				sb.append(" : " + info.toString());
			}
			return sb.toString();
		}

		@Override
		public int compareTo(final Command command) {
			return menuLabel.compareTo(command.menuLabel);
		}

	}

}
