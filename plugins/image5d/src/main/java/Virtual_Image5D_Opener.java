import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;

import i5d.I5DVirtualStack;
import i5d.Image5D;
import i5d.util.NumberedStringSorter;
import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.measure.*;
import ij.plugin.*;

/* Joachim Walter 2006-02-16 
 * Heavily uses code from FileOpener and Virtual_Stack_Opener. */
/** Opens a file series as "virtual" Image5D: every time an image is needed, it is read from disk. */

public class Virtual_Image5D_Opener implements PlugIn {
    
    private int n, start, increment;
    private String filter;
    private Calibration cal;

    private int first;
    private int middle;
    private int last;
    private int nFirst;
    private int nMiddle;
    private int nLast;
    private boolean assignColor;
    
    public void run(String arg) {
        // Get filename and directory and sort list of files in directory.
        OpenDialog od = new OpenDialog("Open Sequence of Images Stacks...", arg);
        String directory = od.getDirectory();
        String name = od.getFileName();
        if (name==null)
            return;
//      selectedFilename = name;        
        String[] list = new File(directory).list();
        if (list==null)
            return;
        NumberedStringSorter.sort(list);
        if (IJ.debugMode) IJ.log("Hypervolume_Opener: "+directory+" ("+list.length+" files)");

        // some inits.
        int width=0,height=0;
        I5DVirtualStack stack = null;           
        IJ.register(Hypervolume_Opener.class);
        
        try {
            // Open selected image and show Dialog.
            ImagePlus imp = new Opener().openImage(directory, name);
            if (imp!=null) {
                width = imp.getWidth();
                height = imp.getHeight();
                cal = imp.getCalibration();
            } else { // Selected file is no image. Try opening starting from first file.//           Open first image in filelist and show Dialog.
                for (int i=0; i<list.length; i++) {
                    if (list[i].endsWith(".txt"))
                        continue;
                    imp = new Opener().openImage(directory, list[i]);
                    if (imp!=null) {
                        width = imp.getWidth();
                        height = imp.getHeight();
                        cal = imp.getCalibration();
                        break;
                    }
                }                   
            }
            
            if (imp != null && !showDialog(imp, list)) {
                return;
            }
            
    
            if (width==0) {
                IJ.showMessage("Import Sequence", "This folder does not appear to contain any TIFF,\n"
                + "JPEG, BMP, DICOM, GIF, FITS or PGM files.");
                return;
            }

            // n: number of images. Given in dialog.
            if (n<1)
                n = list.length;
            if (start<1 || start>list.length)
                start = 1;
            if (start+n-1>list.length)
                n = list.length-start+1;
            int filteredImages = n;
            if (filter!=null && (filter.equals("") || filter.equals("*")))
                filter = null;
            if (filter!=null) {
                filteredImages = 0;
                for (int i=start-1; i<list.length; i++) {
                    if (list[i].indexOf(filter)>=0)
                        filteredImages++;
                }
                if (filteredImages==0) {
                    IJ.error("None of the "+n+" files contain\n the string '"+filter+"' in their name.");
                    return;
                }
            }
            if (filteredImages<n)
                n = filteredImages;
                     
            int count = 0;
            int counter = 0;
            imp = null;
            for (int i=start-1; i<list.length; i++) {
               if (list[i].endsWith(".txt"))
                    continue;
                if (filter!=null && (list[i].indexOf(filter)<0))
                    continue;
                if ((counter++%increment)!=0)
                    continue;
                if (stack==null)
                    imp = new Opener().openImage(directory, list[i]);
                if (imp==null) continue;
                if (stack==null) {
                    width = imp.getWidth();
                    height = imp.getHeight();
                    ColorModel cm = imp.getProcessor().getColorModel();
                    stack = new I5DVirtualStack(width, height, cm, directory);
                 }
                count = stack.getSize()+1;
                IJ.showStatus(count+"/"+n);
                IJ.showProgress((double)count/n);
                stack.addSlice(list[i]);
                if (count>=n)
                    break;
            }
        } catch(OutOfMemoryError e) {
            IJ.outOfMemory("Virtual_Image5D_Opener");
            if (stack!=null) stack.trim();
        }
        
        //Sorting       
        int stackSize = stack.getSize();
        
        // Determine type of third dimension.
        last=0;
        boolean[] thirdChoice = {true, true, true};
        thirdChoice[first] = false;
        thirdChoice[middle] = false;
        for (int j=0; j<3; j++) {
            if (thirdChoice[j]) {
                last = j;
                break;
            }
        }
        // Determine size of third dimension.
        double dLast = (double) stackSize / (double) nFirst / (double) nMiddle;
        nLast = (int) dLast;
        if (nLast != dLast) {
            IJ.error("channels*slices*frames!=stackSize");
            return;
        }
        
        // sort: determine sizes of channels, slices, frames
        int nChannels=1, nSlices=1, nFrames=1;
        switch (first) {
            case 0: nChannels = nFirst; break;
            case 1: nSlices = nFirst; break;
            case 2: nFrames = nFirst; break;
        }       
        switch (middle) {
            case 0: nChannels = nMiddle; break;
            case 1: nSlices = nMiddle; break;
            case 2: nFrames = nMiddle; break;
        }       
        switch (last) {
            case 0: nChannels = nLast; break;
            case 1: nSlices = nLast; break;
            case 2: nFrames = nLast; break;
        }
        
        I5DVirtualStack newStack = new I5DVirtualStack(stack.getWidth(), stack.getHeight(), stack.getColorModel(), directory);

        // Sort: loop with channel changing fastest. Read appropriate slice filename from stack and 
        // add to virtual stack.
        int[] index = new int[3];
        for (index[2]=0; index[2]<nFrames; ++index[2]) {
            for (index[1]=0; index[1]<nSlices; ++index[1]) {
                for (index[0]=0; index[0]<nChannels; ++index[0]) {
                    int stackPosition = 1 + index[first] + index[middle]*nFirst + index[last]*nFirst*nMiddle;
                    newStack.addSlice(stack.getSliceLabel(stackPosition));
                }
            }
        }
 
        // Make Image5D from virtual stack.
        Image5D img5d = new Image5D("virtual", newStack, nChannels, nSlices, nFrames);
        
        img5d.setDefaultChannelNames();
        
        if(assignColor) {
            img5d.setDefaultColors();
        }
        
        img5d.setCurrentPosition(0,0,0,0,0);       
        img5d.setCalibration(cal.copy());

        img5d.show();
        

        IJ.showProgress(1.0);
        System.gc();
    }
    
  
    
    boolean showDialog(ImagePlus imp, String[] list) {
        int fileCount = list.length;

        String name = imp.getTitle();
        if (name.length()>4 && 
                (name.substring(name.length()-4, name.length())).equalsIgnoreCase(".tif") ) {
            name = name.substring(0, name.length()-4);
        }
        int i = name.length()-1;
        while (i>1 && name.charAt(i)>='0' && name.charAt(i)<='9') {
            name = name.substring(0, i);
            i--;
        }

        String[] dimensions = new String[] {"ch", "z", "t"};
        first=1;
        middle=0;
        last=2;
        
        Vi5dOpenerDialog gd = new Vi5dOpenerDialog("Sequence Options", imp, list);
        gd.addNumericField("Number of Images: ", fileCount, 0);
        gd.addNumericField("Starting Image: ", 1, 0);
        gd.addNumericField("Increment: ", 1, 0);
        gd.addStringField("File Name Contains: ", name);
        gd.addMessage("10000 x 10000 x 1000 (100.3MB)");

        // Part for sorting the stack to the Image5D dimensions.
        gd.addChoice("3rd dimension", dimensions, dimensions[first]);
        gd.addChoice("4th dimension", dimensions, dimensions[middle]);
        gd.addNumericField("3rd_dimension_size", 1, 0, 8, "");
        gd.addNumericField("4th_dimension_size", 1, 0, 8, "");
        gd.addCheckbox("Assign default color", true);
        
        gd.showDialog();
        if (gd.wasCanceled())
            return false;
        
        // Parameters for reading data
        n = (int)gd.getNextNumber();
        start = (int)gd.getNextNumber();
        increment = (int)gd.getNextNumber();
        if (increment<1)
            increment = 1;
        filter = gd.getNextString();
        
        // Parameters for sorting data
        first = gd.getNextChoiceIndex();
        middle = gd.getNextChoiceIndex();
        nFirst = (int) gd.getNextNumber();
        nMiddle = (int) gd.getNextNumber();
        assignColor = gd.getNextBoolean();
        
        return true;
    }
    
    
    
    
    class Vi5dOpenerDialog extends GenericDialog {
        /**
		 * 
		 */
		private static final long serialVersionUID = -2870244327145783251L;
		ImagePlus imp;
        int fileCount;
        boolean eightBits;
        String saveFilter = "";
        String[] list;
        Choice choice1;
        Choice choice2;
        int nChoices;
        

        public Vi5dOpenerDialog(String title, ImagePlus imp, String[] list) {
            super(title);
            this.imp = imp;
            this.list = list;
            this.fileCount = list.length;
        }

        protected void setup() {
            setStackInfo();
            choice1 = (Choice)choice.elementAt(0);
            choice2 = (Choice)choice.elementAt(1);
            nChoices = choice1.getItemCount();
        }
        
        public void itemStateChanged(ItemEvent e) {
            setStackInfo();
            if (e.getItemSelectable().equals(choice1)) {
                int index = choice1.getSelectedIndex();
                if (index == choice2.getSelectedIndex()) {
                    choice2.select((index+1)%nChoices);
                }
            }
            if (e.getItemSelectable().equals(choice2)) {
                int index = choice2.getSelectedIndex();
                if (index == choice1.getSelectedIndex()) {
                    choice1.select((index+1)%nChoices);
                }
            }
        }
        
        public void textValueChanged(TextEvent e) {
            setStackInfo();
        }

        void setStackInfo() {
            int width = imp.getWidth();
            int height = imp.getHeight();
            int bytesPerPixel = 1;
//            int nSlices = imp.getStackSize();
            int n = getNumber(numberField.elementAt(0));
            int start = getNumber(numberField.elementAt(1));
            int inc = getNumber(numberField.elementAt(2));
            
            if (n<1)
                n = fileCount;
            if (start<1 || start>fileCount)
                start = 1;
            if (start+n-1>fileCount) {
                n = fileCount-start+1;
                //TextField tf = (TextField)numberField.elementAt(0);
                //tf.setText(""+nImages);
            }
            if (inc<1)
                inc = 1;
            TextField tf = (TextField)stringField.elementAt(0);
            String filter = tf.getText();
            // IJ.write(nImages+" "+startingImage);
            if (!filter.equals("") && !filter.equals("*")) {
                int n2 = n;
                n = 0;
                for (int i=start-1; i<start-1+n2; i++)
                    if (list[i].indexOf(filter)>=0) {
                        n++;
                        //IJ.write(n+" "+list[i]);
                    }
                saveFilter = filter;
            }
            switch (imp.getType()) {
                case ImagePlus.GRAY16:
                    bytesPerPixel=2;break;
                case ImagePlus.COLOR_RGB:
                case ImagePlus.GRAY32:
                    bytesPerPixel=4; break;
            }
            width = (int)width;
            height = (int)height;
            int n2 = n/inc;
            if (n2<0)
                n2 = 0;
            double size = (double)(width*height*n2*bytesPerPixel)/(1024*1024);
            ((Label)theLabel).setText(width+" x "+height+" x "+n2+" ("+IJ.d2s(size,1)+"MB)");
//            double size = (double)(width*height*nSlices*n2*bytesPerPixel)/(1024*1024);
//            ((Label)theLabel).setText(width+" x "+height+" x "+nSlices*n2+" ("+IJ.d2s(size,1)+"MB)");
        }

        public int getNumber(Object field) {
            TextField tf = (TextField)field;
            String theText = tf.getText();
            Double d;
            try {d = new Double(theText);}
            catch (NumberFormatException e){
                d = null;
            }
            if (d!=null)
                return (int)d.doubleValue();
            else
                return 0;
          }

    }
}


