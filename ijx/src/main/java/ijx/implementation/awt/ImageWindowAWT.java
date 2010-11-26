package ijx.implementation.awt;

import ijx.gui.AbstractImageWindow;
import ijx.gui.IjxImageDisplayWindow;
import java.awt.Frame;
import java.awt.Graphics;

/**
 *
 * @author GBH <imagejdev.org>
 */
public class ImageWindowAWT extends Frame implements IjxImageDisplayWindow {

    private AbstractImageWindow drawer;

    public ImageWindowAWT() {
    }

    public void setDrawingDelegate(AbstractImageWindow _drawer) {
        this.drawer = _drawer;
    }

    @Override
    public void update(Graphics g) {
        paintComponent(g);
    }
    @Override
    public void paint(Graphics g) {
        paintComponent(g);
    }

    public void paintComponent(Graphics g) {
        drawer.paintMe(g);
    }
}
