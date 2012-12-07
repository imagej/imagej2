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

package imagej.legacy.translate;

import ij.IJ;
import ij.plugin.filter.Analyzer;
import ij.text.TextWindow;
import imagej.ImageJ;
import imagej.data.table.DefaultResultsTable;
import imagej.data.table.ResultsTable;
import imagej.data.table.Table;
import imagej.data.table.TableDisplay;
import imagej.display.DisplayService;

/**
 * Harmonizes data between IJ1 and IJ2 ResultsTables
 * 
 * @author Barry DeZonia
 */
public class ResultsTableHarmonizer {

	// -- instance variables --

	private final DisplayService displayService;

	// -- constructor --

	public ResultsTableHarmonizer(ImageJ context) {
		displayService = context.getService(DisplayService.class);
	}

	// -- ResultsTableHarmonizer methods --

	public void setIJ1ResultsTable() {
		TableDisplay display = displayService.getActiveDisplay(TableDisplay.class);
		ResultsTable table = getFirstResultsTable(display);
		if (table == null) {
			Analyzer.setResultsTable(null);
			return;
		}
		ij.measure.ResultsTable ij1Table = new ij.measure.ResultsTable();
		ij1Table.setDefaultHeadings();
		for (int r = 0; r < table.getRowCount(); r++) {
			ij1Table.incrementCounter();
			ij1Table.setLabel(table.getRowHeader(r), r);
			for (int c = 0; c < table.getColumnCount(); c++) {
				double value = table.get(c, r);
				ij1Table.setValue(table.getColumnHeader(c), r, value);
			}
		}
		IJ.getTextPanel(); // HACK - force IJ1 to append data
		Analyzer.setResultsTable(ij1Table);
	}

	public void setIJ2ResultsTable() {
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
			for (int i = 0; i < table.getRowCount(); i++) {
				table.removeRow(table.getRowCount() - 1);
			}
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
				table.addColumn(ij1Table.getColumnHeading(c));
			}
		}
		for (int r = 0; r < ij1Table.getCounter(); r++) {
			table.addRow(ij1Table.getLabel(r));
			for (int ij2Col = 0, c = 0; c <= ij1Table.getLastColumn(); c++) {
				if (ij1Table.columnExists(c)) {
					double value = ij1Table.getValueAsDouble(c, r);
					table.setValue(ij2Col++, r, value);
				}
			}
		}

		// close IJ1's table
		TextWindow window = ij.measure.ResultsTable.getResultsWindow();
		if (window != null) window.close(false);

		// display results in IJ2 as appropriate
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
