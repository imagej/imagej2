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

package imagej.data.lut;

import imagej.data.table.ResultsTable;
import imagej.data.table.TableLoader;
import imagej.log.LogService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;

// Attribution: Much of this code was adapted from ImageJ 1.x LutLoader class
// courtesy of Wayne Rasband.

/**
 * The DefaultLutService loads {@link ColorTable}s from files (hosted locally or
 * externally).
 * 
 * @author Barry DeZonia
 * @author Wayne Rasband
 */
@Plugin(type = Service.class)
public class DefaultLutService extends AbstractService implements LutService {

	// -- Parameters --

	@Parameter
	private LogService logService;

	// -- LutService methods --

	/**
	 * Loads a {@link ColorTable} from a url (represented as a string).
	 * 
	 * @param urlString The url (as a String) where the color table file is found.
	 * @return The color table loaded from the given url.
	 */
	@Override
	public ColorTable loadLut(String urlString) {
		ColorTable table = null;
		try {
			table = openNihImageBinaryLut(urlString);
			if (table == null) table = openLegacyImageJBinaryLut(urlString);
			if (table == null) table = openLegacyImageJTextLut(urlString);
			if (table == null) table = openModernImageJLut(urlString);
		}
		catch (IOException e) {
			logService.error(e.getMessage());
		}
		return table;
	}

	/**
	 * Loads a {@link ColorTable} from a url (represented as a URL).
	 * 
	 * @param url The url (as a URL) where the color table file is found.
	 * @return The color table loaded from the given url.
	 */
	@Override
	public ColorTable loadLut(URL url) {
		return loadLut(url.toString()); // TODO - untested
	}

	// -- private modern lut loading method --

	private ColorTable openModernImageJLut(String url) throws IOException {
		// TODO : support some new more flexible format
		return null;
	}

	// -- private legacy lut loading methods --

	// note: adapted from IJ1 LutLoader class

	private ColorTable8 openNihImageBinaryLut(String url) throws IOException {
		return openOldBinaryLut(false, url);
	}

	private ColorTable8 openLegacyImageJBinaryLut(String url) throws IOException {
		return openOldBinaryLut(true, url);
	}

	private ColorTable8 openLegacyImageJTextLut(String url) throws IOException {
		ResultsTable table = new TableLoader().valuesFromTextFile(url);
		if (table == null) return null;
		byte[] reds = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues = new byte[256];
		int cols = table.getColumnCount();
		int rows = table.getRowCount();
		if (cols < 3 || cols > 4 || rows < 256 || rows > 258) return null;
		int x = cols == 4 ? 1 : 0;
		int y = rows > 256 ? 1 : 0;
		for (int r = 0; r < 256; r++) {
			reds[r] = (byte) table.getValue(x + 0, y + r);
			greens[r] = (byte) table.getValue(x + 1, y + r);
			blues[r] = (byte) table.getValue(x + 2, y + r);
		}
		return new ColorTable8(reds, greens, blues);
	}

	private ColorTable8 openOldBinaryLut(boolean raw, String url)
		throws IOException
	{
		InputStream is = new URL(url).openStream();
		DataInputStream f = new DataInputStream(is);
		int nColors = 256;
		if (!raw) {
			// attempt to read 32 byte NIH Image LUT header
			int id = f.readInt();
			if (id != 1229147980) { // 'ICOL'
				f.close();
				return null;
			}
			int version = f.readShort();
			nColors = f.readShort();
			int start = f.readShort();
			int end = f.readShort();
			long fill1 = f.readLong();
			long fill2 = f.readLong();
			int filler = f.readInt();
		}
		byte[] reds = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues = new byte[256];
		f.read(reds, 0, nColors);
		f.read(greens, 0, nColors);
		f.read(blues, 0, nColors);
		if (nColors < 256) interpolate(reds, greens, blues, nColors);
		f.close();
		return new ColorTable8(reds, greens, blues);
	}

	private void
		interpolate(byte[] reds, byte[] greens, byte[] blues, int nColors)
	{
		byte[] r = new byte[nColors];
		byte[] g = new byte[nColors];
		byte[] b = new byte[nColors];
		System.arraycopy(reds, 0, r, 0, nColors);
		System.arraycopy(greens, 0, g, 0, nColors);
		System.arraycopy(blues, 0, b, 0, nColors);
		double scale = nColors / 256.0;
		int i1, i2;
		double fraction;
		for (int i = 0; i < 256; i++) {
			i1 = (int) (i * scale);
			i2 = i1 + 1;
			if (i2 == nColors) i2 = nColors - 1;
			fraction = i * scale - i1;
			// IJ.write(i+" "+i1+" "+i2+" "+fraction);
			reds[i] =
				(byte) ((1.0 - fraction) * (r[i1] & 255) + fraction * (r[i2] & 255));
			greens[i] =
				(byte) ((1.0 - fraction) * (g[i1] & 255) + fraction * (g[i2] & 255));
			blues[i] =
				(byte) ((1.0 - fraction) * (b[i1] & 255) + fraction * (b[i2] & 255));
		}
	}


}
