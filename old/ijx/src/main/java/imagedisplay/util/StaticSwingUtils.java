package imagedisplay.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;

/**
 * A collection of generally useful Swing utility methods.
 *
 * Copyright:    Copyright (c) 2004
 * Company:      Superliminal Software
 *
 * @author Melinda Green
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

  /**
   * Draws the given string into the given graphics with the area behind the string
   * filled with a given background color.
   */
  public static void fillString(String str, int x, int y, Color bg, Graphics g) {
    Rectangle2D strrect = g.getFontMetrics().getStringBounds(str, null);
    Color ocolor = g.getColor();
    g.setColor(bg);
    g.fillRect((int) (x + strrect.getX()), (int) (y + strrect.getY()), (int) (strrect.getWidth()), (int) (strrect.getHeight()));
    g.setColor(ocolor);
    g.drawString(str, x, y);
  }

  /**
   * Utility class that initializes a meduim sized, screen-centered, exit-on-close JFrame.
   * Mostly useful for simple example main programs.
   */
  public static class QuickFrame extends JFrame {

    public QuickFrame(String title) {
      super(title);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(640, 480);
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      setLocation(
              Math.max(0, screenSize.width / 2 - getWidth() / 2),
              Math.max(0, screenSize.height / 2 - getHeight() / 2));
    }
  }

  /**
   * Selection utility in the style of the JOptionPane.showXxxDialog methods.
   * Given a JTree, presents an option dialog presenting the tree allowing users to select a node.
   * @param tree is the tree to display
   * @param parent is the component to anchor the diaglog to
   * @return the path of the selected tree node or null if cancelled.
   */
  public static TreePath showTreeNodeChooser(JTree tree, String title, Component parent) {
    final String OK = "OK",  CANCEL = "Cancel";
    final JButton ok_butt = new JButton(OK),  cancel_butt = new JButton(CANCEL);
    final TreePath selected[] = new TreePath[]{tree.getLeadSelectionPath()}; /// only an array so it can be final, yet mutable/
    ok_butt.setEnabled(selected[0] != null);
    final JOptionPane option_pane = new JOptionPane(new JScrollPane(tree), JOptionPane.QUESTION_MESSAGE,
            JOptionPane.DEFAULT_OPTION, null, new Object[]{ok_butt, cancel_butt});
    ok_butt.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        option_pane.setValue(OK);
      }
    });
    cancel_butt.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        option_pane.setValue(CANCEL);
        selected[0] = null;
      }
    });
    TreeSelectionListener tsl = new TreeSelectionListener() {

      public void valueChanged(TreeSelectionEvent e) {
        selected[0] = e.getNewLeadSelectionPath();
        ok_butt.setEnabled(selected[0] != null);
      }
    };
    JDialog dialog = option_pane.createDialog(parent, title);
    tree.addTreeSelectionListener(tsl); /// to start monitoring user tree selections/
    dialog.setVisible(true); /// present modal tree dialog to user/
    tree.removeTreeSelectionListener(tsl); /// don't want to clutter caller's tree with listeners/
    return OK.equals(option_pane.getValue()) ? selected[0] : null;
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

  public static void dumpUIDefaults() {
    UIDefaults defaults = UIManager.getDefaults();
    Enumeration enumer = defaults.keys();
    while (enumer.hasMoreElements()) {
      Object key = enumer.nextElement();
      System.out.println(key + ": " + defaults.get(key));
    }
  }
  // e859. Converting a KeyStroke to a String
  // The KeyStroke.toString() method does not return a string that can be
  // parsed by KeyStroke.getKeyStroke(). The method keyStroke2String() in
  //this example returns a string that is parseable by KeyStroke.getKeyStroke().
  // However, there is one keystroke that cannot be represented as a string that
  // can be parsed back to a keystroke - - a typed space character. In order to
  // bind an action to a typed space character,
  // KeyStroke.getKeyStroke(new Character(' '), 0) needs to be called.

  public static String keyStroke2String(KeyStroke key) {
    StringBuffer s = new StringBuffer(50);
    int m = key.getModifiers();

    if ((m & (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK)) != 0) {
      s.append("shift ");
    }
    if ((m & (InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK)) != 0) {
      s.append("ctrl ");
    }
    if ((m & (InputEvent.META_DOWN_MASK | InputEvent.META_MASK)) != 0) {
      s.append("meta ");
    }
    if ((m & (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) != 0) {
      s.append("alt ");
    }
    if ((m & (InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON1_MASK)) != 0) {
      s.append("button1 ");
    }
    if ((m & (InputEvent.BUTTON2_DOWN_MASK | InputEvent.BUTTON2_MASK)) != 0) {
      s.append("button2 ");
    }
    if ((m & (InputEvent.BUTTON3_DOWN_MASK | InputEvent.BUTTON3_MASK)) != 0) {
      s.append("button3 ");
    }

    switch (key.getKeyEventType()) {
      case KeyEvent.KEY_TYPED:
        s.append("typed ");
        s.append(key.getKeyChar() + " ");
        break;
      case KeyEvent.KEY_PRESSED:
        s.append("pressed ");
        s.append(getKeyText(key.getKeyCode()) + " ");
        break;
      case KeyEvent.KEY_RELEASED:
        s.append("released ");
        s.append(getKeyText(key.getKeyCode()) + " ");
        break;
      default:
        s.append("unknown-event-type ");
        break;
    }

    return s.toString();
  }

  public static String getKeyText(int keyCode) {
    if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9 ||
            keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
      return String.valueOf((char) keyCode);
    }

    switch (keyCode) {
      case KeyEvent.VK_COMMA:
        return "COMMA";
      case KeyEvent.VK_PERIOD:
        return "PERIOD";
      case KeyEvent.VK_SLASH:
        return "SLASH";
      case KeyEvent.VK_SEMICOLON:
        return "SEMICOLON";
      case KeyEvent.VK_EQUALS:
        return "EQUALS";
      case KeyEvent.VK_OPEN_BRACKET:
        return "OPEN_BRACKET";
      case KeyEvent.VK_BACK_SLASH:
        return "BACK_SLASH";
      case KeyEvent.VK_CLOSE_BRACKET:
        return "CLOSE_BRACKET";

      case KeyEvent.VK_ENTER:
        return "ENTER";
      case KeyEvent.VK_BACK_SPACE:
        return "BACK_SPACE";
      case KeyEvent.VK_TAB:
        return "TAB";
      case KeyEvent.VK_CANCEL:
        return "CANCEL";
      case KeyEvent.VK_CLEAR:
        return "CLEAR";
      case KeyEvent.VK_SHIFT:
        return "SHIFT";
      case KeyEvent.VK_CONTROL:
        return "CONTROL";
      case KeyEvent.VK_ALT:
        return "ALT";
      case KeyEvent.VK_PAUSE:
        return "PAUSE";
      case KeyEvent.VK_CAPS_LOCK:
        return "CAPS_LOCK";
      case KeyEvent.VK_ESCAPE:
        return "ESCAPE";
      case KeyEvent.VK_SPACE:
        return "SPACE";
      case KeyEvent.VK_PAGE_UP:
        return "PAGE_UP";
      case KeyEvent.VK_PAGE_DOWN:
        return "PAGE_DOWN";
      case KeyEvent.VK_END:
        return "END";
      case KeyEvent.VK_HOME:
        return "HOME";
      case KeyEvent.VK_LEFT:
        return "LEFT";
      case KeyEvent.VK_UP:
        return "UP";
      case KeyEvent.VK_RIGHT:
        return "RIGHT";
      case KeyEvent.VK_DOWN:
        return "DOWN";

      // numpad numeric keys handled below
      case KeyEvent.VK_MULTIPLY:
        return "MULTIPLY";
      case KeyEvent.VK_ADD:
        return "ADD";
      case KeyEvent.VK_SEPARATOR:
        return "SEPARATOR";
      case KeyEvent.VK_SUBTRACT:
        return "SUBTRACT";
      case KeyEvent.VK_DECIMAL:
        return "DECIMAL";
      case KeyEvent.VK_DIVIDE:
        return "DIVIDE";
      case KeyEvent.VK_DELETE:
        return "DELETE";
      case KeyEvent.VK_NUM_LOCK:
        return "NUM_LOCK";
      case KeyEvent.VK_SCROLL_LOCK:
        return "SCROLL_LOCK";

      case KeyEvent.VK_F1:
        return "F1";
      case KeyEvent.VK_F2:
        return "F2";
      case KeyEvent.VK_F3:
        return "F3";
      case KeyEvent.VK_F4:
        return "F4";
      case KeyEvent.VK_F5:
        return "F5";
      case KeyEvent.VK_F6:
        return "F6";
      case KeyEvent.VK_F7:
        return "F7";
      case KeyEvent.VK_F8:
        return "F8";
      case KeyEvent.VK_F9:
        return "F9";
      case KeyEvent.VK_F10:
        return "F10";
      case KeyEvent.VK_F11:
        return "F11";
      case KeyEvent.VK_F12:
        return "F12";
      case KeyEvent.VK_F13:
        return "F13";
      case KeyEvent.VK_F14:
        return "F14";
      case KeyEvent.VK_F15:
        return "F15";
      case KeyEvent.VK_F16:
        return "F16";
      case KeyEvent.VK_F17:
        return "F17";
      case KeyEvent.VK_F18:
        return "F18";
      case KeyEvent.VK_F19:
        return "F19";
      case KeyEvent.VK_F20:
        return "F20";
      case KeyEvent.VK_F21:
        return "F21";
      case KeyEvent.VK_F22:
        return "F22";
      case KeyEvent.VK_F23:
        return "F23";
      case KeyEvent.VK_F24:
        return "F24";

      case KeyEvent.VK_PRINTSCREEN:
        return "PRINTSCREEN";
      case KeyEvent.VK_INSERT:
        return "INSERT";
      case KeyEvent.VK_HELP:
        return "HELP";
      case KeyEvent.VK_META:
        return "META";
      case KeyEvent.VK_BACK_QUOTE:
        return "BACK_QUOTE";
      case KeyEvent.VK_QUOTE:
        return "QUOTE";

      case KeyEvent.VK_KP_UP:
        return "KP_UP";
      case KeyEvent.VK_KP_DOWN:
        return "KP_DOWN";
      case KeyEvent.VK_KP_LEFT:
        return "KP_LEFT";
      case KeyEvent.VK_KP_RIGHT:
        return "KP_RIGHT";

      case KeyEvent.VK_DEAD_GRAVE:
        return "DEAD_GRAVE";
      case KeyEvent.VK_DEAD_ACUTE:
        return "DEAD_ACUTE";
      case KeyEvent.VK_DEAD_CIRCUMFLEX:
        return "DEAD_CIRCUMFLEX";
      case KeyEvent.VK_DEAD_TILDE:
        return "DEAD_TILDE";
      case KeyEvent.VK_DEAD_MACRON:
        return "DEAD_MACRON";
      case KeyEvent.VK_DEAD_BREVE:
        return "DEAD_BREVE";
      case KeyEvent.VK_DEAD_ABOVEDOT:
        return "DEAD_ABOVEDOT";
      case KeyEvent.VK_DEAD_DIAERESIS:
        return "DEAD_DIAERESIS";
      case KeyEvent.VK_DEAD_ABOVERING:
        return "DEAD_ABOVERING";
      case KeyEvent.VK_DEAD_DOUBLEACUTE:
        return "DEAD_DOUBLEACUTE";
      case KeyEvent.VK_DEAD_CARON:
        return "DEAD_CARON";
      case KeyEvent.VK_DEAD_CEDILLA:
        return "DEAD_CEDILLA";
      case KeyEvent.VK_DEAD_OGONEK:
        return "DEAD_OGONEK";
      case KeyEvent.VK_DEAD_IOTA:
        return "DEAD_IOTA";
      case KeyEvent.VK_DEAD_VOICED_SOUND:
        return "DEAD_VOICED_SOUND";
      case KeyEvent.VK_DEAD_SEMIVOICED_SOUND:
        return "DEAD_SEMIVOICED_SOUND";

      case KeyEvent.VK_AMPERSAND:
        return "AMPERSAND";
      case KeyEvent.VK_ASTERISK:
        return "ASTERISK";
      case KeyEvent.VK_QUOTEDBL:
        return "QUOTEDBL";
      case KeyEvent.VK_LESS:
        return "LESS";
      case KeyEvent.VK_GREATER:
        return "GREATER";
      case KeyEvent.VK_BRACELEFT:
        return "BRACELEFT";
      case KeyEvent.VK_BRACERIGHT:
        return "BRACERIGHT";
      case KeyEvent.VK_AT:
        return "AT";
      case KeyEvent.VK_COLON:
        return "COLON";
      case KeyEvent.VK_CIRCUMFLEX:
        return "CIRCUMFLEX";
      case KeyEvent.VK_DOLLAR:
        return "DOLLAR";
      case KeyEvent.VK_EURO_SIGN:
        return "EURO_SIGN";
      case KeyEvent.VK_EXCLAMATION_MARK:
        return "EXCLAMATION_MARK";
      case KeyEvent.VK_INVERTED_EXCLAMATION_MARK:
        return "INVERTED_EXCLAMATION_MARK";
      case KeyEvent.VK_LEFT_PARENTHESIS:
        return "LEFT_PARENTHESIS";
      case KeyEvent.VK_NUMBER_SIGN:
        return "NUMBER_SIGN";
      case KeyEvent.VK_MINUS:
        return "MINUS";
      case KeyEvent.VK_PLUS:
        return "PLUS";
      case KeyEvent.VK_RIGHT_PARENTHESIS:
        return "RIGHT_PARENTHESIS";
      case KeyEvent.VK_UNDERSCORE:
        return "UNDERSCORE";

      case KeyEvent.VK_FINAL:
        return "FINAL";
      case KeyEvent.VK_CONVERT:
        return "CONVERT";
      case KeyEvent.VK_NONCONVERT:
        return "NONCONVERT";
      case KeyEvent.VK_ACCEPT:
        return "ACCEPT";
      case KeyEvent.VK_MODECHANGE:
        return "MODECHANGE";
      case KeyEvent.VK_KANA:
        return "KANA";
      case KeyEvent.VK_KANJI:
        return "KANJI";
      case KeyEvent.VK_ALPHANUMERIC:
        return "ALPHANUMERIC";
      case KeyEvent.VK_KATAKANA:
        return "KATAKANA";
      case KeyEvent.VK_HIRAGANA:
        return "HIRAGANA";
      case KeyEvent.VK_FULL_WIDTH:
        return "FULL_WIDTH";
      case KeyEvent.VK_HALF_WIDTH:
        return "HALF_WIDTH";
      case KeyEvent.VK_ROMAN_CHARACTERS:
        return "ROMAN_CHARACTERS";
      case KeyEvent.VK_ALL_CANDIDATES:
        return "ALL_CANDIDATES";
      case KeyEvent.VK_PREVIOUS_CANDIDATE:
        return "PREVIOUS_CANDIDATE";
      case KeyEvent.VK_CODE_INPUT:
        return "CODE_INPUT";
      case KeyEvent.VK_JAPANESE_KATAKANA:
        return "JAPANESE_KATAKANA";
      case KeyEvent.VK_JAPANESE_HIRAGANA:
        return "JAPANESE_HIRAGANA";
      case KeyEvent.VK_JAPANESE_ROMAN:
        return "JAPANESE_ROMAN";
      case KeyEvent.VK_KANA_LOCK:
        return "KANA_LOCK";
      case KeyEvent.VK_INPUT_METHOD_ON_OFF:
        return "INPUT_METHOD_ON_OFF";

      case KeyEvent.VK_AGAIN:
        return "AGAIN";
      case KeyEvent.VK_UNDO:
        return "UNDO";
      case KeyEvent.VK_COPY:
        return "COPY";
      case KeyEvent.VK_PASTE:
        return "PASTE";
      case KeyEvent.VK_CUT:
        return "CUT";
      case KeyEvent.VK_FIND:
        return "FIND";
      case KeyEvent.VK_PROPS:
        return "PROPS";
      case KeyEvent.VK_STOP:
        return "STOP";

      case KeyEvent.VK_COMPOSE:
        return "COMPOSE";
      case KeyEvent.VK_ALT_GRAPH:
        return "ALT_GRAPH";
    }

    if (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9) {
      char c = (char) (keyCode - KeyEvent.VK_NUMPAD0 + '0');
      return "NUMPAD" + c;
    }

    return "unknown(0x" + Integer.toString(keyCode, 16) + ")";
  }

  public static void main(String[] args) {
//        TreePath got = showTreeNodeChooser(new JTree(), "Select A Node", null);
//        System.out.println(got);
//        System.exit(0);
    QuickFrame f = new QuickFrame("QuickFrame");
    f.setSize(100, 200);
//        locateCenter(f);
    f.setVisible(true);
  //  dumpUIDefaults();


  }
  // Added by GBH
  // for Positioning Components on Desktop

  public static Dimension sizeFrameForDefaultScreen(Dimension imageDim) {
    int xBuffer = 10;
    int yBuffer = 10;
    int boundaryH = 64;
    int boundaryW = 20;
    //Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
    Rectangle screen = defaultScreen.getDefaultConfiguration().getBounds();
    int maxH = screen.height - ((2 * yBuffer) + boundaryH);
    int maxW = screen.width - ((2 * xBuffer) + boundaryW);
    Dimension frameSize = new Dimension();
    int adjustedW = (imageDim.width < maxW) ? imageDim.width : maxW;
    // but width not less than... 480;
    adjustedW = (adjustedW < 480) ? 480 : adjustedW;
    int adjustedH = (int) (((float) imageDim.height / (float) imageDim.width) * adjustedW);
    if (adjustedH > maxH) {
      adjustedH = maxH;
      adjustedW = (int) (((float) imageDim.width / (float) imageDim.height) * adjustedH);
    }
    frameSize.width = adjustedW + boundaryW;
    frameSize.height = adjustedH + boundaryH;
    return frameSize;
  }
  //
  //-------------------------------------------------------------
  //
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
    lastFramePosition.x = lastFramePosition.x + 5;
    lastFramePosition.y = lastFramePosition.y + 5;
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
