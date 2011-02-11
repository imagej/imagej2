package imagej.types;

/** the DataAccessor interface defines how to data is written and read (using either doubles or longs). A DataAccessor
 * sits in front of any data structure that associates an index with a value. This interface works with both integral
 * and real types and it is up to the user to decide which form of access is correct. For instance if one only uses the
 * getReal() method then data values may lose precision for data structures that use longs. This interface is designed
 * as a workaround to get around this problem. 
 */
public interface DataAccessor
{
	/** gets a double value from the backing data structure at the given index */
	double getReal(long index);

	/** sets a double value in the backing data structure at the given index using given data value */
	void setReal(long index, double value);
	
	/** gets a long value from the backing data structure at the given index */
	long getIntegral(long index);

	/** sets a long value in the backing data structure at the given index using given data value */
	void setIntegral(long index, long value);
}
