package ij.gui;

import ijx.gui.IjxImageWindow;
import ijx.gui.IjxImageCanvas;
import ijx.app.IjxApplication;
import ijx.IjxImagePlus;
import java.awt.*;
import java.awt.image.*;
import ij.plugin.WandToolOptions;
import ij.plugin.frame.Recorder;
import ij.plugin.frame.RoiManager;
import ij.macro.*;
import ij.*;
import ij.util.*;
import ijx.CentralLookup;
import ijx.app.KeyboardHandler;
import ijx.gui.IjxStackWindow;
import java.awt.event.*;
import java.util.*;
import javax.swing.JInternalFrame;

/**
 * This is a Canvas used to display images in a Window.
 * This is an AWT version using an awt.Canvas component.
 * 
 * GBH refactoring Sept 2010
 */

public class ImageCanvas extends Canvas implements IjxImageCanvas {
    //implements IjxImageCanvas {
    protected static Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    protected static Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
    protected static Cursor moveCursor = new Cursor(Cursor.MOVE_CURSOR);
    protected static Cursor crosshairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    public static boolean usePointer = Prefs.usePointerCursor;
    protected IjxImagePlus imp;
    protected boolean imageUpdated;
    protected Rectangle srcRect;
    protected int imageWidth, imageHeight;
    protected int xMouse; // current cursor offscreen x location
    protected int yMouse; // current cursor offscreen y location
    private boolean showCursorStatus = true;
    private int sx2, sy2;
    private boolean disablePopupMenu;
    private boolean showAllROIs;
    private static Color zoomIndicatorColor;
    private static Font smallFont, largeFont;
    private Rectangle[] labelRects;
    private boolean maxBoundsReset;
    private Overlay overlay, showAllList;
    private static final int LIST_OFFSET = 100000;
    private static Color showAllColor = Prefs.getColor(Prefs.SHOW_ALL_COLOR, new Color(0, 255, 255));
    private static Color labelColor;
    private int resetMaxBoundsCount;
    private Roi currentRoi;
    protected IjxApplication ij;
    protected double magnification;
    protected int dstWidth, dstHeight;
    protected int xMouseStart;
    protected int yMouseStart;
    protected int xSrcStart;
    protected int ySrcStart;
    protected int flags;
    private Image offScreenImage;
    private int offScreenWidth = 0;
    private int offScreenHeight = 0;
    private boolean mouseExited = true;
    private boolean customRoi;

    private final CentralLookup centralLookup = CentralLookup.getDefault();
    IjxToolbar toolbar = ((IjxToolbar) centralLookup.lookup(IjxToolbar.class));

    public ImageCanvas(IjxImagePlus imp) {
        this.imp = imp;
        ij = IJ.getInstance();
        int width = imp.getWidth();
        int height = imp.getHeight();
        imageWidth = width;
        imageHeight = height;
        srcRect = new Rectangle(0, 0, imageWidth, imageHeight);
        setDrawingSize(imageWidth, (int) (imageHeight));
        magnification = 1.0;
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
        setFocusTraversalKeysEnabled(false);
    }

    public void updateImage(IjxImagePlus imp) {
        this.imp = imp;
        int width = imp.getWidth();
        int height = imp.getHeight();
        imageWidth = width;
        imageHeight = height;
        srcRect = new Rectangle(0, 0, imageWidth, imageHeight);
        setDrawingSize(imageWidth, (int) imageHeight);
        magnification = 1.0;
    }

    /** Update this ImageCanvas to have the same zoom and scale settings as the one specified. */
    public void update(IjxImageCanvas icX) {
        IjxImageCanvas ic = (IjxImageCanvas) icX;
        if (ic == null || ic == this || ic.getImage() == null) {
            return;
        }
        if (ic.getImage().getWidth() != imageWidth || ic.getImage().getHeight() != imageHeight) {
            return;
        }
        srcRect = new Rectangle(ic.getSrcRect().x, ic.getSrcRect().y,
                ic.getSrcRect().width, ic.getSrcRect().height);
        setMagnification(ic.getMagnification());
        setDrawingSize(ic.getCanvas().getPreferredSize().width, ic.getCanvas().getPreferredSize().height);
    }

    public void setSourceRect(Rectangle r) {
        srcRect = r;
    }

    public void setDrawingSize(int width, int height) {
        dstWidth = width;
        dstHeight = height;
        setSize(dstWidth, dstHeight);
    }

    /** IjxImagePlus.updateAndDraw calls this method to get paint
    to update the image from the ImageProcessor. */
    public void setImageUpdated() {
        imageUpdated = true;
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        Roi roi = imp.getRoi();
        if (roi != null || showAllROIs || overlay != null) {
            if (roi != null) {
                roi.updatePaste();
            }
            if (!IJ.isMacOSX() && imageWidth != 0) {
                paintDoubleBuffered(g);
                return;
            }
        }
        try {
            if (imageUpdated) {
                imageUpdated = false;
                imp.updateImage();
            }
            Java2.setBilinearInterpolation(g, Prefs.interpolateScaledImages);
            Image img = imp.getImage();
            if (img != null) {
                g.drawImage(img, 0, 0, (int) (srcRect.width * magnification), (int) (srcRect.height * magnification),
                        srcRect.x, srcRect.y, srcRect.x + srcRect.width, srcRect.y + srcRect.height, null);
            }
            if (overlay != null) {
                drawOverlay(g);
            }
            if (showAllROIs) {
                drawAllROIs(g);
            }
            if (roi != null) {
                drawRoi(roi, g);
            }
            if (srcRect.width < imageWidth || srcRect.height < imageHeight) {
                drawZoomIndicator(g);
            }
            if (IJ.debugMode) {
                showFrameRate(g);
            }
        } catch (OutOfMemoryError e) {
            IJ.outOfMemory("Paint");
        }
    }

    private void drawRoi(Roi roi, Graphics g) {
        if (roi == currentRoi) {
            Color lineColor = roi.getStrokeColor();
            Color fillColor = roi.getFillColor();
            float lineWidth = roi.getStrokeWidth();
            roi.setStrokeColor(null);
            roi.setFillColor(null);
            roi.setStrokeWidth(1);
            roi.draw(g);
            roi.setStrokeColor(lineColor);
            roi.setStrokeWidth(lineWidth);
            roi.setFillColor(fillColor);
            currentRoi = null;
        } else {
            roi.draw(g);
        }
    }

    void drawAllROIs(Graphics g) {
        RoiManager rm = RoiManager.getInstance();
        if (rm == null) {
            rm = Interpreter.getBatchModeRoiManager();
            if (rm != null && rm.getList().getItemCount() == 0) {
                rm = null;
            }
        }
        if (rm == null) {
            if (showAllList != null) {
                overlay = showAllList;
            }
            showAllROIs = false;
            repaint();
            return;
        }
        initGraphics(g);
        Hashtable rois = rm.getROIs();
        java.awt.List list = rm.getList();
        boolean drawLabels = rm.getDrawLabels();
        currentRoi = null;
        int n = list.getItemCount();
        if (IJ.debugMode) {
            IJ.log("paint: drawing " + n + " \"Show All\" ROIs");
        }
        if (labelRects == null || labelRects.length != n) {
            labelRects = new Rectangle[n];
        }
        if (!drawLabels) {
            showAllList = new Overlay();
        } else {
            showAllList = null;
        }
        for (int i = 0; i < n; i++) {
            String label = list.getItem(i);
            Roi roi = (Roi) rois.get(label);
            if (roi == null) {
                continue;
            }
            if (showAllList != null) {
                showAllList.add(roi);
            }
            if (i < 200 && drawLabels && imp != null && roi == imp.getRoi()) {
                currentRoi = roi;
            }
            if (Prefs.showAllSliceOnly && imp.getStackSize() > 1) {
                int slice = getSliceNumber(roi.getName());
                if (slice == -1 || slice == imp.getCurrentSlice()) {
                    drawRoi(g, roi, drawLabels ? i : -1);
                }
            } else {
                drawRoi(g, roi, drawLabels ? i : -1);
            }
        }
        ((Graphics2D) g).setStroke(Roi.onePixelWide);
    }

    public int getSliceNumber(String label) {
        int slice = -1;
        if (label.length() >= 14 && label.charAt(4) == '-' && label.charAt(9) == '-') {
            slice = (int) Tools.parseDouble(label.substring(0, 4), -1);
        } else if (label.length() >= 17 && label.charAt(5) == '-' && label.charAt(11) == '-') {
            slice = (int) Tools.parseDouble(label.substring(0, 5), -1);
        } else if (label.length() >= 20 && label.charAt(6) == '-' && label.charAt(13) == '-') {
            slice = (int) Tools.parseDouble(label.substring(0, 6), -1);
        }
        return slice;
    }

    void drawOverlay(Graphics g) {
        if (imp != null && imp.getHideOverlay()) {
            return;
        }
        initGraphics(g);
        int n = overlay.size();
        if (IJ.debugMode) {
            IJ.log("paint: drawing " + n + " ROI display list");
        }
        boolean drawLabels = overlay.getDrawLabels();
        int stackSize = imp.getStackSize();
        boolean stackLabels = n > 1 && n >= stackSize && (overlay.get(0) instanceof TextRoi) && (overlay.get(stackSize - 1) instanceof TextRoi);
        if (stackLabels) { // created by Image>Stacks>Label
            int index = imp.getCurrentSlice() - 1;
            if (index < n) {
                overlay.hide(0, index - 1);
                overlay.hide(index + 1, stackSize - 1);
            }
        }
        for (int i = 0; i < n; i++) {
            if (overlay == null) {
                break;
            }
            drawRoi(g, overlay.get(i), drawLabels ? i + LIST_OFFSET : -1);
        }
        ((Graphics2D) g).setStroke(Roi.onePixelWide);
    }

    void initGraphics(Graphics g) {
        if (smallFont == null) {
            smallFont = new Font("SansSerif", Font.PLAIN, 9);
            largeFont = new Font("SansSerif", Font.PLAIN, 12);
        }
        if (labelColor == null) {
            int red = showAllColor.getRed();
            int green = showAllColor.getGreen();
            int blue = showAllColor.getBlue();
            if ((red + green + blue) / 3 < 128) {
                labelColor = Color.white;
            } else {
                labelColor = Color.black;
            }
        }
        g.setColor(showAllColor);
    }

    void drawRoi(Graphics g, Roi roi, int index) {
        int type = roi.getType();
        IjxImagePlus imp2 = roi.getImage();
        roi.setImage(imp);
        Color saveColor = roi.getStrokeColor();
        if (saveColor == null) {
            roi.setStrokeColor(showAllColor);
        }
        if (roi instanceof TextRoi) {
            ((TextRoi) roi).drawText(g);
        } else {
            roi.drawOverlay(g);
        }
        roi.setStrokeColor(saveColor);
        if (index >= 0) {
            if (roi == currentRoi) {
                g.setColor(Roi.getColor());
            } else {
                g.setColor(showAllColor);
            }
            drawRoiLabel(g, index, roi.getBounds());
        }
        if (imp2 != null) {
            roi.setImage(imp2);
        } else {
            roi.setImage(null);
        }
    }

    void drawRoiLabel(Graphics g, int index, Rectangle r) {
        int x = screenX(r.x);
        int y = screenY(r.y);
        double mag = getMagnification();
        int width = (int) (r.width * mag);
        int height = (int) (r.height * mag);
        int size = width > 40 && height > 40 ? 12 : 9;
        if (size == 12) {
            g.setFont(largeFont);
        } else {
            g.setFont(smallFont);
        }
        boolean drawingList = index >= LIST_OFFSET;
        if (drawingList) {
            index -= LIST_OFFSET;
        }
        String label = "" + (index + 1);
        FontMetrics metrics = g.getFontMetrics();
        int w = metrics.stringWidth(label);
        x = x + width / 2 - w / 2;
        y = y + height / 2 + Math.max(size / 2, 6);
        int h = metrics.getHeight();
        g.fillRoundRect(x - 1, y - h + 2, w + 1, h - 3, 5, 5);
        if (!drawingList) {
            labelRects[index] = new Rectangle(x - 1, y - h + 2, w + 1, h - 3);
        }
        g.setColor(labelColor);
        g.drawString(label, x, y - 2);
        g.setColor(showAllColor);
    }

    void drawZoomIndicator(Graphics g) {
        int x1 = 10;
        int y1 = 10;
        double aspectRatio = (double) imageHeight / imageWidth;
        int w1 = 64;
        if (aspectRatio > 1.0) {
            w1 = (int) (w1 / aspectRatio);
        }
        int h1 = (int) (w1 * aspectRatio);
        if (w1 < 4) {
            w1 = 4;
        }
        if (h1 < 4) {
            h1 = 4;
        }
        int w2 = (int) (w1 * ((double) srcRect.width / imageWidth));
        int h2 = (int) (h1 * ((double) srcRect.height / imageHeight));
        if (w2 < 1) {
            w2 = 1;
        }
        if (h2 < 1) {
            h2 = 1;
        }
        int x2 = (int) (w1 * ((double) srcRect.x / imageWidth));
        int y2 = (int) (h1 * ((double) srcRect.y / imageHeight));
        if (zoomIndicatorColor == null) {
            zoomIndicatorColor = new Color(128, 128, 255);
        }
        g.setColor(zoomIndicatorColor);
        ((Graphics2D) g).setStroke(Roi.onePixelWide);
        g.drawRect(x1, y1, w1, h1);
        if (w2 * h2 <= 200 || w2 < 10 || h2 < 10) {
            g.fillRect(x1 + x2, y1 + y2, w2, h2);
        } else {
            g.drawRect(x1 + x2, y1 + y2, w2, h2);
        }
    }

    // Use double buffer to reduce flicker when drawing complex ROIs.
    // Author: Erik Meijering
    void paintDoubleBuffered(Graphics g) {
        final int srcRectWidthMag = (int) (srcRect.width * magnification);
        final int srcRectHeightMag = (int) (srcRect.height * magnification);
        if (offScreenImage == null || offScreenWidth != srcRectWidthMag || offScreenHeight != srcRectHeightMag) {
            offScreenImage = createImage(srcRectWidthMag, srcRectHeightMag);
            offScreenWidth = srcRectWidthMag;
            offScreenHeight = srcRectHeightMag;
        }
        Roi roi = imp.getRoi();
        try {
            if (imageUpdated) {
                imageUpdated = false;
                imp.updateImage();
            }
            Graphics offScreenGraphics = offScreenImage.getGraphics();
            Java2.setBilinearInterpolation(offScreenGraphics, Prefs.interpolateScaledImages);
            Image img = imp.getImage();
            if (img != null) {
                offScreenGraphics.drawImage(img, 0, 0, srcRectWidthMag, srcRectHeightMag,
                        srcRect.x, srcRect.y, srcRect.x + srcRect.width, srcRect.y + srcRect.height, null);
            }
            if (overlay != null) {
                drawOverlay(offScreenGraphics);
            }
            if (showAllROIs) {
                drawAllROIs(offScreenGraphics);
            }
            if (roi != null) {
                drawRoi(roi, offScreenGraphics);
            }
            if (srcRect.width < imageWidth || srcRect.height < imageHeight) {
                drawZoomIndicator(offScreenGraphics);
            }
            if (IJ.debugMode) {
                showFrameRate(offScreenGraphics);
            }
            g.drawImage(offScreenImage, 0, 0, null);
        } catch (OutOfMemoryError e) {
            IJ.outOfMemory("Paint");
        }
    }
    long firstFrame;
    int frames, fps;

    void showFrameRate(Graphics g) {
        frames++;
        if (System.currentTimeMillis() > firstFrame + 1000) {
            firstFrame = System.currentTimeMillis();
            fps = frames;
            frames = 0;
        }
        g.setColor(Color.white);
        g.fillRect(10, 12, 50, 15);
        g.setColor(Color.black);
        g.drawString((int) (fps + 0.5) + " fps", 10, 25);
    }

    public Dimension getPreferredSize() {
        return new Dimension(dstWidth, dstHeight);
    }
    int count;

    /*
    public Graphics getGraphics() {
    Graphics g = super.getGraphics();
    IJ.write("getGraphics: "+count++);
    if (IJ.altKeyDown())
    throw new IllegalArgumentException("");
    return g;
    }
     */
    /** Returns the current cursor location in image coordinates. */
    public Point getCursorLoc() {
        return new Point(xMouse, yMouse);
    }

    /** Returns the mouse event modifiers. */
    public int getModifiers() {
        return flags;
    }

    /** Returns the IjxImagePlus object that is associated with this ImageCanvas. */
    public IjxImagePlus getImage() {
        return imp;
    }

    /** Sets the cursor based on the current tool and cursor location. */
    public void setCursor(int sx, int sy, int ox, int oy) {
        xMouse = ox;
        yMouse = oy;
        mouseExited = false;
        Roi roi = imp.getRoi();
        IjxImageWindow win = (IjxImageWindow) imp.getWindow();
        if (win == null) {
            return;
        }
        if (IJ.spaceBarDown()) {
            setCursor(handCursor);
            return;
        }
        int id = toolbar.getToolId();
        switch (toolbar.getToolId()) {
            case IjxToolbar.MAGNIFIER:
                setCursor(moveCursor);
                break;
            case IjxToolbar.HAND:
                setCursor(handCursor);
                break;
            default:  //selection tool
                if (id == Toolbar.SPARE1 || id >= Toolbar.SPARE2) {
                    if (Prefs.usePointerCursor) {
                        setCursor(defaultCursor);
                    } else {
                        setCursor(crosshairCursor);
                    }
                } else if (roi != null && roi.getState() != roi.CONSTRUCTING && roi.isHandle(sx, sy) >= 0) {
                    setCursor(handCursor);
                } else if (Prefs.usePointerCursor || (roi != null && roi.getState() != roi.CONSTRUCTING && roi.contains(ox, oy))) {
                    setCursor(defaultCursor);
                } else {
                    setCursor(crosshairCursor);
                }
        }
    }

    /**Converts a screen x-coordinate to an offscreen x-coordinate.*/
    public int offScreenX(int sx) {
        return srcRect.x + (int) (sx / magnification);
    }

    /**Converts a screen y-coordinate to an offscreen y-coordinate.*/
    public int offScreenY(int sy) {
        return srcRect.y + (int) (sy / magnification);
    }

    /**Converts a screen x-coordinate to a floating-point offscreen x-coordinate.*/
    public double offScreenXD(int sx) {
        return srcRect.x + sx / magnification;
    }

    /**Converts a screen y-coordinate to a floating-point offscreen y-coordinate.*/
    public double offScreenYD(int sy) {
        return srcRect.y + sy / magnification;

    }

    /**Converts an offscreen x-coordinate to a screen x-coordinate.*/
    public int screenX(int ox) {
        return (int) ((ox - srcRect.x) * magnification);
    }

    /**Converts an offscreen y-coordinate to a screen y-coordinate.*/
    public int screenY(int oy) {
        return (int) ((oy - srcRect.y) * magnification);
    }

    /**Converts a floating-point offscreen x-coordinate to a screen x-coordinate.*/
    public int screenXD(double ox) {
        return (int) ((ox - srcRect.x) * magnification);
    }

    /**Converts a floating-point offscreen x-coordinate to a screen x-coordinate.*/
    public int screenYD(double oy) {
        return (int) ((oy - srcRect.y) * magnification);
    }

    public double getMagnification() {
        return magnification;
    }

    public void setMagnification(double magnification) {
        setMagnification2(magnification);
    }

    public void setMagnification2(double magnification) {
        if (magnification > 32.0) {
            magnification = 32.0;
        }
        if (magnification < 0.03125) {
            magnification = 0.03125;
        }
        this.magnification = magnification;
        imp.setTitle(imp.getTitle());
    }

    public Rectangle getSrcRect() {
        return srcRect;
    }

    public void setSrcRect(Rectangle srcRect) {
        this.srcRect = srcRect;
    }

    /** Enlarge the canvas if the user enlarges the window. */
    public void resizeCanvas(int width, int height) {
        IjxImageWindow win = (IjxImageWindow) imp.getWindow();
        //IJ.log("resizeCanvas: "+srcRect+" "+imageWidth+"  "+imageHeight+" "+width+"  "+height+" "+dstWidth+"  "+dstHeight+" "+win.maxBounds);
        if (!maxBoundsReset && (width > dstWidth || height > dstHeight) && win != null && win.getMaximumBounds() != null && width != win.getMaximumBounds().width - 10) {
            if (resetMaxBoundsCount != 0) {
                resetMaxBounds(); // Works around problem that prevented window from being larger than maximized size
            }
            resetMaxBoundsCount++;
        }
        if (IJ.altKeyDown()) {
            fitToWindow();
            return;
        }
        if (srcRect.width < imageWidth || srcRect.height < imageHeight) {
            if (width > imageWidth * magnification) {
                width = (int) (imageWidth * magnification);
            }
            if (height > imageHeight * magnification) {
                height = (int) (imageHeight * magnification);
            }
            setDrawingSize(width, height);
            srcRect.width = (int) (dstWidth / magnification);
            srcRect.height = (int) (dstHeight / magnification);
            if ((srcRect.x + srcRect.width) > imageWidth) {
                srcRect.x = imageWidth - srcRect.width;
            }
            if ((srcRect.y + srcRect.height) > imageHeight) {
                srcRect.y = imageHeight - srcRect.height;
            }
            repaint();
        }
        //IJ.log("resizeCanvas2: "+srcRect+" "+dstWidth+"  "+dstHeight+" "+width+"  "+height);
    }

    public void fitToWindow() {
        IjxImageWindow win = (IjxImageWindow) imp.getWindow();
        if (win == null) {
            return;
        }
        Rectangle bounds = win.getBounds();
        Insets insets = win.getInsets();
        int sliderHeight = (win instanceof IjxStackWindow) ? 20 : 0;
        double xmag = (double) (bounds.width - 10) / srcRect.width;
        double ymag = (double) (bounds.height - (10 + insets.top + sliderHeight)) / srcRect.height;
        setMagnification(Math.min(xmag, ymag));
        int width = (int) (imageWidth * magnification);
        int height = (int) (imageHeight * magnification);
        if (width == dstWidth && height == dstHeight) {
            return;
        }
        srcRect = new Rectangle(0, 0, imageWidth, imageHeight);
        setDrawingSize(width, height);
        getParent().doLayout();
    }

    public void setMaxBounds() {
        if (maxBoundsReset) {
            maxBoundsReset = false;
            IjxImageWindow win = (IjxImageWindow) imp.getWindow();
            if (win != null && !IJ.isLinux() && win.getMaximumBounds() != null) {
                win.setMaximizedBounds(win.getMaximumBounds());
                win.setMaxBoundsTime(System.currentTimeMillis());
            }
        }
    }

    void resetMaxBounds() {
        IjxImageWindow win = (IjxImageWindow) imp.getWindow();
        if (win != null && (System.currentTimeMillis() - win.getMaxBoundsTime()) > 500L) {
            win.setMaximizedBounds(win.getMaximumBounds());
            maxBoundsReset = true;
        }
    }

//	private static final double[] zoomLevels = {
//		1/72.0, 1/48.0, 1/32.0, 1/24.0, 1/16.0, 1/12.0,
//		1/8.0, 1/6.0, 1/4.0, 1/3.0, 1/2.0, 0.75, 1.0, 1.5,
//		2.0, 3.0, 4.0, 6.0, 8.0, 12.0, 16.0, 24.0, 32.0 };
//
//	public static double getLowerZoomLevel(double currentMag) {
//		double newMag = zoomLevels[0];
//		for (int i=0; i<zoomLevels.length; i++) {
//		if (zoomLevels[i] < currentMag)
//			newMag = zoomLevels[i];
//		else
//			break;
//		}
//		return newMag;
//	}
//
//	public static double getHigherZoomLevel(double currentMag) {
//		double newMag = 32.0;
//		for (int i=zoomLevels.length-1; i>=0; i--) {
//			if (zoomLevels[i]>currentMag)
//				newMag = zoomLevels[i];
//			else
//				break;
//		}
//		return newMag;
//	}
    /** Zooms in by making the window bigger. If it can't
    be made bigger, then make the source rectangle
    (srcRect) smaller and center it at (sx,sy). Note that
    sx and sy are screen coordinates. */
    public void zoomIn(int sx, int sy) {
        if (magnification >= 32) {
            return;
        }
        double newMag = ImageCanvasHelper.getHigherZoomLevel(magnification);
        int newWidth = (int) (imageWidth * newMag);
        int newHeight = (int) (imageHeight * newMag);
        Dimension newSize = canEnlarge(newWidth, newHeight);
        if (newSize != null) {
            setDrawingSize(newSize.width, newSize.height);
            if (newSize.width != newWidth || newSize.height != newHeight) {
                adjustSourceRect(newMag, sx, sy);
            } else {
                setMagnification(newMag);
            }
            packImageWindow();
        } else {
            adjustSourceRect(newMag, sx, sy);
        }
        repaint();
        if (srcRect.width < imageWidth || srcRect.height < imageHeight) {
            resetMaxBounds();
        }
    }

    void adjustSourceRect(double newMag, int x, int y) {
        //IJ.log("adjustSourceRect1: "+newMag+" "+dstWidth+"  "+dstHeight);
        int w = (int) Math.round(dstWidth / newMag);
        if (w * newMag < dstWidth) {
            w++;
        }
        int h = (int) Math.round(dstHeight / newMag);
        if (h * newMag < dstHeight) {
            h++;
        }
        x = offScreenX(x);
        y = offScreenY(y);
        Rectangle r = new Rectangle(x - w / 2, y - h / 2, w, h);
        if (r.x < 0) {
            r.x = 0;
        }
        if (r.y < 0) {
            r.y = 0;
        }
        if (r.x + w > imageWidth) {
            r.x = imageWidth - w;
        }
        if (r.y + h > imageHeight) {
            r.y = imageHeight - h;
        }
        srcRect = r;
        setMagnification(newMag);
        //IJ.log("adjustSourceRect2: "+srcRect+" "+dstWidth+"  "+dstHeight);
    }

    protected Dimension canEnlarge(int newWidth, int newHeight) {
        //if ((flags&Event.CTRL_MASK)!=0 || IJ.controlKeyDown()) return null;
        IjxImageWindow win = imp.getWindow();
        if (win == null) {
            return null;
        }
        Rectangle r1 = win.getBounds();
        Insets insets = win.getInsets();
        Point loc = getLocation();
        if (loc.x > insets.left + 5 || loc.y > insets.top + 5) {
            r1.width = newWidth + insets.left + insets.right + 10;
            r1.height = newHeight + insets.top + insets.bottom + 10;
            if (win instanceof IjxStackWindow) {
                r1.height += 20;
            }
        } else {
            r1.width = r1.width - dstWidth + newWidth + 10;
            r1.height = r1.height - dstHeight + newHeight + 10;
        }
        Rectangle max = win.getMaxWindow(r1.x, r1.y);
        boolean fitsHorizontally = r1.x + r1.width < max.x + max.width;
        boolean fitsVertically = r1.y + r1.height < max.y + max.height;
        if (fitsHorizontally && fitsVertically) {
            return new Dimension(newWidth, newHeight);
        } else if (fitsVertically && newHeight < dstWidth) {
            return new Dimension(dstWidth, newHeight);
        } else if (fitsHorizontally && newWidth < dstHeight) {
            return new Dimension(newWidth, dstHeight);
        } else {
            return null;
        }
    }

    /**Zooms out by making the source rectangle (srcRect)
    larger and centering it on (x,y). If we can't make it larger,
    then make the window smaller.*/
    public void zoomOut(int x, int y) {
        if (magnification <= 0.03125) {
            return;
        }
        double oldMag = magnification;
        double newMag = ImageCanvasHelper.getLowerZoomLevel(magnification);
        double srcRatio = (double) srcRect.width / srcRect.height;
        double imageRatio = (double) imageWidth / imageHeight;
        double initialMag = imp.getWindow().getInitialMagnification();
        if (Math.abs(srcRatio - imageRatio) > 0.05) {
            double scale = oldMag / newMag;
            int newSrcWidth = (int) Math.round(srcRect.width * scale);
            int newSrcHeight = (int) Math.round(srcRect.height * scale);
            if (newSrcWidth > imageWidth) {
                newSrcWidth = imageWidth;
            }
            if (newSrcHeight > imageHeight) {
                newSrcHeight = imageHeight;
            }
            int newSrcX = srcRect.x - (newSrcWidth - srcRect.width) / 2;
            int newSrcY = srcRect.y - (newSrcHeight - srcRect.height) / 2;
            if (newSrcX < 0) {
                newSrcX = 0;
            }
            if (newSrcY < 0) {
                newSrcY = 0;
            }
            srcRect = new Rectangle(newSrcX, newSrcY, newSrcWidth, newSrcHeight);
            //IJ.log(newMag+" "+srcRect+" "+dstWidth+" "+dstHeight);
            int newDstWidth = (int) (srcRect.width * newMag);
            int newDstHeight = (int) (srcRect.height * newMag);
            setMagnification(newMag);
            setMaxBounds();
            //IJ.log(newDstWidth+" "+dstWidth+" "+newDstHeight+" "+dstHeight);
            if (newDstWidth < dstWidth || newDstHeight < dstHeight) {
                //IJ.log("pack");
                setDrawingSize(newDstWidth, newDstHeight);
                packImageWindow();
            } else {
                repaint();
            }
            return;
        }
        if (imageWidth * newMag > dstWidth) {
            int w = (int) Math.round(dstWidth / newMag);
            if (w * newMag < dstWidth) {
                w++;
            }
            int h = (int) Math.round(dstHeight / newMag);
            if (h * newMag < dstHeight) {
                h++;
            }
            x = offScreenX(x);
            y = offScreenY(y);
            Rectangle r = new Rectangle(x - w / 2, y - h / 2, w, h);
            if (r.x < 0) {
                r.x = 0;
            }
            if (r.y < 0) {
                r.y = 0;
            }
            if (r.x + w > imageWidth) {
                r.x = imageWidth - w;
            }
            if (r.y + h > imageHeight) {
                r.y = imageHeight - h;
            }
            srcRect = r;
        } else {
            srcRect = new Rectangle(0, 0, imageWidth, imageHeight);
            setDrawingSize((int) (imageWidth * newMag), (int) (imageHeight * newMag));
            //setDrawingSize(dstWidth/2, dstHeight/2);
            packImageWindow();
        }
        //IJ.write(newMag + " " + srcRect.x+" "+srcRect.y+" "+srcRect.width+" "+srcRect.height+" "+dstWidth + " " + dstHeight);
        setMagnification(newMag);
        //IJ.write(srcRect.x + " " + srcRect.width + " " + dstWidth);
        setMaxBounds();
        repaint();
    }

    /** Implements the Image/Zoom/Original Scale command. */
    public void unzoom() {
        double imag = imp.getWindow().getInitialMagnification();
        if (magnification == imag) {
            return;
        }
        srcRect = new Rectangle(0, 0, imageWidth, imageHeight);
        IjxImageWindow win = imp.getWindow();
        setDrawingSize((int) (imageWidth * imag), (int) (imageHeight * imag));
        setMagnification(imag);
        setMaxBounds();
        packImageWindow();
        setMaxBounds();
        repaint();
    }

    /** Implements the Image/Zoom/View 100% command. */
    public void zoom100Percent() {
        if (magnification == 1.0) {
            return;
        }
        double imag = imp.getWindow().getInitialMagnification();
        if (magnification != imag) {
            unzoom();
        }
        if (magnification == 1.0) {
            return;
        }
        if (magnification < 1.0) {
            while (magnification < 1.0) {
                zoomIn(imageWidth / 2, imageHeight / 2);
            }
        } else if (magnification > 1.0) {
            while (magnification > 1.0) {
                zoomOut(imageWidth / 2, imageHeight / 2);
            }
        } else {
            return;
        }
        int x = xMouse, y = yMouse;
        if (mouseExited) {
            x = imageWidth / 2;
            y = imageHeight / 2;
        }
        int sx = screenX(x);
        int sy = screenY(y);
        adjustSourceRect(1.0, sx, sy);
        repaint();
    }

    protected void scroll(int sx, int sy) {
        int ox = xSrcStart + (int) (sx / magnification);  //convert to offscreen coordinates
        int oy = ySrcStart + (int) (sy / magnification);
        //IJ.log("scroll: "+ox+" "+oy+" "+xMouseStart+" "+yMouseStart);
        int newx = xSrcStart + (xMouseStart - ox);
        int newy = ySrcStart + (yMouseStart - oy);
        if (newx < 0) {
            newx = 0;
        }
        if (newy < 0) {
            newy = 0;
        }
        if ((newx + srcRect.width) > imageWidth) {
            newx = imageWidth - srcRect.width;
        }
        if ((newy + srcRect.height) > imageHeight) {
            newy = imageHeight - srcRect.height;
        }
        srcRect.x = newx;
        srcRect.y = newy;
        //IJ.log(sx+"  "+sy+"  "+newx+"  "+newy+"  "+srcRect);
        imp.draw();
        Thread.yield();
    }

    Color getColor(int index) {
        IndexColorModel cm = (IndexColorModel) imp.getProcessor().getColorModel();
        //IJ.write(""+index+" "+(new Color(cm.getRGB(index))));
        return new Color(cm.getRGB(index));
    }

    protected void setDrawingColor(int ox, int oy, boolean setBackground) {
        //IJ.log("setDrawingColor: "+setBackground+this);
        int type = imp.getType();
        int[] v = imp.getPixel(ox, oy);
        switch (type) {
            case IjxImagePlus.GRAY8: {
                if (setBackground) {
                    setBackgroundColor(getColor(v[0]));
                } else {
                    setForegroundColor(getColor(v[0]));
                }
                break;
            }
            case IjxImagePlus.GRAY16:
            case IjxImagePlus.GRAY32: {
                double min = imp.getProcessor().getMin();
                double max = imp.getProcessor().getMax();
                double value = (type == IjxImagePlus.GRAY32) ? Float.intBitsToFloat(v[0]) : v[0];
                int index = (int) (255.0 * ((value - min) / (max - min)));
                if (index < 0) {
                    index = 0;
                }
                if (index > 255) {
                    index = 255;
                }
                if (setBackground) {
                    setBackgroundColor(getColor(index));
                } else {
                    setForegroundColor(getColor(index));
                }
                break;
            }
            case IjxImagePlus.COLOR_RGB:
            case IjxImagePlus.COLOR_256: {
                Color c = new Color(v[0], v[1], v[2]);
                if (setBackground) {
                    setBackgroundColor(c);
                } else {
                    setForegroundColor(c);
                }
                break;
            }
        }
        Color c;
        if (setBackground) {
            c = toolbar.getBackgroundColor();
        } else {
            c = toolbar.getForegroundColor();
            imp.setColor(c);
        }
        IJ.showStatus("(" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ")");
    }

    private void setForegroundColor(Color c) {
        toolbar.setForegroundColor(c);
        if (Recorder.record) {
            Recorder.record("setForegroundColor", c.getRed(), c.getGreen(), c.getBlue());
        }
    }

    private void setBackgroundColor(Color c) {
        toolbar.setBackgroundColor(c);
        if (Recorder.record) {
            Recorder.record("setBackgroundColor", c.getRed(), c.getGreen(), c.getBlue());
        }
    }

    public void mousePressed(MouseEvent e) {
        if (ij == null) {
            return;
        }
        showCursorStatus = true;
        int toolID = toolbar.getToolId();
        IjxImageWindow win = imp.getWindow();
        if (win != null && win.isRunning2() && toolID != Toolbar.MAGNIFIER) {
            if (win instanceof IjxStackWindow) {
                ((IjxStackWindow) win).setAnimate(false);
            } else {
                win.setRunning2(false);
            }
            return;
        }

        int x = e.getX();
        int y = e.getY();
        flags = e.getModifiers();
        //IJ.log("Mouse pressed: " + e.isPopupTrigger() + "  " + ij.modifiers(flags) + " button: " + e.getButton() + ": " + e);
        //if (toolID!=Toolbar.MAGNIFIER && e.isPopupTrigger()) {
        if (toolID != Toolbar.MAGNIFIER && ((e.isPopupTrigger() && e.getButton() != 0) || (!IJ.isMacintosh() && (flags & Event.META_MASK) != 0))) {
            handlePopupMenu(e);
            return;
        }

        int ox = offScreenX(x);
        int oy = offScreenY(y);
        xMouse = ox;
        yMouse = oy;
        if (IJ.spaceBarDown()) {
            // temporarily switch to "hand" tool of space bar down
            setupScroll(ox, oy);
            return;
        }
        if (showAllROIs) {
            Roi roi = imp.getRoi();
            if (!(roi != null && (roi.contains(ox, oy) || roi.isHandle(x, y) >= 0)) && roiManagerSelect(x, y)) {
                return;
            }
        }
        if (customRoi && overlay != null) {
            return;
        }

        switch (toolID) {
            case Toolbar.MAGNIFIER:
                if (IJ.shiftKeyDown()) {
                    zoomToSelection(ox, oy);
                } else if ((flags & (Event.ALT_MASK | Event.META_MASK | Event.CTRL_MASK)) != 0) {
                    IJ.run("Out");
                } else {
                    IJ.run("In");
                }
                break;
            case Toolbar.HAND:
                setupScroll(ox, oy);
                break;
            case Toolbar.DROPPER:
                setDrawingColor(ox, oy, IJ.altKeyDown());
                break;
            case Toolbar.WAND:
                Roi roi = imp.getRoi();
                if (roi != null && roi.contains(ox, oy)) {
                    Rectangle r = roi.getBounds();
                    if (r.width == imageWidth && r.height == imageHeight) {
                        imp.killRoi();
                    } else if (!e.isAltDown()) {
                        handleRoiMouseDown(e);
                        return;
                    }
                }
                if (roi != null) {
                    int handle = roi.isHandle(x, y);
                    if (handle >= 0) {
                        roi.mouseDownInHandle(handle, x, y);
                        return;
                    }
                }
                setRoiModState(e, roi, -1);
                String mode = WandToolOptions.getMode();
                double tolerance = WandToolOptions.getTolerance();
                int npoints = IJ.doWand(ox, oy, tolerance, mode);
                if (Recorder.record && npoints > 0) {
                    if (tolerance == 0.0 && mode.equals("Legacy")) {
                        Recorder.record("doWand", ox, oy);
                    } else {
                        Recorder.recordString("doWand(" + ox + ", " + oy + ", " + tolerance + ", \"" + mode + "\");\n");
                    }
                }
                break;
            case Toolbar.OVAL:
                if (toolbar.getBrushSize() > 0) {
                    new RoiBrush();
                } else {
                    handleRoiMouseDown(e);
                }
                break;
            case IjxToolbar.SPARE1:
            case IjxToolbar.SPARE2:
            case IjxToolbar.SPARE3:
            case IjxToolbar.SPARE4:
            case IjxToolbar.SPARE5:
            case IjxToolbar.SPARE6:
            case IjxToolbar.SPARE7:
            case IjxToolbar.SPARE8:
            case IjxToolbar.SPARE9:
                toolbar.runMacroTool(toolID);
                break;
            default:  //selection tool
                handleRoiMouseDown(e);
        }
    }

    boolean roiManagerSelect(int x, int y) {
        RoiManager rm = RoiManager.getInstance();
        if (rm == null) {
            return false;
        }
        Hashtable rois = rm.getROIs();
        java.awt.List list = rm.getList();
        int n = list.getItemCount();
        if (labelRects == null || labelRects.length != n) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (labelRects[i] != null && labelRects[i].contains(x, y)) {
                //rm.select(i);
                // this needs to run on a separate thread, at least on OS X
                // "update2" does not clone the ROI so the "Show All"
                // outline moves as the user moves the RO.
                new ij.macro.MacroRunner("roiManager('select', " + i + "); roiManager('update2');");
                return true;
            }
        }
        return false;
    }

    void zoomToSelection(int x, int y) {
        IJ.setKeyUp(IJ.ALL_KEYS);
        String macro =
                "args = split(getArgument);\n"
                + "x1=parseInt(args[0]); y1=parseInt(args[1]); flags=20;\n"
                + "while (flags&20!=0) {\n"
                + "getCursorLoc(x2, y2, z, flags);\n"
                + "if (x2>=x1) x=x1; else x=x2;\n"
                + "if (y2>=y1) y=y1; else y=y2;\n"
                + "makeRectangle(x, y, abs(x2-x1), abs(y2-y1));\n"
                + "wait(10);\n"
                + "}\n"
                + "run('To Selection');\n";
        new MacroRunner(macro, x + " " + y);
    }

    protected void setupScroll(int ox, int oy) {
        xMouseStart = ox;
        yMouseStart = oy;
        xSrcStart = srcRect.x;
        ySrcStart = srcRect.y;
    }

    protected void handlePopupMenu(MouseEvent e) {
        if (disablePopupMenu) {
            return;
        }
        if (IJ.debugMode) {
            IJ.log("show popup: " + (e.isPopupTrigger() ? "true" : "false"));
        }
        int x = e.getX();
        int y = e.getY();
        Roi roi = imp.getRoi();
        if (roi != null && (roi.getType() == Roi.POLYGON || roi.getType() == Roi.POLYLINE || roi.getType() == Roi.ANGLE)
                && roi.getState() == roi.CONSTRUCTING) {
            roi.handleMouseUp(x, y); // simulate double-click to finalize
            roi.handleMouseUp(x, y); // polygon or polyline selection
            return;
        }
//        PopupMenu popup = Menus.getPopupMenu();
//        if (popup != null) {
//            add(popup);
//            if (IJ.isMacOSX()) {
//                IJ.wait(10);
//            }
//            popup.show(this, x, y);
//        }
    }

    public void mouseExited(MouseEvent e) {
        //autoScroll(e);
        IjxImageWindow win = (IjxImageWindow) imp.getWindow();
        if (win != null) {
            setCursor(defaultCursor);
        }
        IJ.showStatus("");
        mouseExited = true;
    }

    /*
    public void autoScroll(MouseEvent e) {
    Roi roi = imp.getRoi();
    if (roi==null || roi.getState()!=roi.CONSTRUCTING || srcRect.width>=imageWidth || srcRect.height>=imageHeight
    || !(roi.getType()==Roi.POLYGON || roi.getType()==Roi.POLYLINE || roi.getType()==Roi.ANGLE))
    return;
    int sx = e.getX();
    int sy = e.getY();
    xMouseStart = srcRect.x+srcRect.width/2;
    yMouseStart = srcRect.y+srcRect.height/2;
    Rectangle r = roi.getBounds();
    Dimension size = getSize();
    int deltax=0, deltay=0;
    if (sx<0)
    deltax = srcRect.width/4;
    else if (sx>size.width)
    deltax = -srcRect.width/4;
    if (sy<0)
    deltay = srcRect.height/4;
    else if (sy>size.height)
    deltay = -srcRect.height/4;
    //IJ.log("autoscroll: "+sx+" "+sy+" "+deltax+" "+deltay+" "+r);
    scroll(screenX(xMouseStart+deltax), screenY(yMouseStart+deltay));
    }
     */
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        xMouse = offScreenX(x);
        yMouse = offScreenY(y);
        flags = e.getModifiers();
        //IJ.log("mouseDragged: "+flags);
        if (flags == 0) // workaround for Mac OS 9 bug
        {
            flags = InputEvent.BUTTON1_MASK;
        }
        if (toolbar.getToolId() == IjxToolbar.HAND || IJ.spaceBarDown()) {
            scroll(x, y);
        } else {
            IJ.setInputEvent(e);
            Roi roi = imp.getRoi();
            if (roi != null) {
                roi.handleMouseDrag(x, y, flags);
            }
        }
    }

    protected void handleRoiMouseDown(MouseEvent e) {
        int sx = e.getX();
        int sy = e.getY();
        int ox = offScreenX(sx);
        int oy = offScreenY(sy);
        Roi roi = imp.getRoi();
        int handle = roi != null ? roi.isHandle(sx, sy) : -1;
        boolean multiPointMode = roi != null && (roi instanceof PointRoi) && handle == -1
                && toolbar.getToolId() == IjxToolbar.POINT && toolbar.getMultiPointMode();
        if (multiPointMode) {
            imp.setRoi(((PointRoi) roi).addPoint(ox, oy));
            return;
        }
        setRoiModState(e, roi, handle);
        if (roi != null) {
            if (handle >= 0) {
                roi.mouseDownInHandle(handle, sx, sy);
                return;
            }
            Rectangle r = roi.getBounds();
            int type = roi.getType();
            if (type == Roi.RECTANGLE && r.width == imp.getWidth() && r.height == imp.getHeight()
                    && roi.getPasteMode() == Roi.NOT_PASTING && !(roi instanceof ImageRoi)) {
                imp.killRoi();
                return;
            }
            if (roi.contains(ox, oy)) {
                if (roi.getModState() == Roi.NO_MODS) {
                    roi.handleMouseDown(sx, sy);
                } else {
                    imp.killRoi();
                    imp.createNewRoi(sx, sy);
                }
                return;
            }
            if ((type == Roi.POLYGON || type == Roi.POLYLINE || type == Roi.ANGLE)
                    && roi.getState() == roi.CONSTRUCTING) {
                return;
            }
            int tool = toolbar.getToolId();
            if ((tool == Toolbar.POLYGON || tool == Toolbar.POLYLINE || tool == Toolbar.ANGLE) && !(IJ.shiftKeyDown() || IJ.altKeyDown())) {
                imp.killRoi();
                return;
            }
        }
        imp.createNewRoi(sx, sy);
    }

    void setRoiModState(MouseEvent e, Roi roi, int handle) {
        if (roi == null || (handle >= 0 && roi.getModState() == Roi.NO_MODS)) {
            return;
        }
        if (roi.getState() == Roi.CONSTRUCTING) {
            return;
        }
        int tool = toolbar.getToolId();
        if (tool > Toolbar.FREEROI && tool != Toolbar.WAND && tool != Toolbar.POINT) {
            roi.setModState(Roi.NO_MODS);
            return;
        }
        if (e.isShiftDown()) {
            roi.setModState(Roi.ADD_TO_ROI);
        } else if (e.isAltDown()) {
            roi.setModState(Roi.SUBTRACT_FROM_ROI);
        } else {
            roi.setModState(Roi.NO_MODS);
        }
        //IJ.log("setRoiModState: "+roi.modState+" "+ roi.state);
    }

    /** Disable/enable popup menu. */
    public void disablePopupMenu(boolean status) {
        disablePopupMenu = status;
    }

    /** Enables/disables the ROI Manager "Show All" mode. */
    public void setShowAllROIs(boolean showAllROIs) {
        this.showAllROIs = showAllROIs;
    }

    /** Returns the state of the ROI Manager "Show All" flag. */
    public boolean getShowAllROIs() {
        return showAllROIs;
    }

    /** Return the ROI Manager "Show All" list as an overlay. */
    public Overlay getShowAllList() {
        if (!showAllROIs) {
            return null;
        }
        if (showAllList != null) {
            return showAllList;
        }
        RoiManager rm = RoiManager.getInstance();
        if (rm == null) {
            return null;
        }
        Roi[] rois = rm.getRoisAsArray();
        if (rois.length == 0) {
            return null;
        }
        Overlay overlay = new Overlay();
        for (int i = 0; i < rois.length; i++) {
            overlay.add((Roi) rois[i].clone());
        }
        return overlay;
    }

    /** Use IjxImagePlus.setOverlay(ij.gui.Overlay). */
    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
        repaint();
    }

    /** Use IjxImagePlus.getOverlay(). */
    public Overlay getOverlay() {
        return overlay;
    }

    /**
     * @deprecated
     * replaced by IjxImagePlus.setOverlay(ij.gui.Overlay)
     */
    public void setDisplayList(Vector list) {
        if (list != null) {
            Overlay list2 = new Overlay();
            list2.setVector(list);
            setOverlay(list2);
        } else {
            setOverlay(null);
        }
        if (overlay != null) {
            overlay.drawLabels(overlay.size() > 0 && overlay.get(0).getStrokeColor() == null);
        } else {
            customRoi = false;
        }
        repaint();
    }

    /**
     * @deprecated
     * replaced by IjxImagePlus.setOverlay(Shape, Color, BasicStroke)
     */
    public void setDisplayList(Shape shape, Color color, BasicStroke stroke) {
        if (shape == null) {
            setOverlay(null);
            return;
        }
        Roi roi = new ShapeRoi(shape);
        roi.setStrokeColor(color);
        roi.setStroke(stroke);
        Overlay list = new Overlay();
        list.add(roi);
        setOverlay(list);
    }

    /**
     * @deprecated
     * replaced by IjxImagePlus.setOverlay(Roi, Color, int, Color)
     */
    public void setDisplayList(Roi roi, Color color) {
        roi.setStrokeColor(color);
        Overlay list = new Overlay();
        list.add(roi);
        setOverlay(list);
    }

    /**
     * @deprecated
     * replaced by IjxImagePlus.getOverlay()
     */
    public Vector getDisplayList() {
        if (overlay == null) {
            return null;
        }
        Vector displayList = new Vector();
        for (int i = 0; i < overlay.size(); i++) {
            displayList.add(overlay.get(i));
        }
        return displayList;
    }

    /** Allows plugins (e.g., Orthogonal_Views) to create a custom ROI using a display list. */
    public void setCustomRoi(boolean customRoi) {
        this.customRoi = customRoi;
    }

    /** Called by IJ.showStatus() to prevent status bar text from
    being overwritten until the cursor moves at least 12 pixels. */
    public void setShowCursorStatus(boolean status) {
        showCursorStatus = status;
        if (status == true) {
            sx2 = sy2 = -1000;
        } else {
            sx2 = screenX(xMouse);
            sy2 = screenY(yMouse);
        }
    }

    public void mouseReleased(MouseEvent e) {
        flags = e.getModifiers();
        flags &= ~InputEvent.BUTTON1_MASK; // make sure button 1 bit is not set
        flags &= ~InputEvent.BUTTON2_MASK; // make sure button 2 bit is not set
        flags &= ~InputEvent.BUTTON3_MASK; // make sure button 3 bit is not set
        Roi roi = imp.getRoi();
        if (roi != null) {
            Rectangle r = roi.getBounds();
            int type = roi.getType();
            if ((r.width == 0 || r.height == 0)
                    && !(type == Roi.POLYGON || type == Roi.POLYLINE || type == Roi.ANGLE || type == Roi.LINE)
                    && !(roi instanceof TextRoi)
                    && roi.getState() == roi.CONSTRUCTING
                    && type != roi.POINT) {
                imp.killRoi();
            } else {
                roi.handleMouseUp(e.getX(), e.getY());
                if (roi.getType() == Roi.LINE && roi.getLength() == 0.0) {
                    imp.killRoi();
                }
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (ij == null) {
            return;
        }
        int sx = e.getX();
        int sy = e.getY();
        int ox = offScreenX(sx);
        int oy = offScreenY(sy);
        flags = e.getModifiers();
        setCursor(sx, sy, ox, oy);
        IJ.setInputEvent(e);
        Roi roi = imp.getRoi();
        if (roi != null && (roi.getType() == Roi.POLYGON || roi.getType() == Roi.POLYLINE || roi.getType() == Roi.ANGLE)
                && roi.getState() == roi.CONSTRUCTING) {
            PolygonRoi pRoi = (PolygonRoi) roi;
            pRoi.handleMouseMove(ox, oy);
        } else {
            if (ox < imageWidth && oy < imageHeight) {
                IjxImageWindow win = imp.getWindow();
                // Cursor must move at least 12 pixels before text
                // displayed using IJ.showStatus() is overwritten.
                if ((sx - sx2) * (sx - sx2) + (sy - sy2) * (sy - sy2) > 144) {
                    showCursorStatus = true;
                }
                if (win != null && showCursorStatus) {
                    win.mouseMoved(ox, oy);
                }
            } else {
                IJ.showStatus("");
            }

        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public Component getCanvas() {
        return this;
    }
    private void packImageWindow() {
        IjxImageWindow win = imp.getWindow();
        if (Frame.class.isAssignableFrom(win.getClass())) {
            ((Frame) win).pack();
        }
        if (win instanceof JInternalFrame) {
            ((JInternalFrame) win).pack();
        }
    }
}
