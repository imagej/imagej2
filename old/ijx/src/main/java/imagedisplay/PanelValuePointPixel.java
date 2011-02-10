package imagedisplay;

import imagedisplay.util.ColorUtils;
import imagedisplay.util.StaticSwingUtils;
import java.awt.*;
import java.text.NumberFormat;
import javax.swing.*;

/**
 * <p>Title: Used to display pixel, min, mean, max, and ROI values </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class PanelValuePointPixel extends JPanel implements PixelChangeListener, RoiChangeListener {

    static NumberFormat fmtDec1;
    static NumberFormat fmtDec2;

    static {
        fmtDec1 = NumberFormat.getNumberInstance();
        fmtDec1.setMinimumFractionDigits(1);
        fmtDec1.setMaximumFractionDigits(1);
        fmtDec2 = NumberFormat.getNumberInstance();
        fmtDec2.setMinimumFractionDigits(2);
        fmtDec2.setMaximumFractionDigits(2);
    }
    ValueNoLabel valuePixel = new ValueNoLabel();
    ValueNoLabel valueStats = new ValueNoLabel();
    ValueNoLabel valueROI = new ValueNoLabel();
    private boolean showRetAzim = false;

    public PanelValuePointPixel() {
        this(0);
    }

    public PanelValuePointPixel(int show) {
        if (show == 1) {
            showRetAzim = true;
        }
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(valuePixel, null);
        this.add(valueStats, null);
        this.add(valueROI, null);
        this.setMaximumSize(new Dimension(9999, 20));
    }

    // Pixel changed, PixelChangeListener implementation
    public void pixelChanged(final PixelChangeEvent evnt) {
        StaticSwingUtils.dispatchToEDT(new Runnable() {

            public void run() {
                if (evnt.value < 0) {
                    setValuePixel();
                } else {
                    setValuePixel(evnt.value, evnt.x, evnt.y);
                }
            }
        });
    }

    // ROI changed, RoiChangeListener implementation
    public void roiChanged(final RoiChangeEvent roiEvt) {
        StaticSwingUtils.dispatchToEDT(new Runnable() {

            public void run() {
                if (roiEvt.w > 0) {
                    setValueStats(roiEvt.min, roiEvt.mean, roiEvt.max, Color.blue);
                    setValueROI(roiEvt.x, roiEvt.y, roiEvt.w, roiEvt.h);
                } else {
                    // show values for full image
                    setValueStats(roiEvt.min, roiEvt.mean, roiEvt.max, Color.black);
                    setValueROI();
                }
            }
        });
    }

    //----------------------------------------------------------------------
    public void setValuePixel(int v, int x, int y) {
        valuePixel.set(fixedWidth(v, 3) + " (" + String.valueOf(x) + ", " + String.valueOf(y) + ")");
    }

    String fixedWidth(int v, int width) {
        String s = String.valueOf(v);
        while (s.length() <= width) {
            s = " " + s;
        }
        return s;
    }

    public void setValuePixel() {
        valuePixel.set("");
    }

    public void setValueStats(int min, float mean, int max) {
        valueStats.set(String.valueOf(min) + " < " + fmtDec1.format(mean) + " <  "
                + String.valueOf(max));
    }

    public void setValueStats(int min, float mean, int max, Color color) {
        valueStats.set(String.valueOf(min) + " < " + fmtDec1.format(mean) + " <  "
                + String.valueOf(max), color);
    }

    public void setValueStats() {
        valueStats.set("");
    }

    public void setValueROI(int x, int y, int w, int h) {
        valueROI.set(String.valueOf(x) + ", " + String.valueOf(y) + " (" + String.valueOf(w) + " x "
                + String.valueOf(h) + ")", ColorUtils.darkblue);
    }

    public void setValueROI() {
        valueROI.set("");
    }

    public void blankAll() {
        valuePixel.set("");
        valueStats.set("");
        valueROI.set("");
    }
}
