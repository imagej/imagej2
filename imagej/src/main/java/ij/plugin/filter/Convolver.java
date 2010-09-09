package ij.plugin.filter;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import ij.plugin.TextReader;
import ij.plugin.frame.Recorder;
import ij.util.Tools;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;

/** This plugin convolves images using user user defined kernels. */
public class Convolver implements ExtendedPlugInFilter, DialogListener, ActionListener {

	ImagePlus imp;
	int kw, kh;
	boolean canceled;
	float[] kernel;
	boolean isLineRoi;
	Button open, save;
	GenericDialog gd;
	boolean normalize = true;
	int nSlices;
	int flags = DOES_ALL+CONVERT_TO_FLOAT|SUPPORTS_MASKING|PARALLELIZE_STACKS|KEEP_PREVIEW|FINAL_PROCESSING;
	int nPasses = 1;
	int pass;
	boolean kernelError;
	
	static String kernelText = "-1 -1 -1 -1 -1\n-1 -1 -1 -1 -1\n-1 -1 24 -1 -1\n-1 -1 -1 -1 -1\n-1 -1 -1 -1 -1\n";
	static boolean normalizeFlag = true;

	public int setup(String arg, ImagePlus imp) {
 		this.imp = imp;
		if (imp==null)
			{IJ.noImage(); return DONE;}
		if (arg.equals("final")&&imp.getRoi()==null) {
			imp.getProcessor().resetMinAndMax();
			imp.updateAndDraw();
			return DONE;
		}
		IJ.resetEscape();
		Roi roi = imp.getRoi();
		isLineRoi= roi!=null && roi.isLine();
		nSlices = imp.getStackSize();
		imp.startTiming();
		return flags;
	}

	public void run(ImageProcessor ip) {
		if (canceled) return;
		if (isLineRoi) ip.resetRoi();
		if (!kernelError)
			convolve(ip, kernel, kw, kh);
		if (canceled) Undo.undo();
	}
	
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		gd = new GenericDialog("Convolver...", IJ.getInstance());
		gd.addTextAreas(kernelText, null, 10, 30);
		gd.addPanel(makeButtonPanel(gd));
		gd.addCheckbox("Normalize Kernel", normalizeFlag);
        gd.addPreviewCheckbox(pfr);
        gd.addDialogListener(this);
		gd.showDialog();
		if (gd.wasCanceled()) return DONE;
		return IJ.setupDialog(imp, flags);
	}

    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		kernelText = gd.getNextText();
		normalizeFlag = gd.getNextBoolean();
		normalize = normalizeFlag;
		kernelError = !decodeKernel(kernelText);
		if (!kernelError) {
			IJ.showStatus("Convolve: "+kw+"x"+kh+" kernel");
			return true;
		} else
			return !gd.getPreviewCheckbox().getState();
    }
    
    boolean decodeKernel(String text) {
    	if (Macro.getOptions()!=null && !hasNewLine(text))
    		return decodeSquareKernel(text);
		String[] rows = Tools.split(text, "\n");
		kh = rows.length;
		if (kh==0) return false;
		String[] values = Tools.split(rows[0]);
		kw = values.length;
		kernel = new float[kw*kh];
		boolean done = gd.wasOKed();
		int i = 0;
		for (int y=0; y<kh; y++) {
			values = Tools.split(rows[y]);	
			if (values.length!=kw) {
				String err = "Row "+(y+1)+" is not the same length as the first row";
				if (done)
					IJ.error("Convolver", err);
				else
					IJ.showStatus(err);
				return false;
			}
			for (int x=0; x<kw; x++)
				kernel[i++] = (float)Tools.parseDouble(values[x], 0.0);
		}
		if ((kw&1)!=1 || (kh&1)!=1) {
			String err = "Kernel must have odd width and height. This one is "+kw+"x"+kh+".";
			if (done)
				IJ.error("Convolver", err);
			else
				IJ.showStatus(err);
			return false;
		}
		return true;
    }
    
	boolean hasNewLine(String text) {
		for (int i=0; i<text.length(); i++) {
			if (text.charAt(i)=='\n') return true;
		}
		return false;
	}

    boolean decodeSquareKernel(String text) {
		String[] values = Tools.split(text);
		int n = values.length;
		kw = (int)Math.sqrt(n);
		kh = kw;
		n = kw*kh;
		kernel = new float[n];
		for (int i=0; i<n; i++)
			kernel[i] = (float)Tools.parseDouble(values[i]);
		if (kw>=3 && (kw&1)==1) {
			StringBuffer sb = new StringBuffer();
			int i = 0;
			for (int y=0; y<kh; y++) {
				for (int x=0; x<kw; x++) {
					sb.append(""+kernel[i++]);
					if (x<kw-1) sb.append(" ");
				}
				sb.append("\n");
			}
			kernelText = new String(sb);
			gd.getTextArea1().setText(new String(sb));
			return true;
		} else {
			IJ.error("Kernel must be square with odd width. This one is "+kw+"x"+kh+".");
			return false;
		}
	}
	
	/** Creates a panel containing "Save...", "Save..." and "Preview" buttons. */
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

	/** Convolves <code>ip</code> with a kernel of width <code>kw</code> and
		height <code>kh</code>. Returns false if the user cancels the operation. */
	public boolean convolve(ImageProcessor ip, float[] kernel, int kw, int kh) {
		if (canceled || kw*kh!=kernel.length) return false;
		if ((kw&1)!=1 || (kh&1)!=1)
			throw new IllegalArgumentException("Kernel width or height not odd ("+kw+"x"+kh+")");
		boolean notFloat = !(ip instanceof FloatProcessor);
		ImageProcessor ip2 = ip;
		if (notFloat) {
			if (ip2 instanceof ColorProcessor) 
				throw new IllegalArgumentException("RGB images not supported");
			ip2 = ip2.convertToFloat();
		}
		if (kw==1 || kh==1)
			convolveFloat1D(ip2, kernel, kw, kh);
		else
			convolveFloat(ip2, kernel, kw, kh);
		if (notFloat) {
			if (ip instanceof ByteProcessor)
				ip2 = ip2.convertToByte(false);
			else
				ip2 = ip2.convertToShort(false);
			ip.setPixels(ip2.getPixels());
		}
		return !canceled;
	}
	
	public void setNormalize(boolean normalizeKernel) {
		normalize = normalizeKernel;
	}

	/** Convolves the float image <code>ip</code> with a kernel of width 
		<code>kw</code> and height <code>kh</code>. Returns false if 
		the user cancels the operation by pressing 'Esc'. */
	public boolean convolveFloat(ImageProcessor ip, float[] kernel, int kw, int kh) {
		if (canceled) return false;
		int width = ip.getWidth();
		int height = ip.getHeight();
		Rectangle r = ip.getRoi();
		boolean nonRectRoi = ip.getMask()!=null;
		if (nonRectRoi)
			ip.snapshot();
		int x1 = r.x;
		int y1 = r.y;
		int x2 = x1 + r.width;
		int y2 = y1 + r.height;
		int uc = kw/2;    
		int vc = kh/2;
		float[] pixels = (float[])ip.getPixels();
		float[] pixels2 = (float[])ip.getPixelsCopy();
		double scale = getScale(kernel);

        pass++;
        Thread thread = Thread.currentThread();
        if (pass>nPasses) pass =1;
		double sum;
		int offset, i;
		boolean edgePixel;
		int xedge = width-uc;
		int yedge = height-vc;
		long lastTime = System.currentTimeMillis();
		for(int y=y1; y<y2; y++) {
			long time = System.currentTimeMillis();
			if (time-lastTime>100) {
				lastTime = time;
				if (thread.isInterrupted()) return false;
				if (IJ.escapePressed()) {
					IJ.beep();
					canceled = true;
					ip.reset();
					return false;
				}
				showProgress((y-y1)/(double)(y2-y1));
			}
			for(int x=x1; x<x2; x++) {
				sum = 0.0;
				i = 0;
				edgePixel = y<vc || y>=yedge || x<uc || x>=xedge;
				for(int v=-vc; v <= vc; v++) {
					offset = x+(y+v)*width;
					for(int u = -uc; u <= uc; u++) {
						if (edgePixel) {
 							if (i>=kernel.length) // work around for JIT compiler bug on Linux
 								IJ.log("kernel index error: "+i);
							sum += getPixel(x+u, y+v, pixels2, width, height)*kernel[i++];
						} else
							sum += pixels2[offset+u]*kernel[i++];
					}
		    	}
				pixels[x+y*width] = (float)(sum*scale);
			}
    	}
		if (nonRectRoi)
			ip.reset(ip.getMask());
   		return true;
   	 }

	/** Convolves the image <code>ip</code> with a kernel of width 
		<code>kw</code> and height <code>kh</code>. */
	void convolveFloat1D(ImageProcessor ip, float[] kernel, int kw, int kh) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		Rectangle r = ip.getRoi();
		int x1 = r.x;
		int y1 = r.y;
		int x2 = x1 + r.width;
		int y2 = y1 + r.height;
		int uc = kw/2;    
		int vc = kh/2;
		float[] pixels = (float[])ip.getPixels();
		float[] pixels2 = (float[])ip.getPixelsCopy();
		double scale = getScale(kernel);
		boolean vertical = kw==1;

		double sum;
		int offset, i;
		boolean edgePixel;
		int xedge = width-uc;
		int yedge = height-vc;
		for(int y=y1; y<y2; y++) {
			for(int x=x1; x<x2; x++) {
				sum = 0.0;
				i = 0;
				if (vertical) {
					edgePixel = y<vc || y>=yedge;
					offset = x+(y-vc)*width;
					for(int v=-vc; v<=vc; v++) {
						if (edgePixel)
							sum += getPixel(x+uc, y+v, pixels2, width, height)*kernel[i++];
						else
							sum += pixels2[offset+uc]*kernel[i++];
						offset += width;
					}
				} else {
					edgePixel = x<uc || x>=xedge;
					offset = x+(y-vc)*width;
					for(int u = -uc; u<=uc; u++) {
						if (edgePixel)
							sum += getPixel(x+u, y+vc, pixels2, width, height)*kernel[i++];
						else
							sum += pixels2[offset+u]*kernel[i++];
					}
				}
				pixels[x+y*width] = (float)(sum*scale);
			}
    	}
    }
   	 
	double getScale(float[] kernel) {
		double scale = 1.0;
		if (normalize) {
			double sum = 0.0;
			for (int i=0; i<kernel.length; i++)
				sum += kernel[i];
			if (sum!=0.0)
				scale = (float)(1.0/sum);
		}
		return scale;
	}

	private float getPixel(int x, int y, float[] pixels, int width, int height) {
		if (x<=0) x = 0;
		if (x>=width) x = width-1;
		if (y<=0) y = 0;
		if (y>=height) y = height-1;
		return pixels[x+y*width];
	}
	
	void save() {
		TextArea ta1 = gd.getTextArea1();
		ta1.selectAll();
		String text = ta1.getText();
		ta1.select(0, 0);
		if (text==null || text.length()==0)
			return;
		text += "\n";
		SaveDialog sd = new SaveDialog("Save as Text...", "kernel", ".txt");
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
		pw.print(text);
		pw.close();
	}
	
	void open() {
		OpenDialog od = new OpenDialog("Open Kernel...", "");
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
		if ((width&1)!=1 || (height&1)!=1) {
			IJ.error("Convolver", "Kernel must be have odd width and height");
			return;
		}
		StringBuffer sb = new StringBuffer();
		boolean integers = true;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				double v = ip.getPixelValue(x, y);
				if ((int)v!=v)
					integers = false;
			}
		}
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				if (x!=0) sb.append(" ");
				double v = ip.getPixelValue(x, y);
				if (integers)
					sb.append(IJ.d2s(ip.getPixelValue(x, y),0));
				else
					sb.append(""+ip.getPixelValue(x, y));
			}
			if (y!=height-1)
				sb.append("\n");
		}
		gd.getTextArea1().setText(new String(sb));
	}
	
	public void setNPasses(int nPasses) {
		this.nPasses = nPasses;
		pass = 0;
	}

	void showProgress(double percent) {
		percent = (double)(pass-1)/nPasses + percent/nPasses;
		IJ.showProgress(percent);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		Recorder.disablePathRecording();
		if (source==save)
			save();
		else if (source==open)
			open();
	}

}


