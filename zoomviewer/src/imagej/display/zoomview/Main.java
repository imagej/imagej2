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
		final ImgZoomPanel imgZoomPanel = new ImgZoomPanel();
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
