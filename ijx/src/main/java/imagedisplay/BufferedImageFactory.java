package imagedisplay;

import imagej.imglib.process.ImageUtils;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.container.planar.PlanarContainer;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.logic.BitType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

public class BufferedImageFactory {

    //private static Image<?> image;
    //private static RealType<?> realType;

    @SuppressWarnings("unchecked")
    public static BufferedImage makeBufferedImage(Image<?> img) {
        RealType<?> realType = ImageUtils.getType(img);
        int[] dim = getDim3(getStandardDimensions());
        int sizeX = img.getDimension(dim[ 0]);
        int sizeY = img.getDimension(dim[ 1]);
        int sizeZ = img.getDimension(dim[ 2]);
        ArrayDataAccess ada = ((PlanarContainer) img.getContainer()).getPlane(0);


        if (realType instanceof BitType) {
            return null;
        }

        if (realType instanceof ByteType) {
            byte[] byteArray = (byte[]) ada.getCurrentStorageArray();
            BufferedImage bImage = byteArrayToRenderedImage(byteArray, sizeX, sizeY);
            return bImage;
        }

        if (realType instanceof UnsignedByteType) {
            byte[] byteArray = (byte[]) ada.getCurrentStorageArray();
            BufferedImage bImage = byteArrayToRenderedImage(byteArray, sizeX, sizeY);
            return bImage;
        }

        if (realType instanceof Unsigned12BitType) {
            return null;
        }

        if (realType instanceof ShortType) {
            return null;
        }

        if (realType instanceof UnsignedShortType) {
            short[] array = (short[]) ada.getCurrentStorageArray();
            BufferedImage bImage = unsignedShortArrayToRenderedImage(array, sizeX, sizeY);
            return bImage;
        }

        if (realType instanceof IntType) {
            return null;
        }

        if (realType instanceof UnsignedIntType) {
            return null;
        }

        if (realType instanceof FloatType) {
            return null;
        }

        if (realType instanceof LongType) {
            return null;
        }

        if (realType instanceof DoubleType) {
            return null;
        }

        throw new IllegalStateException("Unknown sample type " + realType.getClass());
    }

    public static BufferedImage byteArrayToRenderedImage(byte[] imgArray, int width, int height) {
        byte[] image_data;
        BufferedImage image = null;
        image_data = imgArray;
        DataBuffer db = new DataBufferByte(image_data, image_data.length);
        try {
            WritableRaster raster = Raster.createPackedRaster(db, width, height, 8, null);
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            image.setData(raster);
        } catch (Exception e) {
        }
        return image;
    }

    public static BufferedImage unsignedShortArrayToRenderedImage(short[] imgArray, int width, int height) {
        short[] image_data;
        BufferedImage image = null;
        image_data = imgArray;
        DataBuffer db = new DataBufferUShort(image_data, image_data.length);
        try {
            WritableRaster raster = Raster.createPackedRaster(db, width, height, 16, null);
            image = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
            image.setData(raster);
        } catch (Exception e) {
        }
        return image;
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
}
