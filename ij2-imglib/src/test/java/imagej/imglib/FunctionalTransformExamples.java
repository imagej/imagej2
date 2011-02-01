package imagej.imglib;

import java.util.Random;

import imagej.function.UnaryFunction;
import imagej.function.unary.AbsUnaryFunction;
import imagej.function.unary.AddNoiseUnaryFunction;
import imagej.function.unary.SqrtUnaryFunction;
import imagej.imglib.process.operation.QueryOperation;
import imagej.imglib.process.operation.UnaryTransformOperation;
import imagej.process.query.InfoCollector;
import imagej.selection.SelectionFunction;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.real.*;

public class FunctionalTransformExamples
{
	private Image<DoubleType> createTestImage(int[] dimensions)
	{
		ImageFactory<DoubleType> factory = new ImageFactory<DoubleType>(new DoubleType(), new ArrayContainerFactory());
		
		return factory.createImage(dimensions);
	}
	
	private void transformImageByFunction()
	{
		int[] dimensions = new int[]{10,20,30};
		
		int[] origin = new int[3];
		
		Image<DoubleType> image = createTestImage(dimensions);
		
		UnaryFunction function = new AbsUnaryFunction();
		
		UnaryTransformOperation<DoubleType> operation = new UnaryTransformOperation<DoubleType>(image, origin, dimensions, function);
		
		operation.execute();
	}

	private void transformSubregionByFunction()
	{
		int[] dimensions = new int[]{10,20,30};
		
		int[] regionOrigin = new int[]{5,5,5};
		
		int[] regionSpan = new int[]{5,10,20};
		
		Image<DoubleType> image = createTestImage(dimensions);
		
		UnaryFunction function = new SqrtUnaryFunction(false);
		
		UnaryTransformOperation<DoubleType> operation = new UnaryTransformOperation<DoubleType>(image, regionOrigin, regionSpan, function);
		
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
		
		Image<DoubleType> image = createTestImage(dimensions);

		boolean isIntegralData = false;

		Cursor<DoubleType> cursor = image.createCursor();
		
		double typeMin = cursor.getType().getMinValue();
		
		double typeMax = cursor.getType().getMaxValue();
		
		UnaryFunction function = new AddNoiseUnaryFunction(isIntegralData, typeMin, typeMax, 10.0);
		
		UnaryTransformOperation<DoubleType> operation = new UnaryTransformOperation<DoubleType>(image, origin, dimensions, function);
		
		operation.setSelectionFunction(new MySubregionSelector());
		
		operation.execute();
	}

	private class PixCounter implements InfoCollector
	{
		private long pixelCount;
		
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
		private LocalizableByDimCursor<DoubleType> cursor;
		private int[] imageDims;
		
		public BorderPixelSelector(Image<DoubleType> image)
		{
			this.cursor = image.createLocalizableByDimCursor();
			this.imageDims = image.getDimensions();
		}

		@Override
		public boolean include(int[] position, double sample)
		{
			if (sample != 7)
				return false;
			
			int[] neighPos = new int[2];

			// look left
			if (position[0] > 0)
			{
				neighPos[0] = position[0]-1;
				neighPos[1] = position[1];
				this.cursor.setPosition(neighPos);
				if (this.cursor.getType().get() != 7)
					return true;
			}

			// look right
			if (position[0] < this.imageDims[0]-1)
			{
				neighPos[0] = position[0]+1;
				neighPos[1] = position[1];
				this.cursor.setPosition(neighPos);
				if (this.cursor.getType().get() != 7)
					return true;
			}

			// look up
			if (position[1] > 0)
			{
				neighPos[0] = position[0];
				neighPos[1] = position[1]-1;
				this.cursor.setPosition(neighPos);
				if (this.cursor.getType().get() != 7)
					return true;
			}

			// look down
			if (position[1] < this.imageDims[1]-1)
			{
				neighPos[0] = position[0];
				neighPos[1] = position[1]+1;
				this.cursor.setPosition(neighPos);
				if (this.cursor.getType().get() != 7)
					return true;
			}

			
			return false;
		}
		
	}

	private void populateTestImage(Image<DoubleType> image)
	{
		Random rng = new Random();
		
		Cursor<DoubleType> cursor = image.createCursor();

		while (cursor.hasNext())
		{
			cursor.fwd();
			if (rng.nextDouble() < 0.7)
				cursor.getType().set(7);
		}
	}
	
	private void queryEdgeOfRegion()
	{
		int[] dimensions = new int[]{10,20};
		
		int[] origin = new int[dimensions.length];
		
		Image<DoubleType> image = createTestImage(dimensions);

		populateTestImage(image);
		
		PixCounter counter = new PixCounter();
		
		QueryOperation<DoubleType> operation = new QueryOperation<DoubleType>(image, origin, dimensions, counter);
		
		operation.setSelectionFunction(new BorderPixelSelector(image));
		
		operation.execute();
		
		System.out.println("number of border pixels = "+counter.pixelCount);
	}
}
