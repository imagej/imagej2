package ijx;

import ij.Command;
import ij.CommandListener;
import ij.CommandListenerPlus;
import ij.IJ;
import ij.Macro;
import ij.Menus;
import ij.WindowManager;
import ij.util.Tools;
import ij.text.TextWindow;
import ij.plugin.MacroInstaller;
import ij.plugin.frame.Recorder;
import ij.io.OpenDialog;
import java.io.*;
import java.util.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.Menu;
import javax.swing.JMenu;

/** Runs ImageJ menu commands in a separate thread.  IJX */

// @todo: Use of ExecutorService, FutureTask

public class ExecuterIjx implements Runnable {

    private static String previousCommand;
    private static CommandListener listener;
    private static Vector listeners = new Vector();
    private String command;
    ActionEvent actionEvent;
    private Thread thread;

    public ExecuterIjx(String cmd, ActionEvent e) {
        command = cmd;
        actionEvent = e;
    }

    /** Create an Executer to run the specified menu command
    in this thread using the active image. */
    public ExecuterIjx(String cmd) {
        command = cmd;
    }

    /** Create an Executer that runs the specified menu command
    in a separate thread using the active image image. */

    public ExecuterIjx(String cmd, IjxImagePlus ignored) {
        if (cmd.startsWith("Repeat")) {
            command = previousCommand;
            IJ.setKeyUp(KeyEvent.VK_SHIFT);
        } else {
            command = cmd;
            if (!(cmd.equals("Undo") || cmd.equals("Close"))) {
                previousCommand = cmd;
            }
        }
        IJ.resetEscape();
        thread = new Thread(this, cmd);
        thread.setPriority(Math.max(thread.getPriority() - 2, Thread.MIN_PRIORITY));
        thread.start();
    }



    public void run() {
        if (command == null) {
            return;
        }
        Command cmd = new Command(command);
        if (listeners.size() > 0) {
            synchronized (listeners) {
                for (int i = 0; i < listeners.size(); i++) {
                    CommandListener listener = (CommandListener) listeners.elementAt(i);
                    cmd.command = listener.commandExecuting(cmd.command);
                    if (listener instanceof CommandListenerPlus) {
                        ((CommandListenerPlus) listener).stateChanged(cmd, CommandListenerPlus.CMD_REQUESTED);
                        if (cmd.isConsumed()) {
                            return;
                        }
                    }
                    if (cmd.command == null) {
                        return;
                    }
                }
            }
        }
        cmd.modifiers = (IJ.altKeyDown() ? ActionEvent.ALT_MASK : 0) | (IJ.shiftKeyDown() ? ActionEvent.SHIFT_MASK : 0);
        try {
            if (Recorder.record) {
                Recorder.setCommand(cmd.command);
                runCommand(cmd);
                Recorder.saveCommand();
            } else {
                runCommand(cmd);
            }
            int len = command.length();
            if (command.charAt(len - 1) != ']' && !(len < 4 && (command.equals("In") || command.equals("Out")))) {
                IJ.setKeyUp(IJ.ALL_KEYS);  // set keys up except for "<", ">", "+" and "-" shortcuts
            }
        } catch (Throwable e) {
            IJ.showStatus("");
            IJ.showProgress(1, 1);
            IjxImagePlus imp = WindowManager.getCurrentImage();
            if (imp != null) {
                imp.unlock();
            }
            String msg = e.getMessage();
            if (e instanceof OutOfMemoryError) {
                IJ.outOfMemory(command);
                notifyCommandListeners(cmd, CommandListenerPlus.CMD_ERROR);
            } else if (e instanceof RuntimeException && msg != null && msg.equals(Macro.MACRO_CANCELED)) {
                notifyCommandListeners(cmd, CommandListenerPlus.CMD_CANCELED);
                ; //do nothing
            } else {
                CharArrayWriter caw = new CharArrayWriter();
                PrintWriter pw = new PrintWriter(caw);
                e.printStackTrace(pw);
                String s = caw.toString();
                if (IJ.isMacintosh()) {
                    if (s.indexOf("ThreadDeath") > 0) {
                        return;
                    }
                    s = Tools.fixNewLines(s);
                }
                int w = 350, h = 250;
                if (s.indexOf("UnsupportedClassVersionError") != -1) {
                    if (s.indexOf("version 49.0") != -1) {
                        s = e + "\n \nThis plugin requires Java 1.5 or later.";
                        w = 700;
                        h = 150;
                    }
                    if (s.indexOf("version 50.0") != -1) {
                        s = e + "\n \nThis plugin requires Java 1.6 or later.";
                        w = 700;
                        h = 150;
                    }
                    if (s.indexOf("version 51.0") != -1) {
                        s = e + "\n \nThis plugin requires Java 1.7 or later.";
                        w = 700;
                        h = 150;
                    }
                }
                if (IJ.getInstance() != null) {
                    new TextWindow("Exception", s, w, h);
                } else {
                    IJ.log(s);
                }
                notifyCommandListeners(cmd, CommandListenerPlus.CMD_ERROR);
            }
            IJ.abort();
        }
    }

    void runCommand(Command cmd) {
        // @todo: Use ExecutorService...
        Hashtable table = Menus.getCommands();
        cmd.className = (String) table.get(cmd.command);
        if (cmd.className != null) {
            cmd.arg = "";
            if (cmd.className.endsWith("\")")) {
                // extract string argument (e.g. className("arg"))
                int argStart = cmd.className.lastIndexOf("(\"");
                if (argStart > 0) {
                    cmd.arg = cmd.className.substring(argStart + 2, cmd.className.length() - 2);
                    cmd.className = cmd.className.substring(0, argStart);
                }
            }
            notifyCommandListeners(cmd, CommandListenerPlus.CMD_READY);
            if (cmd.isConsumed()) {
                return; // last chance to interrupt
            }
            if (IJ.shiftKeyDown() && cmd.className.startsWith("ij.plugin.Macro_Runner") && !Menus.getShortcuts().contains("*" + cmd)) {
                IJ.open(IJ.getDirectory("plugins") + cmd.arg);
            } else {
                cmd.plugin = IJ.runPlugIn(cmd.command, cmd.className, cmd.arg);
            }
            notifyCommandListeners(cmd, CommandListenerPlus.CMD_STARTED);
        } else {
            notifyCommandListeners(cmd, CommandListenerPlus.CMD_READY);
            // Is this command in Plugins>Macros?
            if (MacroInstaller.runMacroCommand(cmd.command)) {
                notifyCommandListeners(cmd, CommandListenerPlus.CMD_MACRO);
                return;
            }
            // Is this command a LUT name?
            String path = IJ.getDirectory("luts") + cmd.command + ".lut";
            File f = new File(path);
            if (f.exists()) {
                String dir = OpenDialog.getLastDirectory();
                IJ.open(path);
                notifyCommandListeners(cmd, CommandListenerPlus.CMD_LUT);
                OpenDialog.setLastDirectory(dir);
            } else if (!openRecent(cmd.command)) {
                IJ.error("Unrecognized command: " + cmd.command);
            }
        }
        notifyCommandListeners(cmd, CommandListenerPlus.CMD_FINISHED);
    }

    /** Opens a file from the File/Open Recent menu and returns 'true' if successful. */
    // @todo - this does not belong here... and needs to deal with AWT
    boolean openRecent(String cmd) {
        //Menu menu = (Menu)Menus.getOpenRecentMenu();
        JMenu menu = (JMenu)Menus.getOpenRecentMenu();
        if (menu == null) {
            return false;
        }
        for (int i = 0; i < menu.getItemCount(); i++) {
            if (menu.getItem(i).getLabel().equals(cmd)) {
                IJ.open(cmd);
                return true;
            }
        }
        return false;
    }

    /** Returns the last command executed. Returns null
    if no command has been executed. */
    public static String getCommand() {
        return previousCommand;
    }

// <editor-fold defaultstate="collapsed" desc=" CommandListening ">
    void notifyCommandListeners(Command cmd, int action) {
        if (listeners.size() > 0) {
            synchronized (listeners) {
                for (int i = 0; i < listeners.size(); i++) {
                    CommandListener listener = (CommandListener) listeners.elementAt(i);
                    if (listener instanceof CommandListenerPlus) {
                        ((CommandListenerPlus) listener).stateChanged(cmd, action);

                    }
                }
            }

        }
    }

    /** Adds the specified command listener. */
    public static void addCommandListener(CommandListener listener) {
        listeners.addElement(listener);
    }

    /** Removes the specified command listener. */
    public static void removeCommandListener(CommandListener listener) {
        listeners.removeElement(listener);
    }
// </editor-fold>
}
