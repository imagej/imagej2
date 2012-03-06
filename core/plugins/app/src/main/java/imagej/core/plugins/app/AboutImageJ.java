//
// AboutImageJ.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.core.plugins.app;

import java.net.URL;

import net.imglib2.img.ImgPlus;
import net.imglib2.io.ImgOpener;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import imagej.ImageJ;
import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.DrawingTool;
import imagej.data.DrawingTool.TextJustification;
import imagej.ext.display.DisplayService;
import imagej.ext.menu.MenuConstants;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsService;
import imagej.options.plugins.OptionsMemoryAndThreads;
import imagej.util.Log;

/**
 * Display information and credits about the ImageJ2 software. Note that some
 * of this code was adapted from code written by Wayne Rasband for ImageJ.
 * 
 * @author Barry DeZonia
 */
@Plugin(iconPath = "/icons/plugins/information.png", menu = {
	@Menu(label = MenuConstants.HELP_LABEL, weight = MenuConstants.HELP_WEIGHT,
		mnemonic = MenuConstants.HELP_MNEMONIC),
	@Menu(label = "About ImageJ...", weight = 43) }, headless = true)
public class AboutImageJ<T extends RealType<T> & NativeType<T>>
	implements ImageJPlugin
{
	// -- parameters --

	@Parameter
	private ImageJ context;
	
	@Parameter
	private DatasetService dataSrv;

	@Parameter
	private DisplayService dispSrv;
	
	// -- public interface --
	
	@Override
	public void run() {
		final Dataset image = getData();
		drawTextOverImage(image);
		dispSrv.createDisplay("About ImageJ", image);
	}

	// -- private helpers --

	/**
	 * Returns a merged color Dataset as a backdrop.
	 */
	private Dataset getData() {
		
		final String title = "About ImageJ " + ImageJ.VERSION;

		Dataset ds = null;
		
		final ImgPlus<T> img = getImage();

		// did we successfully load a background image?
		if (img != null) {
			// yes we did - inspect it
			ds = dataSrv.create(img);
			boolean validImage = true;
			validImage &= (ds.numDimensions() == 3);
			validImage &= (ds.getAxisIndex(Axes.CHANNEL) == 2);
			validImage &= (ds.getImgPlus().firstElement().getBitsPerPixel() == 8);
			validImage &= (ds.isInteger());
			validImage &= (!ds.isSigned());
			if (!validImage) ds = null;
		}

		// Did we fail to load a valid dataset?
		if (ds == null) {
			Log.warn("Could not load a 3 channel unsigned 8 bit image as backdrop");
			// make a black 3 channel 8-bit unsigned background image.
			ds = dataSrv.create(
				new long[]{400,400,3} , title,
				new AxisType[]{Axes.X,Axes.Y,Axes.CHANNEL}, 8, false, false);
		}
		
		ds.setName(title);
		ds.setRGBMerged(true);
		
		return ds;
	}

	/**
	 * Loads an ImgPlus from a URL
	 */
	private ImgPlus<T> getImage() {
		final URL imageURL = getImageURL();
		if (imageURL == null) return null;
		try {
			final ImgOpener opener = new ImgOpener();
			// TODO - ImgOpener should be extended to handle URLs
			// Hack for now to get local file name
			final String urlName = imageURL.toString();
			final String filename = urlName.substring(5); // strip off "file:"
			return opener.openImg(filename);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the URL of a backdrop image
	 */
	private URL getImageURL() {
		// TODO - cycle through one of many
		final String fname = "/images/image1.tif";  // NB - THIS PATH IS CORRECT 
		return getClass().getResource(fname);
	}

	/**
	 * Draws the textual information over a given merged color Dataset
	 */
	private void drawTextOverImage(Dataset ds) {
		// make a yellow like set of channels
		final ChannelCollection chans = new ChannelCollection();
		chans.setChannelValue(0, 255);
		chans.setChannelValue(1, 255);
		chans.setChannelValue(2, 0);
		final DrawingTool tool = new DrawingTool(ds, chans);
		tool.setUAxis(0);
		tool.setVAxis(1);
		final long width = ds.dimension(0);
		final long x = width / 2;
		long y = 50;
		tool.setFontSize(20);
		tool.drawText(x,y,"ImageJ2 "+ImageJ.VERSION, TextJustification.CENTER);
		y += 5*tool.getFontSize()/4;
		tool.setFontSize(13);
		for (final String line : getTextBlock()) {
			tool.drawText(x,y,line, TextJustification.CENTER);
			y += 5*tool.getFontSize()/4;
		}
	}
	
	/**
	 * Returns the paragraph of textual information to display over the
	 * backdrop image
	 */
	private String[] getTextBlock() {
		return new String[] {
			"Open source image processing software",
			"Copyright 2010, 2011, 2012",
			"http://developer.imagej.net/",
			javaInfo(),
			memoryInfo()
		};
	}
	
	/**
	 * Returns a string showing java platform and version information
	 */
	private String javaInfo() {
		return "Java "+System.getProperty("java.version") +
				(is64Bit() ? " (64-bit)" : " (32-bit)");
	}
	
	/**
	 * Returns a string showing used and available memory information
	 */
	private String memoryInfo() {
		long inUse = currentMemory();
		String inUseStr = inUse<10000*1024?inUse/1024L+"K":inUse/1048576L+"MB";
		String maxStr="";
		long max = maxMemory();
		if (max>0L) {
			long percent = inUse * 100 / max;
			maxStr = " of "+max/1048576L+"MB ("+(percent<1 ? "<1" : percent) + "%)";
		}
		return inUseStr + maxStr;
	}
	
	/** Returns the amount of memory currently being used by ImageJ2. */
	private long currentMemory() {
		long freeMem = Runtime.getRuntime().freeMemory();
		long totMem = Runtime.getRuntime().totalMemory();
		return totMem-freeMem;
	}

	/** Returns the maximum amount of memory available to ImageJ2 or
		zero if ImageJ2 is unable to determine this limit. */
	private long maxMemory() {
		OptionsService srv = context.getService(OptionsService.class);
		OptionsMemoryAndThreads opts = srv.getOptions(OptionsMemoryAndThreads.class);
		long totMem = Runtime.getRuntime().totalMemory();
		long userMem = opts.getMaxMemory() * 1024L * 1024L;
		if (userMem > totMem)
			return totMem;
		return userMem;
	}

	/** Returns true if ImageJ2 is running a 64-bit version of Java. */
	private boolean is64Bit() {
		String osarch = System.getProperty("os.arch");
		return osarch!=null && osarch.indexOf("64")!=-1;
	}
}
