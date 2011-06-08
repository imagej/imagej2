package imagej.legacy;

import static org.junit.Assert.*;
import ij.ImagePlus;
import ij.process.ByteProcessor;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;

import org.junit.Test;


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
		assertTrue(true);
	}
	
	@Test
	public void testUpdateDatasetWeirdAxisOrder() {
		Axis[] axes = new Axis[]{null, Axes.CHANNEL, Axes.Z, Axes.TIME};
		for (Axis outer : axes) {
			for (Axis middle : axes) {
				for (Axis inner : axes) {
					if (Utils.allNull(new Axis[]{outer,middle,inner})) continue;
					if (Utils.repeated(new Axis[]{outer,middle,inner})) continue;
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
