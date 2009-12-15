/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx;

import ij.*;
import ij.gui.GenericDialog;
import ijx.gui.IjxWindow;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.macro.Interpreter;
import ij.plugin.MacroInstaller;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.frame.ContrastAdjuster;
import ij.plugin.frame.ThresholdAdjuster;
import ij.text.TextWindow;
import ij.util.Tools;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Menu;
import java.awt.MenuComponent;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JMenuItem;

/**
 *
 * @author GBH
 */
public class IjxAbstractApplication implements IjxApplication {

  /** SansSerif, 12-point, plain font. */
  protected static int port = DEFAULT_PORT;
  protected static String[] arguments;
  private boolean firstTime = true;
  protected java.applet.Applet applet; // null if not running as an applet
  private Vector classes = new Vector();
  private boolean exitWhenQuitting;
  private boolean quitting;
  private long keyPressedTime,  actionPerformedTime;
  private String lastKeyCommand;
  boolean hotkey;
  protected IjxTopComponent topComponent;
  protected boolean embedded;

//	/** Creates a new ImageJ frame that runs as an application. */
//	public ImageJ() {
//		this(null, STANDALONE);
//	}
//	
//	/** Creates a new ImageJ frame that runs as an applet. */
//	public ImageJ(java.applet.Applet applet) {
//		this(applet, 0);
//	}
//
//	/** If 'applet' is not null, creates a new ImageJ frame that runs as an applet.
//		If  'mode' is ImageJ.EMBEDDED and 'applet is null, creates an embedded 
//		version of ImageJ which does not start the SocketListener. */
//	public ImageJ(java.applet.Applet applet, int mode) {
//		//super("ImageJ");
//		embedded = applet==null && mode==EMBEDDED;
//		this.applet = applet;
//		String err1 = Prefs.load(this, applet);
//        
//        //TopComponentAWT topComponent = new TopComponentAWT(this);
//        IjxTopComponent topComponent = new TopComponentDesktop(this);
//        //TopComponentSwing topComponent = new TopComponentSwing(this);
//		if (IJ.isLinux()) {
//			Color backgroundColor = new Color(240,240,240);
//			topComponent.setBackground(backgroundColor);
//		}
//		Menus m = new Menus(topComponent, this, applet);
//		String err2 = m.addMenuBar();
//		m.installPopupMenu(this);
//
//		IJ.init(this, topComponent, applet);
//        IJ.setFactory(new AWTFactory()); // <<== set the Factory, GBH
// 		//addKeyListener(this);
//        this.addKeyListener(this);
//        topComponent.finishAndShow();
//		if (err1!=null)
//			IJ.error(err1);
//		if (err2!=null)
//			IJ.error(err2);
//		if (IJ.isMacintosh()&&applet==null) { 
//			Object qh = null; 
//			if (IJ.isJava14()) 
//				qh = IJ.runPlugIn("MacAdapter", ""); 
//			if (qh==null) 
//				IJ.runPlugIn("QuitHandler", ""); 
//		} 
//		if (applet==null)
//			IJ.runPlugIn("ij.plugin.DragAndDrop", "");
//		m.installStartupMacroSet();
//		String str = m.nMacros==1?" macro)":" macros)";
//		String java = "Java "+System.getProperty("java.version");
//		IJ.showStatus("ImageJ "+VERSION + "/"+java+" ("+ m.nPlugins + " commands, " + m.nMacros + str);
//		if (applet==null && !embedded)
//			new SocketListener();
//		configureProxy();
// 	}
  public void configureProxy() {
    String server = Prefs.get("proxy.server", null);
    if (server == null || server.equals("")) {
      return;
    }
    int port = (int) Prefs.get("proxy.port", 0);
    if (port == 0) {
      return;
    }
    String user = Prefs.get("proxy.user", null);
    Properties props = System.getProperties();
    props.put("proxySet", "true");
    props.put("http.proxyHost", server);
    props.put("http.proxyPort", "" + port);
    if (user != null) {
      props.put("http.proxyUser", user);
    }
  //IJ.log(server+"  "+port+"  "+user);
  }

  /** Starts executing a menu command in a separate thread. */
  public void doCommand(String name) {
    new Executer(name, null);
  }

  public void runFilterPlugIn(Object theFilter, String cmd, String arg) {
    new PlugInFilterRunner(theFilter, cmd, arg);
  }

  public Object runUserPlugIn(String commandName, String className, String arg, boolean createNewLoader) {
    return IJ.runUserPlugIn(commandName, className, arg, createNewLoader);
  }

  /** Return the current list of modifier keys. */
  public static String modifiers(int flags) { //?? needs to be moved
    String s = " [ ";
    if (flags == 0) {
      return "";
    }
    if ((flags & Event.SHIFT_MASK) != 0) {
      s += "Shift ";
    }
    if ((flags & Event.CTRL_MASK) != 0) {
      s += "Control ";
    }
    if ((flags & Event.META_MASK) != 0) {
      s += "Meta ";
    }
    if ((flags & Event.ALT_MASK) != 0) {
      s += "Alt ";
    }
    s += "] ";
    return s;
  }

  /** Handle menu events. */
  public void actionPerformed(ActionEvent e) {
    String cmd = null;
    if ((e.getSource() instanceof MenuItem)) {
      MenuItem item = (MenuItem) e.getSource();
      cmd = e.getActionCommand();
      if (item.getParent() == Menus.openRecentMenu) {
        new RecentOpener(cmd); // open image in separate thread
        return;
      }
    } else if ((e.getSource() instanceof JMenuItem)) { // IjX: Swing-version
      JMenuItem jItem = (JMenuItem) e.getSource();
      cmd = e.getActionCommand();
      if (jItem.getParent() == MenusIjx.openRecentMenu) {
        new RecentOpener(cmd); // open image in separate thread
        return;
      }
    }
    hotkey = false;
    actionPerformedTime = System.currentTimeMillis();
    long ellapsedTime = actionPerformedTime - keyPressedTime;
    if (cmd != null && (ellapsedTime >= 200L || !cmd.equals(lastKeyCommand))) {
      doCommand(cmd);
    }
    lastKeyCommand = null;
    if (IJ.debugMode) {
      IJ.log("actionPerformed: time=" + ellapsedTime + ", " + e);
    }
  }

  /** Handles CheckboxMenuItem state changes. */
  public void itemStateChanged(ItemEvent e) {
    MenuItem item = (MenuItem) e.getSource();
    MenuComponent parent = (MenuComponent) item.getParent();
    String cmd = e.getItem().toString();
    if ((Menu) parent == Menus.window) {
      WindowManager.activateWindow(cmd, item);
    } else {
      doCommand(cmd);
    }

  }

  public void keyPressed(KeyEvent e) {
    int keyCode = e.getKeyCode();
    IJ.setKeyDown(keyCode);
    hotkey = false;
    if (keyCode == e.VK_CONTROL || keyCode == e.VK_SHIFT) {
      return;
    }

    char keyChar = e.getKeyChar();
    int flags = e.getModifiers();
    if (IJ.debugMode) {
      IJ.log("keyPressed: code=" + keyCode + " (" + KeyEvent.getKeyText(keyCode) + "), char=\"" +
              keyChar + "\" (" + (int) keyChar + "), flags=" + KeyEvent.getKeyModifiersText(flags));
    }

    boolean shift = (flags & e.SHIFT_MASK) != 0;
    boolean control = (flags & e.CTRL_MASK) != 0;
    boolean alt = (flags & e.ALT_MASK) != 0;
    boolean meta = (flags & e.META_MASK) != 0;
    String cmd = "";
    ImagePlus imp = WindowManager.getCurrentImage();
    boolean isStack = (imp != null) && (imp.getStackSize() > 1);

    if (imp != null && !control && ((keyChar >= 32 && keyChar <= 255) || keyChar == '\b' || keyChar == '\n')) {
      Roi roi = imp.getRoi();
      if (roi instanceof TextRoi) {
        if ((flags & e.META_MASK) != 0 && IJ.isMacOSX()) {
          return;
        }

        if (alt) {
          switch (keyChar) {
            case 'u':
            case 'm':
              keyChar = IJ.micronSymbol;
              break;

            case 'A':
              keyChar = IJ.angstromSymbol;
              break;

            default:

          }
        }
        ((TextRoi) roi).addChar(keyChar);
        return;

      }




    }

    // Handle one character macro shortcuts
    if (!control && !meta) {
      Hashtable macroShortcuts = Menus.getMacroShortcuts();
      if (macroShortcuts.size() > 0) {
        if (shift) {
          cmd = (String) macroShortcuts.get(new Integer(keyCode + 200));
        } else {
          cmd = (String) macroShortcuts.get(new Integer(keyCode));
        }

        if (cmd != null) {
          //MacroInstaller.runMacroCommand(cmd);
          MacroInstaller.runMacroShortcut(cmd);
          return;

        }




      }
    }

    if (!Prefs.requireControlKey || control || meta) {
      Hashtable shortcuts = Menus.getShortcuts();
      if (shift) {
        cmd = (String) shortcuts.get(new Integer(keyCode + 200));
      } else {
        cmd = (String) shortcuts.get(new Integer(keyCode));
      }

    }

    if (cmd == null) {
      switch (keyChar) {
        case '<':
          cmd = "Previous Slice [<]";
          break;

        case '>':
          cmd = "Next Slice [>]";
          break;

        case '+':
        case '=':
          cmd = "In";
          break;

        case '-':
          cmd = "Out";
          break;

        case '/':
          cmd = "Reslice [/]...";
          break;

        default:

      }
    }

    if (cmd == null) {
      switch (keyCode) {
        case KeyEvent.VK_TAB:
          WindowManager.putBehind();
          return;

        case KeyEvent.VK_BACK_SPACE:
          cmd = "Clear";
          hotkey =
                  true;
          break; // delete
//case KeyEvent.VK_BACK_SLASH: cmd=IJ.altKeyDown()?"Animation Options...":"Start Animation"; break;

        case KeyEvent.VK_EQUALS:
          cmd = "In";
          break;

        case KeyEvent.VK_MINUS:
          cmd = "Out";
          break;

        case KeyEvent.VK_SLASH:
        case 0xbf:
          cmd = "Reslice [/]...";
          break;

        case KeyEvent.VK_COMMA:
        case 0xbc:
          cmd = "Previous Slice [<]";
          break;

        case KeyEvent.VK_PERIOD:
        case 0xbe:
          cmd = "Next Slice [>]";
          break;

        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_UP:
        case KeyEvent.VK_DOWN: // arrow keys
          Roi roi = null;
          if (imp != null) {
            roi = imp.getRoi();
          }

          if (roi == null) {
            return;
          }

          if ((flags & KeyEvent.ALT_MASK) != 0) {
            roi.nudgeCorner(keyCode);
          } else {
            roi.nudge(keyCode);
          }

          return;
        case KeyEvent.VK_ESCAPE:
          abortPluginOrMacro(imp);
          return;

        case KeyEvent.VK_ENTER:
          IJ.getTopComponentFrame().toFront();
          return;

        default:

          break;
      }

    }

    if (cmd != null && !cmd.equals("")) {
      if (cmd.equals("Fill")) {
        hotkey = true;
      }

      if (cmd.charAt(0) == MacroInstaller.commandPrefix) {
        MacroInstaller.runMacroShortcut(cmd);
      } else {
        doCommand(cmd);
        keyPressedTime =
                System.currentTimeMillis();
        lastKeyCommand =
                cmd;
      }

    }
  }

  public void keyTyped(KeyEvent e) {
    char keyChar = e.getKeyChar();
    int flags = e.getModifiers();
    if (IJ.debugMode) {
      IJ.log("keyTyped: char=\"" + keyChar + "\" (" + (int) keyChar + "), flags= " + Integer.toHexString(
              flags) + " (" + KeyEvent.getKeyModifiersText(flags) + ")");
    }

    if (keyChar == '\\' || keyChar == 171 || keyChar == 223) {
      if (((flags & Event.ALT_MASK) != 0)) {
        doCommand("Animation Options...");
      } else {
        doCommand("Start Animation [\\]");
      }

    }
  }

  public void keyReleased(KeyEvent e) {
    IJ.setKeyUp(e.getKeyCode());
  }

  public boolean isHotkey() {
    return hotkey;
  }

  public void abortPluginOrMacro(ImagePlus imp) {
//		if (imp!=null) {
//			IjxWindow win = imp.getWindow();
//			if (win!=null) {
//                if(win instanceof PluginFrame )
//				win.running = false;
//				win.running2 = false;
//			}
//		}
    Macro.abort();
    Interpreter.abort();
    if (Interpreter.getInstance() != null) {
      IJ.beep();
    }

  }

  /** Adds the specified class to a Vector to keep it from being
  garbage collected, causing static fields to be reset. */
  public void register(Class c) {
    if (!classes.contains(c)) {
      classes.addElement(c);
    }

  }

  /** Called by ImageJ when the user selects Quit. */
  public void quit() {
    Thread thread = new Thread(this, "Quit");
    thread.setPriority(Thread.NORM_PRIORITY);
    thread.start();
  }

  /** Returns true if ImageJ is exiting. */
  public boolean quitting() {
    return quitting;
  }

  /** Called once when ImageJ quits. */
  public void savePreferences(Properties prefs) {
    Point loc = IJ.getTopComponentFrame().getLocation();
    prefs.put(IjxTopComponent.IJ_X, Integer.toString(loc.x));
    prefs.put(IjxTopComponent.IJ_Y, Integer.toString(loc.y));
  //prefs.put(IJ_WIDTH, Integer.toString(size.width));
  //prefs.put(IJ_HEIGHT, Integer.toString(size.height));
  }

// Is there another instance of ImageJ? If so, send it the arguments and quit.
  public static boolean isRunning(String args[]) {
    int macros = 0;
    int nArgs = args.length;
    if (nArgs == 2 && args[0].startsWith("-ijpath")) {
      return false;
    }

    int nCommands = 0;
    try {
      sendArgument("user.dir " + System.getProperty("user.dir"));
      for (int i = 0; i <
              nArgs; i++) {
        String arg = args[i];
        if (arg == null) {
          continue;
        }

        String cmd = null;
        if (macros == 0 && arg.endsWith(".ijm")) {
          cmd = "macro " + arg;
          macros++;

        } else if (arg.startsWith("-macro") && i + 1 < nArgs) {
          String macroArg = i + 2 < nArgs ? "(" + args[i + 2] + ")" : "";
          cmd =
                  "macro " + args[i + 1] + macroArg;
          sendArgument(cmd);
          nCommands++;

          break;

        } else if (arg.startsWith("-eval") && i + 1 < nArgs) {
          cmd = "eval " + args[i + 1];
          args[i + 1] = null;
        } else if (arg.startsWith("-run") && i + 1 < nArgs) {
          cmd = "run " + args[i + 1];
          args[i + 1] = null;
        } else if (arg.indexOf("ij.ImageJ") == -1 && !arg.startsWith("-")) {
          cmd = "open " + arg;
        }

        if (cmd != null) {
          sendArgument(cmd);
          nCommands++;

        }




      } // for
    } catch (IOException e) {
      return false;
    }

    return true;
  }

  static void sendArgument(String arg) throws IOException {
    Socket socket = new Socket("localhost", port);
    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    out.println(arg);
    out.close();
    socket.close();
  }

  /**
  Returns the port that ImageJ checks on startup to see if another instance is running.
  @see ij.SocketListener
   */
  public static int getPort() {
    return port;
  }

  /** Returns the command line arguments passed to ImageJ. */
  public static String[] getArgs() {
    return arguments;
  }

  /** ImageJ calls System.exit() when qutting when 'exitWhenQuitting' is true.*/
  public void exitWhenQuitting(boolean ewq) {
    exitWhenQuitting = ewq;
  }

  /** Quit using a separate thread, hopefully avoiding thread deadlocks. */
  public void run() {
    quitting = true;
    boolean changes = false;
    int[] wList = WindowManager.getIDList();
    if (wList != null) {
      for (int i = 0; i <
              wList.length; i++) {
        ImagePlus imp = (ImagePlus) WindowManager.getImage(wList[i]);
        if (imp != null && imp.wasChanged() == true) {
          changes = true;
          break;

        }
      }
    }
    if (IJ.getTopComponent().isClosed() && !changes &&
            Menus.window.getItemCount() > Menus.WINDOW_MENU_ITEMS &&
            !(IJ.macroRunning() && WindowManager.getImageCount() == 0)) {
      GenericDialog gd = new GenericDialog("ImageJ", IJ.getTopComponentFrame());
      gd.addMessage("Are you sure you want to quit ImageJ?");
      gd.showDialog();
      quitting =
              !gd.wasCanceled();
    // windowClosed = false;  // ????????
    }

    if (!quitting) {
      return;
    }

    if (!WindowManager.closeAllWindows()) {
      quitting = false;
      return;

    }
    //IJ.log("savePreferences");

    if (applet == null) {
      saveWindowLocations();
      Prefs.savePreferences();
    }

    IJ.getTopComponentFrame().setVisible(false);
    //IJ.log("dispose");
    IJ.getTopComponentFrame().dispose();
    if (exitWhenQuitting) {
      System.exit(0);
    }

  }

  public void saveWindowLocations() {
    IjxWindow frame = WindowManager.getFrame("B&C");
    if (frame != null) {
      Prefs.saveLocation(ContrastAdjuster.LOC_KEY, frame.getLocation());
    }

    frame = WindowManager.getFrame("Threshold");
    if (frame != null) {
      Prefs.saveLocation(ThresholdAdjuster.LOC_KEY, frame.getLocation());
    }

    frame = WindowManager.getFrame("Results");
    if (frame != null) {
      Prefs.saveLocation(TextWindow.LOC_KEY, frame.getLocation());
      Dimension d = frame.getSize();
      Prefs.set(TextWindow.WIDTH_KEY, d.width);
      Prefs.set(TextWindow.HEIGHT_KEY, d.height);
    }
  }
}
