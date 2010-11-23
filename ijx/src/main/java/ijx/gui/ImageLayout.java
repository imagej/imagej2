package ijx.gui;
import java.awt.*;
import ij.*;
import ijx.gui.IjxImageCanvas;

/** This is a custom layout manager that supports resizing of zoomed
images. It's based on FlowLayout, but with vertical and centered flow. */
public class ImageLayout implements LayoutManager {

    int hgap;
    int vgap;
	IjxImageCanvas ic;

    /** Creates a new ImageLayout with center alignment and 5 pixel horizontal and vertical gaps. */
    public ImageLayout(IjxImageCanvas ic) {
    	this.ic = ic;
		this.hgap = 5;
		this.vgap = 5;
    }

    /** Not used by this class. */
    public void addLayoutComponent(String name, Component comp) {
    }

    /** Not used by this class. */
    public void removeLayoutComponent(Component comp) {
    }

    /** Returns the preferred dimensions for this layout. */
    public Dimension preferredLayoutSize(Container target) {
		Dimension dim = new Dimension(0,0);
		int nmembers = target.getComponentCount();
		for (int i=0; i<nmembers; i++) {
		    Component m = target.getComponent(i);
			Dimension d = m.getPreferredSize();
			dim.width = Math.max(dim.width, d.width);
			if (i>0) dim.height += vgap;
			dim.height += d.height;
		}
		Insets insets = target.getInsets();
		dim.width += insets.left + insets.right + hgap*2;
		dim.height += insets.top + insets.bottom + vgap*2;
		return dim;
    }

    /** Returns the minimum dimensions for this layout. */
    public Dimension minimumLayoutSize(Container target) {
		return preferredLayoutSize(target);
    }

    /** Centers the elements in the specified column, if there is any slack.*/
    private void moveComponents(Container target, int x, int y, int width, int height, int nmembers) {
    	int x2 = 0;
	    y += height / 2;
		for (int i=0; i<nmembers; i++) {
		    Component m = target.getComponent(i);
		    Dimension d = m.getSize();
		    if (i==0 || d.height>60)
		    	x2 = x + (width - d.width)/2;
			m.setLocation(x2, y);
			y += vgap + d.height;
		}
    }

    /** Lays out the container and calls ImageCanvas.resizeCanvas()
		to adjust the image canvas size as needed. */
    public void layoutContainer(Container target) {
		Insets insets = target.getInsets();
		int nmembers = target.getComponentCount();
		Dimension d;
		int extraHeight = 0;
		for (int i=1; i<nmembers; i++) {
			Component m = target.getComponent(i);
			d = m.getPreferredSize();
			extraHeight += d.height;
		}
		d = target.getSize();
		int preferredImageWidth = d.width - (insets.left + insets.right + hgap*2);
		int preferredImageHeight = d.height - (insets.top + insets.bottom + vgap*2 + extraHeight);
		ic.resizeCanvas(preferredImageWidth, preferredImageHeight);
		int maxwidth = d.width - (insets.left + insets.right + hgap*2);
		int maxheight = d.height - (insets.top + insets.bottom + vgap*2);
		Dimension psize = preferredLayoutSize(target);
		int x = insets.left + hgap + (d.width - psize.width)/2;
		int y = 0;
		int colw = 0;
		
		for (int i=0; i<nmembers; i++) {
			Component m = target.getComponent(i);
			d = m.getPreferredSize();
			if ((m instanceof ScrollbarWithLabel) || (m instanceof Scrollbar)) {
				int scrollbarWidth = target.getComponent(0).getPreferredSize().width;
				Dimension minSize = m.getMinimumSize();
				if (scrollbarWidth<minSize.width) scrollbarWidth = minSize.width;
				m.setSize(scrollbarWidth, d.height);
			} else
				m.setSize(d.width, d.height);
			if (y > 0) y += vgap;
			y += d.height;
			colw = Math.max(colw, d.width);
		}
		moveComponents(target, x, insets.top + vgap, colw, maxheight - y, nmembers);
    }
    
}
