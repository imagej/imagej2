import i5d.Image5D;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Set_Channel_Labels implements PlugIn {

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
        
        int nChannels = i5d.getNChannels();
        
        GenericDialog gd = new GenericDialog("Set Channel Labels");
        gd.addMessage("Channels");
        for (int c=1; c<=nChannels; c++) {
            gd.addStringField(new Integer(c).toString(), i5d.getChannelCalibration(c).getLabel(), 10);
        }
        gd.showDialog();
                
        if (gd.wasCanceled()) {
            return;
        }

        for (int c=1; c<=nChannels; c++) {
            i5d.getChannelCalibration(c).setLabel(gd.getNextString());
        }

        i5d.updateWindowControls();      
    }

}
