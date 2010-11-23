package ijx.plugin.filter;
import ijx.process.ImageProcessor;
import ijx.plugin.api.PlugInFilter;
import ijx.gui.Plot;
import ijx.gui.PlotWindow;
import ijx.gui.dialog.GenericDialog;
import ijx.measure.Calibration;
import ijx.measure.CurveFitter;
import ijx.measure.Measurements;
import ijx.util.Tools;
import ijx.io.OpenDialog;
import ijx.io.SaveDialog;
import ijx.io.FileOpener;
import ijx.Macro;
import ijx.WindowManager;
import ijx.IJ;


import ijx.plugin.readwrite.TextReader;
import ijx.IjxImagePlus;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;


/** Implements the Analyze/Calibrate command. */
public class Calibrator implements PlugInFilter, Measurements, ActionListener {

	private static final String NONE = "None";
	private static final String INVERTER = "Pixel Inverter";
	private static final String UNCALIBRATED_OD = "Uncalibrated OD";
	private static final String CUSTOM = "Custom";
	private static boolean showSettings;
	private boolean global1, global2;
    private IjxImagePlus imp;
	private int choiceIndex;
	private String[] functions;
	private	int nFits = CurveFitter.fitList.length;
	private int spacerIndex = nFits+1;
	private int inverterIndex = nFits+2;
	private int odIndex = nFits+3;
	private int customIndex = nFits+4;
	private static String xText = "";
	private static String yText = "";
	private static boolean importedValues;
	private String unit;
	private double lx=0.02, ly=0.1;
	private int oldFunction;
	private String sumResiduals, fitGoodness;
	private Button open, save;
	private GenericDialog gd;
	
	public int setup(String arg, IjxImagePlus imp) {
		this.imp = imp;
		IJ.register(Calibrator.class);
		return DOES_ALL-DOES_RGB+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		global1 = imp.getGlobalCalibration()!=null;
		if (!showDialog(imp))
			return;
		if (choiceIndex==customIndex) {
			showPlot(null, null, imp.getCalibration(), null);
			return;
		} else if (imp.getType()==IjxImagePlus.GRAY32) {
			if (choiceIndex==0)
				imp.getCalibration().setValueUnit(unit);
			else
				IJ.error("Calibrate", "Function must be \"None\" for 32-bit images,\nbut you can change the Unit.");
		} else
			calibrate(imp);
	}

	public boolean showDialog(IjxImagePlus imp) {
		String defaultChoice;
		Calibration cal = imp.getCalibration();
		functions = getFunctionList(cal.getFunction()==Calibration.CUSTOM);
		int function = cal.getFunction();
		oldFunction = function;
		double[] p = cal.getCoefficients();
		unit = cal.getValueUnit();
		if (function==Calibration.NONE)
			defaultChoice=NONE;
		else if (function<nFits&&function==Calibration.STRAIGHT_LINE&&p!=null&& p[0]==255.0&&p[1]==-1.0)
			defaultChoice=INVERTER;
		else if (function<nFits)
			defaultChoice = CurveFitter.fitList[function];
		else if (function==Calibration.UNCALIBRATED_OD)
			defaultChoice=UNCALIBRATED_OD;
		else if (function==Calibration.CUSTOM)
			defaultChoice=CUSTOM;
		else
			defaultChoice=NONE;
			
		String tmpText = getMeans();
		if (!importedValues && !tmpText.equals(""))	
			xText = tmpText;	
		gd = new GenericDialog("Calibrate...");
		gd.addChoice("Function:", functions, defaultChoice);
		gd.addStringField("Unit:", unit, 16);
		gd.addTextAreas(xText, yText, 20, 14);
		//gd.addMessage("Left column contains uncalibrated measured values,\n right column contains known values (e.g., OD).");
		gd.addPanel(makeButtonPanel(gd));
		gd.addCheckbox("Global calibration", global1);
		//gd.addCheckbox("Show Simplex Settings", showSettings);
		gd.addHelp(IJ.URL+"/docs/menus/analyze.html#cal");
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		else {
			choiceIndex = gd.getNextChoiceIndex();
			unit = gd.getNextString();
			xText = gd.getNextText();
			yText = gd.getNextText();
			global2 = gd.getNextBoolean();
			//showSettings = gd.getNextBoolean();
			return true;
		}
	}

	/** Creates a panel containing "Open..." and "Save..." buttons. */
	Panel makeButtonPanel(GenericDialog gd) {
		Panel buttons = new Panel();
    	buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		open = new Button("Open...");
		open.addActionListener(this);
		buttons.add(open);
		save = new Button("Save...");
		save.addActionListener(this);
		buttons.add(save);
		return buttons;
	}
	
	public void calibrate(IjxImagePlus imp) {
		Calibration cal = imp.getCalibration();
		Calibration calOrig = cal.copy();
		int function = Calibration.NONE;
		boolean is16Bits = imp.getType()==IjxImagePlus.GRAY16;
		double[] parameters = null;
		double[] x=null, y=null;
		boolean zeroClip=false;
		if (choiceIndex<=0) {
			if (oldFunction==Calibration.NONE&&!yText.equals("")&&!xText.equals(""))
				IJ.error("Calibrate", "Please select a function");
			function = Calibration.NONE;
		} else if (choiceIndex<=nFits) {
			function = choiceIndex - 1;
			x = getData(xText);
			y = getData(yText);
			if (!cal.calibrated() || y.length!=0 || function!=oldFunction) {
				parameters = doCurveFitting(x, y, function);
				if (parameters==null)
					return;
			}
			if (!is16Bits && function!=Calibration.STRAIGHT_LINE) {
				zeroClip = true;
				for (int i=0; i<y.length; i++)
					if (y[i]<0.0) zeroClip = false;
			}
		} else if (choiceIndex==inverterIndex) {
			function = Calibration.STRAIGHT_LINE;
			parameters = new double[2];
			if (is16Bits)
				parameters[0] = 65535;
			else
				parameters[0] = 255;
			parameters[1] = -1.0;
			unit = "Inverted Gray Value";
		} else if (choiceIndex==odIndex) {
			if (is16Bits) {
				IJ.error("Calibrate", "Uncalibrated OD is not supported on 16-bit images.");
				return;
			}
			function = Calibration.UNCALIBRATED_OD;
			unit = "Uncalibrated OD";
		}
		cal.setFunction(function, parameters, unit, zeroClip);
		if (!cal.equals(calOrig))
			imp.setCalibration(cal);
		imp.setGlobalCalibration(global2?cal:null);
		if (global2 || global2!=global1)
			WindowManager.repaintImageWindows();
		else
			imp.repaintWindow();
		if (global2 && global2!=global1)
			FileOpener.setShowConflictMessage(true);
		if (function!=Calibration.NONE)
			showPlot(x, y, cal, fitGoodness);
	}

	double[] doCurveFitting(double[] x, double[] y, int fitType) {
		if (x.length!=y.length || y.length==0) {
			IJ.error("Calibrate",
				"To create a calibration curve, the left column must\n"
				+"contain a list of measured mean pixel values and the\n"
				+"right column must contain the same number of calibration\n"
				+"standard values. Use the Measure command to add mean\n"
				+"pixel value measurements to the left column.\n"
				+" \n"
				+"    Left column: "+x.length+" values\n"
				+"    Right column: "+y.length+" values\n"
				);
			return null;
		}
		int n = x.length;
		double xmin=0.0,xmax;
		if (imp.getType()==IjxImagePlus.GRAY16)
			xmax=65535.0; 
		else
			xmax=255.0;
		double[] a = Tools.getMinMax(y);
		double ymin=a[0], ymax=a[1]; 
		CurveFitter cf = new CurveFitter(x, y);
		cf.doFit(fitType, showSettings);
		int np = cf.getNumParams();
		double[] p = cf.getParams();
		fitGoodness = IJ.d2s(cf.getRSquared(),6);
		double[] parameters = new double[np];
		for (int i=0; i<np; i++)
			parameters[i] = p[i];
		return parameters;									
	}
	
	void showPlot(double[] x, double[] y, Calibration cal, String rSquared) {
		if (!cal.calibrated() || (IJ.macroRunning()&&Macro.getOptions()!=null))
			return;
		int xmin,xmax,range;
		float[] ctable = cal.getCTable();
		if (ctable.length==256) { //8-bit image
			xmin = 0;
			xmax = 255;
		} else {  // 16-bit image
			xmin = 0;
			xmax = 65535;
		}
		range = 256;
		float[] px = new float[range];
		float[] py = new float[range];
		for (int i=0; i<range; i++)
			px[i]=(float)((i/255.0)*xmax);
		for (int i=0; i<range; i++)
			py[i]=ctable[(int)px[i]];
		double[] a = Tools.getMinMax(py);
		double ymin = a[0];
		double ymax = a[1];
		int fit = cal.getFunction();
		String unit = cal.getValueUnit();
		Plot plot = new Plot("Calibration Function","pixel value",unit,px,py);
		plot.setLimits(xmin,xmax,ymin,ymax);
		if (x!=null&&y!=null&&x.length>0&&y.length>0)
			plot.addPoints(x, y, PlotWindow.CIRCLE);
		double[] p = cal.getCoefficients();
		if (fit<=Calibration.LOG2) {
			drawLabel(plot, CurveFitter.fList[fit]);
			ly += 0.04;
		}
		if (p!=null) {
			int np = p.length;
			drawLabel(plot, "a="+IJ.d2s(p[0],6));
			drawLabel(plot, "b="+IJ.d2s(p[1],6));
			if (np>=3)
				drawLabel(plot, "c="+IJ.d2s(p[2],6));
			if (np>=4)
				drawLabel(plot, "d="+IJ.d2s(p[3],6));
			if (np>=5)
				drawLabel(plot, "e="+IJ.d2s(p[4],6));
			ly += 0.04;
		}
		if (rSquared!=null)
			{drawLabel(plot, "R^2="+rSquared); rSquared=null;}
		plot.show();
	}

	void drawLabel(Plot plot, String label) {
		plot.addLabel(lx, ly, label);
		ly += 0.08;
	}
	

	double sqr(double x) {return x*x;}

	String[] getFunctionList(boolean custom) {
		int n = nFits+4;
		if (custom) n++;
		String[] list = new String[n];
		list[0] = NONE;
		for (int i=0; i<nFits; i++)
			list[1+i] = CurveFitter.fitList[i];
		list[spacerIndex] = "-";
		list[inverterIndex] = INVERTER;
		list[odIndex] = UNCALIBRATED_OD;
		if (custom) 
			list[customIndex] = CUSTOM;
		return list;
 	}
	
	String getMeans() {
		float[] umeans = Analyzer.getUMeans();
		int count = Analyzer.getCounter();
		if (umeans==null || count==0)
			return "";
		if (count>MAX_STANDARDS)
			count = MAX_STANDARDS;
		String s = "";
		for (int i=0; i<count; i++)
			s += IJ.d2s(umeans[i],2)+"\n";
		importedValues = false;
		return s;
	}

	double[] getData(String xData) {
		int len = xData.length();
		StringBuffer sb = new StringBuffer(len);
		for (int i=0; i<len; i++) {
			char c = xData.charAt(i);
			if ((c>='0'&&c<='9') || c=='-'  || c=='.' || c==',' || c=='\n' || c=='\r')
				sb.append(c);
		}
		xData = sb.toString();
		StringTokenizer st = new StringTokenizer(xData);
		int nTokens = st.countTokens();
		if (nTokens<1)
			return new double[0];
		int n = nTokens;
		double data[] = new double[n];
		for (int i=0; i<n; i++) {
			data[i] = getNum(st);
		}
		return data;
	}
	
	double getNum(StringTokenizer st) {
		Double d;
		String token = st.nextToken();
		try {d = new Double(token);}
		catch (NumberFormatException e){d = null;}
		if (d!=null)
			return(d.doubleValue());
		else
			return 0.0;
	}
	
	void save() {
		TextArea ta1 = gd.getTextArea1();
		TextArea ta2 = gd.getTextArea2();
		ta1.selectAll();
		String text1 = ta1.getText();
		ta1.select(0, 0);
		ta2.selectAll();
		String text2 = ta2.getText();
		ta2.select(0, 0);
		double[] x = getData(text1);
		double[] y = getData(text2);
		SaveDialog sd = new SaveDialog("Save as Text...", "calibration", ".txt");
		String name = sd.getFileName();
		if (name == null)
			return;
		String directory = sd.getDirectory();
		PrintWriter pw = null;
		try {
			FileOutputStream fos = new FileOutputStream(directory+name);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			pw = new PrintWriter(bos);
		}
		catch (IOException e) {
			IJ.error("" + e);
			return;
		}
		IJ.wait(250);  // give system time to redraw ImageJ window
		int n = Math.max(x.length, y.length);
		for (int i=0; i<n; i++) {
			String xs = x.length==0?"":i<x.length?""+x[i]:"0";
			String ys = y.length==0?"":i<y.length?""+y[i]:"0";
			pw.println(xs + "\t"+ ys);
		}
		pw.close();
	}
	
	void open() {
		OpenDialog od = new OpenDialog("Open Calibration...", "");
		String directory = od.getDirectory();
		String name = od.getFileName();
		if (name==null)
			return;
		String path = directory + name;
		TextReader tr = new TextReader();
		ImageProcessor ip = tr.open(path);
		if (ip==null)
			return;
		int width = ip.getWidth();
		int height = ip.getHeight();
		if (!((width==1||width==2)&&height>1)) {
			IJ.error("Calibrate", "This appears to not be a one or two column text file");
			return;
		}
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<height; i++) {
			sb.append(""+ip.getPixelValue(0, i));
			sb.append("\n");
		}
		String s1=null, s2=null;
		if (width==2) {
			s1 = new String(sb);
			sb = new StringBuffer();
			for (int i=0; i<height; i++) {
				sb.append(""+ip.getPixelValue(1, i));
				sb.append("\n");
			}
			s2 = new String(sb);
		} else
			s2 = new String(sb);
		if (s1!=null) {
			TextArea ta1 = gd.getTextArea1();
			ta1.selectAll();
			ta1.setText(s1);
		}
		TextArea ta2 = gd.getTextArea2();
		ta2.selectAll();
		ta2.setText(s2);
		importedValues = true;
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source==save)
			save();
		else if (source==open)
			open();
	}

}
