import i5d.Image5D;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Set_Channel_Display implements PlugIn {

    public void run(String arg) {

        ImagePlus imp = WindowManager.getCurrentImage();
        
        if (imp==null) {
            IJ.noImage();
            return;
        }
        if (!(imp instanceof Image5D)) {
            IJ.error("Image is not an Image5D.");
            return;
        }
        
        Image5D i5d = (Image5D)imp;

        int currentChannel = i5d.getCurrentChannel();
        
        GenericDialog gd = new GenericDialog("Set Channel Properties");
        gd.addNumericField("Channel", currentChannel, 0, 5, "");
        gd.addCheckbox("Display_in_Overlay", i5d.isDisplayedInOverlay(currentChannel));
        gd.addCheckbox("Display_Gray", i5d.isDisplayedGray(currentChannel));
        gd.showDialog();
                
        if (gd.wasCanceled()) {
            return;
        }
        
        currentChannel = (int)gd.getNextNumber();
        if (currentChannel<1 || currentChannel>i5d.getNChannels()) {
            IJ.error("Invalid Channel");
            return;
        }
        
        i5d.setDisplayedInOverlay(currentChannel, gd.getNextBoolean());
        i5d.setDisplayedGray(currentChannel, gd.getNextBoolean());      
        
        i5d.updateWindowControls();
        i5d.updateImageAndDraw();
        
    }

}
