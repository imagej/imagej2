/*
 * LocatedImage.java
 */

package imagedisplay;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

// Used to place images in relation to one another, as with SeeView.

public class LocatedImage {
    
    private double x = 0;
    private double y = 0;
    private double z = 0;
    private BufferedImage image;
    
    /** Creates a new instance of LocatedImage */
    public LocatedImage(BufferedImage image,  double x,  double y) {
        this(image, x, y, 0);
    }
    
    public LocatedImage(BufferedImage image,  double x,  double y,  double z) {
        this.image = image;
        this.x = x;
        this.y=y;
        this.z = z;
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public Point2D getPoint() {
        return new Point2D.Double(x,y);
    }
    
}
