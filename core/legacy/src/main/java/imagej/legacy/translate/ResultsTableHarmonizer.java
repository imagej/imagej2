/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.legacy.translate;

import org.scijava.display.DisplayService;

import ij.IJ;
import ij.plugin.filter.Analyzer;
import ij.text.TextWindow;
import imagej.data.table.DefaultResultsTable;
import imagej.data.table.ResultsTable;
import imagej.data.table.Table;
import imagej.data.table.TableDisplay;

/**
 * Harmonizes data between legacy ImageJ and modern ImageJ ResultsTables
 * 
 * @author Barry DeZonia
 */
public class ResultsTableHarmonizer {

	// -- instance variables --

	private final DisplayService displayService;

	// -- constructor --

	public ResultsTableHarmonizer(DisplayService dispSrv) {
		displayService = dispSrv;
	}

	// -- ResultsTableHarmonizer methods --


	// NB - since modern ImageJ currently supports null (empty) column names we
	// need to set headings by col which is deprecated in IJ1.

	@SuppressWarnings("deprecation")

	public void setLegacyImageJResultsTable() {
		TableDisplay display = displayService.getActiveDisplay(TableDisplay.class);
		ResultsTable table = getFirstResultsTable(display);
		if (table == null) {
			Analyzer.setResultsTable(null);
			return;
		}
		ij.measure.ResultsTable ij1Table = new ij.measure.ResultsTable();
		ij1Table.setDefaultHeadings();
		for (int c = 0; c < table.getColumnCount(); c++) {
			String header = table.getColumnHeader(c);
			if (header == null) {
				// TODO - can't help but to use deprecated API
				ij1Table.setHeading(c, null);
			}
			else { // modern ij col header != null
				int colIndex = ij1Table.getColumnIndex(header);
				if (colIndex < 0) {
					int newCol = ij1Table.getFreeColumn(header);
					// TODO - can't help but to use deprecated API
					ij1Table.setHeading(newCol, header);
				}
				else {
					ij1Table.setHeading(colIndex, header);
				}
			}
		}
		for (int r = 0; r < table.getRowCount(); r++) {
			ij1Table.incrementCounter();
			ij1Table.setLabel(table.getRowHeader(r), r);
			for (int c = 0; c < table.getColumnCount(); c++) {
				String header = table.getColumnHeader(c);
				int ij1ColIndex = c;
				if (header != null) {
					ij1ColIndex = ij1Table.getColumnIndex(header);
				}
				double value = table.get(c, r);
				ij1Table.setValue(ij1ColIndex, r, value);
			}
		}
		IJ.getTextPanel(); // HACK - force IJ1 to append data
		Analyzer.setResultsTable(ij1Table);
	}

	public void setModernImageJResultsTable() {
		TableDisplay display = displayService.getActiveDisplay(TableDisplay.class);
		ResultsTable table = getFirstResultsTable(display);
		ij.measure.ResultsTable ij1Table = Analyzer.getResultsTable();

		// were there no ij1 results?
		if (ij1Table == null) {
			if (display == null) return;
			if (table == null) return;
			display.remove(table);
			if (display.isEmpty()) display.close();
			else display.update();
			return;
		}
		// were the results empty?
		if (ij1Table.getCounter() == 0) {
			if (display == null) return;
			if (table == null) return;
		}

		// if here there are nonempty ij1 results to harmonize
		if (table == null) {
			table = new DefaultResultsTable();
			if (display != null) display.add(table);
		}

		// rebuild table
		table.clear();
		table.setRowCount(0);
		for (int c = 0; c <= ij1Table.getLastColumn(); c++) {
			if (ij1Table.columnExists(c)) {
				table.appendColumn(ij1Table.getColumnHeading(c));
			}
		}
		for (int r = 0; r < ij1Table.getCounter(); r++) {
			table.appendRow(ij1Table.getLabel(r));
			for (int modIjCol = 0, c = 0; c <= ij1Table.getLastColumn(); c++) {
				if (ij1Table.columnExists(c)) {
					double value = ij1Table.getValueAsDouble(c, r);
					table.setValue(modIjCol++, r, value);
				}
			}
		}

		// close IJ1's table
		TextWindow window = ij.measure.ResultsTable.getResultsWindow();
		if (window != null) window.close(false);

		// display results in modern ImageJ as appropriate
		if (display == null) {
			displayService.createDisplay(table);
		}
		else {
			display.update();
		}
	}

	// -- private helpers --

	private ResultsTable getFirstResultsTable(TableDisplay display) {
		if (display == null) return null;
		for (Table<?, ?> table : display) {
			if (table instanceof ResultsTable) return (ResultsTable) table;
		}
		return null;
	}
}
