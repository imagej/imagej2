package imagej.envisaje.imageframe;

import imagej.AxisLabel;
import imagej.MetaData;
import imagej.dataset.Dataset;
import imagej.dataset.DatasetFactory;
import imagej.dataset.PlanarDatasetFactory;
import imagej.types.UnsignedByteType;

public class GradientImage {


	public static Dataset createDataset(int width, int height) {
		byte[] data = new byte[width * height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int index = y * width + x;
				data[index] = (byte) (x + y);
			}
		}

		final DatasetFactory datasetFactory = new PlanarDatasetFactory();
		final int[] dims = {width, height};
		Dataset dataset = datasetFactory.createDataset(new UnsignedByteType(), dims);
		dataset.setData(data);
		final MetaData metadata = new MetaData();
		metadata.setAxisLabels(new AxisLabel[] {AxisLabel.X, AxisLabel.Y});
		metadata.setLabel("Gradient Image");
		dataset.setMetaData(metadata);
		return dataset;
  }

}
