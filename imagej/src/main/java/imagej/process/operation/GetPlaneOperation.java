package imagej.process.operation;

import imagej.SampleInfo;
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
	
	private int[] span;
	private SampleInfo.ValueType asType;
	private Object outputPlane;
	private DataWriter planeWriter;

	// ************ public interface - private declarations later **************************
	
	public GetPlaneOperation(Image<T> image, int[] origin, int[] span, SampleInfo.ValueType asType)
	{
		super(image, origin, span);
		this.span = span;
		this.asType = asType;
	}

	@Override
	public void beforeIteration(RealType<T> type)
	{
		int planeSize = span[0] * span[1];
		
		switch (this.asType)
		{
			case BYTE:
				this.outputPlane = new byte[planeSize];
				this.planeWriter = new ByteWriter((byte[])this.outputPlane);
				break;
				
			case UBYTE:
				this.outputPlane = new byte[planeSize];
				this.planeWriter = new UByteWriter((byte[])this.outputPlane);
				break;
		
			case SHORT:
				this.outputPlane = new short[planeSize];
				this.planeWriter = new ShortWriter((short[])this.outputPlane);
				break;
		
			case USHORT:
				this.outputPlane = new short[planeSize];
				this.planeWriter = new UShortWriter((short[])this.outputPlane);
				break;
		
			case INT:
				this.outputPlane = new int[planeSize];
				this.planeWriter = new IntWriter((int[])this.outputPlane);
				break;
		
			case UINT:
				this.outputPlane = new int[planeSize];
				this.planeWriter = new UIntWriter((int[])this.outputPlane);
				break;
		
			case LONG:
				this.outputPlane = new long[planeSize];
				this.planeWriter = new LongWriter((long[])this.outputPlane);
				break;
		
			case FLOAT:
				this.outputPlane = new float[planeSize];
				this.planeWriter = new FloatWriter((float[])this.outputPlane);
				break;
		
			case DOUBLE:
				this.outputPlane = new double[planeSize];
				this.planeWriter = new DoubleWriter((double[])this.outputPlane);
				break;
		
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public void insideIteration(int[] position, RealType<T> sample)
	{
		int index = position[1]*span[0] + position[0]; 
		planeWriter.setValue(index, sample.getRealDouble());
	}

	@Override
	public void afterIteration()
	{
	}
	
	public Object getOutputPlane()
	{
		return this.outputPlane;
	}

	public static <T extends RealType<T>> Object getPlaneAs(Image<T> img, int[] planePos, SampleInfo.ValueType asType)
	{
		int[] origin = Index.create(0,0,planePos);
		
		int[] span = Span.singlePlane(img.getDimension(0), img.getDimension(1), img.getNumDimensions());
		
		GetPlaneOperation<T> operation = new GetPlaneOperation<T>(img, origin, span, asType);
	
		operation.execute();
		
		return operation.getOutputPlane();
	}

	
	// *****************  private stuff ********************************

	private class ByteWriter implements DataWriter
	{
		byte[] bytes;
		
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

	private class UByteWriter implements DataWriter
	{
		byte[] bytes;
		
		public UByteWriter(byte[] bytes)
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

	private class ShortWriter implements DataWriter
	{
		short[] shorts;
		
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

	private class UShortWriter implements DataWriter
	{
		short[] shorts;
		
		public UShortWriter(short[] shorts)
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
		int[] ints;
		
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

	private class UIntWriter implements DataWriter
	{
		int[] ints;
		
		public UIntWriter(int[] ints)
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
		long[] longs;
		
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
		float[] floats;
		
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
		double[] doubles;
		
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
