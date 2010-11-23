package ijx.plugin;

import ijx.plugin.api.PlugIn;
import ijx.gui.dialog.GenericDialog;
import ijx.io.OpenDialog;
import ijx.Menus;
import ijx.Macro;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;
import java.awt.*;
import java.io.*;
import java.util.*;

import ijx.plugin.frame.Editor;
import ijx.IjxImagePlus;
import ijx.app.IjxApplication;
import ijx.sezpoz.ActionIjx;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Compiles and runs plugins using the javac compiler. */
public class Compiler implements PlugIn, FilenameFilter {
    //==========================================================
    @ActionIjx(label = "Compiler...",
               menu = "Edit>Options",
               commandKey = "compiler.options")
    public static final ActionListener MAC = callWithArg("compiler.options", "options");

    private static ActionListener callWithArg(final String commandKey, final String arg) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IJ.runUserPlugIn(commandKey, "ij.plugin.Compiler", arg, false);
            }
        };
    }
    //========================================================
    private static final int TARGET14 = 0, TARGET15 = 1, TARGET16 = 2, TARGET17 = 3;
    private static final String[] targets = {"1.4", "1.5", "1.6", "1.7"};
    private static final String TARGET_KEY = "javac.target";
    private static com.sun.tools.javac.Main javac;
    private static ByteArrayOutputStream output;
    private static String dir, name;
    private static Editor errors;
    private static boolean generateDebuggingInfo;
    private static int target = (int) Prefs.get(TARGET_KEY, TARGET15);

    public void run(String arg) {
        if (arg.equals("edit")) {
            edit();
        } else if (arg.equals("options")) {
            showDialog();
        } else {
            compileAndRun(arg);
        }
    }

    void edit() {
        if (open("", "Open macro or plugin")) {
            Editor ed = (Editor) IJ.runPlugIn("ij.plugin.frame.Editor", "");
            if (ed != null) {
                ed.open(dir, name);
            }
        }
    }

    void compileAndRun(String path) {
        if (!open(path, "Compile and Run Plugin...")) {
            return;
        }
        if (name.endsWith(".class")) {
            runPlugin(name.substring(0, name.length() - 1));
            return;
        }
        if (!isJavac()) {
            return;
        }
        if (compile(dir + name)) {
            runPlugin(name);
        }
    }

    boolean isJavac() {
        try {
            if (javac == null) {
                output = new ByteArrayOutputStream(4096);
                javac = new com.sun.tools.javac.Main();
            }
        } catch (NoClassDefFoundError e) {
            IJ.error("Unable to find the javac compiler, which comes with the Windows and \n"
                    + "Linux versions of ImageJ that include Java in the ImageJ/jre folder.\n"
                    + " \n"
                    + "   java.home: " + System.getProperty("java.home"));
            return false;
        }
        return true;
    }

    boolean compile(String path) {
        IJ.showStatus("compiling: " + path);
        output.reset();
        String classpath = getClassPath(path);
        Vector v = new Vector();
        if (generateDebuggingInfo) {
            v.addElement("-g");
        }
        if (IJ.isJava15()) {
            validateTarget();
            v.addElement("-source");
            v.addElement(targets[target]);
            v.addElement("-target");
            v.addElement(targets[target]);
            v.addElement("-Xlint:unchecked");
        }
        v.addElement("-deprecation");
        v.addElement("-classpath");
        v.addElement(classpath);
        v.addElement(path);
        String[] arguments = new String[v.size()];
        v.copyInto((String[]) arguments);
        if (IJ.debugMode) {
            String str = "javac";
            for (int i = 0; i < arguments.length; i++) {
                str += " " + arguments[i];
            }
            IJ.log(str);
        }
        boolean compiled = javac.compile(arguments, new PrintWriter(output)) == 0;
        String s = output.toString();
        boolean errors = (!compiled || areErrors(s));
        if (errors) {
            showErrors(s);
        } else {
            IJ.showStatus("done");
        }
        return compiled;
    }

    // Returns a string containing the Java classpath,
    // the path to the directory containing the plugin,
    // and paths to any .jar files in the plugins folder.
    String getClassPath(String path) {
        long start = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        sb.append(System.getProperty("java.class.path"));
        File f = new File(path);
        if (f != null) // add directory containing file to classpath
        {
            sb.append(File.pathSeparator + f.getParent());
        }
        String pluginsDir = Menus.getPlugInsPath();
        if (pluginsDir != null) {
            addJars(pluginsDir, sb);
        }
        return sb.toString();
    }

    // Adds .jar files in plugins folder, and subfolders, to the classpath
    void addJars(String path, StringBuffer sb) {
        String[] list = null;
        File f = new File(path);
        if (f.exists() && f.isDirectory()) {
            list = f.list();
        }
        if (list == null) {
            return;
        }
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        for (int i = 0; i < list.length; i++) {
            File f2 = new File(path + list[i]);
            if (f2.isDirectory()) {
                addJars(path + list[i], sb);
            } else if (list[i].endsWith(".jar") && (list[i].indexOf("_") == -1 || list[i].equals("loci_tools.jar"))) {
                sb.append(File.pathSeparator + path + list[i]);
                if (IJ.debugMode) {
                    IJ.log("javac: " + path + list[i]);
                }
            }
        }
    }

    public static boolean compileFile(String path) {
        Compiler compiler = new Compiler();
        if (!compiler.isJavac()) {
            return false;
        }
        return compiler.compile(path);

    }

    boolean areErrors(String s) {
        boolean errors = s != null && s.length() > 0;
        if (errors && s.indexOf("1 warning") > 0 && s.indexOf("[deprecation] show()") > 0) {
            errors = false;
        }
        //if(errors&&s.startsWith("Note:com.sun.tools.javac")&&s.indexOf("error")==-1)
        //	errors = false;
        return errors;
    }

    void showErrors(String s) {
        if (errors == null || !errors.isVisible()) {
            errors = (Editor) IJ.runPlugIn("ij.plugin.frame.Editor", "");
            errors.setFont(new Font("Monospaced", Font.PLAIN, 12));
        }
        if (errors != null) {
            errors.display("Errors", s);
        }
        IJ.showStatus("done (errors)");
    }

    // open the .java source file
    boolean open(String path, String msg) {
        boolean okay;
        String fileName, directory;
        if (path.equals("")) {
            if (dir == null) {
                dir = IJ.getDirectory("plugins");
            }
            OpenDialog od = new OpenDialog(msg, dir, name);
            directory = od.getDirectory();
            fileName = od.getFileName();
            okay = fileName != null;
            String lcName = okay ? fileName.toLowerCase(Locale.US) : null;
            if (okay) {
                if (msg.startsWith("Compile")) {
                    if (!(lcName.endsWith(".java") || lcName.endsWith(".class"))) {
                        IJ.error("File name must end with \".java\" or \".class\".");
                        okay = false;
                    }
                } else if (!(lcName.endsWith(".java") || lcName.endsWith(".txt") || lcName.endsWith(".ijm") || lcName.endsWith(".js"))) {
                    IJ.error("File name must end with \".java\", \".txt\" or \".js\".");
                    okay = false;
                }
            }
        } else {
            int i = path.lastIndexOf('/');
            if (i == -1) {
                i = path.lastIndexOf('\\');
            }
            if (i > 0) {
                directory = path.substring(0, i + 1);
                fileName = path.substring(i + 1);
            } else {
                directory = "";
                fileName = path;
            }
            okay = true;
        }
        if (okay) {
            name = fileName;
            dir = directory;
            Editor.setDefaultDirectory(dir);
        }
        return okay;
    }

    // only show files with names ending in ".java"
    // doesn't work with Windows
    public boolean accept(File dir, String name) {
        return name.endsWith(".java") || name.endsWith(".macro") || name.endsWith(".txt");
    }

    // run the plugin using a new class loader
    void runPlugin(String name) {
        name = name.substring(0, name.length() - 5); // remove ".java"
        new PlugInExecuter(name);
    }

    public void showDialog() {
        validateTarget();
        GenericDialog gd = new GenericDialog("Compile and Run");
        gd.addChoice("Target: ", targets, targets[target]);
        gd.setInsets(15, 5, 0);
        gd.addCheckbox("Generate debugging info (javac -g)", generateDebuggingInfo);
        gd.addHelp(IJ.URL + "/docs/menus/edit.html#compiler");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        target = gd.getNextChoiceIndex();
        generateDebuggingInfo = gd.getNextBoolean();
        validateTarget();
    }

    void validateTarget() {
        if (target < 0 || target > TARGET17) {
            target = TARGET15;
        }
        if ((target > TARGET16 && !IJ.isJava17()) || (target > TARGET15 && !IJ.isJava16())) {
            target = TARGET15;
        }
        if (!IJ.isJava15()) {
            target = TARGET14;
        }
        Prefs.set(TARGET_KEY, target);
    }
}

class PlugInExecuter implements Runnable {
    private String plugin;
    private Thread thread;

    /** Create a new object that runs the specified plugin
    in a separate thread. */
    PlugInExecuter(String plugin) {
        this.plugin = plugin;
        thread = new Thread(this, plugin);
        thread.setPriority(Math.max(thread.getPriority() - 2, Thread.MIN_PRIORITY));
        thread.start();
    }

    public void run() {
        try {
            IJ.resetEscape();
            IJ.runPlugIn("ij.plugin.ClassChecker", "");
            IjxApplication ij = IJ.getInstance();
            if (ij != null) {
                ij.runUserPlugIn(plugin, plugin, "", true);
            }
        } catch (Throwable e) {
            IJ.showStatus("");
            IJ.showProgress(1.0);
            IjxImagePlus imp = WindowManager.getCurrentImage();
            if (imp != null) {
                imp.unlock();
            }
            String msg = e.getMessage();
            if (e instanceof RuntimeException && msg != null && e.getMessage().equals(Macro.MACRO_CANCELED)) {
                return;
            }
            IJ.handleException(e);
        }
    }
}
