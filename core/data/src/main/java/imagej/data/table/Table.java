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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A table of values.
 * 
 * @author Curtis Rueden
 * @param <C> The type of column used by the table.
 * @param <T> The type of data stored in the table.
 */
public interface Table<C extends Column<T>, T> extends List<C> {

	/** Gets the number of columns in the table. */
	int getColumnCount();

	/** Sets the number of columns in the table. */
	void setColumnCount(int colCount);

	/**
	 * Appends a column (with no header) to the table.
	 * 
	 * @return the column that was appended
	 */
	C appendColumn();

	/**
	 * Appends a column with the given header to the table.
	 * 
	 * @return the column that was appended
	 */
	C appendColumn(String header);

	/**
	 * Appends a number of columns (with no headers) to the table.
	 * 
	 * @return the columns that were appended
	 */
	List<C> appendColumns(int count);

	/**
	 * Appends a block of columns with the given headers to the table.
	 * 
	 * @return the columns that were appended
	 */
	List<C> appendColumns(String... headers);

	/**
	 * Inserts a column (with no header) at the given position in the table.
	 * 
	 * @return the column that was inserted
	 */
	C insertColumn(int col);

	/**
	 * Inserts a column with the specified header at the given position in the
	 * table.
	 * 
	 * @return the column that was inserted
	 */
	C insertColumn(int col, String header);

	/**
	 * Inserts a block of columns (with no headers) at the given position in the
	 * table.
	 * 
	 * @return the columns that were inserted
	 */
	List<C> insertColumns(int col, int count);

	/**
	 * Inserts a block of columns with the specified headers at the given position
	 * in the table.
	 * 
	 * @return the columns that were inserted
	 */
	List<C> insertColumns(int col, String... headers);

	/**
	 * Removes the column at the given position from the table.
	 * 
	 * @return the column that was removed
	 */
	C removeColumn(int col);

	/**
	 * Removes the first column with the given header from the table.
	 * 
	 * @return the column that was removed
	 */
	C removeColumn(String header);

	/**
	 * Removes a block of columns starting at the given position from the table.
	 * 
	 * @return the columns that were removed
	 */
	List<C> removeColumns(int col, int count);

	/**
	 * Removes the first columns with the given headers from the table.
	 * 
	 * @return the columns that were removed
	 */
	List<C> removeColumns(String... headers);

	/** Gets the number of rows in the table. */
	int getRowCount();

	/** Sets the number of rows in the table. */
	void setRowCount(int rowCount);

	/** Appends a row (with no header) to the table. */
	void appendRow();

	/** Appends a row with the given header to the table. */
	void appendRow(String header);

	/** Appends a block of rows (with no headers) to the table. */
	void appendRows(int count);

	/** Appends a block of rows with the given headers to the table. */
	void appendRows(String... headers);

	/** Inserts a row (with no header) at the given position in the table. */
	void insertRow(int row);

	/**
	 * Inserts a row with the specified header at the given position in the table.
	 */
	void insertRow(int row, String header);

	/**
	 * Inserts a block of rows (with no headers) at the given position in the
	 * table.
	 */
	void insertRows(int row, int count);

	/**
	 * Inserts a block of rows with the specified headers at the given position in
	 * the table.
	 */
	void insertRows(int row, String... headers);

	/** Removes the row at the given position from the table. */
	void removeRow(int row);

	/** Removes the first row with the given header from the table. */
	void removeRow(String header);

	/**
	 * Removes a block of rows starting at the given position from the table.
	 */
	void removeRows(int row, int count);

	/** Removes the first rows with the given headers from the table. */
	void removeRows(String... headers);

	/** Sets the number of columns and rows in the table. */
	void setDimensions(int colCount, int rowCount);

	/** Gets the column header at the given column. */
	String getColumnHeader(int col);

	/** Sets the column header at the given column. */
	void setColumnHeader(int col, String header);

	/** Gets the column index of the column with the given header. */
	int getColumnIndex(String header);

	/** Gets the row header at the given row. */
	String getRowHeader(int row);

	/** Sets the row header at the given row. */
	void setRowHeader(int row, String header);

	/** Gets the row index of the row with the given header. */
	int getRowIndex(String header);

	/** Sets the table value at the given column and row. */
	void set(int col, int row, T value);

	/** Sets the table value at the given column and row. */
	void set(String colHeader, int row, T value);

	/** Gets the table value at the given column and row. */
	T get(int col, int row);

	/** Gets the table value at the given column and row. */
	T get(String colHeader, int row);

	// -- List methods --

	/** Gets the number of columns in the table. */
	@Override
	int size();

	/** Gets whether the table is empty. */
	@Override
	boolean isEmpty();

	/**
	 * Gets whether the table contains the given column.
	 * 
	 * @param column The {@link Column} whose presence in the table is to be
	 *          tested.
	 */
	@Override
	boolean contains(Object column);

	/** Returns an iterator over the columns in the table in proper sequence. */
	@Override
	Iterator<C> iterator();

	/**
	 * Returns an array containing all of the columns in the table in proper
	 * sequence (from first to last column).
	 */
	@Override
	Object[] toArray();

	/**
	 * Returns an array containing all of the column in the table in proper
	 * sequence (from first to last column); the runtime type of the returned
	 * array is that of the specified array. If the list of columns fits in the
	 * specified array, it is returned therein. Otherwise, a new array is
	 * allocated with the runtime type of the specified array and the size of this
	 * list of columns.
	 */
	@Override
	<A> A[] toArray(A[] a);

	/**
	 * Appends the specified column to the end of the table.
	 * <p>
	 * No checking is done to ensure the new column has the same number of rows as
	 * the other existing columns.
	 * </p>
	 */
	@Override
	boolean add(C column);

	/**
	 * Removes the first occurrence of the specified column from the table, if it
	 * is present.
	 * 
	 * @return <tt>true</tt> if the table contained the specified column
	 */
	@Override
	boolean remove(Object column);

	/**
	 * Returns <tt>true</tt> if the table contains all of the columns of the
	 * specified collection.
	 */
	@Override
	boolean containsAll(Collection<?> c);

	/**
	 * Appends all of the columns in the specified collection to the end of the
	 * table, in the order that they are returned by the specified collection's
	 * iterator.
	 * <p>
	 * No checking is done to ensure the new columns have the same number of rows
	 * as the other existing columns.
	 * </p>
	 * 
	 * @return <tt>true</tt> if the table changed as a result of the call
	 */
	@Override
	boolean addAll(Collection<? extends C> c);

	/**
	 * Inserts all of the columns in the specified collection into this list at
	 * the specified position.
	 * <p>
	 * No checking is done to ensure the new columns have the same number of rows
	 * as the other existing columns.
	 * </p>
	 * 
	 * @return <tt>true</tt> if the table changed as a result of the call
	 */
	@Override
	boolean addAll(int col, Collection<? extends C> c);

	/**
	 * Removes from the table all of its columns that are contained in the
	 * specified collection.
	 * 
	 * @return <tt>true</tt> if the table changed as a result of the call
	 */
	@Override
	boolean removeAll(Collection<?> c);

	/**
	 * Retains only the columns in the table that are contained in the specified
	 * collection. In other words, removes from the table all the columns that are
	 * not contained in the specified collection.
	 * 
	 * @return <tt>true</tt> if the table changed as a result of the call
	 */
	@Override
	boolean retainAll(Collection<?> c);

	/**
	 * Removes all data (including row and column headers) from the table. The
	 * table will be empty after this call returns.
	 * <p>
	 * If you want to retain the column headers, call {@link #setRowCount(int)}
	 * with a value of 0. If you want to retain the row headers, call
	 * {@link #setColumnCount(int)} with a value of 0.
	 * </p>
	 */
	@Override
	void clear();

	/** Returns the column at the specified position in the table. */
	@Override
	C get(int col);

	/**
	 * Replaces the column at the specified position in the table with the
	 * specified column.
	 * <p>
	 * No checking is done to ensure the new column has the same number of rows as
	 * the other existing columns.
	 * </p>
	 * 
	 * @return the column previously at the specified position
	 */
	@Override
	C set(int col, C column);

	/**
	 * Inserts the specified column at the specified position in the table.
	 * <p>
	 * No checking is done to ensure the new column has the same number of rows as
	 * the other existing columns.
	 * </p>
	 */
	@Override
	void add(int col, C column);

	/**
	 * Removes the column at the specified position in the table.
	 * 
	 * @return the column previously at the specified position
	 */
	@Override
	C remove(int col);

	/**
	 * Returns the index of the first occurrence of the specified column in the
	 * table, or -1 if the table does not contain the column.
	 */
	@Override
	int indexOf(Object column);

	/**
	 * Returns the index of the last occurrence of the specified column in the
	 * table, or -1 if the table does not contain the column.
	 */
	@Override
	int lastIndexOf(Object column);

	/**
	 * Returns a list iterator over the columns in the table (in proper sequence).
	 */
	@Override
	ListIterator<C> listIterator();

	/**
	 * Returns a list iterator of the columns in the table (in proper sequence),
	 * starting at the specified position in the table.
	 */
	@Override
	ListIterator<C> listIterator(int col);

	/**
	 * Returns a view of the portion of the table between the specified
	 * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive. The
	 * returned list is backed by the table, so non-structural changes in the
	 * returned list are reflected in the table, and vice-versa.
	 */
	@Override
	List<C> subList(int fromCol, int toCol);

}
