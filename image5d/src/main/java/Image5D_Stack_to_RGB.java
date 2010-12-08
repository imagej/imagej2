import i5d.Image5D;

import ij.*;
import ij.gui.ImageCanvas;
import ij.measure.*;
import ij.plugin.*;
import ij.process.*;

import java.awt.Graphics;

/** Converts the current timeframe of an Image5D to an RGB stack using the current 
 * view settings.
 * @author Joachim Walter
 */
public class Image5D_Stack_to_RGB implements PlugIn {

    public void run(String arg) {
        ImagePlus currentImage = WindowManager.getCurrentImage();
        if (currentImage==null) {
            IJ.noImage();
            return;
        }
        if (!(currentImage instanceof Image5D)) {
            IJ.error("Image is not an Image5D.");
            return;
        }
        
        String title = currentImage.getTitle();
        int width = currentImage.getWidth();
        int height = currentImage.getHeight();
        int depth = currentImage.getNSlices();
        int currentSlice = currentImage.getCurrentSlice();
        Calibration cal = currentImage.getCalibration().copy();
        
        
        currentImage.killRoi();
        
        ImagePlus rgbImage = IJ.createImage(title+"-RGB", "RGB black", width, height, 1);
        ImageStack rgbStack = rgbImage.getStack();
        
        ImageCanvas canvas = currentImage.getCanvas();
        Graphics imageGfx = canvas.getGraphics();
        for (int i=1; i<=depth; i++) {
            currentImage.setSlice(i);

            // HACK: force immediate (synchronous) repaint of image canvas
            if (currentImage instanceof Image5D) {
              ((Image5D) currentImage).updateImageAndDraw();
            }
            currentImage.updateAndDraw();
            canvas.paint(imageGfx);

            currentImage.copy(false);
            
            ImagePlus rgbClip = ImagePlus.getClipboard();
            if (rgbClip.getType()!=ImagePlus.COLOR_RGB)
                new ImageConverter(rgbClip).convertToRGB();
            if (i>1) {
                rgbStack.addSlice(currentImage.getStack().getSliceLabel(i), 
                        rgbClip.getProcessor().getPixels());
            } else {
                rgbStack.setPixels(rgbClip.getProcessor().getPixels(), 1);
                rgbStack.setSliceLabel(currentImage.getStack().getSliceLabel(1), 1);
            }
        }
        imageGfx.dispose();

        currentImage.setSlice(currentSlice);
        rgbImage.setStack(null, rgbStack);
        rgbImage.setSlice(currentSlice);
        rgbImage.setCalibration(cal);
        
        rgbImage.killRoi();
        rgbImage.show();
    }

}
