package ijx.etc;

import imagedisplay.BufferedImageFactory;
import imagedisplay.NavigableImageFrame;
import imagedisplay.ScannerTZ;
import imagedisplay.SeriesOfImagesImgLib;
import imagej.dataset.Dataset;
import imagej.dataset.PlanarDatasetFactory;
import imagej.ij1bridge.BridgeStack;
import imagej.imglib.TypeManager;
import imagej.imglib.process.ImageUtils;
import imagej.process.Index;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import javax.swing.JFrame;
import loci.formats.FormatException;
import mpicbg.imglib.container.basictypecontainer.PlanarAccess;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.io.ImageOpener;
import mpicbg.imglib.type.Type;
import mpicbg.imglib.type.numeric.RealType;

/**
 *
 * @author GBH
 */
public class Experimental<T extends RealType<T>> {

    private Image<?> image;
    //private ImgLibDataset<?> dataset;
    private BridgeStack stack;

    @SuppressWarnings("unchecked")
    private void tryThis() throws FormatException, IOException {
        //new ImageJX();
        // create blank multidim Image
//        int[] dimensions = new int[]{500, 400};
//        this.image = ImageUtils.createImage(
//                new UnsignedByteType(),
//                new PlanarContainerFactory(), dimensions);
        // open test image


        //String filename = "testpattern.tif";
        //String filename = "HyperStack.tif";
        String filename = "Z5_T10.tif";
        final ImageOpener imageOpener = new ImageOpener();
        Image<T> img = imageOpener.openImage(filename);
        RealType<?> type = imagej.imglib.ImageUtils.getType(img);
        System.out.println("type: " + type.getClass().getName());
        reportInformation(img);
        System.out.println("container: " + img.getContainer().getClass().getName());
        System.out.println("Channels: "
                + ImageUtils.getNChannels(img));
        System.out.println("Slices: "
                + ImageUtils.getNSlices(img));
        System.out.println("Samples: "
                + ImageUtils.getTotalSamples(img));
        System.out.println("Frames: "
                + ImageUtils.getNFrames(img));


        BufferedImage bi = BufferedImageFactory.makeBufferedImage(img);

//        ImgLibDataset<T> dataset = new ImgLibDataset<T>((Image<T>) img);
//        dataset.getSubset(index);
//        ArrayDataAccess ada = ((PlanarContainer) img.getContainer()).getPlane(0);
//        Object array = ada.getCurrentStorageArray();


        // BufferedImage bi = toBufferedImageByte(img);
        //bi.getData().getDataBuffer().getDataType();
        BufferedImage bic = toCompatibleImage(bi);

        //BufferedImage bi = copyToBufferedImage(img);
        //FrameImageDisplay fid = new FrameImageDisplay(bic, "testpattern.tif");
        new NavigableImageFrame(bic);
        openScannerTZ(img);
        //new DisplayJAIFrame(bic);




//        ImgLibDataset<T> dataset = new ImgLibDataset<T>((Image<T>) img);
//        BridgeStack stack = new BridgeStack(dataset, new ImgLibProcessorFactory(img));
//        IjxImagePlus imp = ijx.IJ.getFactory().newImagePlus("imglib Stack", (IjxImageStack)stack);
//        System.out.println("type:" + imp.getType());
//        System.out.println("Slices: " + imp.getNSlices());
//        ImageProcessor ip = imp.getStack().getProcessor(1);
//        ImgLibProcessor ilp = (ImgLibProcessor) ip;
//        System.out.println("ImgLibProcessor type: " + ilp.getTypeName());
//        imp.show();


//        //
//        ImageJFunctions.displayAsVirtualStack(inImg).show();
//
//        // Assuming we have an imglib.Image that we want to operate on using an IJ1 Plugin...
//        //
//        System.out.println("ImagePlus from copyToImagePlus");
//        ImagePlus imp2 = ImageJFunctions.copyToImagePlus(inImg);
//        System.out.println("type:" + imp2.getType());
//        System.out.println("Slices: " + imp2.getNSlices());
//        ImageProcessor ip2 = imp2.getStack().getProcessor(1);
//        System.out.println("class: " + ip2.getClass().getName());
        //

    }

    public static void openScannerTZ(Image<?> img) {
        JFrame frame = new JFrame("ScannerTZ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int zSections = 1;
        ScannerTZ stz = new ScannerTZ(new SeriesOfImagesImgLib(img, 0));
        frame.getRootPane().setContentPane(stz);
        frame.setSize(400, 500);
        frame.pack();
        frame.setVisible(true);
    }

    public static BufferedImage toCompatibleImage(BufferedImage image) {
        if (image.getColorModel().equals(CONFIGURATION.getColorModel())) {
            return image;
        }
        BufferedImage compatibleImage = CONFIGURATION.createCompatibleImage(
                image.getWidth(), image.getHeight(), image.getTransparency());
        Graphics g = compatibleImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return compatibleImage;
    }
    private static final GraphicsConfiguration CONFIGURATION =
            GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice().getDefaultConfiguration();

//    public static <T extends Type<T>> BufferedImage copyToBufferedImage(final Image<T> img) {
//        int[] dim = getDim3(getStandardDimensions());
//        final int dimPos[] = new int[img.getNumDimensions()];
//        final int dimX = dim[ 0];
//        final int dimY = dim[ 1];
//        final int dimZ = dim[ 2];
//        int sizeX = img.getDimension(dim[ 0]);
//        int sizeY = img.getDimension(dim[ 1]);
//        int sizeZ = img.getDimension(dim[ 2]);
//
//        int[] rgbPixels = ImageJVirtualStack.extractSliceRGB(img, img.getDisplay(), dimX, dimY, dimPos);
//        BufferedImage bImage = CONFIGURATION.createCompatibleImage(sizeX, sizeY, Transparency.OPAQUE);
//        setPixels(bImage, 0, 0, dimX, dimY, rgbPixels);
//        return bImage;
//    }
    public static void setPixels(BufferedImage img,
            int x, int y, int w, int h, int[] pixels) {
        if (pixels == null || w == 0 || h == 0) {
            return;
        } else if (pixels.length < w * h) {
            throw new IllegalArgumentException("pixels array must have a length >= w*h");
        }
        int imageType = img.getType();
        if (imageType == BufferedImage.TYPE_INT_ARGB
                || imageType == BufferedImage.TYPE_INT_RGB) {
            WritableRaster raster = img.getRaster();
            raster.setDataElements(x, y, w, h, pixels);
        } else {
            // Unmanages the image
            img.setRGB(x, y, w, h, pixels, 0, w);
        }
    }

    /** Prints out some useful information about the {@link Image}. */
    public static <T extends RealType<T>> void reportInformation(Image<T> img) {
        System.out.println(img);
        final Cursor<T> cursor = img.createCursor();
        cursor.fwd();
        System.out.println("\tType = " + cursor.getType().getClass().getName());
        System.out.println("\tContainer = " + cursor.getStorageContainer().getClass().getName());
        cursor.close();
        img.toString();
    }

    public static <T extends RealType<T>> void main(String[] args)
            throws FormatException, IOException {
        Experimental<T> test = new Experimental<T>();
        test.tryThis();
    }
}
