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

package imagej.data.lut;

import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.service.IJService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.imglib2.display.ColorTable;

/**
 * The LUTService loads {@link ColorTable}s (i.e., <b>L</b>ook-<b>U</b>p
 * <b>T</b>ables or LUTs) from various data sources.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public interface LUTService extends IJService {

	/**
	 * Gets whether the given file can be parsed as a color table by
	 * {@link #loadLUT(File)}.
	 * <p>
	 * This method is heuristic in nature, and does not offer a guarantee that
	 * calling {@link #loadLUT(File)} will be successful. However, if it returns
	 * false, it is highly likely that the file is not parsable as a
	 * {@link ColorTable}.
	 * </p>
	 */
	boolean isLUT(File file);

	/**
	 * Loads a {@link ColorTable} from a {@link File}.
	 * 
	 * @param file The file from which the color table data will be read.
	 * @return The color table loaded from the given file.
	 * @throws IOException if there is a problem reading the color table
	 */
	ColorTable loadLUT(File file) throws IOException;

	/**
	 * Loads a {@link ColorTable} from a {@link URL}.
	 * 
	 * @param url The URL from which the color table data will be read.
	 * @return The color table loaded from the given URL.
	 * @throws IOException if there is a problem reading the color table
	 */
	ColorTable loadLUT(URL url) throws IOException;

	/**
	 * Loads a {@link ColorTable} from an input stream.
	 * <p>
	 * Does not close the input stream after reading the color table.
	 * </p>
	 * 
	 * @param is The stream from which the color table data will be read.
	 * @return The color table loaded from the given URL.
	 * @throws IOException if there is a problem reading the color table
	 */
	ColorTable loadLUT(InputStream is) throws IOException;

	/**
	 * Loads a {@link ColorTable} from an input stream with the given expected
	 * length.
	 * 
	 * @param is The stream from which the color table data will be read.
	 * @param length The expected length of the input stream.
	 * @return The color table loaded from the given URL.
	 * @throws IOException if there is a problem reading the color table
	 */
	ColorTable loadLUT(InputStream is, int length) throws IOException;

	/**
	 * Creates a new dataset showing the given {@link ColorTable} as a ramp.
	 * 
	 * @param title The title of the new dataset.
	 * @param colorTable The color table to use.
	 * @return The newly created dataset.
	 */
	Dataset createDataset(String title, ColorTable colorTable);

	/**
	 * Applies the given {@link ColorTable} to the active view of the specified
	 * {@link ImageDisplay}.
	 * 
	 * @param colorTable
	 * @param display
	 */
	void applyLUT(final ColorTable colorTable, final ImageDisplay display);

}
