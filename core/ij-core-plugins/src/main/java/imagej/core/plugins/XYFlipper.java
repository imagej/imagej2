package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.Parameter;
import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

// TODO - in IJ1 this flips single plane in active window. do we want to extend to all planes???

/**
 * XYFlipper is the base class for FlipVertically and FlipHorizontally
 * 
 * @author Barry DeZonia
 *
 * @param <T>
 */
public abstract class XYFlipper extends ImglibOutputAlgorithmPlugin
{
	@Parameter
	protected Dataset in;
	
	private String errMessage = "No error";
	
	private Image<? extends RealType<?>> outputImage;

	public XYFlipper()
	{
		OutputAlgorithm algorithm = new FlipAlgorithm();
		setAlgorithm(algorithm);
	}
	
	abstract void calcOutputPosition(int[] inputDimensions, int[] inputPosition, int[] outputPosition);
	abstract int[] calcOutputDimensions(int[] inputDimensions);
	
	private class FlipAlgorithm implements OutputAlgorithm
	{
		@Override
		public boolean checkInput()
		{
			if (in == null)  // TODO - temporary code to test these until IJ2 plugins can correctly fill a Dataset @Parameter
			{
				Image<UnsignedShortType> junkImage = Dataset.createPlanarImage("", new UnsignedShortType(), new int[]{200,200});
				Cursor<UnsignedShortType> cursor = junkImage.createCursor();
				int index = 0;
				for (UnsignedShortType pixRef : cursor)
					pixRef.set(index++);
				cursor.close();
				in = new Dataset(junkImage);
			}

			int[] inputDimensions = in.getImage().getDimensions();

			if ((calcNumNontrivialDimensions(inputDimensions) != 2) ||
					((inputDimensions[0] == 1) || (inputDimensions[1] == 1)))
			{
				errMessage = "Flipping only works on an Image made of a 2d plane of XY data";
				return false;
			}

			int[] outputDimensions = calcOutputDimensions(inputDimensions);
			
			outputImage = (Image<? extends RealType<?>>) in.getImage().createNewImage(outputDimensions);
			
			return true;
		}

		@Override
		public String getErrorMessage()
		{
			return errMessage;
		}

		@Override
		public boolean process()
		{
			Image<? extends RealType<?>> inputImage = (Image<? extends RealType<?>>) in.getImage();
			
			LocalizableByDimCursor<? extends RealType<?>> inputCursor = inputImage.createLocalizableByDimCursor();
			LocalizableByDimCursor<? extends RealType<?>> outputCursor = outputImage.createLocalizableByDimCursor();
			
			int[] inputDimensions = inputImage.getDimensions();
			
			int width = inputDimensions[0];
			int height = inputDimensions[1];
			
			int[] inputPosition = inputImage.createPositionArray();
			int[] outputPosition = outputImage.createPositionArray();
			
			for (int y = 0; y < height; y++)
			{
				inputPosition[1] = y;

				for (int x = 0; x < width; x++)
				{
					inputPosition[0] = x;
					
					calcOutputPosition(inputDimensions, inputPosition, outputPosition);
					
					inputCursor.setPosition(inputPosition);
					outputCursor.setPosition(outputPosition);
	
					double value = inputCursor.getType().getRealDouble();
					
					outputCursor.getType().setReal(value);
				}
			}
				
			inputCursor.close();
			outputCursor.close();
			
			return true;
		}

		@Override
		public Image<?> getResult()
		{
			return outputImage;
		}

		// TODO - move this somewhere to be reused
		private int calcNumNontrivialDimensions(int[] dimensions)
		{
			int numNonTrivial = 0;
			
			for (int dim : dimensions)
				if (dim > 1)
					numNonTrivial++;
			
			return numNonTrivial;
		}
	}
}
