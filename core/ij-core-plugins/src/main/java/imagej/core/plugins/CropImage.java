package imagej.core.plugins;

import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

// TODO - minX, minY, maxX, maxY are treated as harvested variables (for simple testing). Should make them passed
//        in parameters to constructor

// TODO - the IJ1 crop plugin can do a lot more than this can. Investigate its abilities and replicate them as needed

/**
 * Creates an output Dataset by cropping an input Dataset in X & Y. Works on images of any dimensionality. X & Y are assumed
 * to be the first two dimensions.
 *
 * @author Barry DeZonia
 *
 */
@Plugin(
		menuPath = "PureIJ2>Image>Crop"
)
public class CropImage implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	Dataset input;
	
	@Parameter(output=true)
	Dataset output;
	
	@Parameter
	private int minX;

	@Parameter
	private int minY;

	@Parameter
	private int maxX;

	@Parameter
	private int maxY;
	
	// ***************  constructor ***************************************************************

	/** default constructor */
	public CropImage()
	{
	}
	
	// ***************  public interface ***************************************************************

	/** runs the crop process and returns the output as a Dataset */
	@Override
	public void run()
	{
		OutputAlgorithm algorithm = new CropAlgorithm();
		ImglibOutputAlgorithmRunner runner = new ImglibOutputAlgorithmRunner(algorithm);
		output = runner.run();
	}
	
	// ***************  private interface ***************************************************************

	/** CropAlgorithm is responsible for creating the cropped image from the input Dataset. It is an Imglib OutputAlgorithm. */
	private class CropAlgorithm implements OutputAlgorithm
	{
		private String errMessage = "No error";
		private Image<? extends RealType<?>> inputImage;
		private Image<? extends RealType<?>> outputImage;

		/** returns false if there is any problem with the input data. returns true otherwise. */
		@Override
		public boolean checkInput()
		{
			if (input == null)  // TODO - temporary code to test these until IJ2 plugins can correctly fill a Dataset @Parameter
			{
				Image<UnsignedShortType> junkImage = Dataset.createPlanarImage("", new UnsignedShortType(), new int[]{200,200});
				Cursor<UnsignedShortType> cursor = junkImage.createCursor();
				int index = 0;
				for (UnsignedShortType pixRef : cursor)
					pixRef.set(index++);
				cursor.close();
				input = new Dataset(junkImage);
			}

			inputImage = (Image<? extends RealType<?>>) input.getImage();
			
			int[] newDimensions = inputImage.getDimensions().clone();
			
			newDimensions[0] = maxX - minX + 1;
			newDimensions[1] = maxY - minY + 1;
				
			outputImage = inputImage.createNewImage(newDimensions);
			
			return true;
		}

		/** returns the current error message */
		@Override
		public String getErrorMessage()
		{
			return errMessage;
		}

		/** runs the cropping process */
		@Override
		public boolean process()
		{
			LocalizableByDimCursor<? extends RealType<?>> inputCursor = inputImage.createLocalizableByDimCursor();
			LocalizableByDimCursor<? extends RealType<?>> outputCursor = outputImage.createLocalizableByDimCursor();

			int[] tmpPosition = outputImage.createPositionArray();

			while (outputCursor.hasNext())
			{
				outputCursor.next();
				
				outputCursor.getPosition(tmpPosition);
				
				tmpPosition[0] += minX;
				tmpPosition[1] += minY;
				
				inputCursor.setPosition(tmpPosition);
				
				double value = inputCursor.getType().getRealDouble();
				
				outputCursor.getType().setReal(value);
			}

			inputCursor.close();
			outputCursor.close();
			
			return true;
		}

		/** returns the resulting output image. not valid before checkInput() and process() have been called. */
		@Override
		public Image<?> getResult()
		{
			return outputImage;
		}
		
	}
}
