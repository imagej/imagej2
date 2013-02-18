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

package imagej.data.table;

import java.util.ArrayList;

import org.scijava.util.SizableArrayList;

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
		checkRowCount(rowCount);
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
		checkColCount(colCount);
		setSize(colCount);
		scaleColumns();
	}

	@Override
	public C get(final String colHeader) {
		return get(colIndex(colHeader));
	}

	@Override
	public C appendColumn() {
		return appendColumn(null);
	}

	@Override
	public C appendColumn(final String header) {
		return insertColumn(getColumnCount(), header);
	}

	@Override
	public ArrayList<C> appendColumns(final int count) {
		final ArrayList<C> result = new ArrayList<C>(count);
		for (int c = 0; c < count; c++) {
			result.add(appendColumn());
		}
		return result;
	}

	@Override
	public ArrayList<C> appendColumns(final String... headers) {
		final ArrayList<C> result = new ArrayList<C>(headers.length);
		for (final String header : headers) {
			result.add(appendColumn(header));
		}
		return result;
	}

	@Override
	public C insertColumn(final int col) {
		return insertColumn(col, null);
	}

	@Override
	public C insertColumn(final int col, final String header) {
		final ArrayList<C> result = insertColumns(col, 1);
		setColumnHeader(col, header);
		return result.get(0);
	}

	@Override
	public ArrayList<C> insertColumns(final int col, final int count) {
		checkCol(col, 0);
		final int oldColCount = getColumnCount();
		final int newColCount = oldColCount + count;

		// expand columns list
		setColumnCount(newColCount);

		// copy columns after the inserted range into the new position
		for (int oldC = col; oldC < oldColCount; oldC++) {
			final int newC = oldC + count;
			set(newC, get(oldC));
		}

		// insert new blank columns
		final ArrayList<C> result = new ArrayList<C>(count);
		for (int c = 0; c < count; c++) {
			final C column = createColumn(null);
			result.add(column);
			set(col + c, column);
		}

		return result;
	}

	@Override
	public ArrayList<C> insertColumns(final int col, final String... headers) {
		// insert empty columns as a block
		final ArrayList<C> result = insertColumns(col, headers.length);

		// set headers for newly inserted columns
		for (int c = 0; c < headers.length; c++) {
			setColumnHeader(col + c, headers[c]);
		}
		return result;
	}

	@Override
	public C removeColumn(final int col) {
		return remove(col);
	}

	@Override
	public C removeColumn(final String header) {
		return removeColumn(colIndex(header));
	}

	@Override
	public ArrayList<C> removeColumns(final int col, final int count) {
		checkCol(col, count);

		// save to-be-removed columns
		final ArrayList<C> result = new ArrayList<C>(count);
		for (int c = 0; c < count; c++) {
			result.add(get(col + c));
		}

		final int oldColCount = getColumnCount();
		final int newColCount = oldColCount - count;

		// copy data after the deleted range into the new position
		for (int oldC = col; oldC < oldColCount; oldC++) {
			final int newC = oldC - count;
			set(newC, get(oldC));
		}
		setColumnCount(newColCount);

		return result;
	}

	@Override
	public ArrayList<C> removeColumns(final String... headers) {
		final ArrayList<C> result = new ArrayList<C>(headers.length);
		for (final String header : headers) {
			result.add(removeColumn(header));
		}
		return result;
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public void setRowCount(final int rowCount) {
		checkRowCount(rowCount);
		this.rowCount = rowCount;
		scaleColumns();
	}

	@Override
	public void appendRow() {
		appendRow(null);
	}

	@Override
	public void appendRow(final String header) {
		insertRow(getRowCount(), header);
	}

	@Override
	public void appendRows(final int count) {
		for (int c = 0; c < count; c++) {
			appendRow();
		}
	}

	@Override
	public void appendRows(final String... headers) {
		for (final String header : headers) {
			appendRow(header);
		}
	}

	@Override
	public void insertRow(final int row) {
		insertRow(row, null);
	}

	@Override
	public void insertRow(final int row, final String header) {
		insertRows(row, 1);
		setRowHeader(row, header);
	}

	@Override
	public void insertRows(final int row, final int count) {
		checkRow(row, 0);
		final int oldRowCount = getRowCount();
		final int newRowCount = oldRowCount + count;

		// expand rows list
		setRowCount(newRowCount);

		// copy data after the inserted range into the new position
		for (int oldR = row; oldR < oldRowCount; oldR++) {
			final int newR = oldR + count;
			for (int c = 0; c < getColumnCount(); c++) {
				set(c, newR, get(c, oldR));
			}
		}

		// copy row headers after the inserted range into the new position
		// NB: This loop goes backwards for performance.
		// It ensures that rowHeaders is resized at most once.
		for (int oldR = oldRowCount - 1; oldR >= row; oldR--) {
			final int newR = oldR + count;
			setRowHeader(newR, getRowHeader(oldR));
		}

		// insert new blank row data
		for (int r = 0; r < count; r++) {
			for (int c = 0; c < getColumnCount(); c++) {
				set(c, row + r, null);
			}
		}

		// insert new blank row headers
		for (int r = 0; r < count; r++) {
			setRowHeader(row + r, null);
		}
	}

	@Override
	public void insertRows(final int row, final String... headers) {
		// insert empty rows as a block
		insertRows(row, headers.length);

		// set headers for newly inserted rows
		for (int r = 0; r < headers.length; r++) {
			setRowHeader(row + r, headers[r]);
		}
	}

	@Override
	public void removeRow(final int row) {
		removeRows(row, 1);
	}

	@Override
	public void removeRow(final String header) {
		final int row = getColumnIndex(header);
		if (row < 0) {
			throw new IndexOutOfBoundsException("No such row: " + header);
		}
		removeRow(row);
	}

	@Override
	public void removeRows(final int row, final int count) {
		checkRow(row, count);
		final int oldRowCount = getRowCount();
		final int newRowCount = oldRowCount - count;
		// copy data after the deleted range into the new position
		for (int oldR = row; oldR < oldRowCount; oldR++) {
			final int newR = oldR - count;
			setRowHeader(newR, getRowHeader(oldR));
			for (int c = 0; c < getColumnCount(); c++) {
				set(c, newR, get(c, oldR));
			}
		}
		setRowCount(newRowCount);
		// trim row headers list, if needed
		if (rowHeaders.size() > newRowCount) rowHeaders.setSize(newRowCount);
	}

	@Override
	public void removeRows(final String... headers) {
		for (final String header : headers) {
			removeRow(header);
		}
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
	public void setColumnHeader(final int col, final String header) {
		get(col).setHeader(header);
	}

	@Override
	public int getColumnIndex(final String header) {
		for (int c = 0; c < getColumnCount(); c++) {
			final String h = getColumnHeader(c);
			if (equal(h, header)) return c;
		}
		return -1;
	}

	@Override
	public String getRowHeader(final int row) {
		checkRow(row, 1);
		if (rowHeaders.size() <= row) return null; // label not initialized
		return rowHeaders.get(row);
	}

	@Override
	public void setRowHeader(final int row, final String header) {
		checkRow(row, 1);
		if (row >= rowHeaders.size()) {
			// ensure row headers list is long enough to accommodate the header
			rowHeaders.setSize(row + 1);
		}
		// update the row header value, where applicable
		rowHeaders.set(row, header);
	}

	@Override
	public int getRowIndex(final String header) {
		for (int r = 0; r < getRowCount(); r++) {
			final String h = getRowHeader(r);
			if (equal(h, header)) return r;
		}
		return -1;
	}

	@Override
	public void set(final int col, final int row, final T value) {
		check(col, row);
		get(col).set(row, value);
	}

	@Override
	public void set(final String colHeader, final int row, final T value) {
		final int col = colIndex(colHeader);
		checkRow(row, 1);
		get(col).set(row, value);
	}

	@Override
	public T get(final int col, final int row) {
		check(col, row);
		return get(col).get(row);
	}

	@Override
	public T get(final String colHeader, final int row) {
		final int col = colIndex(colHeader);
		checkRow(row, 1);
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

	/** Throws an exception if the given row or column is out of bounds. */
	private void check(final int col, final int row) {
		checkCol(col, 1);
		checkRow(row, 1);
	}

	/** Throws an exception if the given column(s) are out of bounds. */
	private void checkCol(final int col, final int count) {
		check("column", col, count, getColumnCount());
	}

	/** Throws an exception if the given row(s) are out of bounds. */
	private void checkRow(final int row, final int count) {
		check("row", row, count, getRowCount());
	}

	/** Throws an exception if the given values are out of bounds. */
	private void check(final String name, final int index, final int count,
		final int bound)
	{
		final int last = index + count - 1;
		if (index >= 0 && last < bound) return;
		if (count <= 1) {
			throw new IndexOutOfBoundsException("Invalid " + name + ": " + index);
		}
		throw new IndexOutOfBoundsException("Invalid " + name + "s: " + index +
			" - " + last);
	}

	/** Throws an exception if the given column count is invalid. */
	private void checkColCount(final int count) {
		checkCount("column", count);
	}

	/** Throws an exception if the given row count is invalid. */
	private void checkRowCount(final int count) {
		checkCount("row", count);
	}

	/** Throws an exception if the given count is invalid. */
	private void checkCount(final String name, final int count) {
		if (count >= 0) return;
		throw new IllegalArgumentException("Invalid " + name + " count: " + count);
	}

	/**
	 * Gets the column index corresponding to the given header, throwing an
	 * exception if no such column exists.
	 */
	private int colIndex(final String header) {
		final int col = getColumnIndex(header);
		if (col < 0) {
			throw new IllegalArgumentException("No such column: " + header);
		}
		return col;
	}

	/**
	 * Returns true iff both objects are null, or the objects are equal via
	 * {@link Object#equals}.
	 */
	private boolean equal(final Object o1, final Object o2) {
		if (o1 == null && o2 == null) return true;
		if (o1 != null && o1.equals(o2)) return true;
		return false;
	}

}
