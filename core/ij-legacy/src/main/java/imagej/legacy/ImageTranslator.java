package imagej.legacy;

import ij.ImagePlus;
import imagej.model.AxisLabel;
import imagej.model.Dataset;
import imagej.model.Metadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImagePlusAdapter;
import mpicbg.imglib.image.display.imagej.ImageJFunctions;

/**
 * Translates between legacy and modern ImageJ image structures.
 *
 * @author Curtis Rueden
 */
public class ImageTranslator {

	public Dataset createDataset(final ImagePlus imp) {
		// HACK - avoid ImagePlusAdapter.wrap method's use of generics
		try {
			final Method m = ImagePlusAdapter.class.getMethod("wrap",
				ImagePlus.class);
			final Image<?> img = (Image<?>) m.invoke(null, imp);
			final Metadata metadata = populateMetadata(imp);
			final Dataset dataset = new Dataset(img, metadata);
			return dataset;
		}
		catch (NoSuchMethodException exc) {
			return null;
		}
		catch (IllegalArgumentException e) {
			return null;
		}
		catch (IllegalAccessException e) {
			return null;
		}
		catch (InvocationTargetException e) {
			return null;
		}
	}

	public ImagePlus createLegacyImage(final Dataset dataset) {
		return ImageJFunctions.displayAsVirtualStack(dataset.getImage());
	}

	private Metadata populateMetadata(final ImagePlus imp) {
		final AxisLabel[] axes = {
			AxisLabel.X, AxisLabel.Y, AxisLabel.CHANNEL, AxisLabel.Z, AxisLabel.TIME
		};
		final Metadata metadata = new Metadata();
		metadata.setName(imp.getTitle());
		metadata.setAxes(axes);
		return metadata;
	}

}
