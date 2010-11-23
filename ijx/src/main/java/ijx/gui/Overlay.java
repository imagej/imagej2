package ijx.gui;
import ijx.roi.Roi;
import java.awt.*;
import java.util.Vector;
import ij.*;
import ijx.process.ImageProcessor;

/** An Overlay is a list of Rois that can be drawn non-destructively on an Image. */
public class Overlay {
	private Vector list;
    private boolean label;
    
    /** Constructs an empty Overlay. */
    public Overlay() {
    	list = new Vector();
    }
    
    /** Constructs an Overlay and adds the specified Roi. */
    public Overlay(Roi roi) {
    	list = new Vector();
    	list.add(roi);
    }

    /** Adds an Roi to this Overlay. */
    public void add(Roi roi) {
    	list.add(roi);
    }

    /** Adds an Roi to this Overlay. */
    public void addElement(Roi roi) {
    	list.add(roi);
    }

    /** Removes the Roi with the specified index from this Overlay. */
    public void remove(int index) {
    	list.remove(index);
    }
    
    /** Removes the specified Roi from this Overlay. */
    public void remove(Roi roi) {
    	list.remove(roi);
    }

   /** Removes all the Rois in this Overlay. */
    public void clear() {
    	list.clear();
    }

    /** Returns the Roi with the specified index. */
    public Roi get(int index) {
    	return (Roi)list.get(index);
    }
    
    /** Returns the number of Rois in this Overlay. */
    public int size() {
    	return list.size();
    }
    
    /** Returns on array containing the Rois in this Overlay. */
    public Roi[] toArray() {
    	Roi[] array = new Roi[list.size()];
    	return (Roi[])list.toArray(array);
    }
    
    /** Sets the stroke color of all the Rois in this overlay. */
    public void setStrokeColor(Color color) {
		Roi[] rois = toArray();
		for (int i=0; i<rois.length; i++)
			rois[i].setStrokeColor(color);
	}

    /** Sets the fill color of all the Rois in this overlay. */
    public void setFillColor(Color color) {
		Roi[] rois = toArray();
		for (int i=0; i<rois.length; i++)
			rois[i].setFillColor(color);
	}

    /** Draws outlines of the Rois in this Overlay on the specified
    	ImageProcessor using the current color and line width of 'ip'. */
    //public void draw(ImageProcessor ip) {
	//	Roi[] rois = toArray();
	//	for (int i=0; i<rois.length; i++)
	//		rois[i].drawPixels(ip);
	//}
	
	public String toString() {
    	return list.toString();
    }
    
    public void drawLabels(boolean b) {
    	label = b;
    }
    
    public void hide(int index1, int index2) {
    	int n = list.size();
    	if (index1<0 || index2>=n || index2<index1)
    		return;
    	for (int i=index1; i<=index2; i++)
    		get(i).hide();
    }

    public boolean getDrawLabels() {return label;}
    
    public void setVector(Vector v) {list = v;}
        
    public Vector getVector() {return list;}
    
}
