package imagej.data;

import imagej.StorageType;

public class DoubleType implements Type {

	@Override
	public String getName() {
		return "64-bit float";
	}

	@Override
	public boolean isFloat() {
		return true;
	}

	@Override
	public boolean isUnsigned() {
		return false;
	}

	@Override
	public int getNumBitsData() {
		return 64;
	}

	@Override
	public double getMinReal() {
		return -Double.MAX_VALUE;
	}

	@Override
	public double getMaxReal() {
		return Double.MAX_VALUE;
	}

	@Override
	public long getMinIntegral() {
		return (long) -Double.MAX_VALUE;
	}

	@Override
	public long getMaxIntegral() {
		return (long) Double.MAX_VALUE;
	}

	@Override
	public DataAccessor allocateAccessor(Object array) {
		if ( ! isStorageCompatible(array) )
			throw new IllegalArgumentException("expected a double[] but given storage of type "+array.getClass());

		return new DoubleAccessor(array);
	}

	@Override
	public StorageType getStorageType() {
		return StorageType.FLOAT64;
	}

	@Override
	public double getNumberOfStorageTypesPerValue() {
		return 1;
	}

	@Override
	public boolean isStorageCompatible(Object data) {
		return data instanceof double[];
	}

	@Override
	public long calcNumStorageBytesFromPixelCount(long numPixels) {
		return 8 * calcNumStorageUnitsFromPixelCount(numPixels);
	}

	@Override
	public long calcNumStorageUnitsFromPixelCount(long numPixels) {
		return numPixels;
	}

	@Override
	public Object allocateStorageArray(int numPixels) {
		long storageUnitCount = calcNumStorageUnitsFromPixelCount(numPixels);
		return new double[(int)storageUnitCount];
	}

}
