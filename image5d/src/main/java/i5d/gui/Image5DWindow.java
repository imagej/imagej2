package i5d.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import i5d.ChannelImagePlus;
import i5d.Image5D;
import ij.*;
import ij.gui.*;
import ij.measure.*;

/*
 * Created on 28.03.2005
*/

/** ImageWindow for Image5Ds. Has two scrollbars for slice and time and a panel with controls
 * to change the current channel and its color.
 * @author Joachim Walter
 */
public class Image5DWindow extends StackWindow implements KeyListener {

    private static final long serialVersionUID = -3196514227677416036L;
    
    protected ChannelControl channelControl;
    protected ScrollbarWithLabel[] scrollbarsWL;
	protected Image5D i5d;

    protected Vector channelCanvasses = new Vector();
	
	// Array for storing change of position in each dimension. 
	// 0: no change, 1 - dimensionSize : changed position
	protected int[] positions;
	
	protected int nDimensions = 5;
    protected int[] dimensions;
	
	protected boolean isInitialized = false;
    
    protected int displayMode;
    protected boolean displayGrayInTiles;
	
	/**
	 * @param imp
	 */
	public Image5DWindow(Image5D imp) {
		this(imp, new Image5DCanvas(imp));
	}

	/**
	 * @param imp
	 * @param ic
	 */
	public Image5DWindow(Image5D imp, Image5DCanvas ic) {
		super(imp, ic);
		
		if (ic == null) {
			throw new IllegalArgumentException("Image5DCanvas must not be null.");
		}
        
		i5d = (Image5D)imp;

		if (imp.getNDimensions() != nDimensions) {
			throw new IllegalArgumentException("Wrong number of dimensions.");
		}

        scrollbarsWL = new ScrollbarWithLabel[nDimensions];
		positions = new int[nDimensions];
        dimensions = i5d.getDimensions();

		// Remove all components and then add them with the Image5DLayout layoutmanager.
		removeAll();

		setLayout(new Image5DLayout(ic));
        
        displayMode = ChannelControl.ONE_CHANNEL_COLOR;
		
		// Add ImageCanvas
		add(ic, Image5DLayout.CANVAS);	
       
        // Fill channelCanvasses vector
        for (int i=1; i<=i5d.getNChannels(); i++) {
            channelCanvasses.add(new Image5DCanvas(i5d.getChannelImagePlus(i)));
            // Make channelImagePlus believe, this window contains it.
            i5d.getChannelImagePlus(i).setWindow(this);
        }

		// Add channel selector
		channelControl = new ChannelControl(this);
		add(channelControl, Image5DLayout.CHANNEL_SELECTOR);
     
		// Add slice selector	
        scrollbarsWL[3] = new ScrollbarWithLabel(Scrollbar.HORIZONTAL, 1, 1, 1, dimensions[3]+1, imp.getDimensionLabel(3));
        if (i5d.getNSlices()>1) {
            add(scrollbarsWL[3], Image5DLayout.SLICE_SELECTOR);
        }

		// Add frame selector	
        scrollbarsWL[4] = new ScrollbarWithLabel(Scrollbar.HORIZONTAL, 1, 1, 1, dimensions[4]+1, imp.getDimensionLabel(4));
        if (i5d.getNFrames()>1) {
            add(scrollbarsWL[4], Image5DLayout.FRAME_SELECTOR);
        }

		for(int i=3; i<nDimensions; ++i) {
            scrollbarsWL[i].addAdjustmentListener(this);
            scrollbarsWL[i].setFocusable(false); // prevents scroll bar from accepting key shortcuts and from blinking on Windows
			int blockIncrement = dimensions[i]/10;
			if (blockIncrement<1) blockIncrement = 1;
            scrollbarsWL[i].setUnitIncrement(1);
            scrollbarsWL[i].setBlockIncrement(blockIncrement); 
		}			
		
        //TODO I think the sliceSelector is not necessary for Image5DWindow. Remove this in a moment
        // of clear thought.
		sliceSelector = scrollbarsWL[3].getScrollbar();

        setDisplayGrayInTiles(i5d.isDisplayGrayInTiles());
        setDisplayMode(i5d.getDisplayMode());
        
		pack();

		
		isInitialized = true;	

		updateSliceSelector();	
		i5d.updateAndRepaintWindow();
		i5d.updateImageAndDraw();
        // Stop thread started in parent (StackWindow) constructor.
        done=true;
        thread.interrupt();       
        while(thread.isAlive()){}
        done=false;
        
		thread = new Thread(this, "SliceSelector");    
		thread.start();

    /* ----------------------------------------------------------------------------
     * Handle Key events:
     * Remove ImageJ as KeyListener from this and all canvasses.
     * Then add this as KeyListener, then ImageJ, then this again!
     * Finally, add this as KeyListener to ImageJ and its components.
     * 
     * The reason for this rather complex structure is, that we want to 
     * catch the keystrokes for changing c/z/t before ImageJ gets them,
     * but need to receive keys for text ROIs and the arrow keys after
     * ImageJ got and processed them.
     * 
     * The same order of KeyListeners is applied in the ChannelControl class.
     * 
     */        
        ImageJ ij = IJ.getInstance();

        // Remove ImageJ as keylistener from this and all subcomponents.
        removeKeyListener(ij);
        ic.removeKeyListener(ij);
        for (int i=0; i<i5d.getNChannels(); i++) {
            ((Image5DCanvas)channelCanvasses.get(i)).removeKeyListener(ij);
        }

        // Add this as KeyListener to receive keys for c/z/t before ImageJ.
        addKeyListener(this);
        ic.addKeyListener(this);
        for (int i=0; i<i5d.getNChannels(); i++) {
            ((Image5DCanvas)channelCanvasses.get(i)).addKeyListener(this);
        }
        scrollbarsWL[3].addKeyListener(this);
        scrollbarsWL[4].addKeyListener(this);
        
        // Add ImageJ as KeyListener to receive any keys it processes.
        addKeyListener(ij);
        ic.addKeyListener(ij);
        for (int i=0; i<i5d.getNChannels(); i++) {
            ((Image5DCanvas)channelCanvasses.get(i)).addKeyListener(ij);
        }
        scrollbarsWL[3].addKeyListener(ij);
        scrollbarsWL[4].addKeyListener(ij);       
        
        // Add this as KeyListener to handle text for text ROIs and cursor keys after ImageJ
        // got them when they were received by a canvas or control.
        addKeyListener(this);
        ic.addKeyListener(this);
        for (int i=0; i<i5d.getNChannels(); i++) {
            ((Image5DCanvas)channelCanvasses.get(i)).addKeyListener(this);
        }
        scrollbarsWL[3].addKeyListener(this);
        scrollbarsWL[4].addKeyListener(this);

        // Add this as KeyListener to ImageJ and all of its components (Toolbar etc.)
        // to handle text for text ROIs and cursor keys.
        ij.addKeyListener(this);
        int nIJComponents = ij.getComponentCount();
        for (int i=0; i<nIJComponents; i++) {
            ij.getComponent(i).addKeyListener(this);
        } 
        ij.getProgressBar().addKeyListener(this); // Sub-component of the statusbar of ImageJ.
    /* ----------------------------------------------------------------------------
     */       
        
        
	}

	
	/** Handles changes in the scrollbars for z and t. 
	 */
	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		if (!running2){
			
			for(int i=3; i<nDimensions; ++i) {
				if (e.getSource() == scrollbarsWL[i]) {
					positions[i] = scrollbarsWL[i].getValue();
                }
			}
			notify();
		}
	}

    /** Sets the display mode of this Window */
    public void setDisplayMode(int displayMode) {
        if (this.displayMode == displayMode) {
            return;
        }
        
        if (displayMode == ChannelControl.TILED
                && this.displayMode != ChannelControl.TILED) {
            for (int i=0; i<i5d.getNChannels(); i++) {
                add((Image5DCanvas)channelCanvasses.get(i), Image5DLayout.CANVAS);
            }
        } else if (displayMode != ChannelControl.TILED 
                && this.displayMode == ChannelControl.TILED) {
            for (int i=0; i<i5d.getNChannels(); i++) {
                remove((Image5DCanvas)channelCanvasses.get(i));
            }
        }
        
        if (channelControl!=null) {
            channelControl.setDisplayMode(displayMode);
        }
        
        this.displayMode = displayMode;
    }    
    
	public int getDisplayMode() {
        return displayMode;
	}
    
    public void setDisplayGrayInTiles (boolean displayGrayInTiles) {
        if (this.displayGrayInTiles == displayGrayInTiles) {
            return;
        }
        
        if (channelControl!=null) {
            channelControl.setDisplayGrayInTiles(displayGrayInTiles);
        }
        
        this.displayGrayInTiles = displayGrayInTiles;    
    }
    public boolean isDisplayGrayInTiles() {
        return displayGrayInTiles;
    }
	
	/** Handles change in ChannelControl. 
	 * Is called by ChannelControl without any events involved.
	 */
	public synchronized void channelChanged() {
		if (!running2){
			positions[2] = channelControl.getCurrentChannel();
		}
		
		notify();	
	}
	
    public void updateCanvasses() {   
        int oldN = channelCanvasses.size();
        
        if (displayMode==ChannelControl.TILED) {
            for (int i=0; i<oldN; i++) {
                remove((Image5DCanvas)channelCanvasses.get(i));
            } 
        }
        
        channelCanvasses = new Vector();
        for (int i=1; i<=i5d.getNChannels(); i++) {
            channelCanvasses.add(new Image5DCanvas(i5d.getChannelImagePlus(i)));
            // Make channelImagePlus believe, this window contains it.
            i5d.getChannelImagePlus(i).setWindow(this);
        }
        
        if (displayMode==ChannelControl.TILED) {
            for (int i=0; i<i5d.getNChannels(); i++) {
                add((Image5DCanvas)channelCanvasses.get(i), Image5DLayout.CANVAS);
            }   
        }
    }

	/** Updates the size and value of the stack and time scrollbar 
	 * and the size and value and other display properties of the channel control. */
	public void updateSliceSelector() {		
		if (isInitialized) {
			int[] newDimensions = imp.getDimensions();
			
			// update channel control
            if(newDimensions[2]>1 && dimensions[2]<=1 || newDimensions[2]<=1 && dimensions[2]>1) {
                channelControl.updateSelectorDisplay();
            }                
		    channelControl.setDisplayMode(i5d.getDisplayMode());
			channelControl.updateChannelSelector();
            
			// update z- and time control
			int max;
            
            // Display or remove slice and frame selectors
            if(newDimensions[3]>1 && dimensions[3]<=1) {
                add(scrollbarsWL[3], Image5DLayout.SLICE_SELECTOR);
            } else if (newDimensions[3]<=1 && dimensions[3]>1) {
                remove(scrollbarsWL[3]);
            }
            if(newDimensions[4]>1 && dimensions[4]<=1) {
                add(scrollbarsWL[4], Image5DLayout.FRAME_SELECTOR);
            } else if (newDimensions[4]<=1 && dimensions[4]>1) {
                remove(scrollbarsWL[4]);
            }         
            
            dimensions = newDimensions;
            
			for(int i=3; i<nDimensions; ++i) {
				max = scrollbarsWL[i].getMaximum();
				if (max!=(dimensions[i]+1)) {
					scrollbarsWL[i].setMaximum(dimensions[i]+1);
                    int blockIncrement = dimensions[i]/10;
                    if (blockIncrement<1) blockIncrement = 1;
                    scrollbarsWL[i].setBlockIncrement(blockIncrement); 
                }
				scrollbarsWL[i].setValue(((Image5D)imp).getCurrentPosition(i)+1);
			}
		}
	}
 
    /** Creates the subtitle. */
    //Overwritten in order to use the (1.38m) new ImageWindow feature :createSubtitle     
    public String createSubtitle() {
    	String s="";
	
		Image5D img5 = (Image5D) imp;
		int[] dimensions = imp.getDimensions();
		Calibration cal = img5.getCalibration();
        ImageStack imageStack = img5.getImageStack();
		
		// current position
		for (int i=2; i<img5.getNDimensions(); ++i){
			s += (img5.getDimensionLabel(i)).trim()+":";
			s += (img5.getCurrentPosition(i)+1);
			s += "/";
			s += dimensions[i];
			s += "; ";
		}    
        
        // slice label
        String label = imageStack.getShortSliceLabel(img5.getCurrentImageStackIndex());
        if (label!=null && label.length()>0) {
            s += "(" + label + "); ";
        }
        if (running2) {
            return s;
        }
    	
    	// x/y size
    	if (cal.pixelWidth!=1.0 || cal.pixelHeight!=1.0)
    		s += IJ.d2s(imp.getWidth()*cal.pixelWidth,2) + "x" + IJ.d2s(imp.getHeight()*cal.pixelHeight,2)
 			+ " " + cal.getUnits() + " (" + imp.getWidth() + "x" + imp.getHeight() + "); ";
    	else
    		s += imp.getWidth() + "x" + imp.getHeight() + " pixels; ";
    	
    	// Data type and size in kBytes
		int size = 1;
		for (int i=0; i<img5.getNDimensions(); ++i) {
			size *= dimensions[i];
		}
		size /= 1024;
    	int type = imp.getType();
    	switch (type) {
	    	case ImagePlus.GRAY8:
	    		s += "8-bit";
	    		break;
	    	case ImagePlus.GRAY16:
	    		s += "16-bit";
				size *= 2;
	    		break;
	    	case ImagePlus.GRAY32:
	    		s += "32-bit";
				size *= 4;
	    		break;
    	}
    	if (imp.isInvertedLut())
    		s += " (inverting LUT)";
    	if (size>=10000)    	
    		s += "; " + (int)Math.round(size/1024.0) + "MB";
    	else if (size>=1024) {
    		double size2 = size/1024.0;
    		s += "; " + IJ.d2s(size2,(int)size2==size2?0:1) + "MB";
    	} else
    		s += "; " + size + "K";
    	return s;
    }
    
    public void paint(Graphics g) {
        // Draw outlines of Canvasses and highlight current image.
        drawRectangles();
        
        // Draw image info. This is done after drawing the outlines,
        // so that the lower part of letters (p, q, ...) is not overwritten.
        g.setColor(Color.black);
        drawInfo(g);
    }   
    
	public void run() {
		if (!isInitialized)
			return;
		while (!done) {
			synchronized(this) {
				try {wait(500);}
				catch(InterruptedException e) {}
			}
			if (done) return;

			for(int i=2; i<nDimensions; ++i) {
				if (positions[i]>0) {
					int p = positions[i];
					positions[i] = 0;
					if (p!=i5d.getCurrentPosition(i)+1) {
						i5d.setCurrentPosition(i, p-1);					
					}
				}
			}
			
		}
	}

    public ChannelControl getChannelControl() {
        return channelControl;
    }
    
//    // Handles key events to change c/z/t by keypresses
//    protected void processKeyEvent(KeyEvent e) {
//        if (e.getID() != KeyEvent.KEY_PRESSED || !(imp instanceof Image5D)) {
//            super.processKeyEvent(e);
//        } else {             
//            Image5D i5d = (Image5D)imp;
//            int code = e.getKeyCode();
//            boolean ctrlPressed = ((e.getModifiers()&KeyEvent.CTRL_MASK)!=0);
//            boolean shiftPressed = ((e.getModifiers()&KeyEvent.SHIFT_MASK)!=0);
//            
//            if (code==KeyEvent.VK_NUMPAD4 || (code==KeyEvent.VK_PAGE_DOWN&&shiftPressed )) {
//                // Numpad 4 / 6 and shift-pageDown / shift-pageUp for slice change
//                i5d.setSlice(i5d.getCurrentSlice()-1);                
//            } else if (code==KeyEvent.VK_NUMPAD6 || (code==KeyEvent.VK_PAGE_UP&&shiftPressed )) {
//                i5d.setSlice(i5d.getCurrentSlice()+1);    
//                
//            } else if (code==KeyEvent.VK_NUMPAD7 || (code==KeyEvent.VK_PAGE_DOWN&&ctrlPressed )) {
//                // Numpad 7 / 9 and ctrl-pageDown / ctrl-pageUp for frame change
//                i5d.setFrame(i5d.getCurrentFrame()-1);                
//            } else if (code==KeyEvent.VK_NUMPAD9 || (code==KeyEvent.VK_PAGE_UP&&ctrlPressed )) {
//                i5d.setFrame(i5d.getCurrentFrame()+1);  
//                
//            } else if (code==KeyEvent.VK_NUMPAD1 || (code==KeyEvent.VK_PAGE_DOWN )) {
//                // Numpad 4 / 6 and pageDown / pageUp (w/o modifier) for channel change
//                i5d.setChannel(i5d.getCurrentChannel()-1);                
//            } else if (code==KeyEvent.VK_NUMPAD3 || (code==KeyEvent.VK_PAGE_UP )) {
//                i5d.setChannel(i5d.getCurrentChannel()+1);   
//                
//            } else {
//                // If none of these combinations is pressed, pass on the event.
//                super.processKeyEvent(e);
//            }
//        }
//    }
    
    // methods of the Keylistener interface

    public void keyPressed(KeyEvent e) {        
        if (e.getID() == KeyEvent.KEY_PRESSED && (imp instanceof Image5D)) {             
            Image5D i5d = (Image5D)imp;
            int code = e.getKeyCode();
            boolean ctrlPressed = ((e.getModifiers()&KeyEvent.CTRL_MASK)!=0);
            boolean shiftPressed = ((e.getModifiers()&KeyEvent.SHIFT_MASK)!=0);
                  
            // Keystrokes only act on the active Image.
            if (i5d == WindowManager.getCurrentImage()) {
                // Process defined key events. Make them unusable for ImageJ by setting the 
                // KeyCode to CHAR_UNDEFINED 
                
                if (code==KeyEvent.VK_NUMPAD1 || (code==KeyEvent.VK_PAGE_DOWN&&shiftPressed )) {
                    // Numpad 1 / 2 and shift-pageDown / shift-pageUp for frame change
                    i5d.setFrame(i5d.getCurrentFrame()-1); 
                    e.setKeyCode(KeyEvent.CHAR_UNDEFINED);               
                } else if (code==KeyEvent.VK_NUMPAD2 || (code==KeyEvent.VK_PAGE_UP&&shiftPressed )) {
                    i5d.setFrame(i5d.getCurrentFrame()+1);  
                    e.setKeyCode(KeyEvent.CHAR_UNDEFINED);
                    
                } else if (code==KeyEvent.VK_NUMPAD7 || (code==KeyEvent.VK_PAGE_DOWN&&ctrlPressed )) {
                    // Numpad 7 / 8 and ctrl-pageDown / ctrl-pageUp (w/o modifier) for channel change
                    i5d.setChannel(i5d.getCurrentChannel()-1);  
                    e.setKeyCode(KeyEvent.CHAR_UNDEFINED);              
                } else if (code==KeyEvent.VK_NUMPAD8 || (code==KeyEvent.VK_PAGE_UP&&ctrlPressed )) {
                    i5d.setChannel(i5d.getCurrentChannel()+1);   
                    e.setKeyCode(KeyEvent.CHAR_UNDEFINED);  
                    
                } else if (code==KeyEvent.VK_NUMPAD4 || (code==KeyEvent.VK_PAGE_DOWN ) 
                        || code==KeyEvent.VK_LESS || code==KeyEvent.VK_COMMA) {
                    // Numpad 4 / 5 and pageDown / pageUp for slice change
                    i5d.setSlice(i5d.getCurrentSlice()-1);      
                    e.setKeyCode(KeyEvent.CHAR_UNDEFINED);
                } else if (code==KeyEvent.VK_NUMPAD5 || (code==KeyEvent.VK_PAGE_UP )
                        || code==KeyEvent.VK_GREATER || code==KeyEvent.VK_PERIOD) {
                    i5d.setSlice(i5d.getCurrentSlice()+1);   
                    e.setKeyCode(KeyEvent.CHAR_UNDEFINED);     
                    
                } else if (code==KeyEvent.VK_LEFT || code==KeyEvent.VK_RIGHT || code==KeyEvent.VK_UP 
                        || code==KeyEvent.VK_DOWN || i5d.getRoi() instanceof TextRoi){
                    // arrow keys to move ROIs or text for Text ROI
                    adaptRois((Image5DCanvas)getCanvas());                    
                } 
            }                                    
        }
    }

    public void keyReleased(KeyEvent e) {
    }
    public void keyTyped(KeyEvent e) {
    }
    
    /** Control + Mousewheel moves channel (Ctrl as in Channel)
     *  Shift - Mousewheel moves frame (shiFt as in Frame) 
     */
    public void mouseWheelMoved(MouseWheelEvent event) {
        if (event.isControlDown()) {
            synchronized(this) {
                int channel = i5d.getCurrentChannel() + event.getWheelRotation();
                if (channel<1)
                    channel = 1;
                else if (channel>i5d.getNChannels())
                    channel = i5d.getNChannels();
                i5d.setChannel(channel);
            }
        } else if (event.isShiftDown()) {
            synchronized(this) {
                int frame = i5d.getCurrentFrame() + event.getWheelRotation();
                if (frame<1)
                    frame = 1;
                else if (frame>i5d.getNFrames())
                    frame = i5d.getNFrames();
                i5d.setFrame(frame);
            }
        } else {
            super.mouseWheelMoved(event);
        }
    }
    
    public void setImagesUpdated() {
        ic.setImageUpdated();
        if (channelCanvasses==null) return;
        for (int i=0; i<channelCanvasses.size(); i++) {
            ((Image5DCanvas)channelCanvasses.get(i)).setImageUpdated();
        }
    }
    
    public void repaintCanvasses() {
        ic.repaint();
        if (channelCanvasses==null) return;
        for (int i=0; i<channelCanvasses.size(); i++) {
            ((Image5DCanvas)channelCanvasses.get(i)).repaint();
        }
    }
    
    public void adaptCanvasses(Image5DCanvas i5dc) {
        
        Dimension drawingSize = i5dc.getDrawingSize();        
        Rectangle srcRect = i5dc.getSrcRect();
        double mag = i5dc.getMagnification();
        
        if (ic!=i5dc) {
            Image5DCanvas tmpCanvas = ((Image5DCanvas)ic);
            tmpCanvas.setSrcRectI5d((Rectangle)srcRect.clone());
            tmpCanvas.setMagnification(mag);
            tmpCanvas.setDrawingSize(drawingSize.width, drawingSize.height);
            tmpCanvas.repaint();
        }
        
        if (channelCanvasses==null) return;

        for (int i=0; i<channelCanvasses.size(); i++) {
            Image5DCanvas tmpCanvas = ((Image5DCanvas)channelCanvasses.get(i));
            if (tmpCanvas!=i5dc) {
                tmpCanvas.setSrcRectI5d((Rectangle)srcRect.clone());
                tmpCanvas.setMagnification(mag);
                tmpCanvas.setDrawingSize(drawingSize.width, drawingSize.height);
                tmpCanvas.repaint();
            }
        } 
    }
    
    /** Hands on the <code>roi</code>, cursor location and modifiers of <code>i5dc</code>
     * to all channel canvasses of this window except <code>i5dc</code>. 
     * If a roi is pasting, it is passed on to the main canvas and the one of the current channel
     * without cloning, i.e. preserving the clipboard. Due to this, one can move the paste roi in 
     * the main canvas or in the canvas of the current channel.
     * 
     * @param i5dc
     * @param roi
     */  
    //TODO: hand on ROIs only between synchronized channels (selected in the channel control.
    
    public void adaptRois(Image5DCanvas i5dc) {   
 
        int iCanvas=getCanvasChannelNumber(i5dc);
        if (iCanvas<0) return;        
        
        ImagePlus imp = i5dc.getImage();   
        Roi roi = imp.getRoi();
        Roi tmpRoi = roi;
        
        if (iCanvas!=0) {
            if (roi!=null && roi.isVisible() && roi.getPasteMode()==Roi.NOT_PASTING) {
                    tmpRoi = (Roi)roi.clone();
            }            
            Image5DCanvas tmpCanvas = ((Image5DCanvas)ic);
            ((Image5D)tmpCanvas.getImage()).putRoi(tmpRoi);
            tmpCanvas.repaint();
        }
        
        if (channelCanvasses==null) return;        

        for (int i=0; i<channelCanvasses.size(); i++) {      
            if (iCanvas == (i+1)) 
                continue;
        
            tmpRoi = roi;
            if (roi!=null && roi.isVisible() && 
                    // Don't clone roi if the roi is a pasting roi and is adapted from main canvas to canvas of current channel.
                    !( (iCanvas==0) && (roi.getPasteMode()!=Roi.NOT_PASTING) 
                            && i5d.getCurrentChannel()==(i+1)) ) {
                
                tmpRoi = (Roi)roi.clone();
            }
            
            Image5DCanvas tmpCanvas = ((Image5DCanvas)channelCanvasses.get(i));
            ((ChannelImagePlus)tmpCanvas.getImage()).putRoi(tmpRoi);
            tmpCanvas.repaint();
        }        
    }
    
    /** Hands on the cursor location and modifiers of <code>i5dc</code>
     * to all channel canvasses of this window except <code>i5dc</code>. 
     */    
    public void adaptMouse (Image5DCanvas i5dc) { 
        Point cursorLoc = i5dc.getCursorLoc();
        int flags = i5dc.getModifiers();
        
        if (ic!=i5dc) {            
            Image5DCanvas tmpCanvas = ((Image5DCanvas)ic);
            
            tmpCanvas.setCursorLoc(cursorLoc.x, cursorLoc.y);
            tmpCanvas.setModifiers(flags);
        }
        
        if (channelCanvasses==null) return;

        for (int i=0; i<channelCanvasses.size(); i++) {            
            Image5DCanvas tmpCanvas = ((Image5DCanvas)channelCanvasses.get(i));
            
            if (tmpCanvas!=i5dc) {                
                tmpCanvas.setCursorLoc(cursorLoc.x, cursorLoc.y);
                tmpCanvas.setModifiers(flags);
            }
        }        
    }
    
    /** Returns 0, if i5dc is the main canvas, a number between 1 and nChannels, if it is 
     * a channel canvas and -1, if the canvas is null or does not belong to this window.
     */
    public int getCanvasChannelNumber(Image5DCanvas i5dc) {
        if (i5dc == ic) {
            return 0;
        }
        for (int i=0; i<channelCanvasses.size(); i++) {
            if ( (channelCanvasses.get(i))==i5dc ) {
                return (i+1);
            }    
        }
        return -1;
    }
    
    public void setChannelAsCurrent(Image5DCanvas i5dc) {
        int i = getCanvasChannelNumber(i5dc);

        // Set specified channel as current, if it is not already the current channel.
        if (i>=1 && i!=i5d.getCurrentChannel()) {
            i5d.setChannel(i);
        }
    }
    
    /* Unfortunately, the same method exists in ImageWindow, but has default access rights. 
     * TODO: ask Wayne to change access rights. */
    protected Rectangle getMaxWindowI5d() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle maxWindow = ge.getMaximumWindowBounds();
        Dimension ijSize = ij!=null?ij.getSize():new Dimension(0,0);
        maxWindow.y += ijSize.height;
        maxWindow.height -= ijSize.height;
        return maxWindow;
    }    
    
	protected void drawRectangles() {
		Graphics g = this.getGraphics();
        LayoutManager layout = getLayout();
        if (layout != null && layout instanceof Image5DLayout) {      
            Image5DLayout i5dLayout = (Image5DLayout)layout;
            
            // Paint over all old rubbish.
            g.setColor(Color.white);
            Rectangle coBounds = i5dLayout.getContentBounds(); 
            if (coBounds != null) {
                g.fillRect(coBounds.x, coBounds.y, coBounds.width, coBounds.height);
            }
            
            // Draw rectangles.         
            // If in gallery mode, rectangle is red for current channel and
            // black for the others.            
            // Black rectangle for overlay image.
            Rectangle cBounds = i5dLayout.getCanvasBounds(0);
            if (cBounds != null) {
                g.setColor(Color.black);
                g.drawRect(cBounds.x-1, cBounds.y-1, cBounds.width+1, cBounds.height+1);
            }
            if (displayMode==ChannelControl.TILED) {
                for (int i=1; i<i5dLayout.getNCanvasses(); i++) {   
                    Rectangle caBounds = i5dLayout.getCanvasBounds(i);
                    if (caBounds != null && i!=i5d.getCurrentChannel()) {
                        // black rectangle for all channels
                        g.setColor(Color.black);
                        g.drawRect(caBounds.x-1, caBounds.y-1, caBounds.width+1, caBounds.height+1);
                    } else if (caBounds != null) {
                        // Red rectangle for current channel.
                        g.setColor(Color.red);
                        g.drawRect(caBounds.x-1, caBounds.y-1, caBounds.width+1, caBounds.height+1);
                        g.drawRect(caBounds.x-2, caBounds.y-2, caBounds.width+3, caBounds.height+3);
                        g.drawRect(caBounds.x-3, caBounds.y-3, caBounds.width+5, caBounds.height+5);
                    }
                }
            }
        }
	}

}