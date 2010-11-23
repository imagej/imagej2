/*
 * IjxAbstractApplication
 * IjX
 * @author GBH
 */
package ijx.app;

import ijx.Macro;
import ijx.Prefs;
import ijx.RecentOpener;
import ijx.MenusAWT;
import ijx.WindowManager;
import ijx.ImageJApplet;
import ijx.IJ;
import ijx.Executer;
import ijx.IjxMenus;

import ijx.gui.dialog.GenericDialog;
import ijx.gui.IjxWindow;
import ijx.roi.Roi;
import ijx.roi.TextRoi;
import ijx.macro.Interpreter;
import ijx.plugin.MacroInstaller;
import ijx.plugin.api.PlugInFilterRunner;
import ijx.plugin.frame.ContrastAdjuster;
import ijx.plugin.frame.ThresholdAdjuster;
import ijx.text.TextWindow;
import ijx.CentralLookup;
import ijx.IjxImagePlus;
import ijx.IjxTopComponent;
import implementation.swing.MenusSwing;
import ijx.gui.IjxImageWindow;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuComponent;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author GBH
 */
public class IjxAbstractApplication implements IjxApplication {
    /** SansSerif, 12-point, plain font. */
    protected static int port = DEFAULT_PORT;
    protected static String[] arguments;
    private boolean firstTime = true;
    protected ImageJApplet applet; // null if not running as an applet
    protected Vector classes = new Vector();
    protected boolean exitWhenQuitting;
    protected boolean quitting;
    protected long actionPerformedTime;
    //boolean hotkey;
    protected IjxTopComponent topComponent;
    protected boolean embedded;
    protected static String iconPath;
    protected static boolean prefsLoaded;
    private static IjxImagePlus clipboard;
    public static final String TITLE = "ImageJX";
    public static final String VERSION = "0.10a";
    public static final String BUILD = "";

    /** Returns the internal clipboard or null if the internal clipboard is empty. */
    public IjxImagePlus getClipboard() {
        return clipboard;
    }

    public void setClipboard(IjxImagePlus imp) {
        clipboard = imp;
    }

    /** Clears the internal clipboard. */
    public void resetClipboard() {
        clipboard = null;
    }
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
//			IJ.runPlugIn("ijx.plugin.DragAndDrop", "");
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
        new Executer(name).run();
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
        IjxMenus menus = CentralLookup.getDefault().lookup(IjxMenus.class);
        if (menus instanceof MenusAWT) {
            MenuItem item = (MenuItem) e.getSource();
            cmd = e.getActionCommand();
            if (item.getParent() == menus.getOpenRecentMenu()) {
                new RecentOpener(cmd); // open image in separate thread
                return;
            }
        } else if (menus instanceof MenusSwing) {
            JMenuItem jItem = (JMenuItem) e.getSource();
            cmd = e.getActionCommand();
            if (jItem.getParent() == menus.getOpenRecentMenu()) {
                new RecentOpener(cmd); // open image in separate thread
                return;
            }
        }
        KeyboardHandler keyHandler = CentralLookup.getDefault().lookup(KeyboardHandler.class);
        keyHandler.setHotkey(false);
        actionPerformedTime = System.currentTimeMillis();
        long ellapsedTime = actionPerformedTime
                - CentralLookup.getDefault().lookup(KeyboardHandler.class).getKeyPressedTime();
        if (cmd != null && (ellapsedTime >= 200L || !cmd.equals(keyHandler.getLastKeyCommand()))) {
            doCommand(cmd);
        }
        keyHandler.setLastKeyCommand(null);
        if (IJ.debugMode) {
            IJ.log("actionPerformed: time=" + ellapsedTime + ", " + e);
        }
    }

    /** Handles CheckboxMenuItem state changes. */
    public void itemStateChanged(ItemEvent e) {
        IjxMenus menus = CentralLookup.getDefault().lookup(IjxMenus.class);
        if (menus instanceof MenusAWT) {
            MenuItem item = (MenuItem) e.getSource();
            MenuComponent parent = (MenuComponent) item.getParent();
            String cmd = e.getItem().toString();
            if ((Menu) parent == menus.getWindowMenu()) {
                WindowManager.activateWindow(cmd, item);
            } else {
                doCommand(cmd);
            }
        }
        /*else if (menus instanceof MenusSwing) {
            JMenuItem item = (JMenuItem) e.getSource();
            // if the source of event is the Window menu,
            Object o = item.getParent();
            JComponent parent = (JComponent) item.getParent();
            String cmd = item.getActionCommand();
            // ClassCastException: javax.swing.JPopupMenu cannot be cast to javax.swing.JMenu
            Object winmenu = menus.getWindowMenu();
            if ((JPopupMenu)parent == (JPopupMenu) menus.getWindowMenu()) {
                WindowManager.activateWindow(cmd, item);
            } else {
                doCommand(cmd);
            }
        }
    */

    }

    public void abortPluginOrMacro(IjxImagePlus imp) {
        if (imp != null) {
            IjxImageWindow win = imp.getWindow();
            if (win != null) {
                win.setRunning(false);
                win.setRunning2(false);
            }
        }
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
            for (int i = 0; i
                    < nArgs; i++) {
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
                } else if (arg.indexOf("ijx.ImageJ") == -1 && !arg.startsWith("-")) {
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
    public int getPort() {
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
            for (int i = 0; i
                    < wList.length; i++) {
                IjxImagePlus imp = (IjxImagePlus) WindowManager.getImage(wList[i]);
                if (imp != null && imp.isChanged() == true) {
                    changes = true;
                    break;

                }
            }
        }
        IjxMenus menus = CentralLookup.getDefault().lookup(IjxMenus.class);
        // @todo - AWT implementation
        if (menus instanceof MenusSwing) {
            if (IJ.getTopComponent().isClosed() && !changes
                    && ((JMenu) menus.getWindowMenu()).getItemCount() > IjxMenus.WINDOW_MENU_ITEMS
                    && !(IJ.macroRunning() && WindowManager.getImageCount() == 0)) {
                GenericDialog gd = new GenericDialog("ImageJ", IJ.getTopComponentFrame());
                gd.addMessage("Are you sure you want to quit ImageJ?");
                gd.showDialog();
                quitting =
                        !gd.wasCanceled();
                // windowClosed = false;  // ????????
            }
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

    // move this into WindowManager ??
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

    public ImageIcon getImageIcon() {
        URL url = this.getClass().getResource("/microscope.gif");
        if (url == null) {
            return null;
        }
        ImageIcon img = new ImageIcon(url);
        //createImage((ImageProducer)url.getContent());
        if (img != null) {
            return img;
        }
        return null;
    }

    // @todo Move to ImageUtils, or some such.
    public Image createCompatibleImage(int w, int h) {
        GraphicsEnvironment environment =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = environment.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();
        // Create an image that does not support transparency (Opaque)
        return config.createCompatibleImage(w, h);
        //Transparency.OPAQUE);
    }

    public String getVersion() {
        return VERSION;
    }

    public String getTitle() {
        return TITLE;
    }

    // @todo getApplet - implement this.
    public ImageJApplet getApplet() {
        return null;
    }
    //
    private static boolean compatibilityMode = false;

    public static boolean isCompatibilityMode() {
        return compatibilityMode;
    }

    public static void setCompatibilityMode(boolean compatibilityMode) {
        IjxAbstractApplication.compatibilityMode = compatibilityMode;
    }
    //
}
