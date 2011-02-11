package imagej.ij1bridge.process;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.process.ImageProcessor;
import imagej.imglib.ImageUtils;
import imagej.types.Types;
import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

import org.junit.Test;

public class OldLegacyImageUtilsTest {

	// *************  instance vars ********************************************

	int width = 224, height = 403;

	// *************  private helpers ********************************************

	private Image<? extends RealType<?>> makeImage(RealType<?> type, int[] dimensions)
	{
		PlanarContainerFactory cFact = new PlanarContainerFactory();

		return ImageUtils.createImage(type, cFact, dimensions);
	}

	// *************  public tests ********************************************

	@Test
	public void testCreateProcessor()
	{
		int width= 3, height = 5;

		long[] longs = new long[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};

		ImgLibProcessor<?> proc = OldLegacyImageUtils.createProcessor(width, height, longs, Types.findType("64-bit signed"));

		assertNotNull(proc);
		assertEquals(width, proc.getWidth());
		assertEquals(height, proc.getHeight());
		assertArrayEquals(longs,(long[])proc.getPixels());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCreateImagePlus()
	{
		int[] dimensions = new int[]{3,4,5,6,7};

		Image<UnsignedShortType> image = (Image<UnsignedShortType>) makeImage(new UnsignedShortType(), dimensions);

		// TODO : set pixel data to something

		ImagePlus imp = OldLegacyImageUtils.createImagePlus(image);

		int channels = image.getDimension(2);
		int slices   = image.getDimension(3);
		int frames   = image.getDimension(4);

		assertEquals(frames, imp.getNFrames());
		assertEquals(channels, imp.getNChannels());
		assertEquals(slices, imp.getNSlices());

		ImageStack stack = imp.getStack();
		int totalPlanes = slices * channels * frames;
		for (int i = 0; i < totalPlanes; i++)
		{
			ImageProcessor proc = stack.getProcessor(i+1);
			//assertTrue(proc instanceof ImgLibProcessor);
			assertEquals(image.getDimension(0), proc.getWidth());
			assertEquals(image.getDimension(1), proc.getHeight());
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCreateImagePlusWithString()
	{
		int[] dimensions = new int[]{3,4,5,6,7};

		Image<UnsignedShortType> image = (Image<UnsignedShortType>) makeImage(new UnsignedShortType(), dimensions);

		// TODO : set pixel data to something

		ImagePlus imp = OldLegacyImageUtils.createImagePlus(image, "gadzooks");

		int channels = image.getDimension(2);
		int slices   = image.getDimension(3);
		int frames   = image.getDimension(4);

		assertEquals(frames, imp.getNFrames());
		assertEquals(channels, imp.getNChannels());
		assertEquals(slices, imp.getNSlices());

		ImageStack stack = imp.getStack();
		int totalPlanes = slices * channels * frames;
		for (int i = 0; i < totalPlanes; i++)
		{
			ImageProcessor proc = stack.getProcessor(i+1);
			//assertTrue(proc instanceof ImgLibProcessor);
			assertEquals(image.getDimension(0), proc.getWidth());
			assertEquals(image.getDimension(1), proc.getHeight());
		}

		FileInfo fi = imp.getOriginalFileInfo();
		assertEquals("gadzooks",fi.url);
	}
}
