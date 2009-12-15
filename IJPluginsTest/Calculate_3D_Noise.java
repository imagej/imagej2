import java.awt.*;
import java.awt.Panel;
import java.awt.Button;
import java.awt.event.*;
import javax.swing.*;
import ij.*;
import ij.gui.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.WaitForUserDialog;

/**
 *      This ImageJ plugin calculates the 3D-noise from a sequence
 *      of images.  The 3D-noise algorithm follows that developed by
 *      the Night Vision Lab (or Night Vision Electronic Sensor Directorate)
 *      at Fort Belvoir, VA.
 *      see "Infrared Focal Plane Noise Parameter Definitions"
 *             by J. G. Zeibel & R.T. Littleton at NVESD, October 2003
 *
 *      Useage:
 *      1) Open an image sequence or video file and selects an ROI.
 *      2) Run this plug-in
 *      3) Verify the ROI.
 *      4) Input a Signal Transfer function (SiTF).
 * 	This plugin was developed to analyze the 3D-noise
 * 	of infrared images.  Thus, the SiTF has units to convert
 *	image signal to degrees-Centigrade.  
 *
 *      Output:
 *      The ImageJ Results Window is loaded with
 *	a) Eight 3D-noise components and the temporal noise.
 *	    If an SiTF is entered, the units are milli-Kelvin.
 *	b) The SiTF (if used)
 *	c) The mean image signal within the ROI
 *	d) Size of the "Data Cube": height, width, # frames
 * 
 *      Author = Dwight Urban Bartholomew
 *                     L3 Communications Infrared Products
 *                     February 2008
 */

public class Calculate_3D_Noise implements PlugInFilter, ActionListener {
    int             iX;
    int             iY;
    int             iXROI;
    int             iYROI;
    int             iSlice;
    int	     Nt;
    int             Nv;
    int             Nh;
    boolean    bAbort;
    ImagePlus       imp;
    char	    Tab;
    char	    Sigma;
    double     SiTF;
    boolean   bSiTF;
    GenericDialog TheGD;
    Rectangle r;
    Button     TheButton;
    /**
     *	Called by ImageJ when the filter is loaded
     */
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        if (arg.equals("about"))
                {showAbout(); return DONE;}
        if (IJ.versionLessThan("1.39r")) return DONE;
        return DOES_8G+DOES_16+DOES_32+ROI_REQUIRED+NO_CHANGES;
    }

    /**
     *	Called by ImageJ to process the image
     */
    public void run(ImageProcessor ip) {
        bAbort = false;
        r = ip.getRoi();
        Nt = imp.getNSlices();
        Nv = r.height;
        Nh = r.width;
        iXROI = r.x;
        iYROI = r.y;
        iSlice = imp.getCurrentSlice();
        Tab = (char)9;
        Sigma = (char)963;
        SiTF = 0.0;
        bSiTF = false;

    //#################################################
    //Ask User to verify the Region of Interest (ROI) to analyze
        get3DnoiseROI();
        if (bAbort)
            return;
        iX = iXROI;
        iY = iYROI;
        //Display the ROI as a rectangle on the image
        imp.setRoi(iX, iY, Nh, Nv);

    //#################################################
    //Ask User for a Signal Transfer Function (SITF)
        Object info = imp.getProperty("Info");
        Object DefVal = null;
        double oldSiTF = 0.0;
        if (info!=null && info instanceof String) {
	//oldSiTF = Double.valueOf((String)info).doubleValue();
	oldSiTF = ij.util.Tools.parseDouble((String)info, 0.0);
        }
        SiTF = getSiTF(oldSiTF);
        // Store the SiTF within the image information property
        imp.setProperty("Info", ""+SiTF);

    //#################################################
    //Place the ROI into a 3D array Stvh, the "data cube"
        int t, v, h, cSlice;
        double[][][] Stvh = new double[Nt][Nv][Nh];      
        cSlice = imp.getCurrentSlice();
        for(t=0; t<Nt; t++) {
	imp.setSlice(t+1);
	for(v=0; v<Nv; v++) {
	        for(h=0; h<Nh; h++) {
		Stvh[t][v][h] = (double)ip.getPixel(iX+h,iY+v);
	        }
	}
        }
        imp.setSlice(cSlice);

    //#################################################
    //Calculate 3D Noise using the "data cube"

        //***************************
        // Step 0: Calculate normal temporal noise
        double[][][] SIGMA = new double[Nv][Nh][1];      
        double[] farray = new double[Nt];      
        for(v=0; v<Nv; v++) {
	for(h=0; h<Nh; h++) {
	        for(t=0; t<Nt; t++) {
	                farray[t] = Stvh[t][v][h];
	        }
	        SIGMA[v][h][0] = sdKnuth(farray);
	}
        }
        double Noise_temporal = Mean1D( Make1D(SIGMA) );
	//***MatLab code
	//Noise_temporal = mean(mean(std(Stvh,1),2),3);

        //***************************
        // Step 1: Calculate S
        double S = Mean1D( Make1D(Stvh) );
        for(t=0; t<Nt; t++) {
	for(v=0; v<Nv; v++) {
	        for(h=0; h<Nh; h++) {
		Stvh[t][v][h] -= S;
	        }
	}
        }
	//***MatLab code
	//S = mean(mean(mean(Stvh,1),2),3);
	//Stvh = Stvh - S;

        //***************************
        // Step 2: Calculate Sigma(v)
        double[][][] MUv = new double[1][Nv][1];
        MUv = Mean3D( Mean3D( Stvh, 1), 3);
        double SIGv = sdKnuth( Make1D(MUv) );
        for(t=0; t<Nt; t++) {
	for(v=0; v<Nv; v++) {
	        for(h=0; h<Nh; h++) {
		Stvh[t][v][h] -= MUv[0][v][0];
	        }
	}
        }
	//***MatLab code
	//MUv = reshape(mean(mean(Stvh,1),3), [1 Nv]);
	//SIGMAv = std(MUv);
	//for i=1:Nv
	//    Stvh(:,i,:) = Stvh(:,i,:) - MUv(i);
	//end

        //***************************
        // Step 3: Calculate Sigma(h)
        double[][][] MUh = new double[1][1][Nh];
        MUh = Mean3D( Mean3D( Stvh, 1), 2);
        double SIGh = sdKnuth( Make1D(MUh) );
        for(t=0; t<Nt; t++) {
	for(v=0; v<Nv; v++) {
	        for(h=0; h<Nh; h++) {
		Stvh[t][v][h] -= MUh[0][0][h];
	        }
	}
        }
	//***MatLab code
	//MUh = reshape(mean(mean(Stvh,1),2), [1 Nh]);
	//SIGMAh = std(MUh);
	//for i=1:Nh
	//    Stvh(:,:,i) = Stvh(:,:,i) - MUh(i);
	//end

        //***************************
        // Step 4: Calculate Sigma(t)
        double[][][] MUt = new double[Nt][1][1];
        MUt = Mean3D( Mean3D( Stvh, 2), 3);
        double SIGt = sdKnuth( Make1D(MUt) );
        for(t=0; t<Nt; t++) {
	for(v=0; v<Nv; v++) {
	        for(h=0; h<Nh; h++) {
		Stvh[t][v][h] -= MUt[t][0][0];
	        }
	}
        }
	//***MatLab code
	//MUt = reshape(mean(mean(Stvh,2),3), [1 Nt]);
	//SIGMAt = std(MUt);
	//for i=1:Nt
	//    Stvh(i,:,:) = Stvh(i,:,:) - MUt(i);
	//end

        //***************************
        // Step 5: Calculate Sigma(th)
        double[][][] MUth = new double[Nt][1][Nh];
        MUth = Mean3D( Stvh, 2);
        double SIGth = sdKnuth( Make1D(MUth) );
        for(t=0; t<Nt; t++) {
	for(v=0; v<Nv; v++) {
	        for(h=0; h<Nh; h++) {
		Stvh[t][v][h] -= MUth[t][0][h];
	        }
	}
        }
	//***MatLab code
	//MUth = reshape(mean(Stvh,2), [Nt Nh]);
	//SIGMAth = std(reshape(MUth, [1 Nt*Nh]));
	//for i=1:Nt
	//    for j=1:Nh
	//        Stvh(i,:,j) = Stvh(i,:,j) - MUth(i,j);
	//    end
	//end

        //***************************
        // Step 6: Calculate Sigma(tv)
        double[][][] MUtv = new double[Nt][Nv][1];
        MUtv = Mean3D( Stvh, 3);
        double SIGtv = sdKnuth( Make1D(MUtv) );
        for(t=0; t<Nt; t++) {
	for(v=0; v<Nv; v++) {
	        for(h=0; h<Nh; h++) {
		Stvh[t][v][h] -= MUtv[t][v][0];
	        }
	}
        }
	//***MatLab code
	//MUtv = reshape(mean(Stvh,3), [Nt Nv]);
	//SIGMAtv = std(reshape(MUtv, [1 Nt*Nv]));
	//for i=1:Nt
	//    for j=1:Nv
	//        Stvh(i,j,:) = Stvh(i,j,:) - MUtv(i,j);
	//    end
	//end

        //***************************
        // Step 7: Calculate Sigma(vh)
        double[][][] MUvh = new double[1][Nv][Nh];
        MUvh = Mean3D( Stvh, 1);
        double SIGvh = sdKnuth( Make1D(MUvh) );
        for(t=0; t<Nt; t++) {
	for(v=0; v<Nv; v++) {
	        for(h=0; h<Nh; h++) {
		Stvh[t][v][h] -= MUvh[0][v][h];
	        }
	}
        }
	//***MatLab code
	//MUvh = reshape(mean(Stvh,1), [Nv Nh]);
	//SIGMAvh = std(reshape(MUvh, [1 Nv*Nh]));
	//for i=1:Nv
	//    for j=1:Nh
	//        Stvh(:,i,j) = Stvh(:,i,j) - MUvh(i,j);
	//    end
	//end

        //***************************
        // Step 8: Calculate Sigma(tvh)
        double SIGtvh = sdKnuth( Make1D(Stvh) );
	//***MatLab code
	//SIGMAtvh = std(reshape(Stvh, [1 Nt*Nv*Nh]));

        if(SiTF != 0) {
	//Convert degrees-C into degrees-milli-Kelvin
	SIGt *= 1000*SiTF;
	SIGv *= 1000*SiTF;
	SIGh *= 1000*SiTF;
	SIGvh *= 1000*SiTF;
	SIGtv *= 1000*SiTF;
	SIGth *= 1000*SiTF;
	SIGtvh *= 1000*SiTF;
	Noise_temporal *= 1000*SiTF;
        }

        //***************************
        //Display the results
        IJ.getTextPanel();
        if(SiTF != 0) {
	IJ.setColumnHeadings("Component"+Tab+"mK"+Tab+"Meaning");
        }
        else {
	IJ.setColumnHeadings("Component"+Tab+"image units"+Tab+"Meaning");
        }

        IJ.write(Sigma+"(t)"+Tab+IJ.d2s(SIGt,4)+Tab+"Frame to frame noise or bounce (flicker)");
        IJ.write(Sigma+"(v)"+Tab+IJ.d2s(SIGv,4)+Tab+"Fixed row noise (horizontal lines)");
        IJ.write(Sigma+"(h)"+Tab+IJ.d2s(SIGh,4)+Tab+"Fixed column noise (vertical lines)");
        IJ.write(Sigma+"(vh)"+Tab+IJ.d2s(SIGvh,4)+Tab+"Random time-independent spatial noise (fixed pattern noise, FPN)");
        IJ.write(Sigma+"(tv)"+Tab+IJ.d2s(SIGtv,4)+Tab+"Temporal row bounce (streaking)");
        IJ.write(Sigma+"(th)"+Tab+IJ.d2s(SIGth,4)+Tab+"Temporal column bounce (rain)");
        IJ.write(Sigma+"(tvh)"+Tab+IJ.d2s(SIGtvh,4)+Tab+"Random spatio-temporal noise");
        IJ.write(Sigma+"(temporal)"+Tab+IJ.d2s(Noise_temporal,4)+Tab+"Temporal noise (average temporal noise on any particular pixel)");

        if(SiTF != 0) {
	IJ.write(""+Tab+IJ.d2s(SiTF,6)+Tab+"Signal Transfer Function (SiTF) (in degs-C/image-units)");
        }
        else {
	IJ.write(""+Tab+""+Tab+"");
        }
        IJ.write(""+Tab+IJ.d2s(S,1)+Tab+"Average signal (in image units)");
        IJ.write(""+Tab+Nt+Tab+"Frames");
        IJ.write(""+Tab+Nv+Tab+"Rows");
        IJ.write(""+Tab+Nh+Tab+"Columns");

        IJ.register(Calculate_3Dnoise.class);
    }

    /**
     *	Creates a dialog box, allowing the user to enter the requested
     *	width, height, x & y coordinates for a Region Of Interest.
     *          (From the ImageJ plugin "Specify_ROI" by Anthony Padua)
     */
    void get3DnoiseROI() {

        GenericDialog gd = new GenericDialog("Analysis ROI", IJ.getInstance());
        gd.addNumericField("Width:", Nh, 0);
        gd.addNumericField("Height:", Nv, 0);
        gd.addNumericField("X Coordinate:", iXROI, 0);
        gd.addNumericField("Y Coordinate:", iYROI, 0);

        gd.showDialog();

        if (gd.wasCanceled()) {
            bAbort = true;
            return;
        }

        Nh = (int) gd.getNextNumber();
        Nv = (int) gd.getNextNumber();
        iXROI = (int) gd.getNextNumber();	
        iYROI = (int) gd.getNextNumber();	
    }


    /**
     *	Creates a dialog box, allowing the user to enter an SiTF
     */
    double getSiTF(double TheDefault) {
        bSiTF = false;
        double newSiTF = 0;
        Panel ThePanel = new Panel();
        TheGD = new GenericDialog("Input SiTF", IJ.getInstance());
        TheGD.addNumericField("Input an Signal Transfer Function", TheDefault, 6, 12, "degrees-C / image_units");
        TheGD.addPanel(ThePanel);
        TheButton = new Button("Calculate SiTF");
        TheButton.addActionListener(this);
        ThePanel.add(TheButton);

        TheGD.showDialog();

        if (TheGD.wasCanceled()) {
            bAbort = true;
            return 0.0;
        }

        if (bSiTF) {	// Start calculating the SiTF based on User inputs
	double Ave1=0, Ave2=0, dTemp=0;
	ImageStatistics is = new ImageStatistics();
	// Obtain the average image signal from the first Region Of Interest
            	new WaitForUserDialog( "Calculate 3D Noise", "Create an ROI for region #1, then press \"OK\"").show(); 
	is = imp.getStatistics();
	Ave1 = is.mean;
	// Obtain the average image signal from the second Region Of Interest
            	new WaitForUserDialog("Calculate 3D Noise", "Create an ROI for region #2, then press \"OK\"").show(); 
	is = imp.getStatistics();
	Ave2 = is.mean;
	// Ask for the temperature difference between the two ROIs
            	dTemp = IJ.getNumber("Enter the temperature difference between regions #1 and #2 in degrees-Celius", 2.0);
	if( dTemp == IJ.CANCELED ) dTemp=0.0;
	imp.setRoi(r);  // replace the original ROI
            try { 	
	 newSiTF = Math.abs(dTemp / (Ave1 - Ave2));
            }
            catch (RuntimeException e) {
	 newSiTF = 0.0;
            }
        }
        else {
            newSiTF = (double) TheGD.getNextNumber();
        }
        return newSiTF;
    }


public void actionPerformed(ActionEvent e)
// Detect and handle user interactions with buttons and other window objects.
{
	if (e.getSource()==TheButton)
	{
		bSiTF = true;
		TheGD.dispose();
	}
}


 /**
   * Calculates the standard deviation of an array of numbers.
   * see Knuth's The Art Of Computer Programming
   * Volume II: Seminumerical Algorithms
   * This algorithm is slower, but more resistant to error propagation.
   *
   * @param data Numbers to compute the standard deviation of.
   * Array must contain two or more numbers.
   * @return standard deviation estimate of population
   * ( to get estimate of sample, use n instead of n-1 in last line )
   */
double sdKnuth ( double[] data )
      {
      final int n = data.length;
      if ( n < 2 )
         {
         return Double.NaN;
         }
      double avg = data[0];
      double sum = 0;
      for ( int i = 1; i < data.length; i++ )
         {
         double newavg = avg + ( data[i] - avg ) / ( i + 1 );
         sum += ( data[i] - avg ) * ( data [i] -newavg ) ;
         avg = newavg;
         }
      return Math.sqrt( sum / ( n - 1 ) );
      }


 /**
   * Calculates the mean of an 1D array
   */
double Mean1D ( double[] data )
      {
      int n = data.length;
      if ( n < 1 )
         {
         return Double.NaN;
         }
      double avg = data[0];
      double sum = 0;
      for ( int i = 1; i < data.length; i++ )
         {
         sum += data[i];
         }
      return ( sum/n );
      }

 /**
   * Calculates the mean of a 3D array and
   * returns the result as a 3D array where the dimension
   * being averaged has been reduced to unity.
   */
double[][][] Mean3D ( double[][][] data, int dim )
      {
        int i1=0, i2=0, i3=0;
        int d1=0, d2=0, d3=0;
        int N1 = data.length;
        int N2 = data[0].length;
        int N3 = data[0][0].length;
        double sum=0;

        if( dim==1 ) {
	d1=1; d2=N2; d3=N3;
        }
        if( dim==2 ) {
	d1=N1; d2=1; d3=N3;
        }
        if( dim==3 ) {
	d1=N1; d2=N2; d3=1;
        }
        double[][][] TheAve = new double[d1][d2][d3];

        if( dim==1 ) {
	for(i2=0; i2<N2; i2++) {
	        for(i3=0; i3<N3; i3++) {
		sum=0;
		for(i1=0; i1<N1; i1++) {
		        sum += data[i1][i2][i3];
		}
		TheAve[0][i2][i3] = sum/N1;
	        }
	}
        }
        if( dim==2 ) {
	for(i1=0; i1<N1; i1++) {
	        for(i3=0; i3<N3; i3++) {
		sum=0;
		for(i2=0; i2<N2; i2++) {
		        sum += data[i1][i2][i3];
		}
		TheAve[i1][0][i3] = sum/N2;
	        }
	}
        }
        if( dim==3 ) {
	for(i1=0; i1<N1; i1++) {
	        for(i2=0; i2<N2; i2++) {
		sum=0;
		for(i3=0; i3<N3; i3++) {
		        sum += data[i1][i2][i3];
		}
		TheAve[i1][i2][0] = sum/N3;
	        }
	}
        }

      return ( TheAve );
      }


 /**
   * Inputs a 3D array and outputs a 1D array.
   */
double[] Make1D ( double[][][] data )
      {
        int n=0;
        int i1=0, i2=0, i3=0;
        int N1 = data.length;
        int N2 = data[0].length;
        int N3 = data[0][0].length;

        double[] The1Darray = new double[N1*N2*N3];
        for(i1=0;i1<N1;i1++) {
	for(i2=0; i2<N2; i2++) {
	        for(i3=0; i3<N3; i3++) {
		The1Darray[n] = data[i1][i2][i3];
		n++;
	        }
	}
        }
      return ( The1Darray );
      }


    /**
     *	Displays a short message describing the filter
     */
    void showAbout() {
        IJ.showMessage("About Calculate_3Dnoise...",
                "This PlugIn performs a 3D-Noise Analysis on a specified ROI.\n"
        );
    }

}


