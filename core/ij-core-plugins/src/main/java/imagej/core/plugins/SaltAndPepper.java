package imagej.core.plugins;

import java.util.Random;

import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import imagej.model.Dataset;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

// TODO - IJ1's implementation works on the current ROI rectangle. This plugin works on whole plane

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Noise>Salt and Pepper"
)
public class SaltAndPepper extends ImglibOutputAlgorithmPlugin
{
	@Parameter
	private Dataset in;
	
	private Image<?> inputImage;
	private Image<?> outputImage;
	private String errMessage;
	private LocalizableByDimCursor<? extends RealType<?>> outputCursor;  // working cursor
	private int[] outputPosition;  // workspace for setting output position
	
	public SaltAndPepper()
	{
		errMessage = "No error";
		
		setAlgorithm(new SaltAndPepperAlgorithm());
	}
	
	private class SaltAndPepperAlgorithm implements OutputAlgorithm
	{
		@Override
		public boolean checkInput()
		{
			if (in == null)  // TODO - remove later
			{
				Image<UnsignedByteType> junkImage = Dataset.createPlanarImage("", new UnsignedByteType(), new int[]{200,200});
				Cursor<UnsignedByteType> cursor = junkImage.createCursor();
				int index = 0;
				for (UnsignedByteType pixRef : cursor)
					pixRef.set((index++) % 256);
				cursor.close();
				in = new Dataset(junkImage);
			}
			
			inputImage = in.getImage();
			
			if (inputImage.getNumDimensions() != 2)
			{
				errMessage = "Only 2d images supported";
				return false;
			}
			
			outputImage = inputImage.createNewImage();

			initOutputImageVariables();

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
			Random rng = new Random();

			rng.setSeed(System.currentTimeMillis());

			double percentToChange = 0.05;
			
			long numPixels = (long)(inputImage.getNumPixels() * percentToChange);
			
			int width = inputImage.getDimension(0);
			
			int height = inputImage.getDimension(1);
			
			for (long p = 0; p < numPixels/2; p++)
			{
				int randomX, randomY;
				
				randomX = rng.nextInt(width); 
				randomY = rng.nextInt(height);
				setOutputPixel(randomX, randomY, 255);
				
				randomX = rng.nextInt(width); 
				randomY = rng.nextInt(height);
				setOutputPixel(randomX, randomY, 0);
			}
			
			outputCursor.close();  // FINALLY close working cursor
			
			return true;
		}

		@Override
		public Image<?> getResult()
		{
			return outputImage;
		}
		
		private void initOutputImageVariables()
		{
			outputPosition = new int[2];
			
			LocalizableByDimCursor<? extends RealType<?>> inputCursor = 
				(LocalizableByDimCursor<? extends RealType<?>>) inputImage.createLocalizableByDimCursor();
			
			outputCursor = (LocalizableByDimCursor<? extends RealType<?>>) outputImage.createLocalizableByDimCursor();
			
			while (inputCursor.hasNext() && outputCursor.hasNext())
			{
				inputCursor.next();
				outputCursor.setPosition(inputCursor);
				
				double value = inputCursor.getType().getRealDouble();
				
				outputCursor.getType().setReal(value);
			}
			
			inputCursor.close();
			
			// **** DO NOT CLOSE outputCursor - we'll reuse it
		}
		
		private void setOutputPixel(int x, int y, double value)
		{
			outputPosition[0] = x;
			outputPosition[1] = y;
			
			outputCursor.setPosition(outputPosition);
			
			outputCursor.getType().setReal(value);
		}
	}
}
