import ij.*;
import ij.plugin.PlugIn;
import ij.process.*;
import ij.measure.*;
import ij.gui.*;
import ij.util.Tools;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * This plugin continuously plots the profile along a line scan or a rectangle.
 * The profile is updated if the image changes, thus it can be used to monitor
 * the effect of a filter during preview.
 * Plot size etc. are set by Edit>Options>Profile Plot Options
 *
 * Restrictions:
 * - Plots of rectangular selections are always horizontal, irrespective of the
 *   "vertical profile" option in Edit>Options>Profile Plot Options.
 * - The plot window is not calibrated. Use Analyze>Plot Profile to get a
 *   spatially calibrated plot window where you can do measurements.
 *
 * By Wayne Rasband and Michael Schmid
 * Version 2007-Dec-06
 */
public class Dynamic_Profiler
        implements PlugIn, MouseListener, MouseMotionListener, KeyListener, ImageListener, Runnable {
    //MouseListener, MouseMotionListener, KeyListener: to detect changes to the selection of an ImagePlus
    //ImageListener: listens to changes (updateAndDraw) and closing of an image
    //Runnable: for background thread
    private ImagePlus imp;                  //the ImagePlus that we listen to and the last one
    private ImagePlus plotImage;            //where we plot the profile
    private Thread bgThread;                //thread for plotting (in the background)
    private boolean doUpdate;               //tells the background thread to update

    /* Initialization and plot for the first time. Later on, updates are triggered by the listeners **/
    public void run(String arg) {
        imp = WindowManager.getCurrentImage();
        if (imp==null) {
            IJ.noImage(); return;
        }
        if (!isSelection()) {
            IJ.error("Dynamic Profiler","Line or Rectangular Selection Required"); return;
        }
        ImageProcessor ip = getProfilePlot();  // get a profile
        if (ip==null) {                     // no profile?
            IJ.error("Dynamic Profiler","No Profile Obtained"); return;
        }
                                            // new plot window
        plotImage = new ImagePlus("Profile of "+imp.getShortTitle(), ip);
        plotImage.show();
        IJ.wait(50);
        positionPlotWindow();
                                            // thread for plotting in the background
        bgThread = new Thread(this, "Dynamic Profiler Plot");
        bgThread.setPriority(Math.max(bgThread.getPriority()-3, Thread.MIN_PRIORITY));
        bgThread.start();
        createListeners();
    }

    // these listeners are activated if the selection is changed in the corresponding ImagePlus
    public synchronized void mousePressed(MouseEvent e) { doUpdate = true; notify(); }   
    public synchronized void mouseDragged(MouseEvent e) { doUpdate = true; notify(); }
    public synchronized void mouseClicked(MouseEvent e) { doUpdate = true; notify(); }
    public synchronized void keyPressed(KeyEvent e) { doUpdate = true; notify(); }
    // unused listeners concering actions in the corresponding ImagePlus
    public void mouseReleased(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void imageOpened(ImagePlus imp) {}

    // this listener is activated if the image content is changed (by imp.updateAndDraw)
    public synchronized void imageUpdated(ImagePlus imp) {
        if (imp == this.imp) { doUpdate = true; notify(); }
    }

    // if either the plot image or the image we are listening to is closed, exit
    public void imageClosed(ImagePlus imp) {
        if (imp == this.imp || imp == plotImage) {
            removeListeners();
            closePlotImage();                       //also terminates the background thread
        }
    }

    // the background thread for plotting.
    public void run() {
        while (true) {
            IJ.wait(50);                            //delay to make sure the roi has been updated
            ImageProcessor ip = getProfilePlot();
            if (ip != null) plotImage.setProcessor(null, ip);
            synchronized(this) {
                if (doUpdate) {
                    doUpdate = false;               //and loop again
                } else {
                    try {wait();}                   //notify wakes up the thread
                    catch(InterruptedException e) { //interrupted tells the thread to exit
                        return;
                    }
                }
            }
        }
    }

    private synchronized void closePlotImage() {    //close the plot window and terminate the background thread
        bgThread.interrupt();
        plotImage.getWindow().close();
    }

    private void createListeners() {
        ImageWindow win = imp.getWindow();
        ImageCanvas canvas = win.getCanvas();
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addKeyListener(this);
        imp.addImageListener(this);
        plotImage.addImageListener(this);
    }

    private void removeListeners() {
        ImageWindow win = imp.getWindow();
        ImageCanvas canvas = win.getCanvas();
        canvas.removeMouseListener(this);
        canvas.removeMouseMotionListener(this);
        canvas.removeKeyListener(this);
        imp.removeImageListener(this);
        plotImage.removeImageListener(this);
    }

    /** Place the plot window to the right of the image window */
    void positionPlotWindow() {
        IJ.wait(500);
        if (plotImage==null || imp==null) return;
        ImageWindow pwin = plotImage.getWindow();
        ImageWindow iwin = imp.getWindow();
        if (pwin==null || iwin==null) return;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension plotSize = pwin.getSize();
        Dimension imageSize = iwin.getSize();
        if (plotSize.width==0 || imageSize.width==0) return;
        Point imageLoc = iwin.getLocation();
        int x = imageLoc.x+imageSize.width+10;
        if (x+plotSize.width>screen.width)
            x = screen.width-plotSize.width;
        pwin.setLocation(x, imageLoc.y);
        iwin.toFront();
    }

    /** get a profile, analyze it and return a plot (or null if not possible) */
    ImageProcessor getProfilePlot() {
        if (!isSelection()) return null;
        ImageProcessor ip = imp.getProcessor();
        Roi roi = imp.getRoi();
        if (ip == null || roi == null) return null; //these may change asynchronously
        if (roi.getType() == Roi.LINE)
            ip.setInterpolate(PlotWindow.interpolate);
        else
            ip.setInterpolate(false);
        ProfilePlot profileP = new ProfilePlot(imp);//get the profile
        if (profileP == null) return null;
        double[] profile = profileP.getProfile();
        if (profile==null || profile.length<2)
            return null;
        String xUnit = "pixels";                    //the following code is mainly for x calibration
        double xInc = 1;
        Calibration cal = imp.getCalibration();
        if (roi.getType() == Roi.LINE) {
            Line line = (Line)roi;
            if (cal != null) {
                double dx = cal.pixelWidth*(line.x2 - line.x1);
                double dy = cal.pixelHeight*(line.y2 - line.y1);
                double length = Math.sqrt(dx*dx + dy*dy);
                xInc = length/(profile.length-1);
                xUnit = cal.getUnits();
            }
        } else if (roi.getType() == Roi.RECTANGLE) {
            if (cal != null) {
                xInc = roi.getBounds().getWidth()*cal.pixelWidth/(profile.length-1);
                xUnit = cal.getUnits();
            }
        } else return null;
        String xLabel = "Distance (" + xUnit + ")";
        String yLabel = (cal !=null && cal.getValueUnit()!=null && !cal.getValueUnit().equals("Gray Value")) ?
            "Value ("+cal.getValueUnit()+")" : "Value";

        int n = profile.length;                 // create the x axis
        double[] x = new double[n];
        for (int i=0; i<n; i++)
            x[i] = i*xInc;

        Plot plot = new Plot("profile", xLabel, yLabel, x, profile);
        return plot.getProcessor();
    }


    /** returns true if there is a simple line selection or rectangular selection */
    boolean isSelection() {
        if (imp==null)
            return false;
        Roi roi = imp.getRoi();
        if (roi==null)
            return false;
        return roi.getType()==Roi.LINE || roi.getType()==Roi.RECTANGLE;
    }
}
