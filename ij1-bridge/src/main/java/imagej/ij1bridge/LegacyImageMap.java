package imagej.ij1bridge;

import ij.ImagePlus;
import ij.WindowManager;
import imagej.AxisLabel;
import imagej.MetaData;
import imagej.data.FloatType;
import imagej.data.Type;
import imagej.data.UnsignedByteType;
import imagej.data.UnsignedShortType;
import imagej.dataset.Dataset;
import imagej.dataset.PlanarDataset;
import imagej.plugin.IPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.api.ImageJPluginRunner;

import java.util.Map;
import java.util.WeakHashMap;

public class LegacyImageMap {

	// TODO - change Dataset to Img when imglib2 is ready
	private Map<ImagePlus, Dataset> imageTable =
		new WeakHashMap<ImagePlus, Dataset>();

	/** Ensures that the given legacy image has a corresponding dataset. */
	public void registerLegacyImage(ImagePlus imp) {
		synchronized (imageTable) {
			Dataset dataset = imageTable.get(imp);
			if (dataset == null) {
				// mirror image window to dataset
				dataset = createDatasetMirror(imp);
				imageTable.put(imp, dataset);

				// UGLY HACK - signal creation of new dataset (e.g., to displayers)
				// TODO - think more about the architecture surrounding this
				final Dataset datasetOutput = dataset;
				if (datasetOutput == null) System.out.println("DATASETOUTPUT IS NULL");//TEMP
				new ImageJPluginRunner().postProcess(new IPlugin() {
					@Parameter(output=true)
					private Dataset output = datasetOutput;

					@Override
					public void run() { }
				});
			}
		}
	}

	/** Ensures that the given dataset has a corresponding legacy image. */
	public void registerDataset(Dataset dataset) {
		// find image window
		ImagePlus imp = null;
		synchronized (imageTable) {
			for (final ImagePlus key : imageTable.keySet()) {
				final Dataset value = imageTable.get(key);
				if (dataset == value) {
					imp = key;
					break;
				}
			}
			if (imp == null) {
				// mirror dataset to image window
				imp = createLegacyImageMirror(dataset);
				imageTable.put(imp, dataset);
			}
		}
		WindowManager.setTempCurrentImage(imp);
	}

	private Dataset createDatasetMirror(final ImagePlus imp) {
		final Type type;
		switch (imp.getType()) {
			case ImagePlus.GRAY8:
				type = new UnsignedByteType();
				break;
			case ImagePlus.GRAY16:
				type = new UnsignedShortType();
				break;
			case ImagePlus.GRAY32:
				type = new FloatType();
				break;
			default:
				throw new IllegalArgumentException(
					"Only uint8, uint16 and float32 supported for now");
		}
		// TODO - update this to work for multiple planes
		if (imp.getImageStackSize() > 1) {
			throw new IllegalArgumentException(
				"Only single plane images supported for now");
		}
		//final int[] dims = imp.getDimensions();
		final int[] dims = {imp.getWidth(), imp.getHeight()};
		final Object data = imp.getProcessor().getPixels();
		final Dataset dataset = new PlanarDataset(dims, type, data);
		final MetaData metadata = new MetaData();
		metadata.setLabel(imp.getTitle());
		final AxisLabel[] axisLabels = {
			AxisLabel.X, AxisLabel.Y, AxisLabel.CHANNEL, AxisLabel.Z, AxisLabel.TIME
		};
		metadata.setAxisLabels(axisLabels);
		dataset.setMetaData(metadata);
		return dataset;
	}

	private ImagePlus createLegacyImageMirror(final Dataset dataset) {
		// TODO Auto-generated method stub
		return null;
	}

}
