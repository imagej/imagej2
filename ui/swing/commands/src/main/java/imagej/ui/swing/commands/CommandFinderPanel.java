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

package imagej.ui.swing.commands;

import imagej.module.ModuleInfo;
import imagej.module.ModuleService;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;

import org.scijava.MenuPath;
import org.scijava.util.AppUtils;
import org.scijava.util.ClassUtils;
import org.scijava.util.FileUtils;

/**
 * A panel that allows the user to search for ImageJ commands. Based on the
 * original Command Finder plugin by Mark Longair and Johannes Schindelin.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
@SuppressWarnings("serial")
public class CommandFinderPanel extends JPanel implements ActionListener,
	DocumentListener
{

	protected final JTextField searchField;
	protected final JTable commandsList;
	protected final CommandTableModel tableModel;

	private final List<ModuleInfo> commands;

	public CommandFinderPanel(final ModuleService moduleService) {
		commands = buildCommands(moduleService);

		setPreferredSize(new Dimension(800, 600));

		searchField = new JTextField(12);
		commandsList = new JTable(20, CommandTableModel.COLUMN_COUNT);
		commandsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		commandsList.setRowSelectionAllowed(true);
		commandsList.setColumnSelectionAllowed(false);
		commandsList.setAutoCreateRowSorter(true);

		commandsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() < 2) return;
				int row = commandsList.rowAtPoint(e.getPoint());
				if (row >= 0) {
					closeDialog(true);
				}
			}
		});
		commandsList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					e.consume();
					closeDialog(true);
				}
			}
		});

		searchField.getDocument().addDocumentListener(this);

		tableModel = new CommandTableModel(commands);
		commandsList.setModel(tableModel);
		tableModel.setColumnWidths(commandsList.getColumnModel());

		final String layout = "fillx,wrap 2";
		final String cols = "[pref|fill,grow]";
		final String rows = "[pref|fill,grow|pref]";
		setLayout(new MigLayout(layout, cols, rows));
		add(new JLabel("Type part of a command:"));
		add(searchField);
		add(new JScrollPane(commandsList), "grow,span 2");

		searchField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(final KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_DOWN:
						commandsList.scrollRectToVisible(commandsList.getCellRect(0, 0, true));
						commandsList.setRowSelectionInterval(0, 0);
						commandsList.grabFocus();
						break;
					case KeyEvent.VK_UP:
						final int index = commandsList.getModel().getRowCount() - 1;
						commandsList.scrollRectToVisible(commandsList.getCellRect(index, 0, true));
						commandsList.setRowSelectionInterval(index, index);
						commandsList.grabFocus();
						break;
					default:
						return;
				}
				e.consume();
			}
		});

		commandsList.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(final KeyEvent e) {
				int selected;

				switch (e.getKeyCode()) {
					case KeyEvent.VK_DOWN:
						selected = commandsList.getSelectedRow();
						if (selected != commandsList.getModel().getRowCount() - 1) return;
						searchField.grabFocus();
						break;
					case KeyEvent.VK_UP:
						selected = commandsList.getSelectedRow();
						if (selected != 0) return;
						searchField.grabFocus();
						break;
					default:
						return;
				}
				e.consume();
			}
		});
	}

	// -- CommandFinderPanel methods --

	/** Gets the currently selected command. */
	public ModuleInfo getCommand() {
		if (tableModel.getRowCount() < 1) return null;
		int selectedRow = commandsList.getSelectedRow();
		if (selectedRow < 0) selectedRow = 0;
		return tableModel.get(commandsList.convertRowIndexToModel(selectedRow));
	}

	/** Gets the {@link JTextField} component for specifying the search string. */
	public JTextField getSearchField() {
		return searchField;
	}

	public String getRegex() {
		return ".*" + searchField.getText().toLowerCase() + ".*";
	}

	private void closeDialog(boolean okay) {
		for (Container parent = getParent(); parent != null; parent = parent.getParent()) {
			if (parent instanceof JOptionPane) {
				((JOptionPane)parent).setValue(okay ? JOptionPane.OK_OPTION : JOptionPane.CANCEL_OPTION);
			} else if (parent instanceof JDialog) {
				((JDialog)parent).setVisible(false);
				break;
			}
		}
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
	private List<ModuleInfo> buildCommands(final ModuleService moduleService) {
		final List<ModuleInfo> list = new ArrayList<ModuleInfo>();
		list.addAll(moduleService.getModules());
		Collections.sort(list);
		return list;
	}

	/** Updates the list of visible commands. */
	private void updateCommands() {
		final ModuleInfo selected = commandsList.getSelectedRow() < 0 ? null : getCommand();
		int counter = 0, selectedRow = -1;
		final String regex = getRegex();
		final List<ModuleInfo> matches = new ArrayList<ModuleInfo>();
		for (final ModuleInfo command : commands) {
			if (!command.getMenuPath().toString().toLowerCase().matches(regex)) continue; // no match
			matches.add(command);
			if (command == selected) selectedRow = counter;
			counter++;
		}
		tableModel.setData(matches);
		if (selectedRow >= 0) {
			commandsList.setRowSelectionInterval(selectedRow, selectedRow);
		}
	}

	/** Called when the search filter text field changes. */
	private void filterUpdated() {
		updateCommands();
	}

	// -- Helper classes --

	protected static class CommandTableModel extends AbstractTableModel {
		private final static String ijDir = AppUtils.getBaseDirectory().getAbsolutePath();
		protected List<ModuleInfo> list;

		public final static int COLUMN_COUNT = 8;

		public CommandTableModel(final List<ModuleInfo> list) {
			this.list = list;
		}

		public void setData(List<ModuleInfo> list) {
			this.list = list;
			fireTableDataChanged();
		}

		public void setColumnWidths(TableColumnModel columnModel) {
			int[] widths = { 32, 250, 150, 150, 250, 200, 50, 20 };
			for (int i = 0; i < widths.length; i++) {
				columnModel.getColumn(i).setPreferredWidth(widths[i]);
			}
			final TableColumn iconColumn = columnModel.getColumn(0);
			iconColumn.setMaxWidth(32);
			iconColumn.setCellRenderer(new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					return (Component)value;
				}
			});
		}

		public ModuleInfo get(int index) {
			return list.get(index);
		}

		@Override
		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) return "Icon";
			if (column == 1) return "Command";
			if (column == 2) return "Menu Path";
			if (column == 3) return "Shortcut";
			if (column == 4) return "Class";
			if (column == 5) return "File";
			if (column == 6) return "Description";
			if (column == 7) return "Priority";
			return null;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			final ModuleInfo info = list.get(row);
			if (column == 0) {
				final String iconPath = info.getIconPath();
				if (iconPath == null) return null;
				final URL iconURL = getClass().getResource(iconPath);
				return iconURL == null ? null : new JLabel(new ImageIcon(iconURL));
			}
			if (column == 1) return info.getTitle();
			if (column == 2) {
				final MenuPath menuPath = info.getMenuPath();
				if (menuPath == null) return "";
				return menuPath.getMenuString(false);
			}
			if (column == 3) {
				final MenuPath menuPath = info.getMenuPath();
				if (menuPath == null) return "";
				return menuPath.getLeaf().getAccelerator();
			}
			if (column == 4) return info.getDelegateClassName();
			if (column == 5) {
				final URL location = ClassUtils.getLocation(info.getDelegateClassName());
				final String file = FileUtils.urlToFile(location).getAbsolutePath();
				if (file.startsWith(ijDir))
					return file.substring(ijDir.length() + 1);
				return file;
			}
			if (column == 6) return info.getDescription();
			if (column == 7) return info.getPriority();
			return null;
		}
	}

}
