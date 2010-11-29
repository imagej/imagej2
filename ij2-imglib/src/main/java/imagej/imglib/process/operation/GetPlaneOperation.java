package imagej.imglib.process.operation;

import imagej.DataEncoding;
import imagej.EncodingManager;
import imagej.UserType;
import imagej.process.Index;
import imagej.process.Span;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;


public class GetPlaneOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	// *********** instance variables ******************************************************
	
	private interface DataWriter
	{
		void setValue(int index, double value);
	}
	
	private int originX, originY, spanX, spanY;
	private UserType asType;
	private Object outputPlane;
	private DataWriter planeWriter;

	// ************ public interface - private declarations later **************************
	
	public GetPlaneOperation(Image<T> image, int[] origin, int[] span, UserType asType)
	{
		super(image, origin, span);
		this.originX = origin[0];
		this.originY = origin[1];
		this.spanX = span[0];
		this.spanY = span[1];
		this.asType = asType;
	}

	@Override
	protected void beforeIteration(RealType<T> type)
	{
		int planeSize = this.spanX * this.spanY;
		
		DataEncoding encoding = EncodingManager.getEncoding(this.asType);
		
		int storageRequired = EncodingManager.calcStorageUnitsRequired(encoding, planeSize); 
		
		switch (this.asType)
		{
			case BIT:
				this.outputPlane = new int[storageRequired];
				this.planeWriter = new BitWriter((int[])this.outputPlane);
				break;

			case BYTE:
				this.outputPlane = new byte[storageRequired];
				this.planeWriter = new ByteWriter((byte[])this.outputPlane);
				break;
				
			case UBYTE:
				this.outputPlane = new byte[storageRequired];
				this.planeWriter = new UnsignedByteWriter((byte[])this.outputPlane);
				break;
		
			case UINT12:
				this.outputPlane = new int[storageRequired];
				this.planeWriter = new UnsignedTwelveBitWriter((int[])this.outputPlane);
				break;
		
			case SHORT:
				this.outputPlane = new short[storageRequired];
				this.planeWriter = new ShortWriter((short[])this.outputPlane);
				break;
		
			case USHORT:
				this.outputPlane = new short[storageRequired];
				this.planeWriter = new UnsignedShortWriter((short[])this.outputPlane);
				break;
		
			case INT:
				this.outputPlane = new int[storageRequired];
				this.planeWriter = new IntWriter((int[])this.outputPlane);
				break;
		
			case UINT:
				this.outputPlane = new int[storageRequired];
				this.planeWriter = new UnsignedIntWriter((int[])this.outputPlane);
				break;
		
			case FLOAT:
				this.outputPlane = new float[storageRequired];
				this.planeWriter = new FloatWriter((float[])this.outputPlane);
				break;
		
			case LONG:
				this.outputPlane = new long[storageRequired];
				this.planeWriter = new LongWriter((long[])this.outputPlane);
				break;
		
			case DOUBLE:
				this.outputPlane = new double[storageRequired];
				this.planeWriter = new DoubleWriter((double[])this.outputPlane);
				break;
		
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	protected void insideIteration(int[] position, RealType<T> sample)
	{
		int index = (position[1] - this.originY) * this.spanX + (position[0] - this.originX); 
		
		this.planeWriter.setValue(index, sample.getRealDouble());
	}

	@Override
	protected void afterIteration()
	{
	}
	
	public Object getOutputPlane()
	{
		return this.outputPlane;
	}

	public static <T extends RealType<T>> Object getPlaneAs(Image<T> img, int[] planePos, UserType asType)
	{
		int[] origin = Index.create(0,0,planePos);
		
		int[] span = Span.singlePlane(img.getDimension(0), img.getDimension(1), img.getNumDimensions());
		
		GetPlaneOperation<T> operation = new GetPlaneOperation<T>(img, origin, span, asType);
	
		operation.execute();
		
		return operation.getOutputPlane();
	}

	
	// *****************  private stuff ********************************

	private class BitWriter implements DataWriter
	{
		private int[] ints;
		
		public BitWriter(int[] ints)
		{
			this.ints = ints;
		}

		public void setValue(int index, double value)
		{
			if (value < 0) value = 0;
			if (value > 1) value = 1;
			placeValue(index, (int)value);
		}
		
		private void placeValue(int index, int value)
		{
			// divide pixels into buckets of nibbles. each bucket is 96 bits (three 32 bit ints = eight 12 bit ints)
			int intNumber = index / 32;
			
			int bitNumber = index % 32;

			setBit(intNumber, bitNumber, value);
		}
		
		private void setBit(int intNumber, int bitNumber, int bitValue)
		{
			int currValue = this.ints[intNumber];
			
			int alignedBit = (bitValue) << (bitNumber);
			
			int alignedMask = 1 << (bitNumber);
			
			this.ints[intNumber] = (currValue & ~alignedMask) | alignedBit;
		}
	}
	
	private class ByteWriter implements DataWriter
	{
		private byte[] bytes;
		
		public ByteWriter(byte[] bytes)
		{
			this.bytes = bytes;
		}
		
		public void setValue(int index, double value)
		{
			if (value < Byte.MIN_VALUE)
				value = Byte.MIN_VALUE;
			
			if (value > Byte.MAX_VALUE)
				value = Byte.MAX_VALUE;
			
			bytes[index] = (byte) value;
		}
		
	}

	private class UnsignedByteWriter implements DataWriter
	{
		private byte[] bytes;
		
		public UnsignedByteWriter(byte[] bytes)
		{
			this.bytes = bytes;
		}
		
		public void setValue(int index, double value)
		{
			if (value < 0)
				value = 0;
			
			if (value > 0xff)
				value = 0xff;
			
			bytes[index] = (byte) ((int)value & 0xff);
		}
		
	}

	private class UnsignedTwelveBitWriter implements DataWriter
	{
		private int[] ints;
		
		public UnsignedTwelveBitWriter(int[] ints)
		{
			this.ints = ints;
		}

		public void setValue(int index, double value)
		{
			if (value < 0) value = 0;
			if (value > 4095) value = 4095;
			placeValue(index, (int)value);
		}
		
		private void placeValue(int index, int value)
		{
			// divide pixels into buckets of nibbles. each bucket is 96 bits (three 32 bit ints = eight 12 bit ints)
			int bucketNumber = index / 8;
			
			int intIndexOfBucketStart = bucketNumber * 3;
			
			int indexOfFirstNibble = index % 8;
	
			switch (indexOfFirstNibble)
			{
				case 0:
					setNibble(intIndexOfBucketStart,   0, ((value >> 0) & 15));
					setNibble(intIndexOfBucketStart,   1, ((value >> 4) & 15));
					setNibble(intIndexOfBucketStart,   2, ((value >> 8) & 15));
					break;
				case 1:
					setNibble(intIndexOfBucketStart,   3, ((value >> 0) & 15));
					setNibble(intIndexOfBucketStart,   4, ((value >> 4) & 15));
					setNibble(intIndexOfBucketStart,   5, ((value >> 8) & 15));
					break;
				case 2:
					setNibble(intIndexOfBucketStart,   6, ((value >> 0) & 15));
					setNibble(intIndexOfBucketStart,   7, ((value >> 4) & 15));
					setNibble(intIndexOfBucketStart+1, 0, ((value >> 8) & 15));
					break;
				case 3:
					setNibble(intIndexOfBucketStart+1, 1, ((value >> 0) & 15));
					setNibble(intIndexOfBucketStart+1, 2, ((value >> 4) & 15));
					setNibble(intIndexOfBucketStart+1, 3, ((value >> 8) & 15));
					break;
				case 4:
					setNibble(intIndexOfBucketStart+1, 4, ((value >> 0) & 15));
					setNibble(intIndexOfBucketStart+1, 5, ((value >> 4) & 15));
					setNibble(intIndexOfBucketStart+1, 6, ((value >> 8) & 15));
					break;
				case 5:
					setNibble(intIndexOfBucketStart+1, 7, ((value >> 0) & 15));
					setNibble(intIndexOfBucketStart+2, 0, ((value >> 4) & 15));
					setNibble(intIndexOfBucketStart+2, 1, ((value >> 8) & 15));
					break;
				case 6:
					setNibble(intIndexOfBucketStart+2, 2, ((value >> 0) & 15));
					setNibble(intIndexOfBucketStart+2, 3, ((value >> 4) & 15));
					setNibble(intIndexOfBucketStart+2, 4, ((value >> 8) & 15));
					break;
				case 7:
					setNibble(intIndexOfBucketStart+2, 5, ((value >> 0) & 15));
					setNibble(intIndexOfBucketStart+2, 6, ((value >> 4) & 15));
					setNibble(intIndexOfBucketStart+2, 7, ((value >> 8) & 15));
					break;
				default:
					throw new IllegalStateException();
			}
		}
		
		private void setNibble(int intNumber, int nibbleNumber, int nibbleValue)
		{
			int currValue = this.ints[intNumber];
			
			int alignedNibble = (nibbleValue) << (4 * nibbleNumber);
			
			int alignedMask = (0xf) << (4 * nibbleNumber);
			
			this.ints[intNumber] = (currValue & ~alignedMask) | alignedNibble;
		}
	}

	private class ShortWriter implements DataWriter
	{
		private short[] shorts;
		
		public ShortWriter(short[] shorts)
		{
			this.shorts = shorts;
		}
		
		public void setValue(int index, double value)
		{
			if (value < Short.MIN_VALUE)
				value = Short.MIN_VALUE;
			
			if (value > Short.MAX_VALUE)
				value = Short.MAX_VALUE;
			
			shorts[index] = (short) value;
		}
		
	}

	private class UnsignedShortWriter implements DataWriter
	{
		private short[] shorts;
		
		public UnsignedShortWriter(short[] shorts)
		{
			this.shorts = shorts;
		}
		
		public void setValue(int index, double value)
		{
			if (value < 0)
				value = 0;
			
			if (value > 0xffff)
				value = 0xffff;
			
			shorts[index] = (short) ((int)value & 0xffff);
		}
		
	}

	private class IntWriter implements DataWriter
	{
		private int[] ints;
		
		public IntWriter(int[] ints)
		{
			this.ints = ints;
		}
		
		public void setValue(int index, double value)
		{
			if (value < Integer.MIN_VALUE)
				value = Integer.MIN_VALUE;
			
			if (value > Integer.MAX_VALUE)
				value = Integer.MAX_VALUE;
			
			ints[index] = (int) value;
		}
		
	}

	private class UnsignedIntWriter implements DataWriter
	{
		private int[] ints;
		
		public UnsignedIntWriter(int[] ints)
		{
			this.ints = ints;
		}
		
		public void setValue(int index, double value)
		{
			if (value < 0)
				value = 0;
			
			if (value > (double)(0xffffffffL))
				value = (double)(0xffffffffL);
			
			ints[index] = (int) ((long)value & 0xffffffffL);
		}
		
	}

	private class LongWriter implements DataWriter
	{
		private long[] longs;
		
		public LongWriter(long[] longs)
		{
			this.longs = longs;
		}
		
		public void setValue(int index, double value)
		{
			if (value < Long.MIN_VALUE)
				value = Long.MIN_VALUE;
			
			if (value > Long.MAX_VALUE)
				value = Long.MAX_VALUE;
			
			longs[index] = (long) value;
		}
		
	}

	private class FloatWriter implements DataWriter
	{
		private float[] floats;
		
		public FloatWriter(float[] floats)
		{
			this.floats = floats;
		}
		
		public void setValue(int index, double value)
		{
			floats[index] = (float) value;
		}
		
	}

	private class DoubleWriter implements DataWriter
	{
		private double[] doubles;
		
		public DoubleWriter(double[] doubles)
		{
			this.doubles = doubles;
		}
		
		public void setValue(int index, double value)
		{
			doubles[index] = value;
		}
		
	}
	
}
