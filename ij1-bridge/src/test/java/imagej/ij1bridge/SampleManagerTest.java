package imagej.ij1bridge;

import static org.junit.Assert.*;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import imagej.data.Types;
import imagej.ij1bridge.SampleManager;
import imagej.ij1bridge.process.ImgLibProcessor;
import imagej.imglib.process.ImageUtils;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.real.DoubleType;

import org.junit.Test;

public class SampleManagerTest {

	@Test
	public void testGetTypeImageProcessor()
	{
		ByteProcessor bProc = new ByteProcessor(1, 1, new byte[1], null);
		assertEquals(Types.findType("8-bit unsigned"), SampleManager.getType(bProc));
		
		ShortProcessor sProc = new ShortProcessor(1, 1, new short[1], null);
		assertEquals(Types.findType("16-bit unsigned"), SampleManager.getType(sProc));

		FloatProcessor fProc = new FloatProcessor(1, 1, new float[1], null);
		assertEquals(Types.findType("32-bit float"), SampleManager.getType(fProc));

		ColorProcessor cProc = new ColorProcessor(1, 1, new int[1]);
		assertEquals(Types.findType("32-bit unsigned"), SampleManager.getType(cProc));

		Image<DoubleType> image =
			ImageUtils.createImage(new DoubleType(), new PlanarContainerFactory(), new int[]{6,4,2});
		
		ImgLibProcessor<?> iProc = new ImgLibProcessor<DoubleType>(image, 0);
		
		assertEquals(Types.findType("64-bit float"), SampleManager.getType(iProc));
	}

	@Test
	public void testGetTypeImagePlus()
	{
		ImagePlus imp;
		
		ByteProcessor bProc = new ByteProcessor(1, 1, new byte[1], null);
		imp = new ImagePlus("zacko", bProc);
		assertEquals(Types.findType("8-bit unsigned"), SampleManager.getType(imp));
		
		ShortProcessor sProc = new ShortProcessor(1, 1, new short[1], null);
		imp = new ImagePlus("zacko", sProc);
		assertEquals(Types.findType("16-bit unsigned"), SampleManager.getType(imp));

		FloatProcessor fProc = new FloatProcessor(1, 1, new float[1], null);
		imp = new ImagePlus("zacko", fProc);
		assertEquals(Types.findType("32-bit float"), SampleManager.getType(imp));

		ColorProcessor cProc = new ColorProcessor(1, 1, new int[1]);
		imp = new ImagePlus("zacko", cProc);
		assertEquals(Types.findType("32-bit unsigned"), SampleManager.getType(imp));

		Image<Unsigned12BitType> image =
			ImageUtils.createImage(new Unsigned12BitType(), new PlanarContainerFactory(), new int[]{6,4,2});
		ImgLibProcessor<?> iProc = new ImgLibProcessor<Unsigned12BitType>(image, 0);
		imp = new ImagePlus("zacko", iProc);
		assertEquals(Types.findType("12-bit unsigned"), SampleManager.getType(imp));
	}
}
