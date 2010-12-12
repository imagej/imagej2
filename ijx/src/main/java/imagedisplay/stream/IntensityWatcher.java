/*
 * IntensityWatcher.java
 *
 * Created on June 20, 2006, 11:30 AM
 */
package imagedisplay.stream;


// import edu.mbl.jif.io.CSVFileWrite;

import imagedisplay.PixelChangeEvent;
import imagedisplay.PixelChangeListener;
import imagedisplay.RoiChangeEvent;
import imagedisplay.RoiChangeListener;
import java.util.concurrent.locks.Condition;

//import info.monitorenter.gui.chart.ITrace2D;
//import info.monitorenter.gui.chart.TracePoint2D;
//import info.monitorenter.gui.chart.Chart2D;
//import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
//import info.monitorenter.gui.chart.traces.Trace2DLtd;
//import info.monitorenter.util.Range;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author GBH
 */
public class IntensityWatcher
        implements IntensityWatcherInterface, PixelChangeListener, RoiChangeListener {

    int n = 0;
    int freq = 6;
    String filename;
    //CSVFileWrite file;
    //ITrace2D trace = null;
    long startTime;
    float meanValue;
    float lastMeanValue = 0;
    float delta;
    private float stabilityLevel = 1.0f;
    private boolean stable;
    private int timeToStable;


    public IntensityWatcher() {
        this(6);
    }

    public IntensityWatcher(int freq) {
        this.freq = freq;
    }

    public IntensityWatcher(int freq, String filename) {
        this.freq = freq;
        // openChartFrame();
        // open file for data
       // file = new CSVFileWrite(filename);
        startTime = System.nanoTime();
    }

    public void setMeasurementFreq(int freq) {
        this.freq = freq;
    }


    public void pixelChanged(PixelChangeEvent evnt) {
    }


    public void roiChanged(RoiChangeEvent evnt) {
        n++;
        lastMeanValue = meanValue;
        meanValue = evnt.mean;
        delta = Math.abs(lastMeanValue - meanValue);
        if (!stable) {
            if (delta < stabilityLevel) {
                stablized();
            }
        }
//        if (file != null) {
//            file.writeRow((System.nanoTime() - startTime) / 1000000, meanValue);
//        }
        if (n % freq == 0) {
            //System.out.println(evnt.mean);
//            if (trace != null) {
//                SwingUtilities.invokeLater(new Runnable() {
//
//                    public void run() {
//                        try {
//                            trace.addPoint(
//                                    new TracePoint2D(((double) System.currentTimeMillis() - startTime),
//                                    meanValue));
//                        } catch (Exception exception) {
//                        }
//
//                    }
//                });
//            }
        }
    }


//    public void logToFile(final String msg) {
//        new Thread(new Runnable() {
//            public void run() {
//                
//            }
//        }).start();
//    }
//    
    public float getMean() {
        return meanValue;
    }

    public float getStableMean(long maxMillisecs) {
        long n = maxMillisecs;
        long start = System.nanoTime();
        stable = false;
        try {
            this.waitForStable();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        timeToStable = (int) (System.nanoTime() - start) / 1000;
        return meanValue;
    }

    // nanoseconds
    public int getTimeToStable() {
        return timeToStable;
    }

    public void setStabilityLevel(float stabilityLevel) {
      System.out.println("stabilityLevel = " + stabilityLevel);
        this.stabilityLevel = stabilityLevel;
    }
    public float getStabilityLevel( ) {
      return stabilityLevel;
    }


    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();


    public void stablized() {
        lock.lock();
        try {
            stable = true;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void waitForStable() throws InterruptedException {
        lock.lock();
        try {
            while (stable == false) {
                condition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    //
//    public void openChartFrame() {
//        Chart2D chart = new Chart2D();
//        chart.getAxisY().setRangePolicy(new RangePolicyMinimumViewport(new Range(0,
//                256)));
//        // Note that dynamic charts need limited amount of values!!!
//        trace = new Trace2DLtd(200);
//        trace.setColor(Color.BLUE);
//        chart.addTrace(trace);
//        JFrame frame = new JFrame("Intensity (Avg for ROI)");
//        try {
//            frame.setIconImage((new javax.swing.ImageIcon(getClass().getResource("/edu/mbl/jif/camera/icons/plot.png"))).getImage());
//        } catch (Exception ex) {
//        }
//        frame.getContentPane().add(chart);
//        frame.setSize(400, 300);
//        // Enable the termination button [cross on the upper right edge]:
//        frame.addWindowListener(
//                new WindowAdapter() {
//                    public void windowClosing(WindowEvent e) {
//                        closeTextFile();
//                        trace = null;
//                    }
//                });
//        frame.setLocation(500, 20);
//        frame.setVisible(true);
//        startTime = System.currentTimeMillis();
//    }
//
//
//    public void closeTextFile() {
//        if (file != null) {
//            file.close();
//        }
//    }


}

