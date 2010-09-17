package i5d.gui;
import java.awt.*;
import java.util.*;

import ij.gui.*;

/** Extended ImageLayout: compatible with two scrollbars for z and t below the image
 * and a channelControl panel to the right of the image. */
public class Image5DLayout extends ImageLayout implements LayoutManager2 {

	protected int hgap;
	protected int vgap;
	protected Image5DCanvas ic5d;
	protected Vector imageCanvasses = new Vector();
	protected Vector imageRectangles = new Vector();

	protected int nCanvassesX = 0;
	protected int nCanvassesY = 0; 
    
    protected Rectangle contentBounds = new Rectangle();
	
	
	protected double targetAspect = 4.0/3.0;

	public static final String CANVAS="main", SLICE_SELECTOR="slice", FRAME_SELECTOR="frame", 
	CHANNEL_SELECTOR="channel";

	protected Component slice, frame, channel; // , main;

	/** Creates a new ImageLayout with center alignment and 5 pixel horizontal and vertical gaps. */
	public Image5DLayout(Image5DCanvas ic5d) {
		super(ic5d);
		this.ic5d = ic5d;
		this.hgap = 4;
		this.vgap = 4;
	}


    /** Returns the preferred dimensions for this layout. 
     * This is called when pack() is called on the ImageWindow. (?)*/
    public Dimension preferredLayoutSize(Container target) {
        Dimension dim = new Dimension(0,0);
        Insets insets = target.getInsets();

        dim.width = getHorizontalCoreSize(ic5d.getPreferredSize().width);   
        dim.width += insets.left + insets.right + 2 * hgap;

        dim.height = getVerticalCoreSize(ic5d.getPreferredSize().height);       
        dim.height += insets.top + insets.bottom + 2 * vgap;

        return dim;
    }
    
    /** Returns the preferred dimensions for this layout given the specified
     * target (ImageWindow), canvasWidth and canvasHeight. 
     * This is called from Image5DCanvas.canEnlarge(). */
    public Dimension preferredLayoutSize(Container target, int canvasWidth, int canvasHeight) {
        Dimension dim = new Dimension(0,0);
        Insets insets = target.getInsets();

        dim.width = getHorizontalCoreSize(canvasWidth);   
        dim.width += insets.left + insets.right + 2 * hgap;

        dim.height = getVerticalCoreSize(canvasHeight);       
        dim.height += insets.top + insets.bottom + 2 * vgap;

        return dim;
    }

	/** Returns the minimum dimensions for this layout. */
	public Dimension minimumLayoutSize(Container target) {
		return preferredLayoutSize(target);
	}

	/** Lays out the container. 
	 * This is called when the window is resized manually or by zooming.
	 */

	public void layoutContainer(Container target) {

		Dimension d = target.getSize();
		Insets insets = target.getInsets();

		// preferred layout size for Image5dCanvas
		Dimension prefCanvasSize = new Dimension(d);

        contentBounds.x = insets.left;
        contentBounds.y = insets.top;
        
		// Widths of canvasses
		contentBounds.width = d.width - ( contentBounds.x + getVerticalControlsWidth() + insets.right );
		if (slice != null) {
            contentBounds.width = Math.max(contentBounds.width, slice.getMinimumSize().width + 2*hgap);
		}
		if (frame != null) {
            contentBounds.width = Math.max(contentBounds.width, frame.getMinimumSize().width + 2*hgap);
		}
		prefCanvasSize.width = (int) Math.floor((contentBounds.width - hgap ) / 
				(double) nCanvassesX) - hgap;
		
		// Heights of canvasses
        contentBounds.height = d.height - ( contentBounds.y + insets.bottom );
		if (channel != null) {
            contentBounds.height = Math.max(contentBounds.height, channel.getMinimumSize().height + 2*vgap);
		}  
        contentBounds.height -= getHorizontalControlsHeight();
		prefCanvasSize.height = (int) Math.floor((contentBounds.height - vgap) /
				(double) nCanvassesY) - vgap;
		
		// Resize the ImageCanvas5D. This also resizes its "satellite" canvasses 
		// in montage display mode.
		Dimension canvasDim = ic5d.resizeCanvasI5D(prefCanvasSize.width, prefCanvasSize.height);        
        
		int offsX = insets.left ;
		int offsY = insets.top ;      
		
		// Place canvasses in center of area spanned by the controls.
		int mainOffsX = offsX + (d.width 
								- offsX - getVerticalControlsWidth() - insets.right 
								- canvasDim.width*nCanvassesX - hgap*(nCanvassesX-1)) / 2;
		int mainOffsY = offsY + (d.height 
								- offsY- getHorizontalControlsHeight() - insets.bottom 
								- canvasDim.height*nCanvassesY - hgap*(nCanvassesY-1)) / 2;
		
		// Set location of canvasses and store location and size in canvasRectangles.
        // Canvas #0 is in lower right corner
		for (int i=0; i<nCanvassesX*nCanvassesY; i++) {
		    // Canvas #0 is in lower right corner (overlay). The others start from top left.
            int j=0;
            if(i==0)
                j=nCanvassesX*nCanvassesY-1;
            else 
                j=i-1;
            
			int nX = j % nCanvassesX;
			int nY = j / nCanvassesX;
			int tempOffsX = mainOffsX + nX*(canvasDim.width+hgap);
			int tempOffsY = mainOffsY + nY*(canvasDim.height+vgap); 
			if (i<imageCanvasses.size()) {
				((Canvas)imageCanvasses.get(i)).setLocation(tempOffsX, tempOffsY);
				Rectangle imageRect = ((Rectangle)imageRectangles.get(i));
				imageRect.x = tempOffsX;
				imageRect.y = tempOffsY;
				imageRect.width = canvasDim.width;
				imageRect.height = canvasDim.height;
				
			} // TODO: Add black Canvasses to locations up to n-1 (??)
		}

        // Set locations and sizes of controls.
		int y = d.height - insets.bottom - getHorizontalControlsHeight() - vgap;		
		if (slice != null) {
			slice.setSize(contentBounds.width - 2*hgap, slice.getPreferredSize().height);
			y += vgap;
			slice.setLocation(offsX+hgap, y);
			y += slice.getPreferredSize().height;
		}		
		if (frame != null) {
			frame.setSize(contentBounds.width - 2*hgap, frame.getPreferredSize().height);
			y += vgap;
			frame.setLocation(offsX+hgap, y);
			y += frame.getPreferredSize().height;
		}

		int x = d.width - insets.right - getVerticalControlsWidth() - vgap;
		if (channel != null) {
			channel.setSize(channel.getPreferredSize().width, d.height - insets.top - insets.bottom - 2*vgap);
			x += hgap;
			channel.setLocation(x, offsY+vgap);
			x += channel.getPreferredSize().width;
		}   	
	}

	public void addLayoutComponent(Component comp, Object constraints) {
		synchronized (comp.getTreeLock()) {
			if ((constraints != null) && (constraints instanceof String)) {
				addLayoutComponent((String)constraints, comp);
			} else {
				throw new IllegalArgumentException("cannot add to layout: constraint must be a string");
			}
		}
	}

	public void addLayoutComponent(String name, Component comp) {
		synchronized (comp.getTreeLock()) {
			if (CANVAS.equals(name)) {
				if (comp instanceof Image5DCanvas) {
					imageCanvasses.add(comp);	
					imageRectangles.add(new Rectangle());
					getCanvasLayout();
				} else {
					throw new IllegalArgumentException("Can only use Image5DCanvasses as 'CANVAS' component.");
				}
			} else if (CHANNEL_SELECTOR.equals(name)) {
				channel = comp;
			} else if (SLICE_SELECTOR.equals(name)) {
				slice = comp;
			} else if (FRAME_SELECTOR.equals(name)) {
				frame = comp;
			} else {
				throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name);
			}  	
		}
	}

	public void removeLayoutComponent(Component comp) {
		synchronized (comp.getTreeLock()) {
			if (imageCanvasses.contains(comp)) {
				int i = imageCanvasses.indexOf(comp);
				imageCanvasses.remove(i);
				imageRectangles.remove(i);
				getCanvasLayout();
			} else if (comp == channel) {
				channel = null;
			} else if (comp == slice) {
				slice = null;
			} else if (comp == frame) {
				frame = null;
			}
		}
	}


	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
	 */
	public Dimension maximumLayoutSize(Container target) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}


	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
	 */
	public float getLayoutAlignmentX(Container target) {
		return 0.5f;
	}


	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
	 */
	public float getLayoutAlignmentY(Container target) {
		return 0.5f;
	}


	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
	 */
	public void invalidateLayout(Container target) {

	}

//    protected int getHorizontalCoreSize() {
//        // Take width of ImageCanvas.
//        // Give enough room for horizontal controls.
//        // Add vertical control(s).
//        int width = 0;
//        width = ic5d.getPreferredSize().width * nCanvassesX 
//                + hgap * (nCanvassesX-1);
//        if (slice != null) {
//            width = Math.max(width, slice.getMinimumSize().width);
//        }
//        if (frame != null) {
//            width = Math.max(width, frame.getMinimumSize().width);
//        }       
//        
//        if (channel != null) {
//            width += hgap + channel.getPreferredSize().width;
//        }   
//        return width;
//    }
    
    protected int getHorizontalCoreSize(int canvasWidth) {
        // Take width of ImageCanvas.
        // Give enough room for horizontal controls.
        // Add vertical control(s).
        int width = 0;
        width = canvasWidth * nCanvassesX 
                + hgap * (nCanvassesX-1);
        if (slice != null) {
            width = Math.max(width, slice.getMinimumSize().width);
        }
        if (frame != null) {
            width = Math.max(width, frame.getMinimumSize().width);
        }       
        
        width += getVerticalControlsWidth();
        
        return width;
    }

//    protected int getVerticalCoreSize() {
//        // Take height of ImageCanvasses
//        // Add horizontal controls.     
//        int height = 0;
//        height = ic5d.getPreferredSize().height * nCanvassesY
//                + vgap * (nCanvassesY-1);
//        if (slice != null) {
//            height          += vgap + slice.getPreferredSize().height;
//        }
//        if (frame != null) {
//            height          += vgap + frame.getPreferredSize().height;
//        }
//
//        if (channel != null) {
//            height = Math.max(height, channel.getMinimumSize().height);
//        }
//
//        return height;
//    }
    
    protected int getVerticalCoreSize(int canvasHeight) {
        // Take height of ImageCanvasses
        // Add horizontal controls.     
        int height = 0;
        height = canvasHeight * nCanvassesY
                + vgap * (nCanvassesY-1);
        
        height += getHorizontalControlsHeight();

        if (channel != null) {
            height = Math.max(height, channel.getMinimumSize().height);
        }

        return height;
    }

	/* Get the combined height of slice and frame sliders, if present. */
	protected int getHorizontalControlsHeight() {
		int height = 0;
		if (slice != null) {
			height          += vgap + slice.getPreferredSize().height;
		}
		if (frame != null) {
			height          += vgap + frame.getPreferredSize().height;
		}

		return height;
	}

	/* Get the width of the ChannelSelector, if present. */
	protected int getVerticalControlsWidth() {
		int width = 0;

		if (channel != null) {
			width += hgap + channel.getPreferredSize().width;
		}

		return width;
	}
	
    /** Calculates the grid of Canvasses in the Window for tiled display */
	protected void getCanvasLayout() {
		double width = ic5d.getImage().getWidth();
		double height = ic5d.getImage().getHeight();
		int n = imageCanvasses.size();
		
		if (n <= 1)  {
			nCanvassesX = 1;
			nCanvassesY = 1;
		} else {
			// Result of solving the equation system:
			// nCX*width / (nCY*height) = targetAspect
			// and
			// nCX * nCY = n
			double tempCanvassesX =  Math.sqrt( n * height * targetAspect / width);
			nCanvassesX = (int) Math.round(tempCanvassesX);
//			// If floor doesn't divide without rest, use ceil.
//			if (n%nCanvassesX != 0) {
//				nCanvassesX = (int) Math.ceil(tempCanvassesX);				
//			}
			nCanvassesY = (int) Math.ceil(n / (double) nCanvassesX);			
		}
		
	}
    
    public Rectangle getCanvasBounds(int i) {
        if (i>=0 && i<imageRectangles.size()) {
            return new Rectangle( ((Rectangle)imageRectangles.get(i)) );
        } else {
            return null;
        }
    }   
    
    public int getNCanvasses() {
        return imageCanvasses.size();
    }
    
    public Rectangle getContentBounds() {
        return contentBounds;
    }   
}
