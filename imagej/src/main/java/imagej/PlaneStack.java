package imagej;

import imagej.SampleInfo.ValueType;
import imagej.process.ImageUtils;
import imagej.process.Index;
import imagej.process.Span;
import imagej.process.TypeManager;
import imagej.process.operation.SetPlaneOperation;
import mpicbg.imglib.container.ContainerFactory;
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

public class PlaneStack<T extends RealType<T>>
{
	//****************** instance variables
	
	private Image<T> stack;
	private int planeWidth;
	private int planeHeight;
	private ContainerFactory factory;
	private RealType<?> type;
	
	//****************** private interface
	
	private Image<T> createPlane(RealType<T> type, ContainerFactory cFact, Object data, ValueType dType)
	{
		int[] dimensions = new int[]{planeWidth, planeHeight, 1};
		
		Image<T> image = ImageUtils.createImage(type, cFact, dimensions);
		
		this.type = type;
		
		SetPlaneOperation<T> planeOp = new SetPlaneOperation<T>(image, Index.create(3), data, dType);
		
		planeOp.execute();
		
		return image;
	}

	private void copyPlanesFromTo(int numPlanes, Image<T> srcImage, int srcPlane, Image<T> dstImage, int dstPlane)
	{
		if (numPlanes < 1)
			return;
		
		int[] srcOrigin = Index.create(new int[]{0, 0, srcPlane});
		int[] dstOrigin = Index.create(new int[]{0, 0, dstPlane});
		int[] span = Span.create(new int[]{this.planeWidth, this.planeHeight, numPlanes});
		
		ImageUtils.copyFromImageToImage(srcImage, srcOrigin, span, dstImage, dstOrigin, span);
	}
	
	private void insertPlane(int atPosition, Object data, int dataLen, RealType<T> desiredType, ValueType dType)
	{
		if (dataLen != this.planeWidth*this.planeHeight)
			throw new IllegalArgumentException("insertPlane(): input data does not match XY dimensions of stack - expected "+
					dataLen+" samples but got "+(this.planeWidth*this.planeHeight)+" samples");
		
		long numPlanesNow = 0;
		if (this.stack != null)
			numPlanesNow = getNumPlanes();
		
		if (atPosition < 0)
			throw new IllegalArgumentException("insertPlane(): negative insertion point requested");
			
		if (atPosition > numPlanesNow+1)
			throw new IllegalArgumentException("insertPlane(): insertion point too large");
		
		Image<T> newPlane = createPlane(desiredType, this.factory, data, dType);
		
		if (this.stack == null)
		{
			this.stack = newPlane;
		}
		else  // we already have some entries in stack
		{
			// need to type check against existing planes
			RealType<?> myType = ImageUtils.getType(this.stack);
			
			if ( ! (TypeManager.sameKind(desiredType, myType)) )
				throw new IllegalArgumentException("insertPlane(): type clash - " + myType + " & " + desiredType);

			int[] newDims = new int[]{this.planeWidth, this.planeHeight, this.getEndPosition()+1};
			
			Image<T> newStack = ImageUtils.createImage(desiredType, this.factory, newDims);

			copyPlanesFromTo(atPosition, this.stack, 0, newStack, 0);
			copyPlanesFromTo(1, newPlane, 0, newStack, atPosition);
			copyPlanesFromTo(getEndPosition()-atPosition, this.stack, atPosition, newStack, atPosition+1);
			
			this.stack = newStack;
		}
	}
	
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertUnsignedPlane(int atPosition, byte[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new UnsignedByteType(), ValueType.UBYTE);
	}
	
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertSignedPlane(int atPosition, byte[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new ByteType(), ValueType.BYTE);
	}
	
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertUnsignedPlane(int atPosition, short[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new UnsignedShortType(), ValueType.USHORT);
	}
	
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertSignedPlane(int atPosition, short[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new ShortType(), ValueType.SHORT);
	}
	
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertUnsignedPlane(int atPosition, int[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new UnsignedIntType(), ValueType.UINT);
	}
	
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertSignedPlane(int atPosition, int[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new IntType(), ValueType.INT);
	}
	
	private void insertUnsignedPlane(int atPosition, long[] data)
	{
		throw new IllegalArgumentException("PlaneStack::insertUnsignedPlane(long[]): unsigned long data not supported");
	}
	
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertSignedPlane(int atPosition, long[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new LongType(), ValueType.LONG);
	}
	
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertPlane(int atPosition, float[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new FloatType(), ValueType.FLOAT);
	}
	
	@SuppressWarnings({"rawtypes","unchecked"})
	private void insertPlane(int atPosition, double[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new DoubleType(), ValueType.DOUBLE);
	}

	//****************** public interface 
	
	public PlaneStack(Image<T> stack) {
		this.stack = stack;
		this.planeWidth = ImageUtils.getWidth(stack);
		this.planeHeight = ImageUtils.getHeight(stack);
		this.factory = null;
		this.type = ImageUtils.getType(stack);
	}
	
	public PlaneStack(int width, int height, ContainerFactory factory)
	{
		this.stack = null;
		this.planeWidth = width;
		this.planeHeight = height;
		this.factory = factory;
		this.type = null;
	}

	public Image<T> getStorage()
	{
		return this.stack;
	}
	
	public long getNumPlanes()
	{
		if (this.stack == null)
			return 0;
		
		return ImageUtils.getTotalPlanes(getStorage().getDimensions());
	}

	public int getEndPosition()
	{
		long totalPlanes = getNumPlanes();
		
		if (totalPlanes > Integer.MAX_VALUE)
			throw new IllegalArgumentException("PlaneStack()::getEndPosition() - too many planes");
		
		return (int) totalPlanes;

	}
	
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
	
	public void addPlane(boolean unsigned, Object data)
	{
		insertPlane(getEndPosition(), unsigned, data);
	}
	
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
		Image<T> newImage = ImageUtils.createImage((RealType<T>)this.type, this.stack.getContainerFactory(), newDims);
		
		copyPlanesFromTo(planeNumber, this.stack, 0, newImage, 0);
		copyPlanesFromTo(getEndPosition()-planeNumber-1, this.stack, planeNumber+1, newImage, planeNumber);
		
		this.stack = newImage;
	}
}
