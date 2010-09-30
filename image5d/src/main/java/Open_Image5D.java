/* J. Walter 2005-09-24 
 * Opens a TIFF file as Image5D.
 * Severely copied from ij.io.Opener. Opener is not used or inherited, because 
 * detailed changes to the dialog and behaviour are planned. */

import java.awt.*;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.util.*;

import i5d.Image5D;
import i5d.cal.ChannelCalibration;
import i5d.cal.ChannelDisplayProperties;
import ij.*;
import ij.io.*;
import ij.plugin.*;

public class Open_Image5D implements PlugIn {
    // 0x004c5554 (LUT) for channel lookup tables 
    // Structure: int type (1 for 768 byte RGB LUT)
    //              int channel number (channel, whose LUT this entry is, starting from 1)
    //              768 bytes: LUT for R, G, B in ascending order
public static final int tagLUT = 0x004c5554;
public static final int tagCB = 0x00432642; // "C&B" for contrast and brightness settings
public static final int tagTHR = 0x00544852;
public static final int tagGRA = 0x00475241;
public static final int tagOVL = 0x004f564c;
public static final int tagLBL = 0x004c424c;
public static final int tagCAL = 0x0043414c;



    public void run(String arg) {
        IJ.register(Open_Image5D.class);
        
        if (IJ.versionLessThan("1.35c"))
            return;
        
        // Get filename and path
        OpenDialog od = new OpenDialog("Open Image5D", "");
        String directory = od.getDirectory();
        String name = od.getFileName();
        if (name!=null) {
            
            // TODO: analyze name for -c01z001t001 pattern.
            // Load images in loop according to this pattern.
            
            // Copied from Opener.openTIFF()
            TiffDecoder td = new TiffDecoder(directory, name);
            if (IJ.debugMode) td.enableDebugging();
            FileInfo[] info=null;
            try {info = td.getTiffInfo();}
            catch (IOException e) {
                String msg = e.getMessage();
                if (msg==null||msg.equals("")) msg = ""+e;
                IJ.error("TiffDecoder", msg);
                return;
            }
            if (info==null)
                return;
            
            switch(info[0].fileType) {
            case FileInfo.GRAY8: 
            case FileInfo.COLOR8: 
            case FileInfo.BITMAP:
            case FileInfo.GRAY16_SIGNED: 
            case FileInfo.GRAY16_UNSIGNED:
            case FileInfo.GRAY32_INT: 
            case FileInfo.GRAY32_UNSIGNED: 
            case FileInfo.GRAY32_FLOAT:
                break;
            default: // Unhandled cases, e.g. RGB images
                IJ.error("Unsupported image type.");
                return;
            }
            
            // Copied from Opener.openTIFF2()
            ImagePlus imp = null;
            if (info.length>1) { // try to open as stack
                imp = (new Opener()).openTiffStack(info);
            } else { // Single image or ImageJ-stack 
                FileOpener fo = new FileOpener(info[0]);
                imp = fo.open(false);
            }
 
            if (imp!=null) {                
                // get dimensions:
                String title = imp.getTitle();
                int nChannels = imp.getNChannels();
                int nSlices = imp.getNSlices();
                int nFrames = imp.getNFrames();
                
                // Create Image5D with the loaded image data.
                Image5D i5d = new Image5D(title, imp.getImageStack(), nChannels, nSlices, nFrames);                
            
                // Copy over the calibration (pixel width, height, depth, frame interval).
                i5d.setCalibration(imp.getCalibration().copy());
                boolean[] hasLUT = new boolean[nChannels];    
                
                // Read MetaData, if image is an Image5D
                String description = info[0].description;
                if (description!=null && description.length()>=7 && description.startsWith("ImageJ")) {
    
                    Properties props = new Properties();
                    InputStream is = new ByteArrayInputStream(description.getBytes());
                    try {props.load(is); is.close();}
                    catch (IOException e) {
                        IJ.error("Exception reading properties: "+e.getMessage());
                    }
                                    
                    if(props.getProperty("Image5D",null)!=null) {                                             
                        int[] metaDataTypes = info[0].metaDataTypes;
                        byte[][] metaData = info[0].metaData;              
                        
                        ChannelCalibration[] chCalibration = new ChannelCalibration[nChannels];
                        ChannelDisplayProperties[] chDispProps = new ChannelDisplayProperties[nChannels]; 
                        for (int c=1; c<=nChannels; c++) {
                            chCalibration[c-1] = new ChannelCalibration();
                            chDispProps[c-1] = new ChannelDisplayProperties();
                        }
                        
                        if (metaDataTypes != null) {
                            for(int n=0; n<metaDataTypes.length; ++n) {
                            // Copy Scaling
                            // Copy Calibrations
                            // Fill ChannelDisplayProperties    
                                try {                   
                                    int tag = metaDataTypes[n];
                                    int entryType;
                                    ByteArrayInputStream bs = new ByteArrayInputStream(metaData[n]);
                                    DataInputStream ds = new DataInputStream(bs);
                                    switch(tag) {
                                        case tagLUT:
                                            entryType = ds.readInt();
                                            switch(entryType) {
                                                case 1:
                                                    int channel = ds.readInt();
                                                    if (channel<1 || channel > nChannels)
                                                        break;
                                                    byte[] rLut = new byte[256];
                                                    byte[] gLut = new byte[256];
                                                    byte[] bLut = new byte[256];
                                                    ds.read(rLut);
                                                    ds.read(gLut);
                                                    ds.read(bLut);
                                                    chDispProps[channel-1].setColorModel(new IndexColorModel(8, 256, rLut, gLut, bLut));                                                  
                                                    hasLUT[channel-1] = true;
                                                    break;    
                                            }
                                            break;
                                        case tagCB:
                                            entryType = ds.readInt();
                                            switch(entryType) {
                                                case 1:
                                                    int channel = ds.readInt();
                                                    if (channel<1 || channel > nChannels)
                                                        break;
                                                    chDispProps[channel-1].setMinValue(ds.readDouble());
                                                    chDispProps[channel-1].setMaxValue(ds.readDouble());
                                                    break;    
                                            }                                       
                                            break;
                                        case tagTHR:
                                            entryType = ds.readInt();
                                            switch(entryType) {
                                                case 1:
                                                    int channel = ds.readInt();
                                                    if (channel<1 || channel > nChannels)
                                                        break;
                                                    chDispProps[channel-1].setMinThreshold(ds.readDouble());
                                                    chDispProps[channel-1].setMaxThreshold(ds.readDouble());
                                                    chDispProps[channel-1].setLutUpdateMode(ds.readInt());
                                                    break;    
                                            }                                       
                                            break;
                                        case tagGRA:
                                            entryType = ds.readInt();
                                            switch(entryType) {
                                                case 1:
                                                    int channel = ds.readInt();
                                                    if (channel<1 || channel > nChannels)
                                                        break;
                                                    chDispProps[channel-1].setDisplayedGray(ds.readBoolean());
                                                    break;    
                                            }                                       
                                            break;
                                        case tagOVL:
                                            entryType = ds.readInt();
                                            switch(entryType) {
                                                case 1:
                                                    int channel = ds.readInt();
                                                    if (channel<1 || channel > nChannels)
                                                        break;
                                                    chDispProps[channel-1].setDisplayedInOverlay(ds.readBoolean());
                                                    break;    
                                            }                                       
                                            break;
                                        case tagLBL:
                                            entryType = ds.readInt();
                                            switch(entryType) {
                                                case 1:
                                                    int channel = ds.readInt();
                                                    if (channel<1 || channel > nChannels)
                                                        break;
                                                    byte[] temp = new byte[metaData[n].length-8];
                                                    ds.read(temp);
                                                    chCalibration[channel-1].setLabel(new String(temp));
                                                    break;    
                                            }                                       
                                            break;
                                        case tagCAL:
                                            entryType = ds.readInt();
                                            switch(entryType) {
                                                case 1:
                                                    int channel = ds.readInt();
                                                    if (channel<1 || channel > nChannels)
                                                        break;
                                                    int funct = ds.readInt();
                                                    int num = ds.readInt();
                                                    double[] coeff = new double[num];
                                                    for (int i=0; i<num; i++) {
                                                        coeff[i] = ds.readDouble();
                                                    }
                                                    boolean zeroClip = ds.readBoolean();
                                                    byte[] temp = new byte[metaData[n].length-4*4-num*8-1];
                                                    ds.read(temp);
                                                    chCalibration[channel-1].setFunction(funct, coeff, new String(temp), zeroClip);
                                                    break;    
                                            }                                       
                                            break;
                                    }
                                    bs.close();
                                } catch (IOException e) {
                                    IJ.log("Exception reading metadata entry: "+n+" tag: "+metaDataTypes[n]+"\n"+e.getMessage());                           
                                }
                            }
                                                
                            // write ChannelDisplayProperties to Image5D
                            for (int c=1; c<=nChannels; c++) {
                                i5d.setChannelCalibration(c, chCalibration[c-1]);
                                i5d.setChannelDisplayProperties(c, chDispProps[c-1]);
                                i5d.restoreChannelProperties(c);
                            }   
                        }
                    }
                }
                
                
                // Wenn keine LUTs vorhanden: apply default colormap
                for (int c=0; c<nChannels; ++c) {
                    if (!hasLUT[c]) {
                        i5d.setChannelColorModel(c+1, ChannelDisplayProperties.
                            createModelFromColor(Color.getHSBColor(1f/(float)nChannels*c, 1f, 1f)));
                    }
                }  
                
                // Prune trailing .tif or .tiff from the title of TIFF images
                title = i5d.getTitle();
                int tLength = title.length();
                if (title.substring(tLength-4, tLength).equalsIgnoreCase(".tif"))
                    title = title.substring(0, tLength-4);
                else if (title.substring(tLength-5, tLength).equalsIgnoreCase(".tiff"))
                    title = title.substring(0, tLength-5);
                i5d.setTitle(title);
                
                i5d.setCurrentPosition(0, 0, 0, 0, 0);
                i5d.show();
            } else {
                // error message
            }
        } else {
            //error message
        }
        
    }

 
    
}
