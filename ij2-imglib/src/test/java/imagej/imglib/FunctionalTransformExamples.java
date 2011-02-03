package imagej.imglib;

import java.util.Random;

import imagej.function.BinaryFunction;
import imagej.function.UnaryFunction;
import imagej.function.binary.AverageIntegralBinaryFunction;
import imagej.function.unary.AbsUnaryFunction;
import imagej.function.unary.AddNoiseUnaryFunction;
import imagej.function.unary.FillUnaryFunction;
import imagej.function.unary.SqrtUnaryFunction;
import imagej.imglib.process.operation.BinaryTransformOperation;
import imagej.imglib.process.operation.QueryOperation;
import imagej.imglib.process.operation.UnaryTransformOperation;
import imagej.process.query.InfoCollector;
import imagej.selection.SelectionFunction;
import imagej.selection.ValueGreaterThanSelectionFunction;
import imagej.selection.ValueLessThanSelectionFunction;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

public class FunctionalTransformExamples
{
	private Image<UnsignedByteType> createTestImage(int[] dimensions)
	{
		ImageFactory<UnsignedByteType> factory = new ImageFactory<UnsignedByteType>(new UnsignedByteType(), new ArrayContainerFactory());
		
		return factory.createImage(dimensions);
	}
	
	private void populateTestImageWithTwoValues(Image<UnsignedByteType> image)
	{
		Random rng = new Random();
		
		Cursor<UnsignedByteType> cursor = image.createCursor();

		while (cursor.hasNext())
		{
			cursor.fwd();
			if (rng.nextDouble() < 0.7)
				cursor.getType().set(255);
			else
				cursor.getType().set(0);
		}
	}
	
	private void populateTestImageRandomly(Image<UnsignedByteType> image)
	{
		Random rng = new Random();
		
		Cursor<UnsignedByteType> cursor = image.createCursor();

		while (cursor.hasNext())
		{
			cursor.fwd();
			
			double value = 255 * rng.nextDouble();
			
			cursor.getType().setReal(value);
		}
	}
	
	private void transformImageByFunction()
	{
		int[] dimensions = new int[]{10,20,30};
		
		int[] origin = new int[3];
		
		Image<UnsignedByteType> image = createTestImage(dimensions);
		
		UnaryFunction function = new AbsUnaryFunction();
		
		UnaryTransformOperation<UnsignedByteType> operation = new UnaryTransformOperation<UnsignedByteType>(image, origin, dimensions, function);
		
		operation.execute();
	}

	private void transformSubregionByFunction()
	{
		int[] dimensions = new int[]{10,20,30};
		
		int[] regionOrigin = new int[]{5,5,5};
		
		int[] regionSpan = new int[]{5,10,20};
		
		Image<UnsignedByteType> image = createTestImage(dimensions);
		
		boolean isIntegralData = true;
		
		UnaryFunction function = new SqrtUnaryFunction(isIntegralData);
		
		UnaryTransformOperation<UnsignedByteType> operation = new UnaryTransformOperation<UnsignedByteType>(image, regionOrigin, regionSpan, function);
		
		operation.execute();
	}

	private class MySubregionSelector implements SelectionFunction
	{
		@Override
		public boolean include(int[] position, double sample)
		{
			double sumOfSquares = 0;
			
			for (int indexVal : position)
				sumOfSquares += (indexVal * indexVal);
			
			double distanceFromOrigin = Math.sqrt(sumOfSquares);
			
			if (distanceFromOrigin < 5) return false;
			if (distanceFromOrigin > 15) return false;
			
			return true;
		}
	}
	
	private void transformWithSelectionFunction()
	{
		int[] dimensions = new int[]{20,20,30};
		
		int[] origin = new int[3];
		
		Image<UnsignedByteType> image = createTestImage(dimensions);

		boolean isIntegralData = true;

		Cursor<UnsignedByteType> cursor = image.createCursor();
		
		double typeMin = cursor.getType().getMinValue();
		
		double typeMax = cursor.getType().getMaxValue();
		
		UnaryFunction function = new AddNoiseUnaryFunction(isIntegralData, typeMin, typeMax, 10.0);
		
		UnaryTransformOperation<UnsignedByteType> operation = new UnaryTransformOperation<UnsignedByteType>(image, origin, dimensions, function);
		
		operation.setSelectionFunction(new MySubregionSelector());
		
		operation.execute();
	}

	private class PixCounter implements InfoCollector
	{
		long pixelCount;
		
		@Override
		public void init() {
			this.pixelCount = 0;
		}

		@Override
		public void collectInfo(int[] position, double value) {
			this.pixelCount++;
		}

		@Override
		public void done() {
		}
		
	}
	
	private class BorderPixelSelector implements SelectionFunction
	{
		private Image<UnsignedByteType> image;
		private LocalizableByDimCursor<UnsignedByteType> cursor;
		private int[] imageDims;
		
		public BorderPixelSelector(Image<UnsignedByteType> image)
		{
			this.image = image;
			this.cursor = image.createLocalizableByDimCursor();
			this.imageDims = image.getDimensions();
		}

		@Override
		public boolean include(int[] position, double sample)
		{
			if (sample != 255)
				return false;
			
			int[] neighPos = this.image.createPositionArray();

			// look left
			if (position[0] > 0)
			{
				neighPos[0] = position[0]-1;
				neighPos[1] = position[1];
				this.cursor.setPosition(neighPos);
				if (this.cursor.getType().get() != 255)
					return true;
			}

			// look right
			if (position[0] < this.imageDims[0]-1)
			{
				neighPos[0] = position[0]+1;
				neighPos[1] = position[1];
				this.cursor.setPosition(neighPos);
				if (this.cursor.getType().get() != 255)
					return true;
			}

			// look up
			if (position[1] > 0)
			{
				neighPos[0] = position[0];
				neighPos[1] = position[1]-1;
				this.cursor.setPosition(neighPos);
				if (this.cursor.getType().get() != 255)
					return true;
			}

			// look down
			if (position[1] < this.imageDims[1]-1)
			{
				neighPos[0] = position[0];
				neighPos[1] = position[1]+1;
				this.cursor.setPosition(neighPos);
				if (this.cursor.getType().get() != 255)
					return true;
			}

			return false;
		}
		
	}

	private void queryBorderOfRegion()
	{
		int[] dimensions = new int[]{10,20};
		
		int[] origin = new int[dimensions.length];
		
		Image<UnsignedByteType> image = createTestImage(dimensions);

		populateTestImageWithTwoValues(image);
		
		PixCounter counter = new PixCounter();
		
		QueryOperation<UnsignedByteType> operation = new QueryOperation<UnsignedByteType>(image, origin, dimensions, counter);
		
		operation.setSelectionFunction(new BorderPixelSelector(image));
		
		operation.execute();
		
		System.out.println("number of border pixels = "+counter.pixelCount);
	}

	private void changeBorderOfRegion()
	{
		int[] dimensions = new int[]{10,20};
		
		int[] origin = new int[dimensions.length];
		
		Image<UnsignedByteType> image = createTestImage(dimensions);

		populateTestImageWithTwoValues(image);
		
		FillUnaryFunction fillFunc = new FillUnaryFunction(128);
		
		UnaryTransformOperation<UnsignedByteType> operation = new UnaryTransformOperation<UnsignedByteType>(image, origin, dimensions, fillFunc);
		
		operation.setSelectionFunction(new BorderPixelSelector(image));
		
		operation.execute();
	}
	
	private void multipleDatasetExample()
	{
		int[] dimensions = new int[]{10,20,30};
		
		int[] origin = new int[dimensions.length];
		
		Image<UnsignedByteType> image1 = createTestImage(dimensions);
		Image<UnsignedByteType> image2 = createTestImage(dimensions);

		populateTestImageRandomly(image1);
		populateTestImageRandomly(image2);

		BinaryFunction function = new AverageIntegralBinaryFunction();
		
		BinaryTransformOperation<UnsignedByteType> operation =
			new BinaryTransformOperation<UnsignedByteType>(image1, origin, dimensions, image2, origin, dimensions, function);
		
		SelectionFunction selector1 = new ValueLessThanSelectionFunction(100);
		
		SelectionFunction selector2 = new ValueGreaterThanSelectionFunction(200);
		
		operation.setSelectionFunctions(selector1, selector2);
		
		operation.execute();
	}
}
