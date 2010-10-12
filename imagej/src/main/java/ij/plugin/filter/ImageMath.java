package ij.plugin.filter;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.macro.*;
import imagej.SampleInfo;
import imagej.SampleManager;
import imagej.process.ImgLibProcessor;

import java.awt.*;

/** This plugin implements ImageJ's Process/Math submenu. */
public class ImageMath implements ExtendedPlugInFilter, DialogListener {
	
	public static final String MACRO_KEY = "math.macro";
	private int flags = DOES_ALL|SUPPORTS_MASKING|PARALLELIZE_STACKS|KEEP_PREVIEW;
	private String arg;
	private ImagePlus imp;
	private boolean canceled;	
	private double lower=-1.0, upper=-1.0;
	
	private static double addValue = 25;
	private static double mulValue = 1.25;
	private static double minValue = 0;
	private static double maxValue = 255;
	private static final String defaultAndValue = "11110000";
	private static String andValue = defaultAndValue;
	private static final double defaultGammaValue = 0.5;
	private static double gammaValue = defaultGammaValue;
	private static String macro = Prefs.get(MACRO_KEY, "v=v+50*sin(d/10)");
	private int w, h, w2, h2;
	private boolean hasX, hasA, hasD, hasGetPixel;
	private String macro2;
	private PlugInFilterRunner pfr;
	private GenericDialog gd;

	public int setup(String arg, ImagePlus imp) {
		this.arg = arg;
		this.imp = imp;
		IJ.register(ImageMath.class);
		return flags;
	}

	public void run(ImageProcessor ip) {
	 	if (canceled)
	 		return;
	 	
	 	if (arg.equals("add")) {
			ip.add(addValue);
			return;
		}

	 	if (arg.equals("sub")) {
			ip.add(-addValue);
			return;
		}

	 	if (arg.equals("mul")) {
			ip.multiply(mulValue);
			return;
		}

	 	if (arg.equals("div")) {
	 		if (mulValue==0.0&&imp.getBitDepth()!=32)
	 			return;
			ip.multiply(1.0/mulValue);
			return;
		}

	 	if (arg.equals("and")) {
	 		try {
				ip.and(Integer.parseInt(andValue,2));
			} catch (NumberFormatException e) {
				andValue = defaultAndValue;
				IJ.error("Binary number required");
			}
			return;
		}

	 	if (arg.equals("or")) {
	 		try {
				ip.or(Integer.parseInt(andValue,2));
			} catch (NumberFormatException e) {
				andValue = defaultAndValue;
				IJ.error("Binary number required");
			}
			return;
		}
			
	 	if (arg.equals("xor")) {
	 		try {
				ip.xor(Integer.parseInt(andValue,2));
			} catch (NumberFormatException e) {
				andValue = defaultAndValue;
				IJ.error("Binary number required");
			}
			return;
		}
		
	 	if (arg.equals("min")) {
	 		ip.min(minValue);
			if (!(ip instanceof ByteProcessor))
				ip.resetMinAndMax();
			return;
		}

	 	if (arg.equals("max")) {
	 		ip.max(maxValue);
			if (!(ip instanceof ByteProcessor))
				ip.resetMinAndMax();
			return;
		}

	 	if (arg.equals("gamma")) {
	 		if ((gammaValue<0.1 || gammaValue>5.0) && !previewing()) {
	 			IJ.error("Gamma must be between 0.1 and 5.0");
	 			gammaValue = defaultGammaValue;
	 			return;
	 		}
			ip.gamma(gammaValue);
			return;
		}

	 	if (arg.equals("set")) {
	 		boolean rgb = ip instanceof ColorProcessor;
			if (rgb) {
				if (addValue>255.0) addValue=255.0;
				if (addValue<0.0) addValue=0.0;
				int ival = (int)addValue;
	 			ip.setValue(ival + (ival<<8) + (ival<<16));
			} else
	 			ip.setValue(addValue);
			ip.fill();
			return;
		}

	 	if (arg.equals("log")) {
			ip.log();
			return;
		}
		
	 	if (arg.equals("exp")) {
			ip.exp();
			return;
		}

	 	if (arg.equals("sqr")) {
			ip.sqr();
			return;
		}

	 	if (arg.equals("sqrt")) {
			ip.sqrt();
			return;
		}

	 	if (arg.equals("reciprocal")) {
	 		
			if (!isFloat(ip))
				return;
			
			int w = ip.getWidth();
			int h = ip.getHeight();
			
			if (ip instanceof FloatProcessor)
			{
				float[] pixels = (float[])ip.getPixels();
				int totPix = w*h;
				for (int i=0; i<totPix; i++)
				{
					if (pixels[i]==0f)
						pixels[i] = Float.NaN;
					else
						pixels[i] = 1f/pixels[i];
				}
			}
			else if (ip instanceof ImgLibProcessor)
			{
				ImgLibProcessor<?> proc = (ImgLibProcessor<?>) ip;
				for (int x = 0; x < w; x++)
				{
					for (int y = 0; y < h; y++)
					{
						double value = proc.getd(x,y); 
						if (value == 0)
							proc.setd(x, y, Double.NaN);
						else
							proc.setd(x, y, 1.0/value);
					}
				}
			}
			else
				throw new IllegalStateException();
			
			ip.resetMinAndMax();
			
			return;
		}
		
	 	if (arg.equals("nan")) {
	 		setBackgroundToNaN(ip);
			return;
		}

	 	if (arg.equals("abs")) {
			if (!((ip instanceof FloatProcessor)||imp.getCalibration().isSigned16Bit())) {
				IJ.error("32-bit or signed 16-bit image required");
				canceled = true;
			} else {
				ip.abs();
				ip.resetMinAndMax();
			}
			return;
		}

	 	if (arg.equals("macro")) {
			applyMacro(ip);
			return;
		}

	}
	
	boolean previewing() {
		return gd!=null && gd.getPreviewCheckbox().getState();
	}
 
 	private boolean isFloat(ImageProcessor ip) {
		SampleInfo.ValueType vType = SampleManager.getValueType(ip);
		
		if ((vType == SampleInfo.ValueType.FLOAT) || (vType == SampleInfo.ValueType.DOUBLE))
 			return true;
 		
		IJ.error("floating point image required");
		canceled = true;
		return false;
	}
	
	void getValue (String title, String prompt, double defaultValue, int digits) {
		int places = Analyzer.getPrecision();
		if (digits>0 || (int)defaultValue!=defaultValue)
			digits = Math.max(places, 1);
		gd = new GenericDialog(title);
		gd.addNumericField(prompt, defaultValue, digits, 8, null);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
	}

	void getBinaryValue (String title, String prompt, String defaultValue) {
		gd = new GenericDialog(title);
		gd.addStringField(prompt, defaultValue);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
	}

	void getGammaValue (double defaultValue) {
		gd = new GenericDialog("Gamma");
		gd.addSlider("Value:", 0.05, 5.0, defaultValue);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
	}

	/** Set non-thresholded pixels in a float image to NaN. */
	void setBackgroundToNaN(ImageProcessor ip) {
		if (lower==-1.0 && upper==-1.0) {
			lower = ip.getMinThreshold();
			upper = ip.getMaxThreshold();
			if (lower==ImageProcessor.NO_THRESHOLD || !(ip instanceof FloatProcessor)) {
				IJ.error("Thresholded 32-bit float image required");
				canceled = true;
				return;
			}
		}
        float[] pixels = (float[])ip.getPixels();
        int width = ip.getWidth();
        int height = ip.getHeight();
        double v;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                  v = pixels[y*width+x];
                  if (v<lower || v>upper)
                      pixels[y*width+x] = Float.NaN;
            }
        }
		ip.resetMinAndMax();
		return;
	}
	
	// first default: v = v+(sin(x/(w/25))+sin(y/(h/25)))*40
	// a=round(a/10); if (a%2==0) v=0;
	// cone: v=d
	// translate: v=getPixel(x+10,y+10)
	// flip vertically: v=getPixel(x,h-y-1)
	// spiral: v=(sin(d/10+a*PI/180)+1)*128
	// spiral on image: v=v+50*sin(a*PI/180+d/5)
	// spiral rotation: a+=PI+d*PI/360; v=getPixel(d*cos(a)+w/2,d*sin(a)+h/2);
	// v=sin(log(d)*8 + a) * sin(a*8)
	// v=(a * 40.74 + d) % 32
	// v=floor((a * 40.75 + 1) % 2)
	// v=sin(x) * sin(y)
	// v=cos(0.2*x) + sin(0.2*y)

	void applyMacro(ImageProcessor ip) {
		int PCStart = 23;
		if (macro2==null) return;
		if (macro2.indexOf("=")==-1) {
			IJ.error("The variable 'v' must be assigned a value (e.g., \"v=255-v\")");
			canceled = true;
			return;
		}
		macro = macro2;
		Program pgm = (new Tokenizer()).tokenize(macro);
		hasX = pgm.hasWord("x");
		hasA = pgm.hasWord("a");
		hasD = pgm.hasWord("d");
		hasGetPixel = pgm.hasWord("getPixel");
		w = imp.getWidth();
		h = imp.getHeight();
		w2 = w/2;
		h2 = h/2;
		String code =
			"var v,x,y,z,w,h,d,a;\n"+
			"function dummy() {}\n"+
			macro2+";\n"; // code starts at program counter location 'PCStart'
		Interpreter interp = new Interpreter();
		interp.run(code, null);
		if (interp.wasError())
			return;
		Prefs.set(MACRO_KEY, macro);
		interp.setVariable("w", w);
		interp.setVariable("h", h);
		boolean showProgress = pfr.getSliceNumber()==1 && !Interpreter.isBatchMode();
		interp.setVariable("z", pfr.getSliceNumber()-1);
		int bitDepth = imp.getBitDepth();
		Rectangle r = ip.getRoi();
		int inc = r.height/50;
		if (inc<1) inc = 1;
		double v;
		int index, v2;
		if (bitDepth==8) {
			byte[] pixels1 = (byte[])ip.getPixels();
			byte[] pixels2 = pixels1;
			if (hasGetPixel)
				pixels2 = new byte[w*h];
			for (int y=r.y; y<(r.y+r.height); y++) {
				if (showProgress && y%inc==0)
					IJ.showProgress(y-r.y, r.height);
				interp.setVariable("y", y);
				for (int x=r.x; x<(r.x+r.width); x++) {
					index = y*w+x;
					v = pixels1[index]&255;
					interp.setVariable("v", v);
					if (hasX) interp.setVariable("x", x);
					if (hasA) interp.setVariable("a", getA(x,y));
					if (hasD) interp.setVariable("d", getD(x,y));
					interp.run(PCStart);
					v2 = (int)interp.getVariable("v");
					if (v2<0) v2 = 0;
					if (v2>255) v2 = 255;
					pixels2[index] = (byte)v2;
				}
			}
			if (hasGetPixel) System.arraycopy(pixels2, 0, pixels1, 0, w*h);
		} else if (bitDepth==24) {
			int rgb, red, green, blue;
			int[] pixels1 = (int[])ip.getPixels();
			int[] pixels2 = pixels1;
			if (hasGetPixel)
				pixels2 = new int[w*h];
			for (int y=r.y; y<(r.y+r.height); y++) {
				if (showProgress && y%inc==0)
					IJ.showProgress(y-r.y, r.height);
				interp.setVariable("y", y);
				for (int x=r.x; x<(r.x+r.width); x++) {
					if (hasX) interp.setVariable("x", x);
					if (hasA) interp.setVariable("a", getA(x,y));
					if (hasD) interp.setVariable("d", getD(x,y));
					index = y*w+x;
					rgb = pixels1[index];
					if (hasGetPixel) {
						interp.setVariable("v", rgb);
						interp.run(PCStart);
						rgb = (int)interp.getVariable("v");
					} else {
						red = (rgb&0xff0000)>>16;
						green = (rgb&0xff00)>>8;
						blue = rgb&0xff;
						interp.setVariable("v", red);
						interp.run(PCStart);
						red = (int)interp.getVariable("v");
						if (red<0) red=0; if (red>255) red=255;
						interp.setVariable("v", green);
						interp.run(PCStart);
						green= (int)interp.getVariable("v");
						if (green<0) green=0; if (green>255) green=255;
						interp.setVariable("v", blue);
						interp.run(PCStart);
						blue = (int)interp.getVariable("v");
						if (blue<0) blue=0; if (blue>255) blue=255;
						rgb = 0xff000000 | ((red&0xff)<<16) | ((green&0xff)<<8) | blue&0xff;
					}
					pixels2[index] = rgb;
				}
			}
			if (hasGetPixel) System.arraycopy(pixels2, 0, pixels1, 0, w*h);
		} else {
			for (int y=r.y; y<(r.y+r.height); y++) {
				if (showProgress && y%inc==0)
					IJ.showProgress(y-r.y, r.height);
				interp.setVariable("y", y);
				for (int x=r.x; x<(r.x+r.width); x++) {
					v = ip.getPixelValue(x, y);
					interp.setVariable("v", v);
					if (hasX) interp.setVariable("x", x);
					if (hasA) interp.setVariable("a", getA(x,y));
					if (hasD) interp.setVariable("d", getD(x,y));
					interp.run(PCStart);
					ip.putPixelValue(x, y, interp.getVariable("v"));
				}
			}
		}
		if (showProgress)
			IJ.showProgress(1.0);
		if (pfr.getSliceNumber()==1)
			ip.resetMinAndMax();
	}
	
	final double getD(int x, int y) {
          double dx = x - w2; 
          double dy = y - h2;
          return Math.sqrt(dx*dx + dy*dy);
	}
	
	final double getA(int x, int y) {
		double angle = Math.atan2((h-y-1)-h2, x-w2);
		if (angle<0) angle += 2*Math.PI;
		return angle;
	}

	void getMacro(String macro) {
		gd = new GenericDialog("Macro");
		gd.addStringField("Code:", macro, 42);
		gd.setInsets(0,40,0);
		gd.addMessage("v=pixel value, x,y&z=pixel coordinates, w=image width,\nh=image height, a=angle, d=distance from center\n");
		gd.setInsets(5,40,0);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
	}

    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		this.pfr = pfr;
	 	if (arg.equals("macro"))
			getMacro(macro);
    	else if (arg.equals("add"))
	 		getValue("Add", "Value: ", addValue, 0);
	 	else if (arg.equals("sub"))
	 		getValue("Subtract", "Value: ", addValue, 0);
	 	else if (arg.equals("mul"))
	 		getValue("Multiply", "Value: ", mulValue, 2);
	 	else if (arg.equals("div"))
	 		getValue("Divide", "Value: ", mulValue, 2);
	 	else if (arg.equals("and"))
	 		getBinaryValue("AND", "Value (binary): ", andValue);
	 	else if (arg.equals("or"))
	 		getBinaryValue("OR", "Value (binary): ", andValue);
	 	else if (arg.equals("xor"))
	 		getBinaryValue("XOR", "Value (binary): ", andValue);
	 	else if (arg.equals("min"))
	 		getValue("Min", "Value: ", minValue, 0);
	 	else if (arg.equals("max"))
	 		getValue("Max", "Value: ", maxValue, 0);
	 	else if (arg.equals("gamma"))
	 		getGammaValue(gammaValue);
	 	else if (arg.equals("set")) {
	 		boolean rgb = imp.getBitDepth()==24;
	 		String prompt = rgb?"Value (0-255): ":"Value: ";
	 		getValue("Set", prompt, addValue, 0);
		}
		if (gd!=null && gd.wasCanceled())
			return DONE;
		else
			return IJ.setupDialog(imp, flags);
   }

	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
	 	if (arg.equals("macro")) {
	 		String str = gd.getNextString();
			if (previewing() && macro2!=null && !str.equals(macro2))
				gd.getPreviewCheckbox().setState(false);
			macro2 = str;
    	} else if (arg.equals("add")||arg.equals("sub")||arg.equals("set"))
			addValue = gd.getNextNumber();
	 	else if (arg.equals("mul")||arg.equals("div"))
	 		mulValue = gd.getNextNumber();
	 	else if (arg.equals("and")||arg.equals("or")||arg.equals("xor"))
	 		andValue = gd.getNextString();
	 	else if (arg.equals("min"))
	 		minValue = gd.getNextNumber();
	 	else if (arg.equals("max"))
	 		maxValue = gd.getNextNumber();
	 	else if (arg.equals("gamma"))
	 		gammaValue = gd.getNextNumber();
		canceled = gd.invalidNumber();
		if (gd.wasOKed() && canceled) {
			IJ.error("Value is invalid.");
			return false;
		}
		return true;
	}

	public void setNPasses(int nPasses) {
	}

}
