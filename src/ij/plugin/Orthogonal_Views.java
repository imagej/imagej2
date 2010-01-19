package ij.plugin;
import ij.*;
import ij.gui.*;
import ij.measure.*;
import ij.process.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
 
/**
 * 
* @author Dimiter Prodanov
* 		  IMEC
* 
* @acknowledgments Many thanks to Jerome Mutterer for the code contributions and testing.
* 				   Thanks to Wayne Rasband for the code that properly handles the image magnification.
* 		
* @version 		1.2 28 April 2009
* 					- added support for arrow keys
* 					- fixed a bug in the cross position calculation
* 					- added FocusListener behavior
* 					- added support for magnification factors
* 				1.1.6 31 March 2009
* 					- added AdjustmentListener behavior thanks to Jerome Mutterer
* 					- improved pane visualization
* 					- added window rearrangement behavior. Initial code suggested by Jerome Mutterer
* 					- bug fixes by Wayne Raspband
* 				1.1 24 March 2009
* 					- improved projection image resizing
* 					- added ImageListener behaviors
* 					- added check-ups
* 					- improved pane updating
* 				1.0.5 23 March 2009
* 					- fixed pane updating issue
* 				1.0 21 March 2009
* 
* @contents This plugin projects dynamically orthogonal XZ and YZ views of a stack. 
* The output images are calibrated, which allows measurements to be performed more easily. 
*/

public class Orthogonal_Views implements PlugIn, MouseListener, MouseMotionListener, KeyListener, ActionListener, 
	ImageListener, WindowListener, AdjustmentListener, MouseWheelListener, FocusListener, CommandListener {

	private ImageWindow win;
	private ImagePlus imp;
	private ImageCanvas canvas;
	private static final int H_ROI=0, H_ZOOM=1;
	private ImagePlus xz_image, yz_image;
	private static int xzID, yzID;
	private ImageProcessor fp1, fp2;
	private double ax, ay, az;
	//private static boolean rotate=(boolean)Prefs.getBoolean(YROT,false);
	//private static boolean sticky=(boolean)Prefs.getBoolean(SPANELS,false);
	private static boolean rotate=false;
	private static boolean sticky=true;
	
	private int xyX, xyY;
	private Calibration cal=null, cal_xz=new Calibration(), cal_yz=new Calibration();
	private double magnification=1.0;
	private Color color = Roi.getColor();
	private static Orthogonal_Views instance;
	private Updater updater = new Updater();
	private double min, max;
	private Dimension screen = IJ.getScreenSize();
	private boolean flipXZ;
	private boolean syncZoom = true;
	private Point crossLoc;
	private boolean firstTime = true;
	 
	public void run(String arg) {
		imp = IJ.getImage();
		if (imp.getStackSize()==1) {
			IJ.error("Othogonal Views", "This command requires a stack.");
			return;
		}
		if (instance!=null && imp==instance.imp) {
			//IJ.log("instance!=null: "+imp+"  "+instance.imp);
			return;
		}
		yz_image = WindowManager.getImage(yzID);
		if (yz_image==null || yz_image.getHeight()!=imp.getHeight() || yz_image.getBitDepth()!=imp.getBitDepth())
			yz_image = new ImagePlus();
		xz_image = WindowManager.getImage(xzID);
		//if (xz_image!=null) IJ.log(imp+"  "+xz_image+"  "+xz_image.getHeight()+"  "+imp.getHeight()+"  "+xz_image.getBitDepth()+"  "+imp.getBitDepth());
		if (xz_image==null || xz_image.getWidth()!=imp.getWidth() || xz_image.getBitDepth()!=imp.getBitDepth())
			xz_image = new ImagePlus();
		instance = this;
		ImageProcessor ip = imp.getProcessor();
		min = ip.getMin();
		max = ip.getMax();
		cal=this.imp.getCalibration();
		double calx=cal.pixelWidth;
		double caly=cal.pixelHeight;
		double calz=cal.pixelDepth;
		ax=1.0;
		ay=caly/calx;
		az=calz/calx;
		win = imp.getWindow();
		canvas = win.getCanvas();
		addListeners(canvas);  
		magnification= canvas.getMagnification();
		imp.killRoi();
		crossLoc = new Point(imp.getWidth()/2,imp.getHeight()/2);
		ImageStack is=imp.getStack();
		calibrate();
		if (createProcessors(is)) {
			if (ip.isColorLut() || ip.isInvertedLut()) {
				ColorModel cm = ip.getColorModel();
				fp1.setColorModel(cm);
				fp2.setColorModel(cm);
			}
			update();
		} else
			dispose();
	}
 
	private void addListeners(ImageCanvas canvass) {
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addKeyListener(this);
		win.addWindowListener ((WindowListener) this);  
		win.addMouseWheelListener((MouseWheelListener) this);
		win.addFocusListener(this);
		Component[] c = win.getComponents();
		//IJ.log(c[1].toString());
		((java.awt.Scrollbar) c[1]).addAdjustmentListener ((AdjustmentListener) this);
		ImagePlus.addImageListener(this);
		Executer.addCommandListener(this);
	}
	 
	private void calibrate() {
		double arat=az/ax;
		double brat=az/ay;
		String unit=cal.getUnit();
		double o_depth=cal.pixelDepth;
		double o_height=cal.pixelHeight;
		double o_width=cal.pixelWidth;
		cal_xz.setUnit(unit);
		if (rotate) {
			cal_xz.pixelHeight=o_depth/arat;
			cal_xz.pixelWidth=o_width*ax;
		} else {
			cal_xz.pixelHeight=o_width*ax;//o_depth/arat;
			cal_xz.pixelWidth=o_depth/arat;
		}
		xz_image.setCalibration(cal_xz);
		cal_yz.setUnit(unit);
		cal_yz.pixelWidth=o_height*ay;
		cal_yz.pixelHeight=o_depth/brat;
		yz_image.setCalibration(cal_yz);
	}

	private void updateMagnification(int x, int y) {
        double magnification= win.getCanvas().getMagnification();
        int z = imp.getCurrentSlice()-1;
        ImageWindow win1 = xz_image.getWindow();
        if (win1==null) return;
        ImageCanvas ic1 = win1.getCanvas();
        double mag1 = ic1.getMagnification();
        double arat = az/ax;
		int zcoord=(int)(arat*z);
		if (flipXZ) zcoord=(int)(arat*(imp.getStackSize()-z));
        while (mag1<magnification) {
        	ic1.zoomIn(x, zcoord);
        	mag1 = ic1.getMagnification();
        }
        while (mag1>magnification) {
        	ic1.zoomOut(x, zcoord);
        	mag1 = ic1.getMagnification();
        }
        ImageWindow win2 = yz_image.getWindow();
        if (win2==null) return;
        ImageCanvas ic2 = win2.getCanvas();
        double mag2 = ic2.getMagnification();
		zcoord=(int)(arat*z);
        while (mag2<magnification) {
        	ic2.zoomIn(zcoord,y);
        	mag2 = ic2.getMagnification();
        }
        while (mag2>magnification) {
        	ic2.zoomOut(zcoord,y);
        	mag2 = ic2.getMagnification();
        }
	}
	
	void updateViews(Point p, ImageStack is) {
		if (fp1==null) return;
		updateXZView(p,is);
		
		double arat=az/ax;
		int width2 = (int)Math.round(fp1.getWidth()*ax);
		int height2 = (int)Math.round(fp1.getHeight()*arat);
		if (width2!=fp1.getWidth()||height2!=fp1.getHeight()) {
			fp1.setInterpolate(true);
			ImageProcessor sfp1=fp1.resize(width2, height2);
			sfp1.setMinAndMax(min, max);
			xz_image.setProcessor("XZ "+p.y, sfp1);
		} else {
			fp1.setMinAndMax(min, max);
	    	xz_image.setProcessor("XZ "+p.y, fp1);
		}
			
		if (rotate)
			updateYZView(p,is);
		else
			updateZYView(p,is);
				
		arat=az/ay;
		width2 = (int)Math.round(fp2.getWidth()*arat);
		height2 = (int)Math.round(fp2.getHeight()*ay);
		String title = "YZ ";
		if (rotate) {
			int tmp = width2;
			width2 = height2;
			height2 = tmp;
			title = "ZY ";
		}
		if (width2!=fp2.getWidth()||height2!=fp2.getHeight()) {
			fp2.setInterpolate(true);
			ImageProcessor sfp2=fp2.resize(width2, height2);
			sfp2.setMinAndMax(min, max);
			yz_image.setProcessor(title+p.x, sfp2);
		} else {
			fp2.setMinAndMax(min, max);
			yz_image.setProcessor(title+p.x, fp2);
		}
		
		calibrate();
		if (yz_image.getWindow()==null) {
			yz_image.show();
			yz_image.getCanvas().addKeyListener(this);
			yzID = yz_image.getID();
		}
		if (xz_image.getWindow()==null) {
			xz_image.show();
			xz_image.getCanvas().addKeyListener(this);
			xzID = xz_image.getID();
		}
		 
	}
	
	void arrangeWindows(boolean sticky) {
		ImageWindow xyWin = imp.getWindow();
		if (xyWin==null) return;
		Point loc = xyWin.getLocation();
		if ((xyX!=loc.x)||(xyY!=loc.y)) {
			xyX =  loc.x;
			xyY =  loc.y;
 			ImageWindow yzWin =null;
 			long start = System.currentTimeMillis();
 			while (yzWin==null && (System.currentTimeMillis()-start)<=2500L) {
				yzWin = yz_image.getWindow();
				if (yzWin==null) IJ.wait(50);
			}
			if (yzWin!=null)
 				yzWin.setLocation(xyX+xyWin.getWidth(), xyY);
			ImageWindow xzWin =null;
 			start = System.currentTimeMillis();
 			while (xzWin==null && (System.currentTimeMillis()-start)<=2500L) {
				xzWin = xz_image.getWindow();
				if (xzWin==null) IJ.wait(50);
			}
			if (xzWin!=null)
 				xzWin.setLocation(xyX,xyY+xyWin.getHeight());
 			if (firstTime) {
 				imp.getWindow().toFront();
 				imp.setSlice(imp.getStackSize()/2);
 				firstTime = false;
 			}
		}
	}
	
	/**
	 * @param is - used to get the dimensions of the new ImageProcessors
	 * @return
	 */
	boolean createProcessors(ImageStack is) {
		ImageProcessor ip=is.getProcessor(1);
		int width= is.getWidth();
		int height=is.getHeight();
		int ds=is.getSize(); 
		double arat=1.0;//az/ax;
		double brat=1.0;//az/ay;
		int za=(int)(ds*arat);
		int zb=(int)(ds*brat);
		//IJ.log("za: "+za +" zb: "+zb);
		
		if (ip instanceof FloatProcessor) {
			fp1=new FloatProcessor(width,za);
			if (rotate)
				fp2=new FloatProcessor(height,zb);
			else
				fp2=new FloatProcessor(zb,height);
			return true;
		}
		
		if (ip instanceof ByteProcessor) {
			fp1=new ByteProcessor(width,za);
			if (rotate)
				fp2=new ByteProcessor(height,zb);
			else
				fp2=new ByteProcessor(zb,height);
			return true;
		}
		
		if (ip instanceof ShortProcessor) {
			fp1=new ShortProcessor(width,za);
			if (rotate)
				fp2=new ShortProcessor(height,zb);
			else
				fp2=new ShortProcessor(zb,height);
			return true;
		}
		
		if (ip instanceof ColorProcessor) {
			fp1=new ColorProcessor(width,za);
			if (rotate)
				fp2=new ColorProcessor(height,zb);
			else
				fp2=new ColorProcessor(zb,height);
			return true;
		}
		return false;
	}
	
	void updateXZView(Point p, ImageStack is) {
		int width= is.getWidth();
		int size=is.getSize();
		ImageProcessor ip=is.getProcessor(1);
		
		int y=p.y;
		// XZ
		if (ip instanceof ShortProcessor) {
			short[] newpix=new short[width*size];
			for (int i=0; i<size; i++) { 
				Object pixels=is.getPixels(i+1);
				if (flipXZ)
					System.arraycopy(pixels, width*y, newpix, width*(size-i-1), width);
				else
					System.arraycopy(pixels, width*y, newpix, width*i, width);
			}
			fp1.setPixels(newpix);
			return;
		}
		
		if (ip instanceof ByteProcessor) {
			byte[] newpix=new byte[width*size];
			for (int i=0;i<size; i++) { 
				Object pixels=is.getPixels(i+1);
				if (flipXZ)
					System.arraycopy(pixels, width*y, newpix, width*(size-i-1), width);
				else
					System.arraycopy(pixels, width*y, newpix, width*i, width);
			}
			fp1.setPixels(newpix);
			return;
		}
		
		if (ip instanceof FloatProcessor) {
			float[] newpix=new float[width*size];
			for (int i=0; i<size; i++) { 
				Object pixels=is.getPixels(i+1);
				if (flipXZ)
					System.arraycopy(pixels, width*y, newpix, width*(size-i-1), width);
				else
					System.arraycopy(pixels, width*y, newpix, width*i, width);
			}
			fp1.setPixels(newpix);
			return;
		}
		
		if (ip instanceof ColorProcessor) {
			int[] newpix=new int[width*size];
			for (int i=0;i<size; i++) { 
				Object pixels=is.getPixels(i+1);
				if (flipXZ)
					System.arraycopy(pixels, width*y, newpix, width*(size-i-1), width);
				else
					System.arraycopy(pixels, width*y, newpix, width*i, width);
			}
			fp1.setPixels(newpix);
			return;
		}
		
	}
	
	void updateYZView(Point p, ImageStack is) {
		int width= is.getWidth();
		int height=is.getHeight();
		int ds=is.getSize();
		ImageProcessor ip=is.getProcessor(1);
		int x=p.x;
		
		if (ip instanceof FloatProcessor) {
			float[] newpix=new float[ds*height];
			for (int i=0;i<ds; i++) { 
				float[] pixels= (float[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int j=0;j<height;j++)
					newpix[(ds-i-1)*height + j] = pixels[x + j* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ByteProcessor) {
			byte[] newpix=new byte[ds*height];
			for (int i=0;i<ds; i++) { 
				byte[] pixels= (byte[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int j=0;j<height;j++)
					newpix[(ds-i-1)*height + j] = pixels[x + j* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ShortProcessor) {
			short[] newpix=new short[ds*height];
			for (int i=0;i<ds; i++) { 
				short[] pixels= (short[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int j=0;j<height;j++)
					newpix[(ds-i-1)*height + j] = pixels[x + j* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ColorProcessor) {
			int[] newpix=new int[ds*height];
			for (int i=0;i<ds; i++) { 
				int[] pixels= (int[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int j=0;j<height;j++)
					newpix[(ds-i-1)*height + j] = pixels[x + j* width];
			}
			fp2.setPixels(newpix);
		}
	
	}
	
	void updateZYView(Point p, ImageStack is) {
		int width= is.getWidth();
		int height=is.getHeight();
		int ds=is.getSize();
		ImageProcessor ip=is.getProcessor(1);
		int x=p.x;
		
		if (ip instanceof FloatProcessor) {
			float[] newpix=new float[ds*height];
			for (int i=0;i<ds; i++) { 
				float[] pixels= (float[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int y=0;y<height;y++)
					newpix[i + y*ds] = pixels[x + y* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ByteProcessor) {
			byte[] newpix=new byte[ds*height];
			for (int i=0;i<ds; i++) { 
				byte[] pixels= (byte[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int y=0;y<height;y++)
					newpix[i + y*ds] = pixels[x + y* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ShortProcessor) {
			short[] newpix=new short[ds*height];
			for (int i=0;i<ds; i++) { 
				short[] pixels= (short[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int y=0;y<height;y++)
					newpix[i + y*ds] = pixels[x + y* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ColorProcessor) {
			int[] newpix=new int[ds*height];
			for (int i=0;i<ds; i++) { 
				int[] pixels= (int[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int y=0;y<height;y++)
					newpix[i + y*ds] = pixels[x + y* width];
			}
			fp2.setPixels(newpix);
		}
		
	}
	
 
	/** draws the crosses in the images */
	void drawCross(ImagePlus imp, Point p, GeneralPath path) {
		int width=imp.getWidth();
		int height=imp.getHeight();
		float x = p.x;
		float y = p.y;
		path.moveTo(0f, y);
		path.lineTo(width, y);
		path.moveTo(x, 0f);
		path.lineTo(x, height);	
	}
	 
	/*
	boolean showDialog(ImagePlus imp)   {
        if (imp==null) return true;
        GenericDialog gd=new GenericDialog("Parameters");
        gd.addMessage("This plugin projects orthogonal views\n");
        gd.addNumericField("aspect ratio X:", ax, 3);
        gd.addNumericField("aspect ratio Y:", ay, 3);
        gd.addNumericField("aspect ratio Z:", az, 3);
        gd.addCheckbox("rotate YZ", rotate);
        gd.addCheckbox("sticky panels", sticky);
        gd.showDialog();
        
        ax=(float)gd.getNextNumber();
        ay=(float)gd.getNextNumber();
        az=(float)gd.getNextNumber();
        rotate=gd.getNextBoolean();
        sticky=gd.getNextBoolean();
        if (sticky) rotate = false;
        if (gd.wasCanceled())
            return false;
        return true;
	 }
	     
    void showAbout() {
         IJ.showMessage("About StackSlicer...",
	         "This plugin projects dynamically orthogonal XZ and YZ views of a stack.\n" + 
	         "The user should provide a point selection in the active image window.\n" +
	         "The output images are calibrated, which allows measurements to be performed more easily.\n" +
	         "Optionally the YZ image can be rotated at 90 deg."
         );
     }
     */
     
	void dispose(){
		updater.quit();
		updater = null;
		canvas.removeMouseListener(this);
		canvas.removeMouseMotionListener(this);
		canvas.removeKeyListener(this);
		canvas.setDisplayList(null);
		canvas.setCustomRoi(false);
		ImageWindow win1 = xz_image.getWindow();
		if (win1!=null) {
			win1.getCanvas().setDisplayList(null);
			win1.getCanvas().removeKeyListener(this);
		}
		ImageWindow win2 = yz_image.getWindow();
		if (win2!=null) {
			win2.getCanvas().setDisplayList(null);
			win2.getCanvas().removeKeyListener(this);
		}
		ImagePlus.removeImageListener(this);
		Executer.removeCommandListener(this);
		win.removeWindowListener(this);
		win.removeFocusListener(this);
		win.setResizable(true);
		instance = null;
	}
 	        
    //@Override
	public void mouseClicked(MouseEvent e) {
	}

	//@Override
	public void mouseEntered(MouseEvent e) {
	}

	//@Override
	public void mouseExited(MouseEvent e) {
	}

	//@Override
	public void mousePressed(MouseEvent e) {
		crossLoc = canvas.getCursorLoc();
		update();
	}

	//@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	/**
	 * Refresh the output windows. This is done by sending a signal 
	 * to the Updater() thread. 
	 */
	void update() {
		if (updater!=null)
			updater.doUpdate();
	}
	
	private void exec() {
		if (canvas==null) return;
		int width=imp.getWidth();
		int height=imp.getHeight();
		ImageStack is=imp.getStack();
		double arat=az/ax;
		double brat=az/ay;
		Point p=crossLoc;
		if (p.y>=height) p.y=height-1;
		if (p.x>=width) p.x=width-1;
		if (p.x<0) p.x=0;
		if (p.y<0) p.y=0;
		updateViews(p, is);
		GeneralPath path = new GeneralPath();
		drawCross(imp, p, path);
		canvas.setDisplayList(path, color, new BasicStroke(1));
		canvas.setCustomRoi(true);
		updateCrosses(p.x, p.y, arat, brat);
		if (syncZoom) updateMagnification(p.x, p.y);
		arrangeWindows(sticky);
	}

	private void updateCrosses(int x, int y, double arat, double brat) {
		Point p;
		int z=imp.getNSlices();
		int zlice=imp.getCurrentSlice()-1;
		int zcoord=(int)Math.round(arat*zlice);
		if (flipXZ) zcoord=(int)Math.round(arat*(z-zlice));
		p=new Point (x, zcoord);
		ImageCanvas xz_canvas=xz_image.getCanvas();
		if (xz_canvas!=null) {
			GeneralPath path = new GeneralPath();
			drawCross(xz_image, p, path);
			xz_canvas.setDisplayList(path, color, new BasicStroke(1));
		}
		zcoord=(int)Math.round(brat*(z-zlice));
		if (rotate) 
			p=new Point (y, zcoord);
		else {
			zcoord=(int)Math.round(arat*zlice);
			p=new Point (zcoord, y);
		}
		ImageCanvas yz_canvas=yz_image.getCanvas();
		if (yz_canvas!=null) {
			GeneralPath path = new GeneralPath();
			drawCross(yz_image, p, path);
			yz_canvas.setDisplayList(path, color, new BasicStroke(1));
		}
		IJ.showStatus(imp.getLocationAsString(crossLoc.x, crossLoc.y));
	}

	//@Override
	public void mouseDragged(MouseEvent e) {
		//e.consume();
		crossLoc = canvas.getCursorLoc();
		update();
	}

	//@Override
	public void mouseMoved(MouseEvent e) {
	}

	//@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key==KeyEvent.VK_ESCAPE) {
			IJ.beep();
			dispose();
		} else if (IJ.shiftKeyDown()) {
			int width=imp.getWidth(), height=imp.getHeight();
			switch (key) {
				case KeyEvent.VK_LEFT: crossLoc.x--; if (crossLoc.x<0) crossLoc.x=0; break;
				case KeyEvent.VK_RIGHT: crossLoc.x++; if (crossLoc.x>=width) crossLoc.x=width-1; break;
				case KeyEvent.VK_UP: crossLoc.y--; if (crossLoc.y<0) crossLoc.y=0; break;
				case KeyEvent.VK_DOWN: crossLoc.y++; if (crossLoc.y>=height) crossLoc.y=height-1; break;
				default: return;
			}
			update();
		}
	}

	//@Override
	public void keyReleased(KeyEvent e) {
	}

	//@Override
	public void keyTyped(KeyEvent e) {
	}

	//@Override
	public void actionPerformed(ActionEvent ev) {
	}

	public void imageClosed(ImagePlus imp) {
		dispose();
	}

	public void imageOpened(ImagePlus imp) {
	}

	public void imageUpdated(ImagePlus imp) {
		if (imp==this.imp) {
			ImageProcessor ip = imp.getProcessor();
			min = ip.getMin();
			max = ip.getMax();
			update();
		}
	}

	public String commandExecuting(String command) {
		if (command.equals("In")||command.equals("Out")) {
			ImagePlus cimp = WindowManager.getCurrentImage();
			if (cimp==null) return command;
			if (cimp==imp) {
				/*if (syncZoom) {
					ImageWindow xyWin = cimp.getWindow();
					if (xyWin==null) return command;
					ImageCanvas ic = xyWin.getCanvas();
					Dimension screen = IJ.getScreenSize();
					int xyWidth = xyWin.getWidth();
					ImageWindow yzWin = yz_image.getWindow();
					double mag = ic.getHigherZoomLevel(ic.getMagnification());
					if (yzWin!=null&&xyX+xyWidth+(int)(yzWin.getWidth()*mag)>screen.width) {
						xyX = screen.width-xyWidth-(int)(yzWin.getWidth()*mag);
						if (xyX<10) xyX = 10;
						xyWin.setLocation(xyX, xyY);
					}
 				}*/
				IJ.runPlugIn("ij.plugin.Zoom", command.toLowerCase());
				xyX=0; xyY=0;
				update();
				return null;
			} else if (cimp==xz_image || cimp==yz_image) {
				syncZoom = false;
				return command;
			} else
				return command;
		} else if (command.equals("Flip Vertically")&& xz_image!=null) {
			if (xz_image==WindowManager.getCurrentImage()) {
				flipXZ = !flipXZ;
				update();
				return null;
			} else
				return command;
		} else
			return command;
	}

	//@Override
	public void windowActivated(WindowEvent e) {
		 arrangeWindows(sticky);
	}

	//@Override
	public void windowClosed(WindowEvent e) {
	}

	//@Override
	public void windowClosing(WindowEvent e) {
		dispose();		
	}

	//@Override
	public void windowDeactivated(WindowEvent e) {
	}

	//@Override
	public void windowDeiconified(WindowEvent e) {
		 arrangeWindows(sticky);
	}

	//@Override
	public void windowIconified(WindowEvent e) {
	}

	//@Override
	public void windowOpened(WindowEvent e) {
	}

	//@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		update();
	}
		
	//@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		update();
	}

	//@Override
	public void focusGained(FocusEvent e) {
		ImageCanvas ic = imp.getCanvas();
		if (ic!=null) canvas.requestFocus();
		arrangeWindows(sticky);
	}

	//@Override
	public void focusLost(FocusEvent e) {
		arrangeWindows(sticky);
	}
	
	public static ImagePlus getImage() {
		if (instance!=null)
			return instance.imp;
		else
			return null;
	}

	/**
	 * This is a helper class for Othogonal_Views that delegates the
	 * repainting of the destination windows to another thread.
	 * 
	 * @author Albert Cardona
	 */
	private class Updater extends Thread {
		long request = 0;

		// Constructor autostarts thread
		Updater() {
			super("Othogonal Views Updater");
			setPriority(Thread.NORM_PRIORITY);
			start();
		}

		void doUpdate() {
			if (isInterrupted()) return;
			synchronized (this) {
				request++;
				notify();
			}
		}

		void quit() {
			interrupt();
			synchronized (this) {
				notify();
			}
		}

		public void run() {
			while (!isInterrupted()) {
				try {
					final long r;
					synchronized (this) {
						r = request;
					}
					// Call update from this thread
					if (r>0)
						exec();
					synchronized (this) {
						if (r==request) {
							request = 0; // reset
							wait();
						}
						// else loop through to update again
					}
				} catch (Exception e) { }
			}
		}
		
	}  // Updater class

}
