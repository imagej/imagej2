package imagej.data;

import imagej.StorageType;

public interface Type
{
	// name : unique identifier
	String getName();
	
	// info about type
	boolean isFloat(); // else isIntegral();
	boolean isUnsigned(); // else isSigned();
	int getNumBitsData();
	double getMinReal();
	double getMaxReal();
	long getMinIntegral();
	long getMaxIntegral();
	
	// reader/writer of type
	DataAccessor allocateAccessor(Object array);
	
	// storage related methods for type
	StorageType getStorageType();
	double getNumberOfStorageTypesPerValue();  // could live with DataAccessor???
	boolean isStorageCompatible(Object data);
	long calcNumStorageBytesFromPixelCount(long numPixels);
	long calcNumStorageUnitsFromPixelCount(long numPixels);
	Object allocateStorageArray(int numPixels);
}
