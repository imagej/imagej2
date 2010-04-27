//
// CellProfilerPipeline.java
//

import ij.ImagePlus;
import ij.process.FloatProcessor;

/** A helper class for working with CellProfiler pipelines. */
public class PipelineBridge {
	
	// -- Fields --

	@SuppressWarnings("unused")
	private String path;
	
	private ImagePlus[] results;

	// -- Constructor --

	public PipelineBridge(String pipelinePath) {
		path = pipelinePath;
	}

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
	
}
