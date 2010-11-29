package imagej.ij1bridge;

import java.lang.reflect.Array;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import imagej.DataEncoding;
import imagej.Dataset;
import imagej.EncodingManager;
import imagej.MetaData;
import imagej.SampleInfo;
import imagej.UserType;
import imagej.Utils;
import imagej.imglib.TypeManager;
import imagej.imglib.process.ImageUtils;
import imagej.SampleManager;

public class ImgLibDataset<T extends RealType<T>> implements Dataset
{
	//************ instance variables ********************************************************
	
	private Image<T> image;
	private MetaData metadata;
	private UserType userType;
	private SampleInfo sampleInfo;
	
	/** a per thread variable. a cursor used to facilitate fast access to the ImgLib image. */
	private ThreadLocal<LocalizableByDimCursor<T>> cachedCursor =
		new ThreadLocal<LocalizableByDimCursor<T>>()
		{
			@Override
			protected LocalizableByDimCursor<T> initialValue() {
				return image.createLocalizableByDimCursor();
			}

			@Override
			protected void finalize() throws Throwable {
				try {
					cachedCursor.get().close();
					//System.out.println("closing cursor at "+System.nanoTime());
				} finally {
					super.finalize();
				}
			}
		};
	
	//************ constructor ********************************************************
		
	public ImgLibDataset(Image<T> image)
	{
		this.image = image;
		
		RealType<?> iType = ImageUtils.getType(this.image);

		this.userType = TypeManager.getUserType(iType);

		this.sampleInfo = SampleManager.getSampleInfo(this.userType);
		
		this.metadata = new MetaData();
		this.metadata.setLabel(image.getName());
		//this.metadata.setAxisLabels(axisLabels);
	}
	
	//************ public interface ********************************************************
	
	@Override
	public MetaData getMetaData()
	{
		return this.metadata;
	}

	@Override
	public int[] getDimensions()
	{
		return image.getDimensions();
	}

	@Override
	public SampleInfo getSampleInfo()
	{
		return this.sampleInfo;
	}

	@Override
	public DataEncoding getEncoding()
	{
		return EncodingManager.getEncoding(this.userType);
	}

	@Override
	public double getDouble(int[] index)
	{
		cachedCursor.get().setPosition(index);
		return cachedCursor.get().getType().getRealDouble();
	}

	@Override
	public void setDouble(int[] index, double value)
	{
		cachedCursor.get().setPosition(index);
		cachedCursor.get().getType().setReal(value);
	}

	@Override
	public Dataset getSubset(int[] position, int[] spans)
	{
		return null;
	}

	/** remove the N-1 dimensional subset located at given position along given axis from the underlying dataset */
	@Override
	public Dataset destroySubset(int axis, int position)
	{
		return null;
	}

	/** create an N-1 dimensional subset at given position along given axis within the underlying dataset */
	@Override
	public Dataset createSubset(int axis, int position)
	{
		return null;
	}

	@Override
	public void setData(Object data)
	{
		int[] currDimensions = getDimensions();
		
		if (this.metadata.getDirectAccessDimensionCount() == currDimensions.length)
		{
			EncodingManager.verifyTypeCompatibility(data, this.userType);
			
			int subsetElementCount = (int) Utils.getTotalSamples(currDimensions);
			
			// check that size is correct too using encoding info
			long storageRequired = EncodingManager.calcStorageUnitsRequired(getEncoding(), subsetElementCount);
			
			int storageProvided = Array.getLength(data);
			
			if (storageProvided != storageRequired)
				throw new IllegalArgumentException("given data array is of length " + storageProvided +
						" and expected an array of length " + storageRequired);
			
			//set the plane here
		}
		
		throw new IllegalArgumentException("cannot set data for a multidimensional dataset given just an input plane");
	}

	@Override
	public Object getData()
	{
		if (this.metadata.getDirectAccessDimensionCount() == getDimensions().length)
		{
		}

		throw new IllegalArgumentException("cannot get data for a multidimensional dataset asking for just a plane");
	}

	@Override
	public void releaseData()
	{
		// nothing to do
	}

}
