//
// Pipeline.java
//

import ij.ImagePlus;
import ij.process.FloatProcessor;

/** A helper class for working with CellProfiler pipelines. */
public class Pipeline {

	// -- Fields --

	/** Path to the pipeline on disk. */
	private String path;

	/** Link with CellProfiler in Python land. */
	private PythonLink link;

	/** Results from the pipeline execution. */
	private ImagePlus[] results;

	// -- Constructor --

	public Pipeline(String pipelinePath) {
		path = pipelinePath;
		link = new PythonLink();
		initializePipeline(path);
	}

/*
import cellprofiler.pipeline as cpp
pipeline = cpp.Pipeline()
data = '/Users/curtis/code/Other/CellProfiler/ExampleImages/ExampleSBSImages/ExampleSBS.cp'
pipeline.load(data)
*/

	// -- CellProfilerPipeline methods --

	public String[] getImageNames() {
		//TEMP - give some dummy input image names
		return new String[] {"input", "kernel", "background"};
	}

	public void runPipeline(ImagePlus[] images) {
		//TEMP - create a dummy output image
		int width = 512, height = 512;
		float[][] pix = new float[width][height];
		for (int row=0; row<height; row++) {
			float rowNorm = (float) row / height;
			for (int col=0; col<width; col++) {
				float colNorm = (float) col / height;
				pix[row][col] = rowNorm * colNorm;
			}
		}
		FloatProcessor proc = new FloatProcessor(pix);
		ImagePlus imp = new ImagePlus("result", proc);
		results = new ImagePlus[] {imp};
	}

	public ImagePlus[] getOutputImages() {
		return results;
	}

  // -- Helper methods --

  private void initializePipeline(String path) {
    // TODO
  }

}
