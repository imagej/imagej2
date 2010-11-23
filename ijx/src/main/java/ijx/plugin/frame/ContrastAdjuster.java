package ijx.plugin.frame;
import ijx.plugin.api.PlugInFrame;
import ijx.process.ImageProcessor;
import ijx.process.FloatProcessor;
import ijx.process.StackStatistics;
import ijx.process.ColorProcessor;
import ijx.process.ImageStatistics;
import ijx.process.ByteProcessor;
import ijx.process.ShortProcessor;
import ijx.gui.TrimmedButton;
import ijx.gui.GUI;
import ijx.gui.dialog.YesNoCancelDialog;
import ijx.gui.dialog.GenericDialog;
import ijx.roi.Roi;
import ijx.measure.Calibration;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;
import ijx.Defaults;
import ijx.CompositeImage;
import java.awt.*;
import java.awt.event.*;


import ijx.CentralLookup;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import ijx.app.IjxApplication;
import ijx.app.KeyboardHandler;

/** This plugin implements the Brightness/Contrast, Window/level and
	Color Balance commands, all in the Image/Adjust sub-menu. It 
	allows the user to interactively adjust the brightness  and
	contrast of the active image. It is multi-threaded to 
	provide a more  responsive user interface. */
public class ContrastAdjuster extends PlugInFrame implements Runnable,
	ActionListener, AdjustmentListener, ItemListener {

	public static final String LOC_KEY = "b&c.loc";
	static final int AUTO_THRESHOLD = 5000;
	static final String[] channelLabels = {"Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "All"};
	static final String[] altChannelLabels = {"Channel 1", "Channel 2", "Channel 3", "Channel 4", "Channel 5", "Channel 6", "All"};
	static final int[] channelConstants = {4, 2, 1, 3, 5, 6, 7};
	static final String[] ranges = {"Automatic", "8-bit (0-255)", "10-bit (0-1023)", "12-bit (0-4095)", "15-bit (0-32767)", "16-bit (0-65535)"};
	
	ContrastPlot plot = new ContrastPlot();
	Thread thread;
	private static Frame instance;
		
	int minSliderValue=-1, maxSliderValue=-1, brightnessValue=-1, contrastValue=-1;
	int sliderRange = 256;
	boolean doAutoAdjust,doReset,doSet,doApplyLut;
	
	Panel panel, tPanel;
	Button autoB, resetB, setB, applyB;
	int previousImageID;
	int previousType;
	int previousSlice = 1;
	Object previousSnapshot;
	IjxApplication ij;
	double min, max;
	double previousMin, previousMax;
	double defaultMin, defaultMax;
	int contrast, brightness;
	boolean RGBImage;
	Scrollbar minSlider, maxSlider, contrastSlider, brightnessSlider;
	Label minLabel, maxLabel, windowLabel, levelLabel;
	boolean done;
	int autoThreshold;
	GridBagLayout gridbag;
	GridBagConstraints c;
	int y = 0;
	boolean windowLevel, balance;
	Font monoFont = new Font("Monospaced", Font.PLAIN, 12);
	Font sanFont = new Font("SansSerif", Font.PLAIN, 12);
	int channels = 7; // RGB
	Choice choice;

	public ContrastAdjuster() {
		super("B&C");
	}
	
	public void run(String arg) {
		windowLevel = arg.equals("wl");
		balance = arg.equals("balance");
		if (windowLevel)
			setTitle("W&L");
		else if (balance) {
			setTitle("Color");
			channels = 4;
		}

		if (instance!=null) {
			if (!instance.getTitle().equals(getTitle())) {
				ContrastAdjuster ca = (ContrastAdjuster)instance;
				Prefs.saveLocation(LOC_KEY, ca.getLocation());
				ca.close();
			} else {
				instance.toFront();
				return;
			}
		}
		instance = this;
		IJ.register(ContrastAdjuster.class);
		WindowManager.addWindow(this);

		//ij = IJ.getInstance();
		gridbag = new GridBagLayout();
		c = new GridBagConstraints();
		setLayout(gridbag);
		
		// plot
		c.gridx = 0;
		y = 0;
		c.gridy = y++;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(10, 10, 0, 10);
		gridbag.setConstraints(plot, c);
		add(plot);
		plot.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
		// min and max labels
		
		if (!windowLevel) {
			panel = new Panel();
			c.gridy = y++;
			c.insets = new Insets(0, 10, 0, 10);
			gridbag.setConstraints(panel, c);
			panel.setLayout(new BorderLayout());
			minLabel = new Label("      ", Label.LEFT);
			minLabel.setFont(monoFont);
			panel.add("West", minLabel);
			maxLabel = new Label("      " , Label.RIGHT);
			maxLabel.setFont(monoFont);
			panel.add("East", maxLabel);
			add(panel);
		}

		// min slider
		if (!windowLevel) {
			minSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
			c.gridy = y++;
			c.insets = new Insets(2, 10, 0, 10);
			gridbag.setConstraints(minSlider, c);
			add(minSlider);
			minSlider.addAdjustmentListener(this);
			minSlider.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
			minSlider.setUnitIncrement(1);
			minSlider.setFocusable(false); // prevents blinking on Windows
			addLabel("Minimum", null);
		}

		// max slider
		if (!windowLevel) {
			maxSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
			c.gridy = y++;
			c.insets = new Insets(2, 10, 0, 10);
			gridbag.setConstraints(maxSlider, c);
			add(maxSlider);
			maxSlider.addAdjustmentListener(this);
			maxSlider.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
			maxSlider.setUnitIncrement(1);
			maxSlider.setFocusable(false);
			addLabel("Maximum", null);
		}
		
		// brightness slider
		brightnessSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
		c.gridy = y++;
		c.insets = new Insets(windowLevel?12:2, 10, 0, 10);
		gridbag.setConstraints(brightnessSlider, c);
		add(brightnessSlider);
		brightnessSlider.addAdjustmentListener(this);
		brightnessSlider.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
		brightnessSlider.setUnitIncrement(1);
		brightnessSlider.setFocusable(false);
		if (windowLevel)
			addLabel("Level: ", levelLabel=new TrimmedLabel("        "));
		else
			addLabel("Brightness", null);
			
		// contrast slider
		if (!balance) {
			contrastSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
			c.gridy = y++;
			c.insets = new Insets(2, 10, 0, 10);
			gridbag.setConstraints(contrastSlider, c);
			add(contrastSlider);
			contrastSlider.addAdjustmentListener(this);
			contrastSlider.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
			contrastSlider.setUnitIncrement(1);
			contrastSlider.setFocusable(false);
			if (windowLevel)
				addLabel("Window: ", windowLabel=new TrimmedLabel("        "));
			else
				addLabel("Contrast", null);
		}

		// color channel popup menu
		if (balance) {
			c.gridy = y++;
			c.insets = new Insets(5, 10, 0, 10);
			choice = new Choice();
			addBalanceChoices();
			gridbag.setConstraints(choice, c);
			choice.addItemListener(this);
			//choice.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
			add(choice);
		}
	
		// buttons
		int trim = IJ.isMacOSX()?20:0;
		panel = new Panel();
		panel.setLayout(new GridLayout(0,2, 0, 0));
		autoB = new TrimmedButton("Auto",trim);
		autoB.addActionListener(this);
		autoB.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
		panel.add(autoB);
		resetB = new TrimmedButton("Reset",trim);
		resetB.addActionListener(this);
		resetB.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
		panel.add(resetB);
		setB = new TrimmedButton("Set",trim);
		setB.addActionListener(this);
		setB.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
		panel.add(setB);
		applyB = new TrimmedButton("Apply",trim);
		applyB.addActionListener(this);
		applyB.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
		panel.add(applyB);
		c.gridy = y++;
		c.insets = new Insets(8, 5, 10, 5);
		gridbag.setConstraints(panel, c);
		add(panel);
		
 		addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));  // ImageJ handles keyboard shortcuts
		pack();
		Point loc = Prefs.getLocation(LOC_KEY);
		if (loc!=null)
			setLocation(loc);
		else
			GUI.center(this);
		if (IJ.isMacOSX()) setResizable(false);
		show();

		thread = new Thread(this, "ContrastAdjuster");
		//thread.setPriority(thread.getPriority()-1);
		thread.start();
		setup();
	}
		
	void addBalanceChoices() {
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null && imp.isComposite()) {
			for (int i=0; i<altChannelLabels.length; i++)
				choice.addItem(altChannelLabels[i]);
		} else {
			for (int i=0; i<channelLabels.length; i++)
				choice.addItem(channelLabels[i]);
		}
	}

	void addLabel(String text, Label label2) {
		if (label2==null&&IJ.isMacOSX()) text += "    ";
		panel = new Panel();
		c.gridy = y++;
		int bottomInset = IJ.isMacOSX()?4:0;
		c.insets = new Insets(0, 10, bottomInset, 0);
		gridbag.setConstraints(panel, c);
        panel.setLayout(new FlowLayout(label2==null?FlowLayout.CENTER:FlowLayout.LEFT, 0, 0));
		Label label= new TrimmedLabel(text);
		label.setFont(sanFont);
		panel.add(label);
		if (label2!=null) {
			label2.setFont(monoFont);
			label2.setAlignment(Label.LEFT);
			panel.add(label2);
		}
		add(panel);
	}

	void setup() {
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null) {
			setup(imp);
			updatePlot();
			updateLabels(imp);
			imp.updateAndDraw();
		}
	}
	
	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		Object source = e.getSource();
		if (source==minSlider)
			minSliderValue = minSlider.getValue();
		else if (source==maxSlider)
			maxSliderValue = maxSlider.getValue();
		else if (source==contrastSlider)
			contrastValue = contrastSlider.getValue();
		else
			brightnessValue = brightnessSlider.getValue();
		notify();
	}

	public synchronized  void actionPerformed(ActionEvent e) {
		Button b = (Button)e.getSource();
		if (b==null) return;
		if (b==resetB)
			doReset = true;
		else if (b==autoB)
			doAutoAdjust = true;
		else if (b==setB)
			doSet = true;
		else if (b==applyB)
			doApplyLut = true;
		notify();
	}
	
	ImageProcessor setup(IjxImagePlus imp) {
		Roi roi = imp.getRoi();
		if (roi!=null) roi.endPaste();
		ImageProcessor ip = imp.getProcessor();
		int type = imp.getType();
		int slice = imp.getCurrentSlice();
		RGBImage = type==IjxImagePlus.COLOR_RGB;
		boolean snapshotChanged = RGBImage && previousSnapshot!=null && ((ColorProcessor)ip).getSnapshotPixels()!=previousSnapshot;
		if (imp.getID()!=previousImageID || snapshotChanged || type!=previousType || slice!=previousSlice)
			setupNewImage(imp, ip);
		previousImageID = imp.getID();
	 	previousType = type;
	 	previousSlice = slice;
	 	return ip;
	}

	void setupNewImage(IjxImagePlus imp, ImageProcessor ip)  {
		//IJ.write("setupNewImage");
		previousMin = min;
		previousMax = max;
	 	if (RGBImage) {
	 		ip.snapshot();
	 		previousSnapshot = ((ColorProcessor)ip).getSnapshotPixels();
	 	} else
			previousSnapshot = null;
		double min2 = imp.getDisplayRangeMin();
		double max2 = imp.getDisplayRangeMax();
		if (imp.getType()==IjxImagePlus.COLOR_RGB)
			{min2=0.0; max2=255.0;}
		if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor)) {
			imp.resetDisplayRange();
			defaultMin = imp.getDisplayRangeMin();
			defaultMax = imp.getDisplayRangeMax();
		} else {
			defaultMin = 0;
			defaultMax = 255;
		}
		setMinAndMax(imp, min2, max2);
		min = imp.getDisplayRangeMin();
		max = imp.getDisplayRangeMax();
		if (IJ.debugMode) {
			IJ.log("min: " + min);
			IJ.log("max: " + max);
			IJ.log("defaultMin: " + defaultMin);
			IJ.log("defaultMax: " + defaultMax);
		}
		plot.defaultMin = defaultMin;
		plot.defaultMax = defaultMax;
		//plot.histogram = null;
		int valueRange = (int)(defaultMax-defaultMin);
		int newSliderRange = valueRange;
		if (newSliderRange>640 && newSliderRange<1280)
			newSliderRange /= 2;
		else if (newSliderRange>=1280)
			newSliderRange /= 5;
		if (newSliderRange<256) newSliderRange = 256;
		if (newSliderRange>1024) newSliderRange = 1024;
		double displayRange = max-min;
		if (valueRange>=1280 && valueRange!=0 && displayRange/valueRange<0.25)
			newSliderRange *= 1.6666;
		//IJ.log(valueRange+" "+displayRange+" "+newSliderRange);
		if (newSliderRange!=sliderRange) {
			sliderRange = newSliderRange;
			updateScrollBars(null, true);
		} else
			updateScrollBars(null, false);
		if (balance) {
			if (imp.isComposite()) {
				int channel = imp.getChannel();
				if (channel<=4) {
					choice.select(channel-1);
					channels = channelConstants[channel-1];
				}
				if (choice.getItem(0).equals("Red")) {
					choice.removeAll();
					addBalanceChoices();
				}
			} else { // not composite
				if (choice.getItem(0).equals("Channel 1")) {
					choice.removeAll();
					addBalanceChoices();
				}
			}
		}
		if (!doReset)
			plotHistogram(imp);
		autoThreshold = 0;
		if (imp.isComposite())
			IJ.setKeyUp(KeyEvent.VK_SHIFT);
	}
	
	void setMinAndMax(IjxImagePlus imp, double min, double max) {
		if (channels!=7 && imp.getType()==IjxImagePlus.COLOR_RGB)
			imp.setDisplayRange(min, max, channels);
		else
			imp.setDisplayRange(min, max);
	}

	void updatePlot() {
		plot.min = min;
		plot.max = max;
		plot.repaint();
	}
	
	void updateLabels(IjxImagePlus imp) {
		double min = imp.getDisplayRangeMin();
		double max = imp.getDisplayRangeMax();;
		int type = imp.getType();
		Calibration cal = imp.getCalibration();
		boolean realValue = type==IjxImagePlus.GRAY32;
		if (cal.calibrated()) {
			min = cal.getCValue((int)min);
			max = cal.getCValue((int)max);
			if (type!=IjxImagePlus.GRAY16)
				realValue = true;
		}
		int digits = realValue?2:0;
		if (windowLevel) {
			//IJ.log(min+" "+max);
			double window = max-min;
			double level = min+(window)/2.0;
			windowLabel.setText(IJ.d2s(window, digits));
			levelLabel.setText(IJ.d2s(level, digits));
		} else {
			minLabel.setText(IJ.d2s(min, digits));
			maxLabel.setText(IJ.d2s(max, digits));
		}
	}

	void updateScrollBars(Scrollbar sb, boolean newRange) {
		if (sb==null || sb!=contrastSlider) {
			double mid = sliderRange/2;
			double c = ((defaultMax-defaultMin)/(max-min))*mid;
			if (c>mid)
				c = sliderRange - ((max-min)/(defaultMax-defaultMin))*mid;
			contrast = (int)c;
			if (contrastSlider!=null) {
				if (newRange)
					contrastSlider.setValues(contrast, 1, 0,  sliderRange);
				else
					contrastSlider.setValue(contrast);
			}
		}
		if (sb==null || sb!=brightnessSlider) {
			double level = min + (max-min)/2.0;
			double normalizedLevel = 1.0 - (level - defaultMin)/(defaultMax-defaultMin);
			brightness = (int)(normalizedLevel*sliderRange);
			if (newRange)
				brightnessSlider.setValues(brightness, 1, 0,  sliderRange);
			else
				brightnessSlider.setValue(brightness);
		}
		if (minSlider!=null && (sb==null || sb!=minSlider)) {
			if (newRange)
				minSlider.setValues(scaleDown(min), 1, 0,  sliderRange);
			else
				minSlider.setValue(scaleDown(min));
		}
		if (maxSlider!=null && (sb==null || sb!=maxSlider)) {
			if (newRange)
				maxSlider.setValues(scaleDown(max), 1, 0,  sliderRange);
			else
				maxSlider.setValue(scaleDown(max));
		}
	}
	
	int scaleDown(double v) {
		if (v<defaultMin) v = defaultMin;
		if (v>defaultMax) v = defaultMax;
		return (int)((v-defaultMin)*(sliderRange-1.0)/(defaultMax-defaultMin));
	}
	
	/** Restore image outside non-rectangular roi. */
  	void doMasking(IjxImagePlus imp, ImageProcessor ip) {
		ImageProcessor mask = imp.getMask();
		if (mask!=null)
			ip.reset(mask);
	}

	void adjustMin(IjxImagePlus imp, ImageProcessor ip, double minvalue) {
		min = defaultMin + minvalue*(defaultMax-defaultMin)/(sliderRange-1.0);
		if (max>defaultMax)
			max = defaultMax;
		if (min>max)
			max = min;
		setMinAndMax(imp, min, max);
		if (min==max)
			setThreshold(ip);
		if (RGBImage) doMasking(imp, ip);
		updateScrollBars(minSlider, false);
	}

	void adjustMax(IjxImagePlus imp, ImageProcessor ip, double maxvalue) {
		max = defaultMin + maxvalue*(defaultMax-defaultMin)/(sliderRange-1.0);
		//IJ.log("adjustMax: "+maxvalue+"  "+max);
		if (min<defaultMin)
			min = defaultMin;
		if (max<min)
			min = max;
		setMinAndMax(imp, min, max);
		if (min==max)
			setThreshold(ip);
		if (RGBImage) doMasking(imp, ip);
		updateScrollBars(maxSlider, false);
	}

	void adjustBrightness(IjxImagePlus imp, ImageProcessor ip, double bvalue) {
		double center = defaultMin + (defaultMax-defaultMin)*((sliderRange-bvalue)/sliderRange);
		double width = max-min;
		min = center - width/2.0;
		max = center + width/2.0;
		setMinAndMax(imp, min, max);
		if (min==max)
			setThreshold(ip);
		if (RGBImage) doMasking(imp, ip);
		updateScrollBars(brightnessSlider, false);
	}

	void adjustContrast(IjxImagePlus imp, ImageProcessor ip, int cvalue) {
		double slope;
		double center = min + (max-min)/2.0;
		double range = defaultMax-defaultMin;
		double mid = sliderRange/2;
		if (cvalue<=mid)
			slope = cvalue/mid;
		else
			slope = mid/(sliderRange-cvalue);
		if (slope>0.0) {
			min = center-(0.5*range)/slope;
			max = center+(0.5*range)/slope;
		}
		setMinAndMax(imp, min, max);
		if (RGBImage) doMasking(imp, ip);
		updateScrollBars(contrastSlider, false);
	}

	void reset(IjxImagePlus imp, ImageProcessor ip) {
 		if (RGBImage)
			ip.reset();
		if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor)) {
			imp.resetDisplayRange();
			defaultMin = imp.getDisplayRangeMin();
			defaultMax = imp.getDisplayRangeMax();
			plot.defaultMin = defaultMin;
			plot.defaultMax = defaultMax;
		}
		min = defaultMin;
		max = defaultMax;
		setMinAndMax(imp, min, max);
		updateScrollBars(null, false);
		plotHistogram(imp);
		autoThreshold = 0;
	}

	void plotHistogram(IjxImagePlus imp) {
		ImageStatistics stats;
		if (balance && (channels==4 || channels==2 || channels==1) && imp.getType()==IjxImagePlus.COLOR_RGB) {
			int w = imp.getWidth();
			int h = imp.getHeight();
			byte[] r = new byte[w*h];
			byte[] g = new byte[w*h];
			byte[] b = new byte[w*h];
			((ColorProcessor)imp.getProcessor()).getRGB(r,g,b);
			byte[] pixels=null;
			if (channels==4)
				pixels = r;
			else if (channels==2)
				pixels = g;
			else if (channels==1)
				pixels = b;
			ImageProcessor ip = new ByteProcessor(w, h, pixels, null);
			stats = ImageStatistics.getStatistics(ip, 0, imp.getCalibration());
		} else {
			int range = imp.getType()==IjxImagePlus.GRAY16?Defaults.getDefault16bitRange():0;
			if (range!=0 && imp.getProcessor().getMax()==Math.pow(2,range)-1 && !(imp.getCalibration().isSigned16Bit())) {
				IjxImagePlus imp2 = IJ.getFactory().newImagePlus("Temp", imp.getProcessor());
				stats = new StackStatistics(imp2, 256, 0, Math.pow(2,range));
			} else
				stats = imp.getStatistics();
		}
		Color color = Color.gray;
		if (imp.isComposite() && !(balance&&channels==7))
			color = ((CompositeImage)imp).getChannelColor();
		plot.setHistogram(stats, color);
	}

	void apply(IjxImagePlus imp, ImageProcessor ip) {
		String option = null;
		if (RGBImage)
			imp.unlock();
		if (!imp.lock())
			return;
		if (RGBImage) {
			if (imp.getStackSize()>1)
				applyRGBStack(imp);
			else {
				ip.snapshot();
				reset(imp, ip);
				imp.setChanged(true);
				if (Recorder.record) Recorder.record("run", "Apply LUT");
			}
			imp.unlock();
			return;
		}
		if (imp.isComposite()) {
			imp.unlock();
			((CompositeImage)imp).updateAllChannelsAndDraw();
			return;
		}
		if (imp.getType()!=IjxImagePlus.GRAY8) {
			IJ.beep();
			IJ.showStatus("Apply requires an 8-bit grayscale image or an RGB stack");
			imp.unlock();
			return;
		}
		int[] table = new int[256];
		int min = (int)imp.getDisplayRangeMin();
		int max = (int)imp.getDisplayRangeMax();
		for (int i=0; i<256; i++) {
			if (i<=min)
				table[i] = 0;
			else if (i>=max)
				table[i] = 255;
			else
				table[i] = (int)(((double)(i-min)/(max-min))*255);
		}
		ip.setRoi(imp.getRoi());
		if (imp.getStackSize()>1) {
			IjxImageStack stack = imp.getStack();
			YesNoCancelDialog d = new YesNoCancelDialog(this,
				"Entire Stack?", "Apply LUT to all "+stack.getSize()+" slices in the stack?");
			if (d.cancelPressed())
				{imp.unlock(); return;}
			if (d.yesPressed()) {
				int current = imp.getCurrentSlice();
				ImageProcessor mask = imp.getMask();
				for (int i=1; i<=imp.getStackSize(); i++) {
					imp.setSlice(i);
					ip = imp.getProcessor();
					if (mask!=null) ip.snapshot();
					ip.applyTable(table);
					ip.reset(mask);
				}
				imp.setSlice(current);
				option = "stack";
			} else {
				if (ip.getMask()!=null) ip.snapshot();
				ip.applyTable(table);
				ip.reset(ip.getMask());
				option = "slice";
			}
		} else {
			if (ip.getMask()!=null) ip.snapshot();
			ip.applyTable(table);
			ip.reset(ip.getMask());
		}
		reset(imp, ip);
		imp.setChanged(true);
		imp.unlock();
		if (Recorder.record) {
			if (option!=null)
				Recorder.record("run", "Apply LUT", option);
			else
				Recorder.record("run", "Apply LUT");
		}
	}

	void applyRGBStack(IjxImagePlus imp) {
		int current = imp.getCurrentSlice();
		int n = imp.getStackSize();
		if (!IJ.showMessageWithCancel("Update Entire Stack?",
		"Apply brightness and contrast settings\n"+
		"to all "+n+" slices in the stack?\n \n"+
		"NOTE: There is no Undo for this operation."))
			return;
 		ImageProcessor mask = imp.getMask();
 		Rectangle roi = imp.getRoi()!=null?imp.getRoi().getBounds():null;
 		IjxImageStack stack = imp.getStack();
		for (int i=1; i<=n; i++) {
			IJ.showProgress(i, n);
			IJ.showStatus(i+"/"+n);
			if (i!=current) {
				ImageProcessor ip = stack.getProcessor(i);
				ip.setRoi(roi);
				if (mask!=null) ip.snapshot();
				if (channels!=7)
					((ColorProcessor)ip).setMinAndMax(min, max, channels);
				else
					ip.setMinAndMax(min, max);
				if (mask!=null) ip.reset(mask);
			}
		}
		imp.setStack(null, stack);
		imp.setSlice(current);
		imp.setChanged(true);
		if (Recorder.record)
			Recorder.record("run", "Apply LUT", "stack");
	}

	void setThreshold(ImageProcessor ip) {
		if (!(ip instanceof ByteProcessor))
			return;
		if (((ByteProcessor)ip).isInvertedLut())
			ip.setThreshold(max, 255, ImageProcessor.NO_LUT_UPDATE);
		else
			ip.setThreshold(0, max, ImageProcessor.NO_LUT_UPDATE);
	}

	void autoAdjust(IjxImagePlus imp, ImageProcessor ip) {
 		if (RGBImage)
			ip.reset();
		Calibration cal = imp.getCalibration();
		imp.setCalibration(null);
		ImageStatistics stats = imp.getStatistics(); // get uncalibrated stats
		imp.setCalibration(cal);
		int limit = stats.pixelCount/10;
		int[] histogram = stats.histogram;
		if (autoThreshold<10)
			autoThreshold = AUTO_THRESHOLD;
		else
			autoThreshold /= 2;
		int threshold = stats.pixelCount/autoThreshold;
		int i = -1;
		boolean found = false;
		int count;
		do {
			i++;
			count = histogram[i];
			if (count>limit) count = 0;
			found = count> threshold;
		} while (!found && i<255);
		int hmin = i;
		i = 256;
		do {
			i--;
			count = histogram[i];
			if (count>limit) count = 0;
			found = count > threshold;
		} while (!found && i>0);
		int hmax = i;
		Roi roi = imp.getRoi();
		if (hmax>=hmin) {
			if (RGBImage) imp.killRoi();
			min = stats.histMin+hmin*stats.binSize;
			max = stats.histMin+hmax*stats.binSize;
			if (min==max)
				{min=stats.min; max=stats.max;}
			setMinAndMax(imp, min, max);
			if (RGBImage && roi!=null) imp.setRoi(roi);
		} else {
			reset(imp, ip);
			return;
		}
		updateScrollBars(null, false);
		//if (roi!=null) { ???
		//	ImageProcessor mask = roi.getMask();
		//	if (mask!=null)
		//		ip.reset(mask);
		//}
		if (Recorder.record)
			Recorder.record("run", "Enhance Contrast", "saturated=0.35");
	}
	
	void setMinAndMax(IjxImagePlus imp, ImageProcessor ip) {
		min = imp.getDisplayRangeMin();
		max = imp.getDisplayRangeMax();
		Calibration cal = imp.getCalibration();
		int digits = (ip instanceof FloatProcessor)||cal.calibrated()?2:0;
		double minValue = cal.getCValue(min);
		double maxValue = cal.getCValue(max);
		int channels = imp.getNChannels();
		GenericDialog gd = new GenericDialog("Set Display Range");
		gd.addNumericField("Minimum displayed value: ", minValue, digits);
		gd.addNumericField("Maximum displayed value: ", maxValue, digits);
		gd.addChoice("Unsigned 16-bit range:", ranges, ranges[getRangeIndex()]);
		gd.addCheckbox("Propagate to all open images", false);
		if (imp.isComposite())
			gd.addCheckbox("Propagate to all "+channels+" channels", false);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		minValue = gd.getNextNumber();
		maxValue = gd.getNextNumber();
		minValue = cal.getRawValue(minValue);
		maxValue = cal.getRawValue(maxValue);
		int rangeIndex = gd.getNextChoiceIndex();
		int range1 = Defaults.getDefault16bitRange();
		int range2 = setRange(rangeIndex);
		if (range1!=range2 && imp.getType()==IjxImagePlus.GRAY16 && !cal.isSigned16Bit()) {
			reset(imp, ip);
			minValue = imp.getDisplayRangeMin();
			maxValue = imp.getDisplayRangeMax();
		}
		boolean propagate = gd.getNextBoolean();
		boolean allChannels = imp.isComposite()&&gd.getNextBoolean();
		if (maxValue>=minValue) {
			min = minValue;
			max = maxValue;
			setMinAndMax(imp, min, max);
			updateScrollBars(null, false);
			if (RGBImage) doMasking(imp, ip);
			if (propagate)
				IJ.runMacroFile("ij.jar:PropagateMinAndMax");
			if (allChannels) {
				int channel = imp.getChannel();
				for (int c=1; c<=channels; c++) {
					imp.setPositionWithoutUpdate(c, imp.getSlice(), imp.getFrame());
					imp.setDisplayRange(min, max);
				}
				((CompositeImage)imp).reset();
				imp.setPosition(channel, imp.getSlice(), imp.getFrame());
			}
			if (Recorder.record) {
				if (imp.getBitDepth()==32)
					Recorder.record("setMinAndMax", min, max);
				else {
					int imin = (int)min;
					int imax = (int)max;
					if (cal.isSigned16Bit()) {
						imin = (int)cal.getCValue(imin);
						imax = (int)cal.getCValue(imax);
					}
					Recorder.record("setMinAndMax", imin, imax);
				}
				if (Recorder.scriptMode())
					Recorder.recordCall("IjxImagePlus.setDefault16bitRange("+range2+");");
				else
					Recorder.recordString("call(\"ij.IjxImagePlus.setDefault16bitRange\", "+range2+");\n");

			}
		}
	}
	
	int getRangeIndex() {
		int range = Defaults.getDefault16bitRange();
		int index = 0;
		if (range==8) index = 1;
		else if (range==10) index = 2;
		else if (range==12) index = 3;
		else if (range==15) index = 4;
		else if (range==16) index = 5;
		return index;
	}

	int setRange(int index) {
		int range = 0;
		if (index==1) range = 8;
		else if (index==2) range = 10;
		else if (index==3) range = 12;
		else if (index==4) range = 15;
		else if (index==5) range = 16;
		Defaults.setDefault16bitRange(range);
		return range;
	}

	void setWindowLevel(IjxImagePlus imp, ImageProcessor ip) {
		min = imp.getDisplayRangeMin();
		max = imp.getDisplayRangeMax();
		Calibration cal = imp.getCalibration();
		int digits = (ip instanceof FloatProcessor)||cal.calibrated()?2:0;
		double minValue = cal.getCValue(min);
		double maxValue = cal.getCValue(max);
		//IJ.log("setWindowLevel: "+min+" "+max);
		double windowValue = maxValue - minValue;
		double levelValue = minValue + windowValue/2.0;
		GenericDialog gd = new GenericDialog("Set W&L");
		gd.addNumericField("Window Center (Level): ", levelValue, digits);
		gd.addNumericField("Window Width: ", windowValue, digits);
		gd.addCheckbox("Propagate to all open images", false);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		levelValue = gd.getNextNumber();
		windowValue = gd.getNextNumber();
		minValue = levelValue-(windowValue/2.0);
		maxValue = levelValue+(windowValue/2.0);
		minValue = cal.getRawValue(minValue);
		maxValue = cal.getRawValue(maxValue);
		boolean propagate = gd.getNextBoolean();
		if (maxValue>=minValue) {
			min = minValue;
			max = maxValue;
			setMinAndMax(imp, minValue, maxValue);
			updateScrollBars(null, false);
			if (RGBImage) doMasking(imp, ip);
			if (propagate)
				IJ.runMacroFile("ij.jar:PropagateMinAndMax");
			if (Recorder.record) {
				if (imp.getBitDepth()==32)
					Recorder.record("setMinAndMax", min, max);
				else {
					int imin = (int)min;
					int imax = (int)max;
					if (cal.isSigned16Bit()) {
						imin = (int)cal.getCValue(imin);
						imax = (int)cal.getCValue(imax);
					}
					Recorder.record("setMinAndMax", imin, imax);
				}
			}
		}
	}

	static final int RESET=0, AUTO=1, SET=2, APPLY=3, THRESHOLD=4, MIN=5, MAX=6, 
		BRIGHTNESS=7, CONTRAST=8, UPDATE=9;

	// Separate thread that does the potentially time-consuming processing 
	public void run() {
		while (!done) {
			synchronized(this) {
				try {wait();}
				catch(InterruptedException e) {}
			}
			doUpdate();
		}
	}

	void doUpdate() {
		IjxImagePlus imp;
		ImageProcessor ip;
		int action;
		int minvalue = minSliderValue;
		int maxvalue = maxSliderValue;
		int bvalue = brightnessValue;
		int cvalue = contrastValue;
		if (doReset) action = RESET;
		else if (doAutoAdjust) action = AUTO;
		else if (doSet) action = SET;
		else if (doApplyLut) action = APPLY;
		else if (minSliderValue>=0) action = MIN;
		else if (maxSliderValue>=0) action = MAX;
		else if (brightnessValue>=0) action = BRIGHTNESS;
		else if (contrastValue>=0) action = CONTRAST;
		else return;
		minSliderValue = maxSliderValue = brightnessValue = contrastValue = -1;
		doReset = doAutoAdjust = doSet = doApplyLut = false;
		imp = WindowManager.getCurrentImage();
		if (imp==null) {
			IJ.beep();
			IJ.showStatus("No image");
			return;
		}
		ip = imp.getProcessor();
		if (RGBImage && !imp.lock())
			{imp=null; return;}
		//IJ.write("setup: "+(imp==null?"null":imp.getTitle()));
		switch (action) {
			case RESET:
				reset(imp, ip);
				if (Recorder.record) Recorder.record("resetMinAndMax");
				break;
			case AUTO: autoAdjust(imp, ip); break;
			case SET: if (windowLevel) setWindowLevel(imp, ip); else setMinAndMax(imp, ip); break;
			case APPLY: apply(imp, ip); break;
			case MIN: adjustMin(imp, ip, minvalue); break;
			case MAX: adjustMax(imp, ip, maxvalue); break;
			case BRIGHTNESS: adjustBrightness(imp, ip, bvalue); break;
			case CONTRAST: adjustContrast(imp, ip, cvalue); break;
		}
		updatePlot();
		updateLabels(imp);
		if ((IJ.shiftKeyDown()||(balance&&channels==7)) && imp.isComposite()) {
			((CompositeImage)imp).updateAllChannelsAndDraw();
		} else
			imp.updateChannelAndDraw();
		if (RGBImage)
			imp.unlock();
	}

	public void windowClosing(WindowEvent e) {
	 	close();
		Prefs.saveLocation(LOC_KEY, getLocation());
	}

    /** Overrides close() in PlugInFrame. */
    public boolean close() {
    	super.close();
		instance = null;
		done = true;
		synchronized(this) {
			notify();
		}
        return true;
	}

	public void windowActivated(WindowEvent e) {
		super.windowActivated(e);
		previousImageID = 0; // user may have modified image
		setup();
		WindowManager.setWindow(this);
	}

	public synchronized  void itemStateChanged(ItemEvent e) {
		int index = choice.getSelectedIndex();
		channels = channelConstants[index];
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null && imp.isComposite()) {
			if (index+1<=imp.getNChannels()) 
				imp.setPosition(index+1, imp.getSlice(), imp.getFrame());
			else {
				choice.select(channelLabels.length-1);
				channels = 7;
			}
		} else
			doReset = true;
		notify();
	}

    /** Resets this ContrastAdjuster and brings it to the front. */
    public void updateAndDraw() {
        previousImageID = 0;
        toFront();
    }
    
    /** Updates the ContrastAdjuster. */
    public static void update() {
		if (instance!=null) {
			ContrastAdjuster ca = ((ContrastAdjuster)instance);
			ca.previousImageID = 0;
			ca.setup();
		}
    }
    
} // ContrastAdjuster class


class ContrastPlot extends Canvas implements MouseListener {
	
	static final int WIDTH = 128, HEIGHT=64;
	double defaultMin = 0;
	double defaultMax = 255;
	double min = 0;
	double max = 255;
	int[] histogram;
	int hmax;
	Image os;
	Graphics osg;
	Color color = Color.gray;
	
	public ContrastPlot() {
		addMouseListener(this);
		setSize(WIDTH+1, HEIGHT+1);
	}

    /** Overrides Component getPreferredSize(). Added to work 
    	around a bug in Java 1.4.1 on Mac OS X.*/
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH+1, HEIGHT+1);
    }

	void setHistogram(ImageStatistics stats, Color color) {
		this.color = color;
		histogram = stats.histogram;
		if (histogram.length!=256)
			{histogram=null; return;}
		for (int i=0; i<128; i++)
			histogram[i] = (histogram[2*i]+histogram[2*i+1])/2;
		int maxCount = 0;
		int mode = 0;
		for (int i=0; i<128; i++) {
			if (histogram[i]>maxCount) {
				maxCount = histogram[i];
				mode = i;
			}
		}
		int maxCount2 = 0;
		for (int i=0; i<128; i++) {
			if ((histogram[i]>maxCount2) && (i!=mode))
				maxCount2 = histogram[i];
		}
		hmax = stats.maxCount;
		if ((hmax>(maxCount2*2)) && (maxCount2!=0)) {
			hmax = (int)(maxCount2*1.5);
			histogram[mode] = hmax;
		}
		os = null;
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		int x1, y1, x2, y2;
		double scale = (double)WIDTH/(defaultMax-defaultMin);
		double slope = 0.0;
		if (max!=min)
			slope = HEIGHT/(max-min);
		if (min>=defaultMin) {
			x1 = (int)(scale*(min-defaultMin));
			y1 = HEIGHT;
		} else {
			x1 = 0;
			if (max>min)
				y1 = HEIGHT-(int)((defaultMin-min)*slope);
			else
				y1 = HEIGHT;
		}
		if (max<=defaultMax) {
			x2 = (int)(scale*(max-defaultMin));
			y2 = 0;
		} else {
			x2 = WIDTH;
			if (max>min)
				y2 = HEIGHT-(int)((defaultMax-min)*slope);
			else
				y2 = 0;
		}
		if (histogram!=null) {
			if (os==null && hmax!=0) {
				os = createImage(WIDTH,HEIGHT);
				osg = os.getGraphics();
				osg.setColor(Color.white);
				osg.fillRect(0, 0, WIDTH, HEIGHT);
				osg.setColor(color);
				for (int i = 0; i < WIDTH; i++)
					osg.drawLine(i, HEIGHT, i, HEIGHT - ((int)(HEIGHT * histogram[i])/hmax));
				osg.dispose();
			}
			if (os!=null) g.drawImage(os, 0, 0, this);
		} else {
			g.setColor(Color.white);
			g.fillRect(0, 0, WIDTH, HEIGHT);
		}
		g.setColor(Color.black);
 		g.drawLine(x1, y1, x2, y2);
 		g.drawLine(x2, HEIGHT-5, x2, HEIGHT);
 		g.drawRect(0, 0, WIDTH, HEIGHT);
     }

	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}

} // ContrastPlot class


class TrimmedLabel extends Label {
	int trim = IJ.isMacOSX()?0:6;

    public TrimmedLabel(String title) {
        super(title);
    }

    public Dimension getMinimumSize() {
        return new Dimension(super.getMinimumSize().width, super.getMinimumSize().height-trim);
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

} // TrimmedLabel class


