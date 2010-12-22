package imagej.gui;

import java.io.IOException;

import javax.swing.JFileChooser;

import loci.formats.ChannelMerger;
import loci.formats.FormatException;
import loci.formats.gui.GUITools;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.io.ImageOpener;
import mpicbg.imglib.type.numeric.RealType;
import imagej.dataset.Dataset;
import imagej.ij1bridge.ImgLibDataset;
import imagej.plugin.IPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(
//	menuPath="File>Import>Bio-Formats...",
	menu={
    @Menu(label="File", weight=0, mnemonic='f'),
    @Menu(label="Import", weight=0, mnemonic='i'),
    @Menu(label="Bio-Formats...", weight=0, mnemonic='b')
  },
  accelerator="^O"
)
public class OpenImage<T extends RealType<T>> implements IPlugin {

	@Parameter(output=true)
	private Dataset dataset;

	@Override
	public void run() {
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
			dataset = new ImgLibDataset<T>(img);
			System.out.println("Image dimensions: " + dataset.getDimensions()[0] + " x " + dataset.getDimensions()[1]);//TEMP
		}
		catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }

}
