package imagej.display;

import java.awt.image.BufferedImage;

/**
 *
 * @author GBH
 */
public interface ImageCanvas {

	/**
	 * <p>Identifies that the image in the panel has changed.</p>
	 * (from extracted from NavigableImagePanel)
	 */
	String IMAGE_CHANGED_PROPERTY = "image";

	/*
	 *  Sets the primary, probably background, image.
	 */
	void setImage(BufferedImage image);

	BufferedImage getImage();

	/*
	 * Forces an repaint when the image dataBuffer is changed
	 */
	void updateImage();

}
