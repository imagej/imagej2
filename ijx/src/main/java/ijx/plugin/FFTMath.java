package ijx.plugin;
import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.process.FHT;
import ijx.gui.dialog.GenericDialog;
import ijx.WindowManager;
import ijx.IJ;
import ij.*;


import ijx.IjxImagePlus;

/** The class implements the Process/FFT/Math command. */
public class FFTMath implements PlugIn {

    private static final int CONJUGATE_MULTIPLY=0, MULTIPLY=1, DIVIDE=2;
    private static String[] ops = {"Correlate", "Convolve", "Deconvolve"};
    private static int index1;
    private static int index2;
    private static int operation = CONJUGATE_MULTIPLY;
    private static boolean doInverse = true;
    private static String title = "Result";
    private IjxImagePlus imp1, imp2;
            
    public void run(String arg) {
        if (showDialog())
            doMath(imp1, imp2);
    }
    
    public boolean showDialog() {
        int[] wList = WindowManager.getIDList();
        if (wList==null) {
            IJ.noImage();
            return false;
        }
        String[] titles = new String[wList.length];
        for (int i=0; i<wList.length; i++) {
            IjxImagePlus imp = WindowManager.getImage(wList[i]);
            if (imp!=null)
                titles[i] = imp.getTitle();
            else
                titles[i] = "";
        }
        if (index1>=titles.length)index1 = 0;
        if (index2>=titles.length)index2 = 0;
        GenericDialog gd = new GenericDialog("FFT Math");
        gd.addChoice("Image1: ", titles, titles[index1]);
        gd.addChoice("Operation:", ops, ops[operation]);
        gd.addChoice("Image2: ", titles, titles[index2]);
        gd.addStringField("Result:", title);
        gd.addCheckbox("Do Inverse Transform", doInverse);
        gd.showDialog();
        if (gd.wasCanceled())
            return false;
        index1 = gd.getNextChoiceIndex();
        operation = gd.getNextChoiceIndex();
        index2 = gd.getNextChoiceIndex();
        title = gd.getNextString();
        doInverse = gd.getNextBoolean();
        String title1 = titles[index1];
        String title2 = titles[index2];
        imp1 = WindowManager.getImage(wList[index1]);
        imp2 = WindowManager.getImage(wList[index2]);
        return true;
   }
    
    public void doMath(IjxImagePlus imp1, IjxImagePlus imp2) {
    	FHT h1, h2=null;
    	ImageProcessor fht1, fht2;
		fht1  = (ImageProcessor)imp1.getProperty("FHT");
		if (fht1!=null)
			h1 = new FHT(fht1);
		else {
			IJ.showStatus("Converting to float");
       		ImageProcessor ip1 = imp1.getProcessor();
       	 	h1 = new FHT(ip1);
       	}
		fht2  = (ImageProcessor)imp2.getProperty("FHT");
		if (fht2!=null)
			h2 = new FHT(fht2);
		else {
        	ImageProcessor ip2 = imp2.getProcessor();
        	if (imp2!=imp1)
       	 		h2 = new FHT(ip2);
       	}
        if (!h1.powerOf2Size()) {
        	IJ.error("FFT Math", "Images must be a power of 2 size (256x256, 512x512, etc.)");
        	return;
        }
        if (imp1.getWidth()!=imp2.getWidth()) {
        	IJ.error("FFT Math", "Images must be the same size");
        	return;
        }
		if (fht1==null) {
			IJ.showStatus("Transform image1");
			h1.transform();
		}
		if (fht2==null) {
			if (h2==null)
				h2 = new FHT(h1.duplicate());
				else {
					IJ.showStatus("Transform image2");
					h2.transform();
				}
		}
		FHT result=null;
		switch (operation) {
			case CONJUGATE_MULTIPLY: 
				IJ.showStatus("Complex conjugate multiply");
				result = h1.conjugateMultiply(h2); 
				break;
			case MULTIPLY: 
				IJ.showStatus("Fourier domain multiply");
				result = h1.multiply(h2); 
				break;
			case DIVIDE: 
				IJ.showStatus("Fourier domain divide");
				result = h1.divide(h2); 
				break;
		}
		if (doInverse) {
			IJ.showStatus("Inverse transform");
			result.inverseTransform();
			IJ.showStatus("Swap quadrants");
			result.swapQuadrants();
			IJ.showStatus("Display image");
			result.resetMinAndMax();
        	IJ.getFactory().newImagePlus(title, result).show();
		} else {
			IJ.showStatus("Power spectrum");
			ImageProcessor ps = result.getPowerSpectrum();
			IjxImagePlus imp3 = IJ.getFactory().newImagePlus(title, ps.convertToFloat());
			result.quadrantSwapNeeded = true;
			imp3.setProperty("FHT", result);
        	imp3.show();
		}
		IJ.showProgress(1.0);
    }
 
}
