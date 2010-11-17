package imagej2;

import imagej2.SampleInfo;

// TODO - these classes not yet in use. They document things that would be good to support in a dataset

/*

interface Sample<T>
{
	SampleInfo getInfo();
	int getValuesPerSample();
	T getSubsample(int i);
	void setSubsample(int i, T value);
}


 * things I'd like to do with datasets
 * 
 * has metadata: dimensional labels, slice labels, other things
 * getType()
 * getDimensions()
 * concat two datasets into one
 * get the last three planes in a z stack as a subset
 * get the first three planes in a z stack as a subset
 * get every 4th planes in a z stack as a subset
 * get a range of values within a dataset as a dataset
 *   i.e. rows 1-4 and cols 2-12 of N dim dataset 
 * combine various subset choices and end up with a dataset
 * interleave two datasets into one (1 then 1, or 2 then 1, or some other algorithm  - see functional langs)
 * pull the first six planes in one z stack and 3 from another and combine every 2/1 into a new dataset
 * duplicate a dataset (create all new storage but with old values)
 * remove an axis from the dataset
 *   this includes removing a channel from all samples
 * add an axis to the dataset
 *   this includes adding a channel to all samples
 * transform the values within a dataset given a function and a selector
 * query a dataset (a read only visitor that gathers sample and position info)
 * insert/delete a compatible Dataset at a place in the Dataset (like a plane along an axis)
 * create a dataset from given dimensions.
 * get planeCount along an axis (or get count of objects in dataset shaped like {1,4,3,10} (along axes?)
 * get a reference within a dataset that does not copy data
 * combine n compatible datasets into one n+1 dimensionsal dataset
 * reorder indices of a dataset. also actually transform storage orders????
 * operations that take n datasets, apply func, and combine result into 1 dataset. n can be 1, 2, etc.
 * thinks of operations as operators in an equation. datasets as inputs. store in reverse polish notation?
 *   then we have ds2 = add (multiply ds1 6) 7 
 *     Dataset transform(Dataset ds, NAryFunction function, Dataset[] datasets)
 *       returns original Dataset with pixels changed
 *     Dataset assign(NAryFunction function, Dataset[] datasets)
 *       returns a new Dataset. Actually creates a new dataset and calls transform() on it.
 *     Dataset duplicate(Dataset ds)
 *       returns a new Dataset. Creates one, calls assign with a CopyFunc
 *     Dataset reference(Dataset ds, SomeSubsettingOrSelectionFunction func)
 *       returns new ReferenceDataset that is a reference to old dataset. The function could cause us to make jagged
 *         datasets and may not be supportable
 *     Dataset concat(Dataset[] datasets)
 *       returns a new Dataset (??). Creates a dataset. transform() it with a copy function of some sort.
 *     Dataset interleave(Dataset[] datasets, int alongAxis)
 *       returns a new Dataset. creates one of correct size. alternates between datasets and CopyFunc planes?
 *     Dataset cuboidSubset(Dataset ds, int axis, int[] origin, int[] span)
 *       returns a ReferenceDataset. Does a reference() of some sort.
 *     Dataset copyOfCuboidSubset(Dataset ds, int axis, int[] origin, int[] span)
 *       returns a new Dataset. Does a reference() and a duplicate()
 *       
 * for those operations that 1st create a dataset and then populate it will they know final dimensions before the
 *   chain of operations are done????
 *
 * would be nice to be able to use filters of some sort to just grab every other plane in a dataset for instance.
 *   I think this would use the subset operator to get a reference. Need a rich set of subset operators. And ways
 *   to combine them.
 *   
 * all functions are NAryFunctions. A function specifies the number of parameters it can handle. -1 means any number.
 *   Store them in dffierent packages by the number they can handle. imagej.function.parameters.one etc.  Also could
 *   create special cases (UnaryFunction, BinaryFunction, that are NAryFunctions that handle only one or two values).
 *   

interface Dataset
{
}

// returns its own dimensions. refers to another dataset. does not have its own data. translates indexes from its
//   coord space to the referenced dataset's coord space
class DatasetReference implements Dataset
{
}

// actual storage of data in ram or some other storage 
class ConcreteDataset implements Dataset
{
}

// a dataset made up of other datasets
class CompositeDataset implements Dataset
{
	private List<Dataset> subsets;
}

interface UnaryDatasetOperation
{
	Dataset doOperation(UnaryFunction function);
}

interface BinaryDatasetOperation
{
	Dataset doOperation(BinaryFunction function, Dataset otherDataset);
}

interface NAryDatasetOperation
{
	Dataset doOperation(NAryFunction function, List<Dataset> otherDatasets);
}

*/

interface NumericDataset
{
	SampleInfo getSampleInfo();
	int[] getDimensions();
	long getTotalSamples();
	void getSample(int[] index, Object dest);  // TODO change dest to correct thing when we know
	void getSamples(int[] index, int axisNumber, Object dest, int numSamples);
	void setSample(int[] index, Object src);  // TODO change src to correct thing when we know
	void setSamples(int[] index, int axisNumber, Object src, int numSamples);
	//void transform(UnaryFunction func);
	//void transform(BinaryFunction func, NumericDataset other);
	//void transform(NAryFunction func, NumericDataset[] others);
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
	//void transform(int axisNumber, int plane, UnaryFunction func);
	//void transform(int axisNumber, int plane, BinaryFunction func, NumericDataset other);
	//void transform(int axisNumber, int plane, NAryFunction func, NumericDataset[] others);
}

/*
interface Operation
{
	void apply(NumericDataset ds);
}
*/