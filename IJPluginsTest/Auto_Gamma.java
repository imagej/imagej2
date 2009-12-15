import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

/**
 * Auto_Gamma plugin allows to apply a gamma correction to the image.
 * Gamma Formula used is:
 *  y = range* (x/range)^gamma		
 *  range = the image pixels range (example: 65535 for 16-bit images)
 *  x is the pixel value of the image before gamma transformation.
 *  y is the pixel value of the image after gamma transformation (the image result).
 * 
 * This method allows to determine the gamma value according to x and y values.
 * 
 * Formula used is
 *  gamma = log(y/range)/ log(x/range)
 *  By default:
 * 		x is the mean value of pixels of the image
 * 		y is the half value of the range (32767 for 16-bit image)
 * 
 * note: uncheck boxes if you want to use your own values
 * This plugin works for 16-bits greyscale images but the computeGamma method can be applied
 * on others images.
 *  
 * @author Remi Cathelin SIGENAE Team - INRA - cathelin@toulouse.inra.fr
 * @version 2006/01/25
 */
public class Auto_Gamma implements PlugInFilter, Measurements {
	private ImagePlus imp;
	private GenericDialog gd;
	private double xValue = -1.0D;//default value is computed with the image mean
	private double yValue = 32767.0D;// default y is (range 16bits)/2
	private boolean isXauto = true;
	private boolean isYauto = true;
	private double range = 65535.0D;
	
	public int setup(String arg, ImagePlus imp) {
		if (IJ.versionLessThan("1.18o"))
			return DONE;
		this.imp = imp;
		return DOES_ALL;
	}
	
	public void run(ImageProcessor ip) {
		
		switch (imp.getBitDepth()) {
		
		case 16:
			// interface
			gd = new GenericDialog("Auto gamma transformer", IJ.getInstance());
			gd.addCheckbox("automatic x centering (mean)", true);//true = chosen
			gd.addNumericField("     manual: ",0,0);
			gd.addCheckbox("automatic y centering (32767)", true);//true = chosen
			gd.addNumericField("     manual:",0,0);
			gd.showDialog();
			
			if (gd.wasCanceled())
				return;
			
			//get user choices
			// x for gamma serch is automatically defined with the mean or given
			isXauto = gd.getNextBoolean();
			// y for gamma serch is 32767 or given
			isYauto = gd.getNextBoolean();			
			
			if (!isXauto){//x given by the user
				xValue = gd.getNextNumber();
			}
			else gd.getNextNumber();//in order to pass the current number even if not used
			if (!isYauto){//y given by the user
				yValue = gd.getNextNumber();
			}
			else gd.getNextNumber();//in order to pass the current number even if not used
			//end interface
			
			if (isXauto){
				// x used is the mean value of pixels
				int measurements = Analyzer.getMeasurements(); // defined in Set Measurements dialog
				measurements |= MEAN;
				Analyzer a = new Analyzer();	
				ImageStatistics stats = imp.getStatistics(measurements);
				ResultsTable rt =Analyzer.getResultsTable(); // get the system results table
				System.out.println("stats.mean="+stats.mean);
				xValue = stats.mean;
			}
				
			ImagePlus workImage = gammaTransformation(imp,xValue,yValue,range);
			workImage.show();
			
			displayValues();
			break;
			
		default:
			IJ.error("16 bits grayscale image required!");		
		}				
	}
	
	/**
	 * display user's choices
	 */
	private void displayValues() {
		System.out.println("****VALUES****");
		System.out.println("isXauto= "+isXauto);
		System.out.println("isYauto= "+isYauto);
		System.out.println("xValue= "+xValue);
		System.out.println("yValue= "+yValue);
		System.out.println("*************");
	}
	
	/**
	*
	 * @param imagePlus the image to transform
	 * @param x x Value
	 * @param y y Value
	 * @param range the range (for example 65535 for 16-bit images)
	 * @return the gamma modified result ImagePlus
	 * 
	 */
	public static ImagePlus gammaTransformation(ImagePlus imagePlus, double x, double y, double range) {
		ImageProcessor ip1 = imagePlus.getProcessor().duplicate();

		//test ln(32 767 / 65 535) / ln(1 388 / 65 535) = 0,179821713
		//double gammaTest = Math.log(32767.0D/65535.0D)/Math.log(1388.0D/65535.0D);
		//System.out.println("gammaTest="+gammaTest);

		double gammaValue = computeGamma(x,y,range); 
		System.out.println("gammaValue="+gammaValue);
				
		ip1.gamma(gammaValue);//apply gamma
		String name = "gamma_"+gammaValue+"_"+imagePlus.getShortTitle();
		ImagePlus result = new ImagePlus(name,ip1);
		
		return result; 		
	}	
	
	/**
	 * Gamma Formula
	 *  y = range* (x/range)^gamma	
	 * => gamma = log(y/range)/ log(x/range)
	 * @param x 
	 * @param y
	 * @param range (for example 65535 for 16-bit images)
	 * @return gamma 
	 */
	public static double computeGamma(double x, double y,double range){
		return  Math.log(y/range)/Math.log(x/range); 
	}
	
	
	
}

