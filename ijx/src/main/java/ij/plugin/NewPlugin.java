package ij.plugin;
import java.awt.*;
import ij.*;
import ij.gui.*;
import ij.plugin.frame.Editor;
import ij.text.TextWindow;
import ij.io.SaveDialog;

/** This class creates a new macro or the Java source for a new plugin. */
public class NewPlugin implements PlugIn {

	public static final int MACRO=0, JAVASCRIPT=1, PLUGIN=2, PLUGIN_FILTER=3, PLUGIN_FRAME=4, TEXT_FILE=5, TABLE=6;
    private static int rows = 16;
    private static int columns = 60;
    private static int tableWidth = 350;
    private static int tableHeight = 250;
    private int type = MACRO;
    private String name = "Macro.ijm";
    private boolean monospaced;
    private boolean menuBar = true;
	private Editor ed;
    
    public void run(String arg) {
    	type = -1;
    	if (arg.startsWith("text")||arg.equals("")) {
    		type = TEXT_FILE;
    		name = "Untitled.txt";
    	} else if (arg.equals("macro")) {
    		type = MACRO;
		name = "Macro.ijm";
    	} else if (arg.equals("javascript")) {
    		type = JAVASCRIPT;
    		name = "Script.js";
     	} else if (arg.equals("plugin")) {
    		type = PLUGIN;
    		name = "My_Plugin.java";
    	} else if (arg.equals("frame")) {
    		type = PLUGIN_FRAME;
    		name = "Plugin_Frame.java";
    	} else if (arg.equals("filter")) {
    		type = PLUGIN_FILTER;
    		name = "Filter_Plugin.java";
    	} else if (arg.equals("table")) {
			String options = Macro.getOptions();
			if  (IJ.isMacro() && options!=null && options.indexOf("[Text File]")!=-1) {
    			type = TEXT_FILE;
    			name = "Untitled.txt";
    			arg = "text+dialog";
    		} else {
    			type = TABLE;
    			name = "Table";
    		}
    	}
    	menuBar = true;
    	if (arg.equals("text+dialog") || type==TABLE) {
			if (!showDialog()) return;
		}
		if (type==-1) {
			name = "Converted_Macro.java";
			if (arg.startsWith("name:")) {
				int eol = arg.indexOf('\n');
				name = arg.substring(5, eol);
				arg = arg.substring(eol + 1);
			}
		createPlugin(name, PLUGIN, arg);
		}
		else if (type==MACRO || type==TEXT_FILE || type==JAVASCRIPT) {
			if (type==TEXT_FILE && name.equals("Macro"))
				name = "Untitled.txt";
			createMacro(name);
		} else if (type==TABLE)
			createTable();
		else
			createPlugin(name, type, arg);
    }
    
	public void createMacro(String name) {
		int options = (monospaced?Editor.MONOSPACED:0)+(menuBar?Editor.MENU_BAR:0);
		if (type==MACRO && !name.endsWith(".txt"))
			name = SaveDialog.setExtension(name, ".txt");
		else if (type==JAVASCRIPT && !name.endsWith(".js")) {
			if (name.equals("Macro")) name = "script";
			name = SaveDialog.setExtension(name, ".js");
		}
		if (type == MACRO && IJ.runFijiEditor(name, ""))
			return;
		ed = new Editor(rows, columns, 0, options);
		ed.create(name, "");
	}
	
	void createTable() {
			new TextWindow(name, "", tableWidth, tableHeight);
	}

	public void createPlugin(String name, int type, String methods) {
		String pluginName = name;
		if (!(name.endsWith(".java") || name.endsWith(".JAVA")))
			name = SaveDialog.setExtension(name, ".java");
		String className = pluginName.substring(0, pluginName.length()-5);
		String text = "";
		text += "import ij.*;\n";
		text += "import ij.process.*;\n";
		text += "import ij.gui.*;\n";
		text += "import java.awt.*;\n";
		switch (type) {
			case PLUGIN:
				text += "import ij.plugin.*;\n";
				text += "import ij.plugin.frame.*;\n";
				text += "\n";
				text += "public class "+className+" implements PlugIn {\n";
				text += "\n";
				text += "\tpublic void run(String arg) {\n";
				if (methods.equals("plugin"))
					text += "\t\tIJ.showMessage(\""+className+"\",\"Hello world!\");\n";
				else
					text += methods;
				text += "\t}\n";
				break;
			case PLUGIN_FILTER:
				text += "import ij.plugin.filter.*;\n";
				text += "\n";
				text += "public class "+className+" implements PlugInFilter {\n";
				text += "\tImagePlus imp;\n";
				text += "\n";
				text += "\tpublic int setup(String arg, IjxImagePlus imp) {\n";
				text += "\t\tthis.imp = imp;\n";
				text += "\t\treturn DOES_ALL;\n";
				text += "\t}\n";
				text += "\n";
				text += "\tpublic void run(ImageProcessor ip) {\n";
				text += "\t\tip.invert();\n";
				text += "\t\timp.updateAndDraw();\n";
				text += "\t\tIJ.wait(500);\n";
				text += "\t\tip.invert();\n";
				text += "\t\timp.updateAndDraw();\n";
				text += "\t}\n";
				break;
			case PLUGIN_FRAME:
				text += "import ij.plugin.frame.*;\n";
				text += "\n";
				text += "public class "+className+" extends PlugInFrame {\n";
				text += "\n";
				text += "\tpublic "+className+"() {\n";
				text += "\t\tsuper(\""+className+"\");\n";
				text += "\t\tTextArea ta = new TextArea(15, 50);\n";
				text += "\t\tadd(ta);\n";
				text += "\t\tpack();\n";
				text += "\t\tGUI.center(this);\n";
				text += "\t\tshow();\n";
				text += "\t}\n";
				break;
		}
		text += "\n";
		text += "}\n";

        // @todo: Discover possible Java Editors...
        // Use @ServiceProvider(service="JavaEditor")
        // interface JavaEditor {
        // public class Editor extends PlugInFrame
        //   implements ActionListener, ItemListener, TextListener, ClipboardOwner, MacroConstants {
        //}
		if (IJ.runFijiEditor(pluginName, text))
			return;

		ed = (Editor)IJ.runPlugIn("ij.plugin.frame.Editor", "");
		if (ed==null) return;
		ed.create(pluginName, text);
	}
	
	public boolean showDialog() {
		String title;
		String widthUnit, heightUnit;
		int width, height;
		if (type==TABLE) {
			title = "New Table";
			name = "Table";
			width = tableWidth;
			height = tableHeight;
			widthUnit = "pixels";
			heightUnit = "pixels";
		} else {
			title = "New Text Window";
			name = "Untitled";
			width = columns;
			height = rows;
			widthUnit = "characters";
			heightUnit = "lines";
		}
		GenericDialog gd = new GenericDialog(title);
		gd.addStringField("Name:", name, 16);
		gd.addMessage("");
		gd.addNumericField("Width:", width, 0, 3, widthUnit);
		gd.addNumericField("Height:", height, 0, 3, heightUnit);
		if (type!=TABLE) {
			gd.setInsets(5, 30, 0);
			gd.addCheckbox("Menu Bar", true);
			gd.setInsets(0, 30, 0);
			gd.addCheckbox("Monospaced Font", monospaced);
		}
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		name = gd.getNextString();
		width = (int)gd.getNextNumber();
		height = (int)gd.getNextNumber();
		if (width<1) width = 1;
		if (height<1) height = 1;
		if (type!=TABLE) {
			menuBar = gd.getNextBoolean();
			monospaced = gd.getNextBoolean();
			columns = width;
			rows = height;
			if (rows>100) rows = 100;
			if (columns>200) columns = 200;
		} else {
			tableWidth = width;
			tableHeight = height;
			if (tableWidth<128) tableWidth = 128;
			if (tableHeight<75) tableHeight = 75;
		}
		return true;
	}
	
	/** Returns the Editor the newly created macro or plugin was opened in. */
	public Editor getEditor() {
		return ed;
	}

}
