package imagej.process.operation;

import imagej.process.Span;
import imagej.process.TypeManager;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class SetPlaneOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	public static enum DataType {BYTE,UBYTE,SHORT,USHORT,INT,UINT,LONG,ULONG,FLOAT,DOUBLE};

	// set in constructor
	private DataReader reader;
	
	// set before iteration
	private int pixNum;
	private boolean isIntegral;
	
	public SetPlaneOperation(Image<T> theImage, int[] origin, Object pixels, DataType inputType)
	{
		super(theImage, origin, Span.singlePlane(theImage.getDimension(0), theImage.getDimension(1), theImage.getNumDimensions()));
		
		switch (inputType)
		{
			case BYTE:
				this.reader = new ByteReader(pixels);
				break;
			case UBYTE:
				this.reader = new UnsignedByteReader(pixels);
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
			case LONG:
				this.reader = new LongReader(pixels);
				break;
			case FLOAT:
				this.reader = new FloatReader(pixels);
				break;
			case DOUBLE:
				this.reader = new DoubleReader(pixels);
				break;
			default:  // note ULONG falls through to here by design
				throw new IllegalArgumentException("SetPlaneOperation(): unsupported data type - "+inputType);
		}
		
	}
	
	@Override
	public void beforeIteration(RealType<T> type)
	{
		this.pixNum = 0;
		this.isIntegral = TypeManager.isIntegralType(type);
	}

	@Override
	public void insideIteration(int[] position, RealType<T> sample)
	{

		double pixelValue = reader.getValue(this.pixNum++);
		
		if (this.isIntegral)
			pixelValue = TypeManager.boundValueToType(sample, pixelValue);
		
		sample.setReal(pixelValue);
	}

	@Override
	public void afterIteration()
	{
	}
	
	private interface DataReader
	{
		double getValue(int i);
	}
	
	private class ByteReader implements DataReader
	{
		byte[] pixels;
		
		ByteReader(Object pixels) { this.pixels = (byte[]) pixels; }
		
		public double getValue(int i) { return pixels[i]; }
	}
	
	private class UnsignedByteReader implements DataReader
	{
		byte[] pixels;
		
		UnsignedByteReader(Object pixels) { this.pixels = (byte[]) pixels; }
		
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
		short[] pixels;
		
		ShortReader(Object pixels) { this.pixels = (short[]) pixels; }
		
		public double getValue(int i) { return pixels[i]; }
	}
	
	private class UnsignedShortReader implements DataReader
	{
		short[] pixels;
		
		UnsignedShortReader(Object pixels) { this.pixels = (short[]) pixels; }
		
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
		int[] pixels;
		
		IntReader(Object pixels) { this.pixels = (int[]) pixels; }
		
		public double getValue(int i) { return pixels[i]; }
	}
	
	private class UnsignedIntReader implements DataReader
	{
		int[] pixels;
		
		UnsignedIntReader(Object pixels) { this.pixels = (int[]) pixels; }
		
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
		long[] pixels;
		
		LongReader(Object pixels) { this.pixels = (long[]) pixels; }
		
		public double getValue(int i) { return pixels[i]; }
	}
	
	private class FloatReader implements DataReader
	{
		float[] pixels;
		
		FloatReader(Object pixels) { this.pixels = (float[]) pixels; }
		
		public double getValue(int i) { return pixels[i]; }
	}
	
	private class DoubleReader implements DataReader
	{
		double[] pixels;
		
		DoubleReader(Object pixels) { this.pixels = (double[]) pixels; }
		
		public double getValue(int i) { return pixels[i]; }
	}
}

