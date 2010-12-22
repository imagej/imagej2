package imagej.gui;

import imagej.data.UnsignedByteType;
import imagej.dataset.Dataset;
import imagej.dataset.DatasetFactory;
import imagej.dataset.PlanarDatasetFactory;
import imagej.plugin.ij2.IPlugin;
import imagej.plugin.ij2.Parameter;
import imagej.plugin.ij2.Plugin;

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
  }

}
