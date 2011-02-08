package imagej.ij1bridge.patches;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import imagej.Log;
import imagej.dataset.Dataset;
import imagej.ij1bridge.LegacyManager;
import imagej.plugin.IPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.api.ImageJPluginRunner;

/** Overrides {@link ImageWindow} methods. */
public final class ImageWindowMethods {

	private ImageWindowMethods() {
		// prevent instantiation of utility class
	}

	/** Replaces {@link ImageWindow#setVisible(boolean). */
	public static void setVisible(ImageWindow obj, boolean visible) {
		Log.debug("ImageWindow.setVisible(" + visible + "): " + obj);
		if (!visible) return;
		final ImagePlus imp = obj.getImagePlus();
		final Dataset dataset = LegacyManager.getImageMap().registerLegacyImage(imp);

		// TODO - change how this works:
		// - eliminate PluginRunner interface
		// - instead, all plugins are IPlugins
		// - create an IJ1 wrapper plugin: LegacyPluginRunner
		// - takes care of running ImageJ plugins
		// - produces List<Dataset> for all shown ImageWindows
		// - or could use the ParameterSpecifier(?) interface we plan to do
		// - LegacyPluginFinder would register pluginClass=LegacyPluginRunner
		//   for every IJ1 plugin found
		// - need a way to pass the actual IJ1 plugin class, and arg, to the
		//   LegacyPluginFinder instance
		// - once this works, the plugin postprocessors will work as normal

		// UGLY HACK - signal creation of new dataset (e.g., to displayers)
		// TODO - think more about the architecture surrounding this
		new ImageJPluginRunner().postProcess(new IPlugin() {
			@Parameter(output=true)
			private Dataset output = dataset;

			@Override
			public void run() { }
		});
	}

	/** Replaces {@link ImageWindow#show(). */
	public static void show(ImageWindow obj) {
		setVisible(obj, true);
	}

}
