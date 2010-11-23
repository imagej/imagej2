package ijx.process;
import ijx.IJ;
import java.util.Arrays;

/** Autothresholding methods from the Auto_Threshold plugin (http://pacific.mpi-cbg.de/wiki/index.php/Auto_Threshold)
    by G.Landini at bham dot ac dot uk. */
public class AutoThresholder {
	private static String[] mStrings;
			
	public enum Method {
		Default, 
		Huang,
		Intermodes,
		IsoData, 
		IJ_IsoData,
		Li, 
		MaxEntropy, 
		Mean, 
		MinError, 
		Minimum,
		Moments, 
		Otsu, 
		Percentile, 
		RenyiEntropy, 
		Shanbhag, 
		Triangle, 
		Yen
	};

	public static String[] getMethods() {
		if (mStrings==null) {
			Method[] mVals = Method.values();
			mStrings = new String[mVals.length];
			for (int i=0; i<mVals.length; i++)
				mStrings[i] = mVals[i].name();
		}
		return mStrings;
	}
	
	public int getThreshold(Method method, int[] histogram) {
		int threshold = 0;
		switch (method) {
			case Default: threshold =  defaultIsoData(histogram); break;
			case IJ_IsoData: threshold =  IJIsoData(histogram); break;
			case Huang: threshold = Huang(histogram); break;
			case Intermodes: threshold = Intermodes(histogram); break;
			case IsoData: threshold = IsoData(histogram); break;
			case Li: threshold = Li(histogram); break;
			case MaxEntropy: threshold = MaxEntropy(histogram); break;
			case Mean: threshold = Mean(histogram); break;
			case MinError: threshold = MinErrorI(histogram); break;
			case Minimum: threshold = Minimum(histogram); break;
			case Moments: threshold = Moments(histogram); break;
			case Otsu: threshold = Otsu(histogram); break;
			case Percentile: threshold = Percentile(histogram); break;
			case RenyiEntropy: threshold = RenyiEntropy(histogram); break;
			case Shanbhag: threshold = Shanbhag(histogram); break;
			case Triangle: threshold = Triangle(histogram); break;
			case Yen: threshold = Yen(histogram); break;
		}
		if (threshold==-1) threshold = 0;
		return threshold;
	}

	@Deprecated
	public int getThreshold(String mString, int[] histogram) {
		// throws an exception if unknown argument
		Method method = Method.valueOf(Method.class, mString); 
		return getThreshold(method, histogram);
	}
	
	int Huang(int [] data ) {
		// Implements Huang's fuzzy thresholding method 
		// Uses Shannon's entropy function (one can also use Yager's entropy function) 
		// Huang L.-K. and Wang M.-J.J. (1995) "Image Thresholding by Minimizing  
		// the Measures of Fuzziness" Pattern Recognition, 28(1): 41-51
		// M. Emre Celebi  06.15.2007
		// Ported to ImageJ plugin by G. Landini from E Celebi's fourier_0.8 routines
		int threshold=-1;
		int ih, it;
		int first_bin;
		int last_bin;
		int sum_pix;
		int num_pix;
		double term;
		double ent;  // entropy 
		double min_ent; // min entropy 
		double mu_x;

		/* Determine the first non-zero bin */
		first_bin=0;
		for (ih = 0; ih < 256; ih++ ) {
			if ( data[ih] != 0 ) {
				first_bin = ih;
				break;
			}
		}

		/* Determine the last non-zero bin */
		last_bin=255;
		for (ih = 255; ih >= first_bin; ih-- ) {
			if ( data[ih] != 0 ) {
				last_bin = ih;
				break;
			}
		}
		term = 1.0 / ( double ) ( last_bin - first_bin );
		double [] mu_0 = new double[256];
		sum_pix = num_pix = 0;
		for ( ih = first_bin; ih < 256; ih++ ){
			sum_pix += ih * data[ih];
			num_pix += data[ih];
			/* NUM_PIX cannot be zero ! */
			mu_0[ih] = sum_pix / ( double ) num_pix;
		}

		double [] mu_1 = new double[256];
		sum_pix = num_pix = 0;
		for ( ih = last_bin; ih > 0; ih-- ){
			sum_pix += ih * data[ih];
			num_pix += data[ih];
			/* NUM_PIX cannot be zero ! */
			mu_1[ih - 1] = sum_pix / ( double ) num_pix;
		}

		/* Determine the threshold that minimizes the fuzzy entropy */
		threshold = -1;
		min_ent = Double.MAX_VALUE;
		for ( it = 0; it < 256; it++ ){
			ent = 0.0;
			for ( ih = 0; ih <= it; ih++ ) {
				/* Equation (4) in Ref. 1 */
				mu_x = 1.0 / ( 1.0 + term * Math.abs ( ih - mu_0[it] ) );
				if ( !((mu_x  < 1e-06 ) || ( mu_x > 0.999999))) {
					/* Equation (6) & (8) in Ref. 1 */
					ent += data[ih] * ( -mu_x * Math.log ( mu_x ) - ( 1.0 - mu_x ) * Math.log ( 1.0 - mu_x ) );
				}
			}

			for ( ih = it + 1; ih < 256; ih++ ) {
				/* Equation (4) in Ref. 1 */
				mu_x = 1.0 / ( 1.0 + term * Math.abs ( ih - mu_1[it] ) );
				if ( !((mu_x  < 1e-06 ) || ( mu_x > 0.999999))) {
					/* Equation (6) & (8) in Ref. 1 */
					ent += data[ih] * ( -mu_x * Math.log ( mu_x ) - ( 1.0 - mu_x ) * Math.log ( 1.0 - mu_x ) );
				}
			}
			/* No need to divide by NUM_ROWS * NUM_COLS * LOG(2) ! */
			if ( ent < min_ent ) {
				min_ent = ent;
				threshold = it;
			}
		}
		return threshold;
	}

	boolean bimodalTest(double [] y) {
		int len=y.length;
		boolean b = false;
		int modes = 0;
 
		for (int k=1;k<len-1;k++){
			if (y[k-1] < y[k] && y[k+1] < y[k]) {
				modes++;
				if (modes>2)
					return false;
			}
		}
		if (modes == 2)
			b = true;
		return b;
	}

	int Intermodes(int [] data ) {
		// J. M. S. Prewitt and M. L. Mendelsohn, "The analysis of cell images," in
		// Annals of the New York Academy of Sciences, vol. 128, pp. 1035-1053, 1966.
		// ported to ImageJ plugin by G.Landini from Antti Niemisto's Matlab code (GPL)
		// Original Matlab code Copyright (C) 2004 Antti Niemisto
		// See http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide presentation
		// and the original Matlab code.
		//
		// Assumes a bimodal histogram. The histogram needs is smoothed (using a
		// running average of size 3, iteratively) until there are only two local maxima.
		// j and k
		// Threshold t is (j+k)/2.
		// Images with histograms having extremely unequal peaks or a broad and
		// ﬂat valley are unsuitable for this method.
		double [] iHisto = new double [256];
		int iter =0;
		int threshold=-1;
		for (int i=0; i<256; i++)
			iHisto[i]=(double) data[i];

		double [] tHisto = iHisto;

		while (!bimodalTest(iHisto) ) {
			 //smooth with a 3 point running mean filter
			for (int i=1; i<255; i++)
				tHisto[i]= (iHisto[i-1] + iHisto[i] + iHisto[i+1])/3;
			tHisto[0] = (iHisto[0]+iHisto[1])/3; //0 outside
			tHisto[255] = (iHisto[254]+iHisto[255])/3; //0 outside
			iHisto = tHisto;
			iter++;
			if (iter>10000) {
				threshold = -1;
				IJ.log("Intermodes: threshold not found after 10000 iterations.");
				return threshold;
			}
		}

		// The threshold is the mean between the two peaks.
		int tt=0;
		for (int i=1; i<255; i++) {
			if (iHisto[i-1] < iHisto[i] && iHisto[i+1] < iHisto[i]){
				tt += i;
				//IJ.log("mode:" +i);
			}
		}
		threshold = (int) Math.floor(tt/2.0);
		return threshold;
	}

	int IsoData(int[] data ) {
		// Also called intermeans
		// Iterative procedure based on the isodata algorithm [T.W. Ridler, S. Calvard, Picture 
		// thresholding using an iterative selection method, IEEE Trans. System, Man and 
		// Cybernetics, SMC-8 (1978) 630-632.] 
		// The procedure divides the image into objects and background by taking an initial threshold,
		// then the averages of the pixels at or below the threshold and pixels above are computed. 
		// The averages of those two values are computed, the threshold is incremented and the 
		// process is repeated until the threshold is larger than the composite average. That is,
		//  threshold = (average background + average objects)/2
		// The code in ImageJ that implements this function is the getAutoThreshold() method in the ImageProcessor class. 
		//
		// From: Tim Morris (dtm@ap.co.umist.ac.uk)
		// Subject: Re: Thresholding method?
		// posted to sci.image.processing on 1996/06/24
		// The algorithm implemented in NIH Image sets the threshold as that grey
		// value, G, for which the average of the averages of the grey values
		// below and above G is equal to G. It does this by initialising G to the
		// lowest sensible value and iterating:

		// L = the average grey value of pixels with intensities < G
		// H = the average grey value of pixels with intensities > G
		// is G = (L + H)/2?
		// yes => exit
		// no => increment G and repeat
		//
		int i, l, toth, totl, h, g=0;
		for (i = 1; i < 256; i++){
			if (data[i] > 0){
				g = i + 1;
				break;
			}
		}
		while (true){
			l = 0;
			totl = 0;
			for (i = 0; i < g; i++) {
				 totl = totl + data[i];
				 l = l + (data[i] * i);
			}
			h = 0;
			toth = 0;
			for (i = g + 1; i < 256; i++){
				toth += data[i];
				h += (data[i]*i);
			}
			if (totl > 0 && toth > 0){
				l /= totl;
				h /= toth;
				if (g == (int) Math.round((l + h) / 2.0))
					break;
			}
			g++;
			if (g > 254)
				return -1;
		}
		return g;
	}
	
	int defaultIsoData(int[] data) {
		// This is the modified IsoData method used by the "Threshold" widget in "Default" mode
		int n = data.length;
		int[] data2 = new int[n];
		int mode=0, maxCount=0;
		for (int i=0; i<n; i++) {
			int count = data[i];
			data2[i] = data[i];
			if (data2[i]>maxCount) {
				maxCount = data2[i];
				mode = i;
			}
		}
		int maxCount2 = 0;
		for (int i = 0; i<n; i++) {
			if ((data2[i]>maxCount2) && (i!=mode))
				maxCount2 = data2[i];
		}
		int hmax = maxCount;
		if ((hmax>(maxCount2*2)) && (maxCount2!=0)) {
			hmax = (int)(maxCount2 * 1.5);
			data2[mode] = hmax;
		}
		return IJIsoData(data2);
	}

	int IJIsoData(int[] data) {
		// This is the original ImageJ IsoData implementation, here for backward compatibility.
		int level;
		int maxValue = data.length - 1;
		double result, sum1, sum2, sum3, sum4;
		int count0 = data[0];
		data[0] = 0; //set to zero so erased areas aren't included
		int countMax = data[maxValue];
		data[maxValue] = 0;
		int min = 0;
		while ((data[min]==0) && (min<maxValue))
			min++;
		int max = maxValue;
		while ((data[max]==0) && (max>0))
			max--;
		if (min>=max) {
			data[0]= count0; data[maxValue]=countMax;
			level = data.length/2;
			return level;
		}
		int movingIndex = min;
		int inc = Math.max(max/40, 1);
		do {
			sum1=sum2=sum3=sum4=0.0;
			for (int i=min; i<=movingIndex; i++) {
				sum1 += i*data[i];
				sum2 += data[i];
			}
			for (int i=(movingIndex+1); i<=max; i++) {
				sum3 += i*data[i];
				sum4 += data[i];
			}			
			result = (sum1/sum2 + sum3/sum4)/2.0;
			movingIndex++;
		} while ((movingIndex+1)<=result && movingIndex<max-1);
		data[0]= count0; data[maxValue]=countMax;
		level = (int)Math.round(result);
		return level;
	}


	int Li(int [] data ) {
		// Implements Li's Minimum Cross Entropy thresholding method
		// This implementation is based on the iterative version (Ref. 2) of the algorithm.
		// 1) Li C.H. and Lee C.K. (1993) "Minimum Cross Entropy Thresholding" 
		//    Pattern Recognition, 26(4): 617-625
		// 2) Li C.H. and Tam P.K.S. (1998) "An Iterative Algorithm for Minimum 
		//    Cross Entropy Thresholding"Pattern Recognition Letters, 18(8): 771-776
		// 3) Sezgin M. and Sankur B. (2004) "Survey over Image Thresholding 
		//    Techniques and Quantitative Performance Evaluation" Journal of 
		//    Electronic Imaging, 13(1): 146-165 
		//    http://citeseer.ist.psu.edu/sezgin04survey.html
		// Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines
		int threshold;
		int ih;
		int num_pixels;
		int sum_back; /* sum of the background pixels at a given threshold */
		int sum_obj;  /* sum of the object pixels at a given threshold */
		int num_back; /* number of background pixels at a given threshold */
		int num_obj;  /* number of object pixels at a given threshold */
		double old_thresh;
		double new_thresh;
		double mean_back; /* mean of the background pixels at a given threshold */
		double mean_obj;  /* mean of the object pixels at a given threshold */
		double mean;  /* mean gray-level in the image */
		double tolerance; /* threshold tolerance */
		double temp;

		tolerance=0.5;
		num_pixels = 0;
		for (ih = 0; ih < 256; ih++ ) 
			num_pixels += data[ih];

		/* Calculate the mean gray-level */
		mean = 0.0;
		for ( ih = 0 + 1; ih < 256; ih++ ) //0 + 1?
			mean += ih * data[ih];
		mean /= num_pixels;
		/* Initial estimate */
		new_thresh = mean;

		do {
			old_thresh = new_thresh;
			threshold = (int) (old_thresh + 0.5);	/* range */
			/* Calculate the means of background and object pixels */
			/* Background */
			sum_back = 0;
			num_back = 0;
			for ( ih = 0; ih <= threshold; ih++ ) {
				sum_back += ih * data[ih];
				num_back += data[ih];
			}
			mean_back = ( num_back == 0 ? 0.0 : ( sum_back / ( double ) num_back ) );
			/* Object */
			sum_obj = 0;
			num_obj = 0;
			for ( ih = threshold + 1; ih < 256; ih++ ) {
				sum_obj += ih * data[ih];
				num_obj += data[ih];
			}
			mean_obj = ( num_obj == 0 ? 0.0 : ( sum_obj / ( double ) num_obj ) );

			/* Calculate the new threshold: Equation (7) in Ref. 2 */
			//new_thresh = simple_round ( ( mean_back - mean_obj ) / ( Math.log ( mean_back ) - Math.log ( mean_obj ) ) );
			//simple_round ( double x ) {
			// return ( int ) ( IS_NEG ( x ) ? x - .5 : x + .5 );
			//}
			//
			//#define IS_NEG( x ) ( ( x ) < -DBL_EPSILON ) 
			//DBL_EPSILON = 2.220446049250313E-16
			temp = ( mean_back - mean_obj ) / ( Math.log ( mean_back ) - Math.log ( mean_obj ) );

			if (temp < -2.220446049250313E-16)
				new_thresh = (int) (temp - 0.5);
			else
				new_thresh = (int) (temp + 0.5);
			/*  Stop the iterations when the difference between the
			new and old threshold values is less than the tolerance */
		}
		while ( Math.abs ( new_thresh - old_thresh ) > tolerance );
		return threshold;
	}

	int MaxEntropy(int [] data ) {
		// Implements Kapur-Sahoo-Wong (Maximum Entropy) thresholding method
		// Kapur J.N., Sahoo P.K., and Wong A.K.C. (1985) "A New Method for
		// Gray-Level Picture Thresholding Using the Entropy of the Histogram"
		// Graphical Models and Image Processing, 29(3): 273-285
		// M. Emre Celebi
		// 06.15.2007
		// Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines
		int threshold=-1;
		int ih, it;
		int first_bin;
		int last_bin;
		double tot_ent;  /* total entropy */
		double max_ent;  /* max entropy */
		double ent_back; /* entropy of the background pixels at a given threshold */
		double ent_obj;  /* entropy of the object pixels at a given threshold */
		double [] norm_histo = new double[256]; /* normalized histogram */
		double [] P1 = new double[256]; /* cumulative normalized histogram */
		double [] P2 = new double[256]; 

		int total =0;
		for (ih = 0; ih < 256; ih++ ) 
			total+=data[ih];

		for (ih = 0; ih < 256; ih++ )
			norm_histo[ih] = (double)data[ih]/total;

		P1[0]=norm_histo[0];
		P2[0]=1.0-P1[0];
		for (ih = 1; ih < 256; ih++ ){
			P1[ih]= P1[ih-1] + norm_histo[ih];
			P2[ih]= 1.0 - P1[ih];
		}

		/* Determine the first non-zero bin */
		first_bin=0;
		for (ih = 0; ih < 256; ih++ ) {
			if ( !(Math.abs(P1[ih])<2.220446049250313E-16)) {
				first_bin = ih;
				break;
			}
		}

		/* Determine the last non-zero bin */
		last_bin=255;
		for (ih = 255; ih >= first_bin; ih-- ) {
			if ( !(Math.abs(P2[ih])<2.220446049250313E-16)) {
				last_bin = ih;
				break;
			}
		}

		// Calculate the total entropy each gray-level
		// and find the threshold that maximizes it 
		max_ent = Double.MIN_VALUE;

		for ( it = first_bin; it <= last_bin; it++ ) {
			/* Entropy of the background pixels */
			ent_back = 0.0;
			for ( ih = 0; ih <= it; ih++ )  {
				if ( data[ih] !=0 ) {
					ent_back -= ( norm_histo[ih] / P1[it] ) * Math.log ( norm_histo[ih] / P1[it] );
				}
			}

			/* Entropy of the object pixels */
			ent_obj = 0.0;
			for ( ih = it + 1; ih < 256; ih++ ){
				if (data[ih]!=0){
				ent_obj -= ( norm_histo[ih] / P2[it] ) * Math.log ( norm_histo[ih] / P2[it] );
				}
			}

			/* Total entropy */
			tot_ent = ent_back + ent_obj;

			// IJ.log(""+max_ent+"  "+tot_ent);
			if ( max_ent < tot_ent ) {
				max_ent = tot_ent;
				threshold = it;
			}
		}
		return threshold;
	}

	int Mean(int [] data ) {
		// C. A. Glasbey, "An analysis of histogram-based thresholding algorithms,"
		// CVGIP: Graphical Models and Image Processing, vol. 55, pp. 532-537, 1993.
		//
		// The threshold is the mean of the greyscale data
		int threshold = -1;
		double tot=0, sum=0;
		for (int i=0; i<256; i++){
			tot+= data[i];
			sum+=(i*data[i]);
		}
		threshold =(int) Math.floor(sum/tot);
		return threshold;
	}

	int MinErrorI(int [] data ) {
		  // Kittler and J. Illingworth, "Minimum error thresholding," Pattern Recognition, vol. 19, pp. 41-47, 1986.
		 // C. A. Glasbey, "An analysis of histogram-based thresholding algorithms," CVGIP: Graphical Models and Image Processing, vol. 55, pp. 532-537, 1993.
		// Ported to ImageJ plugin by G.Landini from Antti Niemisto's Matlab code (GPL)
		// Original Matlab code Copyright (C) 2004 Antti Niemisto
		// See http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide presentation
		// and the original Matlab code.

		int threshold =  Mean(data); //Initial estimate for the threshold is found with the MEAN algorithm.
		int Tprev =-2;
		double mu, nu, p, q, sigma2, tau2, w0, w1, w2, sqterm, temp;
		//int counter=1;
		while (threshold!=Tprev){
			//Calculate some statistics.
			mu = B(data, threshold)/A(data, threshold);
			nu = (B(data, 255)-B(data, threshold))/(A(data, 255)-A(data, threshold));
			p = A(data, threshold)/A(data, 255);
			q = (A(data, 255)-A(data, threshold)) / A(data, 255);
			sigma2 = C(data, threshold)/A(data, threshold)-(mu*mu);
			tau2 = (C(data, 255)-C(data, threshold)) / (A(data, 255)-A(data, threshold)) - (nu*nu);

			//The terms of the quadratic equation to be solved.
			w0 = 1.0/sigma2-1.0/tau2;
			w1 = mu/sigma2-nu/tau2;
			w2 = (mu*mu)/sigma2 - (nu*nu)/tau2 + Math.log10((sigma2*(q*q))/(tau2*(p*p)));

			//If the next threshold would be imaginary, return with the current one.
			sqterm = (w1*w1)-w0*w2;
			if (sqterm < 0) {
				IJ.log("MinError(I): not converging.");
				return threshold;
			}

			//The updated threshold is the integer part of the solution of the quadratic equation.
			Tprev = threshold;
			temp = (w1+Math.sqrt(sqterm))/w0;

			if ( Double.isNaN(temp)) {
				IJ.log ("MinError(I): NaN, not converging.");
				threshold = Tprev;
			}
			else
				threshold =(int) Math.floor(temp);
			//IJ.log("Iter: "+ counter+++"  t:"+threshold);
		}
		return threshold;
	}

	double A(int [] y, int j) {
		double x = 0;
		for (int i=0;i<=j;i++)
			x+=y[i];
		return x;
	}

	double B(int [] y, int j) {
		double x = 0;
		for (int i=0;i<=j;i++)
			x+=i*y[i];
		return x;
	}

	double C(int [] y, int j) {
		double x = 0;
		for (int i=0;i<=j;i++)
			x+=i*i*y[i];
		return x;
	}

	int Minimum(int [] data ) {
		// J. M. S. Prewitt and M. L. Mendelsohn, "The analysis of cell images," in
		// Annals of the New York Academy of Sciences, vol. 128, pp. 1035-1053, 1966.
		// ported to ImageJ plugin by G.Landini from Antti Niemisto's Matlab code (GPL)
		// Original Matlab code Copyright (C) 2004 Antti Niemisto
		// See http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide presentation
		// and the original Matlab code.
		//
		// Assumes a bimodal histogram. The histogram needs is smoothed (using a
		// running average of size 3, iteratively) until there are only two local maxima.
		// Threshold t is such that yt−1 > yt ≤ yt+1.
		// Images with histograms having extremely unequal peaks or a broad and
		// ﬂat valley are unsuitable for this method.
		int iter =0;
		int threshold = -1;
		double [] iHisto = new double [256];

		for (int i=0; i<256; i++)
			iHisto[i]=(double) data[i];

		double [] tHisto = iHisto;

		while (!bimodalTest(iHisto) ) {
			 //smooth with a 3 point running mean filter
			for (int i=1; i<255; i++)
				tHisto[i]= (iHisto[i-1] + iHisto[i] +iHisto[i+1])/3;
			tHisto[0] = (iHisto[0]+iHisto[1])/3; //0 outside
			tHisto[255] = (iHisto[254]+iHisto[255])/3; //0 outside
			iHisto = tHisto;
			iter++;
			if (iter>10000) {
				threshold = -1;
				IJ.log("Minimum: threshold not found after 10000 iterations.");
				return threshold;
			}
		}
		// The threshold is the minimum between the two peaks.
		for (int i=1; i<255; i++) {
			//IJ.log(" "+i+"  "+iHisto[i]);
			if (iHisto[i-1] > iHisto[i] && iHisto[i+1] >= iHisto[i])
				threshold = i;
		}
		return threshold;
	}

	int Moments(int [] data ) {
		//  W. Tsai, "Moment-preserving thresholding: a new approach," Computer Vision,
		// Graphics, and Image Processing, vol. 29, pp. 377-393, 1985.
		// Ported to ImageJ plugin by G.Landini from the the open source project FOURIER 0.8
		// by  M. Emre Celebi , Department of Computer Science,  Louisiana State University in Shreveport
		// Shreveport, LA 71115, USA
		//  http://sourceforge.net/projects/fourier-ipal
		//  http://www.lsus.edu/faculty/~ecelebi/fourier.htm
		double total =0;
		double m0=1.0, m1=0.0, m2 =0.0, m3 =0.0, sum =0.0, p0=0.0;
		double cd, c0, c1, z0, z1;	/* auxiliary variables */
		int threshold = -1;

		double [] histo = new  double [256];

		for (int i=0; i<256; i++)
			total+=data[i];

		for (int i=0; i<256; i++)
			histo[i]=(double)(data[i]/total); //normalised histogram

		/* Calculate the first, second, and third order moments */
		for ( int i = 0; i < 256; i++ ){
			m1 += i * histo[i];
			m2 += i * i * histo[i];
			m3 += i * i * i * histo[i];
		}
		/* 
		First 4 moments of the gray-level image should match the first 4 moments
		of the target binary image. This leads to 4 equalities whose solutions 
		are given in the Appendix of Ref. 1 
		*/
		cd = m0 * m2 - m1 * m1;
		c0 = ( -m2 * m2 + m1 * m3 ) / cd;
		c1 = ( m0 * -m3 + m2 * m1 ) / cd;
		z0 = 0.5 * ( -c1 - Math.sqrt ( c1 * c1 - 4.0 * c0 ) );
		z1 = 0.5 * ( -c1 + Math.sqrt ( c1 * c1 - 4.0 * c0 ) );
		p0 = ( z1 - m1 ) / ( z1 - z0 );  /* Fraction of the object pixels in the target binary image */

		// The threshold is the gray-level closest  
		// to the p0-tile of the normalized histogram 
		sum=0;
		for (int i=0; i<256; i++){
			sum+=histo[i];
			if (sum>p0) {
				threshold = i;
				break;
			}
		}
		return threshold;
	}

	int Otsu(int [] data ) {
		// Otsu's threshold algorithm
		// C++ code by Jordan Bevik <Jordan.Bevic@qtiworld.com>
		// ported to ImageJ plugin by G.Landini
		int k,kStar;  // k = the current threshold; kStar = optimal threshold
		int N1, N;    // N1 = # points with intensity <=k; N = total number of points
		double BCV, BCVmax; // The current Between Class Variance and maximum BCV
		double num, denom;  // temporary bookeeping
		int Sk;  // The total intensity for all histogram points <=k
		int S, L=256; // The total intensity of the image

		// Initialize values:
		S = N = 0;
		for (k=0; k<L; k++){
			S += k * data[k];	// Total histogram intensity
			N += data[k];		// Total number of data points
		}

		Sk = 0;
		N1 = data[0]; // The entry for zero intensity
		BCV = 0;
		BCVmax=0;
		kStar = 0;

		// Look at each possible threshold value,
		// calculate the between-class variance, and decide if it's a max
		for (k=1; k<L-1; k++) { // No need to check endpoints k = 0 or k = L-1
			Sk += k * data[k];
			N1 += data[k];

			// The float casting here is to avoid compiler warning about loss of precision and
			// will prevent overflow in the case of large saturated images
			denom = (double)( N1) * (N - N1); // Maximum value of denom is (N^2)/4 =  approx. 3E10

			if (denom != 0 ){
				// Float here is to avoid loss of precision when dividing
				num = ( (double)N1 / N ) * S - Sk; 	// Maximum value of num =  255*N = approx 8E7
				BCV = (num * num) / denom;
			}
			else
				BCV = 0;

			if (BCV >= BCVmax){ // Assign the best threshold found so far
				BCVmax = BCV;
				kStar = k;
			}
		}
		// kStar += 1;	// Use QTI convention that intensity -> 1 if intensity >= k
		// (the algorithm was developed for I-> 1 if I <= k.)
		return kStar;
	}


	int Percentile(int [] data ) {
		// W. Doyle, "Operation useful for similarity-invariant pattern recognition,"
		// Journal of the Association for Computing Machinery, vol. 9,pp. 259-267, 1962.
		// ported to ImageJ plugin by G.Landini from Antti Niemisto's Matlab code (GPL)
		// Original Matlab code Copyright (C) 2004 Antti Niemisto
		// See http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide presentation
		// and the original Matlab code.

		int iter =0;
		int threshold = -1;
		double ptile= 0.5; // default fraction of foreground pixels
		double [] avec = new double [256];

		for (int i=0; i<256; i++)
			avec[i]=0.0;

		double total =partialSum(data, 255);
		double temp = 1.0;
		for (int i=0; i<256; i++){
			avec[i]=Math.abs((partialSum(data, i)/total)-ptile);
			//IJ.log("Ptile["+i+"]:"+ avec[i]);
			if (avec[i]<temp) {
				temp = avec[i];
				threshold = i;
			}
		}
		return threshold;
	}


	double partialSum(int [] y, int j) {
		double x = 0;
		for (int i=0;i<=j;i++)
			x+=y[i];
		return x;
	}


	int RenyiEntropy(int [] data ) {
		// Kapur J.N., Sahoo P.K., and Wong A.K.C. (1985) "A New Method for
		// Gray-Level Picture Thresholding Using the Entropy of the Histogram"
		// Graphical Models and Image Processing, 29(3): 273-285
		// M. Emre Celebi
		// 06.15.2007
		// Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines

		int threshold; 
		int opt_threshold;

		int ih, it;
		int first_bin;
		int last_bin;
		int tmp_var;
		int t_star1, t_star2, t_star3;
		int beta1, beta2, beta3;
		double alpha;/* alpha parameter of the method */
		double term;
		double tot_ent;  /* total entropy */
		double max_ent;  /* max entropy */
		double ent_back; /* entropy of the background pixels at a given threshold */
		double ent_obj;  /* entropy of the object pixels at a given threshold */
		double omega;
		double [] norm_histo = new double[256]; /* normalized histogram */
		double [] P1 = new double[256]; /* cumulative normalized histogram */
		double [] P2 = new double[256]; 

		int total =0;
		for (ih = 0; ih < 256; ih++ ) 
			total+=data[ih];

		for (ih = 0; ih < 256; ih++ )
			norm_histo[ih] = (double)data[ih]/total;

		P1[0]=norm_histo[0];
		P2[0]=1.0-P1[0];
		for (ih = 1; ih < 256; ih++ ){
			P1[ih]= P1[ih-1] + norm_histo[ih];
			P2[ih]= 1.0 - P1[ih];
		}

		/* Determine the first non-zero bin */
		first_bin=0;
		for (ih = 0; ih < 256; ih++ ) {
			if ( !(Math.abs(P1[ih])<2.220446049250313E-16)) {
				first_bin = ih;
				break;
			}
		}

		/* Determine the last non-zero bin */
		last_bin=255;
		for (ih = 255; ih >= first_bin; ih-- ) {
			if ( !(Math.abs(P2[ih])<2.220446049250313E-16)) {
				last_bin = ih;
				break;
			}
		}

		/* Maximum Entropy Thresholding - BEGIN */
		/* ALPHA = 1.0 */
		/* Calculate the total entropy each gray-level
		and find the threshold that maximizes it 
		*/
		threshold =0; // was MIN_INT in original code, but if an empty image is processed it gives an error later on.
		max_ent = 0.0;

		for ( it = first_bin; it <= last_bin; it++ ) {
			/* Entropy of the background pixels */
			ent_back = 0.0;
			for ( ih = 0; ih <= it; ih++ )  {
				if ( data[ih] !=0 ) {
					ent_back -= ( norm_histo[ih] / P1[it] ) * Math.log ( norm_histo[ih] / P1[it] );
				}
			}

			/* Entropy of the object pixels */
			ent_obj = 0.0;
			for ( ih = it + 1; ih < 256; ih++ ){
				if (data[ih]!=0){
				ent_obj -= ( norm_histo[ih] / P2[it] ) * Math.log ( norm_histo[ih] / P2[it] );
				}
			}

			/* Total entropy */
			tot_ent = ent_back + ent_obj;

			// IJ.log(""+max_ent+"  "+tot_ent);

			if ( max_ent < tot_ent ) {
				max_ent = tot_ent;
				threshold = it;
			}
		}
		t_star2 = threshold;

		/* Maximum Entropy Thresholding - END */
		threshold =0; //was MIN_INT in original code, but if an empty image is processed it gives an error later on.
		max_ent = 0.0;
		alpha = 0.5;
		term = 1.0 / ( 1.0 - alpha );
		for ( it = first_bin; it <= last_bin; it++ ) {
			/* Entropy of the background pixels */
			ent_back = 0.0;
			for ( ih = 0; ih <= it; ih++ )
				ent_back += Math.sqrt ( norm_histo[ih] / P1[it] );

			/* Entropy of the object pixels */
			ent_obj = 0.0;
			for ( ih = it + 1; ih < 256; ih++ )
				ent_obj += Math.sqrt ( norm_histo[ih] / P2[it] );

			/* Total entropy */
			tot_ent = term * ( ( ent_back * ent_obj ) > 0.0 ? Math.log ( ent_back * ent_obj ) : 0.0);

			if ( tot_ent > max_ent ){
				max_ent = tot_ent;
				threshold = it;
			}
		}

		t_star1 = threshold;

		threshold = 0; //was MIN_INT in original code, but if an empty image is processed it gives an error later on.
		max_ent = 0.0;
		alpha = 2.0;
		term = 1.0 / ( 1.0 - alpha );
		for ( it = first_bin; it <= last_bin; it++ ) {
			/* Entropy of the background pixels */
			ent_back = 0.0;
			for ( ih = 0; ih <= it; ih++ )
				ent_back += ( norm_histo[ih] * norm_histo[ih] ) / ( P1[it] * P1[it] );

			/* Entropy of the object pixels */
			ent_obj = 0.0;
			for ( ih = it + 1; ih < 256; ih++ )
				ent_obj += ( norm_histo[ih] * norm_histo[ih] ) / ( P2[it] * P2[it] );

			/* Total entropy */
			tot_ent = term *( ( ent_back * ent_obj ) > 0.0 ? Math.log(ent_back * ent_obj ): 0.0 );

			if ( tot_ent > max_ent ){
				max_ent = tot_ent;
				threshold = it;
			}
		}

		t_star3 = threshold;

		/* Sort t_star values */
		if ( t_star2 < t_star1 ){
			tmp_var = t_star1;
			t_star1 = t_star2;
			t_star2 = tmp_var;
		}
		if ( t_star3 < t_star2 ){
			tmp_var = t_star2;
			t_star2 = t_star3;
			t_star3 = tmp_var;
		}
		if ( t_star2 < t_star1 ) {
			tmp_var = t_star1;
			t_star1 = t_star2;
			t_star2 = tmp_var;
		}

		/* Adjust beta values */
		if ( Math.abs ( t_star1 - t_star2 ) <= 5 )  {
			if ( Math.abs ( t_star2 - t_star3 ) <= 5 ) {
				beta1 = 1;
				beta2 = 2;
				beta3 = 1;
			}
			else {
				beta1 = 0;
				beta2 = 1;
				beta3 = 3;
			}
		}
		else {
			if ( Math.abs ( t_star2 - t_star3 ) <= 5 ) {
				beta1 = 3;
				beta2 = 1;
				beta3 = 0;
			}
			else {
				beta1 = 1;
				beta2 = 2;
				beta3 = 1;
			}
		}
		//IJ.log(""+t_star1+" "+t_star2+" "+t_star3);
		/* Determine the optimal threshold value */
		omega = P1[t_star3] - P1[t_star1];
		opt_threshold = (int) (t_star1 * ( P1[t_star1] + 0.25 * omega * beta1 ) + 0.25 * t_star2 * omega * beta2  + t_star3 * ( P2[t_star3] + 0.25 * omega * beta3 ));

		return opt_threshold;
	}


	int Shanbhag(int [] data ) {
		// Shanhbag A.G. (1994) "Utilization of Information Measure as a Means of
		//  Image Thresholding" Graphical Models and Image Processing, 56(5): 414-419
		// Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines
		int threshold;
		int ih, it;
		int first_bin;
		int last_bin;
		double term;
		double tot_ent;  /* total entropy */
		double min_ent;  /* max entropy */
		double ent_back; /* entropy of the background pixels at a given threshold */
		double ent_obj;  /* entropy of the object pixels at a given threshold */
		double [] norm_histo = new double[256]; /* normalized histogram */
		double [] P1 = new double[256]; /* cumulative normalized histogram */
		double [] P2 = new double[256]; 

		int total =0;
		for (ih = 0; ih < 256; ih++ ) 
			total+=data[ih];

		for (ih = 0; ih < 256; ih++ )
			norm_histo[ih] = (double)data[ih]/total;

		P1[0]=norm_histo[0];
		P2[0]=1.0-P1[0];
		for (ih = 1; ih < 256; ih++ ){
			P1[ih]= P1[ih-1] + norm_histo[ih];
			P2[ih]= 1.0 - P1[ih];
		}

		/* Determine the first non-zero bin */
		first_bin=0;
		for (ih = 0; ih < 256; ih++ ) {
			if ( !(Math.abs(P1[ih])<2.220446049250313E-16)) {
				first_bin = ih;
				break;
			}
		}

		/* Determine the last non-zero bin */
		last_bin=255;
		for (ih = 255; ih >= first_bin; ih-- ) {
			if ( !(Math.abs(P2[ih])<2.220446049250313E-16)) {
				last_bin = ih;
				break;
			}
		}

		// Calculate the total entropy each gray-level
		// and find the threshold that maximizes it 
		threshold =-1;
		min_ent = Double.MAX_VALUE;

		for ( it = first_bin; it <= last_bin; it++ ) {
			/* Entropy of the background pixels */
			ent_back = 0.0;
			term = 0.5 / P1[it];
			for ( ih = 1; ih <= it; ih++ )  { //0+1?
				ent_back -= norm_histo[ih] * Math.log ( 1.0 - term * P1[ih - 1] );
			}
			ent_back *= term;

			/* Entropy of the object pixels */
			ent_obj = 0.0;
			term = 0.5 / P2[it];
			for ( ih = it + 1; ih < 256; ih++ ){
				ent_obj -= norm_histo[ih] * Math.log ( 1.0 - term * P2[ih] );
			}
			ent_obj *= term;

			/* Total entropy */
			tot_ent = Math.abs ( ent_back - ent_obj );

			if ( tot_ent < min_ent ) {
				min_ent = tot_ent;
				threshold = it;
			}
		}
		return threshold;
	}


	int Triangle(int [] data ) {
		//  Zack, G. W., Rogers, W. E. and Latt, S. A., 1977,
		//  Automatic Measurement of Sister Chromatid Exchange Frequency,
		// Journal of Histochemistry and Cytochemistry 25 (7), pp. 741-753
		//
		//  modified from Johannes Schindelin plugin
		// 
		// find min and max
		int min = 0, dmax=0, max = 0, min2=0;
		for (int i = 0; i < data.length; i++) {
			if (data[i]>0){
				min=i;
				break;
			}
		}
		if (min>0) min--; // line to the (p==0) point, not to data[min]

		// The Triangle algorithm cannot tell whether the data is skewed to one side or another.
		// This causes a problem as there are 2 possible thresholds between the max and the 2 extremes
		// of the histogram.
		// Here I propose to find out to which side of the max point the data is furthest, and use that as
		//  the other extreme.
		for (int i = 255; i >0; i-- ) {
			if (data[i]>0){
				min2=i;
				break;
			}
		}
		if (min2<255) min2++; // line to the (p==0) point, not to data[min]

		for (int i =0; i < 256; i++) {
			if (data[i] >dmax) {
				max=i;
				dmax=data[i];
			}
		}
		// find which is the furthest side
		//IJ.log(""+min+" "+max+" "+min2);
		boolean inverted = false;
		if ((max-min)<(min2-max)){
			// reverse the histogram
			//IJ.log("Reversing histogram.");
			inverted = true;
			int left  = 0;          // index of leftmost element
			int right = 255; // index of rightmost element
			while (left < right) {
				// exchange the left and right elements
				int temp = data[left]; 
				data[left]  = data[right]; 
				data[right] = temp;
				// move the bounds toward the center
				left++;
				right--;
			}
			min=255-min2;
			max=255-max;
		}

		if (min == max){
			//IJ.log("Triangle:  min == max.");
			return min;
		}

		// describe line by nx * x + ny * y - d = 0
		double nx, ny, d;
		// nx is just the max frequency as the other point has freq=0
		nx = data[max];   //-min; // data[min]; //  lowest value bmin = (p=0)% in the image
		ny = min - max;
		d = Math.sqrt(nx * nx + ny * ny);
		nx /= d;
		ny /= d;
		d = nx * min + ny * data[min];

		// find split point
		int split = min;
		double splitDistance = 0;
		for (int i = min + 1; i <= max; i++) {
			double newDistance = nx * i + ny * data[i] - d;
			if (newDistance > splitDistance) {
				split = i;
				splitDistance = newDistance;
			}
		}
		split--;

		if (inverted) {
			// The histogram might be used for something else, so let's reverse it back
			int left  = 0; 
			int right = 255;
			while (left < right) {
				int temp = data[left]; 
				data[left]  = data[right]; 
				data[right] = temp;
				left++;
				right--;
			}
			return (255-split);
		}
		else
			return split;
	}


	int Yen(int [] data ) {
		// Implements Yen  thresholding method
		// 1) Yen J.C., Chang F.J., and Chang S. (1995) "A New Criterion 
		//    for Automatic Multilevel Thresholding" IEEE Trans. on Image 
		//    Processing, 4(3): 370-378
		// 2) Sezgin M. and Sankur B. (2004) "Survey over Image Thresholding 
		//    Techniques and Quantitative Performance Evaluation" Journal of 
		//    Electronic Imaging, 13(1): 146-165
		//    http://citeseer.ist.psu.edu/sezgin04survey.html
		//
		// M. Emre Celebi
		// 06.15.2007
		// Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines
		int threshold;
		int ih, it;
		double crit;
		double max_crit;
		double [] norm_histo = new double[256]; /* normalized histogram */
		double [] P1 = new double[256]; /* cumulative normalized histogram */
		double [] P1_sq = new double[256]; 
		double [] P2_sq = new double[256]; 

		int total =0;
		for (ih = 0; ih < 256; ih++ ) 
			total+=data[ih];

		for (ih = 0; ih < 256; ih++ )
			norm_histo[ih] = (double)data[ih]/total;

		P1[0]=norm_histo[0];
		for (ih = 1; ih < 256; ih++ )
			P1[ih]= P1[ih-1] + norm_histo[ih];

		P1_sq[0]=norm_histo[0]*norm_histo[0];
		for (ih = 1; ih < 256; ih++ )
			P1_sq[ih]= P1_sq[ih-1] + norm_histo[ih] * norm_histo[ih];

		P2_sq[255] = 0.0;
		for ( ih = 254; ih >= 0; ih-- )
			P2_sq[ih] = P2_sq[ih + 1] + norm_histo[ih + 1] * norm_histo[ih + 1];

		/* Find the threshold that maximizes the criterion */
		threshold = -1;
		max_crit = Double.MIN_VALUE;
		for ( it = 0; it < 256; it++ ) {
			crit = -1.0 * (( P1_sq[it] * P2_sq[it] )> 0.0? Math.log( P1_sq[it] * P2_sq[it]):0.0) +  2 * ( ( P1[it] * ( 1.0 - P1[it] ) )>0.0? Math.log(  P1[it] * ( 1.0 - P1[it] ) ): 0.0);
			if ( crit > max_crit ) {
				max_crit = crit;
				threshold = it;
			}
		}
		return threshold;
	}

}

