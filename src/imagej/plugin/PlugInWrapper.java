package imagej.plugin;

import ij.IJ;

/**
 * @author Johannes Schindelin johannes.schindelin at imagejdev.org
 */
public class PlugInWrapper extends RunnableAdapter {
	public PlugInWrapper() {
		super(null);
	}

	public void run(String arg) {
		try {
			Class clazz = IJ.getClassLoader().loadClass(arg);
			plugin = (Runnable)clazz.newInstance();
			super.run(arg);
		} catch (ClassNotFoundException e) {
			IJ.error("Could not find class '" + arg + "'");
		} catch (InstantiationException e) {
			IJ.error("Could not instantiate class '" + arg + "'");
		} catch (IllegalAccessException e) {
			IJ.error("Could not access constructor of '" + arg + "'");
		}
	}
}
