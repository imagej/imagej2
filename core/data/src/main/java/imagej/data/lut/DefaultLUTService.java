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

import imagej.command.CommandInfo;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.display.DisplayService;
import imagej.menu.MenuConstants;
import imagej.module.ModuleInfo;
import imagej.module.ModuleService;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.scijava.MenuEntry;
import org.scijava.MenuPath;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

// Attribution: Much of this code was adapted from ImageJ 1.x LutLoader class
// courtesy of Wayne Rasband.

//TODO - DefaultRecentFileService, DefaultWindowService, and DefaultLUTService
//all build menus dynamically (see createInfo()). We may be able to abstract a
//helper class out of these that can be used by them and future services.

/**
 * The DefaultLUTService loads {@link ColorTable}s from files (hosted locally or
 * externally).
 * 
 * @author Barry DeZonia
 * @author Wayne Rasband
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultLUTService extends AbstractService implements LUTService {

	// -- Constants --

	private static final int RAMP_WIDTH = 256;
	private static final int RAMP_HEIGHT = 32;

	/** 640K should be more than enough for any LUT! */
	private static final int MAX_LUT_LENGTH = 640 * 1024;

	// -- Parameters --

	@Parameter
	private LogService logService;

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private DisplayService displayService;

	@Parameter
	private ImageDisplayService imageDisplayService;

	// -- LUTService methods --

	@Override
	public boolean isLUT(final File file) {
		return file.getAbsolutePath().toLowerCase().endsWith(".lut");
	}

	@Override
	public ColorTable loadLUT(final File file) throws IOException {
		final FileInputStream is = new FileInputStream(file);
		final int length = (int) Math.min(file.length(), Integer.MAX_VALUE);
		final ColorTable colorTable;
		try {
			colorTable = loadLUT(is, length);
		}
		finally {
			is.close();
		}
		return colorTable;
	}

	@Override
	public ColorTable loadLUT(final URL url) throws IOException {
		final InputStream is = url.openStream();
		final ColorTable colorTable;
		try {
			colorTable = loadLUT(is);
		}
		finally {
			is.close();
		}
		return colorTable;
	}

	@Override
	public ColorTable loadLUT(final InputStream is) throws IOException {
		// read bytes from input stream, up to maximum LUT length
		final byte[] bytes = new byte[MAX_LUT_LENGTH];
		int length = 0;
		while (true) {
			final int r = is.read(bytes, length, bytes.length - length);
			if (r < 0) break; // eof
			length += r;
		}

		return loadLUT(new ByteArrayInputStream(bytes, 0, length), length);
	}

	@Override
	public ColorTable loadLUT(final InputStream is, final int length)
		throws IOException
	{
		if (length > 768) {
			// attempt to read NIH Image LUT
			final ColorTable lut = nihImageBinaryLUT(is);
			if (lut != null) return lut;
		}
		if (length == 0 || length == 768 || length == 970) {
			// attempt to read raw LUT
			final ColorTable lut = legacyBinaryLUT(is);
			if (lut != null) return lut;
		}
		if (length > 768) {
			final ColorTable lut = legacyTextLUT(is);
			if (lut != null) return lut;
		}
		return modernLUT(is);
	}

	// -- Service methods --

	@Override
	public void initialize() {
		final Map<String, URL> luts = new LUTFinder().findLUTs();
		final List<ModuleInfo> modules = new ArrayList<ModuleInfo>();
		for (final String key : luts.keySet()) {
			modules.add(createInfo(key, luts.get(key)));
		}

		// register the modules with the module service
		moduleService.addModules(modules);
	}

	// -- private initialization code --

	private ModuleInfo createInfo(final String key, final URL url) {
		// set menu path
		final String[] subPaths = key.split("/");
		final MenuPath menuPath = new MenuPath();
		menuPath.add(new MenuEntry(MenuConstants.IMAGE_LABEL));
		menuPath.add(new MenuEntry("Lookup Tables"));
		for (int i = 0; i < subPaths.length - 1; i++) {
			menuPath.add(new MenuEntry(subPaths[i]));
		}
		final MenuEntry leaf =
			new MenuEntry(tableName(subPaths[subPaths.length - 1]));
		leaf.setWeight(50); // set menu position: TODO - do this properly
		menuPath.add(leaf);

		// hard code path to open as a preset
		final HashMap<String, Object> presets = new HashMap<String, Object>();
		presets.put("tableURL", url);
		// and create the command info
		final CommandInfo info =
			new CommandInfo("imagej.core.commands.misc.ApplyLookupTable");
		info.setPresets(presets);
		info.setMenuPath(menuPath);
		// use the default icon
		// info.setIconPath(iconPath);

		return info;
	}

	private String tableName(final String filename) {
		final int ext = filename.lastIndexOf(".lut");
		return filename.substring(0, ext);
	}

	// -- private modern LUT loading method --

	private ColorTable modernLUT(final InputStream is) throws IOException {
		// TODO : support some new more flexible format
		return null;
	}

	// -- private legacy LUT loading methods --

	// note: adapted from IJ1 LutLoader class

	private ColorTable nihImageBinaryLUT(final InputStream is)
		throws IOException
	{
		return oldBinaryLUT(false, is);
	}

	private ColorTable legacyBinaryLUT(final InputStream is) throws IOException {
		return oldBinaryLUT(true, is);
	}

	private ColorTable legacyTextLUT(final InputStream is) throws IOException {
		return null;
		// CTR FIXME: TableLoader requires a URL, which is awkward.
//		ResultsTable table = new TableLoader().valuesFromTextFile(is);
//		if (table == null) return null;
//		byte[] reds = new byte[256];
//		byte[] greens = new byte[256];
//		byte[] blues = new byte[256];
//		int cols = table.getColumnCount();
//		int rows = table.getRowCount();
//		if (cols < 3 || cols > 4 || rows < 256 || rows > 258) return null;
//		int x = cols == 4 ? 1 : 0;
//		int y = rows > 256 ? 1 : 0;
//		for (int r = 0; r < 256; r++) {
//			reds[r] = (byte) table.getValue(x + 0, y + r);
//			greens[r] = (byte) table.getValue(x + 1, y + r);
//			blues[r] = (byte) table.getValue(x + 2, y + r);
//		}
//		return new ColorTable8(reds, greens, blues);
	}

	private ColorTable oldBinaryLUT(final boolean raw, final InputStream is)
		throws IOException
	{
		final DataInputStream f = new DataInputStream(is);
		int nColors = 256;
		if (!raw) {
			// attempt to read 32 byte NIH Image LUT header
			final int id = f.readInt();
			if (id != 1229147980) { // 'ICOL'
				f.close();
				return null;
			}
			f.readShort(); // version
			nColors = f.readShort();
			f.readShort(); // start
			f.readShort(); // end
			f.readLong(); // fill1
			f.readLong(); // fill2
			f.readInt(); // filler
		}
		final byte[] reds = new byte[256];
		final byte[] greens = new byte[256];
		final byte[] blues = new byte[256];
		f.read(reds, 0, nColors);
		f.read(greens, 0, nColors);
		f.read(blues, 0, nColors);
		if (nColors < 256) interpolate(reds, greens, blues, nColors);
		f.close();
		return new ColorTable8(reds, greens, blues);
	}

	private void interpolate(final byte[] reds, final byte[] greens,
		final byte[] blues, final int nColors)
	{
		final byte[] r = new byte[nColors];
		final byte[] g = new byte[nColors];
		final byte[] b = new byte[nColors];
		System.arraycopy(reds, 0, r, 0, nColors);
		System.arraycopy(greens, 0, g, 0, nColors);
		System.arraycopy(blues, 0, b, 0, nColors);
		final double scale = nColors / 256.0;
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

	@Override
	public ImageDisplay createDisplay(final String title,
		final ColorTable colorTable)
	{
		final Dataset dataset =
			datasetService.create(new UnsignedByteType(), new long[] { RAMP_WIDTH,
				RAMP_HEIGHT }, title, new AxisType[] { Axes.X, Axes.Y });
		rampFill(dataset);
		// TODO - is this papering over a bug in the dataset/imgplus code?
		if (dataset.getColorTableCount() == 0) dataset.initializeColorTables(1);
		dataset.setColorTable(colorTable, 0);
		// CTR FIXME: May not be safe.
		return (ImageDisplay) displayService.createDisplay(dataset);
	}

	@Override
	public void
		applyLUT(final ColorTable colorTable, final ImageDisplay display)
	{
		final DatasetView view = imageDisplayService.getActiveDatasetView(display);
		if (view == null) return;
		final int channel = view.getIntPosition(Axes.CHANNEL);
		view.setColorTable(colorTable, channel);
		display.update();
	}

	// -- Helper methods --

	private void rampFill(final Dataset dataset) {
		final RandomAccess<? extends RealType<?>> accessor =
			dataset.getImgPlus().randomAccess();
		for (int x = 0; x < RAMP_WIDTH; x++) {
			accessor.setPosition(x, 0);
			for (int y = 0; y < RAMP_HEIGHT; y++) {
				accessor.setPosition(y, 1);
				accessor.get().setReal(x);
			}
		}
	}

}
