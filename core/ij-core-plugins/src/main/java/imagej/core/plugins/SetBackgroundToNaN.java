package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.FloatType;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>NaN Background"
)
public class SetBackgroundToNaN implements ImageJPlugin
{
	// ********** instance variables ****************************************************************
	
	@Parameter
	private Dataset input;

	@Parameter(output=true)
	private Dataset output;

	@Parameter(label="Low threshold")
	private double loThreshold;
	
	@Parameter(label="High threshold")
	private double hiThreshold;
	
	// ***************  constructor ***************************************************************
	
	/** basic constructor */
	public SetBackgroundToNaN()
	{
	}
	
	// ***************  public interface ***************************************************************

	/** runs this plugin */
	@Override
	public void run()
	{
		if (input == null)  // TODO - temporary code to test these until IJ2 plugins can correctly fill a Dataset @Parameter
		{
			Image<FloatType> junkImage = Dataset.createPlanarImage("",new FloatType(), new int[]{200,200});
			Cursor<FloatType> cursor = junkImage.createCursor();
			int index = 0;
			for (FloatType pixRef : cursor)
				pixRef.set(index++);
			cursor.close();
			input = new Dataset(junkImage);
		}
		
		if (input.isFloat())
		{
			OutputAlgorithm algorithm = new SetToNaN(input, loThreshold, hiThreshold);
			ImglibOutputAlgorithmRunner runner = new ImglibOutputAlgorithmRunner(algorithm);
			output = runner.run();
		}
	}
	
	// ********** private interface ****************************************************************
	
	/** private implementation of algorithm */
	private class SetToNaN implements OutputAlgorithm
	{
		private Image<?> inputImage;
		private Image<?> outputImage;
		private double loThreshold;
		private double hiThreshold;
		private String errMessage = "No error";

		public SetToNaN(Dataset in, double loThreshold, double hiThreshold)
		{
			inputImage = in.getImage();  // TODO - failure is a real possibility here (example: pass Image<FloatType> when declared plugin of DoubleType
			outputImage = inputImage.createNewImage();
			this.loThreshold = loThreshold;
			this.hiThreshold = hiThreshold;
		}
		
		@Override
		public boolean checkInput()
		{
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
			Cursor<? extends RealType<?>> inputCursor = (Cursor<? extends RealType<?>>) inputImage.createCursor();
			Cursor<? extends RealType<?>> outputCursor = (Cursor<? extends RealType<?>>) outputImage.createCursor();
			
			while (inputCursor.hasNext() && outputCursor.hasNext())
			{
				double inputValue = inputCursor.next().getRealDouble();
				
				if ((inputValue < loThreshold) || (inputValue > hiThreshold))
					outputCursor.next().setReal(Double.NaN);
				else
					outputCursor.next().setReal(inputValue);
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
}
