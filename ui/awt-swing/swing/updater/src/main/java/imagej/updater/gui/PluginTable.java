
package imagej.updater.gui;

import imagej.updater.core.PluginCollection;
import imagej.updater.core.PluginCollection.UpdateSite;
import imagej.updater.core.PluginObject;
import imagej.updater.core.PluginObject.Action;
import imagej.updater.core.PluginObject.Status;
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
public class PluginTable extends JTable {

	protected UpdaterFrame updaterFrame;
	protected PluginCollection plugins;
	private PluginTableModel pluginTableModel;
	protected Font plain, bold;

	public PluginTable(final UpdaterFrame updaterFrame) {
		this.updaterFrame = updaterFrame;
		plugins = updaterFrame.plugins;

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

		pluginTableModel = new PluginTableModel(plugins);
		setModel(pluginTableModel);
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
	 * this plugin, or when it is locally modified.
	 *
	 * It also warns loudly when the plugin is obsolete, but locally
	 * modified.
	 */
	protected void
		setStyle(final Component comp, final int row, final int column)
	{
		if (plain == null) {
			plain = comp.getFont();
			bold = plain.deriveFont(Font.BOLD);
		}
		final PluginObject plugin = getPlugin(row);
		if (plugin == null) return;
		comp.setFont(plugin.actionSpecified() || plugin.isLocallyModified() ? bold
			: plain);
		comp.setForeground(plugin.getStatus() == Status.OBSOLETE_MODIFIED
			? Color.red : Color.black);
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

	public PluginCollection getAllPlugins() {
		return plugins;
	}

	public void setPlugins(final Iterable<PluginObject> plugins) {
		pluginTableModel.setPlugins(plugins);
	}

	@Override
	public TableCellEditor getCellEditor(final int row, final int col) {
		final PluginObject plugin = getPlugin(row);

		// As we follow PluginTableModel, 1st column is filename
		if (col == 0) return super.getCellEditor(row, col);
		final Action[] actions = plugins.getActions(plugin);
		return new DefaultCellEditor(new JComboBox(actions));
	}

	public void maybeShowPopupMenu(final MouseEvent e) {
		if (!e.isPopupTrigger() &&
			(Util.isMacOSX() || (e.getModifiers() & InputEvent.META_MASK) == 0)) return;
		final Iterable<PluginObject> selected =
			getSelectedPlugins(e.getY() / getRowHeight());
		if (!selected.iterator().hasNext()) return;
		final JPopupMenu menu = new JPopupMenu();
		int count = 0;
		for (final PluginObject.Action action : plugins.getActions(selected)) {
			final JMenuItem item = new JMenuItem(action.toString());
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					for (final PluginObject plugin : selected)
						setPluginAction(plugin, action);
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

	public PluginObject getPlugin(final int row) {
		final PluginObject.LabeledPlugin plugin =
			(PluginObject.LabeledPlugin) getValueAt(row, 0);
		return plugin == null ? null : plugin.getPlugin();
	}

	public Iterable<PluginObject> getSelectedPlugins() {
		return getSelectedPlugins(-1);
	}

	public Iterable<PluginObject> getSelectedPlugins(final int fallbackRow) {
		int[] rows = getSelectedRows();
		if (rows.length == 0 && fallbackRow >= 0 && getPlugin(fallbackRow) != null) rows =
			new int[] { fallbackRow };
		final PluginObject[] result = new PluginObject[rows.length];
		for (int i = 0; i < rows.length; i++)
			result[i] = getPlugin(rows[i]);
		return Arrays.asList(result);
	}

	public String[] getUpdateSitesWithUploads(final PluginCollection plugins) {
		final Set<String> sites = new HashSet<String>();
		for (final PluginObject plugin : plugins.toUpload())
			sites.add(plugin.updateSite);
		return sites.toArray(new String[sites.size()]);
	}

	public boolean areAllSelectedPluginsUploadable() {
		if (getSelectedRows().length == 0) return false;
		for (final PluginObject plugin : getSelectedPlugins())
			if (!plugin.isUploadable(updaterFrame.plugins)) return false;
		return true;
	}

	public boolean chooseUpdateSite(final PluginCollection plugins,
		final PluginObject plugin)
	{
		final List<String> list = new ArrayList<String>();
		for (final String name : plugins.getUpdateSiteNames()) {
			final UpdateSite site = plugins.getUpdateSite(name);
			if (site.isUploadable()) list.add(name);
		}
		if (list.size() == 0) {
			error("No upload site available");
			return false;
		}
		if (list.size() == 1 &&
			list.get(0).equals(PluginCollection.DEFAULT_UPDATE_SITE))
		{
			plugin.updateSite = PluginCollection.DEFAULT_UPDATE_SITE;
			return true;
		}
		final String updateSite =
			SwingTools.getChoice(updaterFrame.hidden, updaterFrame, list,
				"To which upload site do you want to upload " + plugin.filename + "?",
				"Upload site");
		if (updateSite == null) return false;
		plugin.updateSite = updateSite;
		return true;
	}

	protected void
		setPluginAction(final PluginObject plugin, final Action action)
	{
		if (!plugin.getStatus().isValid(action)) return;
		if (action == Action.UPLOAD) {
			final String[] sitesWithUploads =
				getUpdateSitesWithUploads(updaterFrame.plugins);
			if (sitesWithUploads.length > 1) {
				error("Internal error: multiple upload sites selected");
				return;
			}
			final boolean isNew = plugin.getStatus() == Status.LOCAL_ONLY;
			if (sitesWithUploads.length == 0) {
				if (isNew && !chooseUpdateSite(updaterFrame.plugins, plugin)) return;
			}
			else {
				final String siteName = sitesWithUploads[0];
				if (isNew) plugin.updateSite = siteName;
				else if (!plugin.updateSite.equals(siteName)) {
					error("Already have uploads for site '" + siteName +
						"', cannot upload to '" + plugin.updateSite + "', too!");
					return;
				}
			}
		}
		plugin.setAction(updaterFrame.plugins, action);
		firePluginChanged(plugin);
	}

	protected class PluginTableModel extends AbstractTableModel {

		private PluginCollection plugins;
		Map<PluginObject, Integer> pluginToRow;

		public PluginTableModel(final PluginCollection plugins) {
			this.plugins = plugins;
		}

		public void setPlugins(final Iterable<PluginObject> plugins) {
			setPlugins(PluginCollection.clone(plugins));
		}

		public void setPlugins(final PluginCollection plugins) {
			this.plugins = plugins;
			pluginToRow = null;
			fireTableChanged(new TableModelEvent(pluginTableModel));
		}

		@Override
		public int getColumnCount() {
			return 2; // Name of plugin, status
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

		public PluginObject getEntry(final int rowIndex) {
			return plugins.get(rowIndex);
		}

		@Override
		public int getRowCount() {
			return plugins.size();
		}

		@Override
		public Object getValueAt(final int row, final int column) {
			if (row < 0 || row >= plugins.size()) return null;
			return plugins.get(row).getLabeledPlugin(column);
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
				final PluginObject plugin = getPlugin(row);
				setPluginAction(plugin, action);
			}
		}

		public void fireRowChanged(final int row) {
			fireTableRowsUpdated(row, row);
		}

		public void firePluginChanged(final PluginObject plugin) {
			if (pluginToRow == null) {
				pluginToRow = new HashMap<PluginObject, Integer>();
				// the table may be sorted, and we need the model's row
				int i = 0;
				for (final PluginObject p : plugins)
					pluginToRow.put(p, new Integer(i++));
			}
			final Integer row = pluginToRow.get(plugin);
			if (row != null) fireRowChanged(row.intValue());
		}
	}

	public void firePluginChanged(final PluginObject plugin) {
		pluginTableModel.firePluginChanged(plugin);
	}

	protected void error(final String message) {
		SwingTools.showMessageBox(updaterFrame.hidden, updaterFrame, message,
			JOptionPane.ERROR_MESSAGE);
	}
}
