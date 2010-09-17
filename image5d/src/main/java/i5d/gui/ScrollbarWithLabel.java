package i5d.gui;
import java.awt.*;
import java.awt.event.*;
/*
 * Created on 10.04.2005
 */


/** Quick hack to add labels to the dimension sliders of Image5DWindow
 * @author Joachim Walter
 */
public class ScrollbarWithLabel extends Panel implements Adjustable, AdjustmentListener {


    /**
	 * 
	 */
	private static final long serialVersionUID = -7934396430763922931L;
	/**
	 * @param orientation
	 * @param value
	 * @param visible
	 * @param minimum
	 * @param maximum
	 */
	private Scrollbar bar;
	private Label label;
	
	private int orientation;
    
    transient AdjustmentListener adjustmentListener;
	
	public ScrollbarWithLabel(int orientation, int value, int visible,
			int minimum, int maximum, String label) {
		super(new BorderLayout(2, 0));
		this.orientation = orientation;
		bar = new Scrollbar(orientation, value, visible, minimum, maximum);
		if (label != null) {
			this.label = new Label(label);
		} else {
			this.label = new Label("");
		}
		if (orientation == Scrollbar.HORIZONTAL)
			add(this.label, BorderLayout.WEST);
		else if (orientation == Scrollbar.VERTICAL)
			add(this.label, BorderLayout.NORTH);
		else
			throw new IllegalArgumentException("invalid orientation");
		
		add(bar, BorderLayout.CENTER);
        bar.addAdjustmentListener(this);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Component#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		Dimension dim = new Dimension(0,0);

		if (orientation == Scrollbar.HORIZONTAL){
			int width = bar.getPreferredSize().width+label.getPreferredSize().width;
			Dimension minSize = getMinimumSize();
			if (width<minSize.width) width = minSize.width;		
			int height = bar.getPreferredSize().height;
			dim = new Dimension(width, height);
		} else {
			int height = bar.getPreferredSize().height+label.getPreferredSize().height;
			Dimension minSize = getMinimumSize();
			if (height<minSize.height) height = minSize.height;	
//			int width = Math.max(bar.getPreferredSize().width, label.getPreferredSize().width);
			int width = bar.getPreferredSize().width;
			dim = new Dimension(width, height);			
		}
		return dim;
	}
	
	public Dimension getMinimumSize() {
		if(orientation==Scrollbar.HORIZONTAL) {
			return new Dimension(80, 15);
		} else {
			return new Dimension(15, 80);
		}
	}
	
    /**
     * @deprecated
     * Returns a reference to the Scrollbar. Ideally the scrollbar should be fully handled by this 
     * class and hidden to other classes, but as it was once exposed this method is kept for 
     * backward compatibility. 
     * SyncWindows up to version 1.6 needs this method.
     * The Image5DWindow also needs it to mimick the SliceSelector of StackWindow.  
     */
	public Scrollbar getScrollbar() {
		return bar;
	}

    /* Adds KeyListener also to all sub-components.
     */
    public synchronized void addKeyListener(KeyListener l) {
        super.addKeyListener(l);
        bar.addKeyListener(l);
        label.addKeyListener(l);
    }

    /* Removes KeyListener also from all sub-components.
     */
    public synchronized void removeKeyListener(KeyListener l) {
        super.removeKeyListener(l);
        bar.removeKeyListener(l);
        label.removeKeyListener(l);
    }


    /* 
     * Methods of the Adjustable interface
     */
    public synchronized void addAdjustmentListener(AdjustmentListener l) {
        if (l == null) {
            return;
        }
        adjustmentListener = AWTEventMulticaster.add(adjustmentListener, l);
    }
    public int getBlockIncrement() {
        return bar.getBlockIncrement();
    }
    public int getMaximum() {
        return bar.getMaximum();
    }
    public int getMinimum() {
        return bar.getMinimum();
    }
    public int getOrientation() {
        return bar.getOrientation();
    }
    public int getUnitIncrement() {
        return bar.getUnitIncrement();
    }
    public int getValue() {
        return bar.getValue();
    }
    public int getVisibleAmount() {
        return bar.getVisibleAmount();
    }
    public synchronized void removeAdjustmentListener(AdjustmentListener l) {
        if (l == null) {
            return;
        }
        adjustmentListener = AWTEventMulticaster.remove(adjustmentListener, l);
    }
    public void setBlockIncrement(int b) {
        bar.setBlockIncrement(b);        
    }
    public void setMaximum(int max) {
        bar.setMaximum(max);        
    }
    public void setMinimum(int min) {
        bar.setMinimum(min);        
    }
    public void setUnitIncrement(int u) {
        bar.setUnitIncrement(u);        
    }
    public void setValue(int v) {
        bar.setValue(v);        
    }
    public void setVisibleAmount(int v) {
        bar.setVisibleAmount(v);        
    }

    public void setFocusable(boolean focusable) {
        super.setFocusable(focusable);
        bar.setFocusable(focusable);
        label.setFocusable(focusable);
    }
    
    
    /*
     * Method of the AdjustmenListener interface.
     */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (bar != null && e.getSource() == bar) {
            AdjustmentEvent myE = new AdjustmentEvent(this, e.getID(), e.getAdjustmentType(), 
                    e.getValue(), e.getValueIsAdjusting());
            AdjustmentListener listener = adjustmentListener;
            if (listener != null) {
                listener.adjustmentValueChanged(myE);
            }
        }
    }
    

}
