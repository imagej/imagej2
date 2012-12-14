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

package imagej.core.commands.debug;

import imagej.command.Command;
import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.DrawingTool;
import imagej.module.ItemIO;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.render.RenderingService;
import imagej.render.TextRenderer.TextJustification;

import java.util.Arrays;
import java.util.List;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

/**
 * A plugin that creates a simple multidimensional image with axes beyond those
 * legacy ImageJ provides.
 * 
 * @author Barry DeZonia
 */
@Plugin(menuPath = "Plugins>Sandbox>Multidimensional Test Image")
public class MultidimImage implements Command {

	@Parameter
	private DatasetService datasetService;
	
	@Parameter
	private RenderingService renderingService;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset ds;

	@Override
	public void run() {
		final long[] dims = new long[] { 90, 35, 4, 5, 6, 7 };
		final String name = "Multidimensional Example";
		final AxisType[] axes =
			new AxisType[] { Axes.X, Axes.Y, Axes.CHANNEL, Axes.FREQUENCY, Axes.Z,
				Axes.TIME };
		final int bitsPerPixel = 8;
		final boolean signed = false;
		final boolean floating = false;
		ds = datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);
		final long[] pos = new long[dims.length];
		final DrawingTool tool = new DrawingTool(ds, renderingService);
		final List<Double> values =
			Arrays.asList(new Double[] { 255.0, 255.0, 255.0, 255.0 });
		final ChannelCollection channels = new ChannelCollection(values);
		tool.setChannels(channels);
		for (int c = 0; c < dims[2]; c++) {
			for (int f = 0; f < dims[3]; f++) {
				for (int z = 0; z < dims[4]; z++) {
					for (int t = 0; t < dims[5]; t++) {
						pos[2] = c;
						pos[3] = f;
						pos[4] = z;
						pos[5] = t;
						tool.setPosition(pos);
						tool.setPreferredChannel(c);
						final String label = "c " + c + " f " + f + " z " + z + " t " + t;
						tool.drawText(5, 20, label, TextJustification.LEFT);
					}
				}
			}
		}
	}

}
