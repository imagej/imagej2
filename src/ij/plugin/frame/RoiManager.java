package ij.plugin.frame;
import ijx.gui.IjxImageCanvas;
import ijx.IjxApplication;
import ijx.IjxImagePlus;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.awt.List;
import java.util.zip.*;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import ij.plugin.filter.*;
import ij.util.*;
import ij.macro.*;
import ij.measure.*;

/** This plugin implements the Analyze/Tools/ROI Manager command. */
public class RoiManager extends PlugInFrame implements ActionListener, ItemListener, MouseListener, MouseWheelListener {

	public static final String LOC_KEY = "manager.loc";
	static final int BUTTONS = 10;
	static final int DRAW=0, FILL=1, LABEL=2;
	static final int MENU=0, COMMAND=1, MULTI=2;
	static int rows = 15;
	static boolean allowMultipleSelections = true; 
	static String moreButtonLabel = "More "+'\u00bb';
	Panel panel;
	static Frame instance;
	java.awt.List list;
	Hashtable rois = new Hashtable();
	Roi roiCopy;
	boolean canceled;
	boolean macro;
	boolean ignoreInterrupts;
	PopupMenu pm;
	Button moreButton, colorButton;
	static boolean measureAll = true;
	static boolean onePerSlice = true;
	static boolean restoreCentered;
	int prevID;


	public RoiManager() {
		super("ROI Manager");
		if (instance!=null) {
			instance.toFront();
			return;
		}
		instance = this;
		list = new List(rows, allowMultipleSelections);
		showWindow();
	}
	
	public RoiManager(boolean hideWindow) {
		super("ROI Manager");
		list = new List(rows, allowMultipleSelections);
	}

	void showWindow() {
		IjxApplication ij = IJ.getInstance();
 		addKeyListener(ij);
 		addMouseListener(this);
		addMouseWheelListener(this);
		WindowManager.addWindow(this);
		setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		list.add("012345678901234");
		list.addItemListener(this);
 		list.addKeyListener(ij);
 		list.addMouseListener(this);
 		list.addMouseWheelListener(this);
		if (IJ.isLinux()) list.setBackground(Color.white);
		add(list);
		panel = new Panel();
		int nButtons = BUTTONS;
		panel.setLayout(new GridLayout(nButtons, 1, 5, 0));
		addButton("Add [t]");
		addButton("Update");
		addButton("Delete");
		addButton("Rename");
		addButton("Open");
		addButton("Save");
		addButton("Measure");
		addButton("Deselect");
		addButton("Show All");
		addButton(moreButtonLabel);
		add(panel);		
		addPopupMenu();
		pack();
		list.remove(0);
		Point loc = Prefs.getLocation(LOC_KEY);
		if (loc!=null)
			setLocation(loc);
		else
			GUI.center(this);
		show();
	}

	void addButton(String label) {
		Button b = new Button(label);
		b.addActionListener(this);
		b.addKeyListener(IJ.getInstance());
 		b.addMouseListener(this);
 		if (label.equals(moreButtonLabel)) moreButton = b;
		panel.add(b);
	}

	void addPopupMenu() {
		pm=new PopupMenu();
		//addPopupItem("Select All");
		addPopupItem("Draw");
		addPopupItem("Fill");
		addPopupItem("Label");
		pm.addSeparator();
		addPopupItem("Combine");
		addPopupItem("Split");
		addPopupItem("Add Particles");
		addPopupItem("Multi Measure");
		addPopupItem("Sort");
		addPopupItem("Specify...");
		addPopupItem("Remove Slice Info");
		addPopupItem("Help");
		addPopupItem("Options...");
		add(pm);
	}

	void addPopupItem(String s) {
		MenuItem mi=new MenuItem(s);
		mi.addActionListener(this);
		pm.add(mi);
	}
	
	public void actionPerformed(ActionEvent e) {
		int modifiers = e.getModifiers();
		boolean altKeyDown = (modifiers&ActionEvent.ALT_MASK)!=0 || IJ.altKeyDown();
		boolean shiftKeyDown = (modifiers&ActionEvent.SHIFT_MASK)!=0 || IJ.shiftKeyDown();
		IJ.setKeyUp(KeyEvent.VK_ALT);
		IJ.setKeyUp(KeyEvent.VK_SHIFT);
		String label = e.getActionCommand();
		if (label==null)
			return;
		String command = label;
		if (command.equals("Add [t]"))
			add(shiftKeyDown, altKeyDown);
		else if (command.equals("Update"))
			update();
		else if (command.equals("Delete"))
			delete(false);
		else if (command.equals("Rename"))
			rename(null);
		else if (command.equals("Open"))
			open(null);
		else if (command.equals("Save"))
			save();
		else if (command.equals("Measure"))
			measure(MENU);
		else if (command.equals("Show All"))
			showAll();
		else if (command.equals("Draw"))
			drawOrFill(DRAW);
		else if (command.equals("Fill"))
			drawOrFill(FILL);
		else if (command.equals("Label"))
			drawOrFill(LABEL);
		else if (command.equals("Deselect"))
			select(-1);
		else if (command.equals(moreButtonLabel)) {
			Point ploc = panel.getLocation();
			Point bloc = moreButton.getLocation();
			pm.show(this, ploc.x, bloc.y);
		} else if (command.equals("Select All"))
			selectAll();
		else if (command.equals("Combine"))
			combine();
		else if (command.equals("Split"))
			split();
		else if (command.equals("Add Particles"))
			addParticles();
		else if (command.equals("Multi Measure"))
			multiMeasure();
		else if (command.equals("Sort"))
			sort();
		else if (command.equals("Specify..."))
			specify();
		else if (command.equals("Remove Slice Info"))
			removeSliceInfo();
		else if (command.equals("Help"))
			help();
		else if (command.equals("Options..."))
			options();
		else if (command.equals("\"Show All\" Color..."))
			setShowAllColor();
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange()==ItemEvent.SELECTED && !ignoreInterrupts) {
			int index = 0;
            try {index = Integer.parseInt(e.getItem().toString());}
            catch (NumberFormatException ex) {}
			if (index<0) index = 0;
			if (!IJ.shiftKeyDown() && !IJ.isMacintosh()) {
				int[] indexes = list.getSelectedIndexes();
				for (int i=0; i<indexes.length; i++) {
					if (indexes[i]!=index)
						list.deselect(indexes[i]);
				}
			}
			if (WindowManager.getCurrentImage()!=null) {
				restore(index, true);
				if (Recorder.record) Recorder.record("roiManager", "Select", index);
			}
		}
	}
	
	void add(boolean shiftKeyDown, boolean altKeyDown) {
		if (shiftKeyDown)
			addAndDraw(altKeyDown);
		else if (altKeyDown)
			add(true);
		else
			add(false);
	}

	boolean add(boolean promptForName) {
		IjxImagePlus imp = getImage();
		if (imp==null)
			return false;
		Roi roi = imp.getRoi();
		if (roi==null) {
			error("The active image does not have a selection.");
			return false;
		}
		int n = list.getItemCount();
		if (n>0 && !IJ.isMacro()) {
			// check for duplicate
			String label = list.getItem(n-1);
			Roi roi2 = (Roi)rois.get(label);
			if (roi2!=null) {
				int slice2 = getSliceNumber(label);
				if (roi.equals(roi2) && (slice2==-1||slice2==imp.getCurrentSlice()) && imp.getID()==prevID && !Interpreter.isBatchMode())
					return false;
			}
		}
		prevID = imp.getID();
		String name = roi.getName();
		if (isStandardName(name))
			name = null;
		String label = name!=null?name:getLabel(imp, roi, -1);
		if (promptForName)
			label = promptForName(label);
		else
			label = getUniqueName(label);
		if (label==null) return false;
		list.add(label);
		roi.setName(label);
		roiCopy = (Roi)roi.clone();
		Calibration cal = imp.getCalibration();
		if (cal.xOrigin!=0.0 || cal.yOrigin!=0.0) {
			Rectangle r = roiCopy.getBounds();
			roiCopy.setLocation(r.x-(int)cal.xOrigin, r.y-(int)cal.yOrigin);
		}
		rois.put(label, roiCopy);
		updateShowAll();
		if (Recorder.record) Recorder.record("roiManager", "Add");
		return true;
	}
	
	/** Adds the specified ROI to the list. The third argument ('n') will 
		be used to form the first part of the ROI lable if it is >= 0. */
	public void add(IjxImagePlus imp, Roi roi, int n) {
		if (roi==null) return;
		String label = getLabel(imp, roi, n);
		if (label==null) return;
		list.add(label);
		roi.setName(label);
		roiCopy = (Roi)roi.clone();
		Calibration cal = imp.getCalibration();
		if (cal.xOrigin!=0.0 || cal.yOrigin!=0.0) {
			Rectangle r = roiCopy.getBounds();
			roiCopy.setLocation(r.x-(int)cal.xOrigin, r.y-(int)cal.yOrigin);
		}
		rois.put(label, roiCopy);
	}

	boolean isStandardName(String name) {
		if (name==null) return false;
		boolean isStandard = false;
		int len = name.length();
		if (len>=14 && name.charAt(4)=='-' && name.charAt(9)=='-' )
			isStandard = true;
		else if (len>=17 && name.charAt(5)=='-' && name.charAt(11)=='-' )
			isStandard = true;
		else if (len>=9 && name.charAt(4)=='-')
			isStandard = true;
		else if (len>=11 && name.charAt(5)=='-')
			isStandard = true;
		return isStandard;
	}
	
	String getLabel(IjxImagePlus imp, Roi roi, int n) {
		Rectangle r = roi.getBounds();
		int xc = r.x + r.width/2;
		int yc = r.y + r.height/2;
		if (n>=0)
			{xc = yc; yc=n;}
		if (xc<0) xc = 0;
		if (yc<0) yc = 0;
		int digits = 4;
		String xs = "" + xc;
		if (xs.length()>digits) digits = xs.length();
		String ys = "" + yc;
		if (ys.length()>digits) digits = ys.length();
		xs = "000000" + xc;
		ys = "000000" + yc;
		String label = ys.substring(ys.length()-digits) + "-" + xs.substring(xs.length()-digits);
		if (imp.getStackSize()>1) {
			String zs = "000000" + imp.getCurrentSlice();
			label = zs.substring(zs.length()-digits) + "-" + label;
		}
		return label;
	}

	void addAndDraw(boolean altKeyDown) {
		if (altKeyDown) {
			if (!add(true)) return;
		} else if (!add(false))
			return;
		IjxImagePlus imp = WindowManager.getCurrentImage();
		Undo.setup(Undo.COMPOUND_FILTER, imp);
		IJ.run("Draw");
		Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
		if (Recorder.record) Recorder.record("roiManager", "Add & Draw");
	}
	
	boolean delete(boolean replacing) {
		int count = list.getItemCount();
		if (count==0)
			return error("The list is empty.");
		int index[] = list.getSelectedIndexes();
		if (index.length==0 || (replacing&&count>1)) {
			String msg = "Delete all items on the list?";
			if (replacing)
				msg = "Replace items on the list?";
			canceled = false;
			if (!IJ.isMacro() && !macro) {
				YesNoCancelDialog d = new YesNoCancelDialog(this, "ROI Manager", msg);
				if (d.cancelPressed())
					{canceled = true; return false;}
				if (!d.yesPressed()) return false;
			}
			index = getAllIndexes();
		}
		for (int i=count-1; i>=0; i--) {
			boolean delete = false;
			for (int j=0; j<index.length; j++) {
				if (index[j]==i)
					delete = true;
			}
			if (delete) {
				rois.remove(list.getItem(i));
				list.remove(i);
			}
		}
		updateShowAll();
		if (Recorder.record) Recorder.record("roiManager", "Delete");
		return true;
	}
	
	boolean update() {
		IjxImagePlus imp = getImage();
		if (imp==null) return false;
		IjxImageCanvas ic = imp.getCanvas();
		boolean showingAll = ic!=null &&  ic.getShowAllROIs();
		Roi roi = imp.getRoi();
		if (roi==null) {
			error("The active image does not have a selection.");
			return false;
		}
		int index = list.getSelectedIndex();
		if (index<0 && !showingAll)
			return error("Exactly one item in the list must be selected.");
		if (index>=0) {
			String name = list.getItem(index);
			rois.remove(name);
			rois.put(name, (Roi)roi.clone());
		}
		if (Recorder.record) Recorder.record("roiManager", "Update");
		if (showingAll) imp.draw();
		return true;
	}

	boolean rename(String name2) {
		int index = list.getSelectedIndex();
		if (index<0)
			return error("Exactly one item in the list must be selected.");
		String name = list.getItem(index);
		if (name2==null) name2 = promptForName(name);
		if (name2==null) return false;
		Roi roi = (Roi)rois.get(name);
		rois.remove(name);
		roi.setName(name2);
		rois.put(name2, roi);
		list.replaceItem(name2, index);
		list.select(index);
		if (Recorder.record) Recorder.record("roiManager", "Rename", name2);
		return true;
	}
	
	String promptForName(String name) {
		GenericDialog gd = new GenericDialog("ROI Manager");
		gd.addStringField("Rename As:", name, 20);
		gd.showDialog();
		if (gd.wasCanceled())
			return null;
		String name2 = gd.getNextString();
		name2 = getUniqueName(name2);
		return name2;
	}

	boolean restore(int index, boolean setSlice) {
		String label = list.getItem(index);
		Roi roi = (Roi)rois.get(label);
		IjxImagePlus imp = getImage();
		if (imp==null || roi==null)
			return false;
        if (setSlice) {
            int n = getSliceNumber(label);
            if (n>=1 && n<=imp.getStackSize()) {
            	if (imp.isHyperStack())
                	imp.setPosition(n);
                else
                	imp.setSlice(n);
            }
        }
        Roi roi2 = (Roi)roi.clone();
		Calibration cal = imp.getCalibration();
		Rectangle r = roi2.getBounds();
		if (cal.xOrigin!=0.0 || cal.yOrigin!=0.0)
			roi2.setLocation(r.x+(int)cal.xOrigin, r.y+(int)cal.yOrigin);
		int width= imp.getWidth(), height=imp.getHeight();
		if (restoreCentered) {
			IjxImageCanvas ic = imp.getCanvas();
			if (ic!=null) {
				Rectangle r1 = ic.getSrcRect();
				Rectangle r2 = roi2.getBounds();
				roi2.setLocation(r1.x+r1.width/2-r2.width/2, r1.y+r1.height/2-r2.height/2);
			}
		}
		if (r.x>=width || r.y>=height || (r.x+r.width)<=0 || (r.y+r.height)<=0)
			roi2.setLocation((width-r.width)/2, (height-r.height)/2);
		imp.setRoi(roi2);
		return true;
	}
	
	/** Returns the slice number associated with the specified name,
		or -1 if the name does not include a slice number. */
	public int getSliceNumber(String label) {
		int slice = -1;
		if (label.length()>=14 && label.charAt(4)=='-' && label.charAt(9)=='-')
			slice = (int)Tools.parseDouble(label.substring(0,4),-1);
		else if (label.length()>=17 && label.charAt(5)=='-' && label.charAt(11)=='-')
			slice = (int)Tools.parseDouble(label.substring(0,5),-1);
		else if (label.length()>=20 && label.charAt(6)=='-' && label.charAt(13)=='-')
			slice = (int)Tools.parseDouble(label.substring(0,6),-1);
		return slice;
	}
	
	void open(String path) {
		Macro.setOptions(null);
		String name = null;
		if (path==null || path.equals("")) {
			OpenDialog od = new OpenDialog("Open Selection(s)...", "");
			String directory = od.getDirectory();
			name = od.getFileName();
			if (name==null)
				return;
			path = directory + name;
		}
		if (Recorder.record) Recorder.record("roiManager", "Open", path);
		if (path.endsWith(".zip")) {
			openZip(path);
			return;
		}
		Opener o = new Opener();
		if (name==null) name = o.getName(path);
		Roi roi = o.openRoi(path);
		if (roi!=null) {
			if (name.endsWith(".roi"))
				name = name.substring(0, name.length()-4);
			name = getUniqueName(name);
			list.add(name);
			rois.put(name, roi);
		}		
		updateShowAll();
	}
	
	// Modified on 2005/11/15 by Ulrik Stervbo to only read .roi files and to not empty the current list
	void openZip(String path) { 
		ZipInputStream in = null; 
		ByteArrayOutputStream out; 
		int nRois = 0; 
		try { 
			in = new ZipInputStream(new FileInputStream(path)); 
			byte[] buf = new byte[1024]; 
			int len; 
			ZipEntry entry = in.getNextEntry(); 
			while (entry!=null) { 
				String name = entry.getName(); 
				if (name.endsWith(".roi")) { 
					out = new ByteArrayOutputStream(); 
					while ((len = in.read(buf)) > 0) 
						out.write(buf, 0, len); 
					out.close(); 
					byte[] bytes = out.toByteArray(); 
					RoiDecoder rd = new RoiDecoder(bytes, name); 
					Roi roi = rd.getRoi(); 
					if (roi!=null) { 
						name = name.substring(0, name.length()-4); 
						name = getUniqueName(name); 
						list.add(name); 
						rois.put(name, roi); 
						nRois++;
					} 
				} 
				entry = in.getNextEntry(); 
			} 
			in.close(); 
		} catch (IOException e) {error(e.toString());} 
		if(nRois==0)
				error("This ZIP archive does not appear to contain \".roi\" files");
		updateShowAll();
	} 


	String getUniqueName(String name) {
			String name2 = name;
			int n = 1;
			Roi roi2 = (Roi)rois.get(name2);
			while (roi2!=null) {
				roi2 = (Roi)rois.get(name2);
				if (roi2!=null) {
					int lastDash = name2.lastIndexOf("-");
					if (lastDash!=-1 && name2.length()-lastDash<5)
						name2 = name2.substring(0, lastDash);
					name2 = name2+"-"+n;
					n++;
				}
				roi2 = (Roi)rois.get(name2);
			}
			return name2;
	}
	
	boolean save() {
		if (list.getItemCount()==0)
			return error("The selection list is empty.");
		int[] indexes = list.getSelectedIndexes();
		if (indexes.length==0)
			indexes = getAllIndexes();
		if (indexes.length>1)
			return saveMultiple(indexes, null);
		String name = list.getItem(indexes[0]);
		Macro.setOptions(null);
		SaveDialog sd = new SaveDialog("Save Selection...", name, ".roi");
		String name2 = sd.getFileName();
		if (name2 == null)
			return false;
		String dir = sd.getDirectory();
		Roi roi = (Roi)rois.get(name);
		rois.remove(name);
		if (!name2.endsWith(".roi")) name2 = name2+".roi";
		String newName = name2.substring(0, name2.length()-4);
		rois.put(newName, roi);
		roi.setName(newName);
		list.replaceItem(newName, indexes[0]);
		RoiEncoder re = new RoiEncoder(dir+name2);
		try {
			re.write(roi);
		} catch (IOException e) {
			IJ.error("ROI Manager", e.getMessage());
		}
		return true;
	}

	boolean saveMultiple(int[] indexes, String path) {
		Macro.setOptions(null);
		if (path==null) {
			SaveDialog sd = new SaveDialog("Save ROIs...", "RoiSet", ".zip");
			String name = sd.getFileName();
			if (name == null)
				return false;
			if (!(name.endsWith(".zip") || name.endsWith(".ZIP")))
				name = name + ".zip";
			String dir = sd.getDirectory();
			path = dir+name;
		}
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(path));
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
			RoiEncoder re = new RoiEncoder(out);
			for (int i=0; i<indexes.length; i++) {
				String label = list.getItem(indexes[i]);
				Roi roi = (Roi)rois.get(label);
				if (!label.endsWith(".roi")) label += ".roi";
        		zos.putNextEntry(new ZipEntry(label));
				re.write(roi);
				out.flush();
			}
			out.close();
		}
		catch (IOException e) {
			error(""+e);
			return false;
		}
		if (Recorder.record) Recorder.record("roiManager", "Save", path);
		return true;
	}
		
	boolean measure(int mode) {
		IjxImagePlus imp = getImage();
		if (imp==null)
			return false;
		int[] indexes = list.getSelectedIndexes();
		if (indexes.length==0)
			indexes = getAllIndexes();
        if (indexes.length==0) return false;
		boolean allSliceOne = true;
		for (int i=0; i<indexes.length; i++) {
			String label = list.getItem(indexes[i]);
			if (getSliceNumber(label)>1) allSliceOne = false;
			Roi roi = (Roi)rois.get(label);
		}
		int nSlices = 1;
		if (mode==MULTI)
			nSlices = imp.getStackSize();
		int measurements = Analyzer.getMeasurements();
		if (imp.getStackSize()>1)
			Analyzer.setMeasurements(measurements|Measurements.SLICE);
		int currentSlice = imp.getCurrentSlice();
		for (int slice=1; slice<=nSlices; slice++) {
			if (nSlices>1) imp.setSlice(slice);
			for (int i=0; i<indexes.length; i++) {
				if (restore(indexes[i], nSlices==1&&!allSliceOne))
					IJ.run("Measure");
				else
					break;
			}
		}
		imp.setSlice(currentSlice);
		Analyzer.setMeasurements(measurements);
		if (indexes.length>1)
			IJ.run("Select None");
		if (Recorder.record) Recorder.record("roiManager", "Measure");
		return true;
	}	
	
	/*
	void showIndexes(int[] indexes) {
		for (int i=0; i<indexes.length; i++) {
			String label = list.getItem(indexes[i]);
			Roi roi = (Roi)rois.get(label);
			IJ.log(i+" "+roi.getName());
		}
	}
	*/

	/* This method performs measurements for several ROI's in a stack
		and arranges the results with one line per slice.  By constast, the 
		measure() method produces several lines per slice.  The results 
		from multiMeasure() may be easier to import into a spreadsheet 
		program for plotting or additional analysis. Based on the multi() 
		method in Bob Dougherty's Multi_Measure plugin
		(http://www.optinav.com/Multi-Measure.htm).
	*/
 	boolean multiMeasure() {
		IjxImagePlus imp = getImage();
		if (imp==null) return false;
		int[] indexes = list.getSelectedIndexes();
		if (indexes.length==0)
			indexes = getAllIndexes();
        if (indexes.length==0) return false;
		int measurements = Analyzer.getMeasurements();

		int nSlices = imp.getStackSize();
		if (IJ.isMacro()) {
			if (nSlices>1) measureAll = true;
			onePerSlice = true;
		} else {
			GenericDialog gd = new GenericDialog("Multi Measure");
			if (nSlices>1)
				gd.addCheckbox("Measure All "+nSlices+" Slices", measureAll);
			gd.addCheckbox("One Row Per Slice", onePerSlice);
			int columns = getColumnCount(imp, measurements)*indexes.length;
			String str = nSlices==1?"this option":"both options";
			gd.setInsets(10, 25, 0);
			gd.addMessage(
				"Enabling "+str+" will result\n"+
				"in a table with "+columns+" columns."
			);
			gd.showDialog();
			if (gd.wasCanceled()) return false;
			if (nSlices>1)
				measureAll = gd.getNextBoolean();
			onePerSlice = gd.getNextBoolean();
		}
		if (!measureAll) nSlices = 1;
		int currentSlice = imp.getCurrentSlice();
		if (!onePerSlice)
			return measure(MULTI);

		Analyzer aSys = new Analyzer(); //System Analyzer
		ResultsTable rtSys = Analyzer.getResultsTable();
		ResultsTable rtMulti = new ResultsTable();
		Analyzer aMulti = new Analyzer(imp, measurements, rtMulti); //Private Analyzer

		for (int slice=1; slice<=nSlices; slice++) {
			int sliceUse = slice;
			if(nSlices == 1)sliceUse = currentSlice;
			imp.setSlice(sliceUse);
			rtMulti.incrementCounter();
			int roiIndex = 0;
			for (int i=0; i<indexes.length; i++) {
				if (restore(indexes[i], false)) {
					roiIndex++;
					Roi roi = imp.getRoi();
					ImageStatistics stats = imp.getStatistics(measurements);
					aSys.saveResults(stats, roi); //Save measurements in system results table;
					for (int j=0; j<=rtSys.getLastColumn(); j++){
						float[] col = rtSys.getColumn(j);
						String head = rtSys.getColumnHeading(j);
						if (head!=null && col!=null && !head.equals("Slice"))
							rtMulti.addValue(head+roiIndex,rtSys.getValue(j,rtSys.getCounter()-1));
					}
				} else
					break;
			}
			aMulti.displayResults();
			aMulti.updateHeadings();
		}

		imp.setSlice(currentSlice);
		if (indexes.length>1)
			IJ.run("Select None");
		if (Recorder.record) Recorder.record("roiManager", "Multi Measure");
		return true;
	}
	
	int getColumnCount(IjxImagePlus imp, int measurements) {
		ImageStatistics stats = imp.getStatistics(measurements);
		ResultsTable rt = new ResultsTable();
		Analyzer analyzer = new Analyzer(imp, measurements, rt);
		analyzer.saveResults(stats, null);
		int count = 0;
		for (int i=0; i<=rt.getLastColumn(); i++) {
			float[] col = rt.getColumn(i);
			String head = rt.getColumnHeading(i);
			if (head!=null && col!=null)
				count++;
		}
		return count;
	}

	boolean drawOrFill(int mode) {
		int[] indexes = list.getSelectedIndexes();
		if (indexes.length==0)
			indexes = getAllIndexes();
		IjxImagePlus imp = WindowManager.getCurrentImage();
		imp.killRoi();
		ImageProcessor ip = imp.getProcessor();
		ip.setColor(Toolbar.getForegroundColor());
		ip.snapshot();
		Undo.setup(Undo.FILTER, imp);
		Filler filler = mode==LABEL?new Filler():null;
		int slice = imp.getCurrentSlice();
		for (int i=0; i<indexes.length; i++) {
			String name = list.getItem(indexes[i]);
			Roi roi = (Roi)rois.get(name);
			int type = roi.getType();
			if (roi==null) continue;
			if (mode==FILL&&(type==Roi.POLYLINE||type==Roi.FREELINE||type==Roi.ANGLE))
				mode = DRAW;
            int slice2 = getSliceNumber(name);
            if (slice2>=1 && slice2<=imp.getStackSize()) {
                imp.setSlice(slice2);
				ip = imp.getProcessor();
				ip.setColor(Toolbar.getForegroundColor());
				if (slice2!=slice) Undo.reset();
            }
 			switch (mode) {
				case DRAW: roi.drawPixels(ip); break;
				case FILL: ip.fillPolygon(roi.getPolygon()); break;
				case LABEL:
					roi.drawPixels(ip);
					filler.drawLabel(imp, ip, i+1, roi.getBounds());
					break;
			}
		}
		IjxImageCanvas ic = imp.getCanvas();
		if (ic!=null) ic.setShowAllROIs(false);
		imp.updateAndDraw();
		String str=null;
		switch (mode) {
			case DRAW: str="Draw"; break;
			case FILL: str="Fill"; break;
			case LABEL: str="Label"; imp.updateAndDraw(); break;
		}
		if (Recorder.record) Recorder.record("roiManager", str);
		return true;
	}

	void combine() {
		IjxImagePlus imp = getImage();
		if (imp==null) return;
		int[] indexes = list.getSelectedIndexes();
		if (indexes.length==1) {
			error("More than one item must be selected, or none");
			return;
		}
		if (indexes.length==0)
			indexes = getAllIndexes();
		int nPointRois = 0;
		for (int i=0; i<indexes.length; i++) {
			Roi roi = (Roi)rois.get(list.getItem(indexes[i]));
			if (roi.getType()==Roi.POINT)
				nPointRois++;
			else
				break;
		}
		if (nPointRois==indexes.length)
			combinePoints(imp, indexes);
		else
			combineRois(imp, indexes);
		if (Recorder.record) Recorder.record("roiManager", "Combine");
	}
	
	void combineRois(IjxImagePlus imp, int[] indexes) {
		ShapeRoi s1=null, s2=null;
		for (int i=0; i<indexes.length; i++) {
			Roi roi = (Roi)rois.get(list.getItem(indexes[i]));
			if (roi.isLine() || roi.getType()==Roi.POINT)
				continue;
			Calibration cal = imp.getCalibration();
			if (cal.xOrigin!=0.0 || cal.yOrigin!=0.0) {
				roi = (Roi)roi.clone();
				Rectangle r = roi.getBounds();
				roi.setLocation(r.x+(int)cal.xOrigin, r.y+(int)cal.yOrigin);
			}
			if (s1==null) {
				if (roi instanceof ShapeRoi)
					s1 = (ShapeRoi)roi;
				else
					s1 = new ShapeRoi(roi);
				if (s1==null) return;
			} else {
				if (roi instanceof ShapeRoi)
					s2 = (ShapeRoi)roi;
				else
					s2 = new ShapeRoi(roi);
				if (s2==null) continue;
				if (roi.isArea())
					s1.or(s2);
			}
		}
		if (s1!=null)
			imp.setRoi(s1);
	}

	void combinePoints(IjxImagePlus imp, int[] indexes) {
		int n = indexes.length;
		Polygon[] p = new Polygon[n];
		int points = 0;
		for (int i=0; i<n; i++) {
			Roi roi = (Roi)rois.get(list.getItem(indexes[i]));
			p[i] = roi.getPolygon();
			points += p[i].npoints;
		}
		if (points==0) return;
		int[] xpoints = new int[points];
		int[] ypoints = new int[points];
		int index = 0;
		for (int i=0; i<p.length; i++) {
			for (int j=0; j<p[i].npoints; j++) {
				xpoints[index] = p[i].xpoints[j];
				ypoints[index] = p[i].ypoints[j];
				index++;
			}	
		}
		imp.setRoi(new PointRoi(xpoints, ypoints, xpoints.length));
	}

	void addParticles() {
		String err = IJ.runMacroFile("ij.jar:AddParticles", null);
		if (err!=null && err.length()>0)
			error(err);
	}

	void sort() {
		int n = rois.size();
		if (n==0) return;
		String[] labels = new String[n];
		int index = 0;
		for (Enumeration en=rois.keys(); en.hasMoreElements();)
			labels[index++] = (String)en.nextElement();
		list.removeAll();
		StringSorter.sort(labels);
		for (int i=0; i<labels.length; i++)
			list.add(labels[i]);
		if (Recorder.record) Recorder.record("roiManager", "Sort");
	}
	
	void specify() {
		try {IJ.run("Specify...");}
		catch (Exception e) {return;}
		runCommand("add");
	}
	
	void removeSliceInfo() {
		int[] indexes = list.getSelectedIndexes();
		if (indexes.length==0)
			indexes = getAllIndexes();
		for (int i=0; i<indexes.length; i++) {
			int index = indexes[i];
			String name = list.getItem(index);
			int n = getSliceNumber(name);
			if (n==-1) continue;
			String name2 = name.substring(5, name.length());
			name2 = getUniqueName(name2);
			Roi roi = (Roi)rois.get(name);
			rois.remove(name);
			roi.setName(name2);
			rois.put(name2, roi);
			list.replaceItem(name2, index);
		}
	}

	void help() {
		String macro = "run('URL...', 'url="+IJ.URL+"/docs/menus/analyze.html#manager');";
		new MacroRunner(macro);
	}

	void options() {
		Color c = ImageCanvasHelper.getShowAllColor();
		GenericDialog gd = new GenericDialog("Options");
		gd.addPanel(makeButtonPanel(gd), GridBagConstraints.CENTER, new Insets(5, 0, 0, 0));
		gd.addCheckbox("Associate \"Show All\" ROIs with Slices", Prefs.showAllSliceOnly);
		gd.addCheckbox("Restore ROIs Centered", restoreCentered);
		gd.showDialog();
		if (gd.wasCanceled()) {
			if (c!=ImageCanvasHelper.getShowAllColor())
				ImageCanvasHelper.setShowAllColor(c);
			return;
		}
		Prefs.showAllSliceOnly = gd.getNextBoolean();
		restoreCentered = gd.getNextBoolean();
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null) imp.draw();
	}

	Panel makeButtonPanel(GenericDialog gd) {
		Panel panel = new Panel();
    	//buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		colorButton = new Button("\"Show All\" Color...");
		colorButton.addActionListener(this);
		panel.add(colorButton);
		return panel;
	}
	
	void setShowAllColor() {
            ColorChooser cc = new ColorChooser("\"Show All\" Color", ImageCanvasHelper.getShowAllColor() ,  false);
            ImageCanvasHelper.setShowAllColor(cc.getColor());
	}

	void split() {
		IjxImagePlus imp = getImage();
		if (imp==null) return;
		Roi roi = imp.getRoi();
		if (roi==null || roi.getType()!=Roi.COMPOSITE) {
			error("Image with composite selection required");
			return;
		}
		boolean record = Recorder.record;
		Recorder.record = false;
		Roi[] rois = ((ShapeRoi)roi).getRois();
		for (int i=0; i<rois.length; i++) {
			imp.setRoi(rois[i]);
			add(false);
		}
		Recorder.record = record;
		if (Recorder.record) Recorder.record("roiManager", "Split");
	}
	
	void showAll() {
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			{error("There are no images open."); return;}
		IjxImageCanvas ic = imp.getCanvas();
		if (ic==null) return;
		boolean showingROIs = ic.getShowAllROIs();
		ic.setShowAllROIs(!showingROIs);
		if (Recorder.record)
			Recorder.recordString("setOption(\"Show All\","+(showingROIs?"false":"true")+");\n");
		imp.draw();
	}

	void updateShowAll() {
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) return;
		IjxImageCanvas ic = imp.getCanvas();
		if (ic!=null && ic.getShowAllROIs())
			imp.draw();
	}

	int[] getAllIndexes() {
		int count = list.getItemCount();
		int[] indexes = new int[count];
		for (int i=0; i<count; i++)
			indexes[i] = i;
		return indexes;
	}
		
	IjxImagePlus getImage() {
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) {
			error("There are no images open.");
			return null;
		} else
			return imp;
	}

	boolean error(String msg) {
		new MessageDialog(this, "ROI Manager", msg);
		Macro.abort();
		return false;
	}
	
	public void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID()==WindowEvent.WINDOW_CLOSING) {
			instance = null;	
		}
		if (!IJ.isMacro())
			ignoreInterrupts = false;
	}
	
	/** Returns a reference to the ROI Manager
		or null if it is not open. */
	public static RoiManager getInstance() {
		return (RoiManager)instance;
	}

	/**	Returns the ROI Hashtable.
		@see getCount
		@see getRoisAsArray
	*/
	public Hashtable getROIs() {
		return rois;
	}

	/** Returns the selection list.
		@see getCount
		@see getRoisAsArray
	*/
	public List getList() {
		return list;
	}
	
	/** Returns the ROI count. */
	public int getCount() {
		return list.getItemCount();
	}

	/** Returns the ROIs as an array. */
	public Roi[] getRoisAsArray() {
		int n = list.getItemCount();
		Roi[] array = new Roi[n];
		for (int i=0; i<n; i++) {
			String label = list.getItem(i);
			array[i] = (Roi)rois.get(label);
		}
		return array;
	}
	
	/** Returns the selected ROIs as an array, or
		all the ROIs if none are selected. */
	public Roi[] getSelectedRoisAsArray() {
		int[] indexes = list.getSelectedIndexes();
		if (indexes.length==0)
			indexes = getAllIndexes();
		int n = indexes.length;
		Roi[] array = new Roi[n];
		for (int i=0; i<n; i++) {
			String label = list.getItem(indexes[i]);
			array[i] = (Roi)rois.get(label);
		}
		return array;
	}
			
	/** Returns the name of the selection with the specified index.
		Can be called from a macro using
		<pre>call("ij.plugin.frame.RoiManager.getName", index)</pre>
		Returns "null" if the Roi Manager is not open or index is
		out of range.
	*/
	public static String getName(String index) {
		int i = (int)Tools.parseDouble(index, -1);
		RoiManager instance = getInstance();
		if (instance!=null && i>=0 && i<instance.list.getItemCount())
       	 	return  instance.list.getItem(i);
		else
			return "null";
	}

	/** Executes the ROI Manager "Add", "Add & Draw", "Update", "Delete", "Measure", "Draw",
		"Fill", "Deselect", "Select All", "Combine", "Split", "Sort" or "Multi Measure" command. 
		Returns false if <code>cmd</code>  is not one of these strings. */
	public boolean runCommand(String cmd) {
		cmd = cmd.toLowerCase();
		macro = true;
		boolean ok = true;
		if (cmd.equals("add"))
			add(IJ.shiftKeyDown(), IJ.altKeyDown());
		else if (cmd.equals("add & draw"))
			addAndDraw(false);
		else if (cmd.equals("update"))
			update();
		else if (cmd.equals("delete"))
			delete(false);
		else if (cmd.equals("measure"))
			measure(COMMAND);
		else if (cmd.equals("draw"))
			drawOrFill(DRAW);
		else if (cmd.equals("fill"))
			drawOrFill(FILL);
		else if (cmd.equals("label"))
			drawOrFill(LABEL);
		else if (cmd.equals("combine"))
			combine();
		else if (cmd.equals("split"))
			split();
		else if (cmd.equals("sort"))
			sort();
		else if (cmd.startsWith("multi"))
			multiMeasure();
		else if (cmd.equals("deselect")||cmd.indexOf("all")!=-1) {
			if (IJ.isMacOSX()) ignoreInterrupts = true;
			select(-1);
		} else if (cmd.equals("reset")) {
			if (IJ.isMacOSX() && IJ.isMacro())
				ignoreInterrupts = true;
			list.removeAll();
			rois.clear();
		} else if (cmd.equals("debug")) {
			//IJ.log("Debug: "+debugCount);
			//for (int i=0; i<debugCount; i++)
    		//	IJ.log(debug[i]);
		} else
			ok = false;
		macro = false;
		return ok;
	}

	/** Executes the ROI Manager "Open", "Save" or "Rename" command. Returns false if 
	<code>cmd</code> is not "Open", "Save" or "Rename", or if an error occurs. */
	public boolean runCommand(String cmd, String name) {
		cmd = cmd.toLowerCase();
		macro = true;
		if (cmd.equals("open")) {
			open(name);
			macro = false;
			return true;
		} else if (cmd.equals("save")) {
			if (!name.endsWith(".zip") && !name.equals(""))
				return error("Name must end with '.zip'");
			if (list.getItemCount()==0)
				return error("The selection list is empty.");
			int[] indexes = getAllIndexes();
			boolean ok = false;
			if (name.equals(""))
				ok = saveMultiple(indexes, null);
			else
				ok = saveMultiple(indexes, name);
			macro = false;
			return ok;
		} else if (cmd.equals("rename")) {
			rename(name);
			macro = false;
			return true;
		}
		return false;
	}
	
	//public static String[] debug = new String[100000];
	//public static int debugCount;
	
	public void select(int index) {
		int n = list.getItemCount();
		if (index<0) {
			for (int i=0; i<n; i++)
				if (list.isSelected(i)) list.deselect(i);
			if (Recorder.record) Recorder.record("roiManager", "Deselect");
			return;
		}
		if (index>=n) return;
		boolean mm = list.isMultipleMode();
		if (mm) list.setMultipleMode(false);
		int delay = 1;
		long start = System.currentTimeMillis();
		while (true) {
			list.select(index);
			if (delay>1) IJ.wait(delay);
			if (list.isIndexSelected(index))
				break;
			for (int i=0; i<n; i++)
				if (list.isSelected(i)) list.deselect(i);
			IJ.wait(delay);
			delay *= 2; if (delay>32) delay=32;
			if ((System.currentTimeMillis()-start)>1000L)
				error("Failed to select ROI "+index);
		}
		restore(index, true);	
		if (mm) list.setMultipleMode(true);
	}
	
	public void select(int index, boolean shiftKeyDown, boolean altKeyDown) {
		if (!(shiftKeyDown||altKeyDown))
			select(index);
		IjxImagePlus imp = IJ.getImage();
		if (imp==null) return;
		Roi previousRoi = imp.getRoi();
		if (previousRoi==null)
			{select(index); return;}
		Roi.previousRoi = (Roi)previousRoi.clone();
		String label = list.getItem(index);
		Roi roi = (Roi)rois.get(label);
		if (roi!=null) {
			roi.setImage(imp);
			roi.update(shiftKeyDown, altKeyDown);
		}
	}
	
	void selectAll() {
		boolean allSelected = true;
		int count = list.getItemCount();
		for (int i=0; i<count; i++) {
			if (!list.isIndexSelected(i))
				allSelected = false;
		}
		if (allSelected)
			select(-1);
		else {
			for (int i=0; i<count; i++)
				if (!list.isSelected(i)) list.select(i);
		}
	}

    /** Overrides PlugInFrame.close(). */
    public void close() {
    	super.close();
    	instance = null;
		Prefs.saveLocation(LOC_KEY, getLocation());
    }
    
    public void mousePressed (MouseEvent e) {
		int x=e.getX(), y=e.getY();
		if (e.isPopupTrigger() || e.isMetaDown())
			pm.show(e.getComponent(),x,y);
	}

	public void mouseWheelMoved(MouseWheelEvent event) {
		synchronized(this) {
			int index = list.getSelectedIndex();
			int rot = event.getWheelRotation();
			if (rot<-1) rot = -1;
			if (rot>1) rot = 1;
			index += rot;
			if (index<0) index = 0;
			if (index>=list.getItemCount()) index = list.getItemCount();
			//IJ.log(index+"  "+rot);
			select(index);
			if (IJ.isWindows())
				list.requestFocusInWindow();
		}
	}

 	public void mouseReleased (MouseEvent e) {}
	public void mouseClicked (MouseEvent e) {}
	public void mouseEntered (MouseEvent e) {}
	public void mouseExited (MouseEvent e) {}

}

