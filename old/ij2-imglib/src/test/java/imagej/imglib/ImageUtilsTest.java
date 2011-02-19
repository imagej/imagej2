package imagej.imglib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import imagej.imglib.ImageUtils;
import mpicbg.imglib.container.basictypecontainer.PlanarAccess;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

import org.junit.Test;

public class ImageUtilsTest {

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
	public void testGetType()
	{
		int[] dimensions = new int[]{3,5};

		Image<?> image;

		image = makeImage(new UnsignedByteType(), dimensions);
		assertTrue(ImageUtils.getType(image) instanceof UnsignedByteType);

		image = makeImage(new ShortType(), dimensions);
		assertTrue(ImageUtils.getType(image) instanceof ShortType);

		image = makeImage(new UnsignedIntType(), dimensions);
		assertTrue(ImageUtils.getType(image) instanceof UnsignedIntType);

		image = makeImage(new DoubleType(), dimensions);
		assertTrue(ImageUtils.getType(image) instanceof DoubleType);

		image = makeImage(new LongType(), dimensions);
		assertTrue(ImageUtils.getType(image) instanceof LongType);
		
		image = makeImage(new Unsigned12BitType(), dimensions);
		assertTrue(ImageUtils.getType(image) instanceof Unsigned12BitType);
	}

	@Test
	public void testGetPlanarAccess()
	{
		PlanarContainerFactory factory = new PlanarContainerFactory();

		Image<?> testImage;
		PlanarAccess<ArrayDataAccess<?>> access;

		testImage = ImageUtils.createImage(new UnsignedByteType(), factory, new int[]{1,2});
		access = ImageUtils.getPlanarAccess(testImage);
		assertTrue(access != null);
	}

	@Test
	public void testCreateImage()
	{
		Image<?> image;

		image = makeImage(new UnsignedIntType(), new int[]{1});
		assertTrue(ImageUtils.getType(image) instanceof UnsignedIntType);
		assertEquals(1,image.getDimension(0));

		image = makeImage(new FloatType(), new int[]{6,4});
		assertTrue(ImageUtils.getType(image) instanceof FloatType);
		assertEquals(6,image.getDimension(0));
		assertEquals(4,image.getDimension(1));

		image = makeImage(new LongType(), new int[]{6,4,2});
		assertTrue(ImageUtils.getType(image) instanceof LongType);
		assertEquals(6,image.getDimension(0));
		assertEquals(4,image.getDimension(1));
		assertEquals(2,image.getDimension(2));

		image = makeImage(new Unsigned12BitType(), new int[]{3,1,7});
		assertTrue(ImageUtils.getType(image) instanceof Unsigned12BitType);
		assertEquals(3,image.getDimension(0));
		assertEquals(1,image.getDimension(1));
		assertEquals(7,image.getDimension(2));
	}

}
