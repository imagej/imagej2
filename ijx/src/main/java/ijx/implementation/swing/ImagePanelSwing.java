package ijx.implementation.swing;

import ijx.IjxImagePlus;
import ijx.gui.AbstractImageCanvas;
import ijx.gui.IjxImageDisplayPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JPanel;

/**
 *
 * @author GBH <imagejdev.org>
 */
public class ImagePanelSwing extends JPanel implements IjxImageDisplayPanel {
    private final IjxImagePlus imp;
    private final int imageWidth;
    private final int imageHeight;
    private int dstWidth;
    private int dstHeight;
    protected Rectangle srcRect;

    private AbstractImageCanvas drawer;

    public ImagePanelSwing(IjxImagePlus _imp) {
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
        paintComponent(g);
    }
    @Override
    public void paint(Graphics g) {
        paintComponent(g);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawer.paintMe(g);
    }
}
