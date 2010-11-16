package ij.plugin.filter;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.util.Tools;
import ij.io.FileOpener;
import ijx.IjxImagePlus;
import java.awt.*;
import java.awt.event.*;

/** Implements the Analyze/Set Scale command. */
public class ScaleDialog implements PlugInFilter {

    private IjxImagePlus imp;

	public int setup(String arg, IjxImagePlus imp) {
		this.imp = imp;
		IJ.register(ScaleDialog.class);
		return DOES_ALL+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		double measured = 0.0;
		double known = 0.0;
		double aspectRatio = 1.0;
		String unit = "pixel";
		boolean global1 = imp.getGlobalCalibration()!=null;
		boolean global2;
		Calibration cal = imp.getCalibration();
		Calibration calOrig = cal.copy();
		boolean isCalibrated = cal.scaled();
		String length = "0.00";
		
		String scale = "<no scale>";
		int digits = 2;
		//IJ.log("ScaleDialog: "+isCalibrated);
		Roi roi = imp.getRoi();
		if (roi!=null && (roi instanceof Line)) {
			measured = ((Line)roi).getRawLength();
			length = IJ.d2s(measured, 2);
		}
		if (isCalibrated) {
			if (measured!=0.0)
				known = measured*cal.pixelWidth;
			else {
				measured = 1.0/cal.pixelWidth;
				known = 1.0;
			}
			double dscale = measured/known;
			digits = Tools.getDecimalPlaces(dscale, dscale);
			unit = cal.getUnit();
			scale = IJ.d2s(dscale, digits)+" pixels/"+unit;
			aspectRatio = cal.pixelHeight/cal.pixelWidth;
		}
		
		digits = Tools.getDecimalPlaces(measured, measured);
		int asDigits = aspectRatio==1.0?1:3;
		SetScaleDialog gd = new SetScaleDialog("Set Scale", scale, length);
		gd.addNumericField("Distance in pixels:", measured, digits, 8, null);
		gd.addNumericField("Known distance:", known, 2, 8, null);
		gd.addNumericField("Pixel aspect ratio:", aspectRatio, asDigits, 8, null);
		gd.addStringField("Unit of length:", unit);
		gd.addPanel(makeButtonPanel(gd), GridBagConstraints.EAST, new Insets(5, 0, 0, 0));
		gd.setInsets(0, 30, 0);
		gd.addCheckbox("Global", global1);
		gd.setInsets(10, 0, 0);
		gd.addMessage("Scale: "+"12345.789 pixels per centimeter");
		gd.addHelp(IJ.URL+"/docs/menus/analyze.html#scale");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		measured = gd.getNextNumber();
		known = gd.getNextNumber();
		aspectRatio = gd.getNextNumber();
		unit = gd.getNextString();
        if (unit.equals("A"))
        	unit = ""+IJ.angstromSymbol;
 		global2 = gd.getNextBoolean();
		//if (measured!=0.0 && known==0.0) {
		//	imp.setGlobalCalibration(global2?cal:null);
		//	return;
		//}
		if (measured==known && unit.equals("unit"))
			unit = "pixel";
		if (measured<=0.0 || known<=0.0 || unit.startsWith("pixel") || unit.startsWith("Pixel") || unit.equals("")) {
			cal.pixelWidth = 1.0;
			cal.pixelHeight = 1.0;
			cal.pixelDepth = 1.0;
			cal.setUnit("pixel");
		} else {
			if (gd.scaleChanged || IJ.macroRunning()) {
				cal.pixelWidth = known/measured;
				cal.pixelDepth = cal.pixelWidth;
			}
			if (aspectRatio!=0.0)
				cal.pixelHeight = cal.pixelWidth*aspectRatio;
			else
				cal.pixelHeight = cal.pixelWidth;
			cal.setUnit(unit);
		}
		if (!cal.equals(calOrig))
			imp.setCalibration(cal);
		imp.setGlobalCalibration(global2?cal:null);
		if (global2 || global2!=global1)
			WindowManager.repaintImageWindows();
		else
			imp.repaintWindow();
		if (global2 && global2!=global1)
			FileOpener.setShowConflictMessage(true);
	}
	
	/** Creates a panel containing an "Unscale" button. */
	Panel makeButtonPanel(SetScaleDialog gd) {
		Panel panel = new Panel();
    	panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		gd.unscaleButton = new Button("Click to Remove Scale");
		gd.unscaleButton.addActionListener(gd);
		panel.add(gd.unscaleButton);
		return panel;
	}

}

class SetScaleDialog extends GenericDialog {
	static final String NO_SCALE = "<no scale>";
	String initialScale;
	Button unscaleButton;
	String length;
	boolean scaleChanged;

	public SetScaleDialog(String title, String scale, String length) {
		super(title);
		initialScale = scale;
		this.length = length;
	}

    protected void setup() {
    	initialScale += "                   ";
   		setScale(initialScale);
    }
 	
 	public void textValueChanged(TextEvent e) {
		Object source = e.getSource();
		if (source==numberField.elementAt(0) || source==numberField.elementAt(1))
			scaleChanged = true;
 		Double d = getValue(((TextField)numberField.elementAt(0)).getText());
 		if (d==null)
 			{setScale(NO_SCALE); return;}
 		double measured = d.doubleValue();
 		d = getValue(((TextField)numberField.elementAt(1)).getText());
 		if (d==null)
 			{setScale(NO_SCALE); return;}
 		double known = d.doubleValue();
 		String theScale;
 		String unit = ((TextField)stringField.elementAt(0)).getText();
 		boolean noUnit = unit.startsWith("pixel")||unit.startsWith("Pixel")||unit.equals("");
 		if (known>0.0 && noUnit && e.getSource()==numberField.elementAt(1)) {
 			unit = "unit";
			((TextField)stringField.elementAt(0)).setText(unit);
 		}
 		boolean noScale = measured<=0||known<=0||noUnit;
 		if (noScale)
 			theScale = NO_SCALE;
 		else {
 			double scale = measured/known;
			int digits = Tools.getDecimalPlaces(scale, scale);
 			theScale = IJ.d2s(scale,digits)+(scale==1.0?" pixel/":" pixels/")+unit;
 		}
 		setScale(theScale);
	}
	
	public void actionPerformed(ActionEvent e) { 
		super.actionPerformed(e);
		if (e.getSource()==unscaleButton) {
			((TextField)numberField.elementAt(0)).setText(length);
			((TextField)numberField.elementAt(1)).setText("0.00");
			((TextField)numberField.elementAt(2)).setText("1.0");
			((TextField)stringField.elementAt(0)).setText("pixel");
			setScale(NO_SCALE);
			scaleChanged = true;
			if (IJ.isMacOSX())
				{setVisible(false); setVisible(true);}
		}
	}

	void setScale(String theScale) {
 		((Label)theLabel).setText("Scale: "+theScale);
	}

}
