package imagej.data;

import imagej.StorageType;

/** The interface for the support of all pixel data types in ImageJ. Types have some methods for querying information. They also have
 * methods for handling the storage of their data. This could be separated but keeping this info together simplifies the expansion to
 * new types when needed. To add a new type you need to define a class derived from Type, implement its methods, and implement a
 * DataAccessor for the Type. The type needs to then be added to the Types class' static initialization code (see Types.java). If a
 * new Imglib type is created and needs to be supported then the process is the same but there are a couple places in ij2-imglib that
 * also need to be tweaked. For example, imagej.imglib.process.ImageUtils::getPlaneCopy(), and
 * imagej.ij1bridge.process.ImageUtils::createProcessor() both need to be updated as they need knowledge of all Imglib types to
 * correctly create their data.
 */
public interface Type
{
	/** the name of the Type - a unique identifier */
	String getName();
	
	// **** informational interface **********************

	/** true if type has floating point values. if false then type is assumed to be integral data. */
	boolean isFloat();
	
	/** true if type has unsigned values only. if false then type is assumed to be signed. */
	boolean isUnsigned();
	
	/** returns the number of bits of information within each value. Does not imply any storage size! */
	int getNumBitsData();
	
	/** returns the minimum value for this type as a real value. some data types (such as longs) may lose precision using this call */
	double getMinReal();

	/** returns the maximum value for this type as a real value. some data types (such as longs) may lose precision using this call */
	double getMaxReal();
	
	/** returns the minimum value for this type as a integral value. some data types (such as floating types) may lose precision using this call */
	long getMinIntegral();

	/** returns the maximum value for this type as a integral value. some data types (such as floating types) may lose precision using this call */
	long getMaxIntegral();
	
	// **** read/write interface **********************
	
	/** returns a type appropriate DataAccessor that can be used to read and write values to the specified array */
	DataAccessor allocateArrayAccessor(Object array);
	
	// **** internal storage interface **********************

	/** returns the StorageType (i.e. UINT8, INT64, FLOAT32, etc.) values of this type are encoded as */
	StorageType getStorageType();
	
	/** returns the number of storage units each value requires to represent itself in its storageType. In practice its a double in the range (0,1]. */
	double getNumberOfStorageTypesPerValue();
	
	/** returns true if the type of the specified data array is compatible with this type's internal representation */
	boolean isStorageCompatible(Object data);

	/** returns the number of bytes of storage used by a given number of pixels of this type */
	long calcNumStorageBytesFromPixelCount(long numPixels);

	/** returns the number of storage units required to storage a given number of pixels of this type */
	long calcNumStorageUnitsFromPixelCount(long numPixels);

	/** allocates and returns an array of the correct storage type that can hold the given number of pixels */
	Object allocateStorageArray(int numPixels);
}
