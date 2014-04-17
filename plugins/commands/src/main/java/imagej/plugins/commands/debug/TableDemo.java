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

package imagej.plugins.commands.debug;

import imagej.command.Command;
import imagej.data.table.DefaultGenericTable;
import imagej.data.table.DefaultResultsTable;
import imagej.data.table.GenericTable;
import imagej.data.table.ResultsTable;

import org.scijava.ItemIO;
import org.scijava.app.StatusService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.MersenneTwisterFast;

/**
 * A demonstration of the {@link imagej.data.table} package.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, menuPath = "Plugins>Sandbox>Table Demo", headless = true)
public class TableDemo implements Command {

	// FIXME: Migrate this to imagej-tutorials!

	@Parameter
	private StatusService statusService;

	@Parameter(label = "Paul Molitor Baseball Statistics", type = ItemIO.OUTPUT)
	private ResultsTable baseball;

	@Parameter(label = "Big Table", type = ItemIO.OUTPUT)
	private ResultsTable big;

	@Parameter(label = "Spreadsheet", type = ItemIO.OUTPUT)
	private GenericTable spreadsheet;

	@Override
	public void run() {
		statusService.showStatus("Creating a small table...");
		createBaseballTable();

		// create a larger table with 10K elements
		statusService.showStatus("Creating a large table...");
		createBigTable();

		// create a simple spreadsheet (with string data values)
		statusService.showStatus("Creating spreadsheet...");
		createSpreadsheet();

		statusService.clearStatus();
	}

	private void createBaseballTable() {
		final double[][] data = {
			{1978, 21, .273},
			{1979, 22, .322},
			{1980, 23, .304},
			{1981, 24, .267},
			{1982, 25, .302},
			{1983, 26, .270},
			{1984, 27, .217},
			{1985, 28, .297},
			{1986, 29, .281},
			{1987, 30, .353},
			{1988, 31, .312},
			{1989, 32, .315},
			{1990, 33, .285},
			{1991, 34, .325},
			{1992, 35, .320},
			{1993, 36, .332},
			{1994, 37, .341},
			{1995, 38, .270},
			{1996, 39, .341},
			{1997, 40, .305},
			{1998, 41, .281},
		};
		baseball = new DefaultResultsTable(data[0].length, data.length);
		baseball.setColumnHeader(0, "Year");
		baseball.setColumnHeader(1, "Age");
		baseball.setColumnHeader(2, "BA");
		baseball.setRowHeader(9, "Best");
		for (int row = 0; row < data.length; row++) {
			for (int col = 0; col < data[row].length; col++) {
				baseball.setValue(col, row, data[row][col]);
			}
		}
	}

	private void createBigTable() {
		final int colCount = 10, rowCount = 10000;
		big = new DefaultResultsTable(colCount, rowCount);
		for (int col = 0; col < colCount; col++) {
			statusService.showProgress(col, colCount);
			for (int row = 0; row < rowCount; row++) {
				big.setValue(col, row, row + col);
			}
		}
	}

	private void createSpreadsheet() {
		spreadsheet = new DefaultGenericTable(26, 50);
		final MersenneTwisterFast rand = new MersenneTwisterFast();
		for (int col = 0; col < spreadsheet.getColumnCount(); col++) {
			final char letter = (char) ('A' + col);
			spreadsheet.setColumnHeader(col, "" + letter);
			for (int row = 0; row < spreadsheet.getRowCount(); row++) {
				final String data = randomWord(rand, 2, 4);
				spreadsheet.set(col, row, data);
			}
		}
	}

	/**
	 * Generates a random word between {@code min} and {@code max} characters
	 * long, inclusive.
	 */
	private String randomWord(final MersenneTwisterFast rand, final int min,
		final int max)
	{
		final StringBuilder sb = new StringBuilder();
		final int length = rand.nextInt(max - min + 1) + min;
		for (int i = 0; i< length; i++) {
			final char letter = (char) ('a' + rand.nextInt(26));
			sb.append(letter);
		}
		return sb.toString();
	}

}
