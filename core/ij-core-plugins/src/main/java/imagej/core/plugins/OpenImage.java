package imagej.core.plugins;

import imagej.Log;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

import java.io.File;
import java.io.IOException;

import loci.formats.FormatException;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.io.ImageOpener;
import mpicbg.imglib.type.numeric.RealType;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menu={
		@Menu(label="File", mnemonic='f'),
		@Menu(label="Import", mnemonic='i', weight=2),
		@Menu(label="Bio-Formats...", mnemonic='b', accelerator="control shift O")
	}
)
public class OpenImage<T extends RealType<T>> implements ImageJPlugin {

	@Parameter(label="File to open")
	private File inputFile;

	@Parameter(output=true)
	private Dataset dataset;

	@Override
	public void run() {
		final String id = inputFile.getAbsolutePath();

		// open image
		final ImageOpener imageOpener = new ImageOpener();
		try {
			final Image<T> img = imageOpener.openImage(id);
			dataset = new Dataset(img);
		}
		catch (FormatException e) {
			Log.error(e);
		}
		catch (IOException e) {
			Log.error(e);
		}
  }

}
