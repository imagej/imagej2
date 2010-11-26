package ijx.implementation.awt;

import ijx.IJ;
import ijx.Prefs;
import ijx.gui.Overlay;
import ijx.roi.Roi;
import ijx.util.Java2;
import ijx.IjxImagePlus;
import ijx.gui.AbstractImageCanvas;
import ijx.gui.IjxImageDisplayPanel;
import ijx.implementation.swing.*;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Rectangle;
import javax.swing.JPanel;

/**
 *
 * @author GBH <imagejdev.org>
 */
public class ImagePanelAWT extends Canvas implements IjxImageDisplayPanel {
    private final IjxImagePlus imp;
    private final int imageWidth;
    private final int imageHeight;
    private int dstWidth;
    private int dstHeight;
    protected Rectangle srcRect;

    private AbstractImageCanvas drawer;

    public ImagePanelAWT(IjxImagePlus _imp) {
        this.imp = _imp;
        int width = imp.getWidth();
        int height = imp.getHeight();
        imageWidth = width;
        imageHeight = height;
        srcRect = new Rectangle(0, 0, imageWidth, imageHeight);
        setDrawingSize(imageWidth, imageHeight);
    }

    public void setDrawingDelegate(AbstractImageCanvas _drawer) {
        this.drawer = _drawer;
    }

    public void setDrawingSize(int width, int height) {
        dstWidth = width;
        dstHeight = height;
        setSize(dstWidth, dstHeight);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(dstWidth, dstHeight);
    }
    @Override
    public void update(Graphics g) {
        drawer.paintMe(g);
    }
    @Override
    public void paint(Graphics g) {
                drawer.paintMe(g);
    }

}
