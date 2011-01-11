package imagej.gui.display;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import imagej.dataset.Dataset;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import loci.formats.gui.AWTImageTools;
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
	private int xIndex, yIndex;
	private int[] pos;

	private NavigableImagePanel imagePanel;
	private JPanel sliders;

	public NavigableImageFrame() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// maximize window size
		final GraphicsEnvironment ge =
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		final Rectangle bounds = ge.getMaximumWindowBounds();
		setBounds(bounds.width / 6, bounds.height / 6,
			2 * bounds.width / 3, 2 * bounds.height / 3);

		imagePanel = new NavigableImagePanel();
		getContentPane().add(imagePanel, BorderLayout.CENTER);
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
		dims = dataset.getDimensions();
		dimLabels = dataset.getMetaData().getAxisLabels();
		setTitle(dataset.getMetaData().getLabel());

		// extract width and height
		xIndex = yIndex = -1;
		for (int i = 0; i < dims.length; i++) {
			if (dimLabels[i].equals(ImageOpener.X)) xIndex = i;
			if (dimLabels[i].equals(ImageOpener.Y)) yIndex = i;
		}
		if (xIndex < 0) throw new IllegalArgumentException("No X dimension");
		if (yIndex < 0) throw new IllegalArgumentException("No Y dimension");

		// create sliders
		if (sliders != null) remove(sliders);
		sliders = createSliders();
		getContentPane().add(sliders, BorderLayout.SOUTH);

		// display first image plane
		pos = new int[dims.length - 2];
		updatePosition();
	}

	private void updatePosition() {
		final BufferedImage image = getImagePlane();
		imagePanel.setImage(image);
		imagePanel.setNavigationImageEnabled(true);
	}

	private BufferedImage getImagePlane() {
		// FIXME - how to get a subset with different axes?
		final Object plane = pos.length == 0 ? dataset.getData() :
			dataset.getSubset(pos).getData();
		if (plane instanceof byte[]) {
			return AWTImageTools.makeImage((byte[]) plane,
				dims[xIndex], dims[yIndex], !dataset.getType().isUnsigned());
		}
		else if (plane instanceof short[]) {
			return AWTImageTools.makeImage((short[]) plane,
					dims[xIndex], dims[yIndex], !dataset.getType().isUnsigned());			
		}
		else if (plane instanceof int[]) {
			return AWTImageTools.makeImage((int[]) plane,
					dims[xIndex], dims[yIndex], !dataset.getType().isUnsigned());			
		}
		else if (plane instanceof float[]) {
			return AWTImageTools.makeImage((float[]) plane,
				dims[xIndex], dims[yIndex]);			
		}
		else if (plane instanceof double[]) {
			return AWTImageTools.makeImage((double[]) plane,
				dims[xIndex], dims[yIndex]);
		}
		else {
			throw new IllegalStateException("Unknown data type: " +
				plane.getClass().getName());
		}
	}

	private JPanel createSliders() {
		if (dims.length == 2) return new JPanel();

		final StringBuilder rows = new StringBuilder("pref");
		for (int i = 3; i < dims.length; i++) rows.append(", 3dlu, pref");
		final PanelBuilder panelBuilder = new PanelBuilder(
			new FormLayout("pref, 3dlu, pref:grow", rows.toString()));
		panelBuilder.setDefaultDialogBorder();
		final CellConstraints cc = new CellConstraints();

		int p = 0;
		for (int i = 0; i < dims.length; i++) {
			if (isXY(dimLabels[i])) continue;
			final JLabel label = new JLabel(dimLabels[i]);
			final JSlider slider = new JSlider(1, dims[i], 1);
			int minorSpacing = dims[i] / 16;
			int majorSpacing = (dims[i] + 4) / 5;
			if (minorSpacing == 0) minorSpacing = 1;
			if (majorSpacing == 0) majorSpacing = 1;
			slider.setMinorTickSpacing(minorSpacing);
			slider.setMajorTickSpacing(majorSpacing);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			final int posIndex = p;
			slider.addChangeListener(new ChangeListener() {
				@Override
				@SuppressWarnings("synthetic-access")
				public void stateChanged(ChangeEvent e) {
					pos[posIndex] = slider.getValue() - 1;
					updatePosition();
				}
			});
			final int row = 2 * p + 1;
			panelBuilder.add(label, cc.xy(1, row));
			panelBuilder.add(slider, cc.xy(3, row));
			p++;
		}

		return panelBuilder.getPanel();
	}

	private static boolean isXY(String dimLabel) {
		return dimLabel.equals(ImageOpener.X) || dimLabel.equals(ImageOpener.Y);
	}

}
