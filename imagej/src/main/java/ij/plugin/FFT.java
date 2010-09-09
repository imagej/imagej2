package ij.plugin;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.ContrastEnhancer;
import ij.measure.Calibration;
import ij.util.Tools;
import java.awt.*;
import java.util.*;

/** 
This class implements the FFT, Inverse FFT and Redisplay Power Spectrum commands 
in the Process/FFT submenu. It is based on Arlo Reeves'  
Pascal implementation of the Fast Hartley Transform from NIH Image 
(http://rsb.info.nih.gov/ij/docs/ImageFFT/). 
The Fast Hartley Transform was restricted by U.S. Patent No. 4,646,256, but was placed 
in the public domain by Stanford University in 1995 and is now freely available.

Version 2008-08-25 inverse transform: mask is always symmetrized
*/
public class FFT implements  PlugIn, Measurements {

    static boolean displayFFT = true;
    public static boolean displayRawPS;
    public static boolean displayFHT;
    public static boolean displayComplex;
    public static String fileName;
    
    private ImagePlus imp;
    private boolean padded;
    private int originalWidth;
    private int originalHeight;
    private int stackSize = 1;
    private int slice = 1;
    private boolean doFFT;

    public void run(String arg) {
        if (arg.equals("options")) {
            showDialog();
            if (doFFT) arg="fft"; else return;
        }
        imp = IJ.getImage();
        if (arg.equals("redisplay"))
            {redisplayPowerSpectrum(); return;}
        if (arg.equals("swap"))
            {swapQuadrants(imp.getStack()); imp.updateAndDraw(); return;}
       if (arg.equals("inverse")) {
            if (imp.getTitle().startsWith("FHT of"))
                {doFHTInverseTransform(); return;}
            if (imp.getStackSize()==2)
                {doComplexInverseTransform(); return;}
        }
        ImageProcessor ip = imp.getProcessor();
        Object obj = imp.getProperty("FHT");
        FHT fht = (obj instanceof FHT)?(FHT)obj:null;
        stackSize = imp.getStackSize();
        boolean inverse;
        if (fht==null && arg.equals("inverse")) {
            IJ.error("FFT", "Frequency domain image required");
            return;
        }
        if (fht!=null) {
            inverse = true;
            imp.killRoi();
        } else {
            if (imp.getRoi()!=null)
                ip = ip.crop();
            fht = newFHT(ip);
            inverse = false;
        }
        if (inverse)
            doInverseTransform(fht);
        else {
            fileName = imp.getTitle();
            doForewardTransform(fht);   
        }    
        IJ.showProgress(1.0);
    }
    
    void doInverseTransform(FHT fht) {
        fht = fht.getCopy();
        doMasking(fht);
        showStatus("Inverse transform");
        fht.inverseTransform();
        if (fht.quadrantSwapNeeded)
            fht.swapQuadrants();
        fht.resetMinAndMax();
        ImageProcessor ip2 = fht;
        if (fht.originalWidth>0) {
            fht.setRoi(0, 0, fht.originalWidth, fht.originalHeight);
            ip2 = fht.crop();
        }
        int bitDepth = fht.originalBitDepth>0?fht.originalBitDepth:imp.getBitDepth();
        switch (bitDepth) {
            case 8: ip2 = ip2.convertToByte(false); break;
            case 16: ip2 = ip2.convertToShort(false); break;
            case 24:
                showStatus("Setting brightness");
                if (fht.rgb==null || ip2==null) {
                    IJ.error("FFT", "Unable to set brightness");
                    return;
                }
                ColorProcessor rgb = (ColorProcessor)fht.rgb.duplicate();
                rgb.setBrightness((FloatProcessor)ip2);
                ip2 = rgb; 
                fht.rgb = null;
                break;
            case 32: break;
        }
        if (bitDepth!=24 && fht.originalColorModel!=null)
            ip2.setColorModel(fht.originalColorModel);
        String title = imp.getTitle();
        if (title.startsWith("FFT of "))
            title = title.substring(7, title.length());
        ImagePlus imp2 = new ImagePlus("Inverse FFT of "+title, ip2);
        if (imp2.getWidth()==imp.getWidth())
            imp2.setCalibration(imp.getCalibration());
        imp2.show();
    }

    void doForewardTransform(FHT fht) {
        showStatus("Foreward transform");
        fht.transform();
        showStatus("Calculating power spectrum");
        ImageProcessor ps = fht.getPowerSpectrum();
        if (!(displayFHT||displayComplex)) displayFFT = true;
        if (displayFFT) {
            ImagePlus imp2 = new ImagePlus("FFT of "+imp.getTitle(), ps);
            imp2.show();
            imp2.setProperty("FHT", fht);
            imp2.setCalibration(imp.getCalibration());
        }
    }
    
    FHT newFHT(ImageProcessor ip) {
        FHT fht;
        if (ip instanceof ColorProcessor) {
            showStatus("Extracting brightness");
            ImageProcessor ip2 = ((ColorProcessor)ip).getBrightness();
            fht = new FHT(pad(ip2));
            fht.rgb = (ColorProcessor)ip.duplicate(); // save so we can later update the brightness
        } else
            fht = new FHT(pad(ip));
        if (padded) {
            fht.originalWidth = originalWidth;
            fht.originalHeight = originalHeight;
        }
        fht.originalBitDepth = imp.getBitDepth();
        fht.originalColorModel = ip.getColorModel();
        return fht;
    }
    
    ImageProcessor pad(ImageProcessor ip) {
        originalWidth = ip.getWidth();
        originalHeight = ip.getHeight();
        int maxN = Math.max(originalWidth, originalHeight);
        int i = 2;
        while(i<maxN) i *= 2;
        if (i==maxN && originalWidth==originalHeight) {
            padded = false;
            return ip;
        }
        maxN = i;
        showStatus("Padding to "+ maxN + "x" + maxN);
        ImageStatistics stats = ImageStatistics.getStatistics(ip, MEAN, null);
        ImageProcessor ip2 = ip.createProcessor(maxN, maxN);
        ip2.setValue(stats.mean);
        ip2.fill();
        ip2.insert(ip, 0, 0);
        padded = true;
        Undo.reset();
        //new ImagePlus("padded", ip2.duplicate()).show();
        return ip2;
    }
    
    void showStatus(String msg) {
        if (stackSize>1)
            IJ.showStatus("FFT: " + slice+"/"+stackSize);
        else
            IJ.showStatus(msg);
    }
    
    void doMasking(FHT ip) {
        if (stackSize>1)
            return;
        float[] fht = (float[])ip.getPixels();
        ImageProcessor mask = imp.getProcessor();
        mask = mask.convertToByte(false);
        if (mask.getWidth()!=ip.getWidth() || mask.getHeight()!=ip.getHeight())
            return;
        ImageStatistics stats = ImageStatistics.getStatistics(mask, MIN_MAX, null);
        if (stats.histogram[0]==0 && stats.histogram[255]==0)
            return;
        boolean passMode = stats.histogram[255]!=0;
        IJ.showStatus("Masking: "+(passMode?"pass":"filter"));
        mask = mask.duplicate();
        if (passMode)
            changeValuesAndSymmetrize(mask, (byte)255, (byte)0); //0-254 become 0
        else
            changeValuesAndSymmetrize(mask, (byte)0, (byte)255); //1-255 become 255
        //long t0=System.currentTimeMillis();
        for (int i=0; i<3; i++)
            smooth(mask);
        //IJ.log("smoothing time:"+(System.currentTimeMillis()-t0));
        if (IJ.debugMode || IJ.altKeyDown())
        	new ImagePlus("mask", mask.duplicate()).show();
        ip.swapQuadrants(mask);
        byte[] maskPixels = (byte[])mask.getPixels();
        for (int i=0; i<fht.length; i++) {
            fht[i] = (float)(fht[i]*(maskPixels[i]&255)/255.0);
        }
        //FloatProcessor fht2 = new FloatProcessor(mask.getWidth(),mask.getHeight(),fht,null);
        //new ImagePlus("fht", fht2.duplicate()).show();
    }

    // Change pixels not equal to v1 to the new value v2.
    // For pixels equal to v1, also the symmetry-equivalent pixel is set to v1
    // Requires a quadratic 8-bit image.
    void changeValuesAndSymmetrize(ImageProcessor ip, byte v1, byte v2) {
        byte[] pixels = (byte[])ip.getPixels();
        int n = ip.getWidth();
        for (int i=0; i<pixels.length; i++) {
            if (pixels[i] == v1) {  //pixel has been edited for pass or filter, set symmetry-equivalent
                if (i%n==0) {       //left edge
                    if (i>0) pixels[n*n-i] = v1;
                } else if (i<n)     //top edge
                    pixels[n-i] = v1;
                else                //no edge
                    pixels[n*(n+1)-i] = v1;
            } else
                pixels[i] = v2;     //reset all other pixel values
        }
    }

    // Smooth an 8-bit square image with periodic boundary conditions
    // by averaging over 3x3 pixels
    // Requires a quadratic 8-bit image.
    static void smooth(ImageProcessor ip) {
        byte[] pixels = (byte[])ip.getPixels();
        byte[] pixels2 = (byte[])pixels.clone();
        int n = ip.getWidth();
        int[] iMinus = new int[n];  //table of previous index modulo n
        int[] iPlus = new int[n];   //table of next index modulo n
        for (int i=0; i<n; i++) {   //creating the tables in advance is faster calculating each time
            iMinus[i] = (i-1+n)%n;
            iPlus[i] = (i+1)%n;
        }
        for (int y=0; y<n; y++) {
            int offset1 = n*iMinus[y];
            int offset2 = n*y;
            int offset3 = n*iPlus[y];
            for (int x=0; x<n; x++) {
                int sum = (pixels2[offset1+iMinus[x]]&255)
                        + (pixels2[offset1+x]&255)
                        + (pixels2[offset1+iPlus[x]]&255)
                        + (pixels2[offset2+iMinus[x]]&255)
                        + (pixels2[offset2+x]&255)
                        + (pixels2[offset2+iPlus[x]]&255)
                        + (pixels2[offset3+iMinus[x]]&255)
                        + (pixels2[offset3+x]&255)
                        + (pixels2[offset3+iPlus[x]]&255);
                pixels[offset2 + x] = (byte)((sum+4)/9);
            }
        }
    }

    void redisplayPowerSpectrum() {
        FHT fht = (FHT)imp.getProperty("FHT");
        if (fht==null)
            {IJ.error("FFT", "Frequency domain image required"); return;}
        ImageProcessor ps = fht.getPowerSpectrum();
        imp.setProcessor(null, ps);
    }
    
    void swapQuadrants(ImageStack stack) {
        FHT fht = new FHT(new FloatProcessor(1, 1));
        for (int i=1; i<=stack.getSize(); i++)
            fht.swapQuadrants(stack.getProcessor(i));
    }

    void showDialog() {
        GenericDialog gd = new GenericDialog("FFT Options");
        gd.setInsets(0, 20, 0);
        gd.addMessage("Display:");
        gd.setInsets(5, 35, 0);
        gd.addCheckbox("FFT Window", displayFFT);
        gd.setInsets(0, 35, 0);
        gd.addCheckbox("Raw Power Spectrum", displayRawPS);
        gd.setInsets(0, 35, 0);
        gd.addCheckbox("Fast Hartley Transform", displayFHT);
        gd.setInsets(0, 35, 0);
        gd.addCheckbox("Complex Fourier Transform", displayComplex);
        gd.setInsets(8, 20, 0);
        gd.addCheckbox("Do Forward Transform", false);
        gd.showDialog();
        if (gd.wasCanceled())
            return;
        displayFFT = gd.getNextBoolean();
        displayRawPS = gd.getNextBoolean();
        displayFHT = gd.getNextBoolean();
        displayComplex = gd.getNextBoolean();
        doFFT = gd.getNextBoolean();
    }
    
    void doFHTInverseTransform() {
        FHT fht = new FHT(imp.getProcessor().duplicate());
        fht.inverseTransform();
        fht.resetMinAndMax();
        String name = WindowManager.getUniqueName(imp.getTitle().substring(7));
        new ImagePlus(name, fht).show();
    }

    void doComplexInverseTransform() {
        ImageStack stack = imp.getStack();
        if (!stack.getSliceLabel(1).equals("Real"))
            return;
        int maxN = imp.getWidth();
        swapQuadrants(stack);
        float[] rein = (float[])stack.getPixels(1);
        float[] imin = (float[])stack.getPixels(2);
        float[] reout= new float[maxN*maxN];
        float[] imout = new float[maxN*maxN];
        c2c2DFFT(rein, imin, maxN, reout, imout);
        ImageStack stack2 = new ImageStack(maxN, maxN);
        swapQuadrants(stack);
        stack2.addSlice("Real", reout);
        stack2.addSlice("Imaginary", imout);
        stack2 = unpad(stack2);
        String name = WindowManager.getUniqueName(imp.getTitle().substring(10));
        ImagePlus imp2 = new ImagePlus(name, stack2);
        imp2.getProcessor().resetMinAndMax();
        imp2.show();
    }
    
    ImageStack unpad(ImageStack stack) {
        Object w = imp.getProperty("FFT width");
        Object h = imp.getProperty("FFT height");
        if (w==null || h==null) return stack;
        int width = (int)Tools.parseDouble((String)w, 0.0);
        int height = (int)Tools.parseDouble((String)h, 0.0);
        if (width==0 || height==0 || (width==stack.getWidth()&&height==stack.getHeight()))
            return stack;
        StackProcessor sp = new StackProcessor(stack, null);
        ImageStack stack2 = sp.crop(0, 0, width, height);
        return stack2;
    }
    
    /** Complex to Complex Inverse Fourier Transform
    *   @author Joachim Wesner
    */
    void c2c2DFFT(float[] rein, float[] imin, int maxN, float[] reout, float[] imout) {
            FHT fht = new FHT(new FloatProcessor(maxN,maxN));
            float[] fhtpixels = (float[])fht.getPixels();
            // Real part of inverse transform
            for (int iy = 0; iy < maxN; iy++)
                  cplxFHT(iy, maxN, rein, imin, false, fhtpixels);
            fht.inverseTransform();
            // Save intermediate result, so we can do a "in-place" transform
            float[] hlp = new float[maxN*maxN];
            System.arraycopy(fhtpixels, 0, hlp, 0, maxN*maxN);
            // Imaginary part of inverse transform
            for (int iy = 0; iy < maxN; iy++)
                  cplxFHT(iy, maxN, rein, imin, true, fhtpixels);
            fht.inverseTransform();
            System.arraycopy(hlp, 0, reout, 0, maxN*maxN);
            System.arraycopy(fhtpixels, 0, imout, 0, maxN*maxN);
      }

    /** Build FHT input for equivalent inverse FFT
    *   @author Joachim Wesner
    */
    void cplxFHT(int row, int maxN, float[] re, float[] im, boolean reim, float[] fht) {
            int base = row*maxN;
            int offs = ((maxN-row)%maxN) * maxN;
            if (!reim) {
                  for (int c=0; c<maxN; c++) {
                        int l =  offs + (maxN-c)%maxN;
                        fht[base+c] = ((re[base+c]+re[l]) - (im[base+c]-im[l]))*0.5f;
                  }
            } else {
                  for (int c=0; c<maxN; c++) {
                        int l = offs + (maxN-c)%maxN;
                        fht[base+c] = ((im[base+c]+im[l]) + (re[base+c]-re[l]))*0.5f;
                  }
            }
      }

}

