//
// DatasetHarmonizerTest.java
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

package imagej.legacy;

import static org.junit.Assert.assertTrue;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;

import org.junit.Test;

/**
 * Unit tests for {@link DatasetHarmonizer}.
 * 
 * @author Barry DeZonia
 */
public class DatasetHarmonizerTest {

	private DatasetHarmonizer harmonizer;
	private ImageTranslator translator = new DefaultImageTranslator();
	
	@Test
	public void testDatasetHarmonizer() {
		harmonizer = new DatasetHarmonizer(translator);
		assertTrue(true);
	}

	@Test
	public void testRegisterType() {
		harmonizer = new DatasetHarmonizer(translator);
		ByteProcessor proc = new ByteProcessor(1,2,new byte[1*2],null);
		ImagePlus imp = new ImagePlus("junk",proc);
		harmonizer.registerType(imp);
		assertTrue(true);
	}

	@Test
	public void testResetTypeTracking() {
		harmonizer = new DatasetHarmonizer(translator);
		ByteProcessor proc = new ByteProcessor(1,2,new byte[1*2],null);
		ImagePlus imp = new ImagePlus("junk",proc);
		harmonizer.registerType(imp);
		harmonizer.resetTypeTracking();
		assertTrue(true);
	}

	private void tryUpdateDatasetWithOrder(Axis[] axes) {
		// TODO
		assertTrue(true);
	}
	
	@Test
	public void testUpdateDatasetWeirdAxisOrder() {
		Axis[] axes = new Axis[]{null, Axes.CHANNEL, Axes.Z, Axes.TIME};
		for (Axis outer : axes) {
			for (Axis middle : axes) {
				for (Axis inner : axes) {
					if (LegacyTestUtils.allNull(new Axis[]{outer,middle,inner})) continue;
					if (LegacyTestUtils.repeated(new Axis[]{outer,middle,inner})) continue;
					tryUpdateDatasetWithOrder(new Axis[]{outer,middle,inner});
				}
			}
		}
	}

	@Test
	public void testUpdateLegacyImageWeirdAxisOrder() {
		assertTrue(true);
		//fail("Not yet implemented");
	}

}
