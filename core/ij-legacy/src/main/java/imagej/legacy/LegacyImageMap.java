package imagej.legacy;

import ij.ImagePlus;
import ij.WindowManager;
import imagej.dataset.Dataset;

import java.util.Map;
import java.util.WeakHashMap;

public class LegacyImageMap {

	// TODO - change Dataset to Img when imglib2 is ready
	private Map<ImagePlus, Dataset> imageTable =
		new WeakHashMap<ImagePlus, Dataset>();

	private ImageTranslator imageTranslator = new ImageTranslator();

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
				dataset = imageTranslator.createDataset(imp);
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
				imp = imageTranslator.createLegacyImage(dataset);
				imageTable.put(imp, dataset);
			}
		}
		WindowManager.setTempCurrentImage(imp);
		return imp;
	}

}
