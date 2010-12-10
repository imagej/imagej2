package imagej.ij1bridge;

import java.lang.reflect.Array;

import imagej.Dimensions;
import imagej.data.Type;
import imagej.data.Types;
import imagej.imglib.TypeManager;
import imagej.imglib.process.ImageUtils;
import imagej.imglib.process.operation.GetPlaneOperation;
import imagej.imglib.process.operation.SetPlaneOperation;
import imagej.process.Index;
import imagej.process.Span;
import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.basictypecontainer.PlanarAccess;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.logic.BitType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

/**
 * PlaneStack is the class where Imglib images are actually stored in ImageJ. Used by ImgLibImageStack as the backing
 * data store to ImagePlus. Should go away as we  phase out ImgLibImageStack.
 */
public class PlaneStack
{
	//****************** instance variables

	private Image<?> stack;
	private RealType<?> imgLibType;
	private int planeWidth;
	private int planeHeight;
	private ContainerFactory factory;

	//****************** private interface

	@SuppressWarnings("unchecked")
	/** creates a single plane Imglib image using passed in data. */
	private Image<?> createPlane(RealType<?> imgLibType, ContainerFactory cFact, Object data)
	{
		int[] dimensions = new int[]{planeWidth, planeHeight, 1};

		Image<?> image = ImageUtils.createImage(imgLibType, cFact, dimensions);

		PlanarAccess<ArrayDataAccess<?>> planar = ImageUtils.getPlanarAccess(image);

		if (planar != null)
		{
			int[] planePosition = Index.create(1);

			ImageUtils.setPlane(image, planePosition, data);
		}
		else
		{
			Type ijType = TypeManager.getIJType(imgLibType);
			
			SetPlaneOperation<?> planeOp;
			
			if (imgLibType instanceof BitType)
				planeOp = new SetPlaneOperation<BitType>((Image<BitType>)image, Index.create(3), data, ijType);
			else if (imgLibType instanceof ByteType)
				planeOp = new SetPlaneOperation<ByteType>((Image<ByteType>)image, Index.create(3), data, ijType);
			else if (imgLibType instanceof UnsignedByteType)
				planeOp = new SetPlaneOperation<UnsignedByteType>((Image<UnsignedByteType>)image, Index.create(3), data, ijType);
			else if (imgLibType instanceof Unsigned12BitType)
				planeOp = new SetPlaneOperation<Unsigned12BitType>((Image<Unsigned12BitType>)image, Index.create(3), data, ijType);
			else if (imgLibType instanceof ShortType)
				planeOp = new SetPlaneOperation<ShortType>((Image<ShortType>)image, Index.create(3), data, ijType);
			else if (imgLibType instanceof UnsignedShortType)
				planeOp = new SetPlaneOperation<UnsignedShortType>((Image<UnsignedShortType>)image, Index.create(3), data, ijType);
			else if (imgLibType instanceof IntType)
				planeOp = new SetPlaneOperation<IntType>((Image<IntType>)image, Index.create(3), data, ijType);
			else if (imgLibType instanceof UnsignedIntType)
				planeOp = new SetPlaneOperation<UnsignedIntType>((Image<UnsignedIntType>)image, Index.create(3), data, ijType);
			else if (imgLibType instanceof LongType)
				planeOp = new SetPlaneOperation<LongType>((Image<LongType>)image, Index.create(3), data, ijType);
			else if (imgLibType instanceof FloatType)
				planeOp = new SetPlaneOperation<FloatType>((Image<FloatType>)image, Index.create(3), data, ijType);
			else if (imgLibType instanceof DoubleType)
				planeOp = new SetPlaneOperation<DoubleType>((Image<DoubleType>)image, Index.create(3), data, ijType);
			else
				throw new IllegalStateException();

			planeOp.execute();
		}

		return image;
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	/** copies a given number of consecutive planes from a source image to a destination image */
	private void copyPlanesFromTo(int numPlanes, Image<?> srcImage, int srcPlane, Image<?> dstImage, int dstPlane)
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

			if (this.imgLibType instanceof BitType)
				ImageUtils.copyFromImageToImage((Image<BitType>)srcImage, srcOrigin, span,
												(Image<BitType>)dstImage, dstOrigin, span);
			else if (this.imgLibType instanceof ByteType)
				ImageUtils.copyFromImageToImage((Image<ByteType>)srcImage, srcOrigin, span,
												(Image<ByteType>)dstImage, dstOrigin, span);
			else if (this.imgLibType instanceof UnsignedByteType)
				ImageUtils.copyFromImageToImage((Image<UnsignedByteType>)srcImage, srcOrigin, span,
												(Image<UnsignedByteType>)dstImage, dstOrigin, span);
			else if (this.imgLibType instanceof Unsigned12BitType)
				ImageUtils.copyFromImageToImage((Image<Unsigned12BitType>)srcImage, srcOrigin, span,
												(Image<Unsigned12BitType>)dstImage, dstOrigin, span);
			else if (this.imgLibType instanceof ShortType)
				ImageUtils.copyFromImageToImage((Image<ShortType>)srcImage, srcOrigin, span,
												(Image<ShortType>)dstImage, dstOrigin, span);
			else if (this.imgLibType instanceof UnsignedShortType)
				ImageUtils.copyFromImageToImage((Image<UnsignedShortType>)srcImage, srcOrigin, span,
												(Image<UnsignedShortType>)dstImage, dstOrigin, span);
			else if (this.imgLibType instanceof IntType)
				ImageUtils.copyFromImageToImage((Image<IntType>)srcImage, srcOrigin, span,
												(Image<IntType>)dstImage, dstOrigin, span);
			else if (this.imgLibType instanceof UnsignedIntType)
				ImageUtils.copyFromImageToImage((Image<UnsignedIntType>)srcImage, srcOrigin, span,
												(Image<UnsignedIntType>)dstImage, dstOrigin, span);
			else if (this.imgLibType instanceof LongType)
				ImageUtils.copyFromImageToImage((Image<LongType>)srcImage, srcOrigin, span,
												(Image<LongType>)dstImage, dstOrigin, span);
			else if (this.imgLibType instanceof FloatType)
				ImageUtils.copyFromImageToImage((Image<FloatType>)srcImage, srcOrigin, span,
												(Image<FloatType>)dstImage, dstOrigin, span);
			else if (this.imgLibType instanceof DoubleType)
				ImageUtils.copyFromImageToImage((Image<DoubleType>)srcImage, srcOrigin, span,
												(Image<DoubleType>)dstImage, dstOrigin, span);
			else
				throw new IllegalStateException();
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
	 * @param dType - the Type of the input data
	 */
	private void insertPlane(int atPosition, Object data, Type dType)
	{
		int expectedSize = (int)dType.calcNumStorageUnitsFromPixelCount((long)this.planeHeight * this.planeWidth);
		
		int dataLen = Array.getLength(data);
		
		if (dataLen != expectedSize)
			throw new IllegalArgumentException("insertPlane(): input data does not match dimensions of stack - given "+
													dataLen+" samples but expected "+expectedSize+" samples");

		long numPlanesNow = getNumPlanes();

		if (atPosition < 0)
			throw new IllegalArgumentException("insertPlane(): negative insertion point requested");

		if (atPosition > numPlanesNow+1)
			throw new IllegalArgumentException("insertPlane(): insertion point too large");

		RealType<?> desiredType = TypeManager.getRealType(dType);
		
		Image<?> newPlane = createPlane(desiredType, this.factory, data);

		if (this.stack == null)
		{
			this.stack = newPlane;
			this.imgLibType = desiredType;
		}
		else  // we already have some entries in stack
		{
			// need to type check against existing planes
			if ( ! (TypeManager.sameKind(desiredType, this.imgLibType)) )
				throw new IllegalArgumentException("insertPlane(): type clash - " + this.imgLibType + " & " + desiredType);

			int[] newDims = new int[]{this.planeWidth, this.planeHeight, this.getEndPosition()+1};

			Image<?> newStack = ImageUtils.createImage(desiredType, this.factory, newDims);

			copyPlanesFromTo(atPosition, this.stack, 0, newStack, 0);
			copyPlanesFromTo(1, newPlane, 0, newStack, atPosition);
			copyPlanesFromTo(getEndPosition()-atPosition, this.stack, atPosition, newStack, atPosition+1);

			this.stack = newStack;
		}
	}


	//****************** public interface

	/** constructor - create a PlaneStack directly from an Imglib image */
	public PlaneStack(Image<?> stack) {
		this.stack = stack;
		this.imgLibType = ImageUtils.getType(stack);
		this.planeWidth = ImageUtils.getWidth(stack);
		this.planeHeight = ImageUtils.getHeight(stack);
		this.factory = stack.getContainerFactory();
	}

	/** constructor - create an empty PlaneStack given (x,y) dimensions and a factory for creating an Imglib image.
	 * Note that the backing type is not set until the first slice is added to the PlaneStack. */
	public PlaneStack(int width, int height, ContainerFactory factory)
	{
		if ((height <= 0) || (width <= 0))
			throw new IllegalArgumentException("PlaneStack constructor: width("+width+") and height("+
					height+") must both be greater than zero.");
		
		this.stack = null;
		this.imgLibType = null;
		this.planeWidth = width;
		this.planeHeight = height;
		this.factory = factory;
	}

	/** returns the backing Imglib image */
	public Image<?> getStorage()
	{
		return this.stack;
	}

	/** returns the number of planes in the PlaneStack */
	public long getNumPlanes()
	{
		if (this.stack == null)
			return 0;

		return Dimensions.getTotalPlanes(getStorage().getDimensions());
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
	 * @param type - the type of the input data
	 * @param data - an array of values representing the plane
	 */
	public void insertPlane(int atPosition, Type type, Object data)
	{
		Types.verifyCompatibility(type, data);
		
		insertPlane(atPosition, data, type);
	}

	/**
	 * adds a plane to the end of the PlaneStack
	 * @param unsigned - a boolean specifying whether input data is signed or unsigned
	 * @param data - an array of values representing the plane
	 */
	public void addPlane(Type type, Object data)
	{
		insertPlane(getEndPosition(), type, data);
	}

	/**
	 * deletes a plane from a PlaneStack
	 * @param planeNumber - the index of the plane to delete
	 */
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
		Image<?> newImage = ImageUtils.createImage(this.imgLibType, this.factory, newDims);

		copyPlanesFromTo(planeNumber, this.stack, 0, newImage, 0);
		copyPlanesFromTo(getEndPosition()-planeNumber-1, this.stack, planeNumber+1, newImage, planeNumber);

		this.stack = newImage;
	}

	@SuppressWarnings("unchecked")
	/** gets a plane from a PlaneStack. returns a reference if possible */
	public Object getPlane(int planeNumber)
	{
		int planes = this.stack.getDimension(2);
		
		if ((planeNumber < 0) || (planeNumber >= planes))
			throw new IllegalArgumentException("PlaneStack: index ("+planeNumber+") out of bounds (0-"+planes+")");
		
		PlanarAccess<ArrayDataAccess<?>> planar = ImageUtils.getPlanarAccess(this.stack);

		if (planar != null)
		{
			return planar.getPlane(planeNumber);
		}
		else  // can't get data directly
		{
			int[] planePosition = new int[]{planeNumber};
			
			Type asType = TypeManager.getIJType(ImageUtils.getType(this.stack));

			if (this.imgLibType instanceof BitType)
				return GetPlaneOperation.getPlaneAs((Image<BitType>)this.stack, planePosition, asType);
			else if (this.imgLibType instanceof ByteType)
				return GetPlaneOperation.getPlaneAs((Image<ByteType>)this.stack, planePosition, asType);
			else if (this.imgLibType instanceof UnsignedByteType)
				return GetPlaneOperation.getPlaneAs((Image<UnsignedByteType>)this.stack, planePosition, asType);
			else if (this.imgLibType instanceof Unsigned12BitType)
				return GetPlaneOperation.getPlaneAs((Image<Unsigned12BitType>)this.stack, planePosition, asType);
			else if (this.imgLibType instanceof ShortType)
				return GetPlaneOperation.getPlaneAs((Image<ShortType>)this.stack, planePosition, asType);
			else if (this.imgLibType instanceof UnsignedShortType)
				return GetPlaneOperation.getPlaneAs((Image<UnsignedShortType>)this.stack, planePosition, asType);
			else if (this.imgLibType instanceof IntType)
				return GetPlaneOperation.getPlaneAs((Image<IntType>)this.stack, planePosition, asType);
			else if (this.imgLibType instanceof UnsignedIntType)
				return GetPlaneOperation.getPlaneAs((Image<UnsignedIntType>)this.stack, planePosition, asType);
			else if (this.imgLibType instanceof LongType)
				return GetPlaneOperation.getPlaneAs((Image<LongType>)this.stack, planePosition, asType);
			else if (this.imgLibType instanceof FloatType)
				return GetPlaneOperation.getPlaneAs((Image<FloatType>)this.stack, planePosition, asType);
			else if (this.imgLibType instanceof DoubleType)
				return GetPlaneOperation.getPlaneAs((Image<DoubleType>)this.stack, planePosition, asType);
			else
				throw new IllegalStateException();
		}
	}
}
