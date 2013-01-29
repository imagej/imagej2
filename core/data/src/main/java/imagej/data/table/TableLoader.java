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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.URL;

// note: adapted from Wayne Rasband's IJ1 TextReader class

/**
 * Loads a text file containing comma separated values into a
 * {@link ResultsTable}.
 * 
 * @author Barry DeZonia
 * @author Wayne Rasband
 */
public class TableLoader {

	// -- instance variables --

	private int rows, cols;

	// -- private legacy text file support methods --

	/**
	 * Loads the values of a table stored in a text file as a ResultsTable.
	 * 
	 * @param url The url (as a URL) of the text table
	 * @return A ResultsTable containing the values (and headers)
	 * @throws IOException
	 */
	public ResultsTable valuesFromTextFile(URL url) throws IOException {
		ResultsTable values = null;
		countRowsAndCols(url);
		if (rows == 0) return null;
		values = new DefaultResultsTable(cols, rows);
		read(url, values);
		int firstRowNaNCount = 0;
		for (int i = 0; i < cols; i++) {
			if (Double.isNaN(values.getValue(i, 0))) firstRowNaNCount++;
		}
		if (firstRowNaNCount == cols) { // assume first row is header
			// throw away first row of non-values
			rows--;
			ResultsTable oldValues = values;
			values = new DefaultResultsTable(cols, rows);
			for (int c = 0; c < cols; c++) {
				String colHeader = oldValues.getColumnHeader(c);
				values.setColumnHeader(c, colHeader);
			}
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < cols; col++) {
					double val = oldValues.getValue(col, row + 1);
					values.setValue(col, row, val);
				}
			}
		}
		return values;
	}

	/**
	 * Loads the values of a table stored in a text file as a ResultsTable.
	 * 
	 * @param urlString The url (as a string) of the text table
	 * @return A ResultsTable containing the values (and headers)
	 * @throws IOException
	 */
	public ResultsTable valuesFromTextFile(String urlString) throws IOException {
		return valuesFromTextFile(new URL(urlString));
	}

	/**
	 * Loads the values of a table stored in a text file as a ResultsTable.
	 * 
	 * @param file The File containing the text table
	 * @return A ResultsTable containing the values (and headers)
	 * @throws IOException
	 */
	public ResultsTable valuesFromTextFile(File file) throws IOException {
		return valuesFromTextFile("file://" + file.getAbsolutePath());
	}

	// -- private helpers -

	private void countRowsAndCols(URL url) throws IOException {
		InputStream str = url.openStream();
		Reader r = new BufferedReader(new InputStreamReader(str));
		StreamTokenizer tok = new StreamTokenizer(r);
		tok.resetSyntax();
		tok.wordChars(43, 43);
		tok.wordChars(45, 127);
		tok.whitespaceChars(0, 42);
		tok.whitespaceChars(44, 44);
		// tok.wordChars(33, 127);
		// tok.whitespaceChars(0, ' ');
		tok.whitespaceChars(128, 255);
		tok.eolIsSignificant(true);

		int words = 0, wordsPrevLine = 0;
		while (tok.nextToken() != StreamTokenizer.TT_EOF) {
			switch (tok.ttype) {
				case StreamTokenizer.TT_EOL:
					rows++;
					if (words == 0) rows--; // ignore empty lines
					if (rows == 1 && words > 0) cols = words;
					if (rows > 1 && words != 0 && words != wordsPrevLine) {
						throw new IOException("Line " + rows +
							" is not the same length as the first line.");
					}
					if (words != 0) wordsPrevLine = words;
					words = 0;
					break;
				case StreamTokenizer.TT_WORD:
					words++;
					break;
			}
		}
		if (words == cols) rows++; // last line does not end with EOL
	}

	private void read(URL url, ResultsTable values) throws IOException {
		InputStream str = url.openStream();
		Reader r = new BufferedReader(new InputStreamReader(str));
		StreamTokenizer tok = new StreamTokenizer(r);
		tok.resetSyntax();
		tok.wordChars(43, 43);
		tok.wordChars(45, 127);
		tok.whitespaceChars(0, 42);
		tok.whitespaceChars(44, 44);
		// tok.wordChars(33, 127);
		// tok.whitespaceChars(0, ' ');
		tok.whitespaceChars(128, 255);
		// tok.parseNumbers();

		int row = 0, col = 0;
		while (tok.nextToken() != StreamTokenizer.TT_EOF) {
			if (tok.ttype == StreamTokenizer.TT_WORD) {
				double value;
				try {
					value = Double.parseDouble(tok.sval);
				}
				catch (NumberFormatException e) {
					value = Double.NaN;
					if (row == 0) values.setColumnHeader(col, tok.sval);
				}
				values.setValue(col, row, value);
				col++;
				if (col == cols) {
					row++;
					col = 0;
				}
			}
		}
	}
}
