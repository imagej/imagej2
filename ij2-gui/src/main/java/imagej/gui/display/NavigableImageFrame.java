package imagej.gui.display;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import imagej.AxisLabel;
import imagej.dataset.Dataset;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import loci.formats.gui.AWTImageTools;

/**
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Grant Harris gharris at mbl.edu
 */
public class NavigableImageFrame extends JFrame {

	// TODO - Rework this class to be a JPanel, not a JFrame.

	private Dataset dataset;
	private int[] dims;
	private AxisLabel[] dimLabels;
	private int xIndex, yIndex;
	private int[] pos;

	private JLabel label;
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

		label = new JLabel(" ");
		getContentPane().add(label, BorderLayout.NORTH);

		imagePanel = new NavigableImagePanel();
		final JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BorderLayout());
		borderPanel.setBorder(new CompoundBorder(
			new EmptyBorder(3, 3, 3, 3),
			new LineBorder(Color.black)));
		borderPanel.add(imagePanel, BorderLayout.CENTER);
		getContentPane().add(borderPanel, BorderLayout.CENTER);
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
		dims = dataset.getDimensions();
		dimLabels = dataset.getMetaData().getAxisLabels();
		setTitle(dataset.getMetaData().getLabel());

		// extract width and height
		xIndex = yIndex = -1;
		for (int i = 0; i < dimLabels.length; i++) {
			if (dimLabels[i] == AxisLabel.X) xIndex = i;
			if (dimLabels[i] == AxisLabel.Y) yIndex = i;
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

		final StringBuilder sb = new StringBuilder();
		for (int i = 0, p = -1; i < dims.length; i++) {
			if (isXY(dimLabels[i])) continue;
			p++;
			if (dims[i] == 1) continue;
			sb.append(dimLabels[i] + ": " + (pos[p] + 1) + "/" + dims[i] + "; ");
		}
		sb.append(dims[xIndex] + "x" + dims[yIndex] + "; ");
		sb.append(dataset.getType());
		label.setText(sb.toString());
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
		for (int i = 3; i < dims.length; i++) {
			if (dims[i] > 1) rows.append(", 3dlu, pref");
		}
		final PanelBuilder panelBuilder = new PanelBuilder(
			new FormLayout("pref, 3dlu, pref:grow", rows.toString()));
		//panelBuilder.setDefaultDialogBorder();
		final CellConstraints cc = new CellConstraints();

		for (int i = 0, p = -1, row = 1; i < dims.length; i++) {
			if (isXY(dimLabels[i])) continue;
			p++;
			if (dims[i] == 1) continue;
			final JLabel label = new JLabel(dimLabels[i].toString());
			final JScrollBar slider = new JScrollBar(Adjustable.HORIZONTAL,
				1, 1, 1, dims[i] + 1);
			final int posIndex = p;
			slider.addAdjustmentListener(new AdjustmentListener() {
				@Override
				@SuppressWarnings("synthetic-access")
				public void adjustmentValueChanged(AdjustmentEvent e) {
					pos[posIndex] = slider.getValue() - 1;
					updatePosition();
				}
			});
			panelBuilder.add(label, cc.xy(1, row));
			panelBuilder.add(slider, cc.xy(3, row));
			row += 2;
		}

		return panelBuilder.getPanel();
	}

	private static boolean isXY(AxisLabel dimLabel) {
		return dimLabel == AxisLabel.X || dimLabel == AxisLabel.Y;
	}

}
