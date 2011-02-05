package imagej.plugins.core;

import imagej.Log;
import imagej.MetaData;
import imagej.dataset.Dataset;
import imagej.imglib.dataset.ImgLibDataset;
import imagej.imglib.dataset.LegacyImgLibDataset;
import imagej.plugin.IPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

import java.io.File;
import java.io.IOException;

import loci.formats.FormatException;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.io.ImageOpener;
import mpicbg.imglib.type.numeric.RealType;

@Plugin(
	menu={
		@Menu(label="File", mnemonic='f'),
		@Menu(label="Import", mnemonic='i', weight=2),
		@Menu(label="Bio-Formats...", mnemonic='b', accelerator="control shift O")
	}
)
public class OpenImage<T extends RealType<T>> implements IPlugin {

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
			dataset = new LegacyImgLibDataset(img);
			// TEMP - populate required axis label metadata
			final MetaData metadata = ImgLibDataset.createMetaData(img.getName());
			dataset.setMetaData(metadata);
		}
		catch (FormatException e) {
			Log.printStackTrace(e);
		}
		catch (IOException e) {
			Log.printStackTrace(e);
		}
  }

}
