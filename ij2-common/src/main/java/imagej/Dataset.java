package imagej;

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

/*
 * some thoughts
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