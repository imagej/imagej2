package imagej.imglib.process.operation;

import imagej.data.DataAccessor;
import imagej.data.Type;
import imagej.imglib.TypeManager;
import imagej.process.Span;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class SetPlaneOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	// **************** instance variables ********************************************
	
	// set in constructor
	private DataAccessor reader;
	
	// set before iteration
	private int currPix;
	private boolean isIntegral;
	
	// **************** public interface ********************************************

	public SetPlaneOperation(Image<T> theImage, int[] origin, Object pixels, Type inputType)
	{
		super(theImage, origin, Span.singlePlane(theImage.getDimension(0), theImage.getDimension(1), theImage.getNumDimensions()));
		
		this.reader = inputType.allocateArrayAccessor(pixels);
	}
	
	@Override
	protected void beforeIteration(RealType<T> type)
	{
		this.currPix = 0;
		this.isIntegral = TypeManager.isIntegralType(type);
	}

	@Override
	protected void insideIteration(int[] position, RealType<T> sample)
	{

		double pixelValue = reader.getReal(this.currPix++);
		
		if (this.isIntegral)
			pixelValue = TypeManager.boundValueToType(sample, pixelValue);
		
		sample.setReal(pixelValue);
	}

	@Override
	protected void afterIteration()
	{
	}
}

