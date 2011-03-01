package imagej.core.plugins;

import imagej.model.AxisLabel;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Rick Lentz
 */
@Plugin(
  menuPath="PureIJ2>Process>Gradient"
)
public class GradientImage implements ImageJPlugin {

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

		final String name = "Gradient Image";
		final int[] dims = {width, height};
		final AxisLabel[] axes = {AxisLabel.X, AxisLabel.Y};
		dataset = Dataset.create(name, new UnsignedByteType(), dims, axes);
		dataset.setPlane(0, data);
  }

}
