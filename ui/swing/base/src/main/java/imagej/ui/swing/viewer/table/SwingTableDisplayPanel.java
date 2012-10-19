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
 */
public class SwingTableDisplayPanel extends JScrollPane implements
	TableDisplayPanel
{

	private final DisplayWindow window;
	private final TableDisplay display;
	private final JTable table;

	@SuppressWarnings("unused")
	private final List<EventSubscriber<?>> subscribers;

	public SwingTableDisplayPanel(final TableDisplay display,
		final DisplayWindow window)
	{
		this.display = display;
		this.window = window;
		table = makeTable();
		table.setAutoCreateRowSorter(true);
		setViewportView(table);
		window.setContent(this);

		final EventService eventService =
			display.getContext().getService(EventService.class);
		subscribers = eventService.subscribe(this);
	}

	private JTable makeTable() {
		return new JTable(new TableModel(getTable()));
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
		// TODO
	}

	// -- Helper methods --

	private Table<?, ?> getTable() {
		return display.size() == 0 ? null : display.get(0);
	}

	// -- Helper classes --

	/** A Swing {@link TableModel} backed by an ImageJ {@link Table}. */
	public static class TableModel extends AbstractTableModel {

		private final Table<?, ?> table;

		public TableModel(final Table<?, ?> table) {
			this.table = table;
		}

		@Override
		public String getColumnName(final int col) {
			return table.getColumnHeader(col);
		}

		@Override
		public int getRowCount() {
			return table.getRowCount();
		}

		@Override
		public int getColumnCount() {
			return table.getColumnCount();
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			return table.get(col, row);
		}

		@Override
		public void setValueAt(final Object value, final int row, final int col) {
			set(table, value, col, row);
			fireTableCellUpdated(row, col);
		}

		private <T> void set(final Table<?, T> table, final Object value,
			final int row, final int col)
		{
			@SuppressWarnings("unchecked")
			final T typedValue = (T) value;
			table.set(typedValue, row, col);
		}

	}

}
