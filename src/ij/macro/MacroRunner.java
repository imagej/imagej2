package ij.macro;
import ijx.IjxImagePlus;
import ij.*;
import ij.text.*;
import ij.util.*;
import java.io.*;
																																																																																																																																																					   

/** This class runs macros in a separate thread. */
public class MacroRunner implements Runnable {

	private String macro;
	private Program pgm;
	private int address;
	private String name;
	private Thread thread;
	private String argument;

	/** Create a MacrRunner. */
	public MacroRunner() {
	}

	/** Create a new object that interprets macro source in a separate thread. */
	public MacroRunner(String macro) {
		this.macro = macro;
		thread = new Thread(this, "Macro$"); 
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		thread.start();
	}

	/** Create a new object that interprets macro source in a 
		separate thread, and also passing a string argument. */
	public MacroRunner(String macro, String argument) {
		this.macro = macro;
		this.argument = argument;
		thread = new Thread(this, "Macro$"); 
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		thread.start();
	}

	/** Create a new object that interprets a macro file using a separate thread. */
	public MacroRunner(File file) {
		int size = (int)file.length();
		if (size<=0)
			return;
		try {
			StringBuffer sb = new StringBuffer(5000);
			BufferedReader r = new BufferedReader(new FileReader(file));
			while (true) {
				String s=r.readLine();
				if (s==null)
					break;
				else
					sb.append(s+"\n");
			}
			r.close();
			macro = new String(sb);
		}
		catch (Exception e) {
			IJ.error(e.getMessage());
			return;
		}
		thread = new Thread(this, "Macro$"); 
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		thread.start();
	}

	/** Create a new object that runs a tokenized macro in a separate thread. */
	public MacroRunner(Program pgm, int address, String name) {
		this(pgm, address, name, null);
	}

	/** Create a new object that runs a tokenized macro in a separate thread,
		passing a string argument. */
	public MacroRunner(Program pgm, int address, String name, String argument) {
		this.pgm = pgm;
		this.address = address;
		this.name = name;
		this.argument = argument;
		thread = new Thread(this, name+"_Macro$");
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		thread.start();
	}

	/** Runs tokenized macro on current thread if pgm.queueCommands is true. */
	public void runShortcut(Program pgm, int address, String name) {
		this.pgm = pgm;
		this.address = address;
		this.name = name;
		if (pgm.queueCommands)
			run();
		else {
			thread = new Thread(this, name+"_Macro$");
			thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
			thread.start();
		}
	}

	public void run() {
		Interpreter interp = new Interpreter();
		interp.argument = argument;
		try {
			if (pgm==null)
				interp.run(macro);
			else
				interp.runMacro(pgm, address, name);
		} catch(Throwable e) {
			Interpreter.abort(interp);
			IJ.showStatus("");
			IJ.showProgress(1.0);
			IjxImagePlus imp = WindowManager.getCurrentImage();
			if (imp!=null) imp.unlock();
			String msg = e.getMessage();
			if (e instanceof RuntimeException && msg!=null && e.getMessage().equals(Macro.MACRO_CANCELED))
				return;
			CharArrayWriter caw = new CharArrayWriter();
			PrintWriter pw = new PrintWriter(caw);
			e.printStackTrace(pw);
			String s = caw.toString();
			if (IJ.isMacintosh())
				s = Tools.fixNewLines(s);
			//Don't show exceptions resulting from window being closed
			if (!(s.indexOf("NullPointerException")>=0 && s.indexOf("ij.process")>=0))
				new TextWindow("Exception", s, 350, 250);
		}
	}

}

