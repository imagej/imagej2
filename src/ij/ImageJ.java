package ij;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.awt.image.*;
import ij.gui.*;
import ij.process.*;
import ij.io.*;
import ij.plugin.*;
import ij.plugin.filter.*;
import ij.plugin.frame.*;
import ij.text.*;
import ij.macro.Interpreter;
import ij.io.Opener;
import ij.util.*;

/**
This frame is the main ImageJ class.
<p>
ImageJ is a work of the United States Government. It is in the public domain 
and open source. There is no copyright. You are free to do anything you want 
with this source but I like to get credit for my work and I would like you to 
offer your changes to me so I can possibly add them to the "official" version.

<pre>
The following command line options are recognized by ImageJ:

  "file-name"
     Opens a file
     Example 1: blobs.tif
     Example 2: /Users/wayne/images/blobs.tif
     Example3: e81*.tif

  -ijpath path
     Specifies the path to the directory containing the plugins directory
     Example: -ijpath /Applications/ImageJ

  -port<n>
     Specifies the port ImageJ uses to determine if another instance is running
     Example 1: -port1 (use default port address + 1)
     Example 2: -port2 (use default port address + 2)
     Example 3: -port0 (do not check for another instance)

  -macro path [arg]
     Runs a macro or script, passing it an optional argument,
     which can be retieved using getArgument()
     Example 1: -macro analyze.ijm
     Example 2: -macro analyze /Users/wayne/images/stack1

  -batch path [arg]
    Runs a macro or script in batch (no GUI) mode, passing it an optional argument.
    ImageJ exits when the macro finishes.

  -eval "macro code"
     Evaluates macro code
     Example 1: -eval "print('Hello, world');"
     Example 2: -eval "return getVersion();"

  -run command
     Runs an ImageJ menu command
     Example: -run "About ImageJ..."
</pre>
@author Wayne Rasband (wsr@nih.gov)
*/
public class ImageJ extends Frame implements ActionListener, 
	MouseListener, KeyListener, WindowListener, ItemListener, Runnable {

	/** Plugins should call IJ.getVersion() to get the version string. */
	public static final String VERSION = "1.43o";
	public static final String BUILD = "7";
	public static Color backgroundColor = new Color(220,220,220); //224,226,235
	/** SansSerif, 12-point, plain font. */
	public static final Font SansSerif12 = new Font("SansSerif", Font.PLAIN, 12);
	/** Address of socket where Image accepts commands */
	public static final int DEFAULT_PORT = 57294;
	public static final int STANDALONE=0, EMBEDDED=1;

	private static final String IJ_X="ij.x",IJ_Y="ij.y";
	private static int port = DEFAULT_PORT;
	private static String[] arguments;
	
	private Toolbar toolbar;
	private Panel statusBar;
	private ProgressBar progressBar;
	private Label statusLine;
	private boolean firstTime = true;
	private java.applet.Applet applet; // null if not running as an applet
	private Vector classes = new Vector();
	private boolean exitWhenQuitting;
	private boolean quitting;
	private long keyPressedTime, actionPerformedTime;
	private String lastKeyCommand;
	private boolean embedded;
	private boolean windowClosed;
	
	boolean hotkey;
	
	/** Creates a new ImageJ frame that runs as an application. */
	public ImageJ() {
		this(null, STANDALONE);
	}
	
	/** Creates a new ImageJ frame that runs as an applet. */
	public ImageJ(java.applet.Applet applet) {
		this(applet, 0);
	}

	/** If 'applet' is not null, creates a new ImageJ frame that runs as an applet.
		If  'mode' is ImageJ.EMBEDDED and 'applet is null, creates an embedded 
		version of ImageJ which does not start the SocketListener. */
	public ImageJ(java.applet.Applet applet, int mode) {
		super("ImageJ");
		embedded = applet==null && mode==EMBEDDED;
		this.applet = applet;
		String err1 = Prefs.load(this, applet);
		if (IJ.isLinux()) {
			backgroundColor = new Color(240,240,240);
			setBackground(backgroundColor);
		}
		Menus m = new Menus(this, applet);
		String err2 = m.addMenuBar();
		m.installPopupMenu(this);
		setLayout(new GridLayout(2, 1));
		
		// Tool bar
		toolbar = new Toolbar();
		toolbar.addKeyListener(this);
		add(toolbar);

		// Status bar
		statusBar = new Panel();
		statusBar.setLayout(new BorderLayout());
		statusBar.setForeground(Color.black);
		statusBar.setBackground(backgroundColor);
		statusLine = new Label();
		statusLine.setFont(SansSerif12);
		statusLine.addKeyListener(this);
		statusLine.addMouseListener(this);
		statusBar.add("Center", statusLine);
		progressBar = new ProgressBar(120, 20);
		progressBar.addKeyListener(this);
		progressBar.addMouseListener(this);
		statusBar.add("East", progressBar);
		statusBar.setSize(toolbar.getPreferredSize());
		add(statusBar);

		IJ.init(this, applet);
 		addKeyListener(this);
 		addWindowListener(this);
		setFocusTraversalKeysEnabled(false);
 		
		Point loc = getPreferredLocation();
		Dimension tbSize = toolbar.getPreferredSize();
		int ijWidth = tbSize.width+10;
		int ijHeight = 100;
		setCursor(Cursor.getDefaultCursor()); // work-around for JDK 1.1.8 bug
		if (IJ.isWindows()) try {setIcon();} catch(Exception e) {}
		setBounds(loc.x, loc.y, ijWidth, ijHeight); // needed for pack to work
		setLocation(loc.x, loc.y);
		pack();
		setResizable(!(IJ.isMacintosh() || IJ.isWindows())); // make resizable on Linux
		//if (IJ.isJava15()) {
		//	try {
		//		Method method = Frame.class.getMethod("setAlwaysOnTop", new Class[] {boolean.class});
		//		method.invoke(this, new Object[]{Boolean.TRUE});
		//	} catch(Exception e) {}
		//}
		show();
		if (err1!=null)
			IJ.error(err1);
		if (err2!=null) {
			IJ.error(err2);
			IJ.runPlugIn("ij.plugin.ClassChecker", "");
		}
		if (IJ.isMacintosh()&&applet==null) { 
			Object qh = null; 
			qh = IJ.runPlugIn("MacAdapter", ""); 
			if (qh==null) 
				IJ.runPlugIn("QuitHandler", ""); 
		} 
		if (applet==null)
			IJ.runPlugIn("ij.plugin.DragAndDrop", "");
		m.installStartupMacroSet();
		String str = m.getMacroCount()==1?" macro":" macros";
		IJ.showStatus(version()+ m.getPluginCount() + " commands; " + m.getMacroCount() + str);
		if (applet==null && !embedded && Prefs.runSocketListener)
			new SocketListener();
		configureProxy();
 	}
    	
	void configureProxy() {
		String server = Prefs.get("proxy.server", null);
		if (server==null||server.equals("")) return;
		int port = (int)Prefs.get("proxy.port", 0);
		if (port==0) return;
		String user = Prefs.get("proxy.user", null);	
		Properties props = System.getProperties();
		props.put("proxySet", "true");
		props.put("http.proxyHost", server);
		props.put("http.proxyPort", ""+port);
		if (user!=null)
			props.put("http.proxyUser", user);
		//IJ.log(server+"  "+port+"  "+user);
	}
	
    void setIcon() throws Exception {
		URL url = this.getClass().getResource("/microscope.gif");
		if (url==null) return;
		Image img = createImage((ImageProducer)url.getContent());
		if (img!=null) setIconImage(img);
	}
	
	public Point getPreferredLocation() {
		if (!IJ.isJava14()) return new Point(0, 0);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle maxBounds = ge.getMaximumWindowBounds();
		int ijX = Prefs.getInt(IJ_X,-99);
		int ijY = Prefs.getInt(IJ_Y,-99);
		if (ijX>=0 && ijY>0 && ijX<(maxBounds.x+maxBounds.width-75))
			return new Point(ijX, ijY);
		Dimension tbsize = toolbar.getPreferredSize();
		int ijWidth = tbsize.width+10;
		double percent = maxBounds.width>832?0.8:0.9;
		ijX = (int)(percent*(maxBounds.width-ijWidth));
		if (ijX<10) ijX = 10;
		return new Point(ijX, maxBounds.y);
	}
	
	void showStatus(String s) {
        statusLine.setText(s);
	}

	public ProgressBar getProgressBar() {
        return progressBar;
	}

	public Panel getStatusBar() {
        return statusBar;
	}

    /** Starts executing a menu command in a separate thread. */
    void doCommand(String name) {
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
		if (flags == 0) return "";
		if ((flags & Event.SHIFT_MASK) != 0) s += "Shift ";
		if ((flags & Event.CTRL_MASK) != 0) s += "Control ";
		if ((flags & Event.META_MASK) != 0) s += "Meta ";
		if ((flags & Event.ALT_MASK) != 0) s += "Alt ";
		s += "] ";
		return s;
	}

	/** Handle menu events. */
	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() instanceof MenuItem)) {
			MenuItem item = (MenuItem)e.getSource();
			String cmd = e.getActionCommand();
			if (item.getParent()==Menus.openRecentMenu) {
				new RecentOpener(cmd); // open image in separate thread
				return;
			}
			int flags = e.getModifiers();
			//IJ.log(""+KeyEvent.getKeyModifiersText(flags));
			hotkey = false;
			actionPerformedTime = System.currentTimeMillis();
			long ellapsedTime = actionPerformedTime-keyPressedTime;
			if (cmd!=null && (ellapsedTime>=200L||!cmd.equals(lastKeyCommand))) {
				if ((flags & Event.ALT_MASK)!=0)
					IJ.setKeyDown(KeyEvent.VK_ALT);
				if ((flags & Event.SHIFT_MASK)!=0)
					IJ.setKeyDown(KeyEvent.VK_SHIFT);
				doCommand(cmd);
			}
			lastKeyCommand = null;
			if (IJ.debugMode) IJ.log("actionPerformed: time="+ellapsedTime+", "+e);
		}
	}

	/** Handles CheckboxMenuItem state changes. */
	public void itemStateChanged(ItemEvent e) {
		MenuItem item = (MenuItem)e.getSource();
		MenuComponent parent = (MenuComponent)item.getParent();
		String cmd = e.getItem().toString();
		if ((Menu)parent==Menus.window)
			WindowManager.activateWindow(cmd, item);
		else
			doCommand(cmd);
	}

	public void mousePressed(MouseEvent e) {
		Undo.reset();
		System.gc();
		IJ.showStatus(version()+IJ.freeMemory());
		if (IJ.debugMode)
			IJ.log("Windows: "+WindowManager.getWindowCount());
	}
	
	private String version() {
		return "ImageJ "+VERSION+BUILD + "; "+"Java "+System.getProperty("java.version")+(IJ.is64Bit()?" [64-bit]; ":" [32-bit]; ");
	}
	
	public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}

 	public void keyPressed(KeyEvent e) {
 		//if (e.isConsumed()) return;
		int keyCode = e.getKeyCode();
		IJ.setKeyDown(keyCode);
		hotkey = false;
		if (keyCode==e.VK_CONTROL || keyCode==e.VK_SHIFT)
			return;
		char keyChar = e.getKeyChar();
		int flags = e.getModifiers();
		if (IJ.debugMode) IJ.log("keyPressed: code=" + keyCode + " (" + KeyEvent.getKeyText(keyCode)
			+ "), char=\"" + keyChar + "\" (" + (int)keyChar + "), flags="
			+ KeyEvent.getKeyModifiersText(flags));
		boolean shift = (flags & e.SHIFT_MASK) != 0;
		boolean control = (flags & e.CTRL_MASK) != 0;
		boolean alt = (flags & e.ALT_MASK) != 0;
		boolean meta = (flags & e.META_MASK) != 0;
		String cmd = "";
		ImagePlus imp = WindowManager.getCurrentImage();
		boolean isStack = (imp!=null) && (imp.getStackSize()>1);
		
		if (imp!=null && !control && ((keyChar>=32 && keyChar<=255) || keyChar=='\b' || keyChar=='\n')) {
			Roi roi = imp.getRoi();
			if (roi instanceof TextRoi) {
				if ((flags & e.META_MASK)!=0 && IJ.isMacOSX()) return;
				if (alt)
					switch (keyChar) {
						case 'u': case 'm': keyChar = IJ.micronSymbol; break;
						case 'A': keyChar = IJ.angstromSymbol; break;
						default:
					}
				((TextRoi)roi).addChar(keyChar);
				return;
			}
		}
        		
		// Handle one character macro shortcuts
		if (!control && !meta) {
			Hashtable macroShortcuts = Menus.getMacroShortcuts();
			if (macroShortcuts.size()>0) {
				if (shift)
					cmd = (String)macroShortcuts.get(new Integer(keyCode+200));
				else
					cmd = (String)macroShortcuts.get(new Integer(keyCode));
				if (cmd!=null) {
					//MacroInstaller.runMacroCommand(cmd);
					MacroInstaller.runMacroShortcut(cmd);
					return;
				}
			}
		}

		if (!Prefs.requireControlKey || control || meta) {
			Hashtable shortcuts = Menus.getShortcuts();
			if (shift)
				cmd = (String)shortcuts.get(new Integer(keyCode+200));
			else
				cmd = (String)shortcuts.get(new Integer(keyCode));
		}
		
		if (cmd==null) {
			switch (keyChar) {
				case '<': case ',': cmd="Previous Slice [<]"; break;
				case '>': case '.': case ';': cmd="Next Slice [>]"; break;
				case '+': case '=': cmd="In"; break;
				case '-': cmd="Out"; break;
				case '/': cmd="Reslice [/]..."; break;
				default:
			}
		}

		if (cmd==null) {
			switch(keyCode) {
				case KeyEvent.VK_TAB: WindowManager.putBehind(); return;
				case KeyEvent.VK_BACK_SPACE: cmd="Clear"; hotkey=true; break; // delete
				//case KeyEvent.VK_BACK_SLASH: cmd=IJ.altKeyDown()?"Animation Options...":"Start Animation"; break;
				case KeyEvent.VK_EQUALS: cmd="In"; break;
				case KeyEvent.VK_MINUS: cmd="Out"; break;
				case KeyEvent.VK_SLASH: case 0xbf: cmd="Reslice [/]..."; break;
				case KeyEvent.VK_COMMA: case 0xbc: cmd="Previous Slice [<]"; break;
				case KeyEvent.VK_PERIOD: case 0xbe: cmd="Next Slice [>]"; break;
				case KeyEvent.VK_LEFT: case KeyEvent.VK_RIGHT: case KeyEvent.VK_UP: case KeyEvent.VK_DOWN: // arrow keys
					if (imp==null) return;
					Roi roi = imp.getRoi();
					if (IJ.shiftKeyDown()&&imp==Orthogonal_Views.getImage())
						return;
					boolean stackKey = imp.getStackSize()>1 && (roi==null||IJ.shiftKeyDown());
					boolean zoomKey = roi==null || IJ.shiftKeyDown() || IJ.controlKeyDown();
					if (stackKey && keyCode==KeyEvent.VK_RIGHT)
							cmd="Next Slice [>]";
					else if (stackKey && keyCode==KeyEvent.VK_LEFT)
							cmd="Previous Slice [<]";
					else if (zoomKey &&keyCode==KeyEvent.VK_DOWN)
							cmd="Out";
					else if (zoomKey && keyCode==KeyEvent.VK_UP)
							cmd="In";
					else if (roi!=null) {
						if ((flags & KeyEvent.ALT_MASK) != 0)
							roi.nudgeCorner(keyCode);
						else
							roi.nudge(keyCode);
						return;
					}
					break;
				case KeyEvent.VK_ESCAPE:
					abortPluginOrMacro(imp);
					return;
				case KeyEvent.VK_ENTER: this.toFront(); return;
				default: break;
			}
		}
		
		if (cmd!=null && !cmd.equals("")) {
			if (cmd.equals("Fill")||cmd.equals("Draw"))
				hotkey = true;
			if (cmd.charAt(0)==MacroInstaller.commandPrefix)
				MacroInstaller.runMacroShortcut(cmd);
			else {
				doCommand(cmd);
				keyPressedTime = System.currentTimeMillis();
				lastKeyCommand = cmd;
			}
		}
	}
	
	public void keyTyped(KeyEvent e) {
		char keyChar = e.getKeyChar();
		int flags = e.getModifiers();
		if (IJ.debugMode) IJ.log("keyTyped: char=\"" + keyChar + "\" (" + (int)keyChar 
			+ "), flags= "+Integer.toHexString(flags)+ " ("+KeyEvent.getKeyModifiersText(flags)+")");
		if (keyChar=='\\' || keyChar==171 || keyChar==223) {
			if (((flags&Event.ALT_MASK)!=0))
				doCommand("Animation Options...");
			else
				doCommand("Start Animation [\\]");
		}
	}

	public void keyReleased(KeyEvent e) {
		IJ.setKeyUp(e.getKeyCode());
	}
		
	void abortPluginOrMacro(ImagePlus imp) {
		if (imp!=null) {
			ImageWindow win = imp.getWindow();
			if (win!=null) {
				win.running = false;
				win.running2 = false;
			}
		}
		Macro.abort();
		Interpreter.abort();
		if (Interpreter.getInstance()!=null) IJ.beep();
	}

	public void windowClosing(WindowEvent e) {
		doCommand("Quit");
		windowClosed = true;
	}

	public void windowActivated(WindowEvent e) {
		if (IJ.isMacintosh() && !quitting) {
			IJ.wait(10); // may be needed for Java 1.4 on OS X
			setMenuBar(Menus.getMenuBar());
		}
	}
	
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	
	/** Adds the specified class to a Vector to keep it from being
		garbage collected, causing static fields to be reset. */
	public void register(Class c) {
		if (!classes.contains(c))
			classes.addElement(c);
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
		Point loc = getLocation();
		prefs.put(IJ_X, Integer.toString(loc.x));
		prefs.put(IJ_Y, Integer.toString(loc.y));
		//prefs.put(IJ_WIDTH, Integer.toString(size.width));
		//prefs.put(IJ_HEIGHT, Integer.toString(size.height));
	}

	public static void main(String args[]) {
		if (System.getProperty("java.version").substring(0,3).compareTo("1.4")<0) {
			javax.swing.JOptionPane.showMessageDialog(null,"ImageJ "+VERSION+" requires Java 1.4.1 or later.");
			System.exit(0);
		}
		boolean noGUI = false;
		int mode = STANDALONE;
		arguments = args;
		int nArgs = args!=null?args.length:0;
		for (int i=0; i<nArgs; i++) {
			String arg = args[i];
			if (arg==null) continue;
			//IJ.log(i+"  "+arg);
			if (args[i].startsWith("-")) {
				if (args[i].startsWith("-batch"))
					noGUI = true;
				else if (args[i].startsWith("-debug"))
					IJ.debugMode = true;
				else if (args[i].startsWith("-ijpath") && i+1<nArgs) {
					Prefs.setHomeDir(args[i+1]);
					args[i+1] = null;
				} else if (args[i].startsWith("-port")) {
					int delta = (int)Tools.parseDouble(args[i].substring(5, args[i].length()), 0.0);
					if (delta==0)
						mode = EMBEDDED;
					else if (delta>0 && DEFAULT_PORT+delta<65536)
						port = DEFAULT_PORT+delta;
				}
			} 
		}
  		// If ImageJ is already running then isRunning()
  		// will pass the arguments to it using sockets.
		if (nArgs>0 && !noGUI && (mode==STANDALONE) && isRunning(args))
  				return;
 		ImageJ ij = IJ.getInstance();    	
		if (!noGUI && (ij==null || (ij!=null && !ij.isShowing()))) {
			ij = new ImageJ(null, mode);
			ij.exitWhenQuitting = true;
		}
		int macros = 0;
		for (int i=0; i<nArgs; i++) {
			String arg = args[i];
			if (arg==null) continue;
			if (arg.startsWith("-")) {
				if ((arg.startsWith("-macro") || arg.startsWith("-batch")) && i+1<nArgs) {
					String arg2 = i+2<nArgs?args[i+2]:null;
					Prefs.commandLineMacro = true;
					IJ.runMacroFile(args[i+1], arg2);
					break;
				} else if (arg.startsWith("-eval") && i+1<nArgs) {
					String rtn = IJ.runMacro(args[i+1]);
					if (rtn!=null)
						System.out.print(rtn);
					args[i+1] = null;
				} else if (arg.startsWith("-run") && i+1<nArgs) {
					IJ.run(args[i+1]);
					args[i+1] = null;
				}
			} else if (macros==0 && (arg.endsWith(".ijm") || arg.endsWith(".txt"))) {
				IJ.runMacroFile(arg);
				macros++;
			} else if (arg.indexOf("ij.ImageJ")==-1) {
				File file = new File(arg);
				IJ.open(file.getAbsolutePath());
			}
		}
		if (IJ.debugMode && IJ.getInstance()==null)
			new JavaProperties().run("");
		if (noGUI) System.exit(0);
	}
	
	// Is there another instance of ImageJ? If so, send it the arguments and quit.
	static boolean isRunning(String args[]) {
		int macros = 0;
		int nArgs = args.length;
		if (nArgs==2 && args[0].startsWith("-ijpath"))
			return false;
		int nCommands = 0;
		try {
			sendArgument("user.dir "+System.getProperty("user.dir"));
			for (int i=0; i<nArgs; i++) {
				String arg = args[i];
				if (arg==null) continue;
				String cmd = null;
				if (macros==0 && arg.endsWith(".ijm")) {
					cmd = "macro " + arg;
					macros++;
				} else if (arg.startsWith("-macro") && i+1<nArgs) {
					String macroArg = i+2<nArgs?"("+args[i+2]+")":"";
					cmd = "macro " + args[i+1] + macroArg;
					sendArgument(cmd);
					nCommands++;
					break;
				} else if (arg.startsWith("-eval") && i+1<nArgs) {
					cmd = "eval " + args[i+1];
					args[i+1] = null;
				} else if (arg.startsWith("-run") && i+1<nArgs) {
					cmd = "run " + args[i+1];
					args[i+1] = null;
				} else if (arg.indexOf("ij.ImageJ")==-1 && !arg.startsWith("-"))
					cmd = "open " + arg;
				if (cmd!=null) {
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
		PrintWriter out = new PrintWriter (new OutputStreamWriter(socket.getOutputStream()));
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
		if (wList!=null) {
			for (int i=0; i<wList.length; i++) {
				ImagePlus imp = WindowManager.getImage(wList[i]);
				if (imp!=null && imp.changes==true) {
					changes = true;
					break;
				}
			}
		}
		if (windowClosed && !changes && Menus.window.getItemCount()>Menus.WINDOW_MENU_ITEMS && !(IJ.macroRunning()&&WindowManager.getImageCount()==0)) {
			GenericDialog gd = new GenericDialog("ImageJ", this);
			gd.addMessage("Are you sure you want to quit ImageJ?");
			gd.showDialog();
			quitting = !gd.wasCanceled();
			windowClosed = false;
		}
		if (!quitting)
			return;
		if (!WindowManager.closeAllWindows()) {
			quitting = false;
			return;
		}
		//IJ.log("savePreferences");
		if (applet==null) {
			saveWindowLocations();
			Prefs.savePreferences();
		}
		setVisible(false);
		//IJ.log("dispose");
		dispose();
		if (exitWhenQuitting)
			System.exit(0);
	}
	
	void saveWindowLocations() {
		Frame frame = WindowManager.getFrame("B&C");
		if (frame!=null)
			Prefs.saveLocation(ContrastAdjuster.LOC_KEY, frame.getLocation());
		frame = WindowManager.getFrame("Threshold");
		if (frame!=null)
			Prefs.saveLocation(ThresholdAdjuster.LOC_KEY, frame.getLocation());
		frame = WindowManager.getFrame("Results");
		if (frame!=null) {
			Prefs.saveLocation(TextWindow.LOC_KEY, frame.getLocation());
			Dimension d = frame.getSize();
			Prefs.set(TextWindow.WIDTH_KEY, d.width);
			Prefs.set(TextWindow.HEIGHT_KEY, d.height);
		}
	}
	
}
