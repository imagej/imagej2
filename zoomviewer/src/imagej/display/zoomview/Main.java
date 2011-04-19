/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.display.zoomview;

import java.io.File;
import java.awt.Adjustable;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Customer
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        final JFrame frame = new JFrame("ImgPanel Test Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SetupDialog setupDialog = new SetupDialog(frame);
        System.out.println("setupDialog filename " + setupDialog.getFileName());

        // this is just a nominal pixel size for fake images
        int dim[] = { 1024, 768 };

        switch (setupDialog.getPixels()) {
            case ACTUAL:
                break;
            case X4:
                dim[0] *= 4;
                dim[1] *= 4;
                break;
            case X16:
                dim[0] *= 16;
                dim[1] *= 16;
                break;
            case MP50:
                dim = new int[] { 7 * 1024, 7 * 1024 };
                break;
            case MP100:
                dim = new int[] { 10 * 1024 , 10 * 1024 };
                break;
            case MP500:
                dim = new int[] { 23 * 1024, 23 * 1024 };
                break;
            case GP1:
                dim = new int[] { 32 * 1024, 32 * 1024 };
                break;
            case GP4:
                dim = new int[] { 64 * 1024, 64 * 1024 };
                break;
        }

        //TODO setting up the zoom viewer, could happen elsewhere

        //TODO the cache should be shared
        TileCache tileCache = new TileCache(setupDialog.getCacheSize());

        //TODO need factory that projects ImgLib images
        ITileFactory factory = new MyTileFactory(new File(setupDialog.getFileName()));

        ZoomTileServer zoomTileServer = new ZoomTileServer();
        zoomTileServer.init(tileCache, factory, dim);

        final ImgZoomPanel imgZoomPanel = new ImgZoomPanel(zoomTileServer);
        frame.setContentPane(imgZoomPanel);
        frame.pack();
        center(frame);
        frame.setVisible(true);
    }


    private static void center(final Window win) {
        final Dimension size = win.getSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = (screen.width - size.width) / 2;
        final int h = (screen.height - size.height) / 2;
        win.setLocation(w, h);
    }
}
