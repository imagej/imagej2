/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx;

import ijx.gui.IjxProgressBar;
import ijx.gui.IjxWindow;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.KeyListener;

import java.awt.event.MouseListener;
import java.awt.event.WindowListener;

/**
 *
 * @author GBH
 */
public interface IjxTopComponent extends IjxWindow, MouseListener, WindowListener, KeyListener {
    String IJ_X = "ij.x", IJ_Y = "ij.y";
    Font SansSerif12 = new Font("SansSerif", Font.PLAIN, 12); // SansSerif, 12-point, plain font.

    Frame getFrame();

    void setMenuBar(Object menuBar);
    void setToolbar(Object toolbar);
    void addStatusBar();

    Point getPreferredLocation();

    IjxProgressBar getProgressBar();

    void setIcon() throws Exception;

    void setBackground(Color color);

    void showStatus(java.lang.String message);

    Component getStatusBar();

    void finishAndShow();

    public void setDropTarget(Object object);
    /*  implementors also need to implement:

    void mouseClicked(MouseEvent e);

    void mouseEntered(MouseEvent e);

    void mouseExited(MouseEvent e);

    void mousePressed(MouseEvent e);

    void mouseReleased(MouseEvent e);


    void windowActivated(WindowEvent e);

    void windowClosed(WindowEvent e);

    void windowClosing(WindowEvent e);

    void windowDeactivated(WindowEvent e);

    void windowDeiconified(WindowEvent e);

    void windowIconified(WindowEvent e);

    void windowOpened(WindowEvent e);
    

    boolean canClose();

    void close();
    
    boolean isClosed();
     */
}
