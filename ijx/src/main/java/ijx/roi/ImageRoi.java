package ijx.roi;
import ijx.process.ImageProcessor;
import java.awt.*;
import java.awt.image.*;

	/** An ImageRoi is an Roi that displays an image as an overlay. 
	 * @see ij.IjxImagePlus#setOverlay(ij.gui.Overlay)
	 */
public class ImageRoi extends Roi {
	private Image img;
	private Composite composite;
	private double opacity = 1.0;

	/** Creates a new ImageRoi from a BufferedImage.*/
	public ImageRoi(int x, int y, BufferedImage bi) {
		super(x, y, bi.getWidth(), bi.getHeight());
		img = bi;
		setStrokeColor(Color.black);
	}

	/** Creates a new ImageRoi from a ImageProcessor.*/
	public ImageRoi(int x, int y, ImageProcessor ip) {
		super(x, y, ip.getWidth(), ip.getHeight());
		img = ip.createImage();
		setStrokeColor(Color.black);
	}
		
	public void draw(Graphics g) {
		if (ic==null) return;
		Graphics2D g2d = (Graphics2D)g;						
		double mag = ic.getMagnification();
		int sx2 = ic.screenX(x+width);
		int sy2 = ic.screenY(y+height);
		Composite saveComposite = null;
		if (composite!=null) {
			saveComposite = g2d.getComposite();
			g2d.setComposite(composite);
		}
		g.drawImage(img, ic.screenX(x), ic.screenY(y), sx2, sy2, 0, 0, img.getWidth(null), img.getHeight(null), null);
		if (composite!=null) g2d.setComposite(saveComposite);
 	}
 	 	
	/** Sets the composite mode. */
	public void setComposite(Composite composite) {
		this.composite = composite;
	}
	
	/** Sets the composite mode using the specified opacity (alpha), in the 
	     range 0.0-1.0, where 0.0 is fully transparent and 1.0 is fully opaque. */
	public void setOpacity(double opacity) {
		if (opacity<0.0) opacity = 0.0;
		if (opacity>1.0) opacity = 1.0;
		this.opacity = opacity;
		if (opacity!=1.0)
			composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)opacity);
		else
			composite = null;
	}
	
	/** Returns the current opacity. */
	public double getOpacity() {
		return opacity;
	}

	//public void setImage(IjxImagePlus imp) {
	//	ij.IJ.log("draw "+ic+"  "+img);
	//	super.setImage(imp);
	//	if (imp==null) img = null;
	//}

}
