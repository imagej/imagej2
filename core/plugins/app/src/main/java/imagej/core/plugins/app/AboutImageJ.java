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

import net.imglib2.Cursor;
import net.imglib2.img.ImgPlus;
import net.imglib2.io.ImgOpener;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import imagej.ImageJ;
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
import imagej.util.Colors;

/**
 * Display information and credits about the ImageJ software.
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
	DatasetService dataSrv;

	@Parameter
	DisplayService dispSrv;
	
	// -- public interface --
	
	@Override
	public void run() {
		final Dataset image = getData();
		drawTextOverImage(image);
		dispSrv.createDisplay("About ImageJ", image);
	}

	// -- private helpers --
	
	private Dataset getData() {
		final String title = "About ImageJ " + ImageJ.VERSION;

		final ImgPlus<T> img = getImage();
		
		// did we successfully load a background image?
		if (img != null) {
			// yes we did - return it
			final Dataset ds = dataSrv.create(img);
			ds.setName(title);
			return ds;
		}
		
		// We could not load a dataset. So make a rgb background image.
		final Dataset ds = dataSrv.create(
			new long[]{400,400,3} , title,
			new AxisType[]{Axes.X,Axes.Y,Axes.CHANNEL}, 8, false, false);
		ds.setRGBMerged(true);
		
		// And make it all white.
		final Cursor<? extends RealType<?>> cursor = ds.getImgPlus().cursor();
		while (cursor.hasNext()) {
			cursor.next().setReal(255);
		}
		
		return ds;
	}
	
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
	
	private URL getImageURL() {
		// TODO - cycle through one of many
		final String fname = "/images/image1.tif";  // NB - THIS PATH IS CORRECT 
		return getClass().getResource(fname);
	}
	
	private void drawTextOverImage(Dataset ds) {
		final DrawingTool tool = new DrawingTool(ds);
		tool.setUAxis(0);
		tool.setVAxis(1);
		final long width = ds.dimension(0);
		tool.setColorValue(Colors.YELLOW);
		tool.setGrayValue(0);
		final long x = width / 2;
		long y = 50;
		tool.setFontSize(18);
		tool.drawText(x,y,"ImageJ2 "+ImageJ.VERSION, TextJustification.CENTER);
		y += 5*tool.getFontSize()/4;
		tool.setFontSize(12);
		for (final String line : getTextBlock()) {
			tool.drawText(x,y,line, TextJustification.CENTER);
			y += 5*tool.getFontSize()/4;
		}
	}
	
	private String[] getTextBlock() {
		return new String[] {
			"Open source image processing software",
			"Copyright 2010, 2011, 2012",
			"developer.imagej.net"
		};
	}
}
