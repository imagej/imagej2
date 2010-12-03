package imagej.ij1bridge;

import static org.junit.Assert.*;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import imagej.DataType;
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
	public void testGetUserTypeImageProcessor()
	{
		ByteProcessor bProc = new ByteProcessor(1, 1, new byte[1], null);
		assertEquals(DataType.UBYTE, SampleManager.getUserType(bProc));
		
		ShortProcessor sProc = new ShortProcessor(1, 1, new short[1], null);
		assertEquals(DataType.USHORT, SampleManager.getUserType(sProc));

		FloatProcessor fProc = new FloatProcessor(1, 1, new float[1], null);
		assertEquals(DataType.FLOAT, SampleManager.getUserType(fProc));

		ColorProcessor cProc = new ColorProcessor(1, 1, new int[1]);
		assertEquals(DataType.UINT, SampleManager.getUserType(cProc));

		Image<DoubleType> image =
			ImageUtils.createImage(new DoubleType(), new PlanarContainerFactory(), new int[]{6,4,2});
		
		ImgLibProcessor<?> iProc = new ImgLibProcessor<DoubleType>(image, 0);
		
		assertEquals(DataType.DOUBLE, SampleManager.getUserType(iProc));
	}

	@Test
	public void testGetUserTypeImagePlus()
	{
		ImagePlus imp;
		
		ByteProcessor bProc = new ByteProcessor(1, 1, new byte[1], null);
		imp = new ImagePlus("zacko", bProc);
		assertEquals(DataType.UBYTE, SampleManager.getUserType(imp));
		
		ShortProcessor sProc = new ShortProcessor(1, 1, new short[1], null);
		imp = new ImagePlus("zacko", sProc);
		assertEquals(DataType.USHORT, SampleManager.getUserType(imp));

		FloatProcessor fProc = new FloatProcessor(1, 1, new float[1], null);
		imp = new ImagePlus("zacko", fProc);
		assertEquals(DataType.FLOAT, SampleManager.getUserType(imp));

		ColorProcessor cProc = new ColorProcessor(1, 1, new int[1]);
		imp = new ImagePlus("zacko", cProc);
		assertEquals(DataType.UINT, SampleManager.getUserType(imp));

		Image<Unsigned12BitType> image =
			ImageUtils.createImage(new Unsigned12BitType(), new PlanarContainerFactory(), new int[]{6,4,2});
		ImgLibProcessor<?> iProc = new ImgLibProcessor<Unsigned12BitType>(image, 0);
		imp = new ImagePlus("zacko", iProc);
		assertEquals(DataType.UINT12, SampleManager.getUserType(imp));
	}
}
