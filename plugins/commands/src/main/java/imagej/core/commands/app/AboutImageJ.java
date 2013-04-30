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

package imagej.core.commands.app;

import imagej.app.ImageJApp;
import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.DrawingTool;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.io.IOService;
import imagej.menu.MenuConstants;
import imagej.render.RenderingService;
import imagej.render.TextRenderer.TextJustification;
import imagej.util.AppUtils;
import imagej.util.ColorRGB;
import imagej.util.Colors;
import imagej.util.MersenneTwisterFast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import ome.scifio.io.img.ImgIOException;

import org.scijava.ItemIO;
import org.scijava.app.App;
import org.scijava.app.AppService;
import org.scijava.log.LogService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Manifest;

// TODO
//   Have imageX.ext image file and imageX.ext.txt metadata files
//     Metadata file lists some useful info
//     - image attribution
//     - best color to render text in
//     - recommended font size
//   This plugin loads a random image and displays info including
//     attribution text

/**
 * Displays information and credits about the ImageJ software. Note that some of
 * this code was adapted from code written by Wayne Rasband for ImageJ1.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, label = "About ImageJ...",
	iconPath = "/icons/commands/information.png", menu = {
		@Menu(label = MenuConstants.HELP_LABEL,
			weight = MenuConstants.HELP_WEIGHT,
			mnemonic = MenuConstants.HELP_MNEMONIC),
		@Menu(label = "About ImageJ...", weight = 43) }, headless = true)
public class AboutImageJ extends ContextCommand {

	// -- constants --

	private static final long ONE_K_BYTES = 1024;
	private static final long ONE_M_BYTES = ONE_K_BYTES * ONE_K_BYTES;

	// -- parameters --

	@Parameter
	private LogService log;

	@Parameter
	private AppService appService;

	@Parameter
	private DatasetService dataSrv;

	@Parameter
	private DisplayService dispSrv;

	@Parameter
	private IOService ioService;
	
	@Parameter
	private RenderingService rendSrv;
	
	@Parameter(type = ItemIO.OUTPUT)
	private Display<?> display;

	// -- instance variables that are not parameters --

	private final List<String> attributionStrings = new LinkedList<String>();
	private ColorRGB textColor = Colors.YELLOW;
	private final ColorRGB outlineColor = Colors.BLACK;
	private int largestFontSize = 35;
	private ChannelCollection textChannels = null;
	private ChannelCollection outlineChannels = null;

	// -- public interface --

	@Override
	public void run() {
		final Dataset dataset = createDataset();
		drawTextOverImage(dataset);
		final String title = getApp().getTitle();
		display = dispSrv.createDisplay("About " + title, dataset);
	}

	public Display<?> getDisplay() {
		return display;
	}

	// -- private helpers --

	/**
	 * Returns a merged color Dataset as a backdrop.
	 */
	private Dataset createDataset() {
		final File imageFile = getRandomAboutImagePath();

		final String title = "About " + getAppString();

		Dataset ds = null;
		try {
			ds = ioService.loadDataset(imageFile.getAbsolutePath());
		}
		catch (final ImgIOException e) {
			log.error(e);
		}
		catch (final IncompatibleTypeException e) {
			log.error(e);
		}

		// did we successfully load a background image?
		if (ds != null) {
			// yes we did - inspect it
			boolean validImage = true;
			validImage &= (ds.numDimensions() == 3);
			// Too restrictive? Ran into images where 3rd axis is mislabeled
			// validImage &= (ds.getAxisIndex(Axes.CHANNEL) == 2);
			validImage &= (ds.getImgPlus().firstElement().getBitsPerPixel() == 8);
			validImage &= (ds.isInteger());
			validImage &= (!ds.isSigned());
			if (validImage) {
				loadAttributes(imageFile);
			}
			else {
				ds = null;
			}
		}

		// Did we fail to load a valid dataset?
		if (ds == null) {
			log.warn("Could not load a 3 channel unsigned 8 bit image as backdrop");
			// make a black 3 channel 8-bit unsigned background image.
			ds =
				dataSrv.create(new long[] { 500, 500, 3 }, title, new AxisType[] {
					Axes.X, Axes.Y, Axes.CHANNEL }, 8, false, false);
		}

		ds.setName(title);
		ds.setRGBMerged(true);

		return ds;
	}

	/**
	 * Returns the path to a backdrop image file. Chooses randomly from those
	 * present in the "about" folder off the ImageJ base directory.
	 * 
	 * @return file path of the chosen image
	 */
	private File getRandomAboutImagePath() {
		final File aboutDir = new File(AppUtils.getBaseDirectory(), "about");
		if (!aboutDir.exists()) {
			// no "about" folder found
			log.warn("About folder '" + aboutDir.getPath() + "' does not exist.");
			return null;
		}

		// get list of available image files
		final File[] aboutFiles = aboutDir.listFiles(new java.io.FileFilter() {

			@Override
			public boolean accept(final File pathname) {
				// ignore .txt metadata files
				return !pathname.getName().toLowerCase().endsWith(".txt");
			}
		});

		// choose a random image file
		final MersenneTwisterFast rng = new MersenneTwisterFast();
		final int index = rng.nextInt(aboutFiles.length);
		return aboutFiles[index];
	}

	/**
	 * Draws the textual information over a given merged color Dataset.
	 */
	private void drawTextOverImage(final Dataset ds) {
		textChannels = new ChannelCollection(textColor);
		outlineChannels = new ChannelCollection(outlineColor);
		final DrawingTool tool = new DrawingTool(ds, rendSrv);
		tool.setUAxis(0);
		tool.setVAxis(1);
		final long width = ds.dimension(0);
		final long x = width / 2;
		long y = 50;
		tool.setTextAntialiasing(true);
		// tool.setTextOutlineWidth(5);
		tool.setFontSize(largestFontSize);
		drawOutlinedText(tool, x, y, getAppString(),
			TextJustification.CENTER, textChannels, outlineChannels);
		y += 5 * tool.getFontSize() / 4;
		tool.setFontSize((int) Math.round(0.6 * largestFontSize));
		for (final String line : getTextBlock()) {
			drawOutlinedText(tool, x, y, line, TextJustification.CENTER,
				textChannels, outlineChannels);
			y += 5 * tool.getFontSize() / 4;
		}
	}

	/**
	 * Draws a text string and outline in two different sets of fill values.
	 */
	private void drawOutlinedText(final DrawingTool tool, final long x,
		final long y, final String text, final TextJustification just,
		final ChannelCollection textValues, final ChannelCollection outlineValues)
	{
		tool.setChannels(outlineValues);
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0) continue;
				tool.drawText(x + dx, y + dy, text, just);
			}
		}
		tool.setChannels(textValues);
		tool.drawText(x, y, text, just);
	}

	/**
	 * Returns the paragraph of textual information to display over the backdrop
	 * image.
	 */
	private List<String> getTextBlock() {
		final Manifest mft = getApp().getManifest();

		final LinkedList<String> stringList = new LinkedList<String>();
		if (mft != null) {
			stringList.add("Build: " + mft.getImplementationBuild());
			stringList.add("Date: " + mft.getImplementationDate());
		}
		stringList.add("Open source image processing software");
		final int year = Calendar.getInstance().get(Calendar.YEAR);
		stringList.add("Copyright 2010 - " + year);
		stringList.add("http://developer.imagej.net/");
		stringList.add(javaInfo());
		stringList.add(memoryInfo());
		stringList.addAll(attributionStrings);

		return stringList;
	}

	/**
	 * Returns a string showing java platform and version information.
	 */
	private String javaInfo() {
		return "Java " + System.getProperty("java.version") +
			(is64Bit() ? " (64-bit)" : " (32-bit)");
	}

	// NB - this code used to mirror IJ1 more closely. It figured max memory from
	// the setting in OptionsMemoryAndThreads. But then it was possible to have
	// different reports when double clicking the status area vs. running the
	// about dialog. This code now matches SwingStatusBar's method.

	// TODO We should make a centralized memory reporter that depends upon
	// ij-options that this class and SwingStatusBar use. Then we can decide
	// whether to emulate IJ1's memory option constraint. Or if we'll instead
	// rely on launcher to constrain memory. Either way the two will be consistent

	/**
	 * Returns a string showing used and available memory information.
	 */
	private String memoryInfo() {
		final long maxMem = Runtime.getRuntime().maxMemory();
		final long totalMem = Runtime.getRuntime().totalMemory();
		final long freeMem = Runtime.getRuntime().freeMemory();
		final long usedMem = totalMem - freeMem;
		final long maxMB = maxMem / ONE_M_BYTES;
		String inUseStr =
			(usedMem < 10000 * ONE_K_BYTES) ? (usedMem / ONE_K_BYTES + "K")
				: (usedMem / ONE_M_BYTES + "MB");
		if (maxMem > 0L) {
			final long percent = usedMem * 100 / maxMem;
			inUseStr +=
				" of " + maxMB + "MB (" + (percent < 1 ? "<1" : percent) + "%)";
		}
		return inUseStr;
	}

	/** Returns true if ImageJ is running a 64-bit version of Java. */
	private boolean is64Bit() {
		final String osarch = System.getProperty("os.arch");
		return osarch != null && osarch.indexOf("64") != -1;
	}

	/**
	 * Given an image file name (i.e. filename.ext) loads associated attributes
	 * from a text file (filename.ext.txt) if possible.
	 */
	private void loadAttributes(final File baseFile) {
		final String fileName = baseFile.getAbsolutePath() + ".txt";
		final File file = new File(fileName);
		if (file.exists()) {
			final Pattern attributionPattern =
				Pattern.compile("attribution\\s+(.*)");
			final Pattern colorPattern =
				Pattern.compile("color\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)");
			final Pattern fontsizePattern =
				Pattern.compile("fontsize\\s+([1-9][0-9]*)");
			try {
				final FileInputStream fstream = new FileInputStream(file);
				final DataInputStream in = new DataInputStream(fstream);
				final BufferedReader br =
					new BufferedReader(new InputStreamReader(in));
				String strLine;
				// Read File Line By Line
				while ((strLine = br.readLine()) != null) {
					final Matcher attributionMatcher =
						attributionPattern.matcher(strLine);
					if (attributionMatcher.matches()) {
						attributionStrings.add(attributionMatcher.group(1).trim());
					}
					final Matcher colorMatcher = colorPattern.matcher(strLine);
					if (colorMatcher.matches()) {
						try {
							final int r = Integer.parseInt(colorMatcher.group(1));
							final int g = Integer.parseInt(colorMatcher.group(2));
							final int b = Integer.parseInt(colorMatcher.group(3));
							textColor = new ColorRGB(r, g, b);
						}
						catch (final Exception e) {
							// do nothing
						}
					}
					final Matcher fontsizeMatcher = fontsizePattern.matcher(strLine);
					if (fontsizeMatcher.matches()) {
						try {
							largestFontSize = Integer.parseInt(fontsizeMatcher.group(1));
						}
						catch (final Exception e) {
							// do nothing
						}
					}
				}
				// Close the input stream
				in.close();

			}
			catch (final Exception e) {
				// do nothing
			}
		}
	}

	private String getAppString() {
		return getApp().getTitle() + " " + getApp().getVersion();
	}

	private App getApp() {
		return appService.getApp(ImageJApp.NAME);
	}

}
