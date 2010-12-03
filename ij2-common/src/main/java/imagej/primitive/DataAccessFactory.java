package imagej.primitive;

import imagej.DataType;

public class DataAccessFactory
{
	private DataAccessFactory() {}
	
	public static DataReader getReader(DataType type, Object arrayOfData)
	{
		switch (type)
		{
			case BIT: return new BitReader(arrayOfData);
			case BYTE: return new ByteReader(arrayOfData);
			case UBYTE: return new UnsignedByteReader(arrayOfData);
			case UINT12: return new UnsignedTwelveBitReader(arrayOfData);
			case SHORT: return new ShortReader(arrayOfData);
			case USHORT: return new UnsignedShortReader(arrayOfData);
			case INT: return new IntReader(arrayOfData);
			case UINT: return new UnsignedIntReader(arrayOfData);
			case FLOAT: return new FloatReader(arrayOfData);
			case LONG: return new LongReader(arrayOfData);
			case DOUBLE: return new DoubleReader(arrayOfData);
			default: throw new IllegalStateException("unknown user type "+type);
		}
	}
	
	public static DataWriter getWriter(DataType type, Object arrayOfData)
	{
		switch (type)
		{
			case BIT: return new BitWriter(arrayOfData);
			case BYTE: return new ByteWriter(arrayOfData);
			case UBYTE: return new UnsignedByteWriter(arrayOfData);
			case UINT12: return new UnsignedTwelveBitWriter(arrayOfData);
			case SHORT: return new ShortWriter(arrayOfData);
			case USHORT: return new UnsignedShortWriter(arrayOfData);
			case INT: return new IntWriter(arrayOfData);
			case UINT: return new UnsignedIntWriter(arrayOfData);
			case FLOAT: return new FloatWriter(arrayOfData);
			case LONG: return new LongWriter(arrayOfData);
			case DOUBLE: return new DoubleWriter(arrayOfData);
			default: throw new IllegalStateException("unknown user type "+type);
		}
	}
	
	
}
