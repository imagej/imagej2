package ij;
import ijx.IjxImagePlus;
import ij.util.Tools;
import ij.text.TextWindow;
import ij.plugin.MacroInstaller;
import ij.plugin.frame.Recorder;
import java.io.*;
import java.util.*;
import java.awt.event.KeyEvent;

/** Runs ImageJ menu commands in a separate thread.*/
public class Executer implements Runnable {

	private static String previousCommand;
	private static CommandListener listener;
	private static Vector listeners = new Vector();
	
	private String command;
	private Thread thread;
	
	/** Create an Executer to run the specified menu command
		in this thread using the active image. */
	public Executer(String cmd) {
		command = cmd;
	}

	/** Create an Executer that runs the specified menu command
		in a separate thread using the active image image. */
	public Executer(String cmd, ImagePlus ignored) {
		if (cmd.startsWith("Repeat")) {
			command = previousCommand;
			IJ.setKeyUp(KeyEvent.VK_SHIFT);		
		} else {
			command = cmd;
			if (!(cmd.equals("Undo")||cmd.equals("Close")))
				previousCommand = cmd;
		}
		IJ.resetEscape();
		thread = new Thread(this, cmd);
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		thread.start();
	}

	public void run() {
		if (command==null) return;
		if (listeners.size()>0) synchronized (listeners) {
			for (int i=0; i<listeners.size(); i++) {
				CommandListener listener = (CommandListener)listeners.elementAt(i);
				command = listener.commandExecuting(command);
				if (command==null) return;
			}
		}
		try {
			if (Recorder.record) {
				Recorder.setCommand(command);
				runCommand(command);
				Recorder.saveCommand();
			} else
				runCommand(command);
		} catch(Throwable e) {
			IJ.showStatus("");
			IJ.showProgress(1.0);
			IjxImagePlus imp = WindowManager.getCurrentImage();
			if (imp!=null) imp.unlock();
			String msg = e.getMessage();
			if (e instanceof OutOfMemoryError)
				IJ.outOfMemory(command);
			else if (e instanceof RuntimeException && msg!=null && msg.equals(Macro.MACRO_CANCELED))
				; //do nothing
			else {
				CharArrayWriter caw = new CharArrayWriter();
				PrintWriter pw = new PrintWriter(caw);
				e.printStackTrace(pw);
				String s = caw.toString();
				if (IJ.isMacintosh()) {
					if (s.indexOf("ThreadDeath")>0)
						return;
					s = Tools.fixNewLines(s);
				}
				if (IJ.getInstance()!=null)
					new TextWindow("Exception", s, 350, 250);
				else
					IJ.log(s);
			}
		}
	}
	
    void runCommand(String cmd) {
		Hashtable table = Menus.getCommands();
		String className = (String)table.get(cmd);
		if (className!=null) {
			String arg = "";
			if (className.endsWith("\")")) {
				// extract string argument (e.g. className("arg"))
				int argStart = className.lastIndexOf("(\"");
				if (argStart>0) {
					arg = className.substring(argStart+2, className.length()-2);
					className = className.substring(0, argStart);
				}
			}
			if (IJ.shiftKeyDown() && className.startsWith("ij.plugin.Macro_Runner") && !Menus.getShortcuts().contains("*"+cmd)) {
    			IJ.open(IJ.getDirectory("plugins")+arg);
				IJ.setKeyUp(KeyEvent.VK_SHIFT);		
    		} else
				IJ.runPlugIn(cmd, className, arg);
		} else {
			// Is this command in Plugins>Macros?
			if (MacroInstaller.runMacroCommand(cmd))
				return;
			// Is this command a LUT name?
			String path = Prefs.getHomeDir()+File.separator+"luts"+File.separator+cmd+".lut";
			File f = new File(path);
			if (f.exists())
				IJ.open(path);
			else
				IJ.error("Unrecognized command: " + cmd);
	 	}
    }

	/** Returns the last command executed. Returns null
		if no command has been executed. */
	public static String getCommand() {
		return previousCommand;
	}
	
	/** Adds the specified command listener. */
	public static void addCommandListener(CommandListener listener) {
		listeners.addElement(listener);
	}
	
	/** Removes the specified command listener. */
	public static void removeCommandListener(CommandListener listener) {
		listeners.removeElement(listener);
	}

}


