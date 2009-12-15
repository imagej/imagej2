import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.util.*;
import java.lang.Math.*;
import java.awt.image.*;


public class ThreePointCircularROI_ implements PlugIn {

	// G.Landini at bham.ac.uk
	// 28/Dec/2003.
	// This plugin creates a circular ROI defined by 3 points.
	// http://www.faqs.org/faqs/graphics/algorithms-faq/
	// Subject 1.04: How do I generate a circle through three points?

	public void run(String arg) {
		ImagePlus imp = WindowManager.getCurrentImage();
        
		if (imp==null){
			IJ.error("No image!");
			return;
		}
		IJ.run("Select None");
		IJ.showMessage("Select 3 points");
		ImageProcessor ip = imp.getProcessor();
		double [] p1 = new double [2];
		double [] p2 = new double [2];
		double [] p3 = new double [2];
		double [] circ = new double [3];

		getPoint(1,p1,imp);
		getPoint(2,p2,imp);
		IJ.makeLine((int) p1[0],(int)p1[1],(int)p2[0],(int)p2[1]);
		getPoint(3,p3,imp);
		osculating(p1,p2, p3,circ);
		IJ.log("x: "+(int)circ[0]+", y: "+(int)circ[1]+", radius:"+(int)circ[2]);
		if (circ[2]>0)
			IJ.makeOval((int)(circ[0]-circ[2]),(int)(circ[1]-circ[2]),(int)(circ[2]*2), (int)(circ[2]*2));
	}

	void getPoint(int i, double [] p, ImagePlus imp){
		int [] xyzf = new int [4]; //[0]=x, [1]=y, [2]=z, [3]=flags
		double log255=Math.log(255.0);
		ImageProcessor ip = imp.getProcessor();

		IJ.showStatus("Select point ["+i+"]");
		getCursorLoc( xyzf, imp );
		while ((xyzf[3] & 4) !=0){  //trap until right released
			getCursorLoc( xyzf, imp );
		}

		while ((xyzf[3] & 16)==0)  { //wait for left to be pressed
			getCursorLoc( xyzf, imp );
		}
		p[0]=(double)xyzf[0];
		p[1]=(double)xyzf[1];
		IJ.makeRectangle(xyzf[0],xyzf[1],1,1);
		while ((xyzf[3] & 16) !=0){  //trap until left released
				getCursorLoc( xyzf, imp );
		}
	}


	void getCursorLoc(int [] xyzf, ImagePlus imp ) {
		ImageWindow win = (ImageWindow) imp.getWindow();
		ImageCanvas ic = (ImageCanvas) win.getCanvas();
		Point p = ic.getCursorLoc();
		xyzf[0]=p.x;
		xyzf[1]=p.y;
		xyzf[2]=imp.getCurrentSlice()-1;
		xyzf[3]=ic.getModifiers();
	}


	void osculating( double [] pa, double [] pb, double [] pc, double [] centrad){
		// returns 3 double values: the centre (x,y) coordinates & radius
		// of the circle passing through 3 points pa, pb and pc
		double a, b, c, d, e, f, g;

		if ((pa[0]==pb[0] && pb[0]==pc[0]) || (pa[1]==pb[1] && pb[1]==pc[1])){ //colinear coordinates
			centrad[0]=0; //x
			centrad[1]=0; //y
			centrad[2]=-1; //radius
			return;
		}

		a = pb[0] - pa[0];
		b = pb[1] - pa[1];
		c = pc[0] - pa[0];
		d = pc[1] - pa[1];

		e = a*(pa[0] + pb[0]) + b*(pa[1] + pb[1]);
		f = c*(pa[0] + pc[0]) + d*(pa[1] + pc[1]);

		g = 2.0*(a*(pc[1] - pb[1])-b*(pc[0] - pb[0]));
		//  If g is 0 then the three points are colinear and no finite-radius
		//  circle through them exists. Return -1 for the radius. Somehow this does not
		// work as it should (representation of double number?), so it is trapped earlier..

		if (g==0.0){
			centrad[0]=0; //x
			centrad[1]=0; //y
			centrad[2]=-1; //radius
		}
		else //return centre and radius of the circle
			centrad[0] = (d * e - b * f) / g;
			centrad[1] = (a * f - c * e) / g;
			centrad[2] = Math.sqrt(Math.pow((pa[0] - centrad[0]),2) + Math.pow((pa[1] - centrad[1]),2));
	}
}
