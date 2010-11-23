package ijx.process;
import ij.*;

import java.awt.*;
import ij.plugin.filter.*;

/*
Best-fitting ellipse routines by:

  Bob Rodieck
  Department of Ophthalmology, RJ-10
  University of Washington, 
  Seattle, WA, 98195

Notes on best-fitting ellipse:

  Consider some arbitrarily shaped closed profile, which we wish to
  characterize in a quantitative manner by a series of terms, each 
  term providing a better approximation to the shape of the profile.  
  Assume also that we wish to include the orientation of the profile 
  (i.e. which way is up) in our characterization. 

  One approach is to view the profile as formed by a series harmonic 
  components, much in the same way that one can decompose a waveform
  over a fixed interval into a series of Fourier harmonics over that 
  interval. From this perspective the first term is the mean radius,
  or some related value (i.e. the area).  The second term is the 
  magnitude and phase of the first harmonic, which is equivalent to the
  best-fitting ellipse.  

  What constitutes the best-fitting ellipse?  First, it should have the
  same area.  In statistics, the measure that attempts to characterize some
  two-dimensional distribution of data points is the 'ellipse of 
  concentration' (see Cramer, Mathematical Methods of Statistics, 
  Princeton Univ. Press, 945, page 283).  This measure equates the second
  order central moments of the ellipse to those of the distribution, 
  and thereby effectively defines both the shape and size of the ellipse. 

  This technique can be applied to a profile by assuming that it constitutes
  a uniform distribution of points bounded by the perimeter of the profile.
  For most 'blob-like' shapes the area of the ellipse is close to that
  of the profile, differing by no more than about 4%. We can then make
  a small adjustment to the size of the ellipse, so as to give it the 
  same area as that of the profile.  This is what is done here, and 
  therefore this is what we mean by 'best-fitting'. 

  For a real pathologic case, consider a dumbell shape formed by two small
  circles separated by a thin line. Changing the distance between the
  circles alters the second order moments, and thus the size of the ellipse 
  of concentration, without altering the area of the profile. 

public class Ellipse_Fitter implements PlugInFilter {
	public int setup(String arg, IjxImagePlus imp) {
		return DOES_ALL;
	}
	public void run(ImageProcessor ip) {
		EllipseFitter ef = new EllipseFitter();
		ef.fit(ip);
		IJ.write(IJ.d2s(ef.major)+" "+IJ.d2s(ef.minor)+" "+IJ.d2s(ef.angle)+" "+IJ.d2s(ef.xCenter)+" "+IJ.d2s(ef.yCenter));
		ef.drawEllipse(ip);
	}
}
*/


/** This class fits an ellipse to an ROI. */
public class EllipseFitter {

	static final double HALFPI = 1.5707963267949;
	
	/** X centroid */
	public double xCenter;

	/** X centroid */
	public double  yCenter;
	
	/** Length of major axis */
	public double major;
	
	/** Length of minor axis */
	public double minor;
	
	/** Angle in degrees */
	public double angle;
	
	/** Angle in radians */
	public double theta;
	
	/** Initialized by makeRoi() */
	public int[] xCoordinates;
	/** Initialized by makeRoi() */
	public int[] yCoordinates;
	/** Initialized by makeRoi() */
	public int nCoordinates = 0;

	
	private int bitCount;
	private double  xsum, ysum, x2sum, y2sum, xysum;
	private byte[] mask;
	private int left, top, width, height;
 	private double   n;
	private double   xm, ym;   //mean values
	private double   u20, u02, u11;  //central moments
	private ImageProcessor ip;
	//private double pw, ph;
	private boolean record;

	/** Fits an ellipse to the current ROI. The 'stats' argument, currently not used, 
		can be null. The fit parameters are returned in public fields. */
	public void fit(ImageProcessor ip, ImageStatistics stats) {
		this.ip = ip;
		mask = ip.getMaskArray();
		Rectangle r = ip.getRoi();
		//this.pw = stats.pw;
		//this.ph = stats.ph;
		left = r.x;
		top = r.y;
		width = r.width;
		height = r.height;
		getEllipseParam();
	}
	
	void getEllipseParam() {
		double    sqrtPi = 1.772453851;
		double    a11, a12, a22, m4, z, scale, tmp, xoffset, yoffset;
		double    RealAngle;

		if (mask==null) {
			major = (width*2) / sqrtPi;
			minor = (height*2) / sqrtPi; // * Info->PixelAspectRatio;
			angle = 0.0;
			theta = 0.0;
			if (major < minor) {
				tmp = major;
				major = minor;
				minor = tmp;
				angle = 90.0;
				theta = Math.PI/2.0;
			}
			xCenter = left + width / 2.0;
			yCenter = top + height / 2.0;
			return;
		}

		computeSums();
		getMoments();
		m4 = 4.0 * Math.abs(u02 * u20 - u11 * u11);
		if (m4 < 0.000001)
			m4 = 0.000001;
		a11 = u02 / m4;
		a12 = u11 / m4;
		a22 = u20 / m4;
		xoffset = xm;
		yoffset = ym;

		tmp = a11 - a22;
		if (tmp == 0.0)
			tmp = 0.000001;
		theta = 0.5 * Math.atan(2.0 * a12 / tmp);
		if (theta < 0.0)
			theta += HALFPI;
		if (a12 > 0.0)
			theta += HALFPI;
		else if (a12 == 0.0) {
			if (a22 > a11) {
				theta = 0.0;
				tmp = a22;
				a22 = a11;
				a11 = tmp;
			} else if (a11 != a22)
				theta = HALFPI;
		}
		tmp = Math.sin(theta);
		if (tmp == 0.0)
			tmp = 0.000001;
		z = a12 * Math.cos(theta) / tmp;
		major = Math.sqrt (1.0 / Math.abs(a22 + z));
		minor = Math.sqrt (1.0 / Math.abs(a11 - z));
		scale = Math.sqrt (bitCount / (Math.PI * major * minor)); //equalize areas
		major = major*scale*2.0;
		minor = minor*scale*2.0;
		angle = 180.0 * theta / Math.PI;
		if (angle == 180.0)
			angle = 0.0;
 		if (major < minor) {
			tmp = major;
			major = minor;
			minor = tmp;
		}
		xCenter = left + xoffset + 0.5;
		yCenter = top + yoffset + 0.5;
	}

	void computeSums () {
		xsum = 0.0;
		ysum = 0.0;
		x2sum = 0.0;
		y2sum = 0.0;
		xysum = 0.0;
		int bitcountOfLine;
		double   xe, ye;
		int xSumOfLine;
 		for (int y=0; y<height; y++) {
 			bitcountOfLine = 0;
 			xSumOfLine = 0;
			int offset = y*width;
			for (int x=0; x<width; x++) {
				if (mask[offset+x] != 0) {
					bitcountOfLine++;
					xSumOfLine += x;
					x2sum += x * x;
				}
			} 
 			xsum += xSumOfLine;
			ysum += bitcountOfLine * y;
			ye = y;
			xe = xSumOfLine;
			xysum += xe*ye;
			y2sum += ye*ye*bitcountOfLine;
			bitCount += bitcountOfLine;
		}
	}

	void getMoments () {
		double   x1, y1, x2, y2, xy;

		if (bitCount == 0)
			return;

		x2sum += 0.08333333 * bitCount;
		y2sum += 0.08333333 * bitCount;
		n = bitCount;
		x1 = xsum/n;
 		y1 = ysum / n;
		x2 = x2sum / n;
		y2 = y2sum / n;
		xy = xysum / n;
		xm = x1;
		ym = y1;
		u20 = x2 - (x1 * x1);
		u02 = y2 - (y1 * y1);
		u11 = xy - x1 * y1;
	}
	
	/* 
	basic equations:

		a: major axis
		b: minor axis
		t: theta, angle of major axis, clockwise with respect to x axis. 

		g11*x^2 + 2*g12*x*y + g22*y^2 = 1       -- equation of ellipse

		g11:= ([cos(t)]/a)^2 + ([sin(t)]/b)^2
		g12:= (1/a^2 - 1/b^2) * sin(t) * cos(t)
		g22:= ([sin(t)]/a)^2 + ([cos(t)]/b)^2

		solving for x:      x:= k1*y  sqrt( k2*y^2 + k3 )

		where:  k1:= -g12/g11
		k2:= (g12^2 - g11*g22)/g11^2
		k3:= 1/g11

		ymax or ymin occur when there is a single value for x, that is when:    
		k2*y^2 + k3 = 0    
	*/
	
	/** Draws the ellipse on the specified image. */
	public void drawEllipse(ImageProcessor ip) {
		if (major==0.0 && minor==0.0)
			return;
		int xc = (int)Math.round(xCenter);
		int yc = (int)Math.round(yCenter);
		int maxY = ip.getHeight();
		int xmin, xmax;
		double sint, cost, rmajor2, rminor2, g11, g12, g22, k1, k2, k3;
		int x, xsave, ymin, ymax;
		int[] txmin = new int[maxY];
		int[] txmax = new int[maxY];
		double j1, j2, yr;

		sint = Math.sin(theta);
		cost = Math.cos(theta);
		rmajor2 = 1.0 / sqr(major/2);
		rminor2 = 1.0 / sqr(minor/2);
		g11 = rmajor2 * sqr(cost) + rminor2 * sqr(sint);
		g12 = (rmajor2 - rminor2) * sint * cost;
		g22 = rmajor2 * sqr(sint) + rminor2 * sqr(cost);
		k1 = -g12 / g11;
		k2 = (sqr(g12) - g11 * g22) / sqr(g11);
		k3 = 1.0 / g11;
		ymax = (int)Math.floor(Math.sqrt(Math.abs(k3 / k2)));
		if (ymax>maxY)
			ymax = maxY;
		if (ymax<1)
			ymax = 1;
		ymin = -ymax;
 		// Precalculation and use of symmetry speed things up
		for (int y=0; y<=ymax; y++) {
			//GetMinMax(y, aMinMax);
			j2 = Math.sqrt(k2 * sqr(y) + k3);
			j1 = k1 * y;
			txmin[y] = (int)Math.round(j1 - j2);
			txmax[y] = (int)Math.round(j1 + j2);
		}
		if (record) {
			xCoordinates[nCoordinates] = xc + txmin[ymax - 1];
			yCoordinates[nCoordinates] = yc + ymin;
			nCoordinates++;
		} else
			ip.moveTo(xc + txmin[ymax - 1], yc + ymin);
		for (int y=ymin; y<ymax; y++) {
			x = y<0?txmax[-y]:-txmin[y];
			if (record) {
				xCoordinates[nCoordinates] = xc + x;
				yCoordinates[nCoordinates] = yc + y;
				nCoordinates++;
			} else
				ip.lineTo(xc + x, yc + y);
		}
		for (int y=ymax; y>ymin; y--) {
			x = y<0?txmin[-y]:-txmax[y];
			if (record) {
				xCoordinates[nCoordinates] = xc + x;
				yCoordinates[nCoordinates] = yc + y;
				nCoordinates++;
			} else
				ip.lineTo(xc + x, yc + y);
		}
	}
	
	/** Generates the xCoordinates, yCoordinates public arrays 
		that can be used to create an ROI. */
	public void makeRoi(ImageProcessor ip) {
		record = true;
		int size = ip.getHeight()*3;
		xCoordinates = new int[size];
		yCoordinates = new int[size];
		nCoordinates = 0;
		drawEllipse(ip);
		record = false;
	}
	
	private double sqr(double x) {
		return x*x;
	}

}
