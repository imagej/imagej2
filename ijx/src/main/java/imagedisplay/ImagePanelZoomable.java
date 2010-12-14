package imagedisplay;

import imagedisplay.util.StaticSwingUtils;
import imagedisplay.zoom.core.ZoomGraphics;
import imagedisplay.zoom.core.ZoomJPanel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.event.MouseInputAdapter;

/**
 * <p>Title: </p>
 * <p>Description: This is the core of the image viewer
 *  includes.... </p>
 * Is an ImageConsumer...
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: </p.
 * @author gharris@mbl.edu
 * @version 1.0
 */
public class ImagePanelZoomable
        extends ZoomJPanel
        implements ImageConsumer, RoiChangeTalker, PixelChangeTalker {

    ImageDisplayPanel iPanel = null; // parent container
    public Dimension imageDim;
    int iWidth;
    int iHeight;
    private int depth = 8;
    private BufferedImage imageDisplayed;
    private WritableRaster wr;
    //private Image streamImageDisplayed;
    //LookupOp
    BufferedImageOp lookupOp = null;
    LookupTable lut = null;
    //
    RenderingHints hints;
    // Pixel arrays ---
    private int[] pixelArray = null;
    public byte[] imageArrayByte = null;
    private short[] imageArrayShort = null;
    private final int MAX_VALUE = 255;
    BufferedImage over; // Overlay for stamps, etc.
    byte[] incoming;  // for streaming ImageConsumer
    ArrayList overlayGraphics = new ArrayList<GraphicOverlay>();
    // Cursor ---
    public int cursorPosX = 0;
    public int cursorPosY = 0;
    int pixOffset = 0;
    //private boolean cursorInPanel = false;
    private boolean cursorInImage;
    // Elastic-band ROI ---
    public Rectangle box = new Rectangle(0, 0, 0, 0);
    private Rectangle currentRect = null;
    private Rectangle rectToDraw = null;
    private Rectangle previousRectDrawn = new Rectangle();
    // for selecting ROI or line.
    private boolean selectingROI = true;
    private boolean selectingLine = false;
    // ROIs ---
    public static Rectangle cameraSelROI = new Rectangle(0, 0, 0, 0);
    public static int cameraROIsetSize = 0;
    public static Rectangle analyzerROI = new Rectangle(0, 0, 0, 0);
    public static int analyzerROIsetSize = 0;
    public static Rectangle analyzerLine = new Rectangle(0, 0, 0, 0);
    public static Rectangle lastROI = new Rectangle(0, 0, 0, 0);
    // Listeners ---
    ArrayList pixelListeners = new ArrayList();
    ArrayList roiListeners = new ArrayList();
    MouseListenerROI mListenerROI = new MouseListenerROI();
    // How might we attach (inject?) a feature (Mode?) like MarkPoint...
    // that includes an activation button, a MouseListener, and a returned object ?
    //
    // PolStack-specific ---
    int x1Azim;
    int x2Azim;
    int y1Azim;
    int y2Azim;
    double azimCursorLength = 32;
    private boolean azimCursor = false;
    private double azimAtPixel = 0;
    double magAtPixel = 1;
    int azimAtPixelInt = 0;
    private int slice = 0;
    // Marked points --
    int displayMode = 0;    // Marked Points
    private boolean showMarkedPoints = true;

    //----------------------------------------------------------------------
    public ImagePanelZoomable(ImageDisplayPanel iPanel) {
        super();
        this.iPanel = iPanel;
        iWidth = (int) iPanel.imageDim.getWidth();
        iHeight = (int) iPanel.imageDim.getHeight();
        this.imageDim = iPanel.imageDim;
        //_observable = new MyObservable();
        //      if(iPanel.valuePanel != null){
        //         this.addPixelChangeListener(iPanel.valuePanel);
        //         this.addRoiChangeListener(iPanel.valuePanel);
        //      }
        setDefaultMouseInputAdapter(mListenerROI);
        setBackground(Color.black);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        // Rendering hints
        hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // Cause of memory leak...?  Nov 12
        // pixelArray = new int[(int) (iWidth * iHeight)];
        // Image for overlays
        // over = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_ARGB);
    }

    // <editor-fold defaultstate="collapsed" desc="<<< Show/Change Image >>>">
    // changeImage - to show a PolStack Slice
    // changeImage using byte[] source
    public void changeImage(int _slice, BufferedImage _imageDisplayed, byte[] _imageArray,
            byte[] _azimArrayByte) {
        imageDisplayed = _imageDisplayed;
        slice = _slice;
        imageArrayByte = _imageArray;
        depth = 8;
        pixelArray = null;
        //    try {
        //      // grab pixels from image into int[] pixelArray
        //      PixelGrabber grabber =
        //        new PixelGrabber(imageDisplayed, 0, 0, (int) imageDim.getWidth(),
        //          (int) imageDim.getHeight(), pixelArray, 0, (int) imageDim.getWidth());
        //      grabber.grabPixels();
        //      pgColorModel = grabber.getColorModel();
        //    } catch (InterruptedException ie) {
        //      System.out.println("PixelGrabber failed");
        //      return;
        //    }
        updateAfterChange();
    }

    // changeImage for PolStack, using short[] source
    public void changeImage(int _slice, BufferedImage _imageDisplayed, short[] _imageArray,
            short[] _azimArrayByte) {
        imageDisplayed = _imageDisplayed;
        slice = _slice;
        depth = 16;
        imageArrayShort = _imageArray;
        pixelArray = null;
        //    try {
        //      // grab pixels from image into int[] pixelArray
        //      PixelGrabber grabber =
        //        new PixelGrabber(imageDisplayed, 0, 0, (int) imageDim.getWidth(),
        //          (int) imageDim.getHeight(), pixelArray, 0, (int) imageDim.getWidth());
        //      grabber.grabPixels();
        //      pgColorModel = grabber.getColorModel();
        //    } catch (InterruptedException ie) {
        //      System.out.println("PixelGrabber failed");
        //      return;
        //    }
        updateAfterChange();
    }

    // changeImage for Other Imagetypes
    public void changeImage(BufferedImage _imageDisplayed) {
        imageDisplayed = _imageDisplayed;
        imageArrayByte = null;
        wr = _imageDisplayed.getRaster();
        //        int dataType = _imageDisplayed.getData().getDataBuffer().getDataType();
        //        if (true) { // if ObserversEnabled
        //            try {
        //                if (dataType != 3) {
        //                    if ((pixelArray != null) && (imageDisplayed != null)) {
        //                        // grab pixels from image into pixelArray (int[])
        //                        grabber = new PixelGrabber(imageDisplayed, 0, 0,
        //                                (int) imageDisplayed.getWidth(this),
        //                                (int) imageDisplayed.getHeight(this), pixelArray, 0,
        //                                (int) imageDisplayed.getWidth(this));
        //                        grabber.grabPixels();
        //
        //                        //pgColorModel = grabber.getColorModel();
        //                    }
        //                }
        //            } catch (InterruptedException ie) {
        //                System.out.println("PixelGrabber failed");
        //                return;
        //            }
        //        }
        updateAfterChange();
    }

    void updateAfterChange() {
        updateMinMeanMax();
        roiChanged();
        repaint();
        iPanel.zoomCtrl.updateZoomValue();
    }

    public void showImage(BufferedImage _imageDisplayed) {
        // Used by SeriesPlayer - no pixel/roi value updates
        imageDisplayed = _imageDisplayed;
        imageArrayByte = null;
        repaint();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="<<< Paint >>>">
  /* Paint the image(s) in the background */
    @Override
    public void paintBackground(ZoomGraphics zg) {
        this.setBackground(Color.DARK_GRAY);

        //      if (streamImageDisplayed != null) {
        //         zg.setHints(hints);
        //         zg.drawImage(imageDisplayed, null, 0, 0);
        //         Graphics g = imageDisplayed.getGraphics();
        //         g.drawImage(streamImageDisplayed, 0, 0, null);
        //      }

        zg.setHints(hints);
        if (images.isEmpty()) {
            if (imageDisplayed != null) {
                zg.drawImage(imageDisplayed, lookupOp, 0, 0);
            }
        } else {
            for (Iterator it = images.iterator(); it.hasNext();) {
                LocatedImage Limage = (LocatedImage) it.next();
                zg.drawImage(Limage.getImage(), lookupOp,
                        Limage.getPoint().getX(), Limage.getPoint().getY());
            }
        }
    }

    /* Paint the overlays in the front */
    @Override
    public void paintFront(ZoomGraphics zg) {
        //         zg.setColor(Color.RED);
        //         zg.fillRect(10, 15, 150, 20);
        //         zg.setColor(Color.WHITE);
        //         zg.drawString("Happy Programming", 30, 30);

        drawROI_or_Line(zg);

        // paintAtCursor(zg);

        if (imageArrayByte != null) {
            //drawRatioingRoi(zg);
            //drawStampsOnOverlay();
        }

        for (int i = 0; i < overlayGraphics.size(); i++) {
            ((GraphicOverlay) overlayGraphics.get(i)).drawGraphicOverlay(zg);
        }

        // Nov 7 test change... >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        if (over != null) {
            //            zg.drawImage(over, null, 0, 0);
            //      zg.setColor(Color.WHITE);
            //      zg.drawLine( -2, 0, 2, 0);
            //      zg.drawLine(0, -2, 0, 2);
            //      zg.drawString("this is zoom center", 10, 5);
            //zg.drawString("haha! You Found Me :))", -100, -100);
        }
    }

    public void paintAtCursor(ZoomGraphics zg) {
        int wid = 30;
        zg.setColor(Color.black);
        zg.setStroke(new BasicStroke(2.0f));
        zg.drawRect(cursorPosX, cursorPosY, wid, wid);
        zg.setColor(Color.yellow);
        zg.setStroke(new BasicStroke(1.0f));
        zg.drawRect(cursorPosX, cursorPosY, wid, wid);
    }

    void drawROI_or_Line(ZoomGraphics zg) {
        if (selectingLine) { // draw the selected Line
            if (!((box.width == 0) && (box.height == 0))) {
                zg.setColor(Color.red);
                zg.setStroke(new BasicStroke(1.0f));
                //System.out.println("box: " +
                //box.x +", "+ box.y+", "+ (box.x + box.width)+", "+ (box.y + box.height));
                zg.drawLine(box.x, box.y, box.x + box.width, box.y + box.height);
                zg.fillOval(box.x - 1, box.y - 1, 3, 3);
            }
        } else { // draw the selected ROI
            if ((box.width != 0) && (box.height != 0)) {
                zg.setColor(Color.black);
                zg.setStroke(new BasicStroke(2.0f));
                zg.drawRect(box.x, box.y, box.width, box.height);
                zg.setColor(Color.yellow);
                zg.setStroke(new BasicStroke(1.0f));
                zg.drawRect(box.x, box.y, box.width, box.height);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=">>>--- GraphicOverlay management  ---<<<" >
    public void addGraphicOverlay(GraphicOverlay overlay) {
        overlayGraphics.add(overlay);
    }

    public void removeGraphicOverlay(GraphicOverlay overlay) {
        overlayGraphics.remove(overlay);
    }

    public void clearGraphicOverlays() {
        overlayGraphics.clear();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="<<< LUT >>>">
    void setLookupOp(BufferedImageOp op) {
        lookupOp = op;
    }

    public void setDisplayLUT(int mode) {
        displayMode = mode;
        updateDisplayLUT();
    }

    public void updateDisplayLUT() { // for enhanced mode display

        Object lutArray = null;
        if (displayMode == 0) { // normal display, no change

            lookupOp = null;
            return;
        }
        if (displayMode == 1) { // "Enhanced" display w/ Histogram Equalization
            // @todo make this work using current image
            if (imageArrayByte != null) {
                lutArray = Equalizer.equalize(imageArrayByte, iWidth, iHeight);
            } else if ((wr != null)) { //&& (pixelArray != null)) {
                //pixelArray = wr.getPixels(0, 0, wr.getWidth(), wr.getHeight(), pixelArray);
                //lutArray = Equalizer.equalize(pixelArray, iWidth, iHeight);
            }
            lut = new ByteLookupTable(0, (byte[]) lutArray);
            //         }
            lookupOp = new LookupOp(lut, null);
        }
        if (displayMode == 2) { // Highlight saturated and zero pixels
            //            lut = new ShortLookupTable(0, (short[]) lutArray);
            //            lut = new ByteLookupTable(0, (byte[]) lutArray);
            //         }
            lookupOp = new LookupOp(lut, null);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="<<<  ROI >>>">
    public void setSelectingROI(boolean selRoi) {
        selectingROI = selRoi;
        selectingLine = !selRoi;
    }

    public void setROI(Rectangle roi) {
        //System.out.println("roi: " + roi);
        if (roi == null) {
            box.setBounds(0, 0, 0, 0);
        } else {
            box.setBounds(roi.getBounds());
        }
        roiChanged();
    }

    public Rectangle getROI() {
        if (isROIset()) {
            return box.getBounds();
        } else {
            box.setBounds(0, 0, 0, 0);
            return box;
        }
    }

    public boolean isROIset() {
        return ((box != null) && (box.width > 0) && (box.height > 0));
    }

    public void roiChanged() {
        ImageStatistics iStat = null;
        if (box == null) {
            box.setBounds(0, 0, 0, 0);
        }
        if (selectingROI) {
            if (imageArrayByte != null) {
                iStat = ImageAnalyzer.getStats(imageArrayByte, box, imageDim);
            } else if (pixelArray != null) {
                iStat = ImageAnalyzer.getStats(pixelArray, box, imageDim);
            }
            if (isROIset()) {
                analyzerROI.setBounds(box.getBounds());
                lastROI.setBounds(box.getBounds());
                analyzerROIsetSize = imageDisplayed.getWidth(this) * imageDisplayed.getHeight(this);

                if (iStat != null) {
                    //fireFoo(new RoiChangeEvent(this));
                    this.fireRoiChange(new RoiChangeEvent(this, box.x, box.y, box.height,
                            box.width, iStat.minInROI, iStat.meanInROI, iStat.maxInROI));
                }
            } else {
                analyzerROI.setBounds(new Rectangle(0, 0, 0, 0));
                analyzerROIsetSize = 0;

                if (iStat != null) {
                    this.fireRoiChange(new RoiChangeEvent(this, box.x, box.y, box.height,
                            box.width, iStat.min, iStat.mean, iStat.max));
                }
            }
        }
        if (selectingLine) {
            if (isROIset()) {
                // Here is where we do the LineProfile();
                analyzerLine.setBounds(box.getBounds());
            } else {
                analyzerLine.setBounds(new Rectangle(0, 0, 0, 0));
            }
            iPanel.valuePanel.setValueROI();
        }
        //updateObservers();
        repaint();
    }

    public void clearROI() {
        //System.out.println("roi: " + roi);
        box.setBounds(0, 0, 0, 0);
        roiChanged();
    }

    // -- Called by SelectArea as the selection rectangle changes.
    public void rectChanged(Rectangle rect) {
        box.x = (int) (rect.x);
        box.y = (int) (rect.y);
        box.width = (int) (rect.width);
        box.height = (int) (rect.height);
        repaint();
    }

    void updateDrawableRect() {
        int x = currentRect.x;
        int y = currentRect.y;
        int width = currentRect.width;
        int height = currentRect.height;
        if (!selectingLine) {
            //Make the width and height positive, if necessary.
            if (width < 0) {
                width = 0 - width;
                x = x - width + 1;
                if (x < 0) {
                    width += x;
                    x = 0;
                }
            }
            if (height < 0) {
                height = 0 - height;
                y = y - height + 1;
                if (y < 0) {
                    height += y;
                    y = 0;
                }
            }
        }
        //The rectangle shouldn't extend past the drawing area.
        if ((x + width) > iWidth) {
            width = iWidth - x;
        }
        if ((y + height) > iHeight) {
            height = iHeight - y;
        }
        // Update rectToDraw after saving old value.
        if (rectToDraw != null) {
            previousRectDrawn.setBounds(rectToDraw.x, rectToDraw.y, rectToDraw.width,
                    rectToDraw.height);
            rectToDraw.setBounds(x, y, width, height);
        } else {
            rectToDraw = new Rectangle(x, y, width, height);
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="<<< Pixel/ROI ValueUpdates >>>">
    void valuePoint(MouseEvent e) {
        if (imageDisplayed == null) {
            return;
        }
        setPixOffset(e);
        updatePixelValue();
    }

    private void setCursorInImage(boolean t) {
        cursorInImage = t;
    }

    private boolean isCursorInImage() {
        return cursorInImage;
    }

    void updatePixelValue() {
        if (!isCursorInImage()) {
            // clear them
            firePixelChange(new PixelChangeEvent(this, -1, 0, 0));
            return;
        }

        if (imageArrayByte != null) {
            int value = 0;
            double retMag = 0;
            if (depth == 8) {
                value = (int) (imageArrayByte[pixOffset] & 0xFF);
            } else {
                value = (int) (imageArrayShort[pixOffset]); // & 0xFFFF);
            }
            firePixelChange(new PixelChangeEvent(this, value, cursorPosX, cursorPosY));
        }

        if (pixelArray != null) {
            int pixel = pixelArray[pixOffset];
            //            int alpha_val = (pixel >>> 24) & 0xFF;
            //            int red_val = (pixel >>> 16) & 0xFF;
            //            int green_val = (pixel >>> 8) & 0xFF;
            //            int blue_val = pixel & 0xFF;
            //            System.out.println(alpha_val + ":" + red_val + ":" + green_val + ":"
            //                  + blue_val);
            firePixelChange(new PixelChangeEvent(this, pixel & 0xFF, cursorPosX, cursorPosY));

            //iPanel.valuePanel.setValuePixel(pixel & 0xFF, cursorPosX, cursorPosY);
        }

        // Get color of one point.
        //         pixelData = bufferedImage.getRaster().getDataElements(x, y, pixelData);
        //         int rgb = bufferedImage.getColorModel().getRGB(pixelData);

        // if Color Image...
        //strValuePoint =
        //"(" + ((pixel >> 24) & 0xff) + ", " + ((pixel >> 16) & 0xff)
        //+ "," + ((pixel >> 8) & 0xff) + "," + (pixel & 0xff) + ")"
        //+ "  @ (" + cursorPosX + "," + cursorPosY + ")";
        //or... ??  int pByte = new Integer(pixelArray[pixOffset]).byteValue();
        // int red = pgColorModel.getRed(pixOffset);
        // int green = pgColorModel.getGreen(pixOffset);
        // int blue = pgColorModel.getBlue(pixOffset);
        // strValuePoint = "RGB: " + String.valueOf(red) + ", " + String.valueOf(green) +
        //   ", " + String.valueOf(blue) + "  (" + cursorPosX + ", " + cursorPosY +
        //   ")  pByte: " + String.valueOf(pByte);
    }

    private void setPixOffset(MouseEvent e) {
        // sets the current
        Point2D.Double zc = toUserSpace(e.getX(), e.getY());
        cursorPosX = (int) zc.getX();
        cursorPosY = (int) zc.getY();
        if ((imageDisplayed == null) || (cursorPosX < 0) || (cursorPosY < 0)) {
            pixOffset = 0;
            setCursorInImage(false);
        } else {
            pixOffset = (cursorPosY * imageDim.width) + cursorPosX;
            if ((pixOffset >= (imageDim.width * imageDim.height))
                    || (cursorPosX >= imageDim.width)) {
                pixOffset = (imageDim.width * imageDim.height) - 1;
                setCursorInImage(false);
            } else {
                setCursorInImage(true);
            }
        }
    }

    public void updateMinMeanMax() {
        ImageStatistics iStat = null;
        if (box == null) {
            return;
        }
        if (imageArrayByte != null) {
            iStat = ImageAnalyzer.getStats(imageArrayByte, box, imageDim);
        } else if ((wr != null)) { //&& (pixelArray != null)) {
            pixelArray = wr.getPixels(0, 0, wr.getWidth(), wr.getHeight(), pixelArray);
            iStat = ImageAnalyzer.getStats(pixelArray, box, imageDim);
        }
        if (iStat != null) {
            if ((box.width > 0) && (box.height > 0)) {
                //iPanel.valuePanel.setValueStats(iStat.min, iStat.mean, iStat.max);
                fireRoiChange(new RoiChangeEvent(this, box.x, box.y, box.height, box.width,
                        iStat.minInROI, iStat.meanInROI, iStat.maxInROI));
            } else {
                fireRoiChange(new RoiChangeEvent(this, box.x, box.y, box.height, box.width,
                        iStat.min, iStat.mean, iStat.max));
            }
            updatePixelValue();
        } else {
            System.err.println("iStat != null");
        }
        //valuePoint(new MouseEvent(null,0,0,0,0));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="<<< Image Getters >>>">
    // getImageDisplayed - returns the currently displayed image
    public BufferedImage getImageDisplayed() {
        //        BufferedImage bimg = new BufferedImage(imageDisplayed.getWidth(null),
        //                imageDisplayed.getHeight(null), imageDisplayed.getType());
        //        return imageDisplayed;
        return deepCopy(imageDisplayed);
    }

    // getImageWithOverlay- returns the current image including the overlays (stamps, vectors, etc)
    public BufferedImage getImageWithOverlay() {
        BufferedImage bimg = new BufferedImage(imageDisplayed.getWidth(null),
                imageDisplayed.getHeight(null), BufferedImage.TYPE_INT_RGB); //or whatever type is appropriate

        bimg.getGraphics().drawImage(imageDisplayed, 0, 0, null);
        bimg.getGraphics().drawImage(over, 0, 0, null);
        return bimg;
    }

    public BufferedImage getImageDisplayedLut() {
        BufferedImage img = null;
        if (lookupOp != null) { // apply LookupTableOp
            try {
                img = lookupOp.filter(imageDisplayed, null);
            } catch (Exception ex) {
            }
            return img;
        } else {
            return imageDisplayed;
        }
    }

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    //-----------------------------------------------------------
    // Colorize - using JAI
    //
    //  public void colorize(byte[][] lt) {
    //    LookupTableJAI lookup = new LookupTableJAI(lt);
    //    ParameterBlock pb = new ParameterBlock();
    //    pb.addSource(imageDisplayed);
    //    pb.add(lookup);
    //    PlanarImage dst = JAI.create("lookup", pb, null);
    //    changeImage(dst.getAsBufferedImage());
    //  }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="<<< Pixel & ROI changeListeners >>>">
    public synchronized void addPixelChangeListener(PixelChangeListener listener) {
        if (listener != null) {
            pixelListeners.add(listener);
        }
    }

    public synchronized void removePixelChangeListener(PixelChangeListener listener) {
        pixelListeners.remove(listener);
    }

    public synchronized void firePixelChange(PixelChangeEvent evnt) {
        for (Iterator i = pixelListeners.iterator(); i.hasNext();) {
            ((PixelChangeListener) i.next()).pixelChanged(evnt);
        }
    }

    public synchronized void addRoiChangeListener(RoiChangeListener listener) {
        if (listener != null) {
            roiListeners.add(listener);
        }
    }

    public synchronized void removeRoiChangeListener(RoiChangeListener listener) {
        roiListeners.remove(listener);
    }

    public synchronized void fireRoiChange(RoiChangeEvent evnt) {
        for (Iterator i = roiListeners.iterator(); i.hasNext();) {
            ((RoiChangeListener) i.next()).roiChanged(evnt);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="<<< Streaming / ImageConsumer implementation >>>">
    // Streaming / ImageConsumer implementation
    // For displaying Stream images
    // set 8-bit streaming source (live display)
    public void setupForStreaming() {
        //streamImageDisplayed = createImage(source);
        //imageDisplayed = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_BYTE_GRAY);
        byte[] rLUT = new byte[256];
        byte[] gLUT = new byte[256];
        byte[] bLUT = new byte[256];

        for (int i = 0; i < 256; i++) {
            rLUT[i] = (byte) (i);
            gLUT[i] = (byte) (i);
            bLUT[i] = (byte) (i);
//            rLUT[i] = (byte) (i * 0.299);
//            gLUT[i] = (byte) (i * 0.587);
//            bLUT[i] = (byte) (i * 0.114);
        }
        // @todo This will be extended to use a range of values for saturated/zero highlighting
        bLUT[0] = (byte) 255;  // zero pixels are blue
        rLUT[255] = (byte) 255; // saturated pixels are red
        gLUT[255] = (byte) 0;
        bLUT[255] = (byte) 0;

        IndexColorModel cm = new IndexColorModel(8, 256, rLUT, gLUT, bLUT);
        imageDisplayed = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_BYTE_INDEXED, cm);
        wr = imageDisplayed.getRaster();
    }

    @Override
    public void setDimensions(int i, int i0) {
    }

    @Override
    public void setProperties(Hashtable<?, ?> props) {
    }

    @Override
    public void setColorModel(ColorModel colorModel) {
    }

    @Override
    public void setHints(int i) {
    }

    @Override
    public void setPixels(int i, int i0, int i1, int i2, ColorModel colorModel, byte[] b, int i3,
            int i4) {
        incoming = b;
    }

    @Override
    public void setPixels(int i, int i0, int i1, int i2, ColorModel colorModel, int[] i3, int i4,
            int i5) {
    }

    //  When streaming... >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    @Override
    public void imageComplete(int i) {
        //System.out.println("imagecomplete");
        if (wr == null) {
            System.err.println("wr == null");
            return;
        }
        if (incoming == null) {
            System.err.println("incoming == null");
            return;
        }
        wr.setDataElements(0, 0, wr.getWidth(), wr.getHeight(), incoming);
        //      try {
        //         grabber.grabPixels();
        //      } catch (InterruptedException ex) {
        //         ex.printStackTrace();
        //      }
        // @todo updateMinMeanMax only on nth frame... to reduce overhead

        StaticSwingUtils.dispatchToEDT(new Runnable() {

            public void run() {
                // @todo add mod n on update value.
                updateMinMeanMax();
                repaint();
            }
        });
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="<<< MultipleImages >>>">
    // SeeFloor - for displaying image mosaic
    ArrayList images = new ArrayList();

    public void addImage(BufferedImage img, double x, double y) {
        images.add(new LocatedImage(img, x, y));
        repaint();
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="<<< MouseListeners >>>">
    //-----------------------------------------------------------
    // for when in ROI/Line Select (not Stamp) mode
    // This is the Default MouseInputAdapter
    class MouseListenerROI
            extends MouseInputAdapter {

        //mouseDragged(MouseEvent e)
        //mouseExited(MouseEvent e)
        //mouseMoved(MouseEvent e)
        //mousePressed(MouseEvent e)
        //mouseReleased(MouseEvent e)
        public void mouseClicked(MouseEvent e) {
            //stampValue(e);
        }

        public void mousePressed(MouseEvent e) {
            Point2D.Double zc = toUserSpace(e.getX(), e.getY());
            int x = (int) (zc.getX());
            int y = (int) (zc.getY());

            if (isInImage(x, y)) {
                currentRect = new Rectangle(x, y, 0, 0);
                updateDrawableRect();
                rectChanged(rectToDraw);
            }
        }

        public void mouseDragged(MouseEvent e) {
            Point2D.Double zc = toUserSpace(e.getX(), e.getY());
            int x = (int) (zc.getX());
            int y = (int) (zc.getY());

            if (isInImage(x, y)) {
                updateSize(x, y);
            }
        }

        public void mouseReleased(MouseEvent e) {
            Point2D.Double zc = toUserSpace(e.getX(), e.getY());
            int x = (int) (zc.getX());
            int y = (int) (zc.getY());

            if (isInImage(x, y)) {
                updateSize(x, y);
                roiChanged();
            }
        }

        void updateSize(final int x, final int y) {
//      StaticSwingUtils.dispatchToEDT(new Runnable() {
//
//        public void run() {
            currentRect.setSize(x - currentRect.x, y - currentRect.y);
            updateDrawableRect();
            rectChanged(rectToDraw);
//        }
//      });
        }

        public void mouseMoved(MouseEvent e) {
            valuePoint(e);
        }

        public void mouseEntered(MouseEvent e) {
            //cursorInPanel = true;
        }

        public void mouseExited(MouseEvent e) {
            firePixelChange(new PixelChangeEvent(this, -1, 0, 0));
            //cursorInPanel = false;
            repaint();
        }

        public boolean isInImage(int x, int y) {
            if (imageDisplayed != null) {
                return (x >= 0) && (y >= 0) && (x <= iWidth) && (y <= iHeight);
            } else {
                return false;
            }
        }
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=">>>--- MouseInputAdapter default & management ---<<<" >
    MouseInputAdapter defaultMouseInputAdapter;

    public void setDefaultMouseInputAdapter(MouseInputAdapter defaultMouseInputAdapter) {
        this.defaultMouseInputAdapter = defaultMouseInputAdapter;
        addMouseListener(defaultMouseInputAdapter);
        addMouseMotionListener(defaultMouseInputAdapter);
    }

    public void setMouseInputAdapter(MouseInputAdapter mListenerAdapter) {
        removeMouseListener(defaultMouseInputAdapter);
        removeMouseMotionListener(defaultMouseInputAdapter);
        addMouseListener(mListenerAdapter);
        addMouseMotionListener(mListenerAdapter);
        this.removeMouseMotionListener(mListenerROI);
    }

    public void restoreDefaultMouseInputAdapter() {
        MouseListener[] ml = this.getMouseListeners();
        for (int i = 0; i < ml.length; i++) {
            MouseListener mouseListener = ml[i];
            this.removeMouseListener(mouseListener);
        }
        MouseMotionListener[] mml = this.getMouseMotionListeners();
        for (int i = 0; i < mml.length; i++) {
            MouseMotionListener mouseMotionListener = mml[i];
            this.removeMouseMotionListener(mouseMotionListener);
        }
        addMouseListener(defaultMouseInputAdapter);
        addMouseMotionListener(defaultMouseInputAdapter);
    }
// </editor-fold>
}
/////////////////////////////////////////////////////////////////////////////
// MyObservable
//
/* DEFUNCT
public void updateObservers () {
if (observersEnabled) {
ImageManipulator iM = new ImageManipulator(imageDisplayed,
imageArrayByte,
getROI(), box.x, box.y, box.x + box.width, box.y + box.height);
_observable.setChanged();
_observable.notifyObservers(iM);
_observable.clearChanged();
}
}
public Observable observable () {
return _observable;
}
public void enableObservers (boolean t) {
observersEnabled = t;
}
 */
//class MyObservable extends Observable {
//    public void clearChanged() {
//        super.clearChanged();
//    }
//
//    public void setChanged() {
//        super.setChanged();
//    }
//}

