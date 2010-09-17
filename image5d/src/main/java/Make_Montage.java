import java.awt.Color;

import i5d.Image5D;
import i5d.gui.ChannelControl;
import ij.*;
import ij.gui.*;
import ij.measure.Calibration;
import ij.plugin.*;
import ij.process.*;

/** Does Montages of Image5Ds
 * with help of ij.plugin.Make_Montage
 * The Methods
 * <code> 
 * public void doMontage(Image5D i5d, int columns, int rows, double scale, int first, int last, int inc, int borderWidth, boolean labels)
 * </code>
 * and
 * <code> 
 * public void doI5DMontage(Image5D i5d, int columns, int rows, double scale, int first, int last, int inc, int borderWidth, boolean labels)
 * </code>
 * Can be called directly without GUI. To do this, first create a Make_Montage object and set parameters via
 * <code>
 *     public void setDisplayedChannelsOnly(boolean displayedChannelsOnly)
 *     public void setAllTimeFrames(boolean allTimeFrames)
 *     public void setOutputImage5D(boolean outputImage5D) 
 *     public void setDoScaling(boolean doScaling) 
 *  </code>
 * J. Walter 2005-10-03*/

public class Make_Montage implements PlugIn {    
    private static int columns, rows, first, last, inc, borderWidth;
    private static double scale;
    private static boolean label;
    private static int saveID;

    private static boolean bDisplayedChannelsOnly = false;
    private static boolean bAllTimeFrames = false;
    private static boolean bOutputImage5D = false;
    private static boolean bDoScaling = true;
    
    
    public Make_Montage() {
    }
    
    public void run(String arg) {
        IJ.register(Make_Montage.class);       
        ImagePlus imp = WindowManager.getCurrentImage();
        if(imp==null) {
            IJ.noImage(); 
            return; 
        }
        makeMontage(imp);
    }
    
    /** To make montages via a GUI */
    public void makeMontage(ImagePlus imp) {
        //  Check if image is an Image5D.
        if(!(imp instanceof Image5D)) {
            IJ.error("Make Montage", "Image5D required");
                return; 
        }
        
        Image5D i5d = (Image5D)imp;
        
        int nSlices = imp.getStackSize();
        if (columns==0 || imp.getID()!=saveID) {
            columns = (int)Math.sqrt(nSlices);
            rows = columns;
            int n = nSlices - columns*rows;
            if (n>0) columns += (int)Math.ceil((double)n/rows);
            scale = 1.0;
            if (imp.getWidth()*columns>800)
                scale = 0.5;
            if (imp.getWidth()*columns>1600)
                scale = 0.25;
            inc = 1;
            first = 1;
            last = nSlices;
        }
        
        GenericDialog gd = new GenericDialog("Make Montage", IJ.getInstance());
        gd.addNumericField("Columns:", columns, 0);
        gd.addNumericField("Rows:", rows, 0);
        gd.addNumericField("Scale Factor:", scale, 2);
        gd.addNumericField("First Slice:", first, 0);
        gd.addNumericField("Last Slice:", last, 0);
        gd.addNumericField("Increment:", inc, 0);
        gd.addNumericField("Border Width:", borderWidth, 0);
        gd.addCheckbox("Label Slices", label);
        
        gd.addCheckbox("Displayed Channels only", bDisplayedChannelsOnly);
        gd.addCheckbox("All Time Frames", bAllTimeFrames);
        gd.addCheckbox("Output as Image5D", bOutputImage5D);
        gd.addCheckbox("Copy Contrast and Brightness", bDoScaling);
        gd.showDialog();
        if (gd.wasCanceled())
            return;
        columns = (int)gd.getNextNumber();
        rows = (int)gd.getNextNumber();
        scale = gd.getNextNumber();
        first = (int)gd.getNextNumber();
        last = (int)gd.getNextNumber();
        inc = (int)gd.getNextNumber();
        borderWidth = (int)gd.getNextNumber();
        if (borderWidth<0) borderWidth = 0;
        if (first<1) first = 1;
        if (last>nSlices) last = nSlices;
        if (inc<1) inc = 1;
        if (gd.invalidNumber()) {
            IJ.error("Invalid number");
            return;
        }
        label = gd.getNextBoolean();

        setDisplayedChannelsOnly(gd.getNextBoolean());
        setAllTimeFrames(gd.getNextBoolean());
        setOutputImage5D(gd.getNextBoolean());
        setDoScaling(gd.getNextBoolean());
        
        saveID = imp.getID();

        long tstart = System.currentTimeMillis();

        // Do Montage
        if(bOutputImage5D) {
            Image5D resultI5D = doI5DMontage(i5d, columns, rows, scale, first, last, inc, borderWidth, label);
            if (resultI5D != null) 
                resultI5D.show();
        } else {
            ImagePlus resultImage = doMontage(i5d, columns, rows, scale, first, last, inc, borderWidth, label);
            if(resultImage != null)
                resultImage.show();
        }

        long tstop = System.currentTimeMillis();
        IJ.showStatus("Montage: " +IJ.d2s((tstop-tstart)/1000.0,2)+" seconds");
    }
    
    
    public void setDisplayedChannelsOnly(boolean displayedChannelsOnly) {
        bDisplayedChannelsOnly = displayedChannelsOnly;
    }
    public void setAllTimeFrames(boolean allTimeFrames) {
        bAllTimeFrames = allTimeFrames;
    }
    public void setOutputImage5D(boolean outputImage5D) {
        bOutputImage5D = outputImage5D;
    }
    public void setDoScaling(boolean doScaling) {
        bDoScaling = doScaling;
    }
    
    ImagePlus doMontage(Image5D i5d, int columns, int rows, double scale, int first, int last, int inc, int borderWidth, boolean labels) {
        if(!i5d.lock()) return null;   // exit if in use
               
        ImagePlus imp = (ImagePlus)i5d;

        int width = (int)(i5d.getWidth()*scale);
        int height = (int)(i5d.getHeight()*scale);
        int montageImageWidth = width*columns+borderWidth/2;
        int montageImageHeight = height*rows+borderWidth/2;
            
        int currentChannel = i5d.getCurrentChannel();
        int currentSlice = i5d.getCurrentSlice();
        int currentFrame = i5d.getCurrentFrame();                
        
        // Do Projection. 
        // Check, which channels are projected and store channel changes to keep data consistent
        int nMontagedChannels=0;
        int[] montagedChannels = new int[i5d.getNChannels()];
        for(int c=1; c<=i5d.getNChannels(); c++) {
            i5d.storeChannelProperties(c);
            if (bDisplayedChannelsOnly &&
                    ((i5d.getDisplayMode()==ChannelControl.OVERLAY && !i5d.getChannelDisplayProperties(c).isDisplayedInOverlay())
                    ||i5d.getDisplayMode()!=ChannelControl.OVERLAY && c!=currentChannel))
                continue;
            montagedChannels[nMontagedChannels] = c;
            nMontagedChannels++;
        }
        if (bDisplayedChannelsOnly && nMontagedChannels==0) {
            return null;
        }
        
        int startFrame = i5d.getCurrentFrame();
        int nFrames = 1; 
        if(bAllTimeFrames) {
            startFrame = 1;
            nFrames = i5d.getNFrames(); 
        }
        
        // Allocate output arrays
        byte[] reds = new byte[montageImageWidth*montageImageHeight];
        byte[] greens = new byte[montageImageWidth*montageImageHeight];
        byte[] blues = new byte[montageImageWidth*montageImageHeight];
        String newTitle = WindowManager.makeUniqueName(imp.getTitle()+" Montage");
        ImagePlus resultImp = IJ.createImage(newTitle, "rgb black", montageImageWidth, montageImageHeight, nFrames);
        resultImp.setCalibration(imp.getCalibration().copy());

        for (int frame=startFrame; frame<startFrame+nFrames; frame++) {
            for (int destChannel=1; destChannel<=nMontagedChannels; destChannel++) {
                int srcChannel = montagedChannels[destChannel-1];                
                
                // Do montage for each channel separately
                i5d.setCurrentPosition(0, 0, srcChannel-1, currentSlice-1, frame-1);
                ImageStack tmp = i5d.getStack();
                ImagePlus tempImg = new ImagePlus(imp.getTitle()+" Montage", tmp);
//                ImagePlus tempImg = new ImagePlus(imp.getTitle()+" Montage", i5d.getStack());
                ImagePlus montage = makeMontage(tempImg, columns, rows, scale, first, last, inc, borderWidth, label);
                tempImg.flush();
                if(bDoScaling) {
                    montage.getProcessor().setMinAndMax(i5d.getChannelDisplayProperties(srcChannel).getMinValue(), i5d.getChannelDisplayProperties(srcChannel).getMaxValue());
                } else {
                    montage.getProcessor().resetMinAndMax();
                }

                // Sort projections into color channels
                ColorProcessor proc = (ColorProcessor)(new TypeConverter(montage.getProcessor(), bDoScaling)).convertToRGB();
                int[] rgb = new int[3];
                for(int x=0; x<montageImageWidth; x++) {
                    for(int y=0; y<montageImageHeight; y++) {
                        int pos = x+montageImageWidth*y;
                        proc.getPixel(x, y, rgb);
                        
                        int newval = rgb[0]+(0xff&reds[pos]);
                        if(newval < 256) {
                            reds[pos] = (byte)newval;
                        } else {
                            reds[pos] = (byte)0xff;
                        }
                        newval = rgb[1]+(0xff&greens[pos]);
                        if(newval < 256) {
                            greens[pos] = (byte)newval;
                        } else {
                            greens[pos] = (byte)0xff;
                        }
                        newval = rgb[2]+(0xff&blues[pos]);
                        if(newval < 256) {
                            blues[pos] = (byte)newval;
                        } else {
                            blues[pos] = (byte)0xff;
                        }
                    }
                }    
                montage.flush();            
            }
            ColorProcessor cp = new ColorProcessor(montageImageWidth, montageImageHeight);
            cp.setRGB(reds, greens, blues);
            resultImp.setSlice(frame-startFrame+1);
            resultImp.setProcessor(null, cp);
            for(int i=0; i<montageImageWidth*montageImageHeight; i++) {
                reds[i]=0;
                greens[i]=0;
                blues[i]=0;
            }  
        }
        
        i5d.setCurrentPosition(0, 0, currentChannel-1, currentSlice-1, currentFrame-1);
        i5d.unlock(); 
        
        return resultImp;    
    }
    
    
    Image5D doI5DMontage(Image5D i5d, int columns, int rows, double scale, int first, int last, int inc, int borderWidth, boolean labels) {
        if(!i5d.lock()) return null;   // exit if in use
        
        ImagePlus imp = (ImagePlus)i5d;

        int width = (int)(i5d.getWidth()*scale);
        int height = (int)(i5d.getHeight()*scale);
        int montageImageWidth = width*columns+borderWidth/2;
        int montageImageHeight = height*rows+borderWidth/2;
        
        int currentChannel = i5d.getCurrentChannel();
        int currentSlice = i5d.getCurrentSlice();
        int currentFrame = i5d.getCurrentFrame();
        
        // Do Montage. 
        // Check, which channels are montaged and store channel changes to keep data consistent
        int nMontagedChannels=0;
        int[] montagedChannels = new int[i5d.getNChannels()];
        for(int c=1; c<=i5d.getNChannels(); c++) {
            i5d.storeChannelProperties(c);
            if (bDisplayedChannelsOnly &&
                    ((i5d.getDisplayMode()==ChannelControl.OVERLAY && !i5d.getChannelDisplayProperties(c).isDisplayedInOverlay())
                    ||i5d.getDisplayMode()!=ChannelControl.OVERLAY && c!=currentChannel))
                continue;
            montagedChannels[nMontagedChannels] = c;
            nMontagedChannels++;
        }
        if (bDisplayedChannelsOnly && nMontagedChannels==0) {
            return null;
        }
        
        int startFrame = i5d.getCurrentFrame();
        int nFrames = 1; 
        if(bAllTimeFrames) {
            startFrame = 1;
            nFrames = i5d.getNFrames(); 
        }
        
        // Allocate output Image
        String newTitle = WindowManager.makeUniqueName(imp.getTitle()+" Montage");
        Image5D resultI5D = new Image5D(newTitle, i5d.getType(), montageImageWidth, montageImageHeight, nMontagedChannels, 1, nFrames, false);
        resultI5D.setCalibration(i5d.getCalibration().copy());

        
        for (int frame=startFrame; frame<startFrame+nFrames; frame++) {
            for (int destChannel=1; destChannel<=nMontagedChannels; destChannel++) {
                int srcChannel = montagedChannels[destChannel-1];
                
                // Do montage for each channel separately
                i5d.setCurrentPosition(0, 0, srcChannel-1, currentSlice-1, frame-1);
                ImagePlus tempImg = new ImagePlus(imp.getTitle()+" Montage", i5d.getStack());
                ImagePlus montage = makeMontage(tempImg, columns, rows, scale, first, last, inc, borderWidth, label);
                tempImg.flush();
                               
                resultI5D.setPixels(montage.getProcessor().getPixels(), destChannel, 1, frame-startFrame+1);
                montage.flush();
                if (frame==startFrame) {
                    if (destChannel==resultI5D.getCurrentChannel()) {
                        resultI5D.setChannelCalibration(destChannel, i5d.getChannelCalibration(srcChannel).copy());
                        resultI5D.setChannelDisplayProperties(destChannel, i5d.getChannelDisplayProperties(srcChannel).copy());
                        resultI5D.restoreCurrentChannelProperties();
                      } else {
                          resultI5D.setChannelCalibration(destChannel, i5d.getChannelCalibration(srcChannel).copy());
                          resultI5D.setChannelDisplayProperties(destChannel, i5d.getChannelDisplayProperties(srcChannel).copy());
                          resultI5D.restoreChannelProperties(destChannel);   
                    }    
                    if(!bDoScaling) {
                        resultI5D.getProcessor(destChannel).resetMinAndMax();
                    }
                }                
            }            
        }
        
        i5d.setCurrentPosition(0, 0, currentChannel-1, currentSlice-1, currentFrame-1);
        i5d.unlock(); 
        
        return resultI5D;
    }
    
    /** Copied fr0m ij.plugin.MontageMaker, because there the Montage image is not returned but displayed.*/
    public ImagePlus makeMontage(ImagePlus imp, int columns, int rows, double scale, int first, int last, int inc, int borderWidth, boolean labels) {
        int stackWidth = imp.getWidth();
        int stackHeight = imp.getHeight();
        int width = (int)(stackWidth*scale);
        int height = (int)(stackHeight*scale);
        int montageWidth = width*columns;
        int montageHeight = height*rows;
        ImageProcessor ip = imp.getProcessor();
        ImageProcessor montage = ip.createProcessor(montageWidth+borderWidth/2, montageHeight+borderWidth/2);
        ImageStatistics is = imp.getStatistics();
        boolean blackBackground = is.mode<200;
        if (imp.isInvertedLut())
            blackBackground = !blackBackground;
        if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor))
            blackBackground = true;
        if (blackBackground) {
            float[] cTable = imp.getCalibration().getCTable();
            boolean signed16Bit = cTable!=null && cTable[0]==-32768;
            if (signed16Bit)
                montage.setValue(32768);
            else
                montage.setColor(Color.black);
            montage.fill();
            montage.setColor(Color.white);
        } else {
            montage.setColor(Color.white);
            montage.fill();
            montage.setColor(Color.black);
        }
        ImageStack stack = imp.getStack();
        int x = 0;
        int y = 0;
        ImageProcessor aSlice;
        int slice = first;
        while (slice<=last) {
            aSlice = stack.getProcessor(slice);
            if (scale!=1.0)
                aSlice = aSlice.resize(width, height);
            montage.insert(aSlice, x, y);
            String label = stack.getShortSliceLabel(slice);
            if (borderWidth>0) drawBorder(montage, x, y, width, height, borderWidth);
            if (labels) drawLabel(montage, slice, label, x, y, width, height);
            x += width;
            if (x>=montageWidth) {
                x = 0;
                y += height;
                if (y>=montageHeight)
                    break;
            }
            IJ.showProgress((double)(slice-first)/(last-first));
            slice += inc;
        }
        if (borderWidth>0) {
            int w2 = borderWidth/2;
            drawBorder(montage, w2, w2, montageWidth-w2, montageHeight-w2, borderWidth);
        }
        IJ.showProgress(1.0);
        ImagePlus imp2 = new ImagePlus("Montage", montage);
        imp2.setCalibration(imp.getCalibration());
        Calibration cal = imp2.getCalibration();
        if (cal.scaled()) {
            cal.pixelWidth /= scale;
            cal.pixelHeight /= scale;
        }
        return imp2;
    }
    
    void drawBorder(ImageProcessor montage, int x, int y, int width, int height, int borderWidth) {
        montage.setLineWidth(borderWidth);
        montage.moveTo(x, y);
        montage.lineTo(x+width, y);
        montage.lineTo(x+width, y+height);
        montage.lineTo(x, y+height);
        montage.lineTo(x, y);
    }
    
    void drawLabel(ImageProcessor montage, int slice, String label, int x, int y, int width, int height) {
        if (label!=null && !label.equals("") && montage.getStringWidth(label)>=width) {
            do {
                label = label.substring(0, label.length()-1);
            } while (label.length()>1 && montage.getStringWidth(label)>=width);
        }
        if (label==null || label.equals(""))
            label = ""+slice;
        int swidth = montage.getStringWidth(label);
        x += width/2 - swidth/2;
        y += height;
        montage.moveTo(x, y); 
        montage.drawString(label);
    }
    
}
