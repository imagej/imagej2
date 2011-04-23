package imagej.ui.swing.display;

import imagej.display.view.DatasetView;
import imagej.display.view.DimensionSliderPanel;
import imagej.display.view.DisplayViewBuilder;
import imagej.display.view.ImageDisplayPanel;
import imagej.display.view.ImgDisplayController;
import imagej.display.view.Util;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 *
 * @author GBH
 */


public class DisplayViewTest <T extends RealType<T> & NativeType<T>> {

		private static void center(final Window win) {
		final Dimension size = win.getSize();
		final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		final int w = (screen.width - size.width) / 2;
		final int h = (screen.height - size.height) / 2;
		win.setLocation(w, h);
	}

	public <T extends RealType<T> & NativeType<T>> void buildDisplay(final String[] args) {
		final String[] urls = {
			//"file:///C:/TestImages/TestImages/MyoblastCells.tif"
			"http://loci.wisc.edu/files/software/data/mitosis-test.zip"
			//"http://loci.wisc.edu/files/software/ome-tiff/z-series.zip"
		};
		final JFrame frame = new JFrame("ImgPanel Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImgDisplayController ctrl = new ImgDisplayController();
		final ImageDisplayPanel imgPanel = new ImageDisplayPanel(ctrl);
		int positionX = 0;
		int positionY = 0;
		//
		for (String url : urls) {
			final ImgPlus<T> img = Util.loadImage(url);
			//img.getAxes(Metadata
			// is it multichannel ??
			DatasetView view = null;
			if (img.getAxisIndex(Axes.CHANNEL) > 0) {
				if (img.dimension(img.getAxisIndex(Axes.CHANNEL)) == 3) {
					// If 3 channels, probably an RGB image
					view = DisplayViewBuilder.createCompositeRGBView(url, img);

				} else {
					view = DisplayViewBuilder.createView(url, img);
					// more than 3 channels
					// default luts up to 7
					// > 7, gray
				}
			} else {
				view = DisplayViewBuilder.createView(url, img);
			}
			if (view != null) {
				view.setPositionX(positionX);
				view.setPositionY(positionY);
				view.setImgCanvas(imgPanel);
				ctrl.addView(view);
				imgPanel.setMaxDimension(view);
				DimensionSliderPanel dsp = new DimensionSliderPanel(view);
				//imgPanel.addToControlPanel(dsp);
				imgPanel.add(dsp);
				positionX += 32;
				positionY += 32;
			}
		}
		imgPanel.resetSize();

		frame.setContentPane(imgPanel);
		frame.pack();
		center(frame);
		frame.setVisible(true);
		
	}

	public static final <T extends RealType<T> & NativeType<T>> void main(final String[] args) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new DisplayViewTest().buildDisplay(args);
				}

			});
		}

	}
}
