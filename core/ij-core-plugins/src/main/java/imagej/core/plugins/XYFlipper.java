package imagej.core.plugins;

import imagej.Dimensions;
import imagej.model.Dataset;
import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

// TODO - in IJ1 this flips single plane in active window. do we want to extend to all planes???

/**
 * XYFlipper is used by FlipVertically, FlipHorizontally, Rotate90DegreesLeft and Rotate90DegreesRight
 * 
 * @author Barry DeZonia
 *
 */
public class XYFlipper implements OutputAlgorithm
{
	// ***************  instance variables ***************************************************************

	private Dataset input;
	
	private String errMessage = "No error";
	
	private Image<? extends RealType<?>> outputImage;
	
	private FlipCoordinateTransformer flipper;

	// ***************  exported interface ***************************************************************

	interface FlipCoordinateTransformer
	{
		int[] calcOutputDimensions(int[] inputDimensions);
		void calcOutputPosition(int[] inputDimensions, int[] inputPosition, int[] outputPosition);
	}
	
	// ***************  constructor ***************************************************************

	public XYFlipper(Dataset input, FlipCoordinateTransformer flipper)
	{
		this.input = input;
		this.flipper = flipper;
	}
	
	// ***************  public interface : implementation of OutputAlgorithm methods  *********************

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

		int[] inputDimensions = input.getImage().getDimensions();

		// with the use of countNontrivialDimensions() this algorithm can accept images of dim {x,y,1,...}
		if ((Dimensions.countNontrivialDimensions(inputDimensions) != 2) ||
				((inputDimensions[0] == 1) || (inputDimensions[1] == 1)))
		{
			errMessage = "Flipping only works on an Image made of a 2d plane of XY data";
			return false;
		}

		int[] outputDimensions = flipper.calcOutputDimensions(inputDimensions);
		
		outputImage = (Image<? extends RealType<?>>) input.getImage().createNewImage(outputDimensions);
		
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
		Image<? extends RealType<?>> inputImage = (Image<? extends RealType<?>>) input.getImage();
		
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
				
				flipper.calcOutputPosition(inputDimensions, inputPosition, outputPosition);
				
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
}
