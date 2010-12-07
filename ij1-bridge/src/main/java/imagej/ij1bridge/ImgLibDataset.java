package imagej.ij1bridge;

import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.container.basictypecontainer.PlanarAccess;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import imagej.MetaData;
import imagej.data.Type;
import imagej.dataset.Dataset;
import imagej.dataset.PlanarDatasetFactory;
import imagej.dataset.RecursiveDataset;
import imagej.imglib.TypeManager;
import imagej.imglib.process.ImageUtils;
import imagej.process.Index;

public class ImgLibDataset<T extends RealType<T>> implements Dataset, RecursiveDataset
{
	//************ instance variables ********************************************************
	private Dataset dataset;
	private Image<?> shadowImage;
	private Type ijType;
	private RealType<?> realType;
	private ArrayContainerFactory planarFactory;

	// Notes
	// This is an ImgLib aware Dataset. Constructor takes an imglib image and makes a dataset whose primitive access arrays match.
	// Also overrides the add/remove subset calls to set an invalid flag. Then user should always call its method called getImage() that returns
	// a cached Image and cached Image is recreated and populated with correct primitive access when invalid. User should not cache image from the
	// getImage() call.
	
	//************ constructor ********************************************************
		
	public ImgLibDataset(Image<T> image)
	{
		this.shadowImage = image;
		
		this.planarFactory = new ArrayContainerFactory();
		
		this.planarFactory.setOptimizedContainerUse(true);
		
		this.realType = ImageUtils.getType(image);

		this.ijType = TypeManager.getIJType(realType);
		
		int[] dimensions = image.getDimensions();
		
		this.dataset = new PlanarDatasetFactory().createDataset(this.ijType, dimensions);
		
		int subDimensionLength = dimensions.length-2;
	
		int[] position = Index.create(subDimensionLength);
		
		int[] origin = Index.create(subDimensionLength);

		int[] span = new int[subDimensionLength];
		for (int i = 0; i < subDimensionLength; i++)
			span[i] = dimensions[i];
		
		PlanarAccess<ArrayDataAccess<?>> access = ImageUtils.getPlanarAccess(this.shadowImage);
		
		int planeNum = 0;
		
		while (Index.isValid(position, origin, span))
		{
			Dataset plane = this.dataset.getSubset(position);
			ArrayDataAccess<?> arrayAccess = access.getPlane(planeNum++);
			plane.setData(arrayAccess.getCurrentStorageArray());
			Index.increment(position, origin, span);
		}
		
		// TODO - have a factory that takes a list of planerefs and builds a dataset without allocating data unnecessarily
	}

	//************ public interface ********************************************************

	@Override
	public int[] getDimensions()
	{
		return this.dataset.getDimensions();
	}

	@Override
	public Type getType()
	{
		return this.dataset.getType();
	}

	@Override
	public MetaData getMetaData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMetaData(MetaData metadata)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isComposite()
	{
		return this.dataset.isComposite();
	}

	@Override
	public Dataset getParent()
	{
		return this.dataset.getParent();
	}

	@Override
	public void setParent(Dataset dataset)
	{
		this.dataset.setParent(dataset);
	}

	@Override
	public Object getData()
	{
		return this.dataset.getData();
	}

	@Override
	public void setData(Object data)
	{
		this.dataset.setData(data);
	}

	@Override
	public void releaseData()
	{
		this.dataset.releaseData();
	}

	@Override
	public Dataset insertNewSubset(int position)
	{
		this.shadowImage = null;
		return this.dataset.insertNewSubset(position);
	}

	@Override
	public Dataset removeSubset(int position)
	{
		this.shadowImage = null;
		return this.dataset.removeSubset(position);
	}

	@Override
	public Dataset getSubset(int position)
	{
		return this.dataset.getSubset(position);
	}

	@Override
	public Dataset getSubset(int[] index)
	{
		return this.dataset.getSubset(index);
	}

	@Override
	public double getDouble(int[] position)
	{
		return this.dataset.getDouble(position);
	}

	@Override
	public void setDouble(int[] position, double value)
	{
		this.dataset.setDouble(position, value);
	}

	@Override
	public double getDouble(int[] index, int axis)
	{
		RecursiveDataset ds = (RecursiveDataset) this.dataset;
		
		return ds.getDouble(index, axis);
	}

	@Override
	public void setDouble(int[] index, int axis, double value)
	{
		RecursiveDataset ds = (RecursiveDataset) this.dataset;
		
		ds.setDouble(index, axis, value);
	}

	@Override
	public Dataset getSubset(int[] partialIndex, int axis)
	{
		RecursiveDataset ds = (RecursiveDataset) this.dataset;
		
		return ds.getSubset(partialIndex, axis);
	}

	public Image<?> getImage()
	{
		if (this.shadowImage == null)
		{
			int[] dimensions = this.dataset.getDimensions();
			
			this.shadowImage = ImageUtils.createImage(this.realType, this.planarFactory, dimensions);

			int subDimensionLength = dimensions.length-2;
			
			int[] position = Index.create(subDimensionLength);
			
			int[] origin = Index.create(subDimensionLength);

			int[] span = new int[subDimensionLength];
			for (int i = 0; i < subDimensionLength; i++)
				span[i] = dimensions[i];
			
			PlanarAccess<ArrayDataAccess<?>> access = ImageUtils.getPlanarAccess(this.shadowImage);

			int planeNum = 0;
			
			while (Index.isValid(position, origin, span))
			{
				Dataset plane = this.dataset.getSubset(position);
				Object array = plane.getData();
				access.setPlane(planeNum, ImageUtils.makeArray(array));
				Index.increment(position, origin, span);
			}
			
		}
		
		return this.shadowImage;
	}
}
