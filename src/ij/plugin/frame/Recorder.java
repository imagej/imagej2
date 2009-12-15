package ij.plugin.frame;
import ijx.IjxImagePlus;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import ij.*;
import ij.plugin.*;
import ij.plugin.frame.*; 
import ij.text.*;
import ij.gui.*;
import ij.util.*;
import ij.io.*;
import ij.process.*;
import ij.measure.*;

/** This is ImageJ's macro recorder. */
public class Recorder extends PlugInFrame implements PlugIn, ActionListener {

	/** This variable is true if the recorder is running. */
	public static boolean record;
	
	/** Set this variable true to allow recording within IJ.run() calls. */
	public static boolean recordInMacros;

	private Button makeMacro, help;
	private TextField macroName;
	private String fitTypeStr = CurveFitter.fitList[0];
	private static TextArea textArea;
	private static Frame instance;
	private static String commandName;
	private static String commandOptions;
	private static String defaultName = "Macro";
	private static boolean recordPath = true;

	public Recorder() {
		super("Recorder");
		if (instance!=null) {
			instance.toFront();
			return;
		}
		WindowManager.addWindow(this);
		instance = this;
		record = true;
		recordInMacros = false;
		Panel panel = new Panel(new FlowLayout(FlowLayout.CENTER, 2, 0));
		panel.add(new Label("Name:"));
		macroName = new TextField(defaultName,15);
		panel.add(macroName);
		panel.add(new Label("     "));
		makeMacro = new Button("Create");
		makeMacro.addActionListener(this);
		panel.add(makeMacro);
		panel.add(new Label("     "));
		help = new Button("?");
		help.addActionListener(this);
		panel.add(help);
		add("North", panel);
		textArea = new TextArea("",15,60,TextArea.SCROLLBARS_VERTICAL_ONLY);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		if (IJ.isLinux()) textArea.setBackground(Color.white);
		add("Center", textArea);
		pack();
		GUI.center(this);
		show();
		IJ.register(Recorder.class);
	}

	public static void record(String method) {
		if (textArea==null)
			return;
		textArea.append(method+"();\n");
	}

	/** Starts recording a command. Does nothing if the recorder is
		not open or the command being recorded has called IJ.run(). 
	*/
	public static void setCommand(String command) {
		boolean isMacro = Thread.currentThread().getName().startsWith("Run$_");
		if (textArea==null || (isMacro&&!recordInMacros))
			return;
		commandName = command;
		commandOptions = null;
		recordPath = true;
		//IJ.log("setCommand: "+command+" "+Thread.currentThread().getName());
	}

	/** Returns the name of the command currently being recorded, or null. */
	public static String getCommand() {
		return commandName;
	}

	static String fixPath (String path) {
		StringBuffer sb = new StringBuffer();
		char c;
		for (int i=0; i<path.length(); i++) {
			sb.append(c=path.charAt(i));
			if (c=='\\')
				sb.append("\\");
		}
		return new String(sb);
	}

	public static void record(String method, String arg) {
		if (textArea==null) return;
		textArea.append(method+"(\""+arg+"\");\n");
	}

	public static void record(String method, String arg1, String arg2) {
		if (textArea==null) return;
		if (arg1.equals("Open")||arg1.equals("Save")||method.equals("saveAs"))
			arg2 = fixPath(arg2);
		textArea.append(method+"(\""+arg1+"\", \""+arg2+"\");\n");
	}

	public static void record(String method, String arg1, String arg2, String arg3) {
		if (textArea==null) return;
		textArea.append(method+"(\""+arg1+"\", \""+arg2+"\",\""+arg3+"\");\n");
	}

	public static void record(String method, int a1) {
		if (textArea==null) return;
		if (method.equals("setTool"))
			method = "//"+method;
		textArea.append(method+"("+a1+");\n");
	}

	public static void record(String method, int a1, int a2) {
		if (textArea==null) return;
		textArea.append(method+"("+a1+", "+a2+");\n");
	}

	public static void record(String method, double a1, double a2) {
		if (textArea==null) return;
		int places = Math.abs(a1)<0.0001||Math.abs(a2)<0.0001?9:4;
		textArea.append(method+"("+IJ.d2s(a1,places)+", "+IJ.d2s(a2,places)+");\n");
	}

	public static void record(String method, int a1, int a2, int a3) {
		if (textArea==null) return;
		textArea.append(method+"("+a1+", "+a2+", "+a3+");\n");
	}

	public static void record(String method, String a1, int a2) {
		textArea.append(method+"(\""+a1+"\", "+a2+");\n");
	}

	public static void record(String method, String args, int a1, int a2) {
		if (textArea==null) return;
		method = "//"+method;
		textArea.append(method+"(\""+args+"\", "+a1+", "+a2+");\n");
	}

	public static void record(String method, int a1, int a2, int a3, int a4) {
		if (textArea==null) return;
		textArea.append(method+"("+a1+", "+a2+", "+a3+", "+a4+");\n");
	}

	public static void record(String method, String path, String args, int a1, int a2, int a3, int a4, int a5) {
		if (textArea==null) return;
		path = fixPath(path);
		method = "//"+method;
		textArea.append(method+"(\""+path+"\", "+"\""+args+"\", "+a1+", "+a2+", "+a3+", "+a4+", "+a5+");\n");
	}
	
	public static void recordString (String str) {
		if (textArea==null) return;
		textArea.append(str);
	}

	public static void recordRoi(Polygon p, int type) {
		if (textArea==null) return;
		if (type==Roi.ANGLE) {
			String xarr = "newArray(", yarr="newArray(";
			xarr += p.xpoints[0]+",";
			yarr += p.ypoints[0]+",";
			xarr += p.xpoints[1]+",";
			yarr += p.ypoints[1]+",";
			xarr += p.xpoints[2]+")";
			yarr += p.ypoints[2]+")";
			textArea.append("makeSelection(\"angle\","+xarr+","+yarr+");\n");
		} else {
			String method = type==Roi.POLYGON?"makePolygon":"makeLine";
			StringBuffer args = new StringBuffer();
			for (int i=0; i<p.npoints; i++) {
				args.append(p.xpoints[i]+",");
				args.append(""+p.ypoints[i]);
				if (i!=p.npoints-1) args.append(",");
			}
			textArea.append(method+"("+args.toString()+");\n");
		}
	}

	public static void recordOption(String key, String value) {
		if (key==null) return;
		key = trimKey(key);
		value = addQuotes(value);
		checkForDuplicate(key+"=", value);
		if (commandOptions==null)
			commandOptions = key+"="+value;
		else
			commandOptions += " "+key+"="+value;
	}

	public static void recordPath(String key, String path) {
		if (key==null || !recordPath)
			{recordPath=true; return;}
		key = trimKey(key);
		path = fixPath(path);
		path = addQuotes(path);
		checkForDuplicate(key+"=", path);
		if (commandOptions==null)
			commandOptions = key+"="+path;
		else
			commandOptions += " "+key+"="+path;
		//IJ.log("recordPath: "+key+"="+path);
	}

	public static void recordOption(String key) {
		if (key==null) return;
		if (commandOptions==null && key.equals(" "))
			commandOptions = " ";
		else {
			key = trimKey(key);
			checkForDuplicate(" "+key, "");
			if (commandOptions==null)
				commandOptions = key;
			else
				commandOptions += " "+key;
		}
	}
	
	static void checkForDuplicate(String key, String value) {
		if (commandOptions!=null && commandName!=null && commandOptions.indexOf(key)!=-1 && (value.equals("") || commandOptions.indexOf(value)==-1)) {
			if (key.endsWith("=")) key = key.substring(0, key.length()-1);
			IJ.showMessage("Recorder", "Duplicate keyword:\n \n" 
				+ "    Command: " + "\"" + commandName +"\"\n"
				+ "    Keyword: " + "\"" + key +"\"\n"
				+ "    Value: " + value+"\n \n"
				+ "Add an underscore to the corresponding label\n"
				+ "in the dialog to make the first word unique.");
		}
	}
	
	static String trimKey(String key) {
		int index = key.indexOf(" ");
		if (index>-1)
			key = key.substring(0,index);
		index = key.indexOf(":");
		if (index>-1)
			key = key.substring(0,index);
		key = key.toLowerCase(Locale.US);
		return key;
	}

	/** Writes the current command and options to the Recorder window. */
	public static void saveCommand() {
		String name = commandName;
		if (name!=null) {
			if (commandOptions!=null) {
				if (name.equals("Open..."))
					textArea.append("open(\""+strip(commandOptions)+"\");\n");
				else if (isSaveAs()) {
							if (name.endsWith("..."))
									name= name.substring(0, name.length()-3);
							String path = strip(commandOptions);
							textArea.append("saveAs(\""+name+"\", \""+path+"\");\n");
				} else if (name.equals("Image..."))
					appendNewImage();
				else if (name.equals("Set Slice..."))
					textArea.append("setSlice("+strip(commandOptions)+");\n");
				else if (name.equals("Rename..."))
					textArea.append("rename(\""+strip(commandOptions)+"\");\n");
				else if (name.equals("Image Calculator..."))
					textArea.append("//run(\""+name+"\", \""+commandOptions+"\");\n");
				else 
					textArea.append("run(\""+name+"\", \""+commandOptions+"\");\n");
			} else {
				if (name.equals("Threshold...") || name.equals("Fonts..."))
					textArea.append("//run(\""+name+"\");\n");
				else if (name.equals("Start Animation [\\]"))
					textArea.append("doCommand(\"Start Animation [\\\\]\");\n");
				else if (name.equals("Draw")) {
					IjxImagePlus imp = WindowManager.getCurrentImage();
					Roi roi = imp.getRoi();
					if (roi!=null && (roi instanceof TextRoi))
						textArea.append(((TextRoi)roi).getMacroCode(imp.getProcessor()));
					else
						textArea.append("run(\""+name+"\");\n");
				} else {
					if (IJ.altKeyDown() && (name.equals("Open Next")||name.equals("Plot Profile")))
						textArea.append("setKeyDown(\"alt\"); ");
					textArea.append("run(\""+name+"\");\n");
				}
			}
		}
		commandName = null;
		commandOptions = null;
	}
	
	static boolean isSaveAs() {
		return commandName.equals("Tiff...")
			|| commandName.equals("Gif...")
			|| commandName.equals("Jpeg...")
			|| commandName.equals("Text Image...")
			|| commandName.equals("ZIP...")
			|| commandName.equals("Raw Data...")
			|| commandName.equals("BMP...")
			|| commandName.equals("PNG...")
			|| commandName.equals("PGM...")
			|| commandName.equals("FITS...")
			|| commandName.equals("LUT...")
			|| commandName.equals("Selection...")
			|| commandName.equals("XY Coordinates...")
			|| commandName.equals("Measurements...")
			|| commandName.equals("Text... ");
	}

	static void appendNewImage() {
		String options = getCommandOptions() + " ";
		String title = Macro.getValue(options, "name", "Untitled");
		String type = Macro.getValue(options, "type", "8-bit");
		String fill = Macro.getValue(options, "fill", "");
		if (!fill.equals("")) type = type +" " + fill;
		int width = (int)Tools.parseDouble(Macro.getValue(options, "width", "512"));
		int height = (int)Tools.parseDouble(Macro.getValue(options, "height", "512"));
		int depth= (int)Tools.parseDouble(Macro.getValue(options, "slices", "1"));
		textArea.append("newImage(\""+title+"\", "+"\""+type+"\", "+width+", "+height+", "+depth+");\n");
	}

	static String strip(String value) {
		int index = value.indexOf('=');
		if (index>=0)
			value = value.substring(index+1);
		if (value.startsWith("[")) {
			int index2 = value.indexOf(']');
			if (index2==-1) index2 = value.length();
			value = value.substring(1, index2);
		} else {
			index = value.indexOf(' ');
			if (index!=-1)
				value = value.substring(0, index);
		}
		return value;
	}

	static String addQuotes(String value) {
		int index = value.indexOf(' ');
		if (index>-1)
			value = "["+value+"]";
		return value;
	}
	
	/** Used by GenericDialog to determine if any options have been recorded. */
	static public String getCommandOptions() {
		return commandOptions;
	}

	void createMacro() {
		String text = textArea.getText();
		if (text==null || text.equals("")) {
			IJ.showMessage("Recorder", "A macro cannot be created until at least\none command has been recorded.");
			return;
		}
		Editor ed = (Editor)IJ.runPlugIn("ij.plugin.frame.Editor", "");
		if (ed==null)
			return;
		String name = macroName.getText();
		int dotIndex = name.lastIndexOf(".");
		if (dotIndex>=0) name = name.substring(0, dotIndex);
		name += ".txt";
		ed.createMacro(name, text);
	}
	
	/** Temporarily disables path recording. */
	public static void disablePathRecording() {
		recordPath = false;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==makeMacro)
			createMacro();
		else if (e.getSource()==help)
			showHelp();
	}

    void showHelp() {
    	IJ.showMessage("Recorder",
			"Click \"Create\" to open recorded commands\n"  
			+"as a macro in an editor window.\n" 
			+" \n" 
			+"In the editor:\n" 
			+" \n"
			+"    Type ctrl+R (Macros>Run Macro) to\n" 
			+"    run the macro.\n"     
			+" \n"    
			+"    Use File>Save As to save it and\n" 
			+"    ImageJ's Open command to open it.\n" 
			+" \n"    
			+"    To create a command, use File>Save As,\n"  
			+"    add a '_' to the name, save in the \n" 
			+"    plugins folder, and restart ImageJ.\n" 
			+" \n"     
			+"    Use Edit>Convert to Plugin to convert\n" 
			+"    the macro to a plugin."
		);
    }
    
    public void windowClosing(WindowEvent e) {
    	close();
	}

	public void close() {
		super.close();
		record = false;
		textArea = null;
		commandName = null;
		instance = null;	
	}

	public String getText() {
		if (textArea==null)
			return "";
		else
			return textArea.getText();
	}

}