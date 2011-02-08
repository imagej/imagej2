package imagej.ij1bridge;

import ij.ImagePlus;
import ij.WindowManager;
import imagej.AxisLabel;
import imagej.MetaData;
import imagej.data.FloatType;
import imagej.data.Type;
import imagej.data.UnsignedByteType;
import imagej.data.UnsignedShortType;
import imagej.dataset.CompositeDataset;
import imagej.dataset.Dataset;
import imagej.dataset.PlanarDataset;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

public class LegacyImageMap {

	// TODO - change Dataset to Img when imglib2 is ready
	private Map<ImagePlus, Dataset> imageTable =
		new WeakHashMap<ImagePlus, Dataset>();

	/**
	 * Ensures that the given legacy image has a corresponding dataset.
	 *
	 * @return the {@link Dataset} object shadowing this legacy image.
	 */
	public Dataset registerLegacyImage(ImagePlus imp) {
		synchronized (imageTable) {
			Dataset dataset = imageTable.get(imp);
			if (dataset == null) {
				// mirror image window to dataset
				dataset = createDatasetMirror(imp);
				imageTable.put(imp, dataset);
			}
			return dataset;
		}
	}

	/**
	 * Ensures that the given dataset has a corresponding legacy image.
	 *
	 * @return the {@link ImagePlus} object shadowing this dataset.
	 */
	public ImagePlus registerDataset(Dataset dataset) {
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
		return imp;
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
		final int[] dims = imp.getDimensions();
		final int[] cDims = {dims[0], dims[1]};
		final int[] zDims = {dims[0], dims[1], dims[2]};
		final int[] tDims = {dims[0], dims[1], dims[2], dims[3]};
		int i = 0;
		final ArrayList<Dataset> tDatasets = new ArrayList<Dataset>();
		for (int t=0; t<dims[4]; t++) {
			final ArrayList<Dataset> zDatasets = new ArrayList<Dataset>();
			for (int z=0; z<dims[3]; z++) {
				final ArrayList<Dataset> cDatasets = new ArrayList<Dataset>();
				for (int c=0; c<dims[2]; c++) {
					final Object plane = imp.getStack().getPixels(++i);
					cDatasets.add(new PlanarDataset(cDims, type, plane));
				}
				zDatasets.add(new CompositeDataset(type, zDims, cDatasets));
			}
			tDatasets.add(new CompositeDataset(type, tDims, zDatasets));
		}
		final Dataset dataset = new CompositeDataset(type, dims, tDatasets);

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
