package ijx.plugin;
import ijx.plugin.api.PlugIn;
import ijx.gui.IjxToolbar;
import ijx.gui.dialog.DialogListener;
import ijx.gui.dialog.NonBlockingGenericDialog;
import ijx.gui.dialog.GenericDialog;
import ijx.roi.Arrow;
import ijx.roi.Roi;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;
import ij.*;

import ijx.CentralLookup;
import ijx.IjxImagePlus;
import java.awt.*;

/** This plugin implements the Edit/Options/Arrow Tool command. */
public class ArrowToolOptions implements PlugIn, DialogListener {
	private String colorName;
	private static GenericDialog gd;

 	public void run(String arg) {
 		if (gd!=null && gd.isVisible())
 			gd.toFront();
 		else
			arrowToolOptions();
	}
				
	void arrowToolOptions() {
		if (!((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).getToolName().equals("arrow"))
			IJ.setTool("arrow");
		double width = Arrow.getDefaultWidth();
		double headSize = Arrow.getDefaultHeadSize();
		Color color = ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).getForegroundColor();
		colorName = Colors.getColorName(color, "red");
		int style = Arrow.getDefaultStyle();
		gd = new NonBlockingGenericDialog("Arrow Tool");
		gd.addSlider("Width:", 1, 50, (int)width);
		gd.addSlider("Size:", 0, 30, headSize);
		gd.addChoice("Color:", Colors.colors, colorName);
		gd.addChoice("Style:", Arrow.styles, Arrow.styles[style]);
		gd.addCheckbox("Double headed", Arrow.getDefaultDoubleHeaded());
		gd.addDialogListener(this);
		gd.showDialog();
	}

	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		double width2 = gd.getNextNumber();
		double headSize2 = gd.getNextNumber();
		String colorName2 = gd.getNextChoice();
		int style2 = gd.getNextChoiceIndex();
		boolean doubleHeaded2 = gd.getNextBoolean();
		if (colorName!=null && !colorName2.equals(colorName)) {
			Color color = Colors.getColor(colorName2, Color.black);
			((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).setForegroundColor(color);
		}
		colorName = colorName2;
		Arrow.setDefaultWidth(width2);
		Arrow.setDefaultHeadSize(headSize2);
		Arrow.setDefaultStyle(style2);
		Arrow.setDefaultDoubleHeaded(doubleHeaded2);
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) return true;
		Roi roi = imp.getRoi();
		if (roi==null) return true;
		if (roi instanceof Arrow) {
			Arrow arrow = (Arrow)roi;
			roi.setStrokeWidth((float)width2);
			arrow.setHeadSize(headSize2);
			arrow.setStyle(style2);
			arrow.setDoubleHeaded(doubleHeaded2);
			imp.draw();
		}
		Prefs.set(Arrow.STYLE_KEY, style2);
		Prefs.set(Arrow.WIDTH_KEY, width2);
		Prefs.set(Arrow.SIZE_KEY, headSize2);
		Prefs.set(Arrow.DOUBLE_HEADED_KEY, doubleHeaded2);
		return true;
	}
	
} 
