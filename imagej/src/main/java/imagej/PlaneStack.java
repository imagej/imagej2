package imagej;

import imagej.SampleInfo.ValueType;
import imagej.process.ImageUtils;
import imagej.process.Index;
import imagej.process.Span;
import imagej.process.TypeManager;
import imagej.process.operation.SetPlaneOperation;
import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.basictypecontainer.PlanarAccess;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

/**
 * PlaneStack is the class where Imglib images are actually stored in ImageJ. Used by ImageStack as the backing
 * data store to ImagePlus.
 */
public class PlaneStack<T extends RealType<T>>
{
	//****************** instance variables

	private Image<T> stack;
	private int planeWidth;
	private int planeHeight;
	private ContainerFactory factory;
	private RealType<?> type;

	//****************** private interface

	/** creates a single plane Imglib image using passed in data. */
	private Image<T> createPlane(RealType<T> type, ContainerFactory cFact, Object data, ValueType dType)
	{
		int[] dimensions = new int[]{planeWidth, planeHeight, 1};

		Image<T> image = ImageUtils.createImage(type, cFact, dimensions);

		PlanarAccess<ArrayDataAccess<?>> planar = ImageUtils.getPlanarAccess(image);

		if (planar != null)
		{
			int[] planePosition = Index.create(1);

			ImageUtils.setPlane(image, planePosition, data);
		}
		else
		{
			SetPlaneOperation<T> planeOp = new SetPlaneOperation<T>(image, Index.create(3), data, dType);

			planeOp.execute();
		}

		return image;
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	/** copies a given number of consecutive planes from a source image to a destination image */
	private void copyPlanesFromTo(int numPlanes, Image<T> srcImage, int srcPlane, Image<T> dstImage, int dstPlane)
	{
		if (numPlanes < 1)
			return;

		PlanarAccess srcImagePlanarAccess = ImageUtils.getPlanarAccess(srcImage);
		PlanarAccess dstImagePlanarAccess = ImageUtils.getPlanarAccess(dstImage);

		// if both datasets are planar
		if ((srcImagePlanarAccess != null) && (dstImagePlanarAccess != null))
		{
			// simply copy pixel references
			for (int i = 0; i < numPlanes; i++)
			{
				Object plane = srcImagePlanarAccess.getPlane(srcPlane+i);
				dstImagePlanarAccess.setPlane(dstPlane+i, plane);
			}
		}
		else // can't just copy pixel references
		{
			int[] srcOrigin = Index.create(new int[]{0, 0, srcPlane});
			int[] dstOrigin = Index.create(new int[]{0, 0, dstPlane});
			int[] span = Span.create(new int[]{this.planeWidth, this.planeHeight, numPlanes});

			ImageUtils.copyFromImageToImage(srcImage, srcOrigin, span, dstImage, dstOrigin, span);
		}
	}

	/**
	 * inserts a plane into the PlaneStack. Since Imglib images are immutable it does this by creating a new stack
	 * and copying all the old data appropriately. The new plane's contents are provided. Note that callers of this
	 * method carefully match the desired imglib type and the input data type.
	 * @param atPosition - the stack position for the new plane to occupy
	 * @param data - the plane data as an array of the appropriate type
	 * @param dataLen - the number of elements in the plane
	 * @param desiredType - the imglib type we desire the plane to be of
	 * @param dType - the ValueType of the input data
	 */
	private void insertPlane(int atPosition, Object data, int dataLen, RealType<T> desiredType, ValueType dType)
	{
		if (dataLen != this.planeWidth*this.planeHeight)
			throw new IllegalArgumentException("insertPlane(): input data does not match XY dimensions of stack - expected "+
					dataLen+" samples but got "+(this.planeWidth*this.planeHeight)+" samples");

		long numPlanesNow = getNumPlanes();

		if (atPosition < 0)
			throw new IllegalArgumentException("insertPlane(): negative insertion point requested");

		if (atPosition > numPlanesNow+1)
			throw new IllegalArgumentException("insertPlane(): insertion point too large");

		Image<T> newPlane = createPlane(desiredType, this.factory, data, dType);

		if (this.stack == null)
		{
			this.stack = newPlane;
			this.type = desiredType;
		}
		else  // we already have some entries in stack
		{
			// need to type check against existing planes
			if ( ! (TypeManager.sameKind(desiredType, this.type)) )
				throw new IllegalArgumentException("insertPlane(): type clash - " + this.type + " & " + desiredType);

			int[] newDims = new int[]{this.planeWidth, this.planeHeight, this.getEndPosition()+1};

			Image<T> newStack = ImageUtils.createImage(desiredType, this.factory, newDims);

			copyPlanesFromTo(atPosition, this.stack, 0, newStack, 0);
			copyPlanesFromTo(1, newPlane, 0, newStack, atPosition);
			copyPlanesFromTo(getEndPosition()-atPosition, this.stack, atPosition, newStack, atPosition+1);

			this.stack = newStack;
		}
	}

	/** a helper method to insert unsigned byte plane data into a PlaneStack */
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertUnsignedPlane(int atPosition, byte[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new UnsignedByteType(), ValueType.UBYTE);
	}

	/** a helper method to insert signed byte plane data into a PlaneStack */
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertSignedPlane(int atPosition, byte[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new ByteType(), ValueType.BYTE);
	}

	/** a helper method to insert unsigned short plane data into a PlaneStack */
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertUnsignedPlane(int atPosition, short[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new UnsignedShortType(), ValueType.USHORT);
	}

	/** a helper method to insert signed short plane data into a PlaneStack */
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertSignedPlane(int atPosition, short[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new ShortType(), ValueType.SHORT);
	}

	/** a helper method to insert unsigned int plane data into a PlaneStack */
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertUnsignedPlane(int atPosition, int[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new UnsignedIntType(), ValueType.UINT);
	}

	/** a helper method to insert signed int plane data into a PlaneStack */
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertSignedPlane(int atPosition, int[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new IntType(), ValueType.INT);
	}

	/** a helper method to insert unsigned long plane data into a PlaneStack */
	private void insertUnsignedPlane(int atPosition, long[] data)
	{
		throw new IllegalArgumentException("PlaneStack::insertUnsignedPlane(long[]): unsigned long data not supported");
	}

	/** a helper method to insert signed long plane data into a PlaneStack */
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertSignedPlane(int atPosition, long[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new LongType(), ValueType.LONG);
	}

	/** a helper method to insert float plane data into a PlaneStack */
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertPlane(int atPosition, float[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new FloatType(), ValueType.FLOAT);
	}

	/** a helper method to insert double plane data into a PlaneStack */
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertPlane(int atPosition, double[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new DoubleType(), ValueType.DOUBLE);
	}

	//****************** public interface

	/** constructor - create a PlaneStack directly from an Imglib image */
	public PlaneStack(Image<T> stack) {
		this.stack = stack;
		this.planeWidth = ImageUtils.getWidth(stack);
		this.planeHeight = ImageUtils.getHeight(stack);
		this.factory = null;  // TODO - CTR code - ask what he wants to do. causes problems with adding slices to stacks
		this.type = ImageUtils.getType(stack);
	}

	/** constructor - create an empty PlaneStack given (x,y) dimensions and a factory for creating an Imglib image.
	 * Note that the backing type is not set until the first slice is added to the PlaneStack. */
	public PlaneStack(int width, int height, ContainerFactory factory)
	{
		this.stack = null;
		this.planeWidth = width;
		this.planeHeight = height;
		this.factory = factory;
		this.type = null;
	}

	/** returns the backing Imglib image */
	public Image<T> getStorage()
	{
		return this.stack;
	}

	/** returns the number of planes in the PlaneStack */
	public long getNumPlanes()
	{
		if (this.stack == null)
			return 0;

		return ImageUtils.getTotalPlanes(getStorage().getDimensions());
	}

	/** returns the end position of the PlaneStack where a new plane can be added */
	public int getEndPosition()
	{
		long totalPlanes = getNumPlanes();

		if (totalPlanes > Integer.MAX_VALUE)
			throw new IllegalArgumentException("PlaneStack()::getEndPosition() - too many planes");

		return (int) totalPlanes;

	}

	/**
	 * inserts a plane into the PlaneStack
	 * @param atPosition - the position within the stack where the plane should be inserted. other planes moved to make way
	 * @param unsigned - a boolean specifying whether input data is signed or unsigned
	 * @param data - an array of values representing the plane
	 */
	public void insertPlane(int atPosition, boolean unsigned, Object data)
	{
		if (data instanceof byte[])
		{
			if (unsigned)
				insertUnsignedPlane(atPosition,(byte[])data);
			else
				insertSignedPlane(atPosition,(byte[])data);
		}
		else if (data instanceof short[])
		{
			if (unsigned)
				insertUnsignedPlane(atPosition,(short[])data);
			else
				insertSignedPlane(atPosition,(short[])data);
		}
		else if (data instanceof int[])
		{
			if (unsigned)
				insertUnsignedPlane(atPosition,(int[])data);
			else
				insertSignedPlane(atPosition,(int[])data);
		}
		else if (data instanceof long[])
		{
			if (unsigned)
				insertUnsignedPlane(atPosition,(long[])data);
			else
				insertSignedPlane(atPosition,(long[])data);
		}
		else if (data instanceof float[])
		{
			insertPlane(atPosition,(float[])data);
		}
		else if (data instanceof double[])
		{
			insertPlane(atPosition,(double[])data);
		}
		else
			throw new IllegalArgumentException("PlaneStack::insertPlane(Object): unknown data type passed in - "+data.getClass());
	}

	/**
	 * adds a plane to the end of the PlaneStack
	 * @param unsigned - a boolean specifying whether input data is signed or unsigned
	 * @param data - an array of values representing the plane
	 */
	public void addPlane(boolean unsigned, Object data)
	{
		insertPlane(getEndPosition(), unsigned, data);
	}

	/**
	 * deletes a plane from a PlaneStack
	 * @param planeNumber - the index of the plane to delete
	 */
	@SuppressWarnings("unchecked")
	public void deletePlane(int planeNumber)  // since multidim an int could be too small but can't avoid
	{
		if (this.stack == null)
			throw new IllegalArgumentException("PlaneStack::deletePlane() - cannot delete from empty stack");

		int endPosition = getEndPosition();
		if ((planeNumber < 0) || (planeNumber >= endPosition))
			throw new IllegalArgumentException("PlaneStack::deletePlane() - planeNumber out of bounds - "+planeNumber+" outside [0,"+endPosition+")");

		int[] newDims = new int[]{this.planeWidth, this.planeHeight, this.stack.getDimension(2)-1};

		if (newDims[2] == 0)
		{
			this.stack = null;
			return;
		}

		// create a new image one plane smaller than existing
		Image<T> newImage = ImageUtils.createImage((RealType<T>)this.type, this.factory, newDims);

		copyPlanesFromTo(planeNumber, this.stack, 0, newImage, 0);
		copyPlanesFromTo(getEndPosition()-planeNumber-1, this.stack, planeNumber+1, newImage, planeNumber);

		this.stack = newImage;
	}
}
