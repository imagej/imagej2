/******************************************************************************\
* 2D Anisotropic Diffusion Tschumperle-Deriche Filtering Plug-in for ImageJ    *
* version 0.3, 2006/02/02                                                      *
* written by Vladimir Pilny (vladimir@pilny.com) and Jiri Janacek                                                      *
* based on CImg Library by David Tschumperle (http://cimg.sourceforge.net/)    *
* GNU GPL2 license                                                             *
* This plug-in is designed to perform Filtering on an 8-bit, 16-bit    *
* and RGB images with support for ROI and Stacks. Long processing can be       *
* stopped with Esc.                                                       *
\******************************************************************************/


/* importing standard Java API Files and ImageJ packages */
import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.awt.*;
import java.io.*;
import java.lang.*;
import ij.gui.*;
import java.util.*; // for time measuring

public class Anisotropic_Diffusion_2D implements PlugInFilter 
{
	static final String ADTDVersion = "0.3";
	static final String ADTDDate = "2006/02/02";

	// the following are the input parameters, with default values assigned to them
	static int nb_iter       = 20;    // Number of iterations
	static int nb_smoothings = 1;     // Number of smoothings per iteration
	static double dt         = 20.0;  // Adapting time step
	static float a1          = 0.5f;  // Diffusion limiter along minimal variations
	static float a2          = 0.9f;  // Diffusion limiter along maximal variations
	static int save          = 20;    // Iteration saving step
	static boolean sstats    = false; // display xdt value in each iteration
	static boolean tstats    = false; // measure needed runtime
	static boolean add_labels    = false; // add labels to output stack
	static float edgeheight  = 5;     // edge threshold
		
	Color label_color;
	Font font;
	int font_size = 18;

	int scount;   // number of stacks

	protected ImagePlus imp, imp2;
	protected ImageStack stack, stack2;
	   
	//-----------------------------------------------------------------------------------

	public int setup(String arg, ImagePlus imp) 
	{
		if (arg.equals("about")) 
		{
			showAbout();
			return DONE;
		}
		this.imp = imp;
		IJ.resetEscape();
		return DOES_16+DOES_8G+DOES_RGB+NO_UNDO;
	} // end of 'setup' method

	//-----------------------------------------------------------------------------------

	void showAbout() 
	{
		IJ.showMessage("About 2D Anisotropic Diffusion Tschumperle-Deriche",
			"version "+ADTDVersion+" ("+ADTDDate+")\n"+
			"Vladimir Pilny, Jiri Janacek GPL2\n"+
			"Based on CImg Library - http://cimg.sourceforge.net/\n\n"+
			"This plugin is designed to filter images\n"+
			"using 2D Tschumperle-Deriche's anisotropic diffusion.");
	} // end of 'showAbout' method

	//-----------------------------------------------------------------------------------

	private boolean GUI() 
	{
		GenericDialog gd = new GenericDialog("2D Anisotropic Diffusion Tschumperle-Deriche v"+ADTDVersion);
		gd.addNumericField("Number of iterations", nb_iter, 0);
		gd.addNumericField("Smoothings per iteration", nb_smoothings, 0);
		if (scount==1) {
			if (save>nb_iter) save = nb_iter;
			gd.addNumericField("Keep each ", save, 0, 2, "iteration");
		}
		gd.addNumericField("a1 (Diffusion limiter along minimal variations)", a1, 2);
		gd.addNumericField("a2 (Diffusion limiter along maximal variations)", a2, 2);
		gd.addNumericField("dt (Time step)", dt, 1);
		gd.addNumericField("edge threshold height", edgeheight, 1);
		
        String[] labels = {"Show_filter stats", "Show_time stats", "Add labels"};
		boolean[] values = {sstats, tstats, add_labels};
		gd.addCheckboxGroup(2, 2, labels, values);
		gd.addMessage("Incorrect values will be replaced by defaults.\nLabels are drawn in the foreground color.\nPress Esc to stop processing.");
		return getUserParams(gd);
	}

	//-----------------------------------------------------------------------------------

	private boolean getUserParams(GenericDialog gd) 
	{
		gd.showDialog();
		// the user presses the Cancel button
		if (gd.wasCanceled()) return false;

		nb_iter = (int) gd.getNextNumber();
		if (nb_iter<1) nb_iter=1;

		nb_smoothings = (int) gd.getNextNumber();
		if (nb_smoothings<1) nb_smoothings=1;

		if (scount==1) 
		{
			save = (int) gd.getNextNumber();
			if (save<1 || save>nb_iter) save=nb_iter;
		} else
			save = 0;
		a1 = (float) gd.getNextNumber();
		a2 = (float) gd.getNextNumber();
		dt = (double) gd.getNextNumber();
		edgeheight = (float) gd.getNextNumber();
		sstats = (boolean) gd.getNextBoolean();
		tstats = (boolean) gd.getNextBoolean();
		add_labels = (boolean) gd.getNextBoolean();

		return true;
	} // end of 'getUserParams' method

	//-----------------------------------------------------------------------------------

	public void run(ImageProcessor ip) 
	{
		stack = imp.getStack();
		scount = stack.getSize();
		if (GUI()) runTD(ip);
	} // end of 'run' method

	//-----------------------------------------------------------------------------------

	public void runTD(ImageProcessor ip) 
	{
		Date d1, d2;
		d1 = new Date();

		IJ.showStatus("Initializing...");
		IJ.showProgress(0.0);
		if (sstats) IJ.log("\n--- "+imp.getShortTitle()+" --- a1="+a1+", a2 = "+a2+", dt = "+dt);

		// variables and arrays
		int channels = ip instanceof ColorProcessor ? 3 : 1;
		Rectangle r = ip.getRoi();
		int width = r.width;
		int height = r.height;
		int totalwidth = ip.getWidth();
		int totalheight = ip.getHeight();
		ImageProcessor ip2;
		ImageStack stack2 = imp.createEmptyStack();
		float[][][][] grad = new float[2][width][height][channels];
        float[][][] G = new float[width][height][3]; // must be clean for each slice
		float[][][] T = new float[width][height][3];
		float[][][] veloc = new float[width][height][3];
		float val1, val2;
		float vec1, vec2;
		double xdt;
		float[][][] ipf  = new float[width][height][channels];
		float[][][] ipfo = new float[width][height][channels];
		int[] pixel = new int[channels];
		float fx, fy;
		double ipfnew;
		long[] time = new long[6];
		boolean breaked = false;
		int iter = 0;
		double average, stddev, drange, drange2, drange0;
		
		// consts
		final float c1 = (float)(0.25*(2-Math.sqrt(2.0))), c2 = (float)(0.5f*(Math.sqrt(2.0)-1));

		if (tstats) { d2 = new Date(); time[0]=d2.getTime()-d1.getTime(); }

		// slices loop
		for (int s=0; s++ < scount && !breaked; )
		{
			if (tstats) d1 = new Date();
			if (scount>1)
			{
				ip = stack.getProcessor(s);
				IJ.showStatus("Slice "+s+"/"+scount+"...");
			}

			// convert image into the float channels
			for (int x=width; x-->0;)
				for (int y=height; y-->0;) 
				{
					pixel = ip.getPixel(x+r.x, y+r.y, pixel);
					for (int k=channels; k-->0;) 
					{
						ipf[x][y][k] = pixel[k];                        
					}
				}

			// get initial stats for later normalizing
			average = 0;
			float initial_max=ipf[0][0][0], initial_min=ipf[0][0][0], pix;
			for (int x=width; x-->0;)
				for (int y=height; y-->0;)
					for (int k=channels; k-->0;) 
					{
						pix = ipf[x][y][k];
						if (pix>initial_max) initial_max=pix;
						if (pix<initial_min) initial_min=pix;
						average += pix;
					}
			average /= (width*height*channels);
			// standard deviation
			stddev = 0;
            for (int x=width; x-->0;)
				for (int y=height; y-->0;)
					for (int k=channels; k-->0;) 
					{
						pix = ipf[x][y][k];
						stddev += (pix-average)*(pix-average);
					}
			stddev = Math.sqrt(stddev/(width*height*channels));
            
			//version 0.3 normalization
			drange= (edgeheight*stddev)/256.0;
			drange0= (6*stddev)/256.0;
			drange2= drange * drange;

			if (tstats) { d2 = new Date(); time[0]+=d2.getTime()-d1.getTime(); }

			// PDE main iteration loop
			for (iter=0; (iter < nb_iter) && (!breaked); iter++)
			{
				if (scount == 1) IJ.showStatus("Iteration "+(iter+1)+"/"+nb_iter+"...");

				// compute gradients
				if (tstats) d1 = new Date();
				float Ipp,Icp,Inp=0,Ipc,Icc,Inc=0,Ipn,Icn,Inn=0;
				/*/
				for (int k=0; k<channels; k++)
					for (int y=0, py=0, ny=1; ny<height || y==--ny; py=y++, ny++) 
					{
						Icp=Ipp=ipf[0][py][k];
						Icc=Ipc=ipf[0][y] [k];
						Icn=Ipn=ipf[0][ny][k];
						for (int nx=1, x=0, px=0; nx<width || x==--nx; Ipp=Icp, Ipc=Icc, Ipn=Icn, Icp=Inp, Icc=Inc, Icn=Inn, px=x++,nx++ ) 
						{
							Inp=ipf[nx][py][k];
							Inc=ipf[nx][y] [k];
							Inn=ipf[nx][ny][k];
							grad[0][x][y][k] = (float)(-c1*Ipp-c2*Ipc-c1*Ipn+c1*Inp+c2*Inc+c1*Inn);
							grad[1][x][y][k] = (float)(-c1*Ipp-c2*Icp-c1*Inp+c1*Ipn+c2*Icn+c1*Inn);
						}
					}
				/*/
				// the following seems several times faster
				for (int x=width; x-->0;) 
				{
					int px=x-1; if (px<0) px=0;
					int nx=x+1; if (nx==width) nx--;
					for (int y=height; y-->0;) 
					{
						int py=y-1; if (py<0) py=0;
						int ny=y+1; if (ny==height) ny--;
						for (int k=channels; k-->0;) 
						{
							Ipp=ipf[px][py][k];
							Ipc=ipf[px][y] [k];
							Ipn=ipf[px][ny][k];
							Icp=ipf[x] [py][k];
							Icn=ipf[x] [ny][k];
							Inp=ipf[nx][py][k];
							Inc=ipf[nx][y] [k];
							Inn=ipf[nx][ny][k];
							float IppInn = c1*(Inn-Ipp);
							float IpnInp = c1*(Ipn-Inp);
							grad[0][x][y][k] = (float)(IppInn-IpnInp-c2*Ipc+c2*Inc);
							grad[1][x][y][k] = (float)(IppInn+IpnInp-c2*Icp+c2*Icn);
						}
					}
				}
				//*/

				if (tstats) { d2 = new Date(); time[1]+=d2.getTime()-d1.getTime(); d1 = new Date(); }

				// compute structure tensor field G
//				G = new float[width][height][3]; // must be clean for each slice
				for (int x=width; x-->0;)
					for (int y=height; y-->0;) {
						G[x][y][0]= 0.0f;
						G[x][y][1]= 0.0f;
						G[x][y][2]= 0.0f;
						for (int k=channels; k-->0;) 
						{
						//version 0.2 normalization
							fx = grad[0][x][y][k];
							fy = grad[1][x][y][k];
							G[x][y][0] += fx*fx;
							G[x][y][1] += fx*fy;
							G[x][y][2] += fy*fy;
						}
					}
				if (tstats) { d2 = new Date(); time[2]+=d2.getTime()-d1.getTime(); d1 = new Date(); }

				// compute the tensor field T, used to drive the diffusion
				for (int x=width; x-->0;)
					for (int y=height; y-->0;) 
					{
						// eigenvalues:
						double a = G[x][y][0], b = G[x][y][1], c = G[x][y][1], d = G[x][y][2], e = a+d;
						double f = Math.sqrt(e*e-4*(a*d-b*c));
						double l1 = 0.5*(e-f), l2 = 0.5*(e+f);
						// more precise computing of quadratic equation
						if (e>0) { if (l1!=0) l2 = (a*d - b*c)/l1; }
						else     { if (l2!=0) l1 = (a*d - b*c)/l2; }

   
						val1=(float)(l2 / drange2);
						val2=(float)(l1 / drange2);
						// slight cheat speedup for default a1 value
						float f1 = (a1==.5) ? (float)(1/Math.sqrt(1.0f+val1+val2)) : (float)(Math.pow(1.0f+val1+val2,-a1));
						float f2 = (float)(Math.pow(1.0f+val1+val2,-a2));

						// eigenvectors:
						double u, v, n;
						if (Math.abs(b)>Math.abs(a-l1)) { u = 1; v = (l1-a)/b; }
						else { if (a-l1!=0) { u = -b/(a-l1); v = 1; } 
							   else { u = 1; v = 0; } 
						}
						n = Math.sqrt(u*u+v*v); u/=n; v/=n; 
						vec1 = (float)u; vec2 = (float)v;
						float vec11 = vec1*vec1, vec12 = vec1*vec2, vec22 = vec2*vec2;
						T[x][y][0] = f1*vec11 + f2*vec22;
						T[x][y][1] = (f1-f2)*vec12;
						T[x][y][2] = f1*vec22 + f2*vec11;
					}
				
				if (tstats) { d2 = new Date(); time[3]+=d2.getTime()-d1.getTime(); d1 = new Date(); }
				xdt= 0.0;

				// multiple smoothings per iteration
				for(int sit=0; sit < nb_smoothings && !breaked; sit++)
				{
					IJ.showProgress((scount==1) ? (iter+sit/(float)nb_smoothings)/(float)nb_iter : s/(float)scount);

                    // compute the PDE velocity and update the iterated image
					Inp=Inc=Inn=0;
					/*/
					for (int k=0; k<channels; k++)
						for (int y=0, py=0, ny=1; ny<height || y==--ny; py=y++,ny++) 
						{
							Icp=Ipp=ipf[0][py][k];
							Icc=Ipc=ipf[0][y] [k];
							Icn=Ipn=ipf[0][ny][k];
							for (int nx=1, x=0, px=0; nx<width || x==--nx; Ipp=Icp, Ipc=Icc, Ipn=Icn, Icp=Inp, Icc=Inc, Icn=Inn, px=x++, nx++ ) 
							{
								Inp=ipf[nx][py][k];
								Inc=ipf[nx][y] [k];
								Inn=ipf[nx][ny][k];
								float a = T[x][y][0],
									b = T[x][y][1],
									c = T[x][y][2],
									ixx = Inc+Ipc-2*Icc,
									iyy = Icn+Icp-2*Icc,
									ixy = 0.5f*(Ipp+Inn-Ipn-Inp);
								veloc[x][y][k] = a*ixx + b*ixy + c*iyy; 
							}
						}
					/*/
					// the following seems several times faster
					for (int x=width; x-->0;) 
					{
						int px=x-1; if (px<0) px=0;
						int nx=x+1; if (nx==width) nx--;
						for (int y=height; y-->0;) 
						{
							int py=y-1; if (py<0) py=0;
							int ny=y+1; if (ny==height) ny--;
							for (int k=channels; k-->0;) 
							{
								Ipp=ipf[px][py][k];
								Ipc=ipf[px][y] [k];
								Ipn=ipf[px][ny][k];
								Icp=ipf[x] [py][k];
								Icc=ipf[x] [y] [k];
								Icn=ipf[x] [ny][k];
								Inp=ipf[nx][py][k];
								Inc=ipf[nx][y] [k];
								Inn=ipf[nx][ny][k];
								float ixx = Inc+Ipc-2*Icc,
									iyy = Icn+Icp-2*Icc,
									ixy = 0.5f*(Ipp+Inn-Ipn-Inp);
								veloc[x][y][k] = T[x][y][0]*ixx + T[x][y][1]*ixy + T[x][y][2]*iyy; 
							}
						}
					}
					//*/

					// find xdt coefficient
					if (dt>0) 
					{
						float max=veloc[0][0][0], min=veloc[0][0][0];
						for (int x=width; x-->0;)
							for (int y=height; y-->0;)
								for (int k=channels; k-->0;) 
								{
									if (veloc[x][y][k]>max) max=veloc[x][y][k];
									if (veloc[x][y][k]<min) min=veloc[x][y][k];
								}
						//version 0.2 normalization
						xdt = dt/Math.max(Math.abs(max), Math.abs(min))*drange0;
					} 
					else xdt = -dt;
					if (tstats) { d2 = new Date(); time[4]+=d2.getTime()-d1.getTime(); d1 = new Date(); }

					// update image
					for (int x=width; x-->0;)
						for (int y=height; y-->0;)
							for (int k=channels; k-->0;) 
							{
								ipfnew = ipf[x][y][k] + veloc[x][y][k]*xdt;
								ipf[x][y][k] = (float)ipfnew;
								// normalize image to the original range
								if (ipf[x][y][k] < initial_min) ipf[x][y][k] = initial_min;
								if (ipf[x][y][k] > initial_max) ipf[x][y][k] = initial_max;
							}

					if (breaked=IJ.escapePressed()) IJ.beep();
				} // smoothings per iteration

				// save result
				if ((scount==1 && ((save>0 && (iter+1)%save==0) || breaked)) || (iter==nb_iter-1))
				// create new image if you break the cycle with Esc, if it is saving step, or if we get below stopping condition
				{
					ip2 = ip.createProcessor(totalwidth, totalheight);
					for (int x=totalwidth; x-->0;)
						for (int y=totalheight; y-->0;)
							for (int k=channels; k-->0;) 
							{
								if ((x<r.x) || (x>=r.x+width) || (y<r.y) || (y>=r.y+height))
									ip2.putPixel(x,y,ip.getPixel(x,y));
								else 
								{
									pixel[k] = (int)ipf[x-r.x][y-r.y][k];
									ip2.putPixel(x,y,pixel);
								}
							}
					if (scount == 1) {
						if (add_labels) {
							if (label_color==null) {
								label_color = Toolbar.getForegroundColor();
								font = new Font("SansSerif", Font.PLAIN, font_size);
							}
							ip2.setColor(label_color);
							ip2.setFont(font);
							ip2.drawString("iterations: "+(iter+1), 5, (int)(font_size*1.3));
						}
						if (imp2==null) {
							stack2 = new ImageStack(ip2.getWidth(), ip2.getHeight());
							stack2.addSlice("iter"+(iter+1), ip2);
							imp2 = new ImagePlus(imp.getShortTitle()+"-iter"+(iter+1)+((breaked)?"-interrupted":""), stack2);
							imp2.show();
						} else {
							stack2.addSlice("iter"+(iter+1), ip2);
							imp2.setStack(null, stack2);
							imp2.setSlice(imp2.getStackSize());
							imp2.setTitle(imp.getShortTitle()+"-iter"+(iter+1));
						}
					} else
						stack2.addSlice(stack.getSliceLabel(s), ip2);
				}
				if (sstats) IJ.log("\riter "+(iter+1)+", xdt = "+xdt);
				if (tstats) { d2 = new Date(); time[5]+=d2.getTime()-d1.getTime(); }
			} // iterations
		} // slices
		if (imp2!=null && imp2.getStackSize()>1) {
			ip2 = imp.getProcessor().duplicate();
			if (add_labels) {
				ip2.setColor(label_color);
				ip2.setFont(font);
				ip2.drawString("original", 5,  (int)(font_size*1.3));
			}
			stack2.addSlice("orig", ip2, 0);
			imp2.setStack(null, stack2);
			imp2.setSlice(imp2.getStackSize());
		}
		IJ.showProgress(1.0);
		if (scount>1) new ImagePlus(imp.getShortTitle()+"-iter"+nb_iter, stack2).show();


		if (tstats) 
		{
			IJ.log("\r--- time statistics ---");
			IJ.log("\rinitialization = "+time[0]+" ms");
			IJ.log("\rgradients = "+time[1]+" ms");
			IJ.log("\rG tensor field = "+time[2]+" ms");
			IJ.log("\rT tensor field = "+time[3]+" ms");
			IJ.log("\rvelocity = "+time[4]+" ms");
			IJ.log("\rupdate image = "+time[5]+" ms");
		}

	} // end of 'runTD' method

} // end of filter Class 
