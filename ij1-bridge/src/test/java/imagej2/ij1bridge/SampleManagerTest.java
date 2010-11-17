package imagej2.ij1bridge;

import static org.junit.Assert.*;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import imagej2.SampleInfo.ValueType;
import imagej2.imglib.process.ImageUtils;
import imagej2.ij1bridge.process.ImgLibProcessor;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.FloatType;

import org.junit.Test;

public class SampleManagerTest {

	@Test
	public void testGetValueTypeImageProcessor()
	{
		ByteProcessor bProc = new ByteProcessor(1, 1, new byte[1], null);
		assertEquals(ValueType.UBYTE, SampleManager.getValueType(bProc));
		
		ShortProcessor sProc = new ShortProcessor(1, 1, new short[1], null);
		assertEquals(ValueType.USHORT, SampleManager.getValueType(sProc));

		FloatProcessor fProc = new FloatProcessor(1, 1, new float[1], null);
		assertEquals(ValueType.FLOAT, SampleManager.getValueType(fProc));

		ColorProcessor cProc = new ColorProcessor(1, 1, new int[1]);
		assertEquals(ValueType.UINT, SampleManager.getValueType(cProc));

		Image<UnsignedShortType> image =
			ImageUtils.createImage(new UnsignedShortType(), new PlanarContainerFactory(), new int[]{6,4,2});
		
		ImgLibProcessor<?> iProc = new ImgLibProcessor<UnsignedShortType>(image, 0);
		
		assertEquals(ValueType.USHORT, SampleManager.getValueType(iProc));
	}

	@Test
	public void testGetValueTypeImagePlus()
	{
		ImagePlus imp;
		
		ByteProcessor bProc = new ByteProcessor(1, 1, new byte[1], null);
		imp = new ImagePlus("zacko", bProc);
		assertEquals(ValueType.UBYTE, SampleManager.getValueType(imp));
		
		ShortProcessor sProc = new ShortProcessor(1, 1, new short[1], null);
		imp = new ImagePlus("zacko", sProc);
		assertEquals(ValueType.USHORT, SampleManager.getValueType(imp));

		FloatProcessor fProc = new FloatProcessor(1, 1, new float[1], null);
		imp = new ImagePlus("zacko", fProc);
		assertEquals(ValueType.FLOAT, SampleManager.getValueType(imp));

		ColorProcessor cProc = new ColorProcessor(1, 1, new int[1]);
		imp = new ImagePlus("zacko", cProc);
		assertEquals(ValueType.UINT, SampleManager.getValueType(imp));

		Image<FloatType> image =
			ImageUtils.createImage(new FloatType(), new PlanarContainerFactory(), new int[]{6,4,2});
		ImgLibProcessor<?> iProc = new ImgLibProcessor<FloatType>(image, 0);
		imp = new ImagePlus("zacko", iProc);
		assertEquals(ValueType.FLOAT, SampleManager.getValueType(imp));
	}
}
