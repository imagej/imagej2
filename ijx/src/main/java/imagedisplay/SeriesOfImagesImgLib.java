package imagedisplay;

import imagej.dataset.Dataset;
import imagej.imglib.dataset.ImgLibDataset;
import imagej.imglib.process.ImageUtils;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.container.planar.PlanarContainer;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

/**
 * <p>SeriesOfImages: </p>
 * <p>Description: Contains a series of images </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author GBH
 * A Series of Images accessed from an imglib.Image<T>
 *
 * @version 1.0
 */
// 
// 
public class SeriesOfImagesImgLib implements SeriesOfImages {

    private String seriesFile;
    private int numImages; // total, should = nT * nZ
    private Dimension imgDim;
    // Sequence, time-series
    private int nT; // number of time intervals
    private long interval = 0; // time interval or period in millisec.
    // through-focus, Z-axis stack
    // A z-series is a sequence of optical sections
    private int nZ; // number of Z sections
    Image<?> img;
    Dataset dataSet;

    public SeriesOfImagesImgLib(Image<?> img) {
        this(img, 500);
    }

    public SeriesOfImagesImgLib(Image<?> img, long interval) {
        this.img = img;
        System.out.println("Channels: " + ImageUtils.getNChannels(img));
        this.interval = interval;
        nZ = ImageUtils.getNSlices(img);
        nT = ImageUtils.getNFrames(img);
        imgDim = new Dimension(ImageUtils.getWidth(img), ImageUtils.getHeight(img));
        numImages = nZ * nT;
        //
        dataSet = new ImgLibDataset(img);

        //dataset.getSubset(index);
        ArrayDataAccess ada = ((PlanarContainer) img.getContainer()).getPlane(0);
        Object array = ada.getCurrentStorageArray();
    }

    public int getNumImages() {
        return numImages;
    }

    public int getZSections() {
        return nZ;
    }

    public int getTimeIntervals() {
        return nT;
    }

    public double getInterval() {
        return interval;
    }

    public String getFilename() {
        return seriesFile;
    }

    public Dimension getImageDimensions() {
        return imgDim;
    }

    public BufferedImage getImage(int n) {
        if (n < numImages) {
            int z = n / nT;
            int t = n / nZ;
            int[] planeIndex = new int[]{z, t};
            Object array = dataSet.getSubset(planeIndex);
            return BufferedImageFactory.makeBufferedImage(array, imgDim);
        } else {
            return null;
        }
    }

    public BufferedImage getAsThumbnail(int n, int sample) {
        return null;
    }

    public Object testGetSubsetIntArray(Image<?> image, int z, int t) {
        int[] dimensions = new int[]{102, 88, 4, 3};

        RealType<?> type = new UnsignedByteType();

        ImgLibDataset ds = new ImgLibDataset(image);

        int[] planeIndex = new int[]{z, t};
        Object plane = ds.getSubset(planeIndex);

        return plane;
    }
}
