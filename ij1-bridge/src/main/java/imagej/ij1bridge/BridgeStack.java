package imagej.ij1bridge;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.util.ArrayList;

import ij.ImageStack;
import ij.process.ImageProcessor;

// TODO - for performance could use planeRef's size/cache methods rather than querying dataset all the time

/** BridgeStack will take data from a PlanarDataset and point into it so that ij1 can use it as an ImageStack and make changes to it */
public class BridgeStack extends ImageStack
{
	// **** base interface instance variables
	private PlanarDataset dataset;
	private ArrayList<Object> planeRefs;

	// *** compatibility instance variables
	private final String outOfRange = "stack index out of range: ";
	private double min = Double.MAX_VALUE;
	private double max;
	private Rectangle roi;
	private ColorModel cm;
	private float[] cTable;

	// ********* constructor ******************************************************
	
	public BridgeStack(PlanarDataset ds)
	{
		this.dataset = ds;
		
		this.planeRefs = new ArrayList<Object>();
		
		int numPlanes = ds.getPlaneCount();
		
		for (int i = 0; i < numPlanes; i++)
		{
			Object planeRef = ds.getPrimitiveArray(i);
			
			this.planeRefs.add(planeRef);
		}
	}
	
	// ********* private interface ******************************************************

	private void insertSlice(int index, String sliceLabel, Object pixels)
	{
		this.dataset.insertPlaneAt(index, pixels);  // TODO - make sure this creates a new plane with default label
		
		this.dataset.setPlaneLabel(index, sliceLabel);  // update the label
		
		this.planeRefs.add(index, pixels);  // update our cache
	}
	
	// ********* public interface ******************************************************
	
	@Override
	public void addSlice(String sliceLabel, Object pixels)
	{
		int end = this.dataset.getPlaneCount();
		
		insertSlice(end, sliceLabel, pixels);
	}

	@Override
	public void addSlice(String sliceLabel, ImageProcessor ip)
	{
		if ((ip.getWidth() != this.dataset.getPlaneWidth()) ||
				(ip.getHeight()!=this.dataset.getPlaneHeight()))
			throw new IllegalArgumentException("Dimensions do not match");
		
		if (this.dataset.getPlaneCount() == 0)  // TODO - note this code will never evaluate to true for imglib datasets as imglib constituted 11-20-10
		{
			this.cm = ip.getColorModel();
			
			this.min = ip.getMin();
			
			this.max = ip.getMax();
		}
		
		addSlice(sliceLabel, ip.getPixels());
	}
	
	@Override
	public void addSlice(String sliceLabel, ImageProcessor ip, int n)
	{
		if (n<1 || n>this.dataset.getPlaneCount())
			throw new IllegalArgumentException(outOfRange+n);
		
		insertSlice(n, sliceLabel, ip);
	}

	@Override
	public void deleteSlice(int n)
	{
		if (n<1 || n>this.dataset.getPlaneCount())
			throw new IllegalArgumentException(outOfRange+n);
		
		this.dataset.deletePlaneAt(n-1);
		
		this.planeRefs.remove(n-1);
	}
	
	@Override
	public void deleteLastSlice()
	{
		int numPlanes = this.dataset.getPlaneCount();
		
		if (numPlanes > 0)  // TODO - imglib forces this to be true!!! should fail on deleting last plane. address with imglib people.
		{
			int lastPlane = numPlanes - 1;
			
			this.dataset.deletePlaneAt(lastPlane);
			
			this.planeRefs.remove(lastPlane);
		}
	}

	@Override
	public int getWidth()
	{
		return this.dataset.getPlaneWidth();
    }

	@Override
    public int getHeight()
	{
		return this.dataset.getPlaneHeight();
    }
    
	@Override
	public void setRoi(Rectangle roi)
	{
		this.roi = roi;
	}
	
	@Override
	public Rectangle getRoi()
	{
		if (this.roi==null)
			return new Rectangle(0, 0, getWidth(), getHeight());
		
		return this.roi;
	}
	
	@Override
	/** Updates this stack so its attributes, such as min, max,
		calibration table and color model, are the same as 'ip'. */
	public void update(ImageProcessor ip)
	{
		if (ip!=null)
		{
			this.min = ip.getMin();
			
			this.max = ip.getMax();
			
			this.cTable = ip.getCalibrationTable();
			
			this.cm = ip.getColorModel();
		}
	}
	
	@Override
	/** Returns the pixel array for the specified slice, were 1<=n<=nslices. */
	public Object getPixels(int n)
	{
		if (n<1 || n>this.dataset.getPlaneCount())
			throw new IllegalArgumentException(outOfRange+n);

		return this.planeRefs.get(n-1);
	}
	
	@Override
	/** Assigns a pixel array to the specified slice,
		were 1<=n<=nslices. */
	public void setPixels(Object pixels, int n)
	{
		if (n<1 || n>this.dataset.getPlaneCount())
			throw new IllegalArgumentException(outOfRange+n);

		this.dataset.setPrimitiveArray(n-1, pixels);
		
		this.planeRefs.set(n-1, pixels);
	}
	
	@Override
	/** Returns the stack as an array of 1D pixel arrays. Note
		that the size of the returned array may be greater than
		the number of slices currently in the stack, with
		unused elements set to null. */
	public Object[] getImageArray()
	{
		return this.planeRefs.toArray();
	}
	
	@Override
	/** Returns the number of slices in this stack. */
	public int getSize()
	{
		return this.dataset.getPlaneCount();
	}

	@Override
	/** Returns the slice labels as an array of Strings. Returns null
		if the stack is empty.  */
	public String[] getSliceLabels()
	{
		if (this.dataset.getPlaneCount() == 0)
			return null;

		// NOTE - we will return a COPY of the labels. Users should access them readonly.
		// TODO - document.
		
		String[] labels = new String[this.dataset.getPlaneCount()];
		
		for (int i = 0; i < labels.length; i++)
			labels[i] = this.dataset.getPlaneLabel(i);
		
		return labels;
	}
	
	@Override
	/** Returns the label of the specified slice, were 1<=n<=nslices.
		Returns null if the slice does not have a label. For DICOM
		and FITS stacks, labels may contain header information. */
	public String getSliceLabel(int n)
	{
		if (n<1 || n>this.dataset.getPlaneCount())
			throw new IllegalArgumentException(outOfRange+n);

		return this.dataset.getPlaneLabel(n-1);
	}
	
	@Override
	/** Returns a shortened version (up to the first 60 characters or first newline and 
		suffix removed) of the label of the specified slice.
		Returns null if the slice does not have a label. */
	public String getShortSliceLabel(int n)
	{
		String shortLabel = getSliceLabel(n);
		
		if (shortLabel == null)
			return null;
		
    	int newline = shortLabel.indexOf('\n');
    	
    	if (newline == 0)
    		return null;
    	
    	if (newline > 0)
    		shortLabel = shortLabel.substring(0, newline);
    	
    	int len = shortLabel.length();
    	
		if ((len>4) &&
				(shortLabel.charAt(len-4) == '.') &&
				(!Character.isDigit(shortLabel.charAt(len-1))))
			shortLabel = shortLabel.substring(0,len-4);
		
		if (shortLabel.length() > 60)
			shortLabel = shortLabel.substring(0, 60);
		
		return shortLabel;
	}

	@Override
	/** Sets the label of the specified slice, were 1<=n<=nslices. */
	public void setSliceLabel(String label, int n)
	{
		if (n<1 || n>this.dataset.getPlaneCount())
			throw new IllegalArgumentException(outOfRange+n);
		
		this.dataset.setPlaneLabel(n-1, label);
	}
	
	@Override
	/** Returns an ImageProcessor for the specified slice,
		were 1<=n<=nslices. Returns null if the stack is empty.
	*/
	public ImageProcessor getProcessor(int n)
	{
		if (n<1 || n>this.dataset.getPlaneCount())
			throw new IllegalArgumentException(outOfRange+n);
		
		if (this.dataset.getPrimitiveArray(n-1) == null)                // TODO - impossible????
			throw new IllegalArgumentException("Pixel array is null");

		ImageProcessor ip = null;
		/*
		ImageProcessor ip;  problem - what if we want one processor per plane and return it over and over. here we are hatching new all the time.
		switch (this.dataset.getSampleInfo().getUserType())
		{
		case BIT:
		case BYTE:
		case UBYTE:
		case UINT12:
		case SHORT:
		case USHORT:
		case INT:
		case UINT:
		case FLOAT:
		case LONG:
		case DOUBLE:
		default:
			throw new IllegalStateException("Unknown sample type "+this.dataset.getSampleInfo().getUserType());	
		}
		*/
		
		//ImageProcessor ip = this.dataset.getProcessor(n-1);
		
		if ((this.min != Double.MAX_VALUE) && (ip!=null))
			ip.setMinAndMax(this.min, this.max);
		
		if (this.cTable!=null)
			ip.setCalibrationTable(this.cTable);
		
		return ip;
	}
	
	@Override
	/** Assigns a new color model to this stack. */
	public void setColorModel(ColorModel cm)
	{
		this.cm = cm;
	}
	
	@Override
	/** Returns this stack's color model. May return null. */
	public ColorModel getColorModel()
	{
		return this.cm;
	}
	
	@Override
	/** Returns true if this is a 3-slice RGB stack. */
	public boolean isRGB()
	{
    	if ((this.dataset.getPlaneCount()==3) &&
    			(this.dataset.getPrimitiveArray(0) instanceof byte[]) &&
    			(getSliceLabel(1)!=null) &&
    			(getSliceLabel(1).equals("Red")))	
			return true;
		
    	return false;
	}
	
	@Override
	/** Returns true if this is a 3-slice HSB stack. */
	public boolean isHSB()
	{
    	if ((this.dataset.getPlaneCount()==3) &&
    			(getSliceLabel(1)!=null) &&
    			(getSliceLabel(1).equals("Hue")))	
			return true;
    	
		return false;
	}

	@Override
	/** Returns true if this is a virtual (disk resident) stack. 
		This method is overridden by the VirtualStack subclass. */
	public boolean isVirtual()
	{
		return false;  // TODO - assuming this means I am not a VirtualStack class. If it means something else then we'll need to query imglib
	}

	@Override
	/** Frees memory by deleting a few slices from the end of the stack. */
	public void trim()
	{
		int n = (int)Math.round(Math.log(this.dataset.getPlaneCount())+1.0);
		
		for (int i=0; i<n; i++)
		{
			deleteLastSlice();
			
			System.gc();
		}
	}

	@Override
	public String toString()
	{
		String v = isVirtual()?"(V)":"";
		
		return ("stack["+getWidth()+"x"+getHeight()+"x"+getSize()+v+"]");
	}
}
