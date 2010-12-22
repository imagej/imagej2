package imagej.gui.display;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import imagej.dataset.Dataset;
import imagej.imglib.dataset.ImgLibDataset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.WindowConstants;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.io.ImageOpener;

/**
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Grant Harris gharris at mbl.edu
 */
public class NavigableImageFrame extends JFrame {

	private Dataset dataset;
	private int[] dims;
	private String[] dimLabels;
	private int width, height;

	private NavigableImagePanel imagePanel;
	private JPanel sliders;

	public NavigableImageFrame() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// maximize window size
		final GraphicsEnvironment ge =
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		final Rectangle bounds = ge.getMaximumWindowBounds();
		setSize(new Dimension(bounds.width, bounds.height));

		imagePanel = new NavigableImagePanel();
		getContentPane().add(imagePanel, BorderLayout.CENTER);
	}

	public void setDataset(final Dataset dataset) {
		final int[] dims = dataset.getDimensions();
		if (dims.length < 2) {
			throw new IllegalArgumentException(
				"Datasets with fewer than 2 dimensions are not supported");
		}
		this.dataset = dataset;
		this.dims = dims;
		dimLabels = getDimensionLabels(dataset);

		// extract width and height
		width = height = 0;
		for (int i = 0; i < dims.length; i++) {
			if (dimLabels[i] == ImageOpener.X) width = dims[i];
			if (dimLabels[i] == ImageOpener.Y) height = dims[i];
		}

		// create sliders
		if (sliders != null) remove(sliders);
		sliders = createSliders();
		getContentPane().add(sliders, BorderLayout.SOUTH);

		// display first image plane
		final int[] pos = new int[dims.length - 2];
		setPosition(pos);
	}

	private void setPosition(final int[] pos) {
		final BufferedImage image = getImagePlane(pos); 
		imagePanel.setImage(image);
		imagePanel.setNavigationImageEnabled(true);
	}

	private BufferedImage getImagePlane(final int[] pos) {
		if (pos.length != dims.length - 2) {
			throw new IllegalArgumentException("Invalid position");
		}

		//TEMP - need to actually extract image plane from dataset
		//for now, we paint a random image plane, for testing

		// convert position to string label
		final StringBuilder label = new StringBuilder();
		int p = 0;
		boolean first = true;
		for (int i = 0; i < dims.length; i++) {
			if (dimLabels[i] == ImageOpener.X || dimLabels[i] == ImageOpener.Y) {
				continue;
			}
			if (first) first = false;
			else label.append(", ");
			label.append(dimLabels[i] + "=" + pos[p++]);
		}

		// create a random labeled image
		final BufferedImage image = new BufferedImage(width, height,
			BufferedImage.TYPE_BYTE_GRAY);
		final Graphics g = image.getGraphics();
		final int randomR = (int) (200 * Math.random());
		final int randomG = (int) (200 * Math.random());
		final int randomB = (int) (200 * Math.random());
		g.setColor(new Color(randomR, randomG, randomB));
		g.fillRect(0, 0, width, height);
		g.setColor(Color.white);
		g.drawString(label.toString(), width / 2, height / 2);
		g.dispose();

		return image;
	}

	private JPanel createSliders() {
		final StringBuilder rows = new StringBuilder();
		if (dims.length > 0) rows.append("3dlu");
		for (int i = 1; i < dims.length; i++) rows.append(", 3dlu, pref");
		PanelBuilder panelBuilder = new PanelBuilder(
			new FormLayout("pref, 3dlu, pref:grow", rows.toString()));
		CellConstraints cc = new CellConstraints();

		for (int i = 0; i < dims.length; i++) {
			final int row = 2 * i + 1;
			final JLabel label = new JLabel(dimLabels[i]);
			final JSlider slider = new JSlider(1, dims.length, 1);
			panelBuilder.add(label, cc.xy(1, row));
			panelBuilder.add(slider, cc.xy(3, row));
		}

		return panelBuilder.getPanel();
	}

	/**
	 * HACK - extract dimensional types from ImgLib image name.
	 * This code can disappear once Dataset interface supports labels.
	 */
	private static String[] getDimensionLabels(Dataset dataset) {
		String[] dimTypes = null;
		if (dataset instanceof ImgLibDataset) {
			final ImgLibDataset<?> imgLibDataset = (ImgLibDataset<?>) dataset;
			final Image<?> img = imgLibDataset.getImage();
			dimTypes = ImageOpener.decodeTypes(img.getName());
		}
		final int[] dims = dataset.getDimensions();
		if (dimTypes == null || dimTypes.length != dims.length) {
			dimTypes = new String[dims.length];
			if (dimTypes.length > 0) dimTypes[0] = ImageOpener.X;
			if (dimTypes.length > 1) dimTypes[1] = ImageOpener.Y;
			for (int i = 2; i < dimTypes.length; i++) dimTypes[i] = "Unknown";
		}
		return dimTypes;
	}

}
