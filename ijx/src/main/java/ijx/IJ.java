package ijx;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import ijx.app.IjxApplication;
import ijx.app.KeyboardHandler;
import ijx.event.EventBus;
import ijx.event.StatusMessage;
import ijx.gui.IjxImageCanvas;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxProgressBar;
import ijx.gui.IjxToolbar;
import ijx.gui.IjxWindow;
import ijx.gui.NewImage;
import ijx.gui.Wand;
import ijx.gui.dialog.GenericDialog;
import ijx.gui.dialog.HTMLDialog;
import ijx.gui.dialog.MessageDialog;
import ijx.gui.dialog.YesNoCancelDialog;
import ijx.io.DirectoryChooser;
import ijx.io.FileInfo;
import ijx.io.OpenDialog;
import ijx.io.Opener;
import ijx.io.PluginClassLoader;
import ijx.io.SaveDialog;
import ijx.macro.Interpreter;
import ijx.measure.Calibration;
import ijx.measure.ResultsTable;
import ijx.plugin.Macro_Runner;
import ijx.plugin.api.PlugIn;
import ijx.plugin.api.PlugInFilter;
import ijx.plugin.api.PlugInFilterRunner;
import ijx.plugin.filter.Analyzer;
import ijx.plugin.frame.Recorder;
import ijx.process.Blitter;
import ijx.process.ColorProcessor;
import ijx.process.ImageProcessor;
import ijx.roi.Line;
import ijx.roi.OvalRoi;
import ijx.roi.PointRoi;
import ijx.roi.PolygonRoi;
import ijx.roi.Roi;
import ijx.text.*;
import imagej.util.Memory;
import imagej.util.Tools;

/** This class consists of static utility methods. */
public class IJ {

    public static final String URL = "http://rsb.info.nih.gov/ij";
    public static final int ALL_KEYS = -1;
    public static final char micronSymbol = '\u00B5';
    public static final char angstromSymbol = '\u00C5';
    public static final char degreeSymbol = '\u00B0';
    //
    private static IjxApplication ijApp;
    private static IjxTopComponent topComponent;
    private static IjxFactory factory;
    private static ImageJApplet applet;
    private static ClassLoader classLoader;
    private static Hashtable commandTable;
    private static Vector eventListeners = new Vector();
    private static IjxImagePlus clipboard;
    private static IjxProgressBar progressBar;
    private static TextPanel textPanel;
    private static TextPanel logPanel;
    // Environment
    private static String osname, osarch;
    private static boolean isMac, isWin, isJava2, isJava14, isJava15, isJava16, isJava17, isLinux, isVista, is64Bit;
    //
    private static boolean controlDown, altDown, spaceDown, shiftDown;
    private static boolean macroRunning;
    private static Thread previousThread;
    private static boolean checkForDuplicatePlugins = true;
    private static boolean memMessageDisplayed;
    private static long maxMemory;
    private static boolean escapePressed;
    private static boolean redirectErrorMessages, redirectErrorMessages2;
    private static boolean suppressPluginNotFoundError;
    private static boolean centerOnScreen;
    // public static fields
    public static Color backgroundColor = new Color(220, 220, 220); //224,226,235
    public static boolean debugMode;
    public static boolean hideProcessStackDialog;

    public static void init(IjxApplication imagej, IjxTopComponent tc, ImageJApplet theApplet) {
        if (theApplet == null) {
            System.setSecurityManager(null);
        }
        ijApp = imagej;
        topComponent = tc;
        applet = theApplet;
        progressBar = topComponent.getProgressBar();
    }
    /*
     * Called to make sure that things are setup before tunning commands or plugins.
     */

    static void init() {
//        // @todo - I expect that this will break...
//        Menus m = new Menus(topComponent, ij, applet);
//        Prefs.load(m, null);
//        m.addMenuBar();
    }

    /**Returns the Applet that created this ImageJ or null if running as an application.*/
    public static java.applet.Applet getApplet() {
        return applet;
    }

// <editor-fold defaultstate="collapsed" desc=" Ijx... ">
    static void cleanup() {
        ijApp = null;
        applet = null;
        progressBar = null;
        textPanel = null;
        // ? topComponent=null;
    }

    /**Returns a reference to the "ImageJ" frame.*/
    public static IjxApplication getInstance() {
        return ijApp;
    }

    public static IjxTopComponent getTopComponent() {
        return topComponent;
    }

    public static Frame getTopComponentFrame() {
        return topComponent.getFrame();
    }

    public static void setTopComponent(IjxTopComponent tc) {
        topComponent = tc;
    }

    // Factory, GBH
    public static IjxFactory getFactory() {
        return factory;
    }

    public static void setFactory(IjxFactory factory) {
        IJ.factory = factory;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Macro Running ">
    /** Runs the macro contained in the string <code>macro</code>.
    Returns any string value returned by the macro, null if the macro
    does not return a value, or "[aborted]" if the macro was aborted
    due to an error. The equivalent macro function is eval(). */
    public static String runMacro(String macro) {
        return runMacro(macro, "");
    }

    /** Runs the macro contained in the string <code>macro</code>.
    The optional string argument can be retrieved in the
    called macro using the getArgument() macro function.
    Returns any string value returned by the macro, null if the macro
    does not return a value, or "[aborted]" if the macro was aborted
    due to an error.  */
    public static String runMacro(String macro, String arg) {
        if (ijApp == null && Menus.getCommands() == null) {
            init();
        }
        Macro_Runner mr = new Macro_Runner();
        return mr.runMacro(macro, arg);
    }

    /** Runs the specified macro or script file in the current thread.
    The file is assumed to be in the macros folder
    unless <code>name</code> is a full path. ".txt"  is
    added if <code>name</code> does not have an extension.
    The optional string argument (<code>arg</code>) can be retrieved in the called
    macro or script (v1.42k or later) using the getArgument() function.
    Returns any string value returned by the macro, or null. Scripts always return null.
    The equivalent macro function is runMacro(). */
    public static String runMacroFile(String name, String arg) {
        if (ijApp == null && Menus.getCommands() == null) {
            init();
        }
        Macro_Runner mr = new Macro_Runner();
        return mr.runMacroFile(name, arg);
    }

    /** Runs the specified macro file. */
    public static String runMacroFile(String name) {
        return runMacroFile(name, null);
    }

    /** Returns true if the run(), open() or newImage() method is executing. */
    public static boolean macroRunning() {
        return macroRunning;
    }

    /** Returns true if a macro is running, or if the run(), open()
    or newImage() method is executing. */
    public static boolean isMacro() {
        return macroRunning || Interpreter.getInstance() != null;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Pluging Running ">
    /** Runs the specified plugin using the specified image. */
    public static Object runPlugIn(IjxImagePlus imp, String className, String arg) {
        if (imp != null) {
            IjxImagePlus temp = WindowManager.getTempCurrentImage();
            WindowManager.setTempCurrentImage(imp);
            Object o = runPlugIn("", className, arg);
            WindowManager.setTempCurrentImage(temp);
            return o;
        } else {
            return runPlugIn(className, arg);
        }
    }

    /** Runs the specified plugin and returns a reference to it. */
    public static Object runPlugIn(String className, String arg) {
        return runPlugIn("", className, arg);
    }

    /** Runs the specified plugin and returns a reference to it. */
    public static Object runPlugIn(String commandName, String className, String arg) {
        if (IJ.debugMode) {
            IJ.log("runPlugin: " + className + " " + arg);
        }
        if (arg == null) {
            arg = "";
        }
        // Load using custom classloader if this is a user
        // plugin and we are not running as an applet
        if (!className.startsWith("ijx.") && !className.startsWith("imagej.")) {
            return runUserPlugIn(commandName, className, arg, false);
        }
        Object thePlugIn = null;
        try {
            thePlugIn = runPlugInUnchecked(commandName, className, arg);
        } catch (ClassNotFoundException e) {
            if (IJ.getApplet() == null) {
                log("Plugin or class not found: \"" + className + "\"\n(" + e + ")");
            } else {
                showStatus("Plugin or class not found: '" + className + "' (" + e + ")");
            }
        } catch (InstantiationException e) {
            log("Unable to load plugin (ins)");
        } catch (IllegalAccessException e) {
            log("Unable to load plugin, possibly \nbecause it is not public.");
        }
        redirectErrorMessages = false;
        return thePlugIn;
    }

    public static Object runPlugInUnchecked(String commandName, String className, String arg) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class c = Class.forName(className);
        Object thePlugIn = c.newInstance();
        if (thePlugIn instanceof ij.plugin.PlugIn) {
            ((ij.plugin.PlugIn) thePlugIn).run(arg);
        } else if (thePlugIn instanceof PlugIn) {
            ((PlugIn) thePlugIn).run(arg);
        } else {
            new PlugInFilterRunner(thePlugIn, commandName, arg);
        }
        return thePlugIn;
    }

    public static Object runUserPlugIn(String commandName, String className, String arg, boolean createNewLoader) {
        if (applet == null && checkForDuplicatePlugins) {
            // check for duplicate classes and jars in the plugins folder
            IJ.runPlugIn("ijx.plugin.ClassChecker", "");
            checkForDuplicatePlugins = false;
        }
        if (createNewLoader) {
            classLoader = null;
        }
        ClassLoader loader = getClassLoader();
        Object thePlugIn = null;
        try {
            thePlugIn = (loader.loadClass(className)).newInstance();
            if (thePlugIn instanceof PlugIn) {
                ((PlugIn) thePlugIn).run(arg);
            } else if (thePlugIn instanceof PlugInFilter) {
                new PlugInFilterRunner(thePlugIn, commandName, arg);
            }
        } catch (ClassNotFoundException e) {
            if (className.indexOf('_') != -1 && !suppressPluginNotFoundError) {
                error("Plugin or class not found: \"" + className + "\"\n(" + e + ")");
            }
        } catch (NoClassDefFoundError e) {
            int dotIndex = className.indexOf('.');
            String cause = e.getMessage();
            int parenIndex = cause.indexOf('(');
            if (parenIndex >= 1) {
                cause = cause.substring(0, parenIndex - 1);
            }
            boolean correctClass = cause.endsWith(dotIndex < 0
                    ? className : className.substring(dotIndex + 1));
            if (!correctClass && !suppressPluginNotFoundError) {
                error("Plugin " + className
                        + " did not find required class: "
                        + e.getMessage());
            }
            if (correctClass && dotIndex >= 0) {
                return runUserPlugIn(commandName, className.substring(dotIndex + 1), arg, createNewLoader);
            }
            if (className.indexOf('_') != -1 && !suppressPluginNotFoundError) {
                error("Plugin or class not found: \"" + className + "\"\n(" + e + ")");
            }
        } catch (InstantiationException e) {
            error("Unable to load plugin (ins)");
        } catch (IllegalAccessException e) {
            error("Unable to load plugin, possibly \nbecause it is not public.");
        }
        if (redirectErrorMessages && !"HandleExtraFileTypes".equals(className)) {
            redirectErrorMessages = false;
        }
        suppressPluginNotFoundError = false;
        return thePlugIn;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Command Runners ">
    /** Starts executing a menu command in a separete thread and returns immediately. */
    public static void doCommand(String command) {
        if (ijApp != null) {
            ijApp.doCommand(command);
        }
    }

    /** Runs an ImageJ command. Does not return until 
    the command has finished executing. To avoid "image locked",
    errors, plugins that call this method should implement
    the PlugIn interface instead of PlugInFilter. */
    public static void run(String command) {
        run(command, null);
    }

    /** Runs an ImageJ command, with options that are passed to the
    GenericDialog and OpenDialog classes. Does not return until
    the command has finished executing. */
    public static void run(String command, String options) {
        //IJ.log("run1: "+command+" "+Thread.currentThread().hashCode());
        if (ijApp == null && Menus.getCommands() == null) {
            init();
        }
        Macro.abort = false;
        Macro.setOptions(options);
        Thread thread = Thread.currentThread();
        if (previousThread == null || thread != previousThread) {
            String name = thread.getName();
            if (!name.startsWith("Run$_")) {
                thread.setName("Run$_" + name);
            }
        }
        command = convertRenamedCommands(command);
        previousThread = thread;
        macroRunning = true;
        Executer e = new Executer(command);
        e.run();
        macroRunning = false;
        Macro.setOptions(null);
        testAbort();
        //IJ.log("run2: "+command+" "+Thread.currentThread().hashCode());
    }

    /** Runs an ImageJ command using the specified image and options. */
    public static void run(IjxImagePlus imp, String command, String options) {
        if (imp != null) {
            IjxImagePlus temp = WindowManager.getTempCurrentImage();
            WindowManager.setTempCurrentImage(imp);
            run(command, options);
            WindowManager.setTempCurrentImage(temp);
        } else {
            run(command, options);
        }
    }

// </editor-fold>
    /** Converts commands that have been renamed so
    macros using the old names continue to work. */
    private static String convertRenamedCommands(String command) {
        if (commandTable == null) {
            commandTable = new Hashtable(23); // initial capacity should be increased as needed
            commandTable.put("New...", "Image...");
            commandTable.put("Threshold", "Make Binary");
            commandTable.put("Display...", "Appearance...");
            commandTable.put("Start Animation", "Start Animation [\\]");
            commandTable.put("Convert Images to Stack", "Images to Stack");
            commandTable.put("Convert Stack to Images", "Stack to Images");
            commandTable.put("Convert Stack to RGB", "Stack to RGB");
            commandTable.put("Convert to Composite", "Make Composite");
            commandTable.put("New HyperStack...", "New Hyperstack...");
            commandTable.put("Stack to HyperStack...", "Stack to Hyperstack...");
            commandTable.put("HyperStack to Stack", "Hyperstack to Stack");
            commandTable.put("RGB Split", "Split Channels");
            commandTable.put("RGB Merge...", "Merge Channels...");
            commandTable.put("Channels...", "Channels Tool...");
            commandTable.put("New... ", "Table...");
            commandTable.put("Arbitrarily...", "Rotate... ");
            commandTable.put("Measurements...", "Results... ");
        }
        String command2 = (String) commandTable.get(command);
        if (command2 != null) {
            return command2;
        } else {
            return command;
        }
    }

// <editor-fold defaultstate="collapsed" desc=" Results / Log Window ">
    /**
     * @deprecated
     * replaced by IJ.log(), ResultsTable.setResult() and TextWindow.append().
     * There are examples at
     *   http://rsbweb.nih.gov/ij/plugins/sine-cosine.html
     */
    public static void write(String s) {
        if (textPanel == null && ijApp != null) {
            showResults();
        }
        if (textPanel != null) {
            textPanel.append(s);
        } else {
            System.out.println(s);
        }
    }

    public static synchronized void log(String s) {
        if (s == null) {
            return;
        }
        if (logPanel == null && ijApp != null) {
            TextWindow logWindow = new TextWindow("Log", "", 400, 250);
            logPanel = logWindow.getTextPanel();
            logPanel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        }
        if (logPanel != null) {
            if (s.startsWith("\\")) {
                handleLogCommand(s);
            } else {
                logPanel.append(s);
            }
        } else {
            System.out.println(s);
        }
    }

    static void handleLogCommand(String s) {
        if (s.equals("\\Closed")) {
            logPanel = null;
        } else if (s.startsWith("\\Update:")) {
            int n = logPanel.getLineCount();
            String s2 = s.substring(8, s.length());
            if (n == 0) {
                logPanel.append(s2);
            } else {
                logPanel.setLine(n - 1, s2);
            }
        } else if (s.startsWith("\\Update")) {
            int cindex = s.indexOf(":");
            if (cindex == -1) {
                logPanel.append(s);
                return;
            }
            String nstr = s.substring(7, cindex);
            int line = (int) Tools.parseDouble(nstr, -1);
            if (line < 0 || line > 25) {
                logPanel.append(s);
                return;
            }
            int count = logPanel.getLineCount();
            while (line >= count) {
                log("");
                count++;
            }
            String s2 = s.substring(cindex + 1, s.length());
            logPanel.setLine(line, s2);
        } else if (s.equals("\\Clear")) {
            logPanel.clear();
        } else {
            logPanel.append(s);
        }
    }

    /** Clears the "Results" window and sets the column headings to
    those in the tab-delimited 'headings' String. Writes to
    System.out.println if the "ImageJ" frame is not present.*/
    public static void setColumnHeadings(String headings) {
        if (textPanel == null && ijApp != null) {
            showResults();
        }
        if (textPanel != null) {
            textPanel.setColumnHeadings(headings);
        } else {
            System.out.println(headings);
        }
    }

    /** Returns true if the "Results" window is open. */
    public static boolean isResultsWindow() {
        return textPanel != null;
    }

    /** Renames a results window. */
    public static void renameResults(String title) {
        IjxWindow frame = WindowManager.getFrontWindow();
        if (frame != null && (frame instanceof TextWindow)) {
            TextWindow tw = (TextWindow) frame;
            if (tw.getTextPanel().getResultsTable() == null) {
                IJ.error("Rename", "\"" + tw.getTitle() + "\" is not a results table");
                return;
            }
            tw.rename(title);
        } else if (isResultsWindow()) {
            TextPanel tp = getTextPanel();
            TextWindow tw = (TextWindow) tp.getParent();
            tw.rename(title);
        }
    }

    /** Deletes 'row1' through 'row2' of the "Results" window. Arguments
    must be in the range 0-Analyzer.getCounter()-1. */
    public static void deleteRows(int row1, int row2) {
        int n = row2 - row1 + 1;
        ResultsTable rt = Analyzer.getResultsTable();
        for (int i = row1; i < row1 + n; i++) {
            rt.deleteRow(row1);
        }
        rt.show("Results");
    }

    /** Returns a reference to the "Results" window TextPanel.
    Opens the "Results" window if it is currently not open.
    Returns null if the "ImageJ" window is not open. */
    public static TextPanel getTextPanel() {
        if (textPanel == null && ijApp != null) {
            showResults();
        }
        return textPanel;
    }

    /** TextWindow calls this method with a null argument when the "Results" window is closed. */
    public static void setTextPanel(TextPanel tp) {
        textPanel = tp;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Errors ">
    static void wrongType(int capabilities, String cmd) {
        String s = "\"" + cmd + "\" requires an image of type:\n \n";
        if ((capabilities & PlugInFilter.DOES_8G) != 0) {
            s += "    8-bit grayscale\n";
        }
        if ((capabilities & PlugInFilter.DOES_8C) != 0) {
            s += "    8-bit color\n";
        }
        if ((capabilities & PlugInFilter.DOES_16) != 0) {
            s += "    16-bit grayscale\n";
        }
        if ((capabilities & PlugInFilter.DOES_32) != 0) {
            s += "    32-bit (float) grayscale\n";
        }
        if ((capabilities & PlugInFilter.DOES_RGB) != 0) {
            s += "    RGB color\n";
        }
        error(s);
    }

    /**Displays a "no images are open" dialog box.*/
    public static void noImage() {
        error("No Image", "There are no images open.");
    }

    /** Displays an "out of memory" message to the "Log" window. */
    public static void outOfMemory(String name) {
        Undo.reset();
        System.gc();
        String tot = Runtime.getRuntime().totalMemory() / 1048576L + "MB";
        if (!memMessageDisplayed) {
            log(">>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        log("<Out of memory>");
        if (!memMessageDisplayed) {
            log("<All available memory (" + tot + ") has been>");
            log("<used. Instructions for making more>");
            log("<available can be found in the \"Memory\" >");
            log("<sections of the installation notes at>");
            log("<" + IJ.URL + "/docs/install/>");
            log(">>>>>>>>>>>>>>>>>>>>>>>>>>>");
            memMessageDisplayed = true;
        }
        Macro.abort();
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Status / Progress IO ">
    /**Displays a message in the ImageJ status bar.*/
    public static void showStatus(String s) {
        if (ijApp != null) {
            //topComponent.showStatus(s);
            EventBus.getDefault().publish(new StatusMessage(s));
        }
        IjxImagePlus imp = WindowManager.getCurrentImage();
        IjxImageCanvas ic = imp != null ? imp.getCanvas() : null;
        if (ic != null) {
            ic.setShowCursorStatus(s.length() == 0 ? true : false);
        }
    }

    private static void showResults() {
        TextWindow resultsWindow = new TextWindow("Results", "", 400, 250);
        textPanel = resultsWindow.getTextPanel();
        textPanel.setResultsTable(Analyzer.getResultsTable());
        if (ijApp != null) {
            textPanel.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
        }
    }

    /**	Updates the progress bar, where 0<=progress<=1.0. The progress bar is
    not shown in BatchMode and erased if progress>=1.0. The progress bar is
    updated only if more than 90 ms have passes since the last call. Does nothing
    if the ImageJ window is not present. */
    public static void showProgress(double progress) {
        if (progressBar != null) {
            progressBar.show(progress, false);
        }
    }

    /**	Updates the progress bar, where the length of the bar is set to
    (<code>currentValue+1)/finalValue</code> of the maximum bar length.
    The bar is erased if <code>currentValue&gt;=finalValue</code>.
    The bar is updated only if more than 90 ms have passed since the last call.
    Does nothing if the ImageJ window is not present. */
    public static void showProgress(int currentIndex, int finalIndex) {
        if (progressBar != null) {
            progressBar.show(currentIndex, finalIndex);
            if (currentIndex == finalIndex) {
                progressBar.setBatchMode(false);
            }
        }
    }

    /** Displays a message in a dialog box titled "Message".
    Writes the Java console if ImageJ is not present. */
    public static void showMessage(String msg) {
        showMessage("Message", msg);
    }

    /**	Displays a message in a dialog box with the specified TITLE.
    Writes the Java console if ImageJ is not present. */
    public static void showMessage(String title, String msg) {
        if (ijApp != null) {
            if (msg != null && msg.startsWith("<html>")) {
                new HTMLDialog(title, msg);
            } else {
                new MessageDialog(IJ.getTopComponentFrame(), title, msg);
            }
        } else {
            System.out.println(msg);
        }
    }

    /** Displays a message in a dialog box titled "ImageJ". If a
    macro is running, it is aborted. Writes to the Java console
    if the ImageJ window is not present.*/
    public static void error(String msg) {
        error(null, msg);
        if (Thread.currentThread().getName().endsWith("JavaScript")) {
            throw new RuntimeException(Macro.MACRO_CANCELED);
        } else {
            Macro.abort();
        }
    }

    /**Displays a message in a dialog box with the specified TITLE.
    If a macro is running, it is aborted. Writes to the Java
    console if ImageJ is not present. */
    public static void error(String title, String msg) {
        String title2 = title != null ? title : IJ.getInstance().getTitle();
        System.err.println("IJ Error: " + title + ": " + msg);
        boolean abortMacro = title != null;
        if (redirectErrorMessages || redirectErrorMessages2) {
            IJ.log(title2 + ": " + msg);
            if (abortMacro && title.equals("Opener")) {
                abortMacro = false;
            }
        } else {
            showMessage(title2, msg);
        }
        redirectErrorMessages = false;
        if (abortMacro) {
            Macro.abort();
        }
    }

    /** Displays a message in a dialog box with the specified TITLE.
    Returns false if the user pressed "Cancel". */
    public static boolean showMessageWithCancel(String title, String msg) {
        GenericDialog gd = new GenericDialog(title);
        gd.addMessage(msg);
        gd.showDialog();
        return !gd.wasCanceled();
    }
    public static final int CANCELED = Integer.MIN_VALUE;

    /** Allows the user to enter a number in a dialog box. Returns the
    value IJ.CANCELED (-2,147,483,648) if the user cancels the dialog box.
    Returns 'defaultValue' if the user enters an invalid number. */
    public static double getNumber(String prompt, double defaultValue) {
        GenericDialog gd = new GenericDialog("");
        int decimalPlaces = (int) defaultValue == defaultValue ? 0 : 2;
        gd.addNumericField(prompt, defaultValue, decimalPlaces);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return CANCELED;
        }
        double v = gd.getNextNumber();
        if (gd.invalidNumber()) {
            return defaultValue;
        } else {
            return v;
        }
    }

    /** Allows the user to enter a string in a dialog box. Returns
    "" if the user cancels the dialog box. */
    public static String getString(String prompt, String defaultString) {
        GenericDialog gd = new GenericDialog("");
        gd.addStringField(prompt, defaultString, 20);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return "";
        }
        return gd.getNextString();
    }

    /**Delays 'msecs' milliseconds.*/
    public static void wait(int msecs) {
        try {
            Thread.sleep(msecs);
        } catch (InterruptedException e) {
        }
    }

    /** Emits an audio beep. */
    public static void beep() {
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    public static void showTime(IjxImagePlus imp, long start, String str) {
        showTime(imp, start, str, 1);
    }

    public static void showTime(IjxImagePlus imp, long start, String str, int nslices) {
        if (Interpreter.isBatchMode()) {
            return;
        }
        long elapsedTime = System.currentTimeMillis() - start;
        double seconds = elapsedTime / 1000.0;
        long pixels = imp.getWidth() * imp.getHeight();
        int rate = (int) ((double) pixels * nslices / seconds);
        String str2;
        if (rate > 1000000000) {
            str2 = "";
        } else if (rate < 1000000) {
            str2 = ", " + rate + " pixels/second";
        } else {
            str2 = ", " + d2s(rate / 1000000.0, 1) + " million pixels/second";
        }
        showStatus(str + seconds + " seconds" + str2);
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Formating, decimal ">
    /** Converts a number to a formatted string using
    2 digits to the right of the decimal point. */
    public static String d2s(double n) {
        return d2s(n, 2);
    }
    private static DecimalFormat[] df;
    private static DecimalFormat[] sf;
    private static DecimalFormatSymbols dfs;

    /** Converts a number to a rounded formatted string.
    The 'decimalPlaces' argument specifies the number of
    digits to the right of the decimal point (0-9). Uses
    scientific notation if 'decimalPlaces is negative. */
    public static String d2s(double n, int decimalPlaces) {
        if (Double.isNaN(n)) {
            return "NaN";
        }
        if (n == Float.MAX_VALUE) // divide by 0 in FloatProcessor
        {
            return "3.4e38";
        }
        double np = n;
        if (n < 0.0) {
            np = -n;
        }
        if (df == null) {
            dfs = new DecimalFormatSymbols(Locale.US);
            df = new DecimalFormat[10];
            df[0] = new DecimalFormat("0", dfs);
            df[1] = new DecimalFormat("0.0", dfs);
            df[2] = new DecimalFormat("0.00", dfs);
            df[3] = new DecimalFormat("0.000", dfs);
            df[4] = new DecimalFormat("0.0000", dfs);
            df[5] = new DecimalFormat("0.00000", dfs);
            df[6] = new DecimalFormat("0.000000", dfs);
            df[7] = new DecimalFormat("0.0000000", dfs);
            df[8] = new DecimalFormat("0.00000000", dfs);
            df[9] = new DecimalFormat("0.000000000", dfs);
        }
        if (decimalPlaces < 0) {
            decimalPlaces = -decimalPlaces;
            if (decimalPlaces > 9) {
                decimalPlaces = 9;
            }
            if (sf == null) {
                sf = new DecimalFormat[10];
                sf[1] = new DecimalFormat("0.0E0", dfs);
                sf[2] = new DecimalFormat("0.00E0", dfs);
                sf[3] = new DecimalFormat("0.000E0", dfs);
                sf[4] = new DecimalFormat("0.0000E0", dfs);
                sf[5] = new DecimalFormat("0.00000E0", dfs);
                sf[6] = new DecimalFormat("0.000000E0", dfs);
                sf[7] = new DecimalFormat("0.0000000E0", dfs);
                sf[8] = new DecimalFormat("0.00000000E0", dfs);
                sf[9] = new DecimalFormat("0.000000000E0", dfs);
            }
            if (Double.isInfinite(n)) {
                return "" + n;
            } else {
                return sf[decimalPlaces].format(n); // use scientific notation
            }
        }
        if (decimalPlaces < 0) {
            decimalPlaces = 0;
        }
        if (decimalPlaces > 9) {
            decimalPlaces = 9;
        }
        return df[decimalPlaces].format(n);
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Class management ">
    /** Returns the class loader ImageJ uses to run plugins or the
    system class loader if Menus.getPlugInsPath() returns null. */
    public static ClassLoader getClassLoader() {
        if (classLoader == null) {
            String pluginsDir = Menus.getPlugInsPath();
            if (pluginsDir == null) {
                String home = System.getProperty("plugins.dir");
                if (home != null) {
                    if (!home.endsWith(Prefs.separator)) {
                        home += Prefs.separator;
                    }
                    pluginsDir = home + "plugins" + Prefs.separator;
                    if (!(new File(pluginsDir)).isDirectory()) {
                        pluginsDir = home;
                    }
                }
            }
            if (pluginsDir == null) {
                return IJ.class.getClassLoader();
            } else {
                if (MenusAWT.jnlp) {
                    classLoader = new PluginClassLoader(pluginsDir, true);
                } else {
                    classLoader = new PluginClassLoader(pluginsDir);
                }
            }
        }
        return classLoader;
    }

    // GBH: made public for Ijx
    public static void setClassLoader(ClassLoader loader) {
        classLoader = loader;
    }

    /** Adds the specified class to a Vector to keep it from being garbage
    collected, which would cause the classes static fields to be reset.
    Probably not needed with Java 1.2 or later. */
    public static void register(Class c) {
        if (ijApp != null) {
            ijApp.register(c);
        }
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Keyboard Input ">
    /** Returns true if the space bar is down. */
    public static boolean spaceBarDown() {
        return spaceDown;
    }

    /** Returns true if the control key is down. */
    public static boolean controlKeyDown() {
        return controlDown;
    }

    /** Returns true if the alt key is down. */
    public static boolean altKeyDown() {
        return altDown;
    }

    /** Returns true if the shift key is down. */
    public static boolean shiftKeyDown() {
        return shiftDown;
    }

    public static void setKeyDown(int key) {
        if (debugMode) {
            IJ.log("setKeyDown: " + key);
        }
        switch (key) {
            case KeyEvent.VK_CONTROL:
                controlDown = true;
                break;
            case KeyEvent.VK_META:
                if (isMacintosh()) {
                    controlDown = true;
                }
                break;
            case KeyEvent.VK_ALT:
                altDown = true;
                break;
            case KeyEvent.VK_SHIFT:
                shiftDown = true;
                if (debugMode) {
                    beep();
                }
                break;
            case KeyEvent.VK_SPACE: {
                spaceDown = true;
                IjxWindow win = WindowManager.getCurrentWindow();
                if (win != null) {
                    if (win instanceof IjxImageWindow) {
                        ((IjxImageWindow) win).getCanvas().setCursor(-1, -1, -1, -1);
                    }
                }
                break;
            }
            case KeyEvent.VK_ESCAPE: {
                escapePressed = true;
                break;
            }
        }
    }

    public static void setKeyUp(int key) {
        if (debugMode) {
            IJ.log("setKeyUp: " + key);
        }
        switch (key) {
            case KeyEvent.VK_CONTROL:
                controlDown = false;
                break;
            case KeyEvent.VK_META:
                if (isMacintosh()) {
                    controlDown = false;
                }
                break;
            case KeyEvent.VK_ALT:
                altDown = false;
                break;
            case KeyEvent.VK_SHIFT:
                shiftDown = false;
                if (debugMode) {
                    beep();
                }
                break;
            case KeyEvent.VK_SPACE:
                spaceDown = false;
                IjxWindow win = WindowManager.getCurrentWindow();
                if (win != null) {
                    if (win instanceof IjxImageWindow) {
                        ((IjxImageWindow) win).getCanvas().setCursor(-1, -1, -1, -1);
                    }
                }
                break;
            case ALL_KEYS:
                shiftDown = controlDown = altDown = spaceDown = false;
                break;
        }
    }

    public static void setInputEvent(InputEvent e) {
        altDown = e.isAltDown();
        shiftDown = e.isShiftDown();
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Environment ">
    static {
        osname = System.getProperty("os.name");
        isWin = osname.startsWith("Windows");
        isMac = !isWin && osname.startsWith("Mac");
        isLinux = osname.startsWith("Linux");
        isVista = isWin && (osname.indexOf("Vista") != -1 || osname.indexOf(" 7") != -1);
        String version = System.getProperty("java.version").substring(0, 3);
        if (version.compareTo("2.9") <= 0) {  // JVM on Sharp Zaurus PDA claims to be "3.1"!
            isJava2 = version.compareTo("1.1") > 0;
            isJava14 = version.compareTo("1.3") > 0;
            isJava15 = version.compareTo("1.4") > 0;
            isJava16 = version.compareTo("1.5") > 0;
            isJava17 = version.compareTo("1.6") > 0;
        }
    }

    /** Returns the size, in pixels, of the primary display. */
    public static Dimension getScreenSize() {
        if (isWindows()) // GraphicsEnvironment.getConfigurations is *very* slow on Windows
        {
            return Toolkit.getDefaultToolkit().getScreenSize();
        }
        if (GraphicsEnvironment.isHeadless()) {
            return new Dimension(0, 0);
        }
        // Can't use Toolkit.getScreenSize() on Linux because it returns
        // size of all displays rather than just the primary display.
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        GraphicsConfiguration[] gc = gd[0].getConfigurations();
        Rectangle bounds = gc[0].getBounds();
        if (bounds.x == 0 && bounds.y == 0) {
            return new Dimension(bounds.width, bounds.height);
        } else {
            return Toolkit.getDefaultToolkit().getScreenSize();
        }
    }

    /**	Runs the garbage collector and returns a string something
    like "64K of 256MB (25%)" that shows how much of
    the  available memory is in use. This is the string
    displayed when the user clicks in the status bar. */
    public static String freeMemory() {
        long inUse = currentMemory();
        String inUseStr = inUse < 10000 * 1024 ? inUse / 1024L + "K" : inUse / 1048576L + "MB";
        String maxStr = "";
        long max = maxMemory();
        if (max > 0L) {
            double percent = inUse * 100 / max;
            maxStr = " of " + max / 1048576L + "MB (" + (percent < 1.0 ? "<1" : d2s(percent, 0)) + "%)";
        }
        return inUseStr + maxStr;
    }

    /** Returns the amount of memory currently being used by ImageJ. */
    public static long currentMemory() {
        long freeMem = Runtime.getRuntime().freeMemory();
        long totMem = Runtime.getRuntime().totalMemory();
        return totMem - freeMem;
    }

    /** Returns the maximum amount of memory available to ImageJ or
    zero if ImageJ is unable to determine this limit. */
    public static long maxMemory() {
        if (maxMemory == 0L) {
            Memory mem = new Memory();
            maxMemory = mem.getMemorySetting();
            if (maxMemory == 0L) {
                maxMemory = mem.maxMemory();
            }
        }
        return maxMemory;
    }

    /** Returns true if this machine is a Macintosh. */
    public static boolean isMacintosh() {
        return isMac;
    }

    /** Returns true if this machine is a Macintosh running OS X. */
    public static boolean isMacOSX() {
        return isMacintosh();
    }

    /** Returns true if this machine is running Windows. */
    public static boolean isWindows() {
        return isWin;
    }

    /** Always returns true. */
    public static boolean isJava2() {
        return isJava2;
    }

    /** Returns true if ImageJ is running on a Java 1.4 or greater JVM. */
    public static boolean isJava14() {
        return isJava14;
    }

    /** Returns true if ImageJ is running on a Java 1.5 or greater JVM. */
    public static boolean isJava15() {
        return isJava15;
    }

    /** Returns true if ImageJ is running on a Java 1.6 or greater JVM. */
    public static boolean isJava16() {
        return isJava16;
    }

    /** Returns true if ImageJ is running on a Java 1.7 or greater JVM. */
    public static boolean isJava17() {
        return isJava17;
    }

    /** Returns true if ImageJ is running on Linux. */
    public static boolean isLinux() {
        return isLinux;
    }

    /** Returns true if ImageJ is running on Windows Vista. */
    public static boolean isVista() {
        return isVista;
    }

    /** Returns true if ImageJ is running a 64-bit version of Java. */
    public static boolean is64Bit() {
        if (osarch == null) {
            osarch = System.getProperty("os.arch");
        }
        return osarch != null && osarch.indexOf("64") != -1;
    }

    /** Displays an error message and returns false if the
    ImageJ version is less than the one specified. */
    public static boolean versionLessThan(String version) {
        boolean lessThan = IJ.getInstance().getVersion().compareTo(version) < 0;
        if (lessThan) {
            error("This plugin or macro requires ImageJ " + version + " or later. Use\nHelp>Update ImageJ to upgrade to the latest version.");
        }
        return lessThan;
    }

    /** Returns the ImageJ version number as a string. */
    public static String getVersion() {
        return IJ.getInstance().getVersion();
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Drawing ">
    /** Displays a "Process all images?" dialog. Returns
    'flags'+PlugInFilter.DOES_STACKS if the user selects "Yes",
    'flags' if the user selects "No" and PlugInFilter.DONE
    if the user selects "Cancel".
     */
    public static int setupDialog(IjxImagePlus imp, int flags) {
        if (imp == null
                || (ijApp != null && CentralLookup.getDefault().lookup(KeyboardHandler.class).isHotkey())) {
            return flags;
        }
        int stackSize = imp.getStackSize();
        if (stackSize > 1) {
            if (imp.isComposite() && ((CompositeImage) imp).getMode() == CompositeImage.COMPOSITE) {
                return flags + PlugInFilter.DOES_STACKS;
            }
            String macroOptions = Macro.getOptions();
            if (macroOptions != null) {
                if (macroOptions.indexOf("stack ") >= 0) {
                    return flags + PlugInFilter.DOES_STACKS;
                } else {
                    return flags;
                }
            }
            if (hideProcessStackDialog) {
                return flags;
            }
            String note = ((flags & PlugInFilter.NO_CHANGES) == 0) ? " There is\nno Undo if you select \"Yes\"." : "";
            YesNoCancelDialog d = new YesNoCancelDialog(getTopComponentFrame(),
                    "Process Stack?", "Process all " + stackSize + " images?" + note);
            if (d.cancelPressed()) {
                return PlugInFilter.DONE;
            } else if (d.yesPressed()) {
                if (imp.getStack().isVirtual()) {
                    int size = (stackSize * imp.getWidth() * imp.getHeight() * imp.getBytesPerPixel() + 524288) / 1048576;
                    String msg =
                            "Custom code is required to process this virtual stack\n"
                            + "(e.g., \"Process Virtual Stack\" macro) or it must be\n"
                            + "converted to a normal stack using Image>Duplicate,\n"
                            + "which will require " + size + "MB of additional memory.";
                    error(msg);
                    return PlugInFilter.DONE;
                }
                if (Recorder.record) {
                    Recorder.recordOption("stack");
                }
                return flags + PlugInFilter.DOES_STACKS;
            }
            if (Recorder.record) {
                Recorder.recordOption("slice");
            }
        }
        return flags;
    }

    /** Creates a rectangular selection. Removes any existing
    selection if width or height are less than 1. */
    public static void makeRectangle(int x, int y, int width, int height) {
        if (width <= 0 || height < 0) {
            getImage().killRoi();
        } else {
            IjxImagePlus img = getImage();
            if (Interpreter.isBatchMode()) {
                img.setRoi(new Roi(x, y, width, height), false);
            } else {
                img.setRoi(x, y, width, height);
            }
        }
    }

    /** Creates an elliptical selection. Removes any existing
    selection if width or height are less than 1. */
    public static void makeOval(int x, int y, int width, int height) {
        if (width <= 0 || height < 0) {
            getImage().killRoi();
        } else {
            IjxImagePlus img = getImage();
            img.setRoi(new OvalRoi(x, y, width, height));
        }
    }

    /** Creates a straight line selection. */
    public static void makeLine(int x1, int y1, int x2, int y2) {
        getImage().setRoi(new Line(x1, y1, x2, y2));
    }

    /** Creates a point selection. */
    public static void makePoint(int x, int y) {
        IjxImagePlus img = getImage();
        Roi roi = img.getRoi();
        if (shiftKeyDown() && roi != null && roi.getType() == Roi.POINT) {
            Polygon p = roi.getPolygon();
            p.addPoint(x, y);
            img.setRoi(new PointRoi(p.xpoints, p.ypoints, p.npoints));
        } else {
            img.setRoi(new PointRoi(x, y));
        }
    }

    /** Creates a straight line selection using double coordinates. */
    public static void makeLine(double x1, double y1, double x2, double y2) {
        getImage().setRoi(new Line(x1, y1, x2, y2));
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Threshold ">
    /** Sets the minimum and maximum displayed pixel values. */
    public static void setMinAndMax(double min, double max) {
        setMinAndMax(min, max, 7);
    }

    /** Sets the minimum and maximum displayed pixel values on the specified RGB
    channels, where 4=red, 2=green and 1=blue. */
    public static void setMinAndMax(double min, double max, int channels) {
        IjxImagePlus img = getImage();
        Calibration cal = img.getCalibration();
        min = cal.getRawValue(min);
        max = cal.getRawValue(max);
        if (channels == 7) {
            img.setDisplayRange(min, max);
        } else {
            img.setDisplayRange(min, max, channels);
        }
        img.updateAndDraw();
    }

    /** Resets the minimum and maximum displayed pixel values
    to be the same as the min and max pixel values. */
    public static void resetMinAndMax() {
        IjxImagePlus img = getImage();
        img.resetDisplayRange();
        img.updateAndDraw();
    }

    /** Sets the lower and upper threshold levels and displays the image
    using red to highlight thresholded pixels. May not work correctly on
    16 and 32 bit images unless the display range has been reset using IJ.resetMinAndMax().
     */
    public static void setThreshold(double lowerThreshold, double upperThresold) {
        setThreshold(lowerThreshold, upperThresold, null);
    }

    /** Sets the lower and upper threshold levels and displays the image using
    the specified <code>displayMode</code> ("Red", "Black & White", "Over/Under" or "No Update"). */
    public static void setThreshold(double lowerThreshold, double upperThreshold, String displayMode) {
        setThreshold(getImage(), lowerThreshold, upperThreshold, displayMode);
    }

    /** Sets the lower and upper threshold levels of the specified image. */
    public static void setThreshold(IjxImagePlus img, double lowerThreshold, double upperThreshold) {
        setThreshold(img, lowerThreshold, upperThreshold, "Red");
    }

    /** Sets the lower and upper threshold levels of the specified image and updates the display using
    the specified <code>displayMode</code> ("Red", "Black & White", "Over/Under" or "No Update"). */
    public static void setThreshold(IjxImagePlus img, double lowerThreshold, double upperThreshold, String displayMode) {
        int mode = ImageProcessor.RED_LUT;
        if (displayMode != null) {
            displayMode = displayMode.toLowerCase(Locale.US);
            if (displayMode.indexOf("black") != -1) {
                mode = ImageProcessor.BLACK_AND_WHITE_LUT;
            } else if (displayMode.indexOf("over") != -1) {
                mode = ImageProcessor.OVER_UNDER_LUT;
            } else if (displayMode.indexOf("no") != -1) {
                mode = ImageProcessor.NO_LUT_UPDATE;
            }
        }
        Calibration cal = img.getCalibration();
        lowerThreshold = cal.getRawValue(lowerThreshold);
        upperThreshold = cal.getRawValue(upperThreshold);
        img.getProcessor().setThreshold(lowerThreshold, upperThreshold, mode);
        if (mode != ImageProcessor.NO_LUT_UPDATE) {
            img.getProcessor().setLutAnimation(true);
            img.updateAndDraw();
        }
    }

    public static void setAutoThreshold(IjxImagePlus img, String method) {
        ImageProcessor ip = img.getProcessor();
        if (ip instanceof ColorProcessor) {
            throw new IllegalArgumentException("Non-RGB image required");
        }
        ip.setRoi(img.getRoi());
        if (method != null) {
            try {
                ip.setAutoThreshold(method);
            } catch (Exception e) {
                IJ.log(e.getMessage());
            }
        } else {
            ip.setAutoThreshold(ImageProcessor.ISODATA2, ImageProcessor.RED_LUT);
        }
        img.updateAndDraw();
    }

    /** Disables thresholding on the current image. */
    public static void resetThreshold() {
        resetThreshold(getImage());
    }

    /** Disables thresholding on the specified image. */
    public static void resetThreshold(IjxImagePlus img) {
        ImageProcessor ip = img.getProcessor();
        ip.resetThreshold();
        ip.setLutAnimation(true);
        img.updateAndDraw();
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Selection of Image / Slice / Window ">
    /** Returns a reference to the active image. Displays an error
    message and aborts the macro if no images are open. */
    public static IjxImagePlus getImage() {  //ts
        IjxImagePlus img = WindowManager.getCurrentImage();
        if (img == null) {
            IJ.noImage();
            if (ijApp == null) {
                System.exit(0);
            } else {
                abort();
            }
        }
        return img;
    }

    /** Switches to the specified stack slice, where 1<='slice'<=stack-size. */
    public static void setSlice(int slice) {
        getImage().setSlice(slice);
    }

    /** For IDs less than zero, activates the image with the specified ID.
    For IDs greater than zero, activates the Nth image. */
    public static void selectWindow(int id) {
        if (id > 0) {
            id = WindowManager.getNthImageID(id);
        }
        IjxImagePlus imp = WindowManager.getImage(id);
        if (imp == null) {
            error("Macro Error", "Image " + id + " not found or no images are open.");
        }
        if (Interpreter.isBatchMode()) {
            IjxImagePlus imp2 = WindowManager.getCurrentImage();
            if (imp2 != null && imp2 != imp) {
                imp2.saveRoi();
            }
            WindowManager.setTempCurrentImage(imp);
            WindowManager.setWindow(null);
        } else {
            IjxImageWindow win = imp.getWindow();
            win.toFront();
            WindowManager.setWindow(win);
            long start = System.currentTimeMillis();
            // timeout after 2 seconds unless current thread is event dispatch thread
            String thread = Thread.currentThread().getName();
            int timeout = thread != null && thread.indexOf("EventQueue") != -1 ? 0 : 2000;
            while (true) {
                wait(10);
                imp = WindowManager.getCurrentImage();
                if (imp != null && imp.getID() == id) {
                    return; // specified image is now active
                }
                if ((System.currentTimeMillis() - start) > timeout) {
                    WindowManager.setCurrentWindow(win);
                    return;
                }
            }
        }
    }

    /** Activates the window with the specified TITLE. */
    public static void selectWindow(String title) {
        if (title.equals("ImageJ") && IJ.getTopComponentFrame() != null) {
            IJ.getTopComponentFrame().toFront();
            return;
        }
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 3000) { // 3 sec timeout
            IjxWindow frame = WindowManager.getFrame(title);
            if (frame != null && !(frame instanceof IjxImageWindow)) {
                selectWindow(frame);
                return;
            }
            int[] wList = WindowManager.getIDList();
            int len = wList != null ? wList.length : 0;
            for (int i = 0; i < len; i++) {
                IjxImagePlus imp = WindowManager.getImage(wList[i]);
                if (imp != null) {
                    if (imp.getTitle().equals(title)) {
                        selectWindow(imp.getID());
                        return;
                    }
                }
            }
            wait(10);
        }
        error("Macro Error", "No window with the title \"" + title + "\" found.");
    }

    static void selectWindow(IjxWindow frame) {
        frame.toFront();
        long start = System.currentTimeMillis();
        while (true) {
            wait(10);
            if (WindowManager.getFrontWindow() == frame) {
                return; // specified window is now in front
            }
            if ((System.currentTimeMillis() - start) > 1000) {
                WindowManager.setWindow(frame);
                return;   // 1 second timeout
            }
        }
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Color Set/Get (global, ui)">
// @todo - Add corresponding getColor methods; don't store it in Toolbar.
    /** Sets the foreground color. */
    public static void setForegroundColor(int red, int green, int blue) {
        setColor(red, green, blue, true);
    }

    /** Sets the background color. */
    public static void setBackgroundColor(int red, int green, int blue) {
        setColor(red, green, blue, false);
    }

    static void setColor(int red, int green, int blue, boolean foreground) {
        if (red < 0) {
            red = 0;
        }
        if (green < 0) {
            green = 0;
        }
        if (blue < 0) {
            blue = 0;
        }
        if (red > 255) {
            red = 255;
        }
        if (green > 255) {
            green = 255;
        }
        if (blue > 255) {
            blue = 255;
        }
        Color c = new Color(red, green, blue);
        if (foreground) {
            ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).setForegroundColor(c);
            IjxImagePlus img = WindowManager.getCurrentImage();
            if (img != null) {
                img.getProcessor().setColor(c);
            }
        } else {
            ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).setBackgroundColor(c);
        }
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Tools / Modes">
    /** Switches to the specified tool, where id = Toolbar.RECTANGLE (0),
    Toolbar.OVAL (1), etc. */
    public static void setTool(int id) {
        ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).setTool(id);
    }

    /** Switches to the specified tool, where 'name' is "rect", "elliptical",
    "brush", etc. Returns 'false' if the name is not recognized. */
    public static boolean setTool(String name) {
        return ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).setTool(name);
    }

    /** Returns the name of the current tool. */
    public static String getToolName() {
        return ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).getToolName();
    }

    /** Equivalent to clicking on the current image at (x,y) with the
    wand tool. Returns the number of points in the resulting ROI. */
    public static int doWand(int x, int y) {
        return doWand(x, y, 0, null);
    }

    /** Traces the boundary of the area with pixel values within
     * 'tolerance' of the value of the pixel at the starting location.
     * 'tolerance' is in uncalibrated units.
     * 'mode' can be "4-connected", "8-connected" or "Legacy".
     * "Legacy" is for compatibility with previous versions of ImageJ;
     * it is ignored if 'tolerance' > 0.
     */
    public static int doWand(int x, int y, double tolerance, String mode) {
        IjxImagePlus img = getImage();
        ImageProcessor ip = img.getProcessor();
        if ((img.getType() == IjxImagePlus.GRAY32) && Double.isNaN(ip.getPixelValue(x, y))) {
            return 0;
        }
        int imode = Wand.LEGACY_MODE;
        if (mode != null) {
            if (mode.startsWith("4")) {
                imode = Wand.FOUR_CONNECTED;
            } else if (mode.startsWith("8")) {
                imode = Wand.EIGHT_CONNECTED;
            }
        }
        Wand w = new Wand(ip);
        double t1 = ip.getMinThreshold();
        if (t1 == ImageProcessor.NO_THRESHOLD || (ip.getLutUpdateMode() == ImageProcessor.NO_LUT_UPDATE && tolerance > 0.0)) {
            w.autoOutline(x, y, tolerance, imode);
        } else {
            w.autoOutline(x, y, t1, ip.getMaxThreshold(), imode);
        }
        if (w.npoints > 0) {
            Roi previousRoi = img.getRoi();
            int type = Wand.allPoints() ? Roi.FREEROI : Roi.TRACED_ROI;
            Roi roi = new PolygonRoi(w.xpoints, w.ypoints, w.npoints, type);
            img.killRoi();
            img.setRoi(roi);
            // add/subtract this ROI to the previous one if the shift/alt key is down
            if (previousRoi != null) {
                roi.update(shiftKeyDown(), altKeyDown());
            }
        }
        return w.npoints;
    }

    /** Sets the transfer mode used by the <i>Edit/Paste</i> command, where mode is "Copy", "Blend", "Average", "Difference",
    "Transparent", "Transparent2", "AND", "OR", "XOR", "Add", "Subtract", "Multiply", or "Divide". */
    public static void setPasteMode(String mode) {
        mode = mode.toLowerCase(Locale.US);
        int m = Blitter.COPY;
        if (mode.startsWith("ble") || mode.startsWith("ave")) {
            m = Blitter.AVERAGE;
        } else if (mode.startsWith("diff")) {
            m = Blitter.DIFFERENCE;
        } else if (mode.indexOf("zero") != -1) {
            m = Blitter.COPY_ZERO_TRANSPARENT;
        } else if (mode.startsWith("tran")) {
            m = Blitter.COPY_TRANSPARENT;
        } else if (mode.startsWith("and")) {
            m = Blitter.AND;
        } else if (mode.startsWith("or")) {
            m = Blitter.OR;
        } else if (mode.startsWith("xor")) {
            m = Blitter.XOR;
        } else if (mode.startsWith("sub")) {
            m = Blitter.SUBTRACT;
        } else if (mode.startsWith("add")) {
            m = Blitter.ADD;
        } else if (mode.startsWith("div")) {
            m = Blitter.DIVIDE;
        } else if (mode.startsWith("mul")) {
            m = Blitter.MULTIPLY;
        } else if (mode.startsWith("min")) {
            m = Blitter.MIN;
        } else if (mode.startsWith("max")) {
            m = Blitter.MAX;
        }
        Roi.setPasteMode(m);
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" File / Directory Utils ">
    /** Returns the path to the home ("user.home"), startup, ImageJ, plugins, macros,
    luts, temp, current or image directory if <code>TITLE</code> is "home", "startup",
    "imagej", "plugins", "macros", "luts", "temp", "current" or "image", otherwise,
    displays a dialog and returns the path to the directory selected by the user.
    Returns null if the specified directory is not found or the user
    cancels the dialog box. Also aborts the macro if the user cancels
    the dialog box.*/
    public static String getDirectory(String title) {
        if (title.equals("plugins")) {
            return Menus.getPlugInsPath();
        } else if (title.equals("macros")) {
            return Menus.getMacrosPath();
        } else if (title.equals("luts")) {
            String ijdir = getIJDir();
            if (ijdir != null) {
                return ijdir + "luts" + File.separator;
            } else {
                return null;
            }
        } else if (title.equals("home")) {
            return System.getProperty("user.home") + File.separator;
        } else if (title.equals("startup")) {
            return Prefs.getHomeDir() + File.separator;
        } else if (title.equals("imagej")) {
            return getIJDir();
        } else if (title.equals("current")) {
            return OpenDialog.getDefaultDirectory();
        } else if (title.equals("temp")) {
            String dir = System.getProperty("java.io.tmpdir");
            if (isMacintosh()) {
                dir = "/tmp/";
            }
            if (dir != null && !dir.endsWith(File.separator)) {
                dir += File.separator;
            }
            return dir;
        } else if (title.equals("image")) {
            IjxImagePlus imp = WindowManager.getCurrentImage();
            FileInfo fi = imp != null ? imp.getOriginalFileInfo() : null;
            if (fi != null && fi.directory != null) {
                return fi.directory;
            } else {
                return null;
            }
        } else {
            DirectoryChooser dc = new DirectoryChooser(title);
            String dir = dc.getDirectory();
            if (dir == null) {
                Macro.abort();
            }
            return dir;
        }
    }

    private static String getIJDir() {
        String path = Menus.getPlugInsPath();
        if (path == null) {
            return null;
        }
        String ijdir = (new File(path)).getParent();
        if (ijdir != null) {
            ijdir += File.separator;
        }
        return ijdir;
    }

    static String updateExtension(String path, String extension) {
        if (path == null) {
            return null;
        }
        int dotIndex = path.lastIndexOf(".");
        int separatorIndex = path.lastIndexOf(File.separator);
        if (dotIndex >= 0 && dotIndex > separatorIndex && (path.length() - dotIndex) <= 5) {
            if (dotIndex + 1 < path.length() && Character.isDigit(path.charAt(dotIndex + 1))) {
                path += extension;
            } else {
                path = path.substring(0, dotIndex) + extension;
            }
        } else {
            path += extension;
        }
        return path;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Image create / open / save ">
    /** Creates a new IjxImagePlus. <code>Type</code> should contain "8-bit", "16-bit", "32-bit" or "RGB".
    In addition, it can contain "white", "black" or "ramp" (the default is "white"). <code>Width</code>
    and <code>height</code> specify the width and height of the image in pixels.
    <code>Depth</code> specifies the number of stack slices. */
    public static IjxImagePlus createImage(String title, String type, int width, int height, int depth) {
        type = type.toLowerCase(Locale.US);
        int bitDepth = 8;
        if (type.indexOf("16") != -1) {
            bitDepth = 16;
        }
        if (type.indexOf("24") != -1 || type.indexOf("rgb") != -1) {
            bitDepth = 24;
        }
        if (type.indexOf("32") != -1) {
            bitDepth = 32;
        }
        int options = NewImage.FILL_WHITE;
        if (bitDepth == 16 || bitDepth == 32) {
            options = NewImage.FILL_BLACK;
        }
        if (type.indexOf("white") != -1) {
            options = NewImage.FILL_WHITE;
        } else if (type.indexOf("black") != -1) {
            options = NewImage.FILL_BLACK;
        } else if (type.indexOf("ramp") != -1) {
            options = NewImage.FILL_RAMP;
        }
        options += NewImage.CHECK_AVAILABLE_MEMORY;
        return NewImage.createImage(title, width, height, depth, bitDepth, options);
    }

    /** Opens a new image. <code>Type</code> should contain "8-bit", "16-bit", "32-bit" or "RGB".
    In addition, it can contain "white", "black" or "ramp" (the default is "white"). <code>Width</code>
    and <code>height</code> specify the width and height of the image in pixels.
    <code>Depth</code> specifies the number of stack slices. */
    public static void newImage(String title, String type, int width, int height, int depth) {
        IjxImagePlus imp = createImage(title, type, width, height, depth);
        if (imp != null) {
            macroRunning = true;
            imp.show();
            macroRunning = false;
        }
    }

    /** Displays a file open dialog box and then opens the tiff, dicom,
    fits, pgm, jpeg, bmp, gif, lut, roi, or text file selected by
    the user. Displays an error message if the selected file is not
    in one of the supported formats, or if it is not found. */
    public static void open() {
        open(null);
    }

    /** Opens and displays a tiff, dicom, fits, pgm, jpeg, bmp, gif, lut,
    roi, or text file. Displays an error message if the specified file
    is not in one of the supported formats, or if it is not found.
    With 1.41k or later, opens images specified by a URL.
     */
    public static void open(String path) {
        if (ijApp == null && Menus.getCommands() == null) {
            init();
        }
        Opener o = new Opener();
        macroRunning = true;
        if (path == null || path.equals("")) {
            o.open();
        } else {
            o.open(path);
        }
        macroRunning = false;
    }

    /** Opens and displays the nth image in the specified tiff stack. */
    public static void open(String path, int n) {
        if (ijApp == null && Menus.getCommands() == null) {
            init();
        }
        IjxImagePlus imp = openImage(path, n);
        if (imp != null) {
            imp.show();
        }
    }

    /** Opens the specified file as a tiff, bmp, dicom, fits, pgm, gif
    or jpeg image and returns an IjxImagePlus object if successful.
    Calls HandleExtraFileTypes plugin if the file type is not recognised.
    Displays a file open dialog if 'path' is null or an empty string.
    Note that 'path' can also be a URL. Some reader plugins, including
    the Bio-Formats plugin, display the image and return null. */
    public static IjxImagePlus openImage(String path) {
        return (new Opener()).openImage(path);
    }

    /** Opens the nth image of the specified tiff stack. */
    public static IjxImagePlus openImage(String path, int n) {
        return (new Opener()).openImage(path, n);
    }

    /** Opens an image using a file open dialog and returns it as an IjxImagePlus object. */
    public static IjxImagePlus openImage() {
        return openImage(null);
    }

    /** Saves the current image, lookup table, selection or text window to the specified file path.
    The path must end in ".tif", ".jpg", ".gif", ".zip", ".raw", ".avi", ".bmp", ".fits", ".pgm", ".png", ".lut", ".roi" or ".txt".  */
    public static void save(String path) {
        save(null, path);
    }

    /** Saves the specified image, lookup table or selection to the specified file path.
    The path must end in ".tif", ".jpg", ".gif", ".zip", ".raw", ".avi", ".bmp", ".fits", ".pgm", ".png", ".lut", ".roi" or ".txt".  */
    public static void save(IjxImagePlus imp, String path) {
        int dotLoc = path.lastIndexOf('.');
        if (dotLoc != -1) {
            IjxImagePlus imp2 = imp;
            if (imp2 == null) {
                imp2 = WindowManager.getCurrentImage();
            }
            String title = imp2 != null ? imp2.getTitle() : null;
            saveAs(imp, path.substring(dotLoc + 1), path);
            if (title != null) {
                imp2.setTitle(title);
            }
        } else {
            error("The save() macro function requires a file name extension.\n \n" + path);
        }
    }

    /* Saves the active image, lookup table, selection, measurement results, selection XY
    coordinates or text window to the specified file path. The format argument must be "tiff",
    "jpeg", "gif", "zip", "raw", "avi", "bmp", "fits", "pgm", "png", "text image", "lut", "selection", "measurements",
    "xy Coordinates" or "text".  If <code>path</code> is null or an emply string, a file
    save dialog is displayed. */
    public static void saveAs(String format, String path) {
        saveAs(null, format, path);
    }

    /* Saves the specified image. The format argument must be "tiff",
    "jpeg", "gif", "zip", "raw", "avi", "bmp", "fits", "pgm", "png",
    "text image", "lut", "selection" or "xy Coordinates". */
    public static void saveAs(IjxImagePlus imp, String format, String path) {
        if (format == null) {
            return;
        }
        if (path != null && path.length() == 0) {
            path = null;
        }
        format = format.toLowerCase(Locale.US);
        if (format.indexOf("tif") != -1) {
            path = updateExtension(path, ".tif");
            format = "Tiff...";
        } else if (format.indexOf("jpeg") != -1 || format.indexOf("jpg") != -1) {
            path = updateExtension(path, ".jpg");
            format = "Jpeg...";
        } else if (format.indexOf("gif") != -1) {
            path = updateExtension(path, ".gif");
            format = "Gif...";
        } else if (format.indexOf("text image") != -1) {
            path = updateExtension(path, ".txt");
            format = "Text Image...";
        } else if (format.indexOf("text") != -1 || format.indexOf("txt") != -1) {
            if (path != null && !path.endsWith(".xls")) {
                path = updateExtension(path, ".txt");
            }
            format = "Text...";
        } else if (format.indexOf("zip") != -1) {
            path = updateExtension(path, ".zip");
            format = "ZIP...";
        } else if (format.indexOf("raw") != -1) {
            path = updateExtension(path, ".raw");
            format = "Raw Data...";
        } else if (format.indexOf("avi") != -1) {
            path = updateExtension(path, ".avi");
            format = "AVI... ";
        } else if (format.indexOf("bmp") != -1) {
            path = updateExtension(path, ".bmp");
            format = "BMP...";
        } else if (format.indexOf("fits") != -1) {
            path = updateExtension(path, ".fits");
            format = "FITS...";
        } else if (format.indexOf("png") != -1) {
            path = updateExtension(path, ".png");
            format = "PNG...";
        } else if (format.indexOf("pgm") != -1) {
            path = updateExtension(path, ".pgm");
            format = "PGM...";
        } else if (format.indexOf("lut") != -1) {
            path = updateExtension(path, ".lut");
            format = "LUT...";
        } else if (format.indexOf("results") != -1 || format.indexOf("measurements") != -1) {
            format = "Results...";
        } else if (format.indexOf("selection") != -1 || format.indexOf("roi") != -1) {
            path = updateExtension(path, ".roi");
            format = "Selection...";
        } else if (format.indexOf("xy") != -1 || format.indexOf("coordinates") != -1) {
            path = updateExtension(path, ".txt");
            format = "XY Coordinates...";
        } else {
            error("Unrecognized format: " + format);
        }
        if (path == null) {
            run(format);
        } else {
            run(imp, format, "save=[" + path + "]");
        }
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc=" String/Text File Utils ">
    /** Opens a URL and returns the contents as a string.
    Returns "<Error: message>" if there an error, including
    host or file not found. */
    public static String openUrlAsString(String url) {
        StringBuffer sb = null;
        url = url.replaceAll(" ", "%20");
        try {
            URL u = new URL(url);
            URLConnection uc = u.openConnection();
            long len = uc.getContentLength();
            if (len > 1048576L) {
                return "<Error: file is larger than 1MB>";
            }
            InputStream in = u.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            in.close();
        } catch (Exception e) {
            return ("<Error: " + e + ">");
        }
        if (sb != null) {
            return new String(sb);
        } else {
            return "";
        }
    }

    /** Saves a string as a file. Displays a file save dialog if
    'path' is null or blank. Returns an error message
    if there is an exception, otherwise returns null. */
    public static String saveString(String string, String path) {
        return write(string, path, false);
    }

    /** Appends a string to the end of a file. A newline character ("\n")
    is added to the end of the string before it is written. Returns an
    error message if there is an exception, otherwise returns null. */
    public static String append(String string, String path) {
        return write(string + "\n", path, true);
    }

    private static String write(String string, String path, boolean append) {
        if (path == null || path.equals("")) {
            String msg = append ? "Append String..." : "Save String...";
            SaveDialog sd = new SaveDialog(msg, "Untitled", ".txt");
            String name = sd.getFileName();
            if (name == null) {
                return null;
            }
            path = sd.getDirectory() + name;
        }
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path, append));
            out.write(string);
            out.close();
        } catch (IOException e) {
            return "" + e;
        }
        return null;
    }

    /** Opens a text file as a string. Displays a file open dialog
    if path is null or blank. Returns null if the user cancels
    the file open dialog. If there is an error, returns a
    message in the form "Error: message". */
    public static String openAsString(String path) {
        if (path == null || path.equals("")) {
            OpenDialog od = new OpenDialog("Open Text File", "");
            String directory = od.getDirectory();
            String name = od.getFileName();
            if (name == null) {
                return null;
            }
            path = directory + name;
        }
        String str = "";
        File file = new File(path);
        if (!file.exists()) {
            return "Error: file not found";
        }
        try {
            StringBuffer sb = new StringBuffer(5000);
            BufferedReader r = new BufferedReader(new FileReader(file));
            while (true) {
                String s = r.readLine();
                if (s == null) {
                    break;
                } else {
                    sb.append(s + "\n");
                }
            }
            r.close();
            str = new String(sb);
        } catch (Exception e) {
            str = "Error: " + e.getMessage();
        }
        return str;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Abort / Esc ">
    /** Returns true if the <code>Esc</code> key was pressed since the
    last ImageJ command started to execute or since resetEscape() was called. */
    public static boolean escapePressed() {
        return escapePressed;
    }

    /** This method sets the <code>Esc</code> key to the "up" position.
    The Executer class calls this method when it runs
    an ImageJ command in a separate thread. */
    public static void resetEscape() {
        escapePressed = false;
    }

    private static void testAbort() {
        if (Macro.abort) {
            abort();
        }
    }

    public static void abort() {
        if (ijApp != null || Interpreter.isBatchMode()) {
            throw new RuntimeException(Macro.MACRO_CANCELED);
        }
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Error & Exception Handling ">
    /** Causes IJ.error() output to be temporarily redirected to the "Log" window. */
    public static void redirectErrorMessages() {
        redirectErrorMessages = true;
    }

    /** Set 'true' and IJ.error() output will be redirected to the "Log" window. */
    public static void redirectErrorMessages(boolean redirect) {
        redirectErrorMessages2 = redirect;
    }

    /** Returns the state of the  'redirectErrorMessages' flag. The File/Import/Image Sequence command sets this flag.*/
    public static boolean redirectingErrorMessages() {
        return redirectErrorMessages || redirectErrorMessages2;
    }

    /** Temporarily suppress "plugin not found" errors. */
    public static void suppressPluginNotFoundError() {
        suppressPluginNotFoundError = true;
    }

    /** Displays a stack trace. Use the setExceptionHandler
    method() to override with a custom exception handler. */
    public static void handleException(Throwable e) {
        if (exceptionHandler != null) {
            exceptionHandler.handle(e);
            return;
        }
        CharArrayWriter caw = new CharArrayWriter();
        PrintWriter pw = new PrintWriter(caw);
        e.printStackTrace(pw);
        e.printStackTrace();
        String s = caw.toString();
        if (getInstance() != null) {
            new TextWindow("Exception", s, 350, 250);
        } else {
            log(s);
        }
    }

    /** Installs a custom exception handler that
    overrides the handleException() method. */
    public static void setExceptionHandler(ExceptionHandler handler) {
        exceptionHandler = handler;
    }

    public interface ExceptionHandler {

        public void handle(Throwable e);
    }
    static ExceptionHandler exceptionHandler;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Listeners ">
    public static Vector getListeners() {
        return eventListeners;
    }

    public static void addEventListener(IJEventListener listener) {
        eventListeners.addElement(listener);
    }

    public static void removeEventListener(IJEventListener listener) {
        eventListeners.removeElement(listener);
    }

    public static void notifyEventListeners(int eventID) {
        synchronized (eventListeners) {
            for (int i = 0; i < eventListeners.size(); i++) {
                IJEventListener listener = (IJEventListener) eventListeners.elementAt(i);
                listener.eventOccurred(eventID);
            }
        }
    }

// </editor-fold>
    public static boolean runFijiEditor(String title, String body) {
        try {
            Class clazz = IJ.getClassLoader().loadClass("fiji.scripting.TextEditor");
            Frame frame = (Frame) clazz.getConstructor(new Class[]{
                        String.class, String.class}).newInstance(new Object[]{title, body});
            frame.setVisible(true);
            return true;
        } catch (Exception e) {
            IJ.handleException(e); /* ignore */ }
        return false;
    }
}
