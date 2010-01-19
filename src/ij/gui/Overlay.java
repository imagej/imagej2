package ij.gui;
import java.awt.*;
import java.util.Vector;
import ij.*;

public class Overlay {
	private Vector list;
    private boolean label;
    
    public Overlay() {
    	list = new Vector();
    }
    
    public Overlay(Roi roi) {
    	list = new Vector();
    	list.add(roi);
    }

    public void add(Roi roi) {
    	list.add(roi);
    }

    public void addElement(Roi roi) {
    	list.add(roi);
    }

    public void remove(int index) {
    	list.remove(index);
    }
    
    public void remove(Roi roi) {
    	list.remove(roi);
    }

    //public void remove(int x, int y) {
    //	Roi roi = get(x, y);
    //	if (roi!=null) remove(roi);
    //}

    public void clear() {
    	list.clear();
    }

    public Roi get(int i) {
    	return (Roi)list.get(i);
    }
    
    //public synchronized Roi get(int x, int y) {
    // 	for (int i=0; i<list.size(); i++) {
    //		Roi roi = (Roi)list.get(i);
    //		if (roi==null) return null;
    //		Rectangle bounds = roi.getBounds();
    //		if (bounds.x==x && bounds.y==y)
    //			return roi;
    //	}
    //	return null;
    //}

    public int size() {
    	return list.size();
    }
    
    public Roi[] toArray() {
    	return (Roi[])list.toArray();
    }
    
    public String toString() {
    	return list.toString();
    }
    
    public void drawLabels(boolean b) {
    	label = b;
    }

    boolean getDrawLabels() {return label;}
    
    void setVector(Vector v) {list = v;}
        
    Vector getVector() {return list;}

}
