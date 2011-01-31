package imagej.gui;

import imagej.Log;
import imagej.MetaData;
import imagej.dataset.Dataset;
import imagej.imglib.dataset.ImgLibDataset;
import imagej.imglib.dataset.LegacyImgLibDataset;
import imagej.plugin.ij2.IPlugin;
import imagej.plugin.ij2.Menu;
import imagej.plugin.ij2.Parameter;
import imagej.plugin.ij2.Plugin;

import java.io.IOException;

import javax.swing.JFileChooser;

import loci.formats.ChannelMerger;
import loci.formats.FormatException;
import loci.formats.gui.GUITools;
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

	@Parameter(output=true)
	private Dataset dataset;

	@Override
	public void run() {
		// TODO implement input as a java.io.File rather than using GUI explicitly
		// then this plugin can be moved out of ij2-gui and into ij2-io

		// prompt for input file
		final JFileChooser fileChooser =
			GUITools.buildFileChooser(new ChannelMerger());
		final int rval = fileChooser.showOpenDialog(null);
		if (rval != JFileChooser.APPROVE_OPTION) return; // canceled
		final String id = fileChooser.getSelectedFile().getAbsolutePath();

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
