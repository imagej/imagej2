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

package imagej.core.plugins.misc;

import imagej.ext.module.ItemIO;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

/**
 * Example that populates a template using a plugin's input values.
 * 
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Examples>OME-XML Form", headless = true)
public class OMEXMLForm extends DynamicPlugin {

	// -- Constants --

	private static final String TEMPLATE =
		"imagej/core/plugins/misc/omexml.template";

	// -- Parameters --

	@Parameter(label = "Image name")
	protected String imageName;

	@Parameter(label = "Dimension order", choices = { "XYCTZ", "XYCZT", "XYTCZ",
		"XYTZC", "XYZCT", "XYZTC" })
	protected String dimOrder;

	@Parameter(label = "Image width", min = "1")
	protected int sizeX = 512;

	@Parameter(label = "Image height", min = "1")
	protected int sizeY = 512;

	@Parameter(label = "Focal planes", min = "1")
	protected int sizeZ = 1;

	@Parameter(label = "Channel count", min = "1")
	protected int sizeC = 1;

	@Parameter(label = "Time points", min = "1")
	protected int sizeT = 1;

	@Parameter(label = "Pixel type", choices = { "int8", "int16", "int32",
		"uint8", "uint16", "uint32", "float", "bit", "double", "complex",
		"double-complex" })
	protected String pixelType;

	@Parameter(label = "Samples per pixel", min = "1")
	protected int samplesPerPixel = 1;

	@Parameter(label = "Big-endian")
	protected boolean bigEndian;

	@Parameter(label = "OME-XML", type = ItemIO.OUTPUT)
	protected String output;

	// -- Runnable methods --

	@Override
	public void run() {
		final TemplateFiller templateFiller = new TemplateFiller();
		output = templateFiller.fillTemplate(TEMPLATE, this);
	}

}
