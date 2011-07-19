package imagej.ui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * A collection of generally useful Swing utility methods.
 *
 * Copyright:    Copyright (c) 2004
 * Company:      Superliminal Software
 *
 * @author Melinda Green
 * 
 *  Added to imagej.ui.swing by GBH, Jun 2011
 */

public class StaticSwingUtils {
  /// to disallow instantiation/

  private StaticSwingUtils() {
  }

  public static JButton createButton16(ImageIcon icon) {
    return createButton16(icon, null);
  }

  public static JButton createButton16(ImageIcon icon, String toolTip) {
    JButton newButton = new JButton();
    newButton.setMargin(new Insets(0, 0, 0, 0));
    newButton.setMinimumSize(new Dimension(16, 16));
    if (toolTip != null) {
      newButton.setToolTipText(toolTip);
    }
    newButton.setIcon(icon);
    return newButton;
  }

  /**
   * Adds a control hot key to the containing window of a component.
   * In the case of buttons and menu items it also attaches the given action to the component itself.
   *
   * @param key one of the KeyEvent keyboard constants
   * @param to component to map to
   * @param actionName unique action name for the component's action map
   * @param action callback to notify when control key is pressed
   */
  public static void addHotKey(int key, JComponent to, String actionName, Action action) {
    KeyStroke keystroke = KeyStroke.getKeyStroke(key, java.awt.event.InputEvent.CTRL_MASK);
    InputMap map = to.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    map.put(keystroke, actionName);
    to.getActionMap().put(actionName, action);
    if (to instanceof JMenuItem) {
      ((JMenuItem) to).setAccelerator(keystroke);
    }
    if (to instanceof AbstractButton) /// includes JMenuItem/
    {
      ((AbstractButton) to).addActionListener(action);
    }
  }

  /**
   * Finds the top-level JFrame in the component tree containing a given component.
   * @param comp leaf component to search up from
   * @return the containing JFrame or null if none
   */
  public static JFrame getTopFrame(Component comp) {
    if (comp == null) {
      return null;
    }
    while (comp.getParent() != null) {
      comp = comp.getParent();
    }
    if (comp instanceof JFrame) {
      return (JFrame) comp;
    }
    return null;
  }

  /**
   * Different platforms use different mouse gestures as pop-up triggers.
   * This class unifies them. Just implement the abstract popUp method
   * to add your handler.
   */
  public static abstract class PopperUpper extends MouseAdapter {
    /// To work properly on all platforms, must check on mouse press as well as release/

    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) {
        popUp(e);
      }
    }

    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        popUp(e);
      }
    }

    protected abstract void popUp(MouseEvent e);
  }
  /// simple Clipboard string routines/

  public static void placeInClipboard(String str) {
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
            new StringSelection(str), null);
  }

  public static String getFromClipboard() {
    String str = null;
    try {
      str = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(
              DataFlavor.stringFlavor);
    } catch (UnsupportedFlavorException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return str;
  }

  // --- Ultimate Parent ---------------------------------------------
  // Find ultimate parent window, even if in a popup
  // Possible usage:
  //   public void actionPerformed (ActionEvent evt) {
  //   Component c = findUltimateParent((Component) evt.getSource());
  // NEEDS TESTING

  public Component findUltimateParent(Component c) {
    Component parent = c;
    while (null != parent.getParent()) {
      parent = parent.getParent();
      if (parent instanceof JPopupMenu) {
        JPopupMenu popup = (JPopupMenu) parent;
        parent = popup.getInvoker();
      }
    }
    return parent;
  }
  // == Diagnostics =======================================================

  private static Point lastFramePosition = new Point(5, 5);

  public static void locateCenter(java.awt.Container component) {
    int w = component.getWidth();
    int h = component.getHeight();
    Rectangle bounds = getWorkSpaceBounds();
    int x = (int) (bounds.getX() + (bounds.getWidth() - w) / 2);
    int y = (int) (bounds.getY() + (bounds.getHeight() - h) / 2);
    component.setLocation(x, y);
  }

  public static void locateUpperRight(java.awt.Container component) {
    int w = component.getWidth();
    int h = component.getHeight();
    Rectangle bounds = getWorkSpaceBounds();
    int x = (int) (bounds.getX() + bounds.getWidth() - w);
    int y = (int) (bounds.getY());
    component.setLocation(x, y);
  }

  public static void locateLowerRight(java.awt.Container component) {
    int w = component.getWidth();
    int h = component.getHeight();
    Rectangle bounds = getWorkSpaceBounds();
    int x = (int) (bounds.getX() + bounds.getWidth() - w);
    int y = (int) (bounds.getY() + bounds.getHeight() - h);
    component.setLocation(x, y);
  }

  public static void locateUpperLeft(java.awt.Container component) {
    Rectangle bounds = getWorkSpaceBounds();
    component.setLocation((int) bounds.getX(), (int) bounds.getY());
  }

  public static void locateLowerLeft(java.awt.Container component) {
    int w = component.getWidth();
    int h = component.getHeight();
    Rectangle bounds = getWorkSpaceBounds();
    int x = (int) (bounds.getX());
    int y = (int) (bounds.getY() + bounds.getHeight() - h);
    component.setLocation(x, y);
  }

  public static Point nextFramePosition() {
    lastFramePosition.x = lastFramePosition.x + 16;
    lastFramePosition.y = lastFramePosition.y + 16;
    if (lastFramePosition.x > 200) {
      lastFramePosition.x = 5;
      lastFramePosition.y = 5;
    }
    return lastFramePosition;
  }

  public static Rectangle getWorkSpaceBounds() {
    // @todo deal with default when multi-monitor
    return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
  }

  public static void dispatchToEDT(Runnable runnable) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(runnable);
    } else {
      runnable.run();
    }
  }

  public static void dispatchToEDTWait(Runnable runnable) {
    if (!SwingUtilities.isEventDispatchThread()) {
      try {
        SwingUtilities.invokeAndWait(runnable);
      } catch (InterruptedException ex) {
        Logger.getLogger(StaticSwingUtils.class.getName()).log(Level.SEVERE, null, ex);
      } catch (InvocationTargetException ex) {
        Logger.getLogger(StaticSwingUtils.class.getName()).log(Level.SEVERE, null, ex);
      }
    } else {
      runnable.run();
    }
  }
}
