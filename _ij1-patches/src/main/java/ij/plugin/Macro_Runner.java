package ij.plugin;
import ij.*;
import ij.io.*;
import ij.macro.*;
import ij.text.*;
import ij.util.*;
import ij.plugin.frame.Editor;
import java.io.*;
import java.lang.reflect.*;

/** Opens and runs a macro file. */
public class Macro_Runner implements PlugIn {
	
	/** Opens and runs the specified macro file on the current thread. Displays a
		file open dialog if <code>name</code> is an empty string. Loads the
		macro from a JAR file in the plugins folder if <code>name</code> starts
		with "JAR:". Otherwise, loads the specified macro from the plugins folder
		or subfolder. */
	public void run(String name) {
		Thread thread = Thread.currentThread();
		String threadName = thread.getName();
		if (!threadName.endsWith("Macro$"))
			thread.setName(threadName+"Macro$");
		String path = null;
		if (name.equals("")) {
			OpenDialog od = new OpenDialog("Run Macro...", path);
			String directory = od.getDirectory();
			name = od.getFileName();
			if (name!=null)
				runMacroFile(directory+name, null);
		} else if (name.startsWith("JAR:"))
			runMacroFromJar(name);
		else if (name.startsWith("ij.jar:"))
			runMacroFromIJJar(name, null);
		else {
			if (!name.startsWith("ij.jar:"))
				path = Menus.getPlugInsPath() + name;
			runMacroFile(path, null);
		}
	}
        
    void runMacroFromJar(String name) {
    	name = name.substring(4);
    	String macro = null;
        try {
            // get macro text as a stream
			PluginClassLoader pcl = new PluginClassLoader(Menus.getPlugInsPath());
			InputStream is = pcl.getResourceAsStream("/"+name);
            if (is==null) {
            	IJ.showMessage("Macro Runner", "Unable to load \""+name+"\" from jar file");
            	return;
            }
            InputStreamReader isr = new InputStreamReader(is);
            
            StringBuffer sb = new StringBuffer();
            char [] b = new char [8192];
            int n;
            //read a block and append any characters
            while ((n = isr.read(b)) > 0)
                sb.append(b,0, n);
            
            // display the text in a TextWindow
            macro = sb.toString();
            //new TextWindow("Macro Runner", sb.toString(), 450, 450);
        }
        catch (IOException e) {
            String msg = e.getMessage();
            if (msg==null || msg.equals(""))
                msg = "" + e;	
            IJ.showMessage("Macro Runner", msg);
        }
		if (macro!=null)
			runMacro(macro, null);
    }

    /** Opens and runs the specified macro file on the current thread.
    	The file is assumed to be in the macros folder unless 
    	<code>name</code> is a full path. ".txt"  is
    	added if <code>name</code> does not have an extension. */
	public String runMacroFile(String name, String arg) {
		if (name.startsWith("ij.jar:"))
			return runMacroFromIJJar(name, arg);
        if (name.indexOf(".")==-1) name = name + ".txt";
		String name2 = name;
        boolean fullPath = name.startsWith("/") || name.startsWith("\\") || name.indexOf(":\\")==1;
        if (!fullPath) {
        	String macrosDir = Menus.getMacrosPath();
        	if (macrosDir!=null)
        		name2 = Menus.getMacrosPath() + name;
        }
		File file = new File(name2);
		int size = (int)file.length();
		if (size<=0 && !fullPath && name2.endsWith(".txt")) {
			String name3 = name2.substring(0, name2.length()-4)+".ijm";
			file = new File(name3);
			size = (int)file.length();
			if (size>0) name2 = name3;
		}
		if (size<=0 && !fullPath) {
			file = new File(System.getProperty("user.dir") + File.separator + name);
			size = (int)file.length();
			//IJ.log("runMacroFile: "+file.getAbsolutePath()+"  "+name+"  "+size);
		}
		if (size<=0) {
            IJ.error("RunMacro", "Macro or script not found:\n \n"+name2);
			return null;
		}
		try {
			byte[] buffer = new byte[size];
			FileInputStream in = new FileInputStream(file);
			in.read(buffer, 0, size);
			String macro = new String(buffer, 0, size, "ISO8859_1");
			in.close();
			if (name.endsWith(".js"))
				return runJavaScript(macro, arg);
			else
				return runMacro(macro, arg);
		}
		catch (Exception e) {
			IJ.error(e.getMessage());
			return null;
		}
	}

    /** Opens and runs the specified macro on the current thread. Macros can
    	retrieve the optional string argument by calling the getArgument() macro function. 
    	Returns the String value returned by the macro, null if the macro does not
    	return a value, or "[aborted]" if the macro was aborted due to an error. */
	public String runMacro(String macro, String arg) {
		Interpreter interp = new Interpreter();
		try {
			return interp.run(macro, arg);
		} catch(Throwable e) {
			interp.abortMacro();
			IJ.showStatus("");
			IJ.showProgress(1.0);
			ImagePlus imp = WindowManager.getCurrentImage();
			if (imp!=null) imp.unlock();
			String msg = e.getMessage();
			if (e instanceof RuntimeException && msg!=null && e.getMessage().equals(Macro.MACRO_CANCELED))
				return  "[aborted]";
			IJ.handleException(e);
		}
		return  "[aborted]";
	}
	
	public String runMacroFromIJJar(String name, String arg) {
		ImageJ ij = IJ.getInstance();
		//if (ij==null) return null;
		Class c = ij != null ? ImageJ.class : ImageStack.class;
		name = name.substring(7);
		String macro = null;
        try {
			InputStream is = c.getResourceAsStream("/macros/"+name+".txt");
			//IJ.log(is+"  "+("/macros/"+name+".txt"));
			if (is==null)
				return runMacroFile(name, arg);
            InputStreamReader isr = new InputStreamReader(is);
            StringBuffer sb = new StringBuffer();
            char [] b = new char [8192];
            int n;
            while ((n = isr.read(b)) > 0)
                sb.append(b,0, n);
            macro = sb.toString();
        }
        catch (IOException e) {
            String msg = e.getMessage();
            if (msg==null || msg.equals(""))
                msg = "" + e;	
            IJ.showMessage("Macro Runner", msg);
        }
		if (macro!=null)
			return runMacro(macro, arg);
		else
			return null;
	}
	
	/** Runs a script on the current thread, passing 'arg', which
		the script can retrieve using the getArgument() function.*/
	public String runJavaScript(String script, String arg) {
		if (arg==null) arg = "";
		Object js = null;
		if (IJ.isJava16() && !IJ.isMacOSX())
			js = IJ.runPlugIn("JavaScriptEvaluator", "");
		else
			js = IJ.runPlugIn("JavaScript", "");
		if (js==null) IJ.error(Editor.JS_NOT_FOUND);
		script = Editor.getJSPrefix(arg)+script;
		try {
			Class c = js.getClass();
			Method m = c.getMethod("run", new Class[] {script.getClass(), arg.getClass()});
			String s = (String)m.invoke(js, new Object[] {script, arg});			
		} catch(Exception e) {
			String msg = ""+e;
			if (msg.indexOf("NoSuchMethod")!=0)
				msg = "\"JavaScript.jar\" ("+IJ.URL+"/download/tools/JavaScript.jar)\nis outdated";
			IJ.error(msg);
			return null;
		}
		return null;
	}

}
