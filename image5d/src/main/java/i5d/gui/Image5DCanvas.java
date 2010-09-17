package i5d.gui;
import java.awt.*;
import java.awt.event.*;

import ij.*;
import ij.gui.*;
/*
 * Created on 29.05.2005

/** Canvas compatible with Image5DLayout.
 * @author Joachim Walter
 */
public class Image5DCanvas extends ImageCanvas {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8563611296852137396L;


	/**
     * @param imp
     */
    public Image5DCanvas(ImagePlus imp) {
        super(imp);
    }
    
    public ImagePlus getImage() {    	
    	return imp;
    }

    
	/** Adjust the canvas size symmetrically about the middle
     *  of the srcRect, if the user resizes the window. 
	 * 	Called from Image5DLayout.layoutContainer().
     * */
	protected Dimension resizeCanvasI5D(int width, int height) {
	    double magnification = getMagnification();        
     
		if ( width>imageWidth*magnification )
			width = (int)(imageWidth*magnification);
		if ( height>imageHeight*magnification )
			height = (int)(imageHeight*magnification);
		setDrawingSize(width, height);
		Dimension dim = new Dimension(width, height);

        int newSrcRectWidth = (int)(width/magnification);
        // Prevent display of zoomIndicator due to rounding error.
        if (Math.round(magnification) != magnification 
                && (int)((width+1)/magnification) >= imageWidth) {
            newSrcRectWidth = imageWidth;
        }
        int newSrcRectHeight = (int)(height/magnification);
        // Prevent display of zoomIndicator due to rounding error.
        if (Math.round(magnification) != magnification 
                && (int)((height+1)/magnification) >= imageHeight) {
            newSrcRectHeight = imageHeight;
        }
        
        srcRect.x = srcRect.x + (srcRect.width - newSrcRectWidth )/2;
        if (srcRect.x < 0)
            srcRect.x = 0;
        srcRect.y = srcRect.y + (srcRect.height - newSrcRectHeight )/2;
        if (srcRect.y < 0)
            srcRect.y = 0;
        srcRect.width = newSrcRectWidth;
        srcRect.height = newSrcRectHeight;
		if ((srcRect.x+srcRect.width)>imageWidth)
			srcRect.x = imageWidth-srcRect.width;
		if ((srcRect.y+srcRect.height)>imageHeight)
			srcRect.y = imageHeight-srcRect.height;
		repaint();
        
        adaptChannelCanvasses();
        
		return dim;
	}
    
    /* Unfortunately, the setSrcRect method of ImageCanvas has default access rights. 
     * TODO: ask Wayne to change access rights. */    
	public void setSrcRectI5d(Rectangle rect) {
        srcRect = rect;
    }
    
    public void setCursorLoc(int xMouse, int yMouse) {
        this.xMouse = xMouse;
        this.yMouse = yMouse;
    }
    
    public void setModifiers(int flags) {
        this.flags = flags;
    }
    
    public Dimension getDrawingSize() {
        return new Dimension(dstWidth, dstHeight);
    }

    /* copied and modified from ImageCanvas */
    public void zoomOut(int x, int y) {
        if (magnification<=0.03125)
            return;
        double newMag = getLowerZoomLevel(magnification);
//        if (newMag==imp.getWindow().getInitialMagnification()) {
//          unzoom();
//          return;
//        }
        if (imageWidth*newMag>dstWidth) {
            int w = (int)Math.round(dstWidth/newMag);
            if (w*newMag<dstWidth) w++;
            int h = (int)Math.round(dstHeight/newMag);
            if (h*newMag<dstHeight) h++;
            x = offScreenX(x);
            y = offScreenY(y);
            Rectangle r = new Rectangle(x-w/2, y-h/2, w, h);
            if (r.x<0) r.x = 0;
            if (r.y<0) r.y = 0;
            if (r.x+w>imageWidth) r.x = imageWidth-w;
            if (r.y+h>imageHeight) r.y = imageHeight-h;
            srcRect = r;
            setMagnification(newMag);
            adaptChannelCanvasses();
        }
        else {
            srcRect = new Rectangle(0, 0, imageWidth, imageHeight);
            setDrawingSize((int)(imageWidth*newMag), (int)(imageHeight*newMag));
            setMagnification(newMag);
            adaptChannelCanvasses();
            // Pack after adapting all canvasses for case that zoom comes from a channel image.            
            imp.getWindow().pack();
        }
        //IJ.write(newMag + " " + srcRect.x+" "+srcRect.y+" "+srcRect.width+" "+srcRect.height+" "+dstWidth + " " + dstHeight);
        //IJ.write(srcRect.x + " " + srcRect.width + " " + dstWidth);
        repaint();
        

    }
    
//    public void zoomIn(int x, int y) {
//        super.zoomIn(x, y);
//        adaptChannelCanvasses();
//    }
        /** Copied over from ImageCanvas just to move the "pack" call behind adapting channel canvasses. */
    public void zoomIn(int x, int y) {
        if (magnification>=32)
            return;
        double newMag = getHigherZoomLevel(magnification);
        int newWidth = (int)(imageWidth*newMag);
        int newHeight = (int)(imageHeight*newMag);
        Dimension newSize = canEnlarge(newWidth, newHeight);
        if (newSize!=null) {
            setDrawingSize(newSize.width, newSize.height);
            if (newSize.width!=newWidth || newSize.height!=newHeight)
                adjustSourceRectI5d(newMag, x, y);
            else
                setMagnification(newMag);
            adaptChannelCanvasses();
            imp.getWindow().pack();
        } else {
            adjustSourceRectI5d(newMag, x, y);
            adaptChannelCanvasses();
        }
        repaint();
    }    

    /* This method has default access rights in ImageCanvas. 
     * TODO: ask Wayne to make it protected.
     */
    protected void adjustSourceRectI5d(double newMag, int x, int y) {
        //IJ.log("adjustSourceRect1: "+newMag+" "+dstWidth+"  "+dstHeight);
        int w = (int)Math.round(dstWidth/newMag);
        if (w*newMag<dstWidth) w++;
        int h = (int)Math.round(dstHeight/newMag);
        if (h*newMag<dstHeight) h++;
        x = offScreenX(x);
        y = offScreenY(y);
        Rectangle r = new Rectangle(x-w/2, y-h/2, w, h);
        if (r.x<0) r.x = 0;
        if (r.y<0) r.y = 0;
        if (r.x+w>imageWidth) r.x = imageWidth-w;
        if (r.y+h>imageHeight) r.y = imageHeight-h;
        srcRect = r;
        setMagnification(newMag);
        //IJ.log("adjustSourceRect2: "+srcRect+" "+dstWidth+"  "+dstHeight);
    }

    protected void scroll(int sx, int sy) {
        super.scroll(sx, sy);      
        adaptChannelCanvasses();
        repaint();
    }
    

//    protected void setupScroll(int ox, int oy) {
//        super.setupScroll(ox, oy);
//        adaptChannelCanvasses();
//    }

/** Predicts, whether Canvas can enlarge on this desktop, even in tiled mode */
    //TODO: ChannelCanvas or z/t sliders are not handled fully correctly.
    protected Dimension canEnlarge(int newWidth, int newHeight) {
        if ((flags&Event.SHIFT_MASK)!=0 || IJ.shiftKeyDown())
            return null;
        ImageWindow win = imp.getWindow();
        if (win==null) return null;
        Rectangle r1 = win.getBounds();
       
        Dimension prefSize = ((Image5DLayout)win.getLayout()).
            preferredLayoutSize(win, newWidth, newHeight);
        r1.width = prefSize.width;
        r1.height = prefSize.height;      
        
        Rectangle max = ((Image5DWindow)win).getMaxWindowI5d();
        boolean fitsHorizontally = r1.x+r1.width<max.x+max.width;
        boolean fitsVertically = r1.y+r1.height<max.y+max.height;
        if (fitsHorizontally && fitsVertically)
            return new Dimension(newWidth, newHeight);
        else if (fitsVertically && newHeight<dstWidth)
            return new Dimension(dstWidth, newHeight);
        else if (fitsHorizontally && newWidth<dstHeight)
            return new Dimension(newWidth, dstHeight);
        else
            return null;
    }  

  
    public void mouseDragged(MouseEvent e) {        
        boolean selectionBrush = 
            (Toolbar.getToolId()==Toolbar.OVAL && Toolbar.getBrushSize()>0);
        // Keep reference to Roi in main canvas
        Roi oldMainRoi = null;
        Image5DWindow win = (Image5DWindow) imp.getWindow();
        if (win != null) {
            oldMainRoi = win.getImagePlus().getRoi();
        }
        
        super.mouseDragged(e);
        
        adaptChannelMouse();
        
        // Get new reference to Roi in main canvas
        Roi newMainRoi = null;
        if (win != null) {
            newMainRoi = win.getImagePlus().getRoi();
        }       
        // Work around special behaviour of some tools, that operate on the main canvas and
        // not on the channel canvas, that receives the mousePressed event.
        if (oldMainRoi!=newMainRoi || selectionBrush) {
            adaptChannelRois(false);
        } else {
            adaptChannelRois(true);
        }
    }

    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);

        adaptChannelMouse();
        
        // To avoid ROI flickering, only adapt ROI when it really has changed.
        // Code copied from ImageCanvas.mouseMoved().
        Roi roi = imp.getRoi();
        if (roi!=null && (roi.getType()==Roi.POLYGON || roi.getType()==Roi.POLYLINE || roi.getType()==Roi.ANGLE) 
        && roi.getState()==Roi.CONSTRUCTING) {
            adaptChannelRois(true);
        }
    }

    public void mousePressed(MouseEvent e) {   
        boolean selectionBrush = 
            (Toolbar.getToolId()==Toolbar.OVAL && Toolbar.getBrushSize()>0);
        
        setThisChannelAsCurrent();
        
        // Keep reference to Roi in main canvas
        Roi oldMainRoi = null;
        Image5DWindow win = (Image5DWindow) imp.getWindow();
        if (win != null) {
            oldMainRoi = win.getImagePlus().getRoi();
        }
        
        super.mousePressed(e);

        adaptChannelMouse();
        
        // Get new reference to Roi in main canvas
        Roi newMainRoi = null;
        if (win != null) {
            newMainRoi = win.getImagePlus().getRoi();
        }       
        // Work around special behaviour of some tools, that operate on the main canvas and
        // not on the channel canvas, that receives the mousePressed event.
        if (oldMainRoi!=newMainRoi || selectionBrush) {
            adaptChannelRois(false);
        } else {
            adaptChannelRois(true);
        }

        
    }

    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        adaptChannelMouse();
        adaptChannelRois(true);
    }

    protected void adaptChannelCanvasses() {
        Image5DWindow win = (Image5DWindow) imp.getWindow();
        if (win != null) {
            win.adaptCanvasses(this);
        }        
    }
    
    /* If <code>thisChannel</code> is true, hands on the current ROIto all other channels. 
     * If <code>thisChannel</code> is false, hands on the ROI of main canvas to all channels
     * including this one. */
    protected void adaptChannelRois(boolean thisChannel) {        
        Image5DWindow win = (Image5DWindow) imp.getWindow();
        if (win != null) {            
            if (thisChannel) {
                win.adaptRois(this);
            } else {
                win.adaptRois((Image5DCanvas)win.getCanvas());
            }
        }        
    }   
    
    /* Hands on the current cursor location and modifiers to all other channels. */
    protected void adaptChannelMouse() {        
        Image5DWindow win = (Image5DWindow) imp.getWindow();
        if (win != null) {
            win.adaptMouse(this);
        }        
    }
    
    protected void setThisChannelAsCurrent() {
        Image5DWindow win = (Image5DWindow) imp.getWindow();
        if (win != null) {
            win.setChannelAsCurrent(this);
        }        
    }
      
}
