package ijx.text;

import ijx.Menus;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;
import ijx.app.IjxApplication;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.awt.datatransfer.*;
import ijx.plugin.filter.Analyzer;
import ijx.io.SaveDialog;
import ijx.measure.ResultsTable;
import imagej.util.Tools;
import ijx.plugin.frame.Recorder;
import ijx.gui.dialog.GenericDialog;
import ijx.CentralLookup;
import ijx.app.KeyboardHandler;


/**
This is an unlimited size text panel with tab-delimited,
labeled and resizable columns. It is based on the hGrid
class at
    http://www.lynx.ch/contacts/~/thomasm/Grid/index.html.
*/
public class TextPanel extends Panel implements AdjustmentListener,
	MouseListener, MouseMotionListener, KeyListener,  ClipboardOwner,
	ActionListener, MouseWheelListener, Runnable {

	static final int DOUBLE_CLICK_THRESHOLD = 650;
	// height / width
	int iGridWidth,iGridHeight;
	int iX,iY;
	// data
	String[] sColHead;
	Vector vData;
	int[] iColWidth;
	int iColCount,iRowCount;
	int iRowHeight,iFirstRow;
	// scrolling
	Scrollbar sbHoriz,sbVert;
	int iSbWidth,iSbHeight;
	boolean bDrag;
	int iXDrag,iColDrag;
  
	boolean headings = true;
	String title = "";
	String labels;
	KeyListener keyListener;
	Cursor resizeCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
  	Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	int selStart=-1, selEnd=-1,selOrigin=-1, selLine=-1;
	TextCanvas tc;
	PopupMenu pm;
	boolean columnsManuallyAdjusted;
	long mouseDownTime;
    String filePath;
    ResultsTable rt;
    boolean unsavedLines;

  
	/** Constructs a new TextPanel. */
	public TextPanel() {
		tc = new TextCanvas(this);
		setLayout(new BorderLayout());
		add("Center",tc);
		sbHoriz=new Scrollbar(Scrollbar.HORIZONTAL);
		sbHoriz.addAdjustmentListener(this);
		sbHoriz.setFocusable(false); // prevents scroll bar from blinking on Windows
		add("South", sbHoriz);
		sbVert=new Scrollbar(Scrollbar.VERTICAL);
		sbVert.addAdjustmentListener(this);
		sbVert.setFocusable(false);
		IjxApplication ij = IJ.getInstance();
		if (ij!=null) {
			sbHoriz.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
			sbVert.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
		}
		add("East", sbVert);
		addPopupMenu();
	}
  
	/** Constructs a new TextPanel. */
	public TextPanel(String title) {
		this();
		if (title.equals("Results")) {
			pm.addSeparator();
			addPopupItem("Clear Results");
			addPopupItem("Summarize");
			addPopupItem("Distribution...");
			addPopupItem("Set Measurements...");
			addPopupItem("Rename...");
			addPopupItem("Duplicate...");
		}
	}

	void addPopupMenu() {
		pm=new PopupMenu();
		addPopupItem("Save As...");
		pm.addSeparator();
		addPopupItem("Cut");
		addPopupItem("Copy");
		addPopupItem("Clear");
		addPopupItem("Select All");
		add(pm);
	}
	
	void addPopupItem(String s) {
		MenuItem mi=new MenuItem(s);
		mi.addActionListener(this);
		pm.add(mi);
	}

	/**
	Clears this TextPanel and sets the column headings to
	those in the tab-delimited 'headings' String. Set 'headings'
	to "" to use a single column with no headings.
	*/
	public synchronized void setColumnHeadings(String labels) {
		boolean sameLabels = labels.equals(this.labels);
		this.labels = labels;
		if (labels.equals("")) {
			iColCount = 1;
			sColHead=new String[1];
			sColHead[0] = "";
		} else {
			if (labels.endsWith("\t"))
				this.labels = labels.substring(0, labels.length()-1);
			sColHead = Tools.split(this.labels, "\t");
        	iColCount = sColHead.length;
		}
		flush();
		vData=new Vector();
		if (!(iColWidth!=null && iColWidth.length==iColCount && sameLabels && iColCount!=1)) {
			iColWidth=new int[iColCount];
			columnsManuallyAdjusted = false;
		}
		iRowCount=0;
		resetSelection();
		adjustHScroll();
		tc.repaint();
	}
  
	/** Returns the column headings as a tab-delimited string. */
	public String getColumnHeadings() {
		return labels==null?"":labels;
	}
	
	public synchronized void updateColumnHeadings(String labels) {
		this.labels = labels;
		if (labels.equals("")) {
			iColCount = 1;
			sColHead=new String[1];
			sColHead[0] = "";
		} else {
			if (labels.endsWith("\t"))
				this.labels = labels.substring(0, labels.length()-1);
			sColHead = Tools.split(this.labels, "\t");
        	iColCount = sColHead.length;
			iColWidth=new int[iColCount];
			columnsManuallyAdjusted = false;
		}
	}

	public void setFont(Font font, boolean antialiased) {
		tc.fFont = font;
		tc.iImage = null;
		tc.fMetrics = null;
		tc.antialiased = antialiased;
		iColWidth[0] = 0;
		if (isShowing()) updateDisplay();
	}
  
	/** Adds a single line to the end of this TextPanel. */
	public void appendLine(String data) {
		if (vData==null)
			setColumnHeadings("");
		char[] chars = data.toCharArray();
		vData.addElement(chars);
		iRowCount++;
		if (isShowing()) {
			if (iColCount==1 && tc.fMetrics!=null) {
				iColWidth[0] = Math.max(iColWidth[0], tc.fMetrics.charsWidth(chars,0,chars.length));
				adjustHScroll();
			}
			updateDisplay();
			unsavedLines = true;
		}
	}
	
	/** Adds one or more lines to the end of this TextPanel. */
	public void append(String data) {
		if (data==null) data="null";
		if (vData==null)
			setColumnHeadings("");
		while (true) {
			int p=data.indexOf('\n');
			if (p<0) {
				appendWithoutUpdate(data);
				break;
			}
			appendWithoutUpdate(data.substring(0,p));
			data = data.substring(p+1);
			if (data.equals("")) 
				break;
		}
		if (isShowing()) {  // && !(ij.macro.Interpreter.isBatchMode()&&title.equals("Results"))
			updateDisplay();
			unsavedLines = true;
		}
	}
	
	/** Adds strings contained in an ArrayList to the end of this TextPanel. */
	public void append(ArrayList list) {
		if (list==null) return;
		if (vData==null) setColumnHeadings("");
		for (int i=0; i<list.size(); i++)
			appendWithoutUpdate((String)list.get(i));
		if (isShowing()) {
			updateDisplay();
			unsavedLines = true;
		}
	}

	/** Adds a single line to the end of this TextPanel without updating the display. */
	public void appendWithoutUpdate(String data) {
		if (vData!=null) {
			char[] chars = data.toCharArray();
			vData.addElement(chars);
			iRowCount++;
		}
	}

	public void updateDisplay() {
		iY=iRowHeight*(iRowCount+1);
		adjustVScroll();
		if (iColCount>1 && iRowCount<=10 && !columnsManuallyAdjusted)
			iColWidth[0] = 0; // forces column width calculation
		tc.repaint();
	}

	String getCell(int column, int row) {
		if (column<0||column>=iColCount||row<0||row>=iRowCount)
			return null;
		return new String(tc.getChars(column, row));
	}

	synchronized void adjustVScroll() {
		if(iRowHeight==0) return;
		Dimension d = tc.getSize();
		int value = iY/iRowHeight;
		int visible = d.height/iRowHeight;
		int maximum = iRowCount+1;
		if (visible<0) visible=0;
		if (visible>maximum) visible=maximum;
		if (value>(maximum-visible)) value=maximum-visible;
		sbVert.setValues(value,visible,0,maximum);
		iY=iRowHeight*value;
	}

	synchronized void adjustHScroll() {
		if(iRowHeight==0) return;
		Dimension d = tc.getSize();
		int w=0;
		for(int i=0;i<iColCount;i++)
			w+=iColWidth[i];
		iGridWidth=w;
		sbHoriz.setValues(iX,d.width,0,iGridWidth);
		iX=sbHoriz.getValue();
	}

	public void adjustmentValueChanged (AdjustmentEvent e) {
		iX=sbHoriz.getValue();
 		iY=iRowHeight*sbVert.getValue();
		tc.repaint();
 	}
    
	public void mousePressed (MouseEvent e) {
		int x=e.getX(), y=e.getY();
		if ((e.isPopupTrigger() && e.getButton() != 0)|| e.isMetaDown())
			pm.show(e.getComponent(),x,y);
 		else if (e.isShiftDown())
			extendSelection(x, y);
		else {
 			select(x, y);
 			handleDoubleClick();
 		}
	}
	
	void handleDoubleClick() {
		if (selStart<0 || selStart!=selEnd || iColCount!=1) return;
		boolean doubleClick = System.currentTimeMillis()-mouseDownTime<=DOUBLE_CLICK_THRESHOLD;
		mouseDownTime = System.currentTimeMillis();
		if (doubleClick) {
			char[] chars = (char[])(vData.elementAt(selStart));
			String s = new String(chars);
			int index = s.indexOf(": ");
			if (index>-1 && !s.endsWith(": "))
				s = s.substring(index+2); // remove sequence number added by ListFilesRecursively
			if (s.indexOf(File.separator)!=-1 ||  s.indexOf(".")!=-1) {
				filePath = s;
				Thread thread = new Thread(this, "Open");
				thread.setPriority(thread.getPriority()-1);
				thread.start();
			}
		}
	}
	
    /** For better performance, open double-clicked files on 
    	separate thread instead of on event dispatch thread. */
    public void run() {
        if (filePath!=null) IJ.open(filePath);
    }

	public void mouseExited (MouseEvent e) {
		if(bDrag) {
			setCursor(defaultCursor);
			bDrag=false;
		}
	}
	
	public void mouseMoved (MouseEvent e) {
		int x=e.getX(), y=e.getY();
		if(y<=iRowHeight) {
			int xb=x;
			x=x+iX-iGridWidth;
			int i=iColCount-1;
			for(;i>=0;i--) {
				if(x>-7 && x<7) break;
				x+=iColWidth[i];        
			}
			if(i>=0) {
				if(!bDrag) {
					setCursor(resizeCursor);
					bDrag=true;
					iXDrag=xb-iColWidth[i];
					iColDrag=i;
				}
				return;
			}
		}
		if(bDrag) {
			setCursor(defaultCursor);
			bDrag=false;
		}
	}
	
	public void mouseDragged (MouseEvent e) {
		if ((e.isPopupTrigger() && e.getButton() != 0) || e.isMetaDown())
			return;
		int x=e.getX(), y=e.getY();
		if(bDrag && x<tc.getSize().width) {
			int w=x-iXDrag;
			if(w<0) w=0;
			iColWidth[iColDrag]=w;
			columnsManuallyAdjusted = true;
			adjustHScroll();
			tc.repaint();
		} else {
			extendSelection(x, y);
		}
	}

 	public void mouseReleased (MouseEvent e) {}
	public void mouseClicked (MouseEvent e) {}
	public void mouseEntered (MouseEvent e) {}
	
	public void mouseWheelMoved(MouseWheelEvent event) {
		synchronized(this) {
			int rot = event.getWheelRotation();
			sbVert.setValue(sbVert.getValue()+rot);
			iY=iRowHeight*sbVert.getValue();
			tc.repaint();
		}
	}

	/** Unused keyPressed and keyTyped events will be passed to 'listener'.*/
	public void addKeyListener(KeyListener listener) {
		keyListener = listener;
	}
	
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key==KeyEvent.VK_BACK_SPACE)
			clearSelection();
		else if (keyListener!=null&&key!=KeyEvent.VK_S&& key!=KeyEvent.VK_C && key!=KeyEvent.VK_X&& key!=KeyEvent.VK_A)
			keyListener.keyPressed(e);
		
	}
	
	public void keyReleased (KeyEvent e) {}
	
	public void keyTyped (KeyEvent e) {
		if (keyListener!=null)
			keyListener.keyTyped(e);
	}
  
	public void actionPerformed (ActionEvent e) {
		String cmd=e.getActionCommand();
		doCommand(cmd);
	}

 	void doCommand(String cmd) {
 		if (cmd==null)
 			return;
		if (cmd.equals("Save As..."))
			saveAs("");
		else if (cmd.equals("Cut"))
			cutSelection();
		else if (cmd.equals("Copy"))
			copySelection();
		else if (cmd.equals("Clear"))
			clearSelection();
		else if (cmd.equals("Select All"))
			selectAll();
		else if (cmd.equals("Rename..."))
			rename(null);
		else if (cmd.equals("Duplicate..."))
			duplicate();
		else if (cmd.equals("Summarize"))
			IJ.doCommand("Summarize");
		else if (cmd.equals("Distribution..."))
			IJ.doCommand("Distribution...");
		else if (cmd.equals("Clear Results"))
			IJ.doCommand("Clear Results");
		else if (cmd.equals("Set Measurements..."))
			IJ.doCommand("Set Measurements...");
 		else if (cmd.equals("Options..."))
			IJ.doCommand("Input/Output...");
	}
 	
 	public void lostOwnership (Clipboard clip, Transferable cont) {}

	void rename(String title2) {
		if (rt==null) return;
		if (title2!=null && title2.equals(""))
			title2 = null;
		Component comp = getParent();
		if (comp==null || !(comp instanceof TextWindow))
			return;
		TextWindow tw = (TextWindow)comp;
		if (title2==null) {
			GenericDialog gd = new GenericDialog("Rename", tw);
			gd.addStringField("Title:", "Results2", 20);
			gd.showDialog();
			if (gd.wasCanceled())
				return;
			title2 = gd.getNextString();
		}
		String title1 = title;
		if (title!=null && title.equals("Results")) {
			IJ.setTextPanel(null);
			Analyzer.setUnsavedMeasurements(false);
			Analyzer.setResultsTable(null);
			Analyzer.resetCounter();
		}
		if (title2.equals("Results")) {
			tw.setVisible(false);
			tw.dispose();
			WindowManager.removeWindow(tw);
			flush();
			rt.show("Results");
		} else {
			tw.setTitle(title2);
			int mbSize = tw.mb!=null?tw.mb.getMenuCount():0;
			if (mbSize>0 && tw.mb.getMenu(mbSize-1).getLabel().equals("Results"))
				tw.mb.remove(mbSize-1);
			title = title2;
		}
		Menus.updateWindowMenuItem(title1, title2);
		if (Recorder.record)
			Recorder.recordString("IJ.renameResults(\""+title2+"\");\n");
	}

	void duplicate() {
		if (rt==null) return;
		ResultsTable rt2 = (ResultsTable)rt.clone();
		String title2 = IJ.getString("Title:", "Results2");
		if (!title2.equals("")) {
			if (title2.equals("Results")) title2 = "Results2";
			rt2.show(title2);
		}
	}
	
	void select(int x,int y) {
		Dimension d = tc.getSize();
		if(iRowHeight==0 || x>d.width || y>d.height)
			return;
     	int r=(y/iRowHeight)-1+iFirstRow;
     	int lineWidth = iGridWidth;
		if (iColCount==1 && tc.fMetrics!=null && r>=0 && r<iRowCount) {
			char[] chars = (char[])vData.elementAt(r);
			lineWidth = Math.max(tc.fMetrics.charsWidth(chars,0,chars.length), iGridWidth);
		}
      	if(r>=0 && r<iRowCount && x<lineWidth) {
			selOrigin = r;
			selStart = r;
			selEnd = r;
		} else {
			resetSelection();
			selOrigin = r;
			if (r>=iRowCount)
				selOrigin = iRowCount-1;
			//System.out.println("select: "+selOrigin);
		}
		tc.repaint();
		selLine=r;
	}

	void extendSelection(int x,int y) {
		Dimension d = tc.getSize();
		if(iRowHeight==0 || x>d.width || y>d.height)
			return;
     	int r=(y/iRowHeight)-1+iFirstRow;
		//System.out.println(r+"  "+selOrigin);
     	if(r>=0 && r<iRowCount) {
			if (r<selOrigin) {
				selStart = r;
				selEnd = selOrigin;
				
			} else {
				selStart = selOrigin;
				selEnd = r;
			}
		}
		tc.repaint();
		selLine=r;
	}

    /** Converts a y coordinate in pixels into a row index. */
    public int rowIndex(int y) {
        if (y > tc.getSize().height)
        	return -1;
        else
        	return (y/iRowHeight)-1+iFirstRow;
    }

	/**
	Copies the current selection to the system clipboard. 
	Returns the number of characters copied.
	*/
	public int copySelection() {
		if (Recorder.record && title.equals("Results"))
			Recorder.record("String.copyResults");
		if (selStart==-1 || selEnd==-1)
			return copyAll();
		StringBuffer sb = new StringBuffer();
		if (Prefs.copyColumnHeaders && labels!=null && !labels.equals("") && selStart==0 && selEnd==iRowCount-1) {
			if (Prefs.noRowNumbers) {
				String s = labels;
				int index = s.indexOf("\t");
				if (index!=-1)
					s = s.substring(index+1, s.length());
				sb.append(s);
			} else
				sb.append(labels);
			sb.append('\n');
		}
		for (int i=selStart; i<=selEnd; i++) {
			char[] chars = (char[])(vData.elementAt(i));
			String s = new String(chars);
			if (s.endsWith("\t"))
				s = s.substring(0, s.length()-1);
			if (Prefs.noRowNumbers) {
				int index = s.indexOf("\t");
				if (index!=-1)
					s = s.substring(index+1, s.length());
				sb.append(s);
			} else
				sb.append(s);
			if (i<selEnd || selEnd>selStart) sb.append('\n');
		}
		String s = new String(sb);
		Clipboard clip = getToolkit().getSystemClipboard();
		if (clip==null) return 0;
		StringSelection cont = new StringSelection(s);
		clip.setContents(cont,this);
		if (s.length()>0) {
			IJ.showStatus((selEnd-selStart+1)+" lines copied to clipboard");
            // @todo What is this for ????
//			if (this.getParent() instanceof ImageJ)
//				Analyzer.setUnsavedMeasurements(false);
		}
		return s.length();
	}
	
	int copyAll() {
		selectAll();
		int count = selEnd - selStart;
		if (count>0)
			copySelection();
		resetSelection();
		unsavedLines = false;
		return count;
	}
	
	void cutSelection() {
		if (selStart==-1 || selEnd==-1)
			selectAll();
		copySelection();
		clearSelection();
	}	

	/** Deletes the selected lines. */
	public void clearSelection() {
		if (selStart==-1 || selEnd==-1) {
			if (getLineCount()>0)
				IJ.error("Selection required");
			return;
		}
		if (Recorder.record)
			Recorder.recordString("IJ.deleteRows("+selStart+", "+selEnd+");\n");
		if (selStart==0 && selEnd==(iRowCount-1)) {
			vData.removeAllElements();
			iRowCount = 0;
			if (rt!=null) {
				if (IJ.isResultsWindow() && IJ.getTextPanel()==this) {
					Analyzer.setUnsavedMeasurements(false);
					Analyzer.resetCounter();
				} else
					rt.reset();
			}
		} else {
			int rowCount = iRowCount;
			boolean atEnd = rowCount-selEnd<8;
			int count = selEnd-selStart+1;
			for (int i=0; i<count; i++) {
				vData.removeElementAt(selStart);
				iRowCount--;
			}
			if (rt!=null && rowCount==rt.getCounter()) {
				for (int i=0; i<count; i++)
					rt.deleteRow(selStart);
				rt.show(title);
				if (!atEnd) {
					iY = 0;
					tc.repaint();
				}
			}
		}
		selStart=-1; selEnd=-1; selOrigin=-1; selLine=-1; 
		adjustVScroll();
		tc.repaint();
	}
	
	/** Deletes all the lines. */
	public void clear() {
		if (vData==null) return;
		vData.removeAllElements();
		iRowCount = 0;
		selStart=-1; selEnd=-1; selOrigin=-1; selLine=-1;
		adjustVScroll();
		tc.repaint();
	}

	/** Selects all the lines in this TextPanel. */
	public void selectAll() {
		if (selStart==0 && selEnd==iRowCount-1) {
			resetSelection();
			return;
		}
		selStart = 0;
		selEnd = iRowCount-1;
		selOrigin = 0;
		tc.repaint();
		selLine=-1;
	}

	/** Clears the selection, if any. */
	public void resetSelection() {
		selStart=-1;
		selEnd=-1;
		selOrigin=-1;
		selLine=-1;
		if (iRowCount>0)
			tc.repaint();
	}
	
	/** Creates a selection. */
	public void setSelection (int startLine, int endLine) {
		if (startLine>endLine) endLine = startLine;
		if (startLine<0) startLine = 0;
		if (endLine<0) endLine = 0;
		if (startLine>=iRowCount) startLine = iRowCount-1;
		if (endLine>=iRowCount) endLine = iRowCount-1;
		selOrigin = startLine;
		selStart = startLine;
		selEnd = endLine;
		tc.repaint();
	}

	/** Writes all the text in this TextPanel to a file. */
	public void save(PrintWriter pw) {
		resetSelection();
		if (labels!=null && !labels.equals(""))
			pw.println(labels);
		for (int i=0; i<iRowCount; i++) {
			char[] chars = (char[])(vData.elementAt(i));
			String s = new String(chars);
			if (s.endsWith("\t"))
				s = s.substring(0, s.length()-1);
			pw.println(s);
		}
		unsavedLines = false;
	}

	/** Saves all the text in this TextPanel to a file. Set
		'path' to "" to display a save as dialog. Returns
		'false' if the user cancels the save as dialog.*/
	public boolean saveAs(String path) {
		boolean isResults = IJ.isResultsWindow() && IJ.getTextPanel()==this;
		boolean summarized = false;
		if (isResults) {
			String lastLine = iRowCount>=2?getLine(iRowCount-2):null;
			summarized = lastLine!=null && lastLine.startsWith("Max");
		}
		if (rt!=null && !summarized) {
			if (path==null || path.equals("")) {
				IJ.wait(10);
				String name = isResults?"Results":title;
				SaveDialog sd = new SaveDialog("Save Results", name, Prefs.get("options.ext", ".xls"));
				String file = sd.getFileName();
				if (file==null) return false;
				path = sd.getDirectory() + file;
			}
			try {
				rt.saveAs(path);
			} catch (IOException e) {
				IJ.error(""+e);
			}
		} else {
			if (path.equals("")) {
				IJ.wait(10);
				boolean hasHeadings = !getColumnHeadings().equals("");
				String ext = isResults||hasHeadings?Prefs.get("options.ext", ".xls"):".txt";
				if (ext.equals(".csv")) ext = ".txt";
				SaveDialog sd = new SaveDialog("Save as Text", title, ext);
				String file = sd.getFileName();
				if (file == null) return false;
				path = sd.getDirectory() + file;
			}
			PrintWriter pw = null;
			try {
				FileOutputStream fos = new FileOutputStream(path);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				pw = new PrintWriter(bos);
			}
			catch (IOException e) {
				//IJ.write("" + e);
				return true;
			}
			save(pw);
			pw.close();
		}
		if (isResults) {
			Analyzer.setUnsavedMeasurements(false);
			if (Recorder.record && !IJ.isMacro())
				Recorder.record("saveAs", "Results", path);
		} else if (rt!=null) {
			if (Recorder.record && !IJ.isMacro())
				Recorder.record("saveAs", "Results", path);
		} else {
			if (Recorder.record && !IJ.isMacro())
				Recorder.record("saveAs", "Text", path);
		}
		IJ.showStatus("");
		return true;
	}

	/** Returns all the text as a string. */
	public String getText() {
		StringBuffer sb = new StringBuffer();
		if (labels!=null && !labels.equals("")) {
			sb.append(labels);
			sb.append('\n');
		}
		for (int i=0; i<iRowCount; i++) {
			char[] chars = (char[])(vData.elementAt(i));
			sb.append(chars);
			sb.append('\n');
		}
		return new String(sb);
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	/** Returns the number of lines of text in this TextPanel. */
	public int getLineCount() {
		return iRowCount;
	}

	/** Returns the specified line as a string. The argument
		must be greater than or equal to zero and less than 
		the value returned by getLineCount(). */
	public String getLine(int index) {
		if (index<0 || index>=iRowCount)
			throw new IllegalArgumentException("index out of range: "+index);
		return new String((char[])(vData.elementAt(index)));
	}
	
	/** Replaces the contents of the specified line, where 'index'
		must be greater than or equal to zero and less than 
		the value returned by getLineCount(). */
	public void setLine(int index, String s) {
		if (index<0 || index>=iRowCount)
			throw new IllegalArgumentException("index out of range: "+index);
		if (vData!=null) {
			vData.setElementAt(s.toCharArray(), index);	
			tc.repaint();
		}
	}

	/** Returns the index of the first selected line, or -1 
		if there is no slection. */
	public int getSelectionStart() {
		return selStart;
	}

	/** Returns the index of the last selected line, or -1 
		if there is no slection. */
	public int getSelectionEnd() {
		return selEnd;
	}
	
	/** Sets the ResultsTable associated with this TextPanel. */
	public void setResultsTable(ResultsTable rt) {
		this.rt = rt;
	}
	
	/** Returns the ResultsTable associated with this TextPanel, or null. */
	public ResultsTable getResultsTable() {
		return rt;
	}

	public void scrollToTop() {
		sbVert.setValue(0);
		iY = 0;
		for(int i=0;i<iColCount;i++)
			tc.calcAutoWidth(i);
		adjustHScroll();
		tc.repaint();
	}
        
	void flush() {
		if (vData!=null)
			vData.removeAllElements();
		vData = null;
	}

}
