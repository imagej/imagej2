/*
 * MagnifyImageFilter.java
 *
 * Created on November 15, 2006, 11:46 AM
 */
package imagedisplay;

import java.awt.image.ImageConsumer;
import java.awt.image.ColorModel;
import java.util.Hashtable;
import java.awt.Rectangle;
import java.awt.image.ImageFilter;

/**
 * An ImageFilter class for extracting a sub-image from an image (like cropping).
 */
public class MagnifyImageFilter extends ImageFilter {

    int cropX;
    int cropY;
    int cropW;
    int cropH;

    /**
     * Constructs a CropImageFilter that extracts the absolute rectangular
     * region of pixels from its source Image as specified by the x, y,
     * w, and h parameters.
     * @param x the x location of the top of the rectangle to be extracted
     * @param y the y location of the top of the rectangle to be extracted
     * @param w the width of the rectangle to be extracted
     * @param h the height of the rectangle to be extracted
     */
    public MagnifyImageFilter(int x, int y, int w, int h) {
        cropX = x;
        cropY = y;
        cropW = w;
        cropH = h;
    }

    /**
     * Passes along  the properties from the source object after adding a
     * property indicating the cropped region.
     * This method invokes <code>super.setProperties</code>,
     * which might result in additional properties being added.
     * <p>
     * Note: This method is intended to be called by the 
     * <code>ImageProducer</code> of the <code>Image</code> whose pixels 
     * are being filtered. Developers using
     * this class to filter pixels from an image should avoid calling
     * this method directly since that operation could interfere
     * with the filtering operation.
     */
    @Override
    public void setProperties(Hashtable<?, ?> props) {
        Hashtable<Object, Object> p = (Hashtable<Object, Object>) props.clone();
        p.put("croprect", new Rectangle(cropX, cropY, cropW, cropH));
        super.setProperties(p);
    }

    /**
     * Override the source image's dimensions and pass the dimensions
     * of the rectangular cropped region to the ImageConsumer.
     * <p>
     * Note: This method is intended to be called by the 
     * <code>ImageProducer</code> of the <code>Image</code> whose 
     * pixels are being filtered. Developers using
     * this class to filter pixels from an image should avoid calling
     * this method directly since that operation could interfere
     * with the filtering operation.
     * @see ImageConsumer
     */
    @Override
    public void setDimensions(int w, int h) {
        consumer.setDimensions(cropW, cropH);
    }

    /**
     * Determine whether the delivered byte pixels intersect the region to
     * be extracted and passes through only that subset of pixels that
     * appear in the output region.
     * <p>
     * Note: This method is intended to be called by the 
     * <code>ImageProducer</code> of the <code>Image</code> whose 
     * pixels are being filtered. Developers using
     * this class to filter pixels from an image should avoid calling
     * this method directly since that operation could interfere
     * with the filtering operation.
     */
    @Override
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, byte pixels[], int off,
                          int scansize) {


        byte croppedPixels[] = new byte[cropW * cropH];
        for (int i = 0; i < cropH; i++) {
            int voff = cropW * i;
            int voffS = scansize * (cropY + i);
            for (int j = 0; j < cropW; j++) {
                croppedPixels[voff + j] = pixels[voffS + cropX + j];
            }

        }

        int x1 = x;
        if (x1 < cropX) {
            x1 = cropX;
        }
        int x2 = addWithoutOverflow(x, w);
        if (x2 > cropX + cropW) {
            x2 = cropX + cropW;
        }
        int y1 = y;
        if (y1 < cropY) {
            y1 = cropY;
        }

        int y2 = addWithoutOverflow(y, h);
        if (y2 > cropY + cropH) {
            y2 = cropY + cropH;
        }
        if (x1 >= x2 || y1 >= y2) {
            return;
        }
        consumer.setPixels(0, 0, cropW, cropH,
            model, croppedPixels,
            0, cropW);
    }

    /**
     * Determine if the delivered int pixels intersect the region to
     * be extracted and pass through only that subset of pixels that
     * appear in the output region.
     * <p>
     * Note: This method is intended to be called by the 
     * <code>ImageProducer</code> of the <code>Image</code> whose 
     * pixels are being filtered. Developers using
     * this class to filter pixels from an image should avoid calling
     * this method directly since that operation could interfere
     * with the filtering operation.
     */
    @Override
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, int pixels[], int off,
                          int scansize) {
        int x1 = x;
        if (x1 < cropX) {
            x1 = cropX;
        }
        int x2 = addWithoutOverflow(x, w);
        if (x2 > cropX + cropW) {
            x2 = cropX + cropW;
        }
        int y1 = y;
        if (y1 < cropY) {
            y1 = cropY;
        }

        int y2 = addWithoutOverflow(y, h);
        if (y2 > cropY + cropH) {
            y2 = cropY + cropH;
        }
        if (x1 >= x2 || y1 >= y2) {
            return;
        }
        consumer.setPixels(x1 - cropX, y1 - cropY, (x2 - x1), (y2 - y1),
            model, pixels,
            off + (y1 - y) * scansize + (x1 - x), scansize);
    }

    //check for potential overflow (see bug 4801285)	
    private int addWithoutOverflow(int x, int w) {
        int x2 = x + w;
        if (x > 0 && w > 0 && x2 < 0) {
            x2 = Integer.MAX_VALUE;
        } else if (x < 0 && w < 0 && x2 > 0) {
            x2 = Integer.MIN_VALUE;
        }
        return x2;
    }

}
