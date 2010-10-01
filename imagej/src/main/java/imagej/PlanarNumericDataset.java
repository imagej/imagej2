package imagej;

import mpicbg.imglib.container.array.Array;

interface SampleInfo
{
	enum SampleType {BYTE, UBYTE, SHORT, USHORT, INT, UINT, LONG, FLOAT, DOUBLE};
	
	SampleType getValueType(); // type of each value
	int getNumValues();  // values per sample (a Complex could be two values of DOUBLE data)
	int getNumBitsPerValue();  // bits per value
	int getNumBits();  // bits in a complete sample
	// note that we can compose sample types bigger than 64 bit ints via multiple values per sample
}

interface PlanarNumericDataset 
{
	SampleInfo getSampleInfo();
	int[] getDimensions();
	long getTotalSamples();
	double getSample(int[] index, Array<?,?> dest);  // TODO change dest to correct thing when we know
	void getSamples(int[] index, int axisNumber, int numSamples, Array<?,?> dest);
	void doOperation(Object operation);  // TODO - change from Object to Operation once its defined
	long getPlaneCount(int axisNumber);
	void addAxis();
	void insertAxis(int axisNumber);
	void deleteAxis(int axisNumber, int planeToKeep);
	void addPlanes(int axis, Object planes);  // TODO define type so its not an Object
	void insertPlanes(int axis, int insertionSpot, Object planes);  // TODO define type so its not an Object
	void deletePlanes(int axis, int deletionSpot, int planeCount);
	void setAxisOfInterest(int axisNumber);  // hints at how to organize storage for next set of operations : may not belong here
	int[] subregionDimensions(int axisNumber);
	void setPlane(int axisNumber, int planeNumber, Object plane);  // TODO - make plane something other than Object
}

interface Operation
{
	void apply(PlanarNumericDataset ds);
}
