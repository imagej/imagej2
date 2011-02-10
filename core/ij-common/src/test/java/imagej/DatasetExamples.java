package imagej;

import imagej.data.DataAccessor;
import imagej.data.Type;
import imagej.data.Types;
import imagej.dataset.Dataset;
import imagej.dataset.DatasetDuplicator;
import imagej.dataset.DatasetFactory;
import imagej.dataset.DatasetView;
import imagej.dataset.FixedDimensionDataset;
import imagej.dataset.PlanarDatasetFactory;
import imagej.dataset.PrimitiveDatasetCreator;
import imagej.dataset.ReadOnlyDataset;
import imagej.process.Index;

public class DatasetExamples
{
	private Dataset createDataset(String typeString, int[] dimensions)
	{
		Type type = Types.findType(typeString);
		
		DatasetFactory factory = new PlanarDatasetFactory();
		
		return factory.createDataset(type, dimensions);
	}
	
	private void createDataFromValues()
	{
		PrimitiveDatasetCreator creator = new PrimitiveDatasetCreator(new PlanarDatasetFactory());
		
		Dataset ds;
		
		ds = creator.createDataset(new int[]{2,5}, true, new int[]{1,2,3,4,5,6,7,8,9,10});
		
		ds = creator.createDataset(new int[]{2,3}, false, new double[]{1.0,2.0,3.0,4.0,5.0,6.0});

		ds = creator.createDataset(new int[]{2,2}, true, new boolean[]{false,true,false,true});
	}
	
	private void datasetBasics()
	{
		int[] dimensions = new int[]{200,300,25,4,40};  // XYZCT order
		
		Dataset dataset = createDataset("64-bit float", dimensions);
		
		int[] position = Index.create(dimensions.length);  // initialized to origin
		
		// OR
		
		position = Index.create(new int[]{10,20,15,1,5});  // initialized to something else
		
		dataset.setDouble(position, 97.3);
		
		System.out.println("value is " + dataset.getDouble(position));
		
		AxisLabel[] labels = dataset.getMetaData().getAxisLabels();
		System.out.println("axis label for x = " + labels[0]);

		Type type = dataset.getType();
		System.out.println("is floating type = " + type.isFloat());
		System.out.println("is unsigned type = " + type.isUnsigned());
	}
	
	private void workWithAnXYPlane()
	{
		int[] dimensions = new int[]{200,300,25,4,40};

		Dataset ds = createDataset("16-bit signed", dimensions);
		
		Dataset xyPlane = ds.getSubset(new int[]{13,1,20});
		
		int[] twoDimPos = new int[]{50,40};
		
		xyPlane.setLong(twoDimPos, 33);
	}
	
	private void workWithZTPlane()
	{
		int[] dimensions = new int[]{200,300,25,4,40};

		Dataset ds = createDataset("32-bit unsigned", dimensions);
		
		Dataset ztPlane = new DatasetView(ds, new int[]{105,240,-1,0,-1});
		
		int[] ztPos = new int[]{10,15};
		
		ztPlane.setLong(ztPos, 106);
	}
	
	private Dataset createACopyOfZTPlane()
	{
		int[] dimensions = new int[]{200,300,25,4,40};

		Dataset ds = createDataset("32-bit float", dimensions);
		
		Dataset ztPlane = new DatasetView(ds, new int[]{105,240,-1,0,-1});
		
		return new DatasetDuplicator().createDataset(new PlanarDatasetFactory(), ztPlane);
	}
	
	private Dataset readOnlyDataset()
	{
		Dataset ds = createDataset("1-bit unsigned",new int[]{30,50});
		
		return new ReadOnlyDataset(ds);
	}
	
	private Dataset fixedDimensionDataset()
	{
		Dataset ds = createDataset("1-bit unsigned",new int[]{30,50});
		
		return new FixedDimensionDataset(ds);
	}
	
	private void typeRelatedStuff()
	{
		Type type = Types.findType("12-bit unsigned");
		
		long numPixels = 1024;
		
		Object arrayOfData = type.allocateStorageArray(numPixels);
		
		DataAccessor accessor = type.allocateArrayAccessor(arrayOfData);
		
		accessor.setIntegral(0, 44L);
		accessor.setReal(1, 99.0);
		
		accessor.getIntegral(0);
		accessor.getReal(1);
		
		long numStorageUnits = type.calcNumStorageUnitsFromPixelCount(numPixels);
		
		StorageType storageType = type.getStorageType();
		
		String typeNameIdentifier = type.getName();
		
		int numBitsOfInfo = type.getNumBitsData();
	}
}
