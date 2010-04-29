//
// PipelineRunner.java
//

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.io.File;
import java.util.HashMap;

/** A plugin for accessing CellProfiler pipelines from within ImageJ. */
public class PipelineRunner implements PlugIn {

	// -- Constants --

	private static final String NO_IMAGE = "<no open images>";

	// -- PlugIn methods --

	public void run(String arg) {
		String pipelinePath = choosePipeline();

		Pipeline pipeline = new Pipeline(pipelinePath);
		ImagePlus[] inputs = chooseImages(pipeline);
		if (inputs == null) return; // canceled

		pipeline.runPipeline(inputs);

		ImagePlus[] outputs = pipeline.getOutputImages();
		displayResults(outputs);
	}

	// -- PipelineRunner methods --

	public String choosePipeline() {
		OpenDialog od = new OpenDialog("Pipeline to run", null);
		String dir = od.getDirectory();
		String name = od.getFileName();
		if (dir == null || name == null) return null;
		return new File(dir, name).getAbsolutePath();
	}

	public ImagePlus[] chooseImages(Pipeline pipeline) {
		String[] inputNames = pipeline.getImageNames();

		HashMap<String, ImagePlus> imageMap = makeImageMap();
		String[] imageNames = imageMap.keySet().toArray(new String[0]);

		// NB: To allow null images to be passed to CellProfiler in general,
		//     we could remove the conditional here and instead always add
		//     NO_IMAGE to the image map.
    if (imageNames.length == 0) imageNames = new String[] {NO_IMAGE};

		GenericDialog gd = new GenericDialog("Select images");
		for (int i=0; i<inputNames.length; i++) {
			int index = imageNames.length > 0 ? i % imageNames.length : 0;
			gd.addChoice(inputNames[i], imageNames, imageNames[index]);
		}
		gd.showDialog();
		if (gd.wasCanceled()) return null;

		ImagePlus[] images = new ImagePlus[imageNames.length];
		for (int i=0; i<images.length; i++) {
			String imageName = gd.getNextChoice();
			if (!NO_IMAGE.equals(imageName)) {
				images[i] = imageMap.get(imageName);
			}
		}
		return images;
	}

	public void displayResults(ImagePlus[] outputs) {
		for (ImagePlus image : outputs) image.show();
	}

	// -- Helper methods --

	/** Creates a table of images. */
	private HashMap<String, ImagePlus> makeImageMap() {
		HashMap<String, ImagePlus> imageMap = new HashMap<String, ImagePlus>();
		int nImages = WindowManager.getImageCount();
		for (int j=0; j<nImages; j++) {
			ImagePlus image = WindowManager.getImage(j + 1);
			if (image != null) {
				String imageName = (j + 1) + " " + image.getTitle();
				imageMap.put(imageName, image);
			}
		}
		return imageMap;
	}

}
