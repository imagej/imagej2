package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.Parameter;
import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.outofbounds.OutOfBoundsStrategyFactory;
import mpicbg.imglib.outofbounds.OutOfBoundsStrategyMirrorFactory;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

/**
 * Neighborhood3x3Operation - the base class for 3x3 neighborhood operation plugins such as
 *  SmoothDataValues, SharpenDataValues, and FindEdges
 *
 * @author Barry DeZonia
 *
 * @param <T>
 */
public class Neighborhood3x3Operation extends ImglibOutputAlgorithmPlugin
{
	@Parameter
	private Dataset in;
	
	private String errMessage = "No error";
	
	private Neighborhood3x3Watcher watcher; 

	interface Neighborhood3x3Watcher
	{
		void setup();
		void initializeNeighborhood(int[] position);
		void visitLocation(int dx, int dy, double value);
		double calcOutputValue();
	}
	
	public Neighborhood3x3Operation()
	{
	}
	
	protected void setWatcher(Neighborhood3x3Watcher watcher)
	{
		this.watcher = watcher;
	}
	
	@Override
	public void run()
	{
		if (in == null)  // TODO - temporary code to test these until IJ2 plugins can correctly fill a Dataset @Parameter
		{
			Image<UnsignedShortType> junkImage = Dataset.createPlanarImage("", new UnsignedShortType(), new int[]{200,200});
			Cursor<UnsignedShortType> cursor = junkImage.createCursor();
			int index = 0;
			for (UnsignedShortType pixRef : cursor)
				pixRef.set((index++)%243);
			cursor.close();
			in = new Dataset(junkImage);
		}
		
		OutputAlgorithm algorithm = new Neighborhood3x3Algorithm(in);
		
		setAlgorithm(algorithm);
		
		super.run();
	}
	
	/** implements IJ1's ImageProcessor::filter(FIND_EDGES) algorithm within the structures of imglib's OutputAlgorithm */
	private class Neighborhood3x3Algorithm implements OutputAlgorithm
	{
		private Image<?> inputImage;
		private Image<?> outputImage;
		
		public Neighborhood3x3Algorithm(Dataset input)
		{
			inputImage = input.getImage();
			outputImage = inputImage.createNewImage();
		}

		@Override
		public boolean checkInput()
		{
			if (watcher == null)
			{
				errMessage = "neighborhood watcher has not been initialized with setWatcher()";
				return false;
			}
			
			if (inputImage == null)
			{
				errMessage = "input image is null";
				return false;
			}
			
			if (inputImage.getNumDimensions() != 2)
			{
				errMessage = "input image is not 2d but has "+inputImage.getNumDimensions()+" dimensions";
				return false;
			}
			
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
			LocalizableByDimCursor<? extends RealType<?>> outputCursor =
					(LocalizableByDimCursor<? extends RealType<?>>) outputImage.createLocalizableByDimCursor();
			OutOfBoundsStrategyFactory factory = new OutOfBoundsStrategyMirrorFactory();
			LocalizableByDimCursor<? extends RealType<?>> inputCursor =
				(LocalizableByDimCursor<? extends RealType<?>>) inputImage.createLocalizableByDimCursor(factory);

			int[] inputPosition = new int[inputCursor.getNumDimensions()];
			int[] localInputPosition = new int[inputCursor.getNumDimensions()];
			
			watcher.setup();
			
			while (outputCursor.hasNext())
			{
				RealType<?> outputValue = outputCursor.next();
				
				outputCursor.getPosition(inputPosition);

				watcher.initializeNeighborhood(inputPosition);
				
				for (int dy = -1; dy <= 1; dy++)
				{
					localInputPosition[1] = inputPosition[1] + dy;
					for (int dx = -1; dx <= 1; dx++)
					{
						localInputPosition[0] = inputPosition[0] + dx;
						inputCursor.setPosition(localInputPosition);
						watcher.visitLocation(dx, dy, inputCursor.getType().getRealDouble());
					}
				}

                outputValue.setReal(watcher.calcOutputValue());
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
