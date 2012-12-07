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

package imagej.ui.swing.viewer.table;

import imagej.data.table.Table;
import imagej.data.table.TableDisplay;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ui.viewer.DisplayWindow;
import imagej.ui.viewer.table.TableDisplayPanel;

import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * This is the display panel for {@link Table}s.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class SwingTableDisplayPanel extends JScrollPane implements
	TableDisplayPanel
{

	// -- instance variables --

	private final DisplayWindow window;
	private final TableDisplay display;
	private final JTable table;
	private final NullTableModel nullModel;

	@SuppressWarnings("unused")
	private final List<EventSubscriber<?>> subscribers;

	// -- constructor --

	public SwingTableDisplayPanel(final TableDisplay display,
		final DisplayWindow window)
	{
		this.display = display;
		this.window = window;
		nullModel = new NullTableModel();
		table = makeTable();
		table.setAutoCreateRowSorter(true);
		setViewportView(table);
		window.setContent(this);

		final EventService eventService =
			display.getContext().getService(EventService.class);
		subscribers = eventService.subscribe(this);
	}

	// -- TableDisplayPanel methods --

	@Override
	public TableDisplay getDisplay() {
		return display;
	}

	// -- DisplayPanel methods --

	@Override
	public DisplayWindow getWindow() {
		return window;
	}

	@Override
	public void redoLayout() {
		// Nothing to layout
	}

	@Override
	public void setLabel(final String s) {
		// The label is not shown.
	}

	@Override
	public void redraw() {
		// BDZ - I found a TODO here saying implement me. Not sure if my one liner
		// is correct but it seems to work.
		// one liner:
		// table.update(table.getGraphics());
		// BDZ now try something more intuitive
		// table.repaint(); // nope
		// BDZ this?
		// table.doLayout(); // nope

		// note that our table is not attached to the table model as a listener.
		// we might need to do that. but try to find a simple way to enforce a
		// reread of the table because when it gets in here it's contents are ok

		javax.swing.table.TableModel model = table.getModel();

		// BDZ attempt to force a rebuild. no luck. and also fails the Clear test.
		// Object v = model.getValueAt(0, 0);
		// model.setValueAt(v, 0, 0);

		// BDZ hacky hack way that works
		table.setModel(nullModel);
		table.setModel(model);
		// table.repaint();
	}

	// -- Event Handlers

	/*
	@EventHandler
	protected void onEvent(DisplayUpdatedEvent evt) {
		if (evt.getDisplay() != display) return;
		System.out.println("JTable.getModel().getRowCount() == " +
			table.getModel().getRowCount());
		redraw();
	}
	*/

	// -- Helper methods --

	private JTable makeTable() {
		return new JTable(new TableModel(getTable()));
	}

	private Table<?, ?> getTable() {
		return display.size() == 0 ? null : display.get(0);
	}

	// -- Helper classes --

	public static class NullTableModel extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			return 0;
		}

		@Override
		public int getRowCount() {
			return 0;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return null;
		}

	}

	/** A Swing {@link TableModel} backed by an ImageJ {@link Table}. */
	public static class TableModel extends AbstractTableModel {

		private final Table<?, ?> tab;

		public TableModel(final Table<?, ?> table) {
			this.tab = table;
		}

		@Override
		public String getColumnName(final int col) {
			if (col == 0) return "";
			return tab.getColumnHeader(col - 1);
		}

		@Override
		public int getRowCount() {
			return tab.getRowCount();
		}

		@Override
		public int getColumnCount() {
			return tab.getColumnCount() + 1; // +1 for row header column
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			if (row < 0 || row >= getRowCount()) return null;
			if (col < 0 || col >= getColumnCount()) return null;

			if (col == 0) {
				// get row header, or row number if none
				// NB: Assumes the JTable can handle Strings equally as well as the
				// underlying type T of the Table.
				final String header = tab.getRowHeader(row);
				if (header != null) return header;
				return "" + (row + 1);
			}

			// get the underlying table value
			// NB: The column is offset by one to accommodate the row header/number.
			return tab.get(col - 1, row);
		}

		@Override
		public void setValueAt(final Object value, final int row, final int col) {
			if (row < 0 || row >= getRowCount()) return;
			if (col < 0 || col >= getColumnCount()) return;
			if (col == 0) {
				// set row header
				tab.setRowHeader(row, value == null ? null : value.toString());
				return;
			}
			set(tab, col - 1, row, value);
			fireTableCellUpdated(row, col);
		}

		// -- Helper methods --

		private <T> void set(final Table<?, T> table,
			final int col, final int row, final Object value)
		{
			@SuppressWarnings("unchecked")
			final T typedValue = (T) value;
			table.set(col, row, typedValue);
		}

	}

}
