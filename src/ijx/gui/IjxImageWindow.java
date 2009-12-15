/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx.gui;

import ijx.IjxImagePlus;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

/**
 * IjxImageWindow: interface for a generalized Image Window in ImageJ
 * 
 * Refactored from ImageJ by Grant B. Harris, November 2008, at ImageJ 2008, Luxembourg
 **/
public interface IjxImageWindow extends IjxWindow, FocusListener, MouseWheelListener, WindowListener, WindowStateListener {

  int MIN_HEIGHT = 32;
  int MIN_WIDTH = 128;

  public boolean isRunning();

  public void setRunning(boolean b);

  public boolean isRunning2();

  public void setRunning2(boolean b);

  Component add(Component comp);

  /**
   * Removes this window from the window list and disposes of it.
   * Returns false if the user cancels the "save changes" dialog.
   */
//    void close();
//
//    boolean canClose();
  /**
   * Copies the current ROI to the clipboard. The entire
   * image is copied if there is no ROI.
   */
  void copy(boolean cut);

  /**
   * Creates the subtitle.
   */
  String createSubtitle();

  /**
   * Draws the subtitle.
   */
  void drawInfo(Graphics g);

  IjxImageCanvas getCanvas();

  IjxImagePlus getImagePlus();

  void updateImage(IjxImagePlus imp);

  double getInitialMagnification();

  /**
   * Override Container getInsets() to make room for some text above the image.
   */
  Insets getInsets();

  Rectangle getMaximumBounds();

  void setBounds(Rectangle r);

  void setMaximizedBounds(Rectangle r);

  void setMaxBoundsTime(long currentTimeMillis);

  long getMaxBoundsTime();

  /**
   * Has this window been closed?
   */
  //boolean isClosed();
  
  void maximize();

  void minimize();

  /**
   * This method is called by ImageCanvas.mouseMoved(MouseEvent).
   * @see ij.gui.ImageCanvas#mouseMoved
   */
  void mouseMoved(int x, int y);

  //void mouseWheelMoved(MouseWheelEvent event);
  void paint(Graphics g);

  void repaint();

  void pack();

  void paste();

  /**
   * Moves and resizes this window. Changes the
   * magnification so the image fills the window.
   */
  void setLocationAndSize(int x, int y, int width, int height);

  void setForeground(java.awt.Color c);

  void setBackground(java.awt.Color c);

//    String toString();
//
//    void focusGained(FocusEvent e);
//
//    void focusLost(FocusEvent e);
//
//    void windowActivated(WindowEvent e);
//
//    void windowClosed(WindowEvent e);
//
//    void windowClosing(WindowEvent e);
//
//    void windowDeactivated(WindowEvent e);
//
//    void windowDeiconified(WindowEvent e);
//
//    void windowIconified(WindowEvent e);
//
//    void windowOpened(WindowEvent e);
//
//    void windowStateChanged(WindowEvent e);
}
