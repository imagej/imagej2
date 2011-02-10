import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import imagej.AxisLabel;
import imagej.MetaData;
import imagej.data.FloatType;
import imagej.data.Type;
import imagej.data.Types;
import imagej.data.UnsignedByteType;
import imagej.data.UnsignedShortType;
import imagej.dataset.Dataset;
import imagej.dataset.PlanarDatasetFactory;
import imagej.ij1bridge.BridgeStack;
import imagej.ij1bridge.DatasetProcessorFactory;
import imagej.ij1bridge.ProcessorFactory;
import imagej.process.Index;

public class Rescale_to_Type implements PlugIn {

	public void run(String arg) {
		// get currently active image
		final ImagePlus inputImage = IJ.getImage();
		if (inputImage == null) {
			IJ.error("No active images.");
			return;
		}

		// check input type
		int inputType = inputImage.getType();
		if (inputType != ImagePlus.GRAY8 && inputType != ImagePlus.GRAY16 &&
			inputType != ImagePlus.GRAY32)
		{
			IJ.error("Unsupported pixel type (" + inputType + "). Please convert\n" +
				"to 8-bit, 16-bit or 32-bit grayscale first.");
			return;
		}

		// prompt user for desired pixel type
		final Type type = promptType();
		if (type == null) return;

		// convert ImagePlus into rescaled IJ2 Dataset
		final Dataset dataset = convertToDataset(inputImage, type);

		// wrap resultant dataset into a new DatasetProcessor-backed ImagePlus
		final ImagePlus outputImage = createImagePlus(dataset);

		// display resultant image onscreen
		outputImage.show();
	}

	private Type promptType() {
		// get list of available pixel types
		final Type[] types = Types.getAvailableTypes();
		final String[] typeNames = new String[types.length];
		for (int i=0; i<types.length; i++) {
			typeNames[i] = types[i].getName();
		}

		// prompt user for desired pixel type
		final GenericDialog gd = new GenericDialog("Select pixel type");
		gd.addChoice("Type", typeNames, typeNames[0]);
		gd.showDialog();
		if (gd.wasCanceled()) return null;
		final int typeIndex = gd.getNextChoiceIndex();

		return types[typeIndex];
	}

	private Dataset convertToDataset(final ImagePlus inputImage,
		final Type type)
	{
		// construct list of dimensional extents
		final int sizeX = inputImage.getWidth();
		final int sizeY = inputImage.getHeight();
		final int sizeZ = inputImage.getNSlices();
		final int sizeC = inputImage.getNChannels();
		final int sizeT = inputImage.getNFrames();
		final int[] dims = {sizeX, sizeY, sizeC, sizeZ, sizeT};
		final AxisLabel[] axisLabels = {
			AxisLabel.X, AxisLabel.Y, AxisLabel.CHANNEL, AxisLabel.Z, AxisLabel.TIME
		};

		// create dataset
		final PlanarDatasetFactory factory = new PlanarDatasetFactory();
		final Dataset dataset = factory.createDataset(type, dims);
		final MetaData metadata = new MetaData();
		metadata.setLabel(inputImage.getTitle());
		metadata.setAxisLabels(axisLabels);
		dataset.setMetaData(metadata);

		// copy/rescale pixels
		rescalePixels(inputImage, dataset);

		return dataset;
	}
	
	private ImagePlus createImagePlus(final Dataset dataset) {
		// verify dataset dimensional extents
		final AxisLabel[] axisLabels = dataset.getMetaData().getAxisLabels();		
		assert axisLabels.length == 5;
		assert axisLabels[2] == AxisLabel.CHANNEL;
		assert axisLabels[3] == AxisLabel.Z;
		assert axisLabels[4] == AxisLabel.TIME;

		// compute total number of planes
		final int[] dims = dataset.getDimensions();
		final int sizeC = dims[2], sizeZ = dims[3], sizeT = dims[4];

		// build up stack of DatasetProcessors-wrapped planes
		final ProcessorFactory processorFactory =
			new DatasetProcessorFactory(dataset);
		// OR, to use existing IJ1 ImageProcessor types when possible:
		//	new IJ1ProcessorFactory(dataset, new DatasetProcessorFactory(dataset));
		final BridgeStack stack = new BridgeStack(dataset, processorFactory);

		// return ImagePlus of appropriate dimensions
		final String title = dataset.getMetaData().getLabel();
		final ImagePlus imp = new ImagePlus(title, stack);
		imp.setDimensions(sizeC, sizeZ, sizeT);
		imp.setOpenAsHyperStack(true);
		return imp;
	}

	/**
	 * Rescales the pixel values, copying everything into the target dataset.
	 * There is a more performant way to copy entire image planes by reference,
	 * but we are not using it since we need to rescale the pixel values anyway.
	 *
	 * @param imp input image, for conversion to a rescaled dataset
	 * @param dataset pre-allocated dataset, to populate with rescaled pixels
	 */
	private void rescalePixels(final ImagePlus imp, final Dataset dataset) {
		// get appropriate pixel type for input image
		final Type inputType;
		switch (imp.getType()) {
			case ImagePlus.GRAY8:
				inputType = new UnsignedByteType();
				break;
			case ImagePlus.GRAY16:
				inputType = new UnsignedShortType();
				break;
			case ImagePlus.GRAY32:
				inputType = new FloatType();
				break;
			default:
				throw new IllegalArgumentException("Unsupported image type:" +
					imp.getType());
		}
		
		// compute scale factor and offsets
		final double inputMin = inputType.getMinReal();
		final int inputBits = inputType.getNumBitsData();
		final int[] dims = dataset.getDimensions();
		final Type outputType = dataset.getType();
		double outputMin = outputType.getMinReal();
		final int outputBits = outputType.getNumBitsData();
		final double scaleFactor = Math.pow(2, outputBits - inputBits);

		// rescale and copy pixel values
		final int[] origin = new int[5];
		final int[] pos = Index.create(5);
		while (Index.isValid(pos, origin, dims)) {
			// NB: This formula is inexact for some combinations of types,
			//     but should be close enough for testing purposes.
			final double inputValue = getPixel(imp, pos[0], pos[1], pos[2], pos[3], pos[4]);
			final double outputValue = outputType.isFloat() ?
				(inputValue + 0.5) : (inputValue - inputMin) * scaleFactor + outputMin;
			dataset.setDouble(pos, outputValue);
			Index.increment(pos, origin, dims);
		}
	}

	/** Gets the image's pixel value at the given dimensional position. */
	private static double getPixel(final ImagePlus imp,
		int x, int y, int c, int z, int t)
	{
		final ImageStack imageStack = imp.getImageStack();

		// verify inputs
		assert imageStack != null || (c == 0 && z == 0 && t == 0);
		assert x >= 0 && x < imp.getWidth();
		assert y >= 0 && y < imp.getHeight();
		assert c >= 0 && c < imp.getNChannels();
		assert z >= 0 && z < imp.getNSlices();
		assert t >= 0 && t < imp.getNFrames();

		// extract pixel value as a double
		final int index = imp.getStackIndex(c, z, t);
		final ImageProcessor ip = imageStack == null ?
			imp.getProcessor() : imageStack.getProcessor(index);
		return ip.getf(x, y);
	}

}
