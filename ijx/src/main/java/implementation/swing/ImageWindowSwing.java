package implementation.swing;

import ijx.gui.AbstractImageWindow;
import ijx.gui.IjxImageDisplayWindow;
import java.awt.Graphics;
import javax.swing.JFrame;

/**
 *
 * @author GBH <imagejdev.org>
 */
public class ImageWindowSwing extends JFrame implements IjxImageDisplayWindow {

    private AbstractImageWindow drawer;

    public ImageWindowSwing() {
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
        super.paintComponents(g);
        drawer.paintMe(g);
    }
}
