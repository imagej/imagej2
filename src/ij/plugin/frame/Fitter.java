package ij.plugin.frame;
import ijx.IjxImagePlus;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import ij.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import ij.text.*;
import ij.gui.*;
import ij.util.*;
import ij.io.*;
import ij.process.*;
import ij.measure.*;

/** ImageJ plugin that does curve fitting using the modified CurveFitter class.
 *  Includes simplex settings dialog option.
 *
 * @author  Kieran Holland (email: holki659@student.otago.ac.nz)
 */

public class Fitter extends PlugInFrame implements PlugIn, ItemListener, ActionListener {

	Choice fit;
	Button doIt, open, apply;
	Checkbox settings;
	String fitTypeStr = CurveFitter.fitList[0];
	TextArea textArea;

	double[] dx = {0,1,2,3,4,5};
	double[] dy = {0,.9,4.5,8,18,24};
	double[] x,y;

	static CurveFitter cf;
	static int fitType;

	public Fitter() {
		super("Curve Fitter");
		WindowManager.addWindow(this);
		Panel panel = new Panel();
		fit = new Choice();
		for (int i=0; i<CurveFitter.fitList.length; i++)
			fit.addItem(CurveFitter.fitList[i]);
		fit.addItemListener(this);
		panel.add(fit);
		doIt = new Button(" Fit ");
		doIt.addActionListener(this);
		panel.add(doIt);
		open = new Button("Open");
		open.addActionListener(this);
		panel.add(open);
		apply = new Button("Apply");
		apply.addActionListener(this);
		panel.add(apply);
		settings = new Checkbox("Show settings", false);
		panel.add(settings);
		add("North", panel);
		String text = "";
		for (int i=0; i<dx.length; i++)
			text += IJ.d2s(dx[i],2)+"  "+IJ.d2s(dy[i],2)+"\n";
		textArea = new TextArea("",15,30,TextArea.SCROLLBARS_VERTICAL_ONLY);
		//textArea.setBackground(Color.white);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		if (IJ.isLinux()) textArea.setBackground(Color.white);
		textArea.append(text);
		add("Center", textArea);
		pack();
		GUI.center(this);
		show();
		IJ.register(Fitter.class);
	}

	public void doFit(int fitType) {
		this.fitType = fitType;
		if (!getData())
			return;
		cf = new CurveFitter(x, y);
		//double[] params = {0.4, 3.0, 4.6, 44.0};
		//cf.setInitialParameters(params);
		cf.doFit(fitType, settings.getState());
		IJ.log(cf.getResultString());
		plot(cf);
	}
	
	public static void plot(CurveFitter cf) {
		int fit = cf.getFit();
		double[] x = cf.getXPoints();
		double[] y = cf.getYPoints();
		double[] a = Tools.getMinMax(x);
		double xmin=a[0], xmax=a[1]; 
		a = Tools.getMinMax(y);
		double ymin=a[0], ymax=a[1]; 
		float[] px = new float[100];
		float[] py = new float[100];
		double inc = (xmax-xmin)/99.0;
		double tmp = xmin;
		for (int i=0; i<100; i++) {
			px[i]=(float)tmp;
			tmp += inc;
		}
		for (int i=0; i<100; i++)
			py[i] = (float)CurveFitter.f(fit, cf.getParams(), px[i]);
		a = Tools.getMinMax(py);
		ymin = Math.min(ymin, a[0]);
		ymax = Math.max(ymax, a[1]);
		PlotWindow pw = new PlotWindow(cf.fList[fit],"X","Y",px,py);
		pw.setLimits(xmin, xmax, ymin, ymax);
		pw.addPoints(x, y, PlotWindow.CIRCLE);
		double yloc = 0.1;
		double yinc = 0.085;
		pw.addLabel(0.02, yloc, cf.fitList[fit]); yloc+=yinc;
		pw.addLabel(0.02, yloc, cf.fList[fit]);  yloc+=yinc;
        double[] p = cf.getParams();
        int n = cf.getNumParams();
        char pChar = 'a';
        for (int i = 0; i < n; i++) {
			pw.addLabel(0.02, yloc, pChar+"="+IJ.d2s(p[i],4));
			yloc+=yinc;
			pChar++;
        }
		pw.addLabel(0.02, yloc, "R^2="+IJ.d2s(cf.getRSquared(),3));  yloc+=yinc;
		pw.draw();									
	}
	
	double sqr(double x) {return x*x;}
	
	boolean getData() {
		textArea.selectAll();
		String text = textArea.getText();
		text = zapGremlins(text);
		textArea.select(0,0);
		StringTokenizer st = new StringTokenizer(text, " \t\n\r,");
		int nTokens = st.countTokens();
		if (nTokens<4 || (nTokens%2)!=0)
			return false;
		int n = nTokens/2;
		x = new double[n];
		y = new double[n];
		for (int i=0; i<n; i++) {
			x[i] = getNum(st);
			y[i] = getNum(st);
		}
		return true;
	}
	
	void applyFunction() {
		if (cf==null) {
			IJ.error("No function available");
			return;
		}
		IjxImagePlus img = WindowManager.getCurrentImage();
		if (img==null) {
			IJ.noImage();
			return;
		}
		if (img.getTitle().startsWith("y=")) {
			IJ.error("First select the image to be transformed");
			return;
		}
		double[] p = cf.getParams();
		int width = img.getWidth();
		int height = img.getHeight();
		int size = width*height;
		float[] data = new float[size];
		ImageProcessor ip = img.getProcessor();
		float value;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				value = ip.getPixelValue(x,y);
				data[y*width+x] = (float)CurveFitter.f(fitType, p, value);
			}
		}
		ImageProcessor ip2 = new FloatProcessor(width, height, data, ip.getColorModel());
		IJ.getFactory().newImagePlus(img.getTitle()+"-transformed", ip2).show();
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

	void open() {
		OpenDialog od = new OpenDialog("Open Text File...", "");
		String directory = od.getDirectory();
		String name = od.getFileName();
		if (name==null)
			return;
		String path = directory + name;
		textArea.selectAll();
		textArea.setText("");
		try {
			BufferedReader r = new BufferedReader(new FileReader(directory+name));
			while (true) {
				String s=r.readLine();
				if (s==null) break;
				if (s.length()>100) break;
				textArea.append(s+"\n");
			}
		}
		catch (Exception e) {
			IJ.error(e.getMessage());
			return;
		}
	}
	
	public void itemStateChanged(ItemEvent e) {
		fitTypeStr = fit.getSelectedItem();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==doIt)
			doFit(fit.getSelectedIndex());
		else if (e.getSource()==apply)
			applyFunction();
		else
			open();
		//if(e.getSource()==doIt) {
		//	try {doFit(fit.getSelectedIndex());}
		//	catch (Exception ex) {IJ.write(ex.getMessage());}
		//}
	}
	
	String zapGremlins(String text) {
		char[] chars = new char[text.length()];
		chars = text.toCharArray();
		int count=0;
		for (int i=0; i<chars.length; i++) {
			char c = chars[i];
			if (c!='\n' && c!='\t' && (c<32||c>127)) {
				count++;
				chars[i] = ' ';
			}
		}
		if (count>0)
			return new String(chars);
		else
			return text;
	}

}