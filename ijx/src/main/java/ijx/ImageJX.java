/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx;

import ijx.util.Tools;
import ijx.gui.IjxToolbar;
import ijx.implementation.awt.FactoryAWT;
import ijx.implementation.mdi.FactoryMDI;
import ijx.implementation.mdi.TopComponentMDI;
import ijx.implementation.swing.FactorySwing;

import java.awt.*;
import java.io.*;

import ijx.app.IjxAbstractApplication;
import ijx.app.IjxApplication;
import ijx.app.KeyboardHandler;
import ijx.etc.StartupDialog;
import ijx.event.ApplicationEvent;
import ijx.event.EventBus;
import ijx.exec.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

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
Runs a macro, passing it an optional argument
Example 1: -macro analyze.ijm
Example 2: -macro analyze /Users/wayne/images/stack1

-batch path [arg]
Runs a macro in batch (no GUI) mode, passing it an optional argument.
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

/*
 * GBH, ImageJX...
 *
 */
public class ImageJX extends IjxAbstractApplication {

    /** Creates a new ImageJ frame that runs as an application. */
    public ImageJX() {
        this(null, STANDALONE);
    }

    /** Creates a new ImageJ frame that runs as an applet. */
    public ImageJX(ImageJApplet applet) {
        this(applet, 0);
    }

    /** If 'applet' is not null, creates a new ImageJ frame that runs as an applet.
    If  'mode' is ImageJ.EMBEDDED and 'applet is null, creates an embedded
    version of ImageJ which does not start the SocketListener. */
    public ImageJX(ImageJApplet applet, int mode) {
        //super("ImageJ");
        // Mac OSX
        System.setProperty("com.apple.macos.useScreenMenuBar", "true");

        embedded = applet == null && mode == EMBEDDED;
        this.applet = applet;
        String err1 = Prefs.load(this, applet);
        //
        if (guiMode == null) {
            System.exit(999);
        }
        if (guiMode.equalsIgnoreCase("AWT")) {
            IJ.setFactory(new FactoryAWT());
        }
        if (guiMode.equalsIgnoreCase("SDI (Swing)")) {
            IJ.setFactory(new FactorySwing());
        }
        if (guiMode.equalsIgnoreCase("MDI (Swing)")) {
            IJ.setFactory(new FactoryMDI());
        }
        //
        // TopComponent...
        IjxTopComponent topComponent = IJ.getFactory().newTopComponent(this, TITLE);
        CentralLookup.getDefault().add(topComponent);
        IJ.init((IjxApplication) this, topComponent, applet);
        // Menus...
        IjxMenus menus = IJ.getFactory().newMenus(topComponent, this, applet);
        CentralLookup.getDefault().add(menus);
        String err2 = menus.addMenuBar();
        menus.installPopupMenu(this);
        // Toolbar...
        IjxToolbar toolbar = IJ.getFactory().newToolBar();
        CentralLookup.getDefault().add(toolbar);
        topComponent.setToolbar(toolbar);
        // Statusbar...
        topComponent.addStatusBar();
        // Keyboard Handler...
        KeyboardHandler keyHandler = new KeyboardHandler(this);
        CentralLookup.getDefault().add(keyHandler);
        topComponent.getFrame().addKeyListener(keyHandler);
        //
        if (IJ.isLinux()) {
            Color backgroundColor = new Color(240, 240, 240);
            topComponent.setBackground(backgroundColor);
        }
        topComponent.finishAndShow();
//        try {
//            JInternalFrame frame = new JInternalFrame("test",
//                    true, //resizable
//                    true, //closable
//                    true, //maximizable
//                    true);//iconifiable
//            frame.setBounds(10, 10, 300, 400);
//            ((TopComponentMDI) topComponent).getDesktop().add(frame);
//            frame.setVisible(true);
//            frame.setSelected(true);
//        } catch (java.beans.PropertyVetoException pve) {
//        }
        if (err1 != null) {
            IJ.error(err1);
        }
        if (err2 != null) {
            IJ.error(err2);
        }
        // @todo ?? Should this be a PlugIn ??
        if (IJ.isMacintosh() && applet == null) {
            Object qh = null;
            if (IJ.isJava14()) {
                qh = IJ.runPlugIn("MacAdapter", "");
            }
            if (qh == null) {
                IJ.runPlugIn("QuitHandler", "");
            }
        }
        if (applet == null) {
            // @todo ?? Should this be a PlugIn ??
            IJ.runPlugIn("ijx.core.DragAndDrop", "");
        }
        // @todo re-enable
        //m.installStartupMacroSet();
        //String str = m.getMacroCount() == 1 ? " macro)" : " macros)";
        //String java = "Java " + System.getProperty("java.version");
        //IJ.showStatus("ImageJX " + VERSION + "/" + java + " (" + m.getPluginCount() + " commands, " + m.getMacroCount() + str);

        if (applet == null && !embedded) {
            new SocketListener();
        }
        configureProxy();
    }

    public static void main(String args[]) {
        EventBus.getDefault().publish(new ApplicationEvent(ApplicationEvent.Type.STARTING));

        if (System.getProperty("java.version").substring(0, 3).compareTo("1.4") < 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "ImageJ " + VERSION + " requires Java 1.4.1 or later.");
            System.exit(0);
        }

        arguments = args;
        processCommandLineArgs(arguments);
        int nArgs = args != null ? args.length : 0;
        // If ImageJ is already running then isRunning()
        // will pass the arguments to it using sockets.
        if (nArgs > 0 && !noGUI && (mode == STANDALONE) && isRunning(args)) {
            return;
        }

        guiMode = "SDI (Swing)";
//        if (true) { //isSelectModeDialog()
//            try { // For Demo / Testing, Dialog to select GUI mode.
//                java.awt.EventQueue.invokeAndWait(new Runnable() {
//                    public void run() {
//                        StartupDialog dialog = new StartupDialog(new javax.swing.JFrame(), "ImageJX Startup", true);
//                        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//                            public void windowClosing(java.awt.event.WindowEvent e) {
//                                System.exit(0);
//                            }
//                        });
//                        dialog.setVisible(true);
//                        int ret = dialog.getReturnStatus();
//                        guiMode = dialog.getMode();
//                    }
//                });
//            } catch (InterruptedException ex) {
//                Logger.getLogger(ImageJX.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (InvocationTargetException ex) {
//                Logger.getLogger(ImageJX.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        } else {
//            guiMode = "AWT";
//        }

        IjxApplication ij = IJ.getInstance();
        if (!noGUI && (ij == null || (ij != null && !IJ.getTopComponentFrame().isShowing()))) {
            final int mode_ = mode;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setupUI();
                    IjxApplication ij = new ImageJX(null, mode_);
                    ij.exitWhenQuitting(true);
                }
            });
        }
        int macros = 0;
        for (int i = 0; i < nArgs; i++) {
            String arg = args[i];
            if (arg == null) {
                continue;
            }
            if (arg.startsWith("-")) {
                if ((arg.startsWith("-macro") || arg.startsWith("-batch")) && i + 1 < nArgs) {
                    String arg2 = i + 2 < nArgs ? args[i + 2] : null;
                    IJ.runMacroFile(args[i + 1], arg2);
                    break;
                } else if (arg.startsWith("-eval") && i + 1 < nArgs) {
                    String rtn = IJ.runMacro(args[i + 1]);
                    if (rtn != null) {
                        System.out.print(rtn);
                    }
                    args[i + 1] = null;
                } else if (arg.startsWith("-run") && i + 1 < nArgs) {
                    IJ.run(args[i + 1]);
                    args[i + 1] = null;
                }
            } else if (macros == 0 && (arg.endsWith(".ijm") || arg.endsWith(".txt"))) {
                IJ.runMacroFile(arg);
                macros++;
            } else if (arg.indexOf("ijx.ImageJ") == -1) {
                File file = new File(arg);
                IJ.open(file.getAbsolutePath());
            }
        }
        if (noGUI) {
            System.exit(0);
        }
    }

  private static void processCommandLineArgs(String[] args) {
     int nArgs = args != null ? args.length : 0;
        for (int i = 0; i < nArgs; i++) {
            String arg = args[i];
            if (arg == null) {
                continue;
            }
            //IJ.log(i+"  "+arg);
            if (args[i].startsWith("-")) {
                if (args[i].startsWith("-batch")) {
                    noGUI = true;
                } else if (args[i].startsWith("-ijpath") && i + 1 < nArgs) {
                    Prefs.setHomeDir(args[i + 1]);
                    args[i + 1] = null;
                } else if (args[i].startsWith("-port")) {
                    int delta = (int) Tools.parseDouble(args[i].substring(5, args[i].length()), 0.0);
                    if (delta == 0) {
                        mode = EMBEDDED;
                    } else if (delta > 0 && DEFAULT_PORT + delta < 65536) {
                        port = DEFAULT_PORT + delta;
                    }
                }
            }
        }
  }

    private static void setupUI() {
        try {
            //JFrame.setDefaultLookAndFeelDecorated(true);
            // If Globally using heavyweight components:
            JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
            // Use Auditory Queues
            UIManager.put("AuditoryCues.playList", UIManager.get("AuditoryCues.defaultCueList"));
            //UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel");
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            //UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");


//            // Nimbus
//            // http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/nimbus.html
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                System.out.println(info.getName());
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }



    }
//	void configureProxy() {
//		String server = Prefs.get("proxy.server", null);
//		if (server==null||server.equals("")) return;
//		int port = (int)Prefs.get("proxy.port", 0);
//		if (port==0) return;
//		String user = Prefs.get("proxy.user", null);
//		Properties props = System.getProperties();
//		props.put("proxySet", "true");
//		props.put("http.proxyHost", server);
//		props.put("http.proxyPort", ""+port);
//		if (user!=null)
//			props.put("http.proxyUser", user);
//		//IJ.log(server+"  "+port+"  "+user);
//	}
//
//
//
//    /** Starts executing a menu command in a separate thread. */
//    public void doCommand(String name) {
//		new Executer(name, null);
//    }
//
//	public void runFilterPlugIn(Object theFilter, String cmd, String arg) {
//		new PlugInFilterRunner(theFilter, cmd, arg);
//	}
//
//	public Object runUserPlugIn(String commandName, String className, String arg, boolean createNewLoader) {
//		return IJ.runUserPlugIn(commandName, className, arg, createNewLoader);
//	}
//
//	/** Return the current list of modifier keys. */
//	public static String modifiers(int flags) { //?? needs to be moved
//		String s = " [ ";
//		if (flags == 0) return "";
//		if ((flags & Event.SHIFT_MASK) != 0) s += "Shift ";
//		if ((flags & Event.CTRL_MASK) != 0) s += "Control ";
//		if ((flags & Event.META_MASK) != 0) s += "Meta ";
//		if ((flags & Event.ALT_MASK) != 0) s += "Alt ";
//		s += "] ";
//		return s;
//	}
//
//	/** Handle menu events. */
//	public void actionPerformed(ActionEvent e) {
//		if ((e.getSource() instanceof MenuItem)) {
//			MenuItem item = (MenuItem)e.getSource();
//			String cmd = e.getActionCommand();
//			if (item.getParent()==Menus.openRecentMenu) {
//				new RecentOpener(cmd); // open image in separate thread
//				return;
//			}
//			hotkey = false;
//			actionPerformedTime = System.currentTimeMillis();
//			long ellapsedTime = actionPerformedTime-keyPressedTime;
//			if (cmd!=null && (ellapsedTime>=200L||!cmd.equals(lastKeyCommand)))
//				doCommand(cmd);
//			lastKeyCommand = null;
//			if (IJ.debugMode) IJ.log("actionPerformed: time="+ellapsedTime+", "+e);
//		}
//	}
//
//	/** Handles CheckboxMenuItem state changes. */
//	public void itemStateChanged(ItemEvent e) {
//		MenuItem item = (MenuItem)e.getSource();
//		MenuComponent parent = (MenuComponent)item.getParent();
//		String cmd = e.getItem().toString();
//		if ((Menu)parent==Menus.window)
//			WindowManager.activateWindow(cmd, item);
//		else
//			doCommand(cmd);
//	}
//
//
//
// 	public void keyPressed(KeyEvent e) {
//		int keyCode = e.getKeyCode();
//		IJ.setKeyDown(keyCode);
//		hotkey = false;
//		if (keyCode==e.VK_CONTROL || keyCode==e.VK_SHIFT)
//			return;
//		char keyChar = e.getKeyChar();
//		int flags = e.getModifiers();
//		if (IJ.debugMode) IJ.log("keyPressed: code=" + keyCode + " (" + KeyEvent.getKeyText(keyCode)
//			+ "), char=\"" + keyChar + "\" (" + (int)keyChar + "), flags="
//			+ KeyEvent.getKeyModifiersText(flags));
//		boolean shift = (flags & e.SHIFT_MASK) != 0;
//		boolean control = (flags & e.CTRL_MASK) != 0;
//		boolean alt = (flags & e.ALT_MASK) != 0;
//		boolean meta = (flags & e.META_MASK) != 0;
//		String cmd = "";
//		IjxImagePlus imp = WindowManager.getCurrentImage();
//		boolean isStack = (imp!=null) && (imp.getStackSize()>1);
//
//		if (imp!=null && !control && ((keyChar>=32 && keyChar<=255) || keyChar=='\b' || keyChar=='\n')) {
//			Roi roi = imp.getRoi();
//			if (roi instanceof TextRoi) {
//				if ((flags & e.META_MASK)!=0 && IJ.isMacOSX()) return;
//				if (alt)
//					switch (keyChar) {
//						case 'u': case 'm': keyChar = IJ.micronSymbol; break;
//						case 'A': keyChar = IJ.angstromSymbol; break;
//						default:
//					}
//				((TextRoi)roi).addChar(keyChar);
//				return;
//			}
//		}
//
//		// Handle one character macro shortcuts
//		if (!control && !meta) {
//			Hashtable macroShortcuts = Menus.getMacroShortcuts();
//			if (macroShortcuts.size()>0) {
//				if (shift)
//					cmd = (String)macroShortcuts.get(new Integer(keyCode+200));
//				else
//					cmd = (String)macroShortcuts.get(new Integer(keyCode));
//				if (cmd!=null) {
//					//MacroInstaller.runMacroCommand(cmd);
//					MacroInstaller.runMacroShortcut(cmd);
//					return;
//				}
//			}
//		}
//
//		if (!Prefs.requireControlKey || control || meta) {
//			Hashtable shortcuts = Menus.getShortcuts();
//			if (shift)
//				cmd = (String)shortcuts.get(new Integer(keyCode+200));
//			else
//				cmd = (String)shortcuts.get(new Integer(keyCode));
//		}
//
//		if (cmd==null) {
//			switch (keyChar) {
//				case '<': cmd="Previous Slice [<]"; break;
//				case '>': cmd="Next Slice [>]"; break;
//				case '+': case '=': cmd="In"; break;
//				case '-': cmd="Out"; break;
//				case '/': cmd="Reslice [/]..."; break;
//				default:
//			}
//		}
//
//		if (cmd==null) {
//			switch(keyCode) {
//				case KeyEvent.VK_TAB: WindowManager.putBehind(); return;
//				case KeyEvent.VK_BACK_SPACE: cmd="Clear"; hotkey=true; break; // delete
//				//case KeyEvent.VK_BACK_SLASH: cmd=IJ.altKeyDown()?"Animation Options...":"Start Animation"; break;
//				case KeyEvent.VK_EQUALS: cmd="In"; break;
//				case KeyEvent.VK_MINUS: cmd="Out"; break;
//				case KeyEvent.VK_SLASH: case 0xbf: cmd="Reslice [/]..."; break;
//				case KeyEvent.VK_COMMA: case 0xbc: cmd="Previous Slice [<]"; break;
//				case KeyEvent.VK_PERIOD: case 0xbe: cmd="Next Slice [>]"; break;
//				case KeyEvent.VK_LEFT: case KeyEvent.VK_RIGHT: case KeyEvent.VK_UP: case KeyEvent.VK_DOWN: // arrow keys
//					Roi roi = null;
//					if (imp!=null) roi = imp.getRoi();
//					if (roi==null) return;
//					if ((flags & KeyEvent.ALT_MASK) != 0)
//						roi.nudgeCorner(keyCode);
//					else
//						roi.nudge(keyCode);
//					return;
//				case KeyEvent.VK_ESCAPE:
//					abortPluginOrMacro(imp);
//					return;
//				case KeyEvent.VK_ENTER: IJ.getTopComponentFrame().toFront(); return;
//				default: break;
//			}
//		}
//
//		if (cmd!=null && !cmd.equals("")) {
//			if (cmd.equals("Fill"))
//				hotkey = true;
//			if (cmd.charAt(0)==MacroInstaller.commandPrefix)
//				MacroInstaller.runMacroShortcut(cmd);
//			else {
//				doCommand(cmd);
//				keyPressedTime = System.currentTimeMillis();
//				lastKeyCommand = cmd;
//			}
//		}
//	}
//
//	public void keyTyped(KeyEvent e) {
//		char keyChar = e.getKeyChar();
//		int flags = e.getModifiers();
//		if (IJ.debugMode) IJ.log("keyTyped: char=\"" + keyChar + "\" (" + (int)keyChar
//			+ "), flags= "+Integer.toHexString(flags)+ " ("+KeyEvent.getKeyModifiersText(flags)+")");
//		if (keyChar=='\\' || keyChar==171 || keyChar==223) {
//			if (((flags&Event.ALT_MASK)!=0))
//				doCommand("Animation Options...");
//			else
//				doCommand("Start Animation [\\]");
//		}
//	}
//
//	public void keyReleased(KeyEvent e) {
//		IJ.setKeyUp(e.getKeyCode());
//	}
//
//    public boolean isHotkey() {
//        return hotkey;
//    }
//
//	void abortPluginOrMacro(IjxImagePlus imp) {
////		if (imp!=null) {
////			IjxWindow win = imp.getWindow();
////			if (win!=null) {
////                if(win instanceof PluginFrame )
////				win.running = false;
////				win.running2 = false;
////			}
////		}
//		Macro.abort();
//		Interpreter.abort();
//		if (Interpreter.getInstance()!=null) IJ.beep();
//	}
//
//
//
//	/** Adds the specified class to a Vector to keep it from being
//		garbage collected, causing static fields to be reset. */
//	public void register(Class c) {
//		if (!classes.contains(c))
//			classes.addElement(c);
//	}
//
//	/** Called by ImageJ when the user selects Quit. */
//	public void quit() {
//		Thread thread = new Thread(this, "Quit");
//		thread.setPriority(Thread.NORM_PRIORITY);
//		thread.start();
//	}
//
//	/** Returns true if ImageJ is exiting. */
//	public boolean quitting() {
//		return quitting;
//	}
//
//	/** Called once when ImageJ quits. */
//	public void savePreferences(Properties prefs) {
//		Point loc = IJ.getTopComponentFrame().getLocation();
//		prefs.put(IjxTopComponent.IJ_X, Integer.toString(loc.x));
//		prefs.put(IjxTopComponent.IJ_Y, Integer.toString(loc.y));
//		//prefs.put(IJ_WIDTH, Integer.toString(size.width));
//		//prefs.put(IJ_HEIGHT, Integer.toString(size.height));
//	}
//
//	public static void main(String args[]) {
//		if (System.getProperty("java.version").substring(0,3).compareTo("1.4")<0) {
//			javax.swing.JOptionPane.showMessageDialog(null,"ImageJ "+VERSION+" requires Java 1.4.1 or later.");
//			System.exit(0);
//		}
//		boolean noGUI = false;
//		int mode = STANDALONE;
//		arguments = args;
//		int nArgs = args!=null?args.length:0;
//		for (int i=0; i<nArgs; i++) {
//			String arg = args[i];
//			if (arg==null) continue;
//			//IJ.log(i+"  "+arg);
//			if (args[i].startsWith("-")) {
//				if (args[i].startsWith("-batch"))
//					noGUI = true;
//				else if (args[i].startsWith("-ijpath") && i+1<nArgs) {
//					Prefs.setHomeDir(args[i+1]);
//					args[i+1] = null;
//				} else if (args[i].startsWith("-port")) {
//					int delta = (int)Tools.parseDouble(args[i].substring(5, args[i].length()), 0.0);
//					if (delta==0)
//						mode = EMBEDDED;
//					else if (delta>0 && DEFAULT_PORT+delta<65536)
//						port = DEFAULT_PORT+delta;
//				}
//			}
//		}
//  		// If ImageJ is already running then isRunning()
//  		// will pass the arguments to it using sockets.
//		if (nArgs>0 && !noGUI && (mode==STANDALONE) && isRunning(args))
//  				return;
// 		IjxApplication ij = IJ.getInstance();
//		if (!noGUI && (ij==null || (ij!=null && !IJ.getTopComponentFrame().isShowing()))) {
//			ij = new ImageJ(null, mode);
//			ij.exitWhenQuitting(true);
//		}
//		int macros = 0;
//		for (int i=0; i<nArgs; i++) {
//			String arg = args[i];
//			if (arg==null) continue;
//			if (arg.startsWith("-")) {
//				if ((arg.startsWith("-macro") || arg.startsWith("-batch")) && i+1<nArgs) {
//					String arg2 = i+2<nArgs?args[i+2]:null;
//					IJ.runMacroFile(args[i+1], arg2);
//					break;
//				} else if (arg.startsWith("-eval") && i+1<nArgs) {
//					String rtn = IJ.runMacro(args[i+1]);
//					if (rtn!=null)
//						System.out.print(rtn);
//					args[i+1] = null;
//				} else if (arg.startsWith("-run") && i+1<nArgs) {
//					IJ.run(args[i+1]);
//					args[i+1] = null;
//				}
//			} else if (macros==0 && (arg.endsWith(".ijm") || arg.endsWith(".txt"))) {
//				IJ.runMacroFile(arg);
//				macros++;
//			} else if (arg.indexOf("ijx.ImageJ")==-1) {
//				File file = new File(arg);
//				IJ.open(file.getAbsolutePath());
//			}
//		}
//		if (noGUI) System.exit(0);
//	}
//
//	// Is there another instance of ImageJ? If so, send it the arguments and quit.
//	static boolean isRunning(String args[]) {
//		int macros = 0;
//		int nArgs = args.length;
//		if (nArgs==2 && args[0].startsWith("-ijpath"))
//			return false;
//		int nCommands = 0;
//		try {
//			sendArgument("user.dir "+System.getProperty("user.dir"));
//			for (int i=0; i<nArgs; i++) {
//				String arg = args[i];
//				if (arg==null) continue;
//				String cmd = null;
//				if (macros==0 && arg.endsWith(".ijm")) {
//					cmd = "macro " + arg;
//					macros++;
//				} else if (arg.startsWith("-macro") && i+1<nArgs) {
//					String macroArg = i+2<nArgs?"("+args[i+2]+")":"";
//					cmd = "macro " + args[i+1] + macroArg;
//					sendArgument(cmd);
//					nCommands++;
//					break;
//				} else if (arg.startsWith("-eval") && i+1<nArgs) {
//					cmd = "eval " + args[i+1];
//					args[i+1] = null;
//				} else if (arg.startsWith("-run") && i+1<nArgs) {
//					cmd = "run " + args[i+1];
//					args[i+1] = null;
//				} else if (arg.indexOf("ijx.ImageJ")==-1 && !arg.startsWith("-"))
//					cmd = "open " + arg;
//				if (cmd!=null) {
//					sendArgument(cmd);
//					nCommands++;
//				}
//			} // for
//		} catch (IOException e) {
//			return false;
//		}
//		return true;
//	}
//
//	static void sendArgument(String arg) throws IOException {
//		Socket socket = new Socket("localhost", port);
//		PrintWriter out = new PrintWriter (new OutputStreamWriter(socket.getOutputStream()));
//		out.println(arg);
//		out.close();
//		socket.close();
//	}
//
//	/**
//	Returns the port that ImageJ checks on startup to see if another instance is running.
//	@see ij.SocketListener
//	*/
//	public static int getPort() {
//		return port;
//	}
//
//	/** Returns the command line arguments passed to ImageJ. */
//	public static String[] getArgs() {
//		return arguments;
//	}
//
//	/** ImageJ calls System.exit() when qutting when 'exitWhenQuitting' is true.*/
//	public void exitWhenQuitting(boolean ewq) {
//		exitWhenQuitting = ewq;
//	}
//
//	/** Quit using a separate thread, hopefully avoiding thread deadlocks. */
//	public void run() {
//		quitting = true;
//		boolean changes = false;
//		int[] wList = WindowManager.getIDList();
//		if (wList!=null) {
//			for (int i=0; i<wList.length; i++) {
//				IjxImagePlus imp = (IjxImagePlus) WindowManager.getImage(wList[i]);
//				if (imp!=null && imp.wasChanged()==true) {
//					changes = true;
//					break;
//				}
//			}
//		}
//		if (IJ.getTopComponent().isClosed() && !changes &&
//            Menus.window.getItemCount()>Menus.WINDOW_MENU_ITEMS &&
//            !(IJ.macroRunning()&&WindowManager.getImageCount()==0)) {
//			GenericDialog gd = new GenericDialog("ImageJ", IJ.getTopComponentFrame());
//			gd.addMessage("Are you sure you want to quit ImageJ?");
//			gd.showDialog();
//			quitting = !gd.wasCanceled();
//			// windowClosed = false;  // ????????
//		}
//		if (!quitting)
//			return;
//		if (!WindowManager.closeAllWindows()) {
//			quitting = false;
//			return;
//		}
//		//IJ.log("savePreferences");
//		if (applet==null) {
//			saveWindowLocations();
//			Prefs.savePreferences();
//		}
//		IJ.getTopComponentFrame().setVisible(false);
//		//IJ.log("dispose");
//		IJ.getTopComponentFrame().dispose();
//		if (exitWhenQuitting)
//			System.exit(0);
//	}
//
//	public void saveWindowLocations() {
//		IjxWindow frame = WindowManager.getFrame("B&C");
//		if (frame!=null)
//			Prefs.saveLocation(ContrastAdjuster.LOC_KEY, frame.getLocation());
//		frame = WindowManager.getFrame("Threshold");
//		if (frame!=null)
//                Prefs.saveLocation(ThresholdAdjuster.LOC_KEY, frame.getLocation());
//		frame = WindowManager.getFrame("Results");
//		if (frame!=null) {
//			Prefs.saveLocation(TextWindow.LOC_KEY, frame.getLocation());
//			Dimension d = frame.getSize();
//			Prefs.set(TextWindow.WIDTH_KEY, d.width);
//			Prefs.set(TextWindow.HEIGHT_KEY, d.height);
//		}
//	}
}
