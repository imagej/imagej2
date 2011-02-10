import i5d.Image5D;
import i5d.cal.ChannelCalibration;
import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.*;
/** Transfers channels colormaps from one Image5D to the current Image5D */
public class Transfer_Channel_Settings implements PlugIn {

    static int choiceID;
    static boolean transferColors = true;
    static boolean transferLabels = true;
    static boolean transferCalibrations = true;
    
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
        IJ.register(Transfer_Channel_Settings.class);
        
        // Get ID list from WindowManager and sort IDs of all Image5Ds in a list.
        int[] idList = WindowManager.getIDList();
        Image5D[] i5dList = new Image5D[idList.length];
        int nI5Ds=0;
        String choiceTitle=null;
        for (int n=0; n<idList.length; n++) {
            if ((WindowManager.getImage(idList[n]) instanceof Image5D) && 
                    idList[n]!=imp.getID()) {
                i5dList[nI5Ds] = (Image5D)WindowManager.getImage(idList[n]);
                if(idList[n]==choiceID)
                    choiceTitle = WindowManager.getImage(idList[n]).getTitle();
                nI5Ds++;
            }
        }
        
        if (nI5Ds<1) {
            IJ.error("No Image5Ds to transfer from.");
            return;
        }
        
        if (choiceTitle==null) {
            choiceTitle=i5dList[0].getTitle();
        }

        String[] i5dTitles = new String[nI5Ds];
        for (int n=0; n<nI5Ds; n++) {
            i5dTitles[n] = i5dList[n].getTitle();
        }
        
        
        GenericDialog gd = new GenericDialog("Transfer Channel Settings");
        gd.addChoice("Transfer_Settings_from", i5dTitles, choiceTitle);
        gd.addCheckbox("ColorMaps", transferColors);
        gd.addCheckbox("Labels", transferLabels);
        gd.addCheckbox("Density_Calibrations", transferCalibrations);
        gd.showDialog();
        
        if(gd.wasCanceled()) {
            return;
        }

        transferColors = gd.getNextBoolean();
        transferLabels = gd.getNextBoolean();
        transferCalibrations = gd.getNextBoolean();
        
        Image5D src = i5dList[gd.getNextChoiceIndex()];
        Image5D dest = (Image5D)imp;
        
        choiceID = src.getID();
        
        int nChannels = Math.min(src.getNChannels(), dest.getNChannels());
        
        src.storeCurrentChannelProperties();
        dest.storeCurrentChannelProperties();
        for (int c=1; c<=nChannels; c++) {
            if (transferColors) {
                dest.setChannelColorModel(c, src.getChannelDisplayProperties(c).getColorModel());
            }
            if (transferLabels) {
                dest.getChannelCalibration(c).setLabel(src.getChannelCalibration(c).getLabel());
            }
            if (transferCalibrations) {
                // Make deep copy of ChannelCalibration to avoid messing up the function of the src.
                ChannelCalibration chCal = src.getChannelCalibration(c).copy();
                dest.getChannelCalibration(c).setFunction(chCal.getFunction(), chCal.getCoefficients(),
                                                            chCal.getValueUnit(), chCal.isZeroClip());
            }
        }
        
        dest.restoreCurrentChannelProperties();
        
        dest.updateAndRepaintWindow();
        dest.updateWindowControls();
    }

}
