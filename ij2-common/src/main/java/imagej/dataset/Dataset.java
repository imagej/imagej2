package imagej.dataset;

import imagej.MetaData;
import imagej.data.Type;

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
}

/* ORIGINAL PARTIAL HACK - keep for a while to glean any necessary info
public interface Dataset
{
	// ********* info query ops
	MetaData getMetaData();
	int[] getDimensions();
	SampleInfo getSampleInfo();
	DataEncoding getEncoding();
	
	// ********* value support
	double getDouble(int[] index);
	void setDouble(int[] index, double value);
	// void getDoubleSamples(int[] index, int alongAxis, int numSamples, double[] dest, int atDstIndex);
	// void setDoubleSamples(int[] index, int alongAxis, double[] sourceValues);
	
	// ********* subset support
	Dataset getSubset(int[] position, int[] spans);  // allow span values to be 0 if not interested in that axis: return a Reference
	Dataset destroySubset(int axis, int position);  // ImageStack.removeSlice(), position ranges 0..n-1
	Dataset createSubset(int axis, int position);  // ImageStack.addSlice(), position ranges 0..n
	
	// NOTE - create/destroy does not ensure parent Datasets are happy. Its user's responsibility to ensure valid shape of all Datasets
	
	// ********* primitive access support
	void setData(Object data); // can throw exception
	Object getData();  // can return null
	void releaseData();  // might be required for certain implementations (buffered/paging/locking type structures)
}
*/

/*
 * some thoughts that were from original hack but may be useful for later iterations
 *   could have a hint in metadata on how to access Dataset so that you have primitive access if desired
 *     would need to say "i'm organized in two dimensional subunits in the X & Y dimensions".
 *     maybe just specify number of dims of organization and have dimension order labels already stored in order.
 *     I put these hints in MetaData for now.
 *   BridgeStack would have to check that our Dataset organized in 2d XY buckets (also allow YX???). Otherwise
 *     it throws an exception since can't get primitive access. Done.
 *   Should DataEncoding get folded into SampleInfo. If so then SampleInfo limited to single encodings. May want to support multiple.
 *   Does it make sense for a Dataset to know its DataEncoding at all? I think my motivation was wanting the ability to figure out how much disk
 *     and memory space utilized in storing a Dataset's data. This is somewhat common in IJ 1.0. There may be another motivation but not coming
 *     to me right now.
 *   Imglib integration breaks these ideas unless I have ReferenceDatasets that can create coordinate transforms from its local view of the data to
 *     a parent's higher dimensional space. Curtis did not like this idea. Wants all subsets to be copies of the data. Then can maybe have
 *     getSubsetByReference() and getSubsetAsCopy(). This does away with a lot of the power of a recursive dataset representation.
*/
