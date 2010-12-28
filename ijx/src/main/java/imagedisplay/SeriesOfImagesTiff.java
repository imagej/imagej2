package imagedisplay;

import java.awt.*;
import java.awt.image.*;


/**
 * <p>SeriesOfImages: </p>
 * <p>Description: Contains a series of images </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * Represents a series of images
 * Using MultipageTiffFile for viewing
 *
 * @version 1.0
 */

// 
// 

public class SeriesOfImagesTiff implements SeriesFileListener, SeriesOfImages
{
    private String seriesFile;
    private MultipageTiffFile tif;
    private int numImages; // total, should = nT * nZ
    private Dimension imgDim;

    // Sequence, time-series
    private int nT; // number of time intervals
    private long interval = 0; // time interval or period in millisec.

    // through-focus, Z-axis stack
    // A z-series is a sequence of optical sections
    private int nZ; // number of Z sections



    public SeriesOfImagesTiff (String seriesFile) {
        // a time series, default interval one 'unit'
        this(seriesFile, 1);
    }


    public SeriesOfImagesTiff (String seriesFile, long interval) {
        // a time series with known time interval in milliseconds
        this(seriesFile, 1, interval);
    }


    // Time - Z-section Series
    public SeriesOfImagesTiff (String seriesFile, int zSections) {
        // a ZT series
        this(seriesFile, zSections, 0);
    }


    public SeriesOfImagesTiff (String seriesFile, int zSections, long interval) {
        this.seriesFile = seriesFile;
        this.nZ = zSections;
        this.interval = interval;
        try {
            tif = new MultipageTiffFile(this.seriesFile);
            if (tif != null) {
                imgDim = new Dimension(tif.getWidth(0), tif.getHeight(0));
                numImages = tif.getNumImages();
            }
            if (zSections > 1) {
                if (numImages % zSections != 0) {
                    // Warning: 
                }
                nT = numImages / zSections;
            }
            else {
                nT = numImages;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
        }
    }


    public int getNumImages () {
        return numImages;
    }


    public int getZSections () {
        return nZ;
    }


    public int getTimeIntervals () {
        return nT;
    }


    public double getInterval () {
        return interval;
    }


    public String getFilename () {
        return seriesFile;
    }


    public Dimension getImageDimensions () {
        return imgDim;
    }


    public BufferedImage getImage (int n) {
        if (n < numImages) {
            return tif.getImage(n);
        }
        else {
            return null;
        }
    }


    public BufferedImage getAsThumbnail (int n, int sample) {
        if (n < numImages) {
            return tif.getAsThumbnail(n, sample);
        }
        else {
            return null;
        }
    }


    public void close () {
        try {
            if (tif != null) {
                tif.close();
            }
        }
        catch (Exception e) {}
        tif = null;
    }
/** @todo add functions */

    @Override
    public int imageAdded()
      {
        boolean onLastSlice = true;
        tif.closeRead();
        tif.openRead(getFilename());
        numImages = tif.getNumImages();
        if (onLastSlice) {
            //playCtrl.gotoSlice(numImages - 1);
        }
        // tif.getImage(i);
        return numImages;
      }


}
