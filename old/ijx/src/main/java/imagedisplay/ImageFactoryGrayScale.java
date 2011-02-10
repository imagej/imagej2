package imagedisplay;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.*;
import java.awt.image.*;

import java.util.Hashtable;

//import javax.media.jai.RasterFactory;
//import com.sun.media.jai.codec.ImageCodec;

import javax.swing.*;


public class ImageFactoryGrayScale {
    
    // create from short[]
    public static BufferedImage createImage(int imageWidth, int imageHeight, int imageDepth,
        short[] data) {
        ComponentColorModel ccm =
            new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
            new int[]{imageDepth}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
        ComponentSampleModel csm =
            new ComponentSampleModel(DataBuffer.TYPE_USHORT, imageWidth, imageHeight, 1, imageWidth,
            new int[]{0});
        DataBuffer dataBuf = new DataBufferUShort((short[]) data, imageWidth);
        WritableRaster wr = Raster.createWritableRaster(csm, dataBuf, new Point(0, 0));
        Hashtable ht = new Hashtable();
        ht.put("owner", "edu.mbl.jif");
        return new BufferedImage(ccm, wr, true, ht);
    }

    // create from byte[]
    public static BufferedImage createImage(int imageWidth, int imageHeight, int imageDepth,
        byte[] data) {
        ComponentColorModel ccm =
            new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
            new int[]{imageDepth}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        ComponentSampleModel csm =
            new ComponentSampleModel(DataBuffer.TYPE_BYTE, imageWidth, imageHeight, 1, imageWidth,
            new int[]{0});
        DataBuffer dataBuf = new DataBufferByte((byte[]) data, imageWidth);
        WritableRaster wr = Raster.createWritableRaster(csm, dataBuf, new Point(0, 0));
        Hashtable ht = new Hashtable();
        ht.put("owner", "PSj");
        return new BufferedImage(ccm, wr, true, ht);
    }

    //---------------------------------------------------------------------------
   /*      public static BufferedImage createImage (
    int imageWidth, int imageHeight,
    int imageDepth, float[] data) {
    
    ComponentColorModel ccm = new ComponentColorModel(
    ColorSpace.getInstance(ColorSpace.CS_GRAY),
    new int[] {imageDepth}, false, false,
    Transparency.OPAQUE, DataBuffer.TYPE_FLOAT);
    
    ComponentSampleModel csm = new ComponentSampleModel(
    DataBuffer.TYPE_FLOAT, imageWidth, imageHeight, 1, imageWidth,
    new int[] {0});
    
    DataBuffer dataBuf = new DataBufferFloat((float[]) data, imageWidth);
    
    WritableRaster wr =
    Raster.createWritableRaster(csm, dataBuf, new Point(0, 0));
    
    Hashtable ht = new Hashtable();
    ht.put("owner", "PSj");
    return new BufferedImage(ccm, wr, true, ht);
    }
     */    // create from float[]
//    public static BufferedImage createImage(int imageWidth, int imageHeight, int imageDepth,
//        float[] pixels) {
//        int w = imageWidth;
//        int h = imageHeight;
//        int nbBands = 1;
//        int[] rgbOffset = new int[nbBands];
//        SampleModel sampleModel =
//            RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_FLOAT, w, h, nbBands,
//            nbBands * w, rgbOffset);
//        ColorModel colorModel = ImageCodec.createComponentColorModel(sampleModel);
//        DataBufferFloat dataBuffer = new DataBufferFloat(pixels, pixels.length);
//        WritableRaster raster =
//            RasterFactory.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));
//        return new BufferedImage(colorModel, raster, false, null);
//    }

    //Test -------------------------------------------------------------------
//    public static BufferedImage testImageFloat() {
//        int wid = 256;
//        int ht = 256;
//        float max = 1.0f;
//        int len = wid * ht;
//        double scale = max / (float) len;
//        float[] data = new float[wid * ht];
//        for (int i = 0; i < len; i++) {
//            data[i] = (float) ((float) i * scale);
//        }
//        BufferedImage bi = createImage(wid, ht, 32, data);
//        return bi;
//    }

    public static BufferedImage testImageByte() {
        return testImageByte(256, 256);
    }

    public static BufferedImage testImageByte(int wid, int ht) {
        float max = 256f;
        int len = wid * ht;
        byte[] data = new byte[len];
        double scale = max / (float) len;
        for (int i = 0; i < len; i++) {
            data[i] = (byte) ((float) i * scale);
        }

        //      for (int i = 0; i < ht; i++) {
        //         for (int j = 0; j < wid; j++) {
        //            data[(i * ht) + j] = (byte) (Math.sqrt(j + 1 * i + 1));
        //         }
        //      }
        BufferedImage bi = createImage(wid, ht, 8, data);
        return bi;
    }

    public static BufferedImage testImageShort16() {
        int wid = 256;
        int ht = 256;
        short[] data = new short[wid * ht];
        float max = 64000f;
        int len = wid * ht;
        double scale = max / (float) len;
        for (int i = 0; i < len; i++) {
            data[i] = (short) ((float) i * scale);
        }
        BufferedImage bi = createImage(wid, ht, 16, data);
        return bi;
    }

    public static BufferedImage testImageShort12() {
        int wid = 256;
        int ht = 256;
        short[] data = new short[wid * ht];
        float max = 4095f;
        int len = wid * ht;
        double scale = max / (float) len;
        for (int i = 0; i < len; i++) {
            data[i] = (short) ((float) i * scale);
        }
        BufferedImage bi = createImage(wid, ht, 12, data);
        return bi;
    }

    public static void main(String[] args) {
        ImageFactoryGrayScale gs = new ImageFactoryGrayScale();
        BufferedImage biByte = testImageByte();
        displayImage(biByte, biByte.getWidth(), biByte.getHeight(), "Byte");
//        BufferedImage biFloat = testImageFloat();
//        displayImage(biFloat, biFloat.getWidth(), biFloat.getHeight(), "Float");
    }

    public static void displayImage(BufferedImage img, int wid, int ht, String title) {
        JFrame fr = new JFrame();
        fr.setTitle(title);
        fr.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        ImagePanel pan = new ImagePanel(img);
        pan.setPreferredSize(new Dimension(wid, ht));
        fr.getContentPane().add(pan);
        fr.pack();
        //fr.setSize(wid, ht);
        fr.setVisible(true);
    }

    // For testing...
    static class ImagePanel extends JComponent {

        protected BufferedImage image;

        public ImagePanel() {
        }

        public ImagePanel(BufferedImage img) {
            image = img;
        }

        public void setImage(BufferedImage img) {
            image = img;
        }

        public void paint(Graphics g) {
            Rectangle rect = this.getBounds();
            if (image != null) {
                g.drawImage(image, 0, 0, rect.width, rect.height, null);
            }
        }
    }
}
