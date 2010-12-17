package imagej.ij1bridge.plugin;

import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;
import imagej.ij1bridge.BridgeStack;
import imagej.ij1bridge.ImgLibDataset;
import imagej.ij1bridge.ImgLibProcessorFactory;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginException;
import imagej.plugin.PluginRunner;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import loci.formats.FormatException;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.io.ImageOpener;
import mpicbg.imglib.type.numeric.RealType;

import org.openide.util.lookup.ServiceProvider;

/** Executes an IJ1 plugin. */
@ServiceProvider(service=PluginRunner.class)
public class Ij1PluginRunner implements PluginRunner {

	@Override
	public void runPlugin(PluginEntry entry) throws PluginException {
		// get Class object for plugin entry
		final ClassLoader loader = ij.IJ.getClassLoader();
		final Class<?> pluginClass;
		try {
			pluginClass = Class.forName(entry.getPluginClass(), true, loader);
		}
		catch (ClassNotFoundException e) {
			throw new PluginException(e);
		}
		if (!PlugIn.class.isAssignableFrom(pluginClass)) {
			throw new PluginException("Not an IJ1 plugin");
		}

		// instantiate plugin
		final Object pluginInstance;
		try {
			pluginInstance = pluginClass.newInstance();
		}
		catch (InstantiationException e) {
			throw new PluginException(e);
		}
		catch (IllegalAccessException e) {
			throw new PluginException(e);
		}
		if (!(pluginInstance instanceof PlugIn)) {
			throw new PluginException("Not an ij.plugin.PlugIn");
		}
		PlugIn plugin = (PlugIn) pluginInstance;

		// execute plugin
		plugin.run(entry.getArgs());
	}

	///////////////////////////////////////////////////

	public static <T extends RealType<T>> boolean runPlugin(Image<T> img, String imgName,String command, String plugin ) {
		// Start ImageJ, NO_SHOW
		if (IJ.getInstance() == null) {
			new ImageJ(ImageJ.NO_SHOW);
			if (IJ.getInstance() == null) {
				System.err.println("ImageJ failed to start.");
				return false;
			} else {
				IJ.showMessage("OK -- ImageJ Started.");
			}
		}

		// create the ImagePlus
		ImgLibDataset<T> dataset = new ImgLibDataset<T>((Image<T>) img);
		BridgeStack stack = new BridgeStack(dataset, new ImgLibProcessorFactory(img));
		ij.ImagePlus imp = new ij.ImagePlus(imgName, (ij.ImageStack) stack);
		// ?? How do we handle the need for multiple image selections ??
		//    We would need to mirror the open images in IJ2 to the IJ1 WindowManager, no?
		// Perhaps pass (Image<T>[] imgs)
		imp.show();
		runPlugin(command, plugin, false);
		return true;
	}

	public static Object runPlugin(String commandName, String className, boolean createNewLoader) {
		if (className != null) {
			// Parse out the argument
			String arg = "";
			if (className.endsWith("\")")) {
				// extract string argument (e.g. className("arg"))
				int argStart = className.lastIndexOf("(\"");
				if (argStart > 0) {
					arg = className.substring(argStart + 2, className.length() - 2);
					className = className.substring(0, argStart);
				}
			}
			Object output = runUserPlugIn(commandName, className, arg, false);
			return output;
		}
		return null;
	}

	static Object runUserPlugIn(String commandName, String className, String arg, boolean createNewLoader) {
		if (createNewLoader) {
			// ??  classLoader = null;
		}
		ClassLoader loader = ij.IJ.getClassLoader();
		Object thePlugIn = null;
		try {
			thePlugIn = (loader.loadClass(className)).newInstance();
			if (thePlugIn instanceof ij.plugin.PlugIn) {
				((ij.plugin.PlugIn) thePlugIn).run(arg);
			} else if (thePlugIn instanceof ij.plugin.filter.PlugInFilter) {
				new ij.plugin.filter.PlugInFilterRunner(thePlugIn, commandName, arg);
			}
		} catch (ClassNotFoundException e) {
			if (className.indexOf('_') != -1 /*&& !suppressPluginNotFoundError*/) {
				ij.IJ.error("Plugin or class not found: \"" + className + "\"\n(" + e + ")");
			}
		} catch (NoClassDefFoundError e) {
			int dotIndex = className.indexOf('.');
			String cause = e.getMessage();
			int parenIndex = cause.indexOf('(');
			if (parenIndex >= 1) {
				cause = cause.substring(0, parenIndex - 1);
			}
			boolean correctClass = cause.endsWith(dotIndex < 0
				? className : className.substring(dotIndex + 1));
			if (!correctClass /*&& !suppressPluginNotFoundError*/) {
				ij.IJ.error("Plugin " + className
					+ " did not find required class: "
					+ e.getMessage());
			}
			if (correctClass && dotIndex >= 0) {
				return runUserPlugIn(commandName, className.substring(dotIndex + 1), arg, createNewLoader);
			}
			if (className.indexOf('_') != -1 /*&& !suppressPluginNotFoundError*/) {
				ij.IJ.error("Plugin or class not found: \"" + className + "\"\n(" + e + ")");
			}
		} catch (InstantiationException e) {
			ij.IJ.error("Unable to load plugin (ins)");
		} catch (IllegalAccessException e) {
			ij.IJ.error("Unable to load plugin, possibly \nbecause it is not public.");
		}
		//        if (redirectErrorMessages && !"HandleExtraFileTypes".equals(className)) {
		//            redirectErrorMessages = false;
		//        }
		//        suppressPluginNotFoundError = false;
		return thePlugIn;
	}

	// Test =================================================================
	public static <T extends RealType<T>> void main(String[] args) {
		String filename = "testpattern.tif";
		final ImageOpener imageOpener = new ImageOpener();
		try {
			Image<T> img = imageOpener.openImage(filename);
			String command = "Flip Vertically";
			String plugin = "ij.plugin.filter.Transformer(\"flipv\")";
			String imgName = "TestPattern";
			runPlugin(img, imgName, command, plugin);
			//runPlugin("PluginClass", );
		} catch (FormatException ex) {
			Logger.getLogger(Ij1PluginRunner.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Ij1PluginRunner.class.getName()).log(Level.SEVERE, null, ex);
		}
		// run "Red_And_Blue" plugin, no image...
		runPlugin("Red_And_Blue", "Red_And_Blue", false);
	}

}
