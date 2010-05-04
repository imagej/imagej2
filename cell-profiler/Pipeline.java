//
// Pipeline.java
//

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ij.ImagePlus;
import ij.process.FloatProcessor;

/** A helper class for working with CellProfiler pipelines. */
public class Pipeline {

	// -- Constants --

	private static final String INIT_SCRIPT =
		"print 'Running script...'\n" +//TEMP
		"import traceback\n" +
		"try:\n" +
		"  import cellprofiler.utilities.jutil as jutil\n" +
		"  import cellprofiler.pipeline as cpp\n" +
		"  from cellprofiler.preferences import set_headless\n" +
		"  set_headless()\n" +
		"  pipeline = cpp.Pipeline()\n" +
		"  print jutil.to_string(path)\n" +//TEMP
		"  print 'Loading pipeline: ' + jutil.to_string(path)\n" +//TEMP
		// TODO - figure out why pipeline.load crashes
		"  pipeline.load(jutil.to_string(path))\n" +
		"  print 'Pipeline loaded.'\n" +//TEMP
		"  ext_image_names = pipeline.find_external_input_images()\n" +
		//"  ext_image_names = ['original']\n" +//TEMP
		"  dict = jutil.get_dictionary_wrapper(vars)\n" +
		"  for i in range(len(ext_image_names)):\n" +
		"    dict.put('ext_image_name' + str(i), ext_image_names[i])\n" +
		"  print 'Script finished.'\n" +//TEMP
		"except:\n" +
		"  print 'so sorry, caught exception'\n" +
		"  traceback.print_exc()\n";

	// -- Fields --

	/** Link with CellProfiler in Python land. */
	private PythonLink link;

	/** Variables accessible from the Python universe. */
	private Map<String, Object> vars;

	/** Results from the pipeline execution. */
	private ImagePlus[] results;

	// -- Constructor --

	public Pipeline(String pipelinePath) {
		link = new PythonLink();
		link.put("path", pipelinePath);
		vars = new HashMap<String, Object>();
		link.put("vars", vars);
		link.runString(INIT_SCRIPT);
	}

	// -- Pipeline methods --

	public String[] getImageNames() {
		//TEMP - give some dummy input image names
		List<String> nameList = new ArrayList<String>();
		for (int i=0;; i++) {
			Object o = vars.get("ext_image_name" + i);
			if (o == null || !(o instanceof String)) break;
			nameList.add((String) o);
		}
		return nameList.toArray(new String[0]);
	}

	public void runPipeline(ImagePlus[] images) {
		//TEMP - create a dummy output image
		ImagePlus imp = PipelineRunner.makeTestImage("result", 512, 512);
		results = new ImagePlus[] {imp};
		//TODO - pipeline.find_external_output_images()
	}

	public ImagePlus[] getOutputImages() {
		return results;
	}

}
