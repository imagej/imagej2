package ij.plugin.filter;
import ijx.IjxImagePlus;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.util.Tools;
import ij.io.FileOpener;
import java.awt.*;
import java.awt.event.*;

/** Implements the Analyze/Set Scale command. */
public class ScaleDialog implements IjxPlugInFilter {

    private IjxImagePlus imp;

	public int setup(String arg, IjxImagePlus imp) {
		this.imp = imp;
		IJ.register(ScaleDialog.class);
		return DOES_ALL+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		double measured = 0.0;
		double known = 1.0;
		double aspectRatio = 1.0;
		String unit = "cm";
		boolean global1 = imp.getGlobalCalibration()!=null;
		boolean global2;
		Calibration cal = imp.getCalibration();
		Calibration calOrig = cal.copy();
		boolean isCalibrated = cal.scaled();
		
		String scale = "<no scale>";
		int digits = 2;
		if (isCalibrated) {
			measured = 1.0/cal.pixelWidth;
			digits = Tools.getDecimalPlaces(measured, measured);
			known = 1.0;
			aspectRatio = cal.pixelHeight/cal.pixelWidth;
			unit = cal.getUnit();
			scale = IJ.d2s(measured,digits)+" pixels/"+unit;
		}
		Roi roi = imp.getRoi();
		if (roi!=null && (roi instanceof Line)) {
			measured = ((Line)roi).getRawLength();
			known = 0.0;
		}
		
		SetScaleDialog gd = new SetScaleDialog("Set Scale", scale);
		gd.addNumericField("Distance in Pixels:", measured, digits, 8, null);
		gd.addNumericField("Known Distance:", known, 2, 8, null);
		gd.addNumericField("Pixel Aspect Ratio:", aspectRatio, 1, 8, null);
		gd.addStringField("Unit of Length:", unit);
		gd.addMessage("Scale: "+"12345.789 pixels per centimeter");
		gd.addCheckbox("Global", global1);
		gd.addPanel(makeButtonPanel(gd), GridBagConstraints.EAST, new Insets(5, 0, 0, 25));
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		measured = gd.getNextNumber();
		known = gd.getNextNumber();
		aspectRatio = gd.getNextNumber();
		unit = gd.getNextString();
        if (unit.equals("um"))
            unit = IJ.micronSymbol+"m";
        else if (unit.equals("A"))
        	unit = ""+IJ.angstromSymbol;
 		global2 = gd.getNextBoolean();
		if (measured!=0.0 && known==0.0) {
			imp.setGlobalCalibration(global2?cal:null);
			return;
		}
		if (measured<=0.0 || unit.startsWith("pixel") || unit.startsWith("Pixel") || unit.equals("")) {
			cal.pixelWidth = 1.0;
			cal.pixelHeight = 1.0;
			cal.pixelDepth = 1.0;
			cal.setUnit("pixel");
		} else {
			cal.pixelWidth = known/measured;
			if (aspectRatio!=0.0)
				cal.pixelHeight = cal.pixelWidth*aspectRatio;
			else
				cal.pixelHeight = cal.pixelWidth;
			cal.pixelDepth = cal.pixelWidth;
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
		gd.unscaleButton = new Button("Reset");
		gd.unscaleButton.addActionListener(gd);
		panel.add(gd.unscaleButton);
		return panel;
	}

}

class SetScaleDialog extends GenericDialog {
	static final String NO_SCALE = "<no scale>";
	String initialScale;
	Button unscaleButton;

	public SetScaleDialog(String title, String scale) {
		super(title);
		initialScale = scale;
	}

    protected void setup() {
    	initialScale += "          ";
   		setScale(initialScale);
    }
 	
 	public void textValueChanged(TextEvent e) {
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
 		boolean noScale = measured<=0||known<=0||unit.startsWith("pixel")||unit.startsWith("Pixel")||unit.equals("");
 		if (noScale)
 			theScale = NO_SCALE;
 		else {
 			double scale = measured/known;
			int digits = Tools.getDecimalPlaces(scale, scale);
 			theScale = IJ.d2s(scale,digits)+" pixels/"+unit;
 		}
 		setScale(theScale);
	}
	
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource()==unscaleButton) {
			((TextField)numberField.elementAt(0)).setText("0.00");
			((TextField)numberField.elementAt(1)).setText("1.00");
			((TextField)numberField.elementAt(2)).setText("1.0");
			((TextField)stringField.elementAt(0)).setText("pixel");
			setScale(NO_SCALE);
		}
	}

	void setScale(String theScale) {
 		((Label)theLabel).setText("Scale: "+theScale);
	}

}