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

package imagej.core.plugins.debug;

import imagej.data.Dataset;
import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.ext.module.ItemIO;
import imagej.ext.module.ui.WidgetStyle;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PreviewPlugin;
import imagej.util.ColorRGB;
import imagej.util.Log;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Test plugin for verifying that various plugin features work properly.
 * 
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Debug>Parameter Tester", headless = true)
public class ParameterTester implements ImageJPlugin, PreviewPlugin {

	@Parameter(persist = false)
	private EventService eventService;

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

	@Parameter
	private BigInteger bigInteger;

	@Parameter
	private BigDecimal bigDecimal;

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

	@Parameter(persist = false)
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

	@Parameter(type = ItemIO.OUTPUT)
	private String output;

	public String getOutput() {
		return output;
	}

	@Override
	public void run() {
		final StringBuilder sb = new StringBuilder();

		append(sb, "ParameterTester results:");

		append(sb, "");
		append(sb, "-- Toggles --");
		append(sb, "\tboolean = " + pBoolean);
		append(sb, "\tBoolean = " + oBoolean);

		append(sb, "");
		append(sb, "-- Numeric --");
		append(sb, "\tbyte = " + pByte);
		append(sb, "\tdouble = " + pDouble);
		append(sb, "\tfloat = " + pFloat);
		append(sb, "\tint = " + pInt);
		append(sb, "\tlong = " + pLong);
		append(sb, "\tshort = " + pShort);
		append(sb, "\tByte = " + oByte);
		append(sb, "\tDouble = " + oDouble);
		append(sb, "\tFloat = " + oFloat);
		append(sb, "\tInteger = " + oInt);
		append(sb, "\tLong = " + oLong);
		append(sb, "\tShort = " + oShort);
		append(sb, "\tBigInteger = " + bigInteger);
		append(sb, "\tBigDecimal = " + bigDecimal);

		append(sb, "");
		append(sb, "-- Text --");
		append(sb, "\tchar = " + "'" + pChar + "' [" +
			Character.getNumericValue(pChar) + "]");
		final String oCharValue =
			oChar == null ? "null" : "" + Character.getNumericValue(oChar);
		append(sb, "\tCharacter = " + "'" + oChar + "' [" + oCharValue + "]");
		append(sb, "\tString = " + string);

		append(sb, "");
		append(sb, "-- Choice --");
		append(sb, "\tchoice = " + choice);

		append(sb, "");
		append(sb, "-- Object --");
		append(sb, "\tDataset = " + dataset);

		append(sb, "");
		append(sb, "-- File --");
		append(sb, "\tFile = " + file);

		append(sb, "");
		append(sb, "-- Color --");
		append(sb, "\tcolor = " + color);

		append(sb, "");
		append(sb, "-- Miscellaneous --");
		append(sb, "\tspinner = " + spinnerNumber);
		append(sb, "\tslider = " + sliderNumber);
		append(sb, "\tscroll bar = " + scrollBarNumber);
		append(sb, "\tx = " + x);
		append(sb, "\t2x = " + twoX);
		append(sb, "\tmessage = " + message);

		output = sb.toString();
	}

	private int previews = 0;

	@Override
	public void preview() {
		Log.info("ParameterTester: " + ++previews + " previews and counting");
		eventService.publish(new StatusEvent(message));
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

	private void append(final StringBuilder sb, final String s) {
		sb.append(s + "\n");
	}

}
