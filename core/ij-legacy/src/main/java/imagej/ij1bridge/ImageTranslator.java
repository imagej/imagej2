package imagej.ij1bridge;

import ij.ImagePlus;
import imagej.AxisLabel;
import imagej.MetaData;
import imagej.data.Type;
import imagej.dataset.CompositeDataset;
import imagej.dataset.Dataset;
import imagej.dataset.PlanarDataset;

import java.util.ArrayList;

/** Translates between legacy and modern ImageJ image structures. */
public class ImageTranslator {

	public Dataset createDataset(final ImagePlus imp) {
		final Type type = IJ1TypeManager.getType(imp);
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

	public ImagePlus createLegacyImage(final Dataset dataset) {
		// TODO Auto-generated method stub
		return null;
	}

}
