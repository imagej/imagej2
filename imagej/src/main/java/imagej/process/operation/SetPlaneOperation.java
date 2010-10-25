package imagej.process.operation;

import imagej.SampleInfo.ValueType;
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
	private int pixNum;
	private boolean isIntegral;
	
	// **************** public interface ********************************************

	public SetPlaneOperation(Image<T> theImage, int[] origin, Object pixels, ValueType inputType)
	{
		super(theImage, origin, Span.singlePlane(theImage.getDimension(0), theImage.getDimension(1), theImage.getNumDimensions()));
		
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
			default:  // note ULONG falls through to here by design
				throw new IllegalArgumentException("SetPlaneOperation(): unsupported data type - "+inputType);
		}
		
	}
	
	@Override
	protected void beforeIteration(RealType<T> type)
	{
		this.pixNum = 0;
		this.isIntegral = TypeManager.isIntegralType(type);
	}

	@Override
	protected void insideIteration(int[] position, RealType<T> sample)
	{

		double pixelValue = reader.getValue(this.pixNum++);
		
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
}

