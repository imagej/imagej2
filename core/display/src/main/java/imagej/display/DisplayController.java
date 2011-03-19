package imagej.display;

import imagej.model.AxisLabel;
import imagej.model.Dataset;
import imagej.process.Index;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import loci.formats.gui.AWTImageTools;
import mpicbg.imglib.image.Image;

/**
 *
 * @author GBH
 */
public class DisplayController {

	private Dataset dataset;
	private Image<?> image;
	private int[] dims;
	private AxisLabel[] dimLabels;
	private int xIndex, yIndex;
	private int[] pos;
	private ImageDisplayWindow imgWindow;
	private Display display;
	private Object currentPlane;

	public Object getCurrentPlane() {
		return currentPlane;
	}

	public AxisLabel[] getDimLabels() {
		return dimLabels;
	}

	public int[] getDims() {
		return dims;
	}

	public int[] getPos() {
		return pos;
	}

	private String imageName;
	//private NavigableImagePanel imgPanel;

	public DisplayController(Display display) {
		this.display = display;
		this.imgWindow = this.display.getImageDisplayWindow();
		setDataset(this.display.getDataset());
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
		image = dataset.getImage();
		dims = image.getDimensions();
		dimLabels = dataset.getMetadata().getAxes();
		imageName = dataset.getMetadata().getName();
		// extract width and height
		xIndex = yIndex = -1;
		for (int i = 0; i < dimLabels.length; i++) {
			if (dimLabels[i] == AxisLabel.X) {
				xIndex = i;
			}
			if (dimLabels[i] == AxisLabel.Y) {
				yIndex = i;
			}
		}
		if (xIndex < 0) {
			throw new IllegalArgumentException("No X dimension");
		}
		if (yIndex < 0) {
			throw new IllegalArgumentException("No Y dimension");
		}
		imgWindow.setTitle(imageName);
		// display first image plane
		pos = new int[dims.length - 2];
		updatePosition();
	}

	public void updatePosition(int posIndex, int newValue) {
		pos[posIndex] = newValue;
		updatePosition();
	}

	private void updatePosition() {
		final BufferedImage bImage = getImagePlane();
		if (bImage != null) {
			display.getImageCanvas().setImage(bImage);
		}
		// this is ugly... just trying it out.
		//imgPanel.setNavigationImageEnabled(true);
		imgWindow.setLabel(makeLabel(image, dims, dimLabels));
	}

	private String makeLabel(Image<?> image, int[] dims, AxisLabel[] dimLabels) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0, p = -1; i < dims.length; i++) {
			if (AxisLabel.isXY(dimLabels[i])) {
				continue;
			}
			p++;
			if (dims[i] == 1) {
				continue;
			}
			sb.append(dimLabels[i] + ": " + (pos[p] + 1) + "/" + dims[i] + "; ");
		}
		sb.append(dims[xIndex] + "x" + dims[yIndex] + "; ");
		//sb.append(image.getType());
		return sb.toString();

	}

	private BufferedImage getImagePlane() {
		// FIXME - how to get a subset with different axes?
		final int no = Index.positionToRaster(dims, pos);
		final Object plane = dataset.getPlane(no);
		currentPlane = plane;
		if (plane instanceof byte[]) {
			return AWTImageTools.makeImage((byte[]) plane,
					dims[xIndex], dims[yIndex], dataset.isSigned());
		} else if (plane instanceof short[]) {
			return AWTImageTools.makeImage((short[]) plane,
					dims[xIndex], dims[yIndex], dataset.isSigned());
		} else if (plane instanceof int[]) {
			return AWTImageTools.makeImage((int[]) plane,
					dims[xIndex], dims[yIndex], dataset.isSigned());
		} else if (plane instanceof float[]) {
			return AWTImageTools.makeImage((float[]) plane,
					dims[xIndex], dims[yIndex]);
		} else if (plane instanceof double[]) {
			return AWTImageTools.makeImage((double[]) plane,
					dims[xIndex], dims[yIndex]);
		} else {
			throw new IllegalStateException("Unknown data type: "
					+ plane.getClass().getName());
		}
	}

	private static BufferedImage toCompatibleImage(BufferedImage image) {
		if (image.getColorModel().equals(CONFIGURATION.getColorModel())) {
			return image;
		}
		BufferedImage compatibleImage = CONFIGURATION.createCompatibleImage(
				image.getWidth(), image.getHeight(), image.getTransparency());
		Graphics g = compatibleImage.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return compatibleImage;
	}

	private static final GraphicsConfiguration CONFIGURATION =
			GraphicsEnvironment.getLocalGraphicsEnvironment().
			getDefaultScreenDevice().getDefaultConfiguration();
}
