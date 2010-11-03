package imagej.process.operation;

import imagej.SampleInfo.ValueType;
import imagej.SampleManager;
import imagej.process.Span;
import imagej.process.TypeManager;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class SetPlaneOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	// **************** instance variables ********************************************
	
	private interface DataReader
	{
		double getValue(int index);
	}
	
	// set in constructor
	private DataReader reader;
	
	// set before iteration
	private int currPix;
	private boolean isIntegral;
	
	// **************** public interface ********************************************

	public SetPlaneOperation(Image<T> theImage, int[] origin, Object pixels, ValueType inputType)
	{
		super(theImage, origin, Span.singlePlane(theImage.getDimension(0), theImage.getDimension(1), theImage.getNumDimensions()));
		
		SampleManager.verifyTypeCompatibility(pixels, inputType);
		
		switch (inputType)
		{
			case BYTE:
				this.reader = new ByteReader((byte[])pixels);
				break;
			case UBYTE:
				this.reader = new UnsignedByteReader((byte[])pixels);
				break;
			case SHORT:
				this.reader = new ShortReader((short[])pixels);
				break;
			case USHORT:
				this.reader = new UnsignedShortReader((short[])pixels);
				break;
			case INT:
				this.reader = new IntReader((int[])pixels);
				break;
			case UINT:
				this.reader = new UnsignedIntReader((int[])pixels);
				break;
			case LONG:
				this.reader = new LongReader((long[])pixels);
				break;
			case FLOAT:
				this.reader = new FloatReader((float[])pixels);
				break;
			case DOUBLE:
				this.reader = new DoubleReader((double[])pixels);
				break;
			case UINT12:
				this.reader = new UnsignedTwelveBitReader((int[])pixels);
				break;
			default:  // note ULONG falls through to here by design
				throw new IllegalArgumentException("SetPlaneOperation(): unsupported data type - "+inputType);
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
	
	// **************** private code ********************************************
	
	private class ByteReader implements DataReader
	{
		private byte[] pixels;
		
		public ByteReader(byte[] pixels)
		{
			this.pixels = pixels;
		}
		
		public double getValue(int i)
		{
			return pixels[i];
		}
	}
	
	private class UnsignedByteReader implements DataReader
	{
		private byte[] pixels;
		
		public UnsignedByteReader(byte[] pixels)
		{
			this.pixels = pixels;
		}
		
		public double getValue(int i)
		{
			double pixel = pixels[i];
			if (pixel < 0)
				pixel = 256.0 + pixel;
			return pixel;
		}
	}
	
	private class ShortReader implements DataReader
	{
		private short[] pixels;
		
		public ShortReader(short[] pixels)
		{
			this.pixels = pixels;
		}
		
		public double getValue(int i)
		{
			return pixels[i];
		}
	}
	
	private class UnsignedShortReader implements DataReader
	{
		private short[] pixels;
		
		public UnsignedShortReader(short[] pixels)
		{
			this.pixels = pixels;
		}
		
		public double getValue(int i)
		{
			double pixel = pixels[i];
			if (pixel < 0)
				pixel = 65536.0 + pixel;
			return pixel;
		}
	}
	
	private class IntReader implements DataReader
	{
		private int[] pixels;
		
		public IntReader(int[] pixels)
		{
			this.pixels = pixels;
		}
		
		public double getValue(int i)
		{
			return pixels[i];
		}
	}
	
	private class UnsignedIntReader implements DataReader
	{
		private int[] pixels;
		
		public UnsignedIntReader(int[] pixels)
		{
			this.pixels = pixels;
		}
		
		public double getValue(int i)
		{
			double pixel = pixels[i];
			if (pixel < 0)
				pixel = 4294967296.0 + pixel;
			return pixel;
		}
	}
	
	private class LongReader implements DataReader
	{
		private long[] pixels;
		
		public LongReader(long[] pixels)
		{
			this.pixels = pixels;
		}
		
		public double getValue(int i)
		{
			return pixels[i];
		}
	}
	
	private class FloatReader implements DataReader
	{
		private float[] pixels;
		
		public FloatReader(float[] pixels)
		{
			this.pixels = pixels;
		}
		
		public double getValue(int i)
		{
			return pixels[i];
		}
	}
	
	private class DoubleReader implements DataReader
	{
		private double[] pixels;
		
		public DoubleReader(double[] pixels)
		{
			this.pixels = pixels;
		}
		
		public double getValue(int i)
		{
			return pixels[i];
		}
	}

	private class UnsignedTwelveBitReader implements DataReader
	{
		private int[] ints;
		
		public UnsignedTwelveBitReader(int[] ints)
		{
			this.ints = ints;
		}
		
		public double getValue(int index)
		{
			// divide pixels into buckets of nibbles. each bucket is 96 bits (three 32 bit ints = eight 12 bit ints)
			int bucketNumber = index / 8;
			
			int intIndexOfBucketStart = bucketNumber * 3;
			
			int indexOfFirstNibble = index % 8;
			
			int nibble1, nibble2, nibble3;
			
			switch (indexOfFirstNibble)
			
			{
				case 0:
					nibble1 = getNibble(intIndexOfBucketStart,   0);
					nibble2 = getNibble(intIndexOfBucketStart,   1);
					nibble3 = getNibble(intIndexOfBucketStart,   2);
					break;
				case 1:
					nibble1 = getNibble(intIndexOfBucketStart,   3);
					nibble2 = getNibble(intIndexOfBucketStart,   4);
					nibble3 = getNibble(intIndexOfBucketStart,   5);
					break;
				case 2:
					nibble1 = getNibble(intIndexOfBucketStart,   6);
					nibble2 = getNibble(intIndexOfBucketStart,   7);
					nibble3 = getNibble(intIndexOfBucketStart+1, 0);
					break;
				case 3:
					nibble1 = getNibble(intIndexOfBucketStart+1, 1);
					nibble2 = getNibble(intIndexOfBucketStart+1, 2);
					nibble3 = getNibble(intIndexOfBucketStart+1, 3);
					break;
				case 4:
					nibble1 = getNibble(intIndexOfBucketStart+1, 4);
					nibble2 = getNibble(intIndexOfBucketStart+1, 5);
					nibble3 = getNibble(intIndexOfBucketStart+1, 6);
					break;
				case 5:
					nibble1 = getNibble(intIndexOfBucketStart+1, 7);
					nibble2 = getNibble(intIndexOfBucketStart+2, 0);
					nibble3 = getNibble(intIndexOfBucketStart+2, 1);
					break;
				case 6:
					nibble1 = getNibble(intIndexOfBucketStart+2, 2);
					nibble2 = getNibble(intIndexOfBucketStart+2, 3);
					nibble3 = getNibble(intIndexOfBucketStart+2, 4);
					break;
				case 7:
					nibble1 = getNibble(intIndexOfBucketStart+2, 5);
					nibble2 = getNibble(intIndexOfBucketStart+2, 6);
					nibble3 = getNibble(intIndexOfBucketStart+2, 7);
					break;
				default:
					throw new IllegalStateException();
			}
			
			return (nibble3 << 8) + (nibble2 << 4) + (nibble1 << 0);
		}
		
		private int getNibble(int intNumber, int nibbleNumber)
		{
			int currValue = this.ints[intNumber];
			
			int alignedMask = 15 << (4 * nibbleNumber);

			int shiftedValue = currValue & alignedMask;
			
			return shiftedValue >> (4 * nibbleNumber);
		}
	}
}

