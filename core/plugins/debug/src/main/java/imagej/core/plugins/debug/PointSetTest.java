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

import net.imglib2.RandomAccess;
import net.imglib2.img.ImgPlus;
import net.imglib2.ops.PointSetIterator;
import net.imglib2.ops.pointset.TextSpecifiedPointSet;
import net.imglib2.type.numeric.RealType;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

/**
 * A test of TextSpecifiedPointSets
 * 
 * @author Barry DeZonia
 */
@Plugin(menuPath = "Plugins>Sandbox>PointSet Test")
public class PointSetTest implements ImageJPlugin {

	@Parameter(label="PointSet specification", type = ItemIO.INPUT)
	private String specification;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset output;
	
	@Parameter(required = true, persist = false)
	ImageJ context;
	
	@Parameter(required = true, persist = false)
	DatasetService ds;
	
	@Override
	public void run() {
		TextSpecifiedPointSet pointSet = new TextSpecifiedPointSet(specification);
		long[] minBound = pointSet.findBoundMin();
		long[] maxBound = pointSet.findBoundMax();
		
		for (int i = 0; i < minBound.length; i++) {
			if ((minBound[i] < 0) || (maxBound[i] < 0))
				throw new IllegalArgumentException(
					"For now won't handle negative space with this test plugin");
			// make a border around the maximum bound
			maxBound[i] += 10;
		}
		
		output = ds.create(maxBound, "PointSet", null, 8, false, false);
		
		ImgPlus<? extends RealType<?>> imgplus = output.getImgPlus();
		
		RandomAccess<? extends RealType<?>> accessor = imgplus.randomAccess();

		PointSetIterator iter = pointSet.createIterator();
		
		while (iter.hasNext()) {
			long[] pos = iter.next();
			accessor.setPosition(pos);
			accessor.get().setReal(255);
		}
		
	}

}
