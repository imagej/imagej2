package ijx.etc;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import imagedisplay.FrameImageDisplay;
import imagej.ij1bridge.BridgeStack;
import imagej.ij1bridge.ImgLibDataset;
import imagej.ij1bridge.ImgLibProcessorFactory;
import imagej.ij1bridge.process.ImgLibProcessor;
import imagej.imglib.process.ImageUtils;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import loci.formats.FormatException;
import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.display.imagej.ImageJFunctions;
import mpicbg.imglib.image.display.imagej.ImageJVirtualStack;
import mpicbg.imglib.io.ImageOpener;
import mpicbg.imglib.type.Type;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.real.FloatType;

/**
 *
 * @author GBH
 */
public class Experimental<T extends RealType<T>> {

    private Image<?> image;
    private ImgLibDataset<?> dataset;
    private BridgeStack stack;

    @SuppressWarnings("unchecked")
    private void tryThis() throws FormatException, IOException {

        // create blank multidim Image
        int[] dimensions = new int[]{500, 400};
        this.image = ImageUtils.createImage(
                new UnsignedByteType(),
                new PlanarContainerFactory(), dimensions);
        // open test image
        String filename = "Cells.tif";
        final ImageOpener imageOpener = new ImageOpener();
        Image<T> inImg = imageOpener.openImage(filename);
        //
        this.reportInformation(inImg);
        BufferedImage bi = copyToBufferedImage(inImg);
        FrameImageDisplay fid = new FrameImageDisplay(bi, "Cells.tif");
        ImageJFunctions.displayAsVirtualStack(inImg).show();

        // Assuming we have an imglib.Image that we want to operate on using an IJ1 Plugin...
        //
        System.out.println("ImagePlus from copyToImagePlus");
        ImagePlus imp2 = ImageJFunctions.copyToImagePlus(inImg);
        System.out.println("type:" + imp2.getType());
        System.out.println("Slices: " + imp2.getNSlices());
        ImageProcessor ip2 = imp2.getStack().getProcessor(1);
        System.out.println("class: " + ip2.getClass().getName());
        //
        System.out.println("ImagePlus from BridgeStack");
        this.dataset = new ImgLibDataset<UnsignedByteType>((Image<UnsignedByteType>) this.image);
        this.stack = new BridgeStack(this.dataset, new ImgLibProcessorFactory(this.image));
        ImagePlus imp = new ImagePlus("imglib Stack", stack);
        System.out.println("type:" + imp.getType());
        System.out.println("Slices: " + imp.getNSlices());
        ImageProcessor ip = imp.getStack().getProcessor(1);
        ImgLibProcessor ilp = (ImgLibProcessor)ip;
        System.out.println("ImgLibProcessor type: " + ilp.getTypeName());

    }

//    public static BufferedImage toCompatibleImage(BufferedImage image) {
//        if (image.getColorModel().equals(CONFIGURATION.getColorModel())) {
//            return image;
//        }
//        BufferedImage compatibleImage = CONFIGURATION.createCompatibleImage(
//                image.getWidth(), image.getHeight(), image.getTransparency());
//        Graphics g = compatibleImage.getGraphics();
//        g.drawImage(image, 0, 0, null);
//        g.dispose();
//
//        return compatibleImage;
//    }
    private static final GraphicsConfiguration CONFIGURATION =
            GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice().getDefaultConfiguration();

    public static <T extends Type<T>> BufferedImage copyToBufferedImage(final Image<T> img) {
        int[] dim = getDim3(getStandardDimensions());
        final int dimPos[] = new int[img.getNumDimensions()];
        final int dimX = dim[ 0];
        final int dimY = dim[ 1];
        final int dimZ = dim[ 2];
		int sizeX = img.getDimension( dim[ 0 ] );
		int sizeY = img.getDimension( dim[ 1 ] );
		int sizeZ = img.getDimension( dim[ 2 ] );

        int[] rgbPixels = ImageJVirtualStack.extractSliceRGB(img, img.getDisplay(), dimX, dimY, dimPos);
        BufferedImage bImage = CONFIGURATION.createCompatibleImage(sizeX, sizeY, Transparency.OPAQUE);
        setPixels(bImage, 0, 0, dimX, dimY, rgbPixels);
        return bImage;


    }

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

    protected static int[] getStandardDimensions() {
        final int[] dim = new int[3];
        dim[ 0] = 0;
        dim[ 1] = 1;
        dim[ 2] = 2;
        return dim;
    }

    protected static int[] getDim3(int[] dim) {
        int[] dimReady = new int[3];
        dimReady[ 0] = -1;
        dimReady[ 1] = -1;
        dimReady[ 2] = -1;
        for (int d = 0; d < Math.min(dim.length, dimReady.length); d++) {
            dimReady[d] = dim[d];
        }
        return dimReady;
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
