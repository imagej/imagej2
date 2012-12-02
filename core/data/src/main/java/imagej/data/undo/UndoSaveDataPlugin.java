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

package imagej.data.undo;

import imagej.command.CommandService;
import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.module.ItemIO;
import imagej.module.ModuleInfo;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.undo.DefaultUndoInfo;
import imagej.undo.Invertible;
import imagej.undo.UndoInfo;

import java.util.HashMap;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.real.DoubleType;

// NOTE - this plugin unused as of 10-1-12. May prove useful in future.

/**
 * TODO
 * 
 * @author Barry DeZonia
 */
@Plugin
public class UndoSaveDataPlugin extends ContextCommand implements
	Invertible
{

	// -- Parameters --

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private CommandService commandService;

	@Parameter(type = ItemIO.INPUT)
	private Dataset source;

	@Parameter(type = ItemIO.INPUT)
	private PointSet points;

	@Parameter(type = ItemIO.OUTPUT)
	private Img<DoubleType> data;

	// -- private instance variables --

	private ModuleInfo inverse;

	// -- Command methods --

	@Override
	public void run() {
		// TODO - change ArrayImgFactory to some small memory ImgFactory made for
		// undo
		data =
			datasetService.captureData(source, points,
				new ArrayImgFactory<DoubleType>());
		inverse = commandService.getCommand(UndoRestoreDataPlugin.class);
	}

	// -- InvertibleCommand methods --

	@Override
	public UndoInfo getInverse() {
		final HashMap<String, Object> inverseInputs = new HashMap<String, Object>();
		inverseInputs.put("target", source);
		inverseInputs.put("points", points);
		inverseInputs.put("data", data);
		final long size = 8 * numElements(data);
		return new DefaultUndoInfo(inverse, inverseInputs, size);
	}

	// -- UndoSaveDataPlugin methods --

	public void setSource(final Dataset ds) {
		source = ds;
	}

	public Dataset getSource() {
		return source;
	}

	public void setPoints(final PointSet ps) {
		points = ps;
	}

	public PointSet getPoints() {
		return points;
	}

	public Img<DoubleType> getData() {
		return data;
	}

	// -- private helpers --

	private long numElements(final Img<?> img) {
		final int numDims = img.numDimensions();
		if (numDims < 1) return 0;
		long totElems = 1;
		for (int i = 0; i < numDims; i++) {
			totElems *= img.dimension(i);
		}
		return totElems;
	}
}
