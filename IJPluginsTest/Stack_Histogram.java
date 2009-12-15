import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import java.awt.*;

/** This plugin generates and displays the histogram of a stack. */
public class Stack_Histogram implements PlugInFilter, Measurements {
    ImagePlus imp;

    public int setup(String arg, ImagePlus imp) {
        if (IJ.versionLessThan("1.32c"))
            return DONE;
        this.imp = imp;
        return DOES_ALL+NO_CHANGES;
    }

    public void run(ImageProcessor ip) {
        new StackHistogramWindow(imp);
    }

}

class StackHistogramWindow extends HistogramWindow {

    StackHistogramWindow(ImagePlus imp) {
        super(imp);
    }
    
    /** Overrides showHistogram in HistogramWindow. */
    public void showHistogram(ImagePlus imp, int bins) {
        showHistogram(imp, bins, 0.0, 0.0);
     }

    /** Overrides showHistogram in HistogramWindow. */
    public void showHistogram(ImagePlus imp, int bins, double histMin, double histMax) {
        setup();
        cal = imp.getCalibration();
        int type = imp.getType();
        boolean fixedRange = !cal.calibrated() && (type==ImagePlus.GRAY8 || type==ImagePlus.COLOR_256 || type==ImagePlus.COLOR_RGB);
        calculateStackHistogram(imp, fixedRange, bins);
        lut = imp.createLut();
        ImageProcessor ip = this.imp.getProcessor();
        boolean color = !(imp.getProcessor() instanceof ColorProcessor) && !lut.isGrayscale();
        if (color)
            ip = ip.convertToRGB();
        drawHistogram(ip, fixedRange);
        if (color)
            this.imp.setProcessor(null, ip);
        else
            this.imp.updateAndDraw();
    }

    public void calculateStackHistogram(ImagePlus imp, boolean fixedRange, int bins) {
        ImageStack stack = imp.getStack();
        int size = stack.getSize();
        Calibration cal = imp.getCalibration();
        ImageProcessor ip = imp.getProcessor();
        byte[] mask = ip.getMaskArray();
        float[] cTable = imp.getCalibration().getCTable();
        stats = imp.getStatistics(AREA+MEAN+MODE+MIN_MAX,bins);
        histogram = new int[stats.nBins];
        double v;
        double sum = 0;
        double sum2 = 0;
        int width, height;
        int rx, ry, rw, rh;
        double pw, ph;
        
        width = ip.getWidth();
        height = ip.getHeight();
        Rectangle roi = ip.getRoi();
        if (roi != null) {
            rx = roi.x;
            ry = roi.y;
            rw = roi.width;
            rh = roi.height;
        } else {
            rx = 0;
            ry = 0;
            rw = width;
            rh = height;
        }
        
        pw = 1.0;
        ph = 1.0;
        stats.roiX = rx*pw;
        stats.roiY = ry*ph;
        stats.roiWidth = rw*pw;
        stats.roiHeight = rh*ph;
        
        // calculate min and max
        double roiMin = Double.MAX_VALUE;
        double roiMax = -Double.MAX_VALUE;
        for (int slice=1; slice<=size; slice++) {
            IJ.showStatus("MinMax: "+slice+"/"+size);
            IJ.showProgress((double)slice/size);
            ip = stack.getProcessor(slice);
            ip.setCalibrationTable(cTable);
            for (int y=ry, my=0; y<(ry+rh); y++, my++) {
                int i = y * width + rx;
                int mi = my * rw;
                for (int x=rx; x<(rx+rw); x++) {
                    if (mask==null || mask[mi++]!=0) {
                        v = ip.getPixelValue(x,y);
                        if (v<roiMin) roiMin = v;
                        if (v>roiMax) roiMax = v;
                    }
                    i++;
                }
            }
         }
         stats.min = roiMin;
         stats.max = roiMax; 

        double histMin, histMax;
        if (fixedRange) {
            histMin = 0;
            histMax = 255;
        } else {
            histMin = stats.min; 
            histMax =  stats.max;
        }
       
        // Generate histogram
        double scale = stats.nBins/( histMax- histMin);
        stats.pixelCount = 0;
        int index;
        for (int slice=1; slice<=size; slice++) {
            IJ.showStatus("Histogram: "+slice+"/"+size);
            IJ.showProgress((double)slice/size);
            ip = stack.getProcessor(slice);
            ip.setCalibrationTable(cTable);
            for (int y=ry, my=0; y<(ry+rh); y++, my++) {
                int i = y * width + rx;
                int mi = my * rw;
                for (int x=rx; x<(rx+rw); x++) {
                    if (mask==null || mask[mi++]!=0) {
                        v = ip.getPixelValue(x,y);
                        stats.pixelCount++;
                        sum += v;
                        sum2 += v*v;
                        index = (int)(scale*(v-histMin));
                        if (index>=nBins)
                            index = nBins-1;
                        histogram[index]++;
                    }
                    i++;
                }
            }
        }
        stats.area = stats.pixelCount*pw*ph;
        stats.mean = sum/stats.pixelCount;
        //IJ.write(sum+" "+stats.pixelCount);
        stats.stdDev = getStdDev(stats.pixelCount, sum, sum2);
        stats.histMin = cal.getRawValue(histMin); 
        stats.histMax =  cal.getRawValue(histMax);
        if (!fixedRange)
            stats.binSize = (stats.histMax-stats.histMin)/stats.nBins;
        //IJ.log(stats.histMin+" "+stats.histMax+" "+stats.nBins);
        stats.dmode = getMode(cal);
        IJ.showStatus("");
        IJ.showProgress(1.0);
    }

   double getMode(Calibration cal) {
        int count;
        stats.maxCount = 0;
        for (int i = 0; i < nBins; i++) {
            count = histogram[i];
            if (count > stats.maxCount) {
                stats.maxCount = count;
                stats.mode = i;
            }
        }
       return cal.getCValue(stats.histMin+stats.mode*stats.binSize);
    }

    double getStdDev(int n, double sum, double sum2) {
        double stdDev;
        if (n>0) {
            stdDev = (n*sum2-sum*sum)/n;
            if (stdDev>0.0)
                stdDev = Math.sqrt(stdDev/(n-1.0));
            else
                stdDev = 0.0;
        }
        else
            stdDev = 0.0;
        return stdDev;
    }
}
