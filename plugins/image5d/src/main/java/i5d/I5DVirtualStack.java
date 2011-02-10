package i5d;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.Opener;
import ij.process.ImageProcessor;

import java.awt.image.ColorModel;

/**
This class represents an array of disk-resident images.
*/
public class I5DVirtualStack extends ImageStack{
    static final int INITIAL_SIZE = 100;
    String path;
    int nSlices;
    String[] names;
    
    /** Creates a new, empty virtual stack. */
    public I5DVirtualStack(int width, int height, ColorModel cm, String path) {
        super(width, height, cm);
        this.path = path;
        names = new String[INITIAL_SIZE];
        //IJ.log("VirtualStack: "+path);
    }

     /** Adds an image to the end of the stack. */
    public void addSlice(String name) {
        if (name==null) 
            throw new IllegalArgumentException("'name' is null!");
        nSlices++;
       //IJ.log("addSlice: "+nSlices+"  "+name);
       if (nSlices==names.length) {
            String[] tmp = new String[nSlices*2];
            System.arraycopy(names, 0, tmp, 0, nSlices);
            names = tmp;
        }
        names[nSlices-1] = name;
    }

   /** Does nothing. */
    public void addSlice(String sliceLabel, Object pixels) {
    }

    /** Does nothing.. */
    public void addSlice(String sliceLabel, ImageProcessor ip) {
    }
    
    /** Does noting. */
    public void addSlice(String sliceLabel, ImageProcessor ip, int n) {
    }

    /** Deletes the specified slice, were 1<=n<=nslices. */
    public void deleteSlice(int n) {
        if (n<1 || n>nSlices)
            throw new IllegalArgumentException("Argument out of range: "+n);
            if (nSlices<1)
                return;
            for (int i=n; i<nSlices; i++)
                names[i-1] = names[i];
            names[nSlices-1] = null;
            nSlices--;
        }
    
    /** Deletes the last slice in the stack. */
    public void deleteLastSlice() {
        if (nSlices>0)
            deleteSlice(nSlices);
    }
       
   /** Returns the pixel array for the specified slice, were 1<=n<=nslices. */
    public Object getPixels(int n) {
        ImageProcessor ip = getProcessor(n);
        if (ip!=null)
            return ip.getPixels();
        else
            return null;
    }       
    
     /** Assigns a pixel array to the specified slice,
        were 1<=n<=nslices. */
    public void setPixels(Object pixels, int n) {
    }

   /** Returns an ImageProcessor for the specified slice,
        were 1<=n<=nslices. Returns null if the stack is empty.
    */
    public ImageProcessor getProcessor(int n) {
        //IJ.log("getProcessor: "+n+"  "+names[n-1]);
        ImagePlus imp = new Opener().openImage(path, names[n-1]);
        if (imp!=null && this.getColorModel()!=null) {
            imp.getProcessor().setColorModel(this.getColorModel());
        } else
            return null;
        return imp.getProcessor();
     }

    /** Returns the directory of the stack. */
       public String getPath() {
           return path;
       }
   
     /** Returns the number of slices in this stack. */
    public int getSize() {
        return nSlices;
    }

    /** Returns the file name of the Nth image. */
    public String getSliceLabel(int n) {
         return names[n-1];
    }
    
    /** Returns null. */
    public Object[] getImageArray() {
        return null;
    }

   /** Does nothing. */
    public void setSliceLabel(String label, int n) {
    }

    /** Always return true. */
    public boolean isVirtual() {
        return true;
    }

   /** Does nothing. */
    public void trim() {
    }
        
}
