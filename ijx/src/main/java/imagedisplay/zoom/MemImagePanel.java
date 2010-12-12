package imagedisplay.zoom;

/**
ImageCanvas
 */
import imagedisplay.zoom.core.ZoomGraphics;
import imagedisplay.zoom.core.ZoomJPanel;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;


public class MemImagePanel
        extends ZoomJPanel {

    BufferedImage img;
    MemoryImageSource mis;
    byte[] imageArrayByte = null;
    int w = 0;
    int h = 0;
    int frames = 0;
    byte startIntensity = 10;

    // GBH: Added ROI selection functions
    Rectangle roiRect = null;
    Color roiRectColor = Color.YELLOW;

    public MemImagePanel(int w, int h) {
        this.w = w;
        this.h = h;
        createImageSource(w, h);
    }

    public Rectangle getRoiRect() {
        return roiRect;
    }


//   BufferedImage img = loadImage(
//         edu.mbl.jif.Constants.testDataPath +
//         "images\\PSCollagenDark.gif"); //589x421
    public void paintBackground(ZoomGraphics zg) {
        this.setBackground(Color.GRAY);
        //zg.drawImage(img, null, 0, 0);
        String s = "frame" + frames;
        zg.drawImage(img, null, 0, 0);
        zg.setColor(Color.black);
        zg.drawString(s, 20, 20);
    }

    public void createImageSource(int w, int h) {
        this.w = w;
        this.h = h;
        imageArrayByte = new byte[h * w];
        for (int i = 0; i < imageArrayByte.length; i++) {
            imageArrayByte[i] = startIntensity;
        }

        // create a ColorModel with a gray scale palette:
        byte[] gray = new byte[256];
        for (int i = 0; i < 256; i++) {
            gray[i] = (byte) i;
        }
        IndexColorModel cm = new IndexColorModel(8, 256, gray, gray, gray);

        // construct a MemoryImageSource that can be used to create the image
        mis = new MemoryImageSource(w, h, cm, imageArrayByte, 0, w);
        mis.setAnimated(true);
        mis.setFullBufferUpdates(true);
        //mis.addConsumer(new ImageConsumer( );
        // in a component class you can use createImage directly
        //img = createImage(mis);
        img = toBufferedImage(Toolkit.getDefaultToolkit().createImage(mis));

    }

    public void animate() {
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < imageArrayByte.length; j++) {
                imageArrayByte[j] = (byte) (startIntensity + (byte) (i * 2));
            }
            System.out.println(i);
            mis.newPixels();
            super.paint(getGraphics());
            frames++;
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
        }
    }

    public void callBack() {
        frames++;
        mis.newPixels();
    }
    public final static int TRANSPARENT_TO_REMOVE = new Color(255, 0, 255).getRGB();
    /**
     * Description of the Field
     */
    public final static int TRANSPARENT_PIXEL = new Color(255, 0, 0, 0).getRGB();

    public static BufferedImage toBufferedImage(Image image) {
        if (image == null) {
            return null;
        }
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        // This code ensures that all the pixels in
        // the image are loaded.
        image = new ImageIcon(image).getImage();
        // Create the buffered image.
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        // Copy image to buffered image.
        Graphics2D g = bufferedImage.createGraphics();
        // paint the image.
        g.drawImage(image, 0, 0, null);
        g.dispose();
        // Get the DataBuffer from your bufferedImage like
        DataBuffer db = bufferedImage.getRaster().getDataBuffer();
        // then you can just iterate through and convert your chosencolor to a transparent color
        for (int i = 0, c = db.getSize(); i < c; i++) {
            if (db.getElem(i) == TRANSPARENT_TO_REMOVE) {
                // set to transparent
                db.setElem(i, TRANSPARENT_PIXEL);
            }
        }
        return bufferedImage;
    }

    public static void main(String[] args) {
        final MemImagePanel memImg = new MemImagePanel(200, 200);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JFrame f = new JFrame();
                f.add(memImg);
                f.setPreferredSize(new Dimension(220, 220));
                f.pack();
                f.setVisible(true);

                memImg.animate();
            }
        });
    }
}
