
package ijx.plugin.parameterized;

import ijx.plugin.api.PlugIn;
import ijx.WindowManager;
import ijx.IJ;

import ijx.measure.Calibration;

import ijx.IjxImagePlus;

public class Tubeness extends AbstractPlugIn implements PlugIn {

	static final String PLUGIN_VERSION = "1.2";

	@Parameter(label="Input image")
	public IjxImagePlus original = null;

	@Parameter(label="Sigma")
	public double sigma = 1.0;

	@Parameter(label="Use calibration")
	public boolean useCalibration = false;

	@Parameter(label="Output image", output=true)
	public IjxImagePlus result = null;

	public void run() {

		if (original == null)
			original = WindowManager.getCurrentImage();
		if (original == null) {
			IJ.error("No current image to calculate tubeness of.");
			return;
		}

		Calibration calibration = original.getCalibration();

		double minimumSeparation = 1;
		if( calibration != null )
			minimumSeparation = Math.min(calibration.pixelWidth,
						     Math.min(calibration.pixelHeight,
							      calibration.pixelDepth));

		//TubenessProcessor tp = new TubenessProcessor(sigma,useCalibration);

		//result = tp.generateImage(original);
		result.setTitle("tubeness of " + original.getTitle());

		result.show();
	}
}
