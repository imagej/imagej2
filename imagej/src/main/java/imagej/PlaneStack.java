package imagej;

import java.util.ArrayList;

import imagej.process.ImageUtils;
import imagej.process.Index;
import imagej.process.Span;
import imagej.process.TypeManager;
import imagej.process.operation.SetPlaneOperation;
import imagej.process.operation.SetPlaneOperation.DataType;
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
	
	// ONE WAY
	private Image<T> stack;
	// OTHER WAY
	//private ArrayList<Image<T>> stack;
	private int planeWidth;
	private int planeHeight;
	private ContainerFactory factory;
	private RealType<?> type;
	
	//****************** private interface
	
	private Image<T> createPlane(RealType<T> type, ContainerFactory cFact, Object data, DataType dType)
	{
		int[] dimensions = new int[]{planeWidth, planeHeight, 1};
		
		Image<T> image = ImageUtils.createImage(type, cFact, dimensions);
		
		this.type = type;
		
		SetPlaneOperation<T> planeOp = new SetPlaneOperation<T>(image, Index.create(3), data, dType);
		
		planeOp.execute();
		
		return image;
	}

	// NOTE - assumes atPosition has already been checked for validity
	private void copyDataBeforePosition(Image<T> origData, Image<T> newData, int atPosition)
	{
		if (atPosition == 0)  // nothing to copy
			return;
		
		int[] srcOrigin = Index.create(3);
		int[] dstOrigin = Index.create(3);
		int[] span = Span.create(new int[]{this.planeWidth, this.planeHeight, atPosition});
		
		ImageUtils.copyFromImageToImage(origData, srcOrigin, span, newData, dstOrigin, span);
	}
	
	// NOTE - assumes atPosition has already been checked for validity
	private void copyDataAtPosition(Image<T> planeData, Image<T> newData, int atPosition)
	{
		int[] srcOrigin = Index.create(3);
		int[] dstOrigin = Index.create(0,0,new int[]{atPosition});
		int[] span = Span.create(new int[]{this.planeWidth, this.planeHeight, 1});
		
		ImageUtils.copyFromImageToImage(planeData, srcOrigin, span, newData, dstOrigin, span);
	}
	
	// NOTE - assumes atPosition has already been checked for validity
	private void copyDataAfterPosition(Image<T> origData, Image<T> newData, int atPosition)
	{
		int remainingPlanes = origData.getDimension(2) - atPosition; 
			
		if (remainingPlanes <= 0)
			return;
		
		int[] srcOrigin = Index.create(0,0,new int[]{atPosition});
		int[] dstOrigin = Index.create(0,0,new int[]{atPosition+1});
		int[] span = Span.create(new int[]{this.planeWidth, this.planeHeight, remainingPlanes});
		
		ImageUtils.copyFromImageToImage(origData, srcOrigin, span, newData, dstOrigin, span);
	}

	private void insertPlane(int atPosition, Object data, int dataLen, RealType<T> desiredType, DataType dType)
	{
		if (dataLen != this.planeWidth*this.planeHeight)
			throw new IllegalArgumentException("insertPlane(): input data does not match XY dimensions of stack");
		
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
			// ONE WAY
			this.stack = newPlane;
			/* OTHER WAY
			this.stack = new ArrayList<Image<T>>();
			this.stack.add(newPlane);
			*/
		}
		else  // we already have some entries in stack
		{
			// need to type check against existing planes
			// ONE WAY
			RealType<?> myType = ImageUtils.getType(this.stack);
			// OTHER WAY
			// RealType<?> myType = ImageUtils.getType(this.stack.get(0));
			
			if ( ! (TypeManager.sameKind(desiredType, myType)) )
				throw new IllegalArgumentException("insertPlane(): type clash - " + myType + " & " + desiredType);

			/*  ONE WAY
			*/
			int[] newDims = new int[]{this.planeWidth, this.planeHeight, this.getEndPosition()+1};
			
			Image<T> newStack = ImageUtils.createImage(desiredType, this.factory, newDims);

			copyDataBeforePosition(this.stack, newStack, atPosition);
			copyDataAtPosition(newPlane, newStack, atPosition);
			copyDataAfterPosition(this.stack, newStack, atPosition);

			this.stack = newStack;

			// OTHER WAY
			// this.stack.add(atPosition, newPlane);
		}
	}
	
	private void insertUnsignedPlane(int atPosition, byte[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new UnsignedByteType(), DataType.UBYTE);
	}
	
	private void insertSignedPlane(int atPosition, byte[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new ByteType(), DataType.BYTE);
	}
	
	private void insertUnsignedPlane(int atPosition, short[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new UnsignedShortType(), DataType.USHORT);
	}
	
	private void insertSignedPlane(int atPosition, short[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new ShortType(), DataType.SHORT);
	}
	
	private void insertUnsignedPlane(int atPosition, int[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new UnsignedIntType(), DataType.UINT);
	}
	
	private void insertSignedPlane(int atPosition, int[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new IntType(), DataType.INT);
	}
	
	private void insertUnsignedPlane(int atPosition, long[] data)
	{
		throw new IllegalArgumentException("PlaneStack::insertUnsignedPlane(long[]): unsigned long data not supported");
	}
	
	private void insertSignedPlane(int atPosition, long[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new LongType(), DataType.LONG);
	}
	
	private void insertPlane(int atPosition, float[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new FloatType(), DataType.FLOAT);
	}
	
	private void insertPlane(int atPosition, double[] data)
	{
		insertPlane(atPosition, data, data.length, (RealType) new DoubleType(), DataType.DOUBLE);
	}

	//****************** public interface 
	
	public PlaneStack(int width, int height, ContainerFactory factory)
	{
		this.stack = null;
		this.planeWidth = width;
		this.planeHeight = height;
		this.factory = factory;
		this.type = null;
	}

	// ONE WAY
	public Image<T> getStorage()
	{
		return this.stack;
	}
	
	/* OTHER WAY
	public Image<T> getStorage(int i)
	{
		if (this.stack == null)
			return null;
		
		return this.stack.get(i);
	}
	*/
	
	public long getNumPlanes()
	{
		if (this.stack == null)
			return 0;
		
		//ONE WAY
		return ImageUtils.getTotalPlanes(getStorage().getDimensions());
		// OTHER WAY
		//return this.stack.size();
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
	
	public void deletePlane(int planeNumber)  // since multidim an int could be too small but can't avoid
	{
		if (this.stack == null)
			throw new IllegalArgumentException("PlaneStack::deletePlane() - cannot delete from empty stack");

		int endPosition = getEndPosition();
		if ((planeNumber < 0) || (planeNumber >= endPosition))
			throw new IllegalArgumentException("PlaneStack::deletePlane() - planeNumber out of bounds - "+planeNumber+" outside [0,"+endPosition+")");

		/* ONE WAY
		this.stack.remove(planeNumber);
		*/

		/* OTHER WAY
		*/
		int[] newDims = new int[]{this.planeWidth, this.planeHeight, this.stack.getDimension(2)-1};
		
		if (newDims[2] == 0)
		{
			this.stack = null;
			return;
		}
		
		// create a new image one plane smaller than existing
		Image<T> newImage = ImageUtils.createImage((RealType<T>)this.type, this.stack.getContainerFactory(), newDims);
		
		int[] srcOrigin, srcSpan, dstOrigin, dstSpan;

		// TODO refactor and modify/use copyFrom() routines that are above.
		
		// copy planes from slices before the slice number
		if (planeNumber > 0)
		{
			srcOrigin = Index.create(3);
			srcSpan = Span.create(new int[]{this.planeWidth, this.planeHeight, planeNumber});
			dstOrigin = srcOrigin.clone();
			dstSpan = srcSpan.clone();
			ImageUtils.copyFromImageToImage(this.stack, srcOrigin, srcSpan, newImage, dstOrigin, dstSpan);
		}
		
		// copy planes from slices after the slice number
		int totalPlanes = endPosition;
		if (planeNumber < totalPlanes-1)
		{
			srcOrigin = Index.create(new int[]{0, 0, planeNumber+1});
			srcSpan = Span.create(new int[]{this.planeWidth, this.planeHeight, totalPlanes-1-planeNumber});
			dstOrigin = Index.create(new int[]{0, 0, planeNumber});
			dstSpan = srcSpan.clone();
			ImageUtils.copyFromImageToImage(this.stack, srcOrigin, srcSpan, newImage, dstOrigin, dstSpan);
		}
		
		this.stack = newImage;
	}
}
