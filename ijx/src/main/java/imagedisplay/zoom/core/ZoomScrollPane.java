package imagedisplay.zoom.core;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

/**
 * zoomscrollpane works together with zoomjpanel to track the zoom space size
 * and the zoom center position
 *
 * If you need to modify this file or use this library in your own project, please
 * let me know. Thanks!
 *
 * @author  Qiang Yu (qiangyu@gmail.com)
 */
public class ZoomScrollPane
        extends DummyScrollPane implements AdjustmentListener, KeyListener {

    protected ZoomJPanel zoomPanel = null;

    public ZoomJPanel getZoomPanel() {
        return zoomPanel;
    }


    /* constructors */
    public ZoomScrollPane(ZoomJPanel view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
        if (view != null) {
            zoomPanel = view;
        } else {
            zoomPanel = new ZoomJPanel();
            setViewportView(zoomPanel);
        }
        setCorner(JScrollPane.LOWER_LEFT_CORNER, new JPanel());
        setCorner(JScrollPane.LOWER_RIGHT_CORNER, new JPanel());
        setCorner(JScrollPane.UPPER_LEFT_CORNER, new JPanel());
        setCorner(JScrollPane.UPPER_RIGHT_CORNER, new JPanel());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent ce) {
                adjustScrollBars();
            }
        });
        horizontalScrollBar.addAdjustmentListener(this);
        verticalScrollBar.addAdjustmentListener(this);
        horizontalScrollBar.setUnitIncrement(10);
        verticalScrollBar.setUnitIncrement(10);
        zoomPanel.addKeyListener(this);
    }

    public ZoomScrollPane(ZoomJPanel view) {
        this(view, VERTICAL_SCROLLBAR_AS_NEEDED,
                HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
    private boolean initOnce = false;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (!initOnce) {
            adjustScrollBars();
            revalidate();
            initOnce = true;
        }
    }

    /**
     * adjust the scrollbars. scrollbar properties need to be updated when:
     * 1. the window is resized or
     * 2. zooming happened
     *
     */
    protected void adjustScrollBars() {
        Point p = zoomPanel.getOriginOnZoomedSpace();
        Rectangle zoomedSpace = zoomPanel.getZoomedSpace();
        Dimension size = zoomPanel.getSize();
        if (zoomedSpace != null) {
            zoomedSpace = zoomedSpace.union(new Rectangle(p.x, p.y,
                    size.width, size.height));
            setVSBValues(p.y, zoomedSpace.y,
                    zoomedSpace.y + zoomedSpace.height);
            setHSBValues(p.x, zoomedSpace.x,
                    zoomedSpace.x + zoomedSpace.width);
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource() == horizontalScrollBar) {
            hScrollBarValueChanged(e);
            return;
        }
        if (e.getSource() == verticalScrollBar) {
            vScrollBarValueChanged(e);
            return;
        }
    }

    /** @todo Panning with mouse drag */
    private void pan(int x, int y) {
        Point p = zoomPanel.getOriginOnZoomedSpace();
//        if (p.x == e.getValue()) {
//            return;
//        p.x = e.getValue();
        zoomPanel.setOriginOnZoomedSpace(p.x, p.y);
        zoomPanel.repaint();
        adjustScrollBars();
    }

    private void hScrollBarValueChanged(AdjustmentEvent e) {
        Point p = zoomPanel.getOriginOnZoomedSpace();
        if (p.x == e.getValue()) {
            return;
        }
        p.x = e.getValue();
        zoomPanel.setOriginOnZoomedSpace(p.x, p.y);
        zoomPanel.repaint();
        adjustScrollBars();
    }

    private void vScrollBarValueChanged(AdjustmentEvent e) {
        Point p = zoomPanel.getOriginOnZoomedSpace();
        if (p.y == e.getValue()) {
            return;
        }
        p.y = e.getValue();
        zoomPanel.setOriginOnZoomedSpace(p.x, p.y);
        zoomPanel.repaint();
        adjustScrollBars();
    }

    /**
     * doing zooming
     * @param cx zoom center x
     * @param cy zoom center y
     * @param zfx zoom factor x
     * @param zfy zoom factor y
     */
    public void zoom(double cx, double cy, double zfx, double zfy) {
        if (zoomPanel.setZoomParameters(cx, cy, zfx, zfy)) {
            zoomPanel.repaint();
            adjustScrollBars();
            revalidate();
        }
    }

    /**
     * fit the whole pics so that it fills the whole window
     */
    public void fitToScreen() {
        if (zoomPanel.fitToScreen()) {
            zoomPanel.repaint();
            adjustScrollBars();
            revalidate();
        }
    }

    /**
     * restore to the initial status
     */
    public void restore() {
        zoomPanel.restore();
        initOnce = false;
        repaint();
    }

    /**
     * zoom specified rectangle so that it fills the whole window
     * @param r
     */
    public void zoomRectangleToWholeWindow(Rectangle r) {
        if (r != null && r.width > 0 && r.height > 0) {
            if (zoomPanel.zoomRectangleToWholeWindow(r)) {
                zoomPanel.repaint();
                adjustScrollBars();
                revalidate();
            }
        }
    }

    /**
     * transform point x, y in the device space to user space
     * @param x
     * @param y
     * @return
     */
    public Point2D.Double toUserSpace(int x, int y) {
        return zoomPanel.toUserSpace(x, y);
    }

    /**
     * transform point p in the device space to user space
     * @param p
     * @return
     */
    public Point2D.Double toUserSpace(Point p) {
        return zoomPanel.toUserSpace(p);
    }

    /**
     * transform point p in the user space to device space
     * @param p
     * @return
     */
    public Point toDeviceSpace(Point2D.Double p) {
        return zoomPanel.toDeviceSpace(p);
    }

    /**
     * transform point (x, y) in the user space to device space
     * @param x
     * @param y
     * @return
     */
    public Point toDeviceSpace(double x, double y) {
        return zoomPanel.toDeviceSpace(new Point2D.Double(x, y));
    }

    /**
     * make the background invalidate.
     * if background is saved in a bitmap by the zoomjpanel, the bitmap will
     * be recreated
     */
    public void invalidateBackground() {
        zoomPanel.invalidateBackground();
        initOnce = false;
    }

    /**
     * get the x zoom factor
     * @return
     */
    public double getZoomFactorX() {
        return this.zoomPanel.getZoomFactorX();
    }

    /**
     * get the y zoom factor
     * @return
     */
    public double getZoomFactorY() {
        return this.zoomPanel.getZoomFactorY();
    }

    /**
     * let the scrollbars keyboard arrows sensitive.
     * to make it happen, you need to do "requestFocusInWindow" in the mouse
     * event handler of your zoomjpanel derived class, and you need to
     * call setFocusable(true) in your your zoomjpanel derived class
     */
    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            this.verticalScrollBar.setValue(this.verticalScrollBar.getValue() - 10);
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            this.verticalScrollBar.setValue(this.verticalScrollBar.getValue() + 10);
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            this.horizontalScrollBar.setValue(this.horizontalScrollBar.getValue() - 10);
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            this.horizontalScrollBar.setValue(this.horizontalScrollBar.getValue() + 10);
        }
    }

    public void keyReleased(KeyEvent e) {
    }
}
