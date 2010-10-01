package imagej;

import mpicbg.imglib.container.array.Array;
import imagej.SampleInfo;

interface PlanarNumericDataset 
{
	SampleInfo getSampleInfo();
	int[] getDimensions();
	long getTotalSamples();
	void getSample(int[] index, Array<?,?> dest);  // TODO change dest to correct thing when we know
	void getSamples(int[] index, int axisNumber, Array<?,?> dest, int numSamples);
	void setSample(int[] index, Array<?,?> src);  // TODO change src to correct thing when we know
	void setSamples(int[] index, int axisNumber, Array<?,?> src, int numSamples);
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

/*
interface Operation
{
	void apply(PlanarNumericDataset ds);
}
*/