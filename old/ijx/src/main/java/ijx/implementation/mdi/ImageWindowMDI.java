package ijx.implementation.mdi;

import ijx.gui.AbstractImageWindow;
import ijx.gui.IjxImageDisplayWindow;
import ijx.implementation.swing.*;

import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 *
 * @author GBH <imagejdev.org>
 */
public class ImageWindowMDI extends JInternalFrame implements IjxImageDisplayWindow {
    private AbstractImageWindow drawer;

    public ImageWindowMDI() {
        super("ImageWindow",
                true, //resizable
                true, //closable
                true, //maximizable
                true);//iconifiable

        //setLocation(xOffset*openFrameCount, yOffset*openFrameCount);

    }

    public void setDrawingDelegate(AbstractImageWindow _drawer) {
        this.drawer = _drawer;
    }

//    @Override
//    public void update(Graphics g) {
//        paintComponent(g);
//    }
//
//    @Override
//    public void paint(Graphics g) {
//        paintComponent(g);
//    }
//
//    public void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        drawer.paintMe(g);
//    }
}
