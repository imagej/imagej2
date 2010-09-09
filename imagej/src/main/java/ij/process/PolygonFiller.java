package ij.process;
import ij.*;
import ij.gui.*;
import java.awt.Rectangle;


/** This class fills polygons using the scan-line filling algorithm 
	described at "http://www.cs.rit.edu/~icss571/filling/". */
public class PolygonFiller {
	int BLACK=0xff000000, WHITE=0xffffffff;
	int edges; // number of edges
	int activeEdges; // number of  active edges

	// the polygon
	int[] x; // x coordinates
	int[] y; // y coordinates
	int n;  // number of coordinates

	// edge table
	double[] ex;	// x coordinates
	int[] ey1;	// upper y coordinates
	int[] ey2;	// lower y coordinates
	double[] eslope;   // inverse slopes (1/m)

	// sorted edge table (indexes into edge table) (currently not used)
	int[] sedge;

	// active edge table (indexes into edge table)
	int[] aedge; 

	/** Constructs a PolygonFiller. */
	public PolygonFiller() {
	 }

	/** Constructs a PolygonFiller using the specified polygon. */
	public PolygonFiller(int[] x, int[] y, int n) {
		setPolygon(x, y, n);
	 }

	/** Specifies the polygon to be filled. */
	public void setPolygon(int[] x, int[] y, int n) {
		this.x = x;
		this.y = y;
		this.n = n;
	}

	 void allocateArrays(int n) {
		if (ex==null || n>ex.length) {
			ex = new double[n];
			ey1 = new int[n];
			ey2 = new int[n];
			sedge = new int[n];
			aedge = new int[n];
			eslope = new double[n];
		}
	}

	/** Generates the edge table. */
	void buildEdgeTable(int[] x, int[] y, int n) {
		int length, iplus1, x1, x2, y1, y2;
		edges = 0;
		for (int i=0; i<n; i++) {
			iplus1 = i==n-1?0:i+1;
			y1 = y[i];	y2 = y[iplus1];
			x1 = x[i];	x2 = x[iplus1];
			if (y1==y2)
				continue; //ignore horizontal lines
			if (y1>y2) { // swap ends
				int tmp = y1;
				y1=y2; y2=tmp;
				tmp=x1;
				x1=x2; x2=tmp;
			}
			double slope = (double)(x2-x1)/(y2-y1);
			ex[edges] = x1 + slope/2.0; 
			ey1[edges] = y1;
			ey2[edges] = y2;
			eslope[edges] = slope;
			edges++;   
		}
		for (int i=0; i<edges; i++)
			sedge[i] = i;
		activeEdges = 0;
		//quickSort(sedge);
	}


	/** Currently not used since searching the entire edge table
		does not seem to take a significant amount of time. */
	void addToSortedTable(int edge) {
		int index = 0;
		while (index<edges && ey1[edges]>ey1[sedge[index]]) {
			index++;
		}
		for (int i=edges-1; i>=index; i--) {
			sedge[i+1] = sedge[i];
			//IJ.log((i+1)+"="+i);
		}
		sedge[index] = edges;
	}

	/** Fills the polygon using the ImageProcessor's current drawing color. */
	public void fill(ImageProcessor ip, Rectangle r) {
		ip.fill(getMask(r.width, r.height));
	}

	/** Returns a byte mask containing a filled version of the polygon. */
	public ImageProcessor getMask(int width, int height) {
		allocateArrays(n);
		buildEdgeTable(x, y, n);
		//printEdges();
		int x1, x2, offset, index;
		ImageProcessor mask = new ByteProcessor(width, height);
		byte[] pixels = (byte[])mask.getPixels();
		for (int y=0; y<height; y++) {
			removeInactiveEdges(y);
			activateEdges(y);
			offset = y*width;
			for (int i=0; i<activeEdges; i+=2) {
				x1 = (int)(ex[aedge[i]]+0.5);
				if (x1<0) x1=0;
				if (x1>width) x1 = width;
				x2 = (int)(ex[aedge[i+1]]+0.5); 
				if (x2<0) x2=0; 
				if (x2>width) x2 = width;
				//IJ.log(y+" "+x1+"  "+x2);
				for (int x=x1; x<x2; x++)
					pixels[offset+x] = -1; // 255 (white)
			}			
			updateXCoordinates();
		}
		return mask;
	}	

	/** Updates the x coordinates in the active edges list and sorts the list if necessary. */
	void updateXCoordinates() {
		int index;
		double x1=-Double.MAX_VALUE, x2;
		boolean sorted = true;
		for (int i=0; i<activeEdges; i++) {
			index = aedge[i];
			x2 = ex[index] + eslope[index];
			ex[index] = x2;
			if (x2<x1) sorted = false;
			x1 = x2;
		}
		if (!sorted) 
			sortActiveEdges();
	}

	/** Sorts the active edges list by x coordinate using a selection sort. */
	void sortActiveEdges() {
		int min, tmp;
		for (int i=0; i<activeEdges; i++) {
			min = i;
			for (int j=i; j<activeEdges; j++)
				if (ex[aedge[j]] <ex[aedge[min]]) min = j;
			tmp=aedge[min];
			aedge[min] = aedge[i]; 
			aedge[i]=tmp;
		}
	}

	/** Removes edges from the active edge table that are no longer needed. */
	void removeInactiveEdges(int y) {
		int i = 0;
		while (i<activeEdges) {
			int index = aedge[i];
			if (y<ey1[index] || y>=ey2[index]) {
				for (int j=i; j<activeEdges-1; j++)
					aedge[j] = aedge[j+1];
				activeEdges--; 
			} else
				i++;		 
		}
	}

	/** Adds edges to the active edge table. */
	void activateEdges(int y) {
		for (int i=0; i<edges; i++) {
			int edge =sedge[i];
			if (y==ey1[edge]) {
				int index = 0;
				while (index<activeEdges && ex[edge]>ex[aedge[index]])
					index++;
				for (int j=activeEdges-1; j>=index; j--) 
					aedge[j+1] = aedge[j];
				aedge[index] = edge;
				activeEdges++;
			}
		}
	}

	/** Display the contents of the edge table*/
	void printEdges() {
		for (int i=0; i<edges; i++) {
			int index = sedge[i];
			IJ.log(i+"	"+ex[index]+"  "+ey1[index]+"  "+ey2[index] + "  " + IJ.d2s(eslope[index],2) );
		}
	}

	/** Display the contents of the active edge table*/
	void printActiveEdges() {
		for (int i=0; i<activeEdges; i++) {
			int index =aedge[i];
			IJ.log(i+"	"+ex[index]+"  "+ey1[index]+"  "+ey2[index] );
		}
	}

	/*
	void quickSort(int[] a) {
		quickSort(a, 0, a.length-1);
	}
	
	void quickSort(int[] a, int from, int to) {
		int i=from, j=to;
		int center = a[(from+to)/2];
		do {
			//while ( i < to && center.compareTo(a[i]) > 0 ) i++;
			while (i<to && ey1[center]>ey1[a[i]]) j--;
			//while ( j > from && center.compareTo(a[j]) < 0 ) j--;
			while (j>from && ey1[center]<ey1[a[j]]) j--;
			if (i < j) {int temp = a[i]; a[i] = a[j]; a[j] = temp;}
			if (i <= j) { i++; j--; }
		} while(i <= j);
		if (from < j) quickSort(a, from, j);
		if (i < to) quickSort(a,  i, to);
	}
	*/

}
