import i5d.Image5D;
import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.measure.*;
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
        
        for (int i=1; i<=depth; i++) {
            currentImage.setSlice(i);
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

        currentImage.setSlice(currentSlice);
        rgbImage.setStack(null, rgbStack);
        rgbImage.setSlice(currentSlice);
        rgbImage.setCalibration(cal);
        
        rgbImage.killRoi();
        rgbImage.show();
    }

}
