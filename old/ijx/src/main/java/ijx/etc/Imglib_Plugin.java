
package ijx.etc;

import ij.ImagePlus;

import ij.gui.GenericDialog;

import ij.plugin.filter.PlugInFilter;

import ij.process.ImageProcessor;

import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImagePlusAdapter;
import mpicbg.imglib.image.display.imagej.ImageJFunctions;
import mpicbg.imglib.type.numeric.RealType;

// From Fiji template...

public class Imglib_Plugin<T extends RealType<T>> implements PlugInFilter {
	ImagePlus image;

	public int setup(String arg, ImagePlus imp) {
		image = imp;
		return DOES_ALL;
	}

	public void run(ImageProcessor ip) {
		run(image);
		image.updateAndDraw();
	}

	public void run(ImagePlus image) {
		Image<T> img = ImagePlusAdapter.wrap(image);
		add(img, 20);
	}

	public static<T extends RealType<T>> void add(Image<T> img, float value) {
		final Cursor<T> cursor = img.createCursor();
		final T summand = cursor.getType().createVariable();
		summand.setReal(value);
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.getType().add(summand);
		}
		cursor.close();
	}
}
