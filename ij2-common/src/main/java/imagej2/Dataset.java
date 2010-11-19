package imagej2;

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
	Dataset removeSubset(int axis, int posiiton);  // ImageStack.removeSlice()
	void addSubset(int axis, int position, Dataset ds);  // ImageStack.addSlice()
	
	// ********* primitive access support
	Object getPrimitiveAccess();  // can return null
	void releasePrimitiveAccess();
}
