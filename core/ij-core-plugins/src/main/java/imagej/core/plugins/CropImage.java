package imagej.core.plugins;

import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import imagej.model.Dataset;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Image>Crop"
)
public class CropImage extends ImglibOutputAlgorithmPlugin
{
	@Parameter
	Dataset in;
	
	@Parameter
	private int minX;

	@Parameter
	private int minY;

	@Parameter
	private int maxX;

	@Parameter
	private int maxY;

	private String errMessage = "No error";
	private Image<? extends RealType<?>> inputImage;
	private Image<? extends RealType<?>> outputImage;
	
	public CropImage()
	{
	}
	
	@Override
	public void run()
	{
		setAlgorithm(new CropAlgorithm());
		super.run();
	}
	
	private class CropAlgorithm implements OutputAlgorithm
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

			inputImage = (Image<? extends RealType<?>>) in.getImage();
			
			int[] newDimensions = inputImage.getDimensions().clone();
			
			newDimensions[0] = maxX - minX + 1;
			newDimensions[1] = maxY - minY + 1;
				
			outputImage = inputImage.createNewImage(newDimensions);
			
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

		@Override
		public Image<?> getResult()
		{
			return outputImage;
		}
		
	}
}
