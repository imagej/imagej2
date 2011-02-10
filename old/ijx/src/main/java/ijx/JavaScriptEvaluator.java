package ijx;
import ijx.plugin.api.PlugIn;


import ijx.plugin.frame.Editor;
import javax.script.*;

/** Implements the macro editor's Macros/Evaluate JavaScript command 
    on Linux and Windows systems running Java 1.6 or later. This command is
    implemented as a separately compilable plugin so that the ImageJ core
    can continue to be compiled using Java 1.4 and Java 1.5. The JavaScript
    plugin at <http://rsb.info.nih.gov/ij/plugins/download/misc/JavaScript.java>
    is used to evaluate JavaScript on Macs and on systems running versions
    of Java earlier than 1.6. */
public class JavaScriptEvaluator implements PlugIn, Runnable  {
	Thread thread;
	String script;

	// run script in separate thread
	public void run(String script) {
		if (script.equals("")) return;
		if (!IJ.isJava16())
			{IJ.error("Java 1.6 or later required"); return;}
		this.script = script;
		thread = new Thread(this, "JavaScript"); 
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		thread.start();
	}

	// run script in current thread
	public String run(String script, String arg) {
		this.script = script;
		run();
		return null;
	}

	public void run() {
		try {
			Thread.currentThread().setContextClassLoader(IJ.getClassLoader());
			ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
			ScriptEngine engine = scriptEngineManager.getEngineByName("ECMAScript");
			if (engine == null)
				{IJ.error("Could not find JavaScript engine"); return;}
			engine.eval(script);
		} catch(Throwable e) {
			String msg = e.getMessage();
			if (msg.startsWith("sun.org.mozilla.javascript.internal.EcmaError: "))
				msg = msg.substring(47, msg.length());
			if (msg.startsWith("sun.org.mozilla.javascript.internal.EvaluatorException"))
				msg = "Error"+msg.substring(54, msg.length());
			if (msg.indexOf("Macro canceled")==-1)
				IJ.log(msg);
		}
	}

}
