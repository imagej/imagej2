package imagej.plugins.core;

import imagej.AxisLabel;
import imagej.MetaData;
import imagej.data.UnsignedByteType;
import imagej.dataset.Dataset;
import imagej.dataset.DatasetFactory;
import imagej.dataset.PlanarDatasetFactory;
import imagej.plugin.IPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(
  menuPath="Process>Gradient"
)
public class GradientImage implements IPlugin {

	@Parameter
	private int width = 512;

	@Parameter
	private int height = 512;

	@Parameter(output=true)
	private Dataset dataset;

	@Override
	public void run() {
		byte[] data = new byte[width * height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int index = y * width + x;
				data[index] = (byte) (x + y);
			}
		}

		final DatasetFactory datasetFactory = new PlanarDatasetFactory();
		final int[] dims = {width, height};
		dataset = datasetFactory.createDataset(new UnsignedByteType(), dims);
		dataset.setData(data);
		final MetaData metadata = new MetaData();
		metadata.setAxisLabels(new AxisLabel[] {AxisLabel.X, AxisLabel.Y});
		metadata.setLabel("Gradient Image");
		dataset.setMetaData(metadata);
  }

}
