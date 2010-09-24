package imagej;

import imagej.process.ImageUtils;
import imagej.selection.*;
import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

// TODO - this got in the repository by accident. Could delete but could also expand it and use it.

public class NumericDataset
{
	private RealType<?> type;
	private int[] dimensions;
	private ContainerFactory factory;
	private Image<?> data;
	// private threadLocal LocalizablebyDimCursor<?> cursor;
	
	private static class ImgLibImageFactory
	{
		public static Image<?> create(RealType<?> desiredType, int[] dimensions, ContainerFactory cFact)
		{
			return ImageUtils.createImage(desiredType, cFact, dimensions);
		}
	}

	private void validateDimensions(int[] dimensions)
	{
		for (int i = 0; i < dimensions.length; i++)
		{
			if (dimensions[i] < 0)
				throw new IllegalArgumentException("invalid dimension () for dimension number "+i);
		}
	}

	private void validateDimensionChange(int axisNumber, int increment)
	{
		int currPlaneCount = this.dimensions[axisNumber];

		if ((increment > 0) && (currPlaneCount > (Integer.MAX_VALUE-increment)))
			throw new IllegalArgumentException("dimension change of (+"+increment+") would be > the maximum allowed planes in dimension #"+axisNumber);

		if ((increment < 0) && (currPlaneCount < -increment))
			throw new IllegalArgumentException("dimension change of ("+increment+") would be < the minimum allowed planes in dimension #"+axisNumber);
	}
	
	// we're going to allow datasets that have 0 dimensions or 0 dims along an axis
	public NumericDataset(RealType<?> desiredType, int[] dimensions, ContainerFactory cFact)
	{
		this.type = desiredType;
		this.dimensions = dimensions.clone();
		this.factory = cFact;

		validateDimensions(this.dimensions);
		
		if (ImageUtils.getTotalSamples(this.dimensions) > 0)
			this.data = ImgLibImageFactory.create(desiredType, this.dimensions,cFact);
	}

	public RealType<?> getType() { return this.type; }
	public int[] getDimensions() { return this.dimensions.clone(); }
	
	public void addPlane(int axisNumber, Image<?> newPlane)
	{
		if ((axisNumber < 0) || (axisNumber >= dimensions.length))
			throw new IllegalArgumentException("NumericDataset::addPlane(): axis number out of valid range");
		
		// insert at end
		int lastPosition = this.dimensions[axisNumber];
		insertPlane(axisNumber, lastPosition, newPlane);
	}
	
	public void insertPlane(int axisNumber, int planeNumber, Image<?> newPlane)
	{
		// make sure axisNumber is valid
		// make sure planeNumber is valid along desired axis
		// make sure my image and newPlane are the same type
		// make sure newPlane has compatible dimensions for that axis

		validateDimensionChange(axisNumber, +1);

		int[] newDimensions = this.dimensions.clone();
		
		newDimensions[axisNumber]++;
		
		if (this.data == null)
		{
			if (ImageUtils.getTotalSamples(newDimensions) > 0)
				this.data = ImgLibImageFactory.create(this.type, newDimensions, this.factory);
			
			// TODO - copy data from plane into this.data at relevant spot
		}
		else  // we already have data
		{
			Image<?> newData = ImgLibImageFactory.create(this.type, newDimensions, this.factory);
			
			// TODO - copy relevant planes around (until imglib updated)
			
			this.data = newData;
		}
		
		this.dimensions = newDimensions;
	}
	
	// TODO : if make a dim 0 then reduce dataset's dimensionality?????
	
	public void deletePlane(int axisNumber, int planeNumber)
	{
		// make sure axisNumber is valid
		// make sure planeNumber is valid along desired axis
		// make sure tot planes in this axis > 0
		
		validateDimensionChange(axisNumber, -1);
		
		int[] newDimensions = this.dimensions.clone();

		newDimensions[axisNumber]--;
		
		if (this.data == null)
		{
			// probably can't get here if above checking correct
			throw new IllegalArgumentException("can't delete a plane from an empty dataset");
		}
		else  // we already have data
		{
			Image<?> newData = ImgLibImageFactory.create(this.type, newDimensions, this.factory);
			
			// TODO - copy relevant planes around (until imglib updated)
			
			this.data = newData;
		}
		
		this.dimensions = newDimensions;
	}
	
	// TODO : these
	public void addAxis() { insertAxis(-1); }
	public void insertAxis(int axisNumber) {}
	public void deleteAxis(int axisNumber) {}
	
	public Image<?> createPlane(int axisNumber)
	{
		// figure out dimensions of a plane along that axis
		
		int[] planeDims = this.dimensions.clone();
		
		planeDims[axisNumber] = 1;
		
		if (ImageUtils.getTotalSamples(planeDims) > 0)
			return ImgLibImageFactory.create(this.type, planeDims, this.factory);
		else
		{
			throw new IllegalArgumentException("plane dimensions invalid");
		}
	}
	
	public void insertPlanes(int axisNumber, int planeNumber, Image<?> moreplanes)
	{
	}
	
	public void concatPlanes(int axisNumber, Image<?> moreplanes)
	{
	}
	
	public int getPlaneCount(int axisNumber)
	{
		return this.dimensions[axisNumber];
	}
	
	public Image<?> getPlane(int axisNumber, int planeNumber)  // TODO - should it return a NumericDataset
	{
		return null;
	}
	
	// TODO - just to prove power of this code
	public void interleavePlanes(int axisNumber, NumericDataset other)
	{
		// make sure my planeCount equal or one greater than other image
		// make sure the two images are compatible along axisNumber
		// make sure the two images are compatible in type
		
		int myPlanes = this.getPlaneCount(axisNumber);
		int otherPlanes = other.getPlaneCount(axisNumber);
		
		int[] newDimensions = null;  // TODO remember to zero out new dataset dimensions[axisNumber]
		
		NumericDataset newDataset = new NumericDataset(this.type, newDimensions, this.factory);
		
		for (int i = 0; i < otherPlanes; i++)
		{
			newDataset.addPlane(axisNumber, getPlane(axisNumber, i));
			newDataset.addPlane(axisNumber, other.getPlane(axisNumber, i));
		}
		
		if (myPlanes > otherPlanes)
			newDataset.addPlane(axisNumber, getPlane(axisNumber, myPlanes-1));
	}
	
	
	// ***********  might need this in the iamgej.process.function package for other reasons ******************************
	
	private interface BooleanFunction
	{
		boolean compute();
	}
	
	private class AndBooleanFunction implements BooleanFunction
	{
		private BooleanFunction func1, func2;
		
		public AndBooleanFunction(BooleanFunction func1, BooleanFunction func2)
		{
			this.func1 = func1;
			this.func2 = func2;
		}
		
		public boolean compute() { return func1.compute() && func2.compute(); }
	}
	
	private class OrBooleanFunction implements BooleanFunction
	{
		private BooleanFunction func1, func2;
		
		public OrBooleanFunction(BooleanFunction func1, BooleanFunction func2)
		{
			this.func1 = func1;
			this.func2 = func2;
		}
		
		public boolean compute() { return func1.compute() || func2.compute(); }
	}
	
	private class NotBooleanFunction implements BooleanFunction
	{
		private BooleanFunction func1;
		
		public NotBooleanFunction(BooleanFunction func1)
		{
			this.func1 = func1;
		}
		
		public boolean compute() { return !func1.compute(); }
	}

	
	// *********** better try ************************************
	
	// TODO - make an Operation that iterates over a Image<T> and applies a function if a SelectionFunction is satisfied
	//   Would be very easy and powerful!!
	

	// To test power define a selection function that composes other selection functions to allow me to
	// return true for any sample that is in row 1, 4, 13, or odd ones > 19 whose value <= 13.2
	private class WackySelectionFunction implements SelectionFunction
	{
		private SelectionFunction function;
		
		public WackySelectionFunction()
		{
			SelectionFunction rowEq1 = new PositionAxisEqualsSelectionFunction(1,1);
			SelectionFunction rowEq4 = new PositionAxisEqualsSelectionFunction(1,4);
			SelectionFunction rowEq13 = new PositionAxisEqualsSelectionFunction(1,13);
			SelectionFunction rowGreater19 = new PositionAxisGreaterSelectionFunction(1,19);
			SelectionFunction rowOdd = new ModulusSelectionFunction(1,2,1);
			SelectionFunction valueLessEquals13_2 = new ValueLessThanOrEqualsSelectionFunction(13.2);
			
			SelectionFunction rowFunc1 = new OrSelectionFunction(rowEq1,rowEq4);
			SelectionFunction rowFunc2 = rowEq13;
			SelectionFunction rowFunc3 = new AndSelectionFunction(rowGreater19, rowOdd);
			SelectionFunction rowFuncTmp = new OrSelectionFunction(rowFunc1,rowFunc2);
			SelectionFunction rowFunc = new OrSelectionFunction(rowFuncTmp,rowFunc3);
			
			function = new AndSelectionFunction(rowFunc,valueLessEquals13_2);
		}

		public boolean include(int[] position, double sample)
		{
			return function.include(position, sample);
		}

	}
}
