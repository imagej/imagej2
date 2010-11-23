package ijx.gui;

import ijx.Prefs;
import ijx.WindowManager;
import ijx.gui.IjxImageCanvas;
import ijx.IjxImagePlus;
import ij.*;
import java.awt.Color;

/**
 * Part of IJX: 
 * @author GBH
 */
public abstract class ImageCanvasHelper {

    public static Color showAllColor = Prefs.getColor(Prefs.SHOW_ALL_COLOR,
        new Color(128, 255, 255));
    public static Color labelColor;
    public static final double[] zoomLevels = {
        1 / 72.0, 1 / 48.0, 1 / 32.0, 1 / 24.0, 1 / 16.0, 1 / 12.0,
        1 / 8.0, 1 / 6.0, 1 / 4.0, 1 / 3.0, 1 / 2.0, 0.75, 1.0, 1.5,
        2.0, 3.0, 4.0, 6.0, 8.0, 12.0, 16.0, 24.0, 32.0
    };

    public static double getHigherZoomLevel(double currentMag) {
        double newMag = 32.0;
        for (int i = zoomLevels.length - 1; i >= 0; i--) {
            if (zoomLevels[i] > currentMag) {
                newMag = zoomLevels[i];
            } else {
                break;
            }
        }
        return newMag;
    }

    public static double getLowerZoomLevel(double currentMag) {
        double newMag = zoomLevels[0];
        for (int i = 0; i < zoomLevels.length; i++) {
            if (zoomLevels[i] < currentMag) {
                newMag = zoomLevels[i];
            } else {
                break;
            }
        }
        return newMag;
    }

    public static Color getShowAllColor() {
        return showAllColor;
    }

//    public static void setShowAllColor(Color c) {
//        if (c == null) {
//            return;
//        }
//        showAllColor = c;
//        labelColor = null;
//        IjxImagePlus img = WindowManager.getCurrentImage();
//        if (img != null) {
//            IjxImageCanvas ic = img.getCanvas();
//            if (ic != null && ic.getShowAllROIs()) {
//                img.draw();
//            }
//        }
//    }
//	/** Returns the color used for "Show All" mode. */
//	public static Color getShowAllColor() {
//		if (showAllColor!=null && showAllColor.getRGB()==0xff80ffff)
//			showAllColor = Color.cyan;
//		return showAllColor;
//	}

	/** Sets the color used used for "Show All" mode. */
	public static void setShowAllColor(Color c) {
		if (c==null) return;
		showAllColor = c;
		labelColor = null;
		IjxImagePlus img = WindowManager.getCurrentImage();
		if (img!=null) {
			IjxImageCanvas ic = img.getCanvas();
			if (ic!=null && ic.getShowAllROIs()) img.draw();
		}
	}
}
