package imagej.ij1bridge.plugin;

import ij.IJ;
import imagej.dataset.Dataset;
import imagej.plugin.IPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.ListUtils;

import java.util.ArrayList;
import java.util.List;

/** Executes an IJ1 plugin. */
@Plugin
public class LegacyPlugin implements IPlugin {

	@Parameter
	private String className;

	@Parameter
	private String arg;

	@Parameter(output=true)
	private List<Dataset> outputs;

	@Override
	public void run() {
		final ArrayList<Dataset> outputList = LegacyPlugin.getOutputList();
		outputList.clear();
		IJ.runPlugIn(className, arg);
		outputs = ListUtils.copyList(outputList);
		outputList.clear();
	}

	/** Used to provide one list of datasets per calling thread. */
	private static ThreadLocal<ArrayList<Dataset>> outputDatasets =
		new ThreadLocal<ArrayList<Dataset>>()
	{
		@Override
		protected synchronized ArrayList<Dataset> initialValue() {
			return new ArrayList<Dataset>();
		}
	};

	/**
	 * Gets a list for storing output parameter values.
	 * This method is thread-safe, because it uses a separate map per thread.
	 */
	public static ArrayList<Dataset> getOutputList() {
		return outputDatasets.get();
	}

}
