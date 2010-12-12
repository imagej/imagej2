package imagedisplay;

import imagedisplay.util.CheckThreadViolationRepaintManager;
import imagedisplay.zoom.core.ZoomGraphics;

import java.awt.*;
import java.awt.image.BufferedImage;

import java.io.File;


import javax.imageio.ImageIO;

import javax.swing.*;

/**
 * <p>Title: </p>
 *
 * <p>Description:
 * Test of ImageDisplayPanel
 * This uses ZoomControl16 with MouseSensitiveZSP
 * <p>Copyright: Copyright (c) 2006</p>
 * @author gbh at mbl.edu
 * @version 1.0
 */
public class FrameImageDisplay extends JFrame {

    BorderLayout borderLayout1 = new BorderLayout();
    ImageDisplayPanel viewPanel;
    int w;
    int h;
    BufferedImage img = null;
    String title = "(none)";

    public FrameImageDisplay(BufferedImage _img, String title) {
        this(_img);
        this.setTitle(title);
    }

    public FrameImageDisplay(BufferedImage __img) {
        this(__img.getWidth(), __img.getHeight(), __img);
    }

    public FrameImageDisplay(int w, int h, BufferedImage img) {
        this.w = w;
        this.h = h;
        this.img = img;
        try {
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public ImageDisplayPanel getImageDisplayPanel() {
        return viewPanel;
    }

    private void jbInit() throws Exception {
//        try {
//            com.jgoodies.looks.plastic.Plastic3DLookAndFeel lookFeel =
//                    new com.jgoodies.looks.plastic.Plastic3DLookAndFeel();
//            com.jgoodies.looks.plastic.PlasticLookAndFeel.setPlasticTheme(
//                    //.setMyCurrentTheme(//new com.jgoodies.looks.plastic.theme.DesertBluer());
//                    // new com.jgoodies.looks.plastic.theme.Silver());
//                    //new com.jgoodies.looks.plastic.theme.SkyBluerTahoma());
//                    new com.jgoodies.looks.plastic.theme.DesertBlue());
//            com.jgoodies.looks.plastic.PlasticLookAndFeel.setTabStyle("Metal");
//            UIManager.setLookAndFeel(lookFeel);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        getContentPane().setLayout(borderLayout1);
        Dimension imageDim = new Dimension(w, h);

        viewPanel = new ImageDisplayPanel(imageDim);
        viewPanel.addMagnifierButton();
        this.add(viewPanel, BorderLayout.CENTER);
        if (img != null) {
            viewPanel.changeImage(img);
        }
        this.setTitle(title);
        // size it
        setSize(sizeFrameForDefaultScreen(imageDim));
        setLocation(nextFramePosition());
        //new TestAppHarness(fid);
        setVisible(true);
        viewPanel.fitImageToWindow();

    }
    // +++ On maximizeFrame and restore, do FitToWindow
    private static Point lastFramePosition = new Point(5, 5);

    public void changeImage(BufferedImage img) {
        viewPanel.changeImage(img);
    }

    public static void main(String[] args) {
        //set CheckThreadViolationRepaintManager
        RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                runTest();
            }
        });
    }

    public static void runTest() {
        //      SwingUtilities.invokeLater(new Runnable()
        //      {
        //         public void run () {
        //            File file = new File(
        //            edu.mbl.jif.Constants.testDataPath + "Aster_azim.tif");
        //            // @todo Opener...
        //            BufferedImage img = null;
        //            try {
        //               img = ImageIO.read(file);
        //               if(img == null){
        //                  System.err.println("Couldn't load pond");
        //                  return;
        //               }
        //               FrameImageDisplayTest f = new FrameImageDisplayTest(img.getWidth(),
        //                     img.getHeight(), img);
        //
        //               f.setVisible(true); }
        //            catch (IOException ex) {
        //            }

        //         }
        //      });
        int width = 100;
        int height = 100;
        BufferedImage bi = ImageFactoryGrayScale.testImageByte(width, height);

        FrameImageDisplay fid = new FrameImageDisplay(toCompatibleImage(bi), "byte");

        // Add an overlay
        GraphicOverlay overlay = new GraphicOverlay() {

            public void drawGraphicOverlay(ZoomGraphics zg) {
                zg.setColor(Color.red);
                zg.drawRect(20, 20, 40, 40);
            }
        };
        fid.getImageDisplayPanel().imagePane.addGraphicOverlay(overlay);
        // size it
//       // fid.setSize(StaticSwingUtils.sizeFrameForDefaultScreen(new Dimension(width, height)));
//        fid.setLocation(StaticSwingUtils.nextFramePosition());
//        //new TestAppHarness(fid);
//        fid.setVisible(true);
//        fid.viewPanel.fitImageToWindow();

        //new TestAppHarness((new FrameImageDisplay(ImageFactoryGrayScale.testImageFloat(), "floater")));
        //(new FrameImageDisplay(ImageFactoryGrayScale.testImageShort12(), "12")).setVisible(true);
        //(new FrameImageDisplay(ImageFactoryGrayScale.testImageShort16(), "16")).setVisible(true);
        //(new FrameImageDisplay(ImageFactoryGrayScale.testImageFloat(), "floater")).setVisible(true);
        // test with
        // ReallyBigXYImage
        //(new FrameImageDisplay(load("D:/_TestImages/2000Square8bit.tif"), "2000 Square")).setVisible(true);
    }

    public static BufferedImage load(String file) {
        Image image = null;
        try {
            image = ImageIO.read(new File(file));
        } catch (Exception e) {
            System.out.println("Exception loading: " + file);
        }
        return (BufferedImage) image;
    }

    public static Dimension sizeFrameForDefaultScreen(Dimension imageDim) {
        int xBuffer = 10;
        int yBuffer = 10;
        int boundaryH = 64;
        int boundaryW = 20;
        //Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle screen = defaultScreen.getDefaultConfiguration().getBounds();
        int maxH = screen.height - ((2 * yBuffer) + boundaryH);
        int maxW = screen.width - ((2 * xBuffer) + boundaryW);
        Dimension frameSize = new Dimension();
        int adjustedW = (imageDim.width < maxW) ? imageDim.width : maxW;
        // but width not less than... 480;
        adjustedW = (adjustedW < 480) ? 480 : adjustedW;
        int adjustedH = (int) (((float) imageDim.height / (float) imageDim.width) * adjustedW);
        if (adjustedH > maxH) {
            adjustedH = maxH;
            adjustedW = (int) (((float) imageDim.width / (float) imageDim.height) * adjustedH);
        }
        frameSize.width = adjustedW + boundaryW;
        frameSize.height = adjustedH + boundaryH;
        return frameSize;
    }

    public static Point nextFramePosition() {
        lastFramePosition.x = lastFramePosition.x + 5;
        lastFramePosition.y = lastFramePosition.y + 5;
        if (lastFramePosition.x > 200) {
            lastFramePosition.x = 5;
            lastFramePosition.y = 5;
        }
        return lastFramePosition;
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
}
