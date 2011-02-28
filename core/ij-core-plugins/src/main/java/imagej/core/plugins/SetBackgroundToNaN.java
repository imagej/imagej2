package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.FloatType;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "Process>NaN Background"
)
public class SetBackgroundToNaN<T extends RealType<T>> extends ImglibOutputAlgorithmPlugin<T>
{
	// ********** instance variables ****************************************************************
	
	@Parameter
	private Dataset in;

	@Parameter
	private double loThreshold;
	
	@Parameter
	private double hiThreshold;

	// ********** public interface ****************************************************************
	
	/** basic constructor */
	public SetBackgroundToNaN()
	{
	}
	
	/** runs this plugin */
	@Override
	public void run()
	{
		if (in == null)  // TODO - temporary code to test these until IJ2 plugins can correctly fill a Dataset @Parameter
		{
			Image<FloatType> junkImage = Dataset.createPlanarImage("",new FloatType(), new int[]{200,200});
			Cursor<FloatType> cursor = junkImage.createCursor();
			int index = 0;
			for (FloatType pixRef : cursor)
				pixRef.set(index++);
			cursor.close();
			in = new Dataset(junkImage);
		}
		
		if (in.isFloat())
		{
			setAlgorithm(new SetToNaN(in, loThreshold, hiThreshold));
			super.run();
		}
	}
	
	// ********** private interface ****************************************************************
	
	/** provate implementation of algorithm */
	private class SetToNaN implements OutputAlgorithm<T>
	{
		private Image<T> inputImage;
		private Image<T> outputImage;
		private double loThreshold;
		private double hiThreshold;
		
		public SetToNaN(Dataset in, double loThreshold, double hiThreshold)
		{
			inputImage = (Image<T>) in.getImage();  // TODO - failure is a real possibility here (example: pass Image<FloatType> when declared plugin of DoubleType
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
			return null;
		}

		@Override
		public boolean process()
		{
			Cursor<T> inputCursor = inputImage.createCursor();
			Cursor<T> outputCursor = outputImage.createCursor();
			
			while (inputCursor.hasNext() && outputCursor.hasNext())
			{
				T input = inputCursor.next();
				T output = outputCursor.next();
				
				double inputValue = input.getRealDouble();
				
				if ((inputValue < loThreshold) || (inputValue > hiThreshold))
					output.setReal(Double.NaN);
				else
					output.setReal(inputValue);
			}
			
			inputCursor.close();
			outputCursor.close();
			
			return true;
		}

		@Override
		public Image<T> getResult()
		{
			return outputImage;
		}
		
	}
}
