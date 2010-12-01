package imagej.imglib.process.operation;

import imagej.EncodingManager;
import imagej.UserType;
import imagej.imglib.TypeManager;
import imagej.primitive.BitReader;
import imagej.primitive.ByteReader;
import imagej.primitive.DataReader;
import imagej.primitive.DoubleReader;
import imagej.primitive.FloatReader;
import imagej.primitive.IntReader;
import imagej.primitive.LongReader;
import imagej.primitive.ShortReader;
import imagej.primitive.UnsignedByteReader;
import imagej.primitive.UnsignedIntReader;
import imagej.primitive.UnsignedShortReader;
import imagej.primitive.UnsignedTwelveBitReader;
import imagej.process.Span;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class SetPlaneOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	// **************** instance variables ********************************************
	
	// set in constructor
	private DataReader reader;
	
	// set before iteration
	private int currPix;
	private boolean isIntegral;
	
	// **************** public interface ********************************************

	public SetPlaneOperation(Image<T> theImage, int[] origin, Object pixels, UserType inputType)
	{
		super(theImage, origin, Span.singlePlane(theImage.getDimension(0), theImage.getDimension(1), theImage.getNumDimensions()));
		
		EncodingManager.verifyTypeCompatibility(pixels, inputType);
		
		switch (inputType)
		{
			case BIT:
				this.reader = new BitReader(pixels);
				break;
			case BYTE:
				this.reader = new ByteReader(pixels);
				break;
			case UBYTE:
				this.reader = new UnsignedByteReader(pixels);
				break;
			case UINT12:
				this.reader = new UnsignedTwelveBitReader(pixels);
				break;
			case SHORT:
				this.reader = new ShortReader(pixels);
				break;
			case USHORT:
				this.reader = new UnsignedShortReader(pixels);
				break;
			case INT:
				this.reader = new IntReader(pixels);
				break;
			case UINT:
				this.reader = new UnsignedIntReader(pixels);
				break;
			case FLOAT:
				this.reader = new FloatReader(pixels);
				break;
			case LONG:
				this.reader = new LongReader(pixels);
				break;
			case DOUBLE:
				this.reader = new DoubleReader(pixels);
				break;
			default:
				throw new IllegalStateException("SetPlaneOperation(): unsupported data type - "+inputType);
		}
		
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

		double pixelValue = reader.getValue(this.currPix++);
		
		if (this.isIntegral)
			pixelValue = TypeManager.boundValueToType(sample, pixelValue);
		
		sample.setReal(pixelValue);
	}

	@Override
	protected void afterIteration()
	{
	}
}

