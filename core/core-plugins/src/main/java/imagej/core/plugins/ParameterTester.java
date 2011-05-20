//
// ParameterTester.java
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

package imagej.core.plugins;

import imagej.data.Dataset;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.plugin.PreviewPlugin;
import imagej.plugin.ui.WidgetStyle;
import imagej.util.ColorRGB;
import imagej.util.Log;

import java.io.File;
import java.math.BigInteger;

/**
 * Test plugin for verifying that many types of parameters work properly.
 * 
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins > Parameter Tester")
public class ParameterTester implements ImageJPlugin, PreviewPlugin {

	@Parameter(label = "boolean")
	private boolean pBoolean;

	@Parameter(label = "byte")
	private byte pByte;

	@Parameter(label = "double")
	private double pDouble;

	@Parameter(label = "float")
	private float pFloat;

	@Parameter(label = "int")
	private int pInt;

	@Parameter(label = "long")
	private long pLong;

	@Parameter(label = "short")
	private short pShort;

	@Parameter
	private BigInteger bigInteger;

	@Parameter(label = "char")
	private char pChar;

	@Parameter(label = "String")
	private String string;

	@Parameter(label = "multiple choice", choices = { "The", "quick", "brown",
		"fox", "jumps", "over", "the", "lazy", "dog" })
	private String choice;

	@Parameter
	private File file;

	@Parameter
	private ColorRGB color;

	@Parameter
	private Dataset dataset;

	@Parameter(label = "spinner", style = WidgetStyle.NUMBER_SPINNER, min = "0",
		max = "1000")
	private int spinnerNumber;

	@Parameter(label = "slider", style = WidgetStyle.NUMBER_SLIDER, min = "0",
		max = "1000")
	private int sliderNumber;

	@Parameter(label = "scroll bar", style = WidgetStyle.NUMBER_SCROLL_BAR,
		min = "0", max = "1000")
	private int scrollBarNumber;

	@Parameter(description = "Demonstrates preview functionality by " +
		"displaying this message in the ImageJ status bar", columns = 20)
	private String message = "Type a status message here.";

	@Override
	public void run() {
		Log.info("ParameterTester results:");
		Log.info("\tboolean = " + pBoolean);
		Log.info("\tbyte = " + pByte);
		Log.info("\tdouble = " + pDouble);
		Log.info("\tfloat = " + pFloat);
		Log.info("\tint = " + pInt);
		Log.info("\tlong = " + pLong);
		Log.info("\tshort = " + pShort);
		Log.info("\tBigInteger = " + bigInteger);
		Log.info("\tchar = " + "'" + pChar + "' [" + (int) pChar + "]");
		Log.info("\tString = " + string);
		Log.info("\tmultiple choice = " + choice);
		Log.info("\tDataset = " + dataset);
		Log.info("\tFile = " + file);
		Log.info("\tcolor = " + color);
		Log.info("\tspinner = " + spinnerNumber);
		Log.info("\tslider = " + sliderNumber);
		Log.info("\tscroll bar = " + scrollBarNumber);
		Log.info("\tmessage = " + message);
	}

	private int previewCount = 0;

	@Override
	public void preview() {
		Log.info("ParameterTester.preview: " +
			++previewCount + " invocations and counting");
		Events.publish(new StatusEvent(message));
	}

}
