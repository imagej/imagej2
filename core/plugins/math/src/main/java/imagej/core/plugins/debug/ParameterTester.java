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

package imagej.core.plugins.debug;

import imagej.data.Dataset;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.ext.module.ui.WidgetStyle;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PreviewPlugin;
import imagej.util.ColorRGB;
import imagej.util.Log;

import java.io.File;
import java.math.BigInteger;

/**
 * Test plugin for verifying that various plugin features work properly.
 * 
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Debug>Parameter Tester")
public class ParameterTester implements ImageJPlugin, PreviewPlugin {

	@Parameter(label = "boolean")
	private boolean pBoolean;

	@Parameter(label = "Boolean")
	private Boolean oBoolean;

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

	@Parameter(label = "Byte")
	private Byte oByte;

	@Parameter(label = "Double")
	private Double oDouble;

	@Parameter(label = "Float")
	private Float oFloat;

	@Parameter(label = "Integer")
	private Integer oInt;

	@Parameter(label = "Long")
	private Long oLong;

	@Parameter(label = "Short")
	private Short oShort;

//@Parameter
	private BigInteger bigInteger;

	@Parameter(label = "char")
	private char pChar;

	@Parameter(label = "Character")
	private Character oChar;

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
		max = "1000", stepSize = "50")
	private int sliderNumber;

	@Parameter(label = "scroll bar", style = WidgetStyle.NUMBER_SCROLL_BAR,
		min = "0", max = "1000")
	private int scrollBarNumber;

	@Parameter(label = "x", callback = "xChanged")
	private float x;

	@Parameter(label = "2x",
		description = "Demonstrates callback functionality. Equal to double x.",
		callback = "twoXChanged")
	private float twoX;

	@Parameter(description = "Demonstrates preview functionality by "
		+ "displaying the given message in the ImageJ status bar.")
	private String message = "Type a status message here.";

	@Override
	public void run() {
		Log.info("ParameterTester results:");

		Log.info("");
		Log.info("-- Toggles --");
		Log.info("\tboolean = " + pBoolean);
		Log.info("\tBoolean = " + oBoolean);

		Log.info("");
		Log.info("-- Numeric --");
		Log.info("\tbyte = " + pByte);
		Log.info("\tdouble = " + pDouble);
		Log.info("\tfloat = " + pFloat);
		Log.info("\tint = " + pInt);
		Log.info("\tlong = " + pLong);
		Log.info("\tshort = " + pShort);
		Log.info("\tByte = " + oByte);
		Log.info("\tDouble = " + oDouble);
		Log.info("\tFloat = " + oFloat);
		Log.info("\tInteger = " + oInt);
		Log.info("\tLong = " + oLong);
		Log.info("\tShort = " + oShort);
		Log.info("\tBigInteger = " + bigInteger);

		Log.info("");
		Log.info("-- Text --");
		Log.info("\tchar = " + "'" + pChar + "' [" +
			Character.getNumericValue(pChar) + "]");
		Log.info("\tCharacter = " + "'" + oChar + "' [" +
			Character.getNumericValue(oChar) + "]");
		Log.info("\tString = " + string);

		Log.info("");
		Log.info("-- Choice --");
		Log.info("\tchoice = " + choice);

		Log.info("");
		Log.info("-- Object --");
		Log.info("\tDataset = " + dataset);

		Log.info("");
		Log.info("-- File --");
		Log.info("\tFile = " + file);

		Log.info("");
		Log.info("-- Color --");
		Log.info("\tcolor = " + color);

		Log.info("");
		Log.info("-- Miscellaneous --");
		Log.info("\tspinner = " + spinnerNumber);
		Log.info("\tslider = " + sliderNumber);
		Log.info("\tscroll bar = " + scrollBarNumber);
		Log.info("\tx = " + x);
		Log.info("\t2x = " + twoX);
		Log.info("\tmessage = " + message);
	}

	private int previews = 0;

	@Override
	public void preview() {
		Log.info("ParameterTester: " + ++previews + " previews and counting");
		Events.publish(new StatusEvent(message));
	}

	@Override
	public void cancel() {
		Log.info("ParameterTester: canceled");
	}

	@SuppressWarnings("unused")
	private void xChanged() {
		Log.info("ParameterTester: x changed");
		twoX = x * 2;
	}

	@SuppressWarnings("unused")
	private void twoXChanged() {
		Log.info("ParameterTester: 2x changed");
		x = twoX / 2;
	}

}
