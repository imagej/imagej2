package imagej.imglib;

import imagej.DataEncoding;
import imagej.StorageType;
import imagej.UserType;

public class EncodingManager {

	// ***** members  ************************************************/
	
	private static DataEncoding[] encodingArray;

	// ***** public interface  ************************************************/

	/** lookup the DataEncoding assocated with the given user type */
	public static DataEncoding getEncoding(UserType userType)
	{
		return encodingArray[userType.ordinal()];
	}
	
	/** returns the number of storage units required to fully store a given number of pixels with a specified encoding */
	public static int calcStorageUnitsRequired(DataEncoding encoding, int numPixels)
	{
		return (int) Math.ceil(encoding.getNumTypesPerValue() * numPixels);
	}
	
	/** returns the macimum number of pixels a number of storage units can contain using a specified encoding */
	public static int calcMaxPixelsStorable(DataEncoding encoding, int numStorageUnits)
	{
		return (int) Math.floor(numStorageUnits / encoding.getNumTypesPerValue());
	}
	
	//***** static initialization **********************************************/
	
	/** initialize the type lists */
	static
	{
		encodingArray = new DataEncoding[UserType.values().length];

		encodingArray[UserType.BIT.ordinal()] = new BitEncoding();
		encodingArray[UserType.BYTE.ordinal()] = new ByteEncoding();
		encodingArray[UserType.UBYTE.ordinal()] = new UnsignedByteEncoding();
		encodingArray[UserType.UINT12.ordinal()] = new Unsigned12BitEncoding();
		encodingArray[UserType.SHORT.ordinal()] = new ShortEncoding();
		encodingArray[UserType.USHORT.ordinal()] = new UnsignedShortEncoding();
		encodingArray[UserType.INT.ordinal()] = new IntEncoding();
		encodingArray[UserType.UINT.ordinal()] = new UnsignedIntEncoding();
		encodingArray[UserType.FLOAT.ordinal()] = new FloatEncoding();
		encodingArray[UserType.LONG.ordinal()] = new LongEncoding();
		encodingArray[UserType.DOUBLE.ordinal()] = new DoubleEncoding();
	}

	// ***** private support  ************************************************/

	private EncodingManager() {}
	
	private static class BitEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.UINT32; }

		@Override
		public double getNumTypesPerValue() { return 1.0 / 32.0; }
	}
	
	private static class ByteEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.INT8; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class UnsignedByteEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.UINT8; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class Unsigned12BitEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.UINT32; }

		@Override
		public double getNumTypesPerValue() { return 12.0 / 32.0; }
	}

	private static class ShortEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.INT16; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class UnsignedShortEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.UINT16; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class IntEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.INT32; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class UnsignedIntEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.UINT32; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class LongEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.INT64; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class FloatEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.FLOAT32; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class DoubleEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.FLOAT64; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
}
