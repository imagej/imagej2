package imagej.ij1bridge;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.util.ArrayList;

import ij.ImageStack;
import ij.process.ImageProcessor;
import imagej.Dimensions;
import imagej.dataset.Dataset;
import imagej.process.Index;

// TODO - for performance could use planeRef's size/cache methods rather than querying dataset all the time

/** BridgeStack will take data from a PlanarDataset and point into it so that ij1 can use it as an ImageStack and make changes to it */
public class BridgeStack extends ImageStack
{
	// **** base interface instance variables
	private Dataset dataset;
	private ArrayList<Object> planeRefs;
	private int[] planeDims;
	private ProcessorFactory processorFactory;
	private Object[] planeRefCache;
	
	// *** compatibility instance variables
	private final String outOfRange = "stack index out of range: ";
	private double min = Double.MAX_VALUE;
	private double max;
	private Rectangle roi;
	private ColorModel cm;
	private float[] cTable;

	// ********* constructor ******************************************************
	
	public BridgeStack(Dataset ds, ProcessorFactory procFac)
	{
		this.dataset = ds;
		
		this.processorFactory = procFac;
		
		this.planeRefs = new ArrayList<Object>();
		
		// TODO - relaxing for the moment since MetaData code not in place. do some kind of check later.
		//if (ds.getMetaData().getDirectAccessDimensionCount() != 2)
		//	throw new IllegalArgumentException("can't make a BridgeStack on a dataset unless it is organized by plane");

		int[] dimensions = ds.getDimensions();
		
		int numPlanes = (int) Dimensions.getTotalPlanes(dimensions);
		
		if (numPlanes <= 0)
			throw new IllegalArgumentException("can't make a BridgeStack on a dataset that has 0 planes");
		
		this.planeDims = new int[2];
		this.planeDims[0] = dimensions[0];
		this.planeDims[1] = dimensions[1];
		
		int[] subDimensions = new int[dimensions.length-2];
		for (int i = 0; i < subDimensions.length; i++)
			subDimensions[i] = dimensions[i+2];

		if (subDimensions.length == 0)
		{
			this.planeRefs.add(this.dataset.getData());
		}
		else
		{
			int[] origin = Index.create(dimensions.length-2);
			
			int[] position = Index.create(dimensions.length-2);
			
			while (Index.isValid(position, origin, subDimensions))
			{
				Object planeRef = ds.getSubset(position).getData();
				
				this.planeRefs.add(planeRef);
				
				Index.increment(position, origin, subDimensions);
			}
		}
	}
	
	// ********* private interface ******************************************************

	// NOTE - index in range 0..n-1
	private void insertSlice(int index, String sliceLabel, Object pixels)
	{
		Dataset newSubset = this.dataset.insertNewSubset(index);
		
		newSubset.setData(pixels);
		
		this.planeRefs.add(index, pixels);  // update our cache

		setSliceLabel(sliceLabel, index+1);
	}
	
	// ********* public interface ******************************************************
	
	// TODO - make processors event listeners for addition/deletion of subsets. fixup planePos if needed (or even have proc go away if possible)
	@Override
	public void addSlice(String sliceLabel, Object pixels)
	{
		int end = this.planeRefs.size();
		
		insertSlice(end, sliceLabel, pixels);
	}

	// TODO - make processors event listeners for addition/deletion of subsets. fixup planePos if needed (or even have proc go away if possible)
	@Override
	public void addSlice(String sliceLabel, ImageProcessor ip)
	{
		if ((ip.getWidth() != getWidth()) ||
				(ip.getHeight() != getHeight()))
			throw new IllegalArgumentException("Dimensions do not match");
		
		if (this.planeRefs.size() == 0)  // TODO - note this code will never evaluate to true for imglib datasets as imglib constituted 11-20-10
		{
			this.cm = ip.getColorModel();
			
			this.min = ip.getMin();
			
			this.max = ip.getMax();
		}
		
		addSlice(sliceLabel, ip.getPixels());
	}
	
	// TODO - make processors event listeners for addition/deletion of subsets. fixup planePos if needed (or even have proc go away if possible)
	@Override
	public void addSlice(String sliceLabel, ImageProcessor ip, int n)
	{
		if (n<1 || n>this.planeRefs.size())
			throw new IllegalArgumentException(outOfRange+n);
		
		insertSlice(n-1, sliceLabel, ip.getPixels());
	}

	// TODO - make processors event listeners for addition/deletion of subsets. fixup planePos if needed (or even have proc go away if possible)
	@Override
	public void deleteSlice(int n)
	{
		if (n<1 || n>this.planeRefs.size())
			throw new IllegalArgumentException(outOfRange+n);
		
		this.dataset.removeSubset(n-1);
		
		this.planeRefs.remove(n-1);
		
		this.planeRefCache = null;
	}
	
	// TODO - make processors event listeners for addition/deletion of subsets. fixup planePos if needed (or even have proc go away if possible)
	@Override
	public void deleteLastSlice()
	{
		int numPlanes = this.planeRefs.size();
		
		if (numPlanes > 0)  // TODO - imglib forces this to be true!!! should fail on deleting last plane. address with imglib people.
		{
			int lastPlane = numPlanes - 1;
			
			this.dataset.removeSubset(lastPlane);
			
			this.planeRefs.remove(lastPlane);

			this.planeRefCache = null;
		}
	}

	@Override
	public int getWidth()
	{
		return this.planeDims[0];
    }

	@Override
    public int getHeight()
	{
		return this.planeDims[1];
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
		if (n<1 || n>this.planeRefs.size())
			throw new IllegalArgumentException(outOfRange+n);

		return this.planeRefs.get(n-1);
	}
	
	@Override
	/** Assigns a pixel array to the specified slice,
		were 1<=n<=nslices. */
	public void setPixels(Object pixels, int n)
	{
		if (n<1 || n>this.planeRefs.size())
			throw new IllegalArgumentException(outOfRange+n);

		int[] planePos = Index.getPlanePosition(this.dataset.getDimensions(), n-1);
		
		this.dataset.getSubset(planePos).setData(pixels);
		
		this.planeRefs.set(n-1, pixels);
	}
	
	@Override
	/** Returns the stack as an array of 1D pixel arrays. Note
		that the size of the returned array may be greater than
		the number of slices currently in the stack, with
		unused elements set to null. */
	public Object[] getImageArray()
	{
		if ((this.planeRefCache == null) || (this.planeRefs.size() > this.planeRefCache.length))
		{
			this.planeRefCache = new Object[this.planeRefs.size()];
		}
		
		return this.planeRefs.toArray(this.planeRefCache);
	}
	
	@Override
	/** Returns the number of slices in this stack. */
	public int getSize()
	{
		return this.planeRefs.size();
	}

	@Override
	/** Returns the slice labels as an array of Strings. Returns null
		if the stack is empty.  */
	public String[] getSliceLabels()
	{
		if (this.planeRefs.size() == 0)
			return null;

		// NOTE - we will return a COPY of the labels. Users should access them readonly.
		// TODO - document.
		
		String[] labels = new String[this.planeRefs.size()];
		
		for (int i = 0; i < labels.length; i++)
		{
			int[] planePos = Index.getPlanePosition(this.dataset.getDimensions(), i);
			
			labels[i] = this.dataset.getSubset(planePos).getMetaData().getLabel();
		}
		
		return labels;
	}
	
	@Override
	/** Returns the label of the specified slice, were 1<=n<=nslices.
		Returns null if the slice does not have a label. For DICOM
		and FITS stacks, labels may contain header information. */
	public String getSliceLabel(int n)
	{
		if (n<1 || n>this.planeRefs.size())
			throw new IllegalArgumentException(outOfRange+n);

		int[] planePos = Index.getPlanePosition(this.dataset.getDimensions(), n-1);
		
		return this.dataset.getSubset(planePos).getMetaData().getLabel();
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
		if (n<1 || n>this.planeRefs.size())
			throw new IllegalArgumentException(outOfRange+n);

		int[] planePos = Index.getPlanePosition(this.dataset.getDimensions(), n-1);
		
		this.dataset.getSubset(planePos).getMetaData().setLabel(label);
	}

	@Override
	/** Returns an ImageProcessor for the specified slice,
		where 1<=n<=nslices. Returns null if the stack is empty.
	*/
	public ImageProcessor getProcessor(int n)
	{
		if (n<1 || n>this.planeRefs.size())
			throw new IllegalArgumentException(outOfRange+n);
		
		int[] planePos = Index.getPlanePosition(this.dataset.getDimensions(), n-1);
		
		ImageProcessor ip = processorFactory.makeProcessor(planePos);
		
		// TODO : problem - what if we want one processor per plane and return it over and over. here we are hatching new all the time.
		
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
    	if ((this.planeRefs.size()==3) &&
    			(this.planeRefs.get(0) instanceof byte[]) &&
    			("Red".equals(getSliceLabel(1))))	
			return true;
		
    	return false;
	}
	
	@Override
	/** Returns true if this is a 3-slice HSB stack. */
	public boolean isHSB()
	{
    	if ((this.planeRefs.size()==3) &&
    			("Hue".equals(getSliceLabel(1))))	
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
		int n = (int)Math.round(Math.log(this.planeRefs.size())+1.0);
		
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
