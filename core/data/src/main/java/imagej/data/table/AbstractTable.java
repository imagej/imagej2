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

package imagej.data.table;

import imagej.util.SizableArrayList;

/**
 * Abstract superclass for {@link Table} implementations.
 * 
 * @author Curtis Rueden
 * @param <T> The type of data stored in the table.
 */
public abstract class AbstractTable<C extends Column<T>, T> extends
	SizableArrayList<C> implements Table<C, T>
{

	/** Header for each row in the table. Lazily populated. */
	private final SizableArrayList<String> rowHeaders;

	/** Number of rows in the table. */
	private int rowCount;

	/** Creates an empty table. */
	public AbstractTable() {
		this(0, 0);
	}

	/** Creates a table with the given column and row dimensions. */
	public AbstractTable(final int colCount, final int rowCount) {
		super();
		rowHeaders = new SizableArrayList<String>();
		this.rowCount = rowCount;
		setColumnCount(colCount);
	}

	// -- Table methods --

	@Override
	public int getColumnCount() {
		return size();
	}

	@Override
	public void setColumnCount(final int colCount) {
		setSize(colCount);
		scaleColumns();
	}

	@Override
	public C addColumn(final String header) {
		setColumnCount(getColumnCount() + 1);
		final C column = createColumn(header);
		add(column);
		return column;
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public void setRowCount(final int rowCount) {
		this.rowCount = rowCount;
		scaleColumns();
	}

	@Override
	public void addRow() {
		setRowCount(getRowCount() + 1);
	}

	@Override
	public void addRow(final String header) {
		addRow();
		setRowHeader(header, getRowCount() - 1);
	}

	@Override
	public void setDimensions(final int colCount, final int rowCount) {
		setColumnCount(colCount);
		setRowCount(rowCount);
	}

	@Override
	public String getColumnHeader(final int col) {
		return get(col).getHeader();
	}

	@Override
	public void setColumnHeader(final String header, final int col) {
		get(col).setHeader(header);
	}

	@Override
	public int getColumnIndex(final String header) {
		for (int c = 0; c < size(); c++) {
			final Column<T> column = get(c);
			if (column.getHeader().equals(header)) return c;
		}
		return -1;
	}

	@Override
	public String getRowHeader(final int row) {
		checkRow(row);
		if (rowHeaders.size() <= row) return null; // label not initialized
		return rowHeaders.get(row);
	}

	@Override
	public void setRowHeader(final String header, final int row) {
		rowHeaders.setSize(getRowCount());
		rowHeaders.set(row, header);
	}

	@Override
	public void set(final T value, final int col, final int row) {
		check(col, row);
		get(col).set(row, value);
	}

	@Override
	public T get(final int col, final int row) {
		check(col, row);
		return get(col).get(row);
	}

	// -- Internal methods --

	protected abstract C createColumn(final String header);

	// -- Helper methods --

	/** Initializes and scales all columns to match the row count. */
	private void scaleColumns() {
		for (int c = 0; c < getColumnCount(); c++) {
			if (get(c) == null) {
				// initialize a new column
				set(c, createColumn(null));
			}
			get(c).setSize(getRowCount());
		}
	}

	/** Throws an exception if the given row is out of bounds. */
	private void checkRow(final int row) {
		if (row < 0 || row >= getRowCount()) {
			throw new IllegalArgumentException("Invalid row: " + row);
		}
	}

	/** Throws an exception if the given column is out of bounds. */
	private void checkCol(final int col) {
		if (col < 0 || col >= getColumnCount()) {
			throw new IllegalArgumentException("Invalid column: " + col);
		}
	}

	/** Throws an exception if the given row or column is out of bounds. */
	private void check(final int col, final int row) {
		checkCol(col);
		checkRow(row);
	}

}
