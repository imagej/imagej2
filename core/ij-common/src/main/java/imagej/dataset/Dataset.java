package imagej.dataset;

// NOTE - user chooses access by getting longs or double as needed to avoid precision loss problems. For efficiency's sake maybe we extend to all
//   data types. For now if you want integral access use set/getLong(). If you want float access use set/getReal(). The problem with extending it to
//   all data types - imagine the case where you have unsigned byte data. If you call getByte() you'd expect to be okay but really you should getShort()
//   to avoid data loss. This is not intuitive and furthermore loses all benefits of avoiding casts for efficiency's sake. I guess the lesson here is
//   to avoid casts and range checking by using getData(), checking getType(), and doing optimized data manipulation on your own.

import imagej.MetaData;
import imagej.types.Type;

/** the basic interface to data supported in ImageJ */
public interface Dataset
{
	/** the dimensions of the dataset */
	int[] getDimensions();
	
	/** the ImageJ Type of the dataset (ByteType, DoubleType, Unsigned12BitType, BitType, etc.) */
	Type getType();
	
	/** get the metadata associated with the dataset */
	MetaData getMetaData();

	/** set the metadata associated with the dataset */
	void setMetaData(MetaData metadata);
	
	/** returns true if this dataset composed of other datasets - may go away */
	boolean isComposite();
	
	/** returns the Dataset that owns this Dataset. The outermost Dataset has no parent. Somtimes useful information */
	Dataset getParent();
	
	/** sets this Dataset's parent field to the Dataset that owns this Dataset. Can set to null for outermost dataset. Generally do not use. */
	void setParent(Dataset dataset);

	/** gets the primitive backing array of this Dataset's data when possible. throws exception otherwise */
	Object getData();

	/** releases the primitive backing array of this Dataset. necessary for some Dataset implementations */
	void releaseData();

	/** sets the primitive backing array for this Dataset's data when possible. throws exception otherwise */
	void setData(Object data);
	
	/** makes a new subset within this Dataset. Only valid when doing so on Datasets that have no parent (i.e. the outermost Dataset) */
	Dataset insertNewSubset(int position);

	/** removes a subset within this Dataset. Only valid when doing so on Datasets that have no parent (i.e. the outermost Dataset) */
	Dataset removeSubset(int position);

	/** gets a subset one level below me. the position along my axis is specified to distinguish which subset to get */
	Dataset getSubset(int position);
	
	/** gets a subset multiple levels below me.  */
	Dataset getSubset(int[] index);
	
	/** get the data at the specified position as a double */
	double getDouble(int[] position);
	
	/** set the data at the specified position to a double value */
	void setDouble(int[] position, double value);

	/** get the data at the specified position as a long */
	long getLong(int[] position);
	
	/** set the data at the specified position to a long value */
	void setLong(int[] position, long value);
}
