package imagej.imglib.examples.function;

import static org.junit.Assert.assertEquals;
import imagej.imglib.examples.function.condition.PixelOnBorder;
import imagej.imglib.examples.function.condition.ValueGreaterThan;
import imagej.imglib.examples.function.condition.ValueLessThan;
import imagej.imglib.examples.function.function.AverageFunction;
import imagej.imglib.examples.function.function.ConstantFunction;
import imagej.imglib.examples.function.function.SquareFunction;
import imagej.imglib.examples.function.operation.AssignOperation;

import org.junit.Test;

import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

@SuppressWarnings("unchecked")

public class NewFunctionalIdeasTest
{
	// ************  private interface ********************************************************
	
	private static Image<UnsignedByteType> createImage()
	{
		ImageFactory<UnsignedByteType> factory = new ImageFactory<UnsignedByteType>(new UnsignedByteType(), new ArrayContainerFactory());
		
		return factory.createImage(new int[]{3,3});
	}

	private static Image<UnsignedByteType> createPopulatedImage(int[] values)
	{
		Image<UnsignedByteType> image = createImage();
		
		LocalizableByDimCursor<UnsignedByteType> cursor = image.createLocalizableByDimCursor();
		
		int[] position = new int[2];
		
		int i = 0;
		
		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				position[0] = x;
				position[1] = y;
				cursor.setPosition(position);
				cursor.getType().setInteger(values[i++]);
			}
		}

		return image;
	}
	
	
	private static void assertImageValsEqual(int[] values, Image<UnsignedByteType> image)
	{
		LocalizableByDimCursor<UnsignedByteType> cursor = image.createLocalizableByDimCursor();

		int[] position = new int[2];
		
		int i = 0;
		
		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				position[0] = x;
				position[1] = y;
				cursor.setPosition(position);
				assertEquals(values[i++], cursor.getType().getInteger());
			}
		}
	}
	
	// ************  tests ********************************************************

	@Test
	public void testOneImageSquaring()
	{
		//System.out.println("square all input values");
		
		Image<UnsignedByteType> image0 = createPopulatedImage(new int[]{1,2,3,4,5,6,7,8,9});
		
		SquareFunction<UnsignedByteType> function = new SquareFunction<UnsignedByteType>();

		AssignOperation<UnsignedByteType> operation = new AssignOperation<UnsignedByteType>(new Image[]{image0}, image0, function);
		
		operation.execute();
		
		assertImageValsEqual(new int[]{1,4,9,16,25,36,49,64,81}, image0);

		//System.out.println("  success");
	}
	
	@Test
	public void testOneImageInputConditionGreater()
	{
		//System.out.println("square those where input values are greater than 4");
		
		Image<UnsignedByteType> image0 = createPopulatedImage(new int[]{1,2,3,4,5,6,7,8,9});
		
		SquareFunction<UnsignedByteType> function = new SquareFunction<UnsignedByteType>();

		AssignOperation<UnsignedByteType> operation = new AssignOperation<UnsignedByteType>(new Image[]{image0}, image0, function);

		operation.setInputCondition(0, new ValueGreaterThan<UnsignedByteType>(4));
		
		operation.execute();
		
		assertImageValsEqual(new int[]{1,2,3,4,25,36,49,64,81}, image0);
		
		//System.out.println("  success");
	}
	
	@Test
	public void testOneImageOutputConditionLess()
	{
		//System.out.println("square those where original output values are less than 7");
		
		Image<UnsignedByteType> image0 = createPopulatedImage(new int[]{1,2,3,4,5,6,7,8,9});
		
		SquareFunction<UnsignedByteType> function = new SquareFunction<UnsignedByteType>();

		AssignOperation<UnsignedByteType> operation = new AssignOperation<UnsignedByteType>(new Image[]{image0}, image0, function);

		operation.setOutputCondition(new ValueLessThan<UnsignedByteType>(7));
		
		operation.execute();
		
		assertImageValsEqual(new int[]{1,4,9,16,25,36,7,8,9}, image0);

		//System.out.println("  success");
	}
	
	@Test
	public void testSecondImageFromOneImageSquaring()
	{
		//System.out.println("square one image into another");
		
		Image<UnsignedByteType> image0 = createPopulatedImage(new int[]{1,2,3,4,5,6,7,8,9});
		
		Image<UnsignedByteType> image1 = createPopulatedImage(new int[]{0,0,0,0,0,0,0,0,0});
		
		SquareFunction<UnsignedByteType> function = new SquareFunction<UnsignedByteType>();

		AssignOperation<UnsignedByteType> operation = new AssignOperation<UnsignedByteType>(new Image[]{image0}, image1, function);

		operation.execute();
		
		assertImageValsEqual(new int[]{1,2,3,4,5,6,7,8,9}, image0);
		assertImageValsEqual(new int[]{1,4,9,16,25,36,49,64,81}, image1);

		//System.out.println("  success");
	}
	
	@Test
	public void testThirdImageFromTwoImagesAveraging()
	{
		//System.out.println("average two images into third");
		
		Image<UnsignedByteType> image0 = createPopulatedImage(new int[]{1,2,3,4,5,6,7,8,9});
		
		Image<UnsignedByteType> image1 = createPopulatedImage(new int[]{11,12,13,14,15,16,17,18,19});

		Image<UnsignedByteType> image2 = createPopulatedImage(new int[]{0,0,0,0,0,0,0,0,0});
		
		AverageFunction<UnsignedByteType> function = new AverageFunction<UnsignedByteType>();

		AssignOperation<UnsignedByteType> operation = new AssignOperation<UnsignedByteType>(new Image[]{image0,image1}, image2, function);

		operation.execute();
		
		assertImageValsEqual(new int[]{1,2,3,4,5,6,7,8,9}, image0);
		assertImageValsEqual(new int[]{11,12,13,14,15,16,17,18,19}, image1);
		assertImageValsEqual(new int[]{6,7,8,9,10,11,12,13,14}, image2);

		//System.out.println("  success");
	}
	
	@Test
	public void testEverythingAveraging()
	{
		//System.out.println("average two images into third conditionally");
		
		Image<UnsignedByteType> image0 = createPopulatedImage(new int[]{1,2,3,
																		4,5,6,
																		7,8,9});
		
		Image<UnsignedByteType> image1 = createPopulatedImage(new int[]{11,12,13,
																		14,15,16,
																		17,18,19});

		Image<UnsignedByteType> image2 = createPopulatedImage(new int[]{5,5,6,
																		6,7,7,
																		8,8,9});
		
		AverageFunction<UnsignedByteType> function = new AverageFunction<UnsignedByteType>();

		AssignOperation<UnsignedByteType> operation = new AssignOperation<UnsignedByteType>(new Image[]{image0,image1}, image2, function);

		operation.setInputCondition(0, new ValueLessThan<UnsignedByteType>(8));
		operation.setInputRegion(0, new int[]{0,1}, new int[]{2,2});

		operation.setInputCondition(1, new ValueGreaterThan<UnsignedByteType>(14));
		operation.setInputRegion(1, new int[]{0,1}, new int[]{2,2});

		operation.setOutputRegion(new int[]{0,1}, new int[]{2,2});
		
		operation.execute();
		
		assertImageValsEqual(new int[]{1,2,3,4,5,6,7,8,9}, image0);
		assertImageValsEqual(new int[]{11,12,13,14,15,16,17,18,19}, image1);
		assertImageValsEqual(new int[]{5,5,6,6,10,7,12,8,9}, image2);

		//System.out.println("  success");
	}

	@Test
	public void testTwoNonOverlappingRegionsInSameImage()
	{
		//System.out.println("average nonoverlapping regions of a single images into a third");
		
		Image<UnsignedByteType> image0 = createPopulatedImage(new int[]{1,2,3,
																		4,5,6,
																		7,8,9});
		
		Image<UnsignedByteType> image1 = createPopulatedImage(new int[]{0,0,0,
																		0,0,0,
																		0,0,0});

		AverageFunction<UnsignedByteType> function = new AverageFunction<UnsignedByteType>();

		AssignOperation<UnsignedByteType> operation = new AssignOperation<UnsignedByteType>(new Image[]{image0,image0}, image1, function);

		operation.setInputRegion(0, new int[]{0,0}, new int[]{3,1});

		operation.setInputRegion(1, new int[]{0,2}, new int[]{3,1});
		
		operation.setOutputRegion(new int[]{0,2}, new int[]{3,1});

		operation.execute();
		
		assertImageValsEqual(new int[]{1,2,3,4,5,6,7,8,9}, image0);
		assertImageValsEqual(new int[]{0,0,0,0,0,0,4,5,6}, image1);

		//System.out.println("  success");
	}

	@Test
	public void testSpatialCondition()
	{
		Image<UnsignedByteType> image0 = createPopulatedImage(
				new int[]{0,0,0,
						0,255,0,
						0,255,0});

		Image<UnsignedByteType> image1 = createPopulatedImage(
				new int[]{0,0,0,
						0,0,0,
						0,0,0});
		
		ConstantFunction<UnsignedByteType> function = new ConstantFunction<UnsignedByteType>(1);

		AssignOperation<UnsignedByteType> operation = new AssignOperation<UnsignedByteType>(new Image[]{image0}, image1, function);
		
		PixelOnBorder<UnsignedByteType> condition = new PixelOnBorder<UnsignedByteType>(image0, 255);
		
		operation.setInputCondition(0, condition);
		
		operation.execute();
		
		assertImageValsEqual(new int[]{0,0,0,0,255,0,0,255,0}, image0);
		assertImageValsEqual(new int[]{0,0,0,0,1,0,0,1,0}, image1);
	}
}
