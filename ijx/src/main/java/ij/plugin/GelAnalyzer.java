package ij.plugin;

import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ijx.CentralLookup;
import ijx.IjxImagePlus;
import ijx.gui.IjxImageCanvas;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxWindow;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;

/** This plugin generates gel profile plots that can be analyzed using
the wand tool. It is similar to the "Gel Plotting Macros" in NIH Image. */
public class GelAnalyzer implements PlugIn {
    static final String OPTIONS = "gel.options";
    static final String VSCALE = "gel.vscale";
    static final String HSCALE = "gel.hscale";
    static final int OD = 1, PERCENT = 2, OUTLINE = 4, INVERT = 8;
    static int saveID;
    static int nLanes, saveNLanes;
    static Rectangle firstRect;
    static final int MAX_LANES = 100;
    static int[] x = new int[MAX_LANES + 1];
    static PlotsCanvas plotsCanvas;
    static ImageProcessor ipLanes;
    static IjxImagePlus gel;
    static int plotHeight;
    static int options = (int) Prefs.get(OPTIONS, PERCENT + INVERT);
    static boolean uncalibratedOD = (options & OD) != 0;
    static boolean labelWithPercentages = (options & PERCENT) != 0;

    ;
    static boolean outlineLanes;
    static boolean invertPeaks = (options & INVERT) != 0;
    static double verticalScaleFactor = Prefs.get(VSCALE, 1.0);
    static double horizontalScaleFactor = Prefs.get(HSCALE, 1.0);
    static Overlay overlay;
    boolean invertedLut;
    IjxImagePlus imp;
    Font f;
    double odMin = Double.MAX_VALUE, odMax = -Double.MAX_VALUE;
    static boolean isVertical;
    static boolean showLaneDialog = true;

    public void run(String arg) {
        if (arg.equals("options")) {
            showDialog();
            return;
        }

        imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        if (arg.equals("reset")) {
            nLanes = 0;
            saveNLanes = 0;
            saveID = 0;
            if (plotsCanvas != null) {
                plotsCanvas.reset();
            }
            ipLanes = null;
            overlay = null;
            if (gel != null) {
                IjxImageCanvas ic = gel.getCanvas();
                if (ic != null) {
                    ic.setDisplayList(null);
                }
                gel.draw();
            }
            return;
        }

        if (arg.equals("percent") && plotsCanvas != null) {
            plotsCanvas.displayPercentages();
            return;
        }

        if (arg.equals("label") && plotsCanvas != null) {
            if (plotsCanvas.counter == 0) {
                show("There are no peak area measurements.");
            } else {
                plotsCanvas.labelPeaks();
            }
            return;
        }

        if (imp.getID() != saveID) {
            nLanes = 0;
            ipLanes = null;
            saveID = 0;
        }

        if (arg.equals("replot")) {
            if (saveNLanes == 0) {
                show("The data needed to re-plot the lanes is not available");
                return;
            }
            nLanes = saveNLanes;
            plotLanes(gel, true);
            return;
        }

        if (arg.equals("draw")) {
            outlineLanes();
            return;
        }

        Roi roi = imp.getRoi();
        if (roi == null || roi.getType() != Roi.RECTANGLE) {
            show("Rectangular selection required.");
            return;
        }
        Rectangle rect = roi.getBounds();
        if (nLanes == 0) {
            invertedLut = imp.isInvertedLut();
            IJ.register(GelAnalyzer.class);  // keeps this class from being GC'd
        }

        if (arg.equals("first")) {
            selectFirstLane(rect);
            return;
        }

        if (nLanes == 0) {
            show("You must first use the \"Outline First Lane\" command.");
            return;
        }
        if (arg.equals("next")) {
            selectNextLane(rect);
            return;
        }

        if (arg.equals("plot")) {
            if ((isVertical && (rect.x != x[nLanes])) || (!(isVertical) && (rect.y != x[nLanes]))) {
                selectNextLane(rect);
            }
            plotLanes(gel, false);
            return;
        }

    }

    void showDialog() {
        GenericDialog gd = new GenericDialog("Gel Analyzer");
        gd.addNumericField("Vertical scale factor:", verticalScaleFactor, 1);
        gd.addNumericField("Horizontal scale factor:", horizontalScaleFactor, 1);
        gd.addCheckbox("Uncalibrated OD", uncalibratedOD);
        gd.addCheckbox("Label with percentages", labelWithPercentages);
        gd.addCheckbox("Invert peaks", invertPeaks);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        verticalScaleFactor = gd.getNextNumber();
        horizontalScaleFactor = gd.getNextNumber();
        uncalibratedOD = gd.getNextBoolean();
        labelWithPercentages = gd.getNextBoolean();
        invertPeaks = gd.getNextBoolean();
        options = 0;
        if (uncalibratedOD) {
            options |= OD;
        }
        if (labelWithPercentages) {
            options |= PERCENT;
        }
        if (invertPeaks) {
            options |= INVERT;
        }
        Prefs.set(OPTIONS, options);
        Prefs.set(VSCALE, verticalScaleFactor);
        Prefs.set(HSCALE, horizontalScaleFactor);
    }

    void selectFirstLane(Rectangle rect) {
        if (rect.width / rect.height >= 2 || IJ.altKeyDown()) {
            if (showLaneDialog) {
                String msg = "Are the lanes really horizontal?\n \n"
                        + "ImageJ assumes the lanes are\n"
                        + "horizontal if the selection is more\n"
                        + "than twice as wide as it is tall. Note\n"
                        + "that the selection can only be moved\n"
                        + "vertically when the lanes are horizontal.";
                GenericDialog gd = new GenericDialog("Gel Analyzer");
                gd.addMessage(msg);
                gd.setOKLabel("Yes");
                gd.showDialog();
                if (gd.wasCanceled()) {
                    return;
                }
                showLaneDialog = false;
            }
            isVertical = false;
        } else {
            isVertical = true;
        }

        /*
        if ( (isVertical && (rect.height/rect.width)<2 ) || (!isVertical && (rect.width/rect.height)<2 ) ) {
        GenericDialog gd = new GenericDialog("Lane Orientation");
        String[] orientations = {"Vertical","Horizontal"};
        int defaultOrientation = isVertical?0:1;
        gd.addChoice("Lane Orientation:", orientations, orientations[defaultOrientation]);
        gd.showDialog();
        if (gd.wasCanceled())
        return;
        String orientation = gd.getNextChoice();
        if(orientation.equals(orientations[0]))
        isVertical=true;
        else
        isVertical=false;
        }
         */

        IJ.showStatus("Lane 1 selected (" + (isVertical ? "vertical" : "horizontal") + " lanes)");
        firstRect = rect;
        nLanes = 1;
        saveNLanes = 0;
        if (isVertical) {
            x[1] = rect.x;
        } else {
            x[1] = rect.y;
        }
        gel = imp;
        saveID = imp.getID();
        overlay = null;
        updateRoiList(rect);
    }

    void selectNextLane(Rectangle rect) {
        if (rect.width != firstRect.width || rect.height != firstRect.height) {
            show("Selections must all be the same size.");
            return;
        }
        if (nLanes < MAX_LANES) {
            nLanes += 1;
        }
        IJ.showStatus("Lane " + nLanes + " selected");

        if (isVertical) {
            x[nLanes] = rect.x;
        } else {
            x[nLanes] = rect.y;
        }
        if (isVertical && rect.y != firstRect.y) {
            rect.y = firstRect.y;
            gel.setRoi(rect);
        } else if (!isVertical && rect.x != firstRect.x) {
            rect.x = firstRect.x;
            gel.setRoi(rect);
        }
        updateRoiList(rect);
    }

    void updateRoiList(Rectangle rect) {
        if (gel == null) {
            return;
        }
        if (overlay == null) {
            overlay = new Overlay();
            overlay.drawLabels(true);
        }
        overlay.add(new Roi(rect.x, rect.y, rect.width, rect.height, null));
        gel.setOverlay(overlay);
    }

    void plotLanes(IjxImagePlus imp, boolean replot) {
        int topMargin = 16;
        int bottomMargin = 2;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        int plotWidth;
        double[][] profiles;
        profiles = new double[MAX_LANES + 1][];
        IJ.showStatus("Plotting " + nLanes + " lanes");
        ImageProcessor ipRotated = imp.getProcessor();
        if (isVertical) {
            ipRotated = ipRotated.rotateLeft();
        }
        IjxImagePlus imp2 = IJ.getFactory().newImagePlus("", ipRotated);
        imp2.setCalibration(imp.getCalibration());
        if (uncalibratedOD && (imp2.getType() == IjxImagePlus.GRAY16 || imp2.getType() == IjxImagePlus.GRAY32)) {
            new ImageConverter(imp2).convertToGray8();
        }
        if (invertPeaks) {
            ImageProcessor ip2 = imp2.getProcessor().duplicate();
            ip2.invert();
            imp2.setProcessor(null, ip2);
        }
        //imp2.show();

        for (int i = 1; i <= nLanes; i++) {
            if (isVertical) {
                imp2.setRoi(firstRect.y,
                        ipRotated.getHeight() - x[i] - firstRect.width,
                        firstRect.height, firstRect.width);
            } else {
                imp2.setRoi(firstRect.x, x[i], firstRect.width, firstRect.height);
            }
            ProfilePlot pp = new ProfilePlot(imp2);
            profiles[i] = pp.getProfile();
            if (pp.getMin() < min) {
                min = pp.getMin();
            }
            if (pp.getMax() > max) {
                max = pp.getMax();
            }
            if (uncalibratedOD) {
                profiles[i] = od(profiles[i]);
            }
        }
        if (uncalibratedOD) {
            min = odMin;
            max = odMax;
        }

        if (isVertical) {
            plotWidth = firstRect.height;
        } else {
            plotWidth = firstRect.width;
        }
        if (plotWidth < 650) {
            plotWidth = 650;
        }
        if (isVertical) {
            if (plotWidth > 4 * firstRect.height) {
                plotWidth = 4 * firstRect.height;
            }
        } else {
            if (plotWidth > 4 * firstRect.width) {
                plotWidth = 4 * firstRect.width;
            }
        }

        Dimension screen = IJ.getScreenSize();
        if (plotWidth > screen.width - screen.width / 6) {
            plotWidth = screen.width - screen.width / 6;
        }
        plotWidth = (int) (plotWidth * horizontalScaleFactor);
        plotHeight = plotWidth / 2;
        if (plotHeight < 250) {
            plotHeight = 250;
        }
        // if (plotHeight>500) plotHeight = 500;
        plotHeight = (int) (plotHeight * verticalScaleFactor);
        ImageProcessor ip = new ByteProcessor(plotWidth, topMargin + nLanes * plotHeight + bottomMargin);
        ip.setColor(Color.white);
        ip.fill();
        ip.setColor(Color.black);
        //draw border
        int h = ip.getHeight();
        ip.moveTo(0, 0);
        ip.lineTo(plotWidth - 1, 0);
        ip.lineTo(plotWidth - 1, h - 1);
        ip.lineTo(0, h - 1);
        ip.lineTo(0, 0);
        ip.moveTo(0, h - 2);
        ip.lineTo(plotWidth - 1, h - 2);
        String s = imp.getTitle() + "; ";
        Calibration cal = imp.getCalibration();
        if (cal.calibrated()) {
            s += cal.getValueUnit();
        } else if (uncalibratedOD) {
            s += "Uncalibrated OD";
        } else {
            s += "Uncalibrated";
        }
        ip.moveTo(5, topMargin);
        ip.drawString(s);
        double xScale = (double) plotWidth / profiles[1].length;
        double yScale;
        if ((max - min) == 0.0) {
            yScale = 1.0;
        } else {
            yScale = plotHeight / (max - min);
        }
        for (int i = 1; i <= nLanes; i++) {
            double[] profile = profiles[i];
            int top = (i - 1) * plotHeight + topMargin;
            int base = top + plotHeight;
            ip.moveTo(0, base);
            ip.lineTo((int) (profile.length * xScale), base);
            ip.moveTo(0, base - (int) ((profile[0] - min) * yScale));
            for (int j = 1; j < profile.length; j++) {
                ip.lineTo((int) (j * xScale + 0.5), base - (int) ((profile[j] - min) * yScale + 0.5));
            }
        }
        Line.setWidth(1);
        IjxImagePlus plots = new Plots();
        plots.setProcessor("Plots of " + imp.getShortTitle(), ip);
        plots.setChanged(true);
        ip.setThreshold(0, 0, ImageProcessor.NO_LUT_UPDATE); // Wand tool works better with threshold set
        if (cal.calibrated()) {
            double pixelsAveraged = isVertical ? firstRect.width : firstRect.height;
            double scale = Math.sqrt((xScale * yScale) / pixelsAveraged);
            Calibration plotsCal = plots.getCalibration();
            plotsCal.setUnit("unit");
            plotsCal.pixelWidth = 1.0 / scale;
            plotsCal.pixelHeight = 1.0 / scale;
        }
        plots.show();
        saveNLanes = nLanes;
        nLanes = 0;
        saveID = 0;
        //gel = null;
        ipLanes = null;
        ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).setColor(Color.black);
        ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).setTool(IjxToolbar.LINE);
        IjxWindow win = WindowManager.getCurrentWindow();
        if (win instanceof IjxImageWindow) {
            IjxImageCanvas canvas = ((IjxImageWindow) win).getCanvas();
            if (canvas instanceof PlotsCanvas) {
                plotsCanvas = (PlotsCanvas) canvas;
            } else {
                plotsCanvas = null;
            }
        } else {
            plotsCanvas = null;
        }
    }

    double[] od(double[] profile) {
        double v;
        for (int i = 0; i < profile.length; i++) {
            v = 0.434294481 * Math.log(255.0 / (255.0 - profile[i]));
            //v = 0.434294481*Math.log(255.0/v);
            if (v < odMin) {
                odMin = v;
            }
            if (v > odMax) {
                odMax = v;
            }
            profile[i] = v;
        }
        return profile;
    }

    void outlineLanes() {
        if (gel == null || overlay == null) {
            show("Data needed to outline lanes is no longer available.");
            return;
        }
        int lineWidth = (int) (1.0 / gel.getCanvas().getMagnification());
        if (lineWidth < 1) {
            lineWidth = 1;
        }
        Font f = new Font("Helvetica", Font.PLAIN, 12 * lineWidth);
        ImageProcessor ip = gel.getProcessor();
        ImageProcessor ipLanes = ip.duplicate();
        if (!(ipLanes instanceof ByteProcessor)) {
            ipLanes = ipLanes.convertToByte(true);
        }
        ipLanes.setFont(f);
        ipLanes.setLineWidth(lineWidth);
        setCustomLut(ipLanes);
        IjxImagePlus lanes = IJ.getFactory().newImagePlus("Lanes of " + gel.getShortTitle(), ipLanes);
        lanes.setChanged(true);
        lanes.setRoi(gel.getRoi());
        gel.killRoi();
        for (int i = 0; i < overlay.size(); i++) {
            Roi roi = overlay.get(i);
            Rectangle r = roi.getBounds();
            ipLanes.drawRect(r.x, r.y, r.width, r.height);
            String s = "" + (i + 1);
            if (isVertical) {
                int yloc = r.y;
                if (yloc < lineWidth * 12) {
                    yloc += lineWidth * 14;
                }
                ipLanes.drawString(s, r.x + r.width / 2 - ipLanes.getStringWidth(s) / 2, yloc);
            } else {
                int xloc = r.x - ipLanes.getStringWidth(s) - 2;
                if (xloc < lineWidth * 10) {
                    xloc = r.x + 2;
                }
                ipLanes.drawString(s, xloc, r.y + r.height / 2 + 6);
            }
        }
        lanes.killRoi();
        lanes.show();
    }

    void setCustomLut(ImageProcessor ip) {
        IndexColorModel cm = (IndexColorModel) ip.getColorModel();
        byte[] reds = new byte[256];
        byte[] greens = new byte[256];
        byte[] blues = new byte[256];
        cm.getReds(reds);
        cm.getGreens(greens);
        cm.getBlues(blues);
        reds[1] = (byte) 255;
        greens[1] = (byte) 0;
        blues[1] = (byte) 0;
        ip.setColorModel(new IndexColorModel(8, 256, reds, greens, blues));
        byte[] pixels = (byte[]) ip.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            if ((pixels[i] & 255) == 1) {
                pixels[i] = 0;
            }
        }
        ip.setColor(1);
    }

    void show(String msg) {
        IJ.showMessage("Gel Analyzer", msg);
    }
}

class Plots extends ImagePlus {
    /** Overrides IjxImagePlus.show(). */
    public void show() {
        img = ip.createImage();
        IjxImageCanvas ic = new PlotsCanvas(this);
        win = IJ.getFactory().newImageWindow(this, ic);
        IJ.showStatus("");
        if (ic.getMagnification() == 1.0) {
            return;
        }
        while (ic.getMagnification() < 1.0) {
            ic.zoomIn(0, 0);
        }
        Point loc = win.getLocation();
        int w = getWidth() + 20;
        int h = getHeight() + 30;
        Dimension screen = IJ.getScreenSize();
        if (loc.x + w > screen.width) {
            w = screen.width - loc.x - 20;
        }
        if (loc.y + h > screen.height) {
            h = screen.height - loc.y - 30;
        }
        ((Frame)win).setSize(w, h);
        ((Frame)win).validate();
        repaintWindow();
    }
}

class PlotsCanvas extends ImageCanvas {
    public static final int MAX_PEAKS = 200;
    double[] actual = {428566.00, 351368.00, 233977.00, 99413.00, 60057.00, 31382.00,
        14531.00, 7843.00, 2146.00, 752.00, 367.00};
    double[] measured = new double[MAX_PEAKS];
    Rectangle[] rect = new Rectangle[MAX_PEAKS];
    int counter;
    ResultsTable rt;

    public PlotsCanvas(IjxImagePlus imp) {
        super(imp);
    }

    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        Roi roi = imp.getRoi();
        if (roi == null) {
            return;
        }
        if (roi.getType() == Roi.LINE) {
            Roi.setColor(Color.blue);
        } else {
            Roi.setColor(Color.yellow);
        }
        if (((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).getToolId() != IjxToolbar.WAND || IJ.spaceBarDown()) {
            return;
        }
        if (IJ.shiftKeyDown()) {
            IJ.showMessage("Gel Analyzer", "Unable to measure area because shift key is down.");
            imp.killRoi();
            counter = 0;
            return;
        }
        ImageStatistics s = imp.getStatistics();
        if (counter == 0) {
            rt = ResultsTable.getResultsTable();
            rt.reset();
        }
        //IJ.setColumnHeadings(" \tArea");
        double perimeter = roi.getLength();
        String error = "";
        double circularity = 4.0 * Math.PI * (s.pixelCount / (perimeter * perimeter));
        if (circularity < 0.025) {
            error = " (error?)";
        }
        double area = s.pixelCount + perimeter / 2.0; // add perimeter/2 to account area under border
        Calibration cal = imp.getCalibration();
        area = area * cal.pixelWidth * cal.pixelHeight;
        rect[counter] = roi.getBounds();

        //area += (rect[counter].width/rect[counter].height)*1.5;
        // adjustment for small peaks from NIH Image gel macros

        int places = cal.scaled() ? 3 : 0;
        rt.incrementCounter();
        rt.addValue("Area", area);
        rt.show("Results");
        // IJ.write((counter+1)+"\t"+IJ.d2s(area, places)+error);
        measured[counter] = area;
        if (counter < MAX_PEAKS) {
            counter++;
        }
    }

    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        Roi roi = imp.getRoi();
        if (roi != null && roi.getType() == Roi.LINE) {
            Undo.setup(Undo.FILTER, imp);
            imp.getProcessor().snapshot();
            roi.drawPixels();
            imp.updateAndDraw();
            imp.killRoi();
        }
    }

    void reset() {
        counter = 0;
    }

    void labelPeaks() {
        imp.killRoi();
        double total = 0.0;
        for (int i = 0; i < counter; i++) {
            total += measured[i];
        }
        ImageProcessor ip = imp.getProcessor();
        ip.setFont(new Font("SansSerif", Font.PLAIN, 9));
        for (int i = 0; i < counter; i++) {
            Rectangle r = rect[i];
            String s;
            if (GelAnalyzer.labelWithPercentages) {
                s = IJ.d2s((measured[i] / total) * 100, 2);
            } else {
                s = IJ.d2s(measured[i], 0);
            }
            int swidth = ip.getStringWidth(s);
            int x = r.x + r.width / 2 - swidth / 2;
            int y = r.y + r.height * 3 / 4 + 9;
            int[] data = new int[swidth];
            ip.getRow(x, y, data, swidth);
            boolean fits = true;
            for (int j = 0; j < swidth; j++) {
                if (data[j] != 255) {
                    fits = false;
                    break;
                }
            }
            fits = fits && measured[i] > 500;
            if (r.height >= (GelAnalyzer.plotHeight - 11)) {
                fits = true;
            }
            if (!fits) {
                y = r.y - 2;
            }
            ip.drawString(s, x, y);
            //IJ.write(i+": "+x+" "+y+" "+s+" "+ip.StringWidth(s)/2);
        }
        imp.updateAndDraw();
        displayPercentages();
        //Toolbar.getInstance().setTool(Toolbar.RECTANGLE);
        reset();
    }

    void displayPercentages() {
        ResultsTable rt = ResultsTable.getResultsTable();
        rt.reset();
        //IJ.setColumnHeadings(" \tarea\tpercent");
        double total = 0.0;
        for (int i = 0; i < counter; i++) {
            total += measured[i];
        }
        if (IJ.debugMode && counter == actual.length) {
            debug();
            return;
        }
        for (int i = 0; i < counter; i++) {
            double percent = (measured[i] / total) * 100;
            rt.incrementCounter();
            rt.addValue("Area", measured[i]);
            rt.addValue("Percent", percent);
            //IJ.write((i+1)+"\t"+IJ.d2s(measured[i],3)+"\t"+IJ.d2s(percent,3));
        }
        rt.show("Results");
    }

    void debug() {
        for (int i = 0; i < counter; i++) {
            double a = (actual[i] / actual[0]) * 100;
            double m = (measured[i] / measured[0]) * 100;
            IJ.write(IJ.d2s(a, 4) + " "
                    + IJ.d2s(m, 4) + " "
                    + IJ.d2s(((m - a) / m) * 100, 4));
        }
    }

    /* @deprecated */
    public static void savePreferences(Properties prefs) {
    }
}
