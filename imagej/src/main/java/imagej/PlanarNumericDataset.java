package imagej;

import mpicbg.imglib.container.array.Array;
import imagej.SampleInfo;
import imagej.process.function.binary.BinaryFunction;
import imagej.process.function.nary.NAryFunction;
import imagej.process.function.unary.UnaryFunction;

// TODO - these classes not yet in use. They document things that would be good to support in a dataset

interface NumericDataset
{
	SampleInfo getSampleInfo();
	int[] getDimensions();
	long getTotalSamples();
	void getSample(int[] index, Array<?,?> dest);  // TODO change dest to correct thing when we know
	void getSamples(int[] index, int axisNumber, Array<?,?> dest, int numSamples);
	void setSample(int[] index, Array<?,?> src);  // TODO change src to correct thing when we know
	void setSamples(int[] index, int axisNumber, Array<?,?> src, int numSamples);
	void transform(UnaryFunction func);
	void transform(BinaryFunction func, NumericDataset other);
	void transform(NAryFunction func, NumericDataset[] others);
}

interface PlanarNumericDataset extends NumericDataset 
{
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
	void transform(int axisNumber, int plane, UnaryFunction func);
	void transform(int axisNumber, int plane, BinaryFunction func, NumericDataset other);
	void transform(int axisNumber, int plane, NAryFunction func, NumericDataset[] others);
}

interface Operation
{
	void apply(NumericDataset ds);
}
