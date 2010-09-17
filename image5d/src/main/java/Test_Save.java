import java.io.*;

import ij.*;
import ij.io.*;
import ij.measure.Calibration;
import ij.plugin.*;
import ij.process.ImageProcessor;
/*
 * Created on 21.08.2005
 *
 */

/**
 * For testing ImageJ extensions for saving extra Metadata
 * 
 * @author Joachim Walter
 *
 */
public class Test_Save implements PlugIn {
    ImagePlus imp;
    FileInfo fi;
    String name, directory;
   
    String extraDescriptionEntries="testentry1=dodo\ntestentry2=dudu\n";
    int[] extraMetaDataTypes = {0x004c5554, 0x004c5554}; //\0LUT
    byte[][] extraMetaData = {(new String("jajaja")).getBytes(), (new String("neinnein")).getBytes()};
        
    
    public void run(String args) {

        imp = WindowManager.getCurrentImage();
        fi = imp.getOriginalFileInfo();
        if (fi==null) {
            fi = imp.getFileInfo();
        }
        String path = getPath("TIFF", ".tif");
    	if (path==null)
    		return;
    	Object info = imp.getProperty("Info");
    	if (info!=null && (info instanceof String))
    		fi.info = (String)info;
    	else 
    	    fi.info = "empty info";
    	
    	fi.metaDataTypes = extraMetaDataTypes;
    	fi.metaData = extraMetaData;
    	
    	if (imp.getStackSize()==1)
    		saveAsTiff(path);
    	else
    		saveAsTiffStack(path);       
    }


    
    
//
//From here on: methods copied and slightly modified from ij.io.FileSaver
//
/** Save the image in TIFF format using the specified path. */
public boolean saveAsTiff(String path) {
	fi.nImages = 1;
	fi.description = getDescriptionString();
	try {
		TiffEncoder file = new TiffEncoder(fi);
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
		file.write(out);
		out.close();
	}
	catch (IOException e) {
		showErrorMessage(e);
		return false;
	}
//	updateImp(fi, fi.TIFF);
	return true;
}

/** Save the stack as a multi-image TIFF using the specified path. */
public boolean saveAsTiffStack(String path) {
	if (fi.nImages==1)
		{IJ.error("This is not a stack"); return false;}
	if (fi.pixels==null && imp.getStack().isVirtual())
		{IJ.error("Save As Tiff", "Virtual stacks not supported."); return false;}
	fi.description = getDescriptionString();
	fi.sliceLabels = imp.getStack().getSliceLabels();
	try {
		TiffEncoder file = new TiffEncoder(fi);
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
		file.write(out);
		out.close();
	}
	catch (IOException e) {
		showErrorMessage(e);
		return false;
	}
//	updateImp(fi, fi.TIFF);
	return true;
}
    
void showErrorMessage(IOException e) {
	IJ.error("An error occured writing the file.\n \n" + e);
}

/** Returns a string containing information about the specified  image. */
String getDescriptionString() {
	StringBuffer sb = new StringBuffer(100);
	sb.append("ImageJ="+ImageJ.VERSION+"\n");
	if (fi.nImages>1)
		sb.append("images="+fi.nImages+"\n");
	int channels = imp.getNChannels();
	if (channels>1)
		sb.append("channels="+channels+"\n");
	int slices = imp.getNSlices();
	if (slices>1)
		sb.append("slices="+slices+"\n");
	int frames = imp.getNFrames();
	if (frames>1)
		sb.append("frames="+frames+"\n");
	if (fi.unit!=null)
		sb.append("unit="+fi.unit+"\n");
	if (fi.valueUnit!=null) {
		sb.append("cf="+fi.calibrationFunction+"\n");
		if (fi.coefficients!=null) {
			for (int i=0; i<fi.coefficients.length; i++)
				sb.append("c"+i+"="+fi.coefficients[i]+"\n");
		}
		sb.append("vunit="+fi.valueUnit+"\n");
		Calibration cal = imp.getCalibration();
		if (cal.zeroClip()) sb.append("zeroclip=true\n");
	}
	
	// get stack z-spacing and fps
	if (fi.nImages>1) {
		if (fi.pixelDepth!=0.0 && fi.pixelDepth!=1.0)
			sb.append("spacing="+fi.pixelDepth+"\n");
		if (fi.frameInterval!=0.0) {
			double fps = 1.0/fi.frameInterval;
			if ((int)fps==fps)
				sb.append("fps="+(int)fps+"\n");
			else
				sb.append("fps="+fps+"\n");
		}
	}
	
	// get min and max display values
	ImageProcessor ip = imp.getProcessor();
	double min = ip.getMin();
	double max = ip.getMax();
	int type = imp.getType();
	boolean enhancedLut = (type==ImagePlus.GRAY8 || type==ImagePlus.COLOR_256) && (min!=0.0 || max !=255.0);
	if (enhancedLut || type==ImagePlus.GRAY16 || type==ImagePlus.GRAY32) {
		sb.append("min="+min+"\n");
		sb.append("max="+max+"\n");
	}
	
	// get non-zero origins
	Calibration cal = imp.getCalibration();
	if (cal.xOrigin!=0.0)
		sb.append("xorigin="+cal.xOrigin+"\n");
	if (cal.yOrigin!=0.0)
		sb.append("yorigin="+cal.yOrigin+"\n");
	if (cal.zOrigin!=0.0)
		sb.append("zorigin="+cal.zOrigin+"\n");
	if (cal.info!=null && cal.info.length()<=64 && cal.info.indexOf('=')==-1 && cal.info.indexOf('\n')==-1)
		sb.append("info="+cal.info+"\n");
	
	// add extra Description
	sb.append(extraDescriptionEntries);		//JW
	sb.append((char)0);
	return new String(sb);
}    
String getPath(String type, String extension) {
	name = imp.getTitle();
	SaveDialog sd = new SaveDialog("Save as "+type, name, extension);
	name = sd.getFileName();
	if (name==null)
		return null;
	directory = sd.getDirectory();
	imp.startTiming();
	String path = directory+name;
	return path;
}
   
}
