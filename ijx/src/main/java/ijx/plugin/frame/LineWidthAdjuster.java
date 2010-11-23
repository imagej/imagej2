package ijx.plugin.frame;
import ijx.plugin.api.PlugIn;
import ijx.plugin.api.PlugInFrame;
import ijx.gui.GUI;
import ijx.roi.Line;
import ijx.roi.PolygonRoi;
import ijx.roi.Roi;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;
import java.awt.*;
import java.awt.event.*;
import ij.*;
import ij.plugin.*;

import ijx.util.Tools;
import ijx.CentralLookup;
import ijx.IjxImagePlus;
import ijx.app.KeyboardHandler;

/** Adjusts the width of line selections.  */
public class LineWidthAdjuster extends PlugInFrame implements PlugIn,
	Runnable, AdjustmentListener, TextListener, ItemListener {

	public static final String LOC_KEY = "line.loc";
	int sliderRange = 300;
	Scrollbar slider;
	int value;
	boolean setText;
	static LineWidthAdjuster instance; 
	Thread thread;
	boolean done;
	TextField tf;
	Checkbox checkbox;

	public LineWidthAdjuster() {
		super("Line Width");
		if (instance!=null) {
			instance.toFront();
			return;
		}		
		WindowManager.addWindow(this);
		instance = this;
		slider = new Scrollbar(Scrollbar.HORIZONTAL, Line.getWidth(), 1, 1, sliderRange+1);
		slider.setFocusable(false); // prevents blinking on Windows
				
		Panel panel = new Panel();
		int margin = IJ.isMacOSX()?5:0;
		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints c  = new GridBagConstraints();
		panel.setLayout(grid);
		c.gridx = 0; c.gridy = 0;
		c.gridwidth = 1;
		c.ipadx = 100;
		c.insets = new Insets(margin, 15, margin, 5);
		c.anchor = GridBagConstraints.CENTER;
		grid.setConstraints(slider, c);
		panel.add(slider);
		c.ipadx = 0;  // reset
		c.gridx = 1;
		c.insets = new Insets(margin, 5, margin, 15);
		tf = new TextField(""+Line.getWidth(), 4);
		tf.addTextListener(this);
		grid.setConstraints(tf, c);
    	panel.add(tf);
		
		c.gridx = 2;
		c.insets = new Insets(margin, 25, margin, 5);
		checkbox = new Checkbox("Spline Fit", isSplineFit());
		checkbox.addItemListener(this);
		panel.add(checkbox);
		
		add(panel, BorderLayout.CENTER);
		slider.addAdjustmentListener(this);
		slider.setUnitIncrement(1);
		
		pack();
		Point loc = Prefs.getLocation(LOC_KEY);
		if (loc!=null)
			setLocation(loc);
		else
			GUI.center(this);
		setResizable(false);
		show();
		thread = new Thread(this, "LineWidthAdjuster");
		thread.start();
		setup();
        addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
	}
	
	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		value = slider.getValue();
		setText = true;
		notify();
	}

    public  synchronized void textValueChanged(TextEvent e) {
        int width = (int)Tools.parseDouble(tf.getText(), -1);
		//IJ.log(""+width);
        if (width==-1) return;
        if (width<0) width=1;
        if (width!=Line.getWidth()) {
			slider.setValue(width);
        	value = width;
        	notify();
        }
    }
	void setup() {
	}
	
	// Separate thread that does the potentially time-consuming processing 
	public void run() {
		while (!done) {
			synchronized(this) {
				try {wait();}
				catch(InterruptedException e) {}
				if (done) return;
				Line.setWidth(value);
				if (setText) tf.setText(""+value);
				setText = false;
				updateRoi();
			}
		}
	}
	
	private static void updateRoi() {
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null) {
			Roi roi = imp.getRoi();
			if (roi!=null && roi.isLine())
				{roi.updateWideLine(Line.getWidth()); imp.draw(); return;}
		}
		if (Roi.previousRoi==null) return;
		int id = Roi.previousRoi.getImageID();
		if (id>=0) return;
		imp = WindowManager.getImage(id);
		if (imp==null) return;
		Roi roi = imp.getRoi();
		if (roi!=null && roi.isLine()) {
			roi.updateWideLine(Line.getWidth());
			imp.draw();
		}
	}
	
	boolean isSplineFit() {
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) return false;
		Roi roi = imp.getRoi();
		if (roi==null) return false;
		if (!(roi instanceof PolygonRoi)) return false;
		return ((PolygonRoi)roi).isSplineFit();
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
		synchronized(this) {notify();}
        return true;
	}

    public void windowActivated(WindowEvent e) {
    	super.windowActivated(e);
    	checkbox.setState(isSplineFit());
	}

	public void itemStateChanged(ItemEvent e) {
		boolean selected = e.getStateChange()==ItemEvent.SELECTED;
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			{checkbox.setState(false); return;};
		Roi roi = imp.getRoi();
		if (roi==null || !(roi instanceof PolygonRoi))
			{checkbox.setState(false); return;};
		int type = roi.getType();
		if (type==Roi.FREEROI || type==Roi.FREELINE)
			{checkbox.setState(false); return;};;
		PolygonRoi poly = (PolygonRoi)roi;
		boolean splineFit = poly.isSplineFit();
		if (selected && !splineFit)
			{poly.fitSpline(); imp.draw();}
		else if (!selected && splineFit)
			{poly.removeSplineFit(); imp.draw();}
	}
	
	public static void update() {
		if (instance==null) return;
		instance.checkbox.setState(instance.isSplineFit());
		int sliderWidth = instance.slider.getValue();
		int lineWidth = Line.getWidth();
		if (lineWidth!=sliderWidth && lineWidth<=200) {
			instance.slider.setValue(lineWidth);
			instance.tf.setText(""+lineWidth);
		}
	}
	
} 

