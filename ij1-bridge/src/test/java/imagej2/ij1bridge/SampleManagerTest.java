package imagej2.ij1bridge;

import static org.junit.Assert.*;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import imagej2.UserType;
import imagej2.imglib.process.ImageUtils;
import imagej2.ij1bridge.process.ImgLibProcessor;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.FloatType;

import org.junit.Test;

public class SampleManagerTest {

	@Test
	public void testGetUserTypeImageProcessor()
	{
		ByteProcessor bProc = new ByteProcessor(1, 1, new byte[1], null);
		assertEquals(UserType.UBYTE, SampleManager.getUserType(bProc));
		
		ShortProcessor sProc = new ShortProcessor(1, 1, new short[1], null);
		assertEquals(UserType.USHORT, SampleManager.getUserType(sProc));

		FloatProcessor fProc = new FloatProcessor(1, 1, new float[1], null);
		assertEquals(UserType.FLOAT, SampleManager.getUserType(fProc));

		ColorProcessor cProc = new ColorProcessor(1, 1, new int[1]);
		assertEquals(UserType.UINT, SampleManager.getUserType(cProc));

		Image<UnsignedShortType> image =
			ImageUtils.createImage(new UnsignedShortType(), new PlanarContainerFactory(), new int[]{6,4,2});
		
		ImgLibProcessor<?> iProc = new ImgLibProcessor<UnsignedShortType>(image, 0);
		
		assertEquals(UserType.USHORT, SampleManager.getUserType(iProc));
	}

	@Test
	public void testGetUserTypeImagePlus()
	{
		ImagePlus imp;
		
		ByteProcessor bProc = new ByteProcessor(1, 1, new byte[1], null);
		imp = new ImagePlus("zacko", bProc);
		assertEquals(UserType.UBYTE, SampleManager.getUserType(imp));
		
		ShortProcessor sProc = new ShortProcessor(1, 1, new short[1], null);
		imp = new ImagePlus("zacko", sProc);
		assertEquals(UserType.USHORT, SampleManager.getUserType(imp));

		FloatProcessor fProc = new FloatProcessor(1, 1, new float[1], null);
		imp = new ImagePlus("zacko", fProc);
		assertEquals(UserType.FLOAT, SampleManager.getUserType(imp));

		ColorProcessor cProc = new ColorProcessor(1, 1, new int[1]);
		imp = new ImagePlus("zacko", cProc);
		assertEquals(UserType.UINT, SampleManager.getUserType(imp));

		Image<FloatType> image =
			ImageUtils.createImage(new FloatType(), new PlanarContainerFactory(), new int[]{6,4,2});
		ImgLibProcessor<?> iProc = new ImgLibProcessor<FloatType>(image, 0);
		imp = new ImagePlus("zacko", iProc);
		assertEquals(UserType.FLOAT, SampleManager.getUserType(imp));
	}
}
