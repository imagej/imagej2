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

import imagej.MenuEntry;
import imagej.MenuPath;
import imagej.command.CommandInfo;
import imagej.data.table.ResultsTable;
import imagej.data.table.TableLoader;
import imagej.log.LogService;
import imagej.menu.MenuConstants;
import imagej.module.ModuleInfo;
import imagej.module.ModuleService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.AppUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;

// Attribution: Much of this code was adapted from ImageJ 1.x LutLoader class
// courtesy of Wayne Rasband.

//TODO - DefaultRecentFileService, DefaultWindowService, and DefaultLutService
//all build menus dynamically (see createInfo()). We may be able to abstract a
//helper class out of these that can be used by them and future services.

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

	@Parameter
	private ModuleService moduleService;

	// -- instance variables --

	private static final String LUT_DIRECTORY = AppUtils.getBaseDirectory() +
		File.separator + "luts";

	// -- LutService methods --

	/**
	 * Loads a {@link ColorTable} from a url (represented as a string).
	 * 
	 * @param urlString The url (as a String) where the color table file is found.
	 * @return The color table loaded from the given url.
	 */
	@Override
	public ColorTable loadLut(String urlString) {
		try {
			return loadLut(new URL(urlString));
		}
		catch (Exception e) {
			logService.error(e);
			return null;
		}
	}

	/**
	 * Loads a {@link ColorTable} from a url (represented as a URL).
	 * 
	 * @param url The url (as a URL) where the color table file is found.
	 * @return The color table loaded from the given url.
	 */
	@Override
	public ColorTable loadLut(URL url) {
		ColorTable table = null;
		try {
			table = openNihImageBinaryLut(url);
			if (table == null) table = openLegacyImageJBinaryLut(url);
			if (table == null) table = openLegacyImageJTextLut(url);
			if (table == null) table = openModernImageJLut(url);
		}
		catch (IOException e) {
			logService.error(e.getMessage());
		}
		return table;
	}

	@Override
	public void initialize() {
		List<String> filenames = findLuts();
		HashMap<String, ModuleInfo> modules = new HashMap<String, ModuleInfo>();
		for (final String filename : filenames) {
			modules.put(filename, createInfo(filename));
		}

		// register the modules with the module service
		moduleService.addModules(modules.values());
	}

	// -- private initialization code --

	private ModuleInfo createInfo(final String filename) {
		final CommandInfo info =
			new CommandInfo("imagej.core.commands.misc.ApplyLookupTablePlugin");

		// hard code path to open as a preset
		final HashMap<String, Object> presets = new HashMap<String, Object>();
		presets.put("tableURL", "file://" + filename);
		info.setPresets(presets);

		// set menu path
		String shortenedName = nameBeyondBase(filename);
		String[] subPaths = shortenedName.split(File.separator);
		final MenuPath menuPath = new MenuPath();
		menuPath.add(new MenuEntry(MenuConstants.IMAGE_LABEL));
		menuPath.add(new MenuEntry("Lookup Tables"));
		for (int i = 0; i < subPaths.length - 1; i++) {
			menuPath.add(new MenuEntry(subPaths[i]));
		}
		final MenuEntry leaf = new MenuEntry(tableName(filename));
		menuPath.add(leaf);
		info.setMenuPath(menuPath);

		// set menu position
		leaf.setWeight(50); // TODO - do this properly

		// use the default icon
		// info.setIconPath(iconPath);

		return info;
	}

	private String nameBeyondBase(String path) {
		int start = path.indexOf(LUT_DIRECTORY);
		return path.substring(start + LUT_DIRECTORY.length() + 1);
	}

	private String tableName(final String path) {
		int lastSlash = path.lastIndexOf(File.separator);
		int ext = path.lastIndexOf(".");
		int start = lastSlash + 1;
		int end = (ext == -1) ? path.length() : ext;
		return path.substring(start, end);
	}

	private List<String> findLuts() {
		ArrayList<String> filenames = new ArrayList<String>();
		find(LUT_DIRECTORY, filenames);
		return filenames;
	}

	private void find(String dirName, List<String> filenameCollection) {
		File f = new File(dirName);
		if (!f.isDirectory()) return;
		String[] files = f.list();
		for (String file : files) {
			StringBuilder builder = new StringBuilder();
			builder.append(dirName);
			builder.append(File.separator);
			builder.append(file);
			String fullName = builder.toString();
			if (file.endsWith(".lut")) {
				filenameCollection.add(fullName);
			}
			else {
				if (file.equals(".")) continue;
				if (file.equals("..")) continue;
				f = new File(fullName);
				if (f.isDirectory()) find(fullName, filenameCollection);
			}
		}
	}

	// -- private modern lut loading method --

	private ColorTable openModernImageJLut(URL url) throws IOException {
		// TODO : support some new more flexible format
		return null;
	}

	// -- private legacy lut loading methods --

	// note: adapted from IJ1 LutLoader class

	private ColorTable8 openNihImageBinaryLut(URL url)
		throws IOException
	{
		return openOldBinaryLut(false, url);
	}

	private ColorTable8 openLegacyImageJBinaryLut(URL url)
		throws IOException
	{
		return openOldBinaryLut(true, url);
	}

	private ColorTable8 openLegacyImageJTextLut(URL url)
		throws IOException
	{
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

	private ColorTable8 openOldBinaryLut(boolean raw, URL url)
		throws IOException
	{
		InputStream is = url.openStream();
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
