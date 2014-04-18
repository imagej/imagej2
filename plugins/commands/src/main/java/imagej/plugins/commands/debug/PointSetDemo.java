/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.commands.debug;

import java.net.URL;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.RandomAccess;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.ops.pointset.TextSpecifiedPointSet;
import net.imglib2.type.numeric.RealType;

import org.scijava.Cancelable;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.platform.PlatformService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;

/**
 * A test of TextSpecifiedPointSets
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menuPath = "Plugins>Sandbox>PointSet Demo")
public class PointSetDemo implements Command, Cancelable {

	@Parameter
	private PlatformService platformService;
	
	@Parameter(label="PointSet specification", type = ItemIO.INPUT)
	private String specification;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset output;
	
	@Parameter
	private DatasetService ds;
	
	@Parameter(label = "Help",
			description="View a web page detailing the point set language",
			callback="openWebPage", persist = false)
	private Button openWebPage;
	
	private String err;

	@Override
	public void run() {
		TextSpecifiedPointSet pointSet = new TextSpecifiedPointSet(specification);
		if (pointSet.getErrorString() != null) {
			cancel(pointSet.getErrorString());
			return;
		}
		long[] minBound = new long[pointSet.numDimensions()];
		long[] maxBound = new long[pointSet.numDimensions()];
		pointSet.min(minBound);
		pointSet.max(maxBound);
		
		for (int i = 0; i < minBound.length; i++) {
			if ((minBound[i] < 0) || (maxBound[i] < 0)) {
				cancel("For now won't handle negative space with this test plugin");
				return;
			}
			// make a border around the maximum bound
			maxBound[i] += 10;
		}
		
		output = ds.create(maxBound, "PointSet", null, 8, false, false);
		
		ImgPlus<? extends RealType<?>> imgplus = output.getImgPlus();
		
		RandomAccess<? extends RealType<?>> accessor = imgplus.randomAccess();

		PointSetIterator iter = pointSet.iterator();
		
		while (iter.hasNext()) {
			long[] pos = iter.next();
			accessor.setPosition(pos);
			accessor.get().setReal(255);
		}
		
	}

	@Override
	public boolean isCanceled() {
		return err != null;
	}

	@Override
	public void cancel(final String reason) {
		err = reason;
	}

	@Override
	public String getCancelReason() {
		return err;
	}

	// -- callbacks --
	
	protected void openWebPage() {
		try {
			String urlString =
					"http://wiki.imagej.net/ImageJ2/Documentation/PointSetDemo";
			URL url = new URL(urlString);
			platformService.open(url);
		} catch (Exception e) {
			// do nothing
		}
	}
}
