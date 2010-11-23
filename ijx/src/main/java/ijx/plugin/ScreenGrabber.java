package ijx.plugin;
import ijx.plugin.api.PlugIn;
import ijx.WindowManager;
import ijx.IJ;
import ij.*;

import ijx.IjxImagePlus;
import ijx.gui.IjxImageCanvas;
import ijx.gui.IjxImageWindow;
import java.awt.*;

/** This plugin implements the Image/Flatten, Plugins/Utilities/Capture Screen
    and Plugins/Utilities/Capture Image commands. */
public class ScreenGrabber implements PlugIn {

	public void run(String arg) {
		IjxImagePlus imp2 = null;
		if (arg.equals("image") || arg.equals("flatten"))
			imp2 = captureImage();
		else
			imp2 = captureScreen();
		if (imp2!=null)
			imp2.show();
		/*
		if (imp2==null) return;
		if (arg.equals("flatten")) {
			IjxImagePlus imp = WindowManager.getCurrentImage();
			if (imp==null) return;
			if (imp.isHyperStack() || imp.isComposite())
				imp2.show();
			else {
				Undo.setup(Undo.TYPE_CONVERSION, imp);
				imp.setProcessor(null, imp2.getProcessor());
				imp.killRoi();
			}
		} else
			imp2.show();
		*/
	}
    
	/** Captures the entire screen and returns it as an IjxImagePlus. */
	public IjxImagePlus captureScreen() {
		IjxImagePlus imp = null;
		try {
			Robot robot = new Robot();
			Dimension dimension = IJ.getScreenSize();
			Rectangle r = new Rectangle(dimension);
			Image img = robot.createScreenCapture(r);
			if (img!=null) imp = IJ.getFactory().newImagePlus("Screen", img);
		} catch(Exception e) {}
		return imp;
	}

	/** Captures the active image window and returns it as an IjxImagePlus. */
	public IjxImagePlus captureImage() {
		IjxImagePlus imp = IJ.getImage();
		if (imp==null) {
			IJ.noImage();
			return null;
		}
		IjxImagePlus imp2 = null;
		try {
			IjxImageWindow win = imp.getWindow();
			if (win==null) return null;
			win.toFront();
			IJ.wait(500);
			Point loc = win.getLocation();
			IjxImageCanvas ic = win.getCanvas();
			Rectangle bounds = ic.getBounds();
			loc.x += bounds.x;
			loc.y += bounds.y;
			Rectangle r = new Rectangle(loc.x, loc.y, bounds.width, bounds.height);
			Robot robot = new Robot();
			Image img = robot.createScreenCapture(r);
			if (img!=null) {
				String title = WindowManager.getUniqueName(imp.getTitle());
				imp2 = IJ.getFactory().newImagePlus(title, img);
			}
		} catch(Exception e) {}
		return imp2;
	}

}

