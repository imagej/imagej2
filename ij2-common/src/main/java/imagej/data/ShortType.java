package imagej.data;

import imagej.StorageType;

public class ShortType implements Type {

	@Override
	public String getName() {
		return "16-bit signed";
	}

	@Override
	public boolean isFloat() {
		return false;
	}

	@Override
	public boolean isUnsigned() {
		return false;
	}

	@Override
	public int getNumBitsData() {
		return 16;
	}

	@Override
	public double getMinReal() {
		return Short.MIN_VALUE;
	}

	@Override
	public double getMaxReal() {
		return Short.MAX_VALUE;
	}

	@Override
	public long getMinIntegral() {
		return Short.MIN_VALUE;
	}

	@Override
	public long getMaxIntegral() {
		return Short.MAX_VALUE;
	}

	@Override
	public DataAccessor allocateAccessor(Object array) {
		if ( ! isStorageCompatible(array) )
			throw new IllegalArgumentException("expected a short[] but given storage of type "+array.getClass());

		return new ShortAccessor(array);
	}

	@Override
	public StorageType getStorageType() {
		return StorageType.INT16;
	}

	@Override
	public double getNumberOfStorageTypesPerValue() {
		return 1;
	}

	@Override
	public boolean isStorageCompatible(Object data) {
		return data instanceof short[];
	}

	@Override
	public long calcNumStorageBytesFromPixelCount(long numPixels) {
		return 2 * calcNumStorageUnitsFromPixelCount(numPixels);
	}

	@Override
	public long calcNumStorageUnitsFromPixelCount(long numPixels) {
		return numPixels;
	}

	@Override
	public Object allocateStorageArray(int numPixels) {
		return new short[numPixels];
	}

}
