package imagej.display;

import imagej.display.ImageCanvas;
import java.awt.image.BufferedImage;

/**
 *
 * @author GBH
 */
public interface ImageDisplayWindow {

	void setDisplayController(DisplayController controller);

	//void addControls(DisplayController controller);

	void setImageCanvas(ImageCanvas canvas);

	ImageCanvas getImageCanvas();

	void setImage(BufferedImage image); //

	void updateImage();

	void setLabel(String s);

	void setTitle(String s);

	void addEventDispatcher(EventDispatcher dispatcher);

}
