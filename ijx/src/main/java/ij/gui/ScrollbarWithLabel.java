package ij.gui;
import ij.IJ;
import ijx.CentralLookup;
import ijx.app.KeyboardHandler;
import ijx.gui.IjxStackWindow;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;


/** This class, based on Joachim Walter's Image5D package, adds "c", "z" labels 
	 and play-pause icons (T) to the stack and hyperstacks dimension sliders.
 * @author Joachim Walter
 */
public class ScrollbarWithLabel extends Panel implements Adjustable, AdjustmentListener {
	public Scrollbar bar;
	private Icon icon;
	private IjxStackWindow IjxStackWindow;
	transient AdjustmentListener adjustmentListener;
	
	public ScrollbarWithLabel() {
	}

	public ScrollbarWithLabel(IjxStackWindow IjxStackWindow, int value, int visible, int minimum, int maximum, char label) {
		super(new BorderLayout(2, 0));
		this.IjxStackWindow = IjxStackWindow;
		bar = new Scrollbar(Scrollbar.HORIZONTAL, value, visible, minimum, maximum);
		icon = new Icon(label);
		add(icon, BorderLayout.WEST);
		add(bar, BorderLayout.CENTER);
		bar.addAdjustmentListener(this);
        addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Component#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		Dimension dim = new Dimension(0,0);
		int width = bar.getPreferredSize().width;
		Dimension minSize = getMinimumSize();
		if (width<minSize.width) width = minSize.width;		
		int height = bar.getPreferredSize().height;
		dim = new Dimension(width, height);
		return dim;
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(80, 15);
	}
	
	/* Adds KeyListener also to all sub-components.
	 */
	public synchronized void addKeyListener(KeyListener l) {
		super.addKeyListener(l);
		bar.addKeyListener(l);
	}

	/* Removes KeyListener also from all sub-components.
	 */
	public synchronized void removeKeyListener(KeyListener l) {
		super.removeKeyListener(l);
		bar.removeKeyListener(l);
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
		
	public void updatePlayPauseIcon() {
		icon.repaint();
	}
	
	
	class Icon extends Canvas implements MouseListener {
		private static final int WIDTH = 12, HEIGHT=14;
		private BasicStroke stroke = new BasicStroke(2f);
		private char type;
		private Image image;

		public Icon(char type) {
			addMouseListener(this);
            addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
			setSize(WIDTH, HEIGHT);
			this.type = type;
		}
		
		/** Overrides Component getPreferredSize(). */
		public Dimension getPreferredSize() {
			return new Dimension(WIDTH, HEIGHT);
		}
				
		public void update(Graphics g) {
			paint(g);
		}
		
		public void paint(Graphics g) {
			g.setColor(Color.white);
			g.fillRect(0, 0, WIDTH, HEIGHT);
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (type=='t')
				drawPlayPauseButton(g2d);
			else
				drawLetter(g);
		}
		
		private void drawLetter(Graphics g) {
			g.setFont(new Font("SansSerif", Font.PLAIN, 14));
			g.setColor(Color.black);
			g.drawString(type=='c'?"c":"z", 2, 12);
		}

		private void drawPlayPauseButton(Graphics2D g) {
			if (IjxStackWindow.getAnimate()) {
				g.setColor(Color.black);
				g.setStroke(stroke);
				g.drawLine(3, 3, 3, 11);
				g.drawLine(8, 3, 8, 11);
			} else {
				g.setColor(Color.darkGray);
				GeneralPath path = new GeneralPath();
				path.moveTo(3f, 2f);
				path.lineTo(10f, 7f);
				path.lineTo(3f, 12f);
				path.lineTo(3f, 2f);
				g.fill(path);
			}
		}
		
		public void mousePressed(MouseEvent e) {
			if (type!='t') return;
			int flags = e.getModifiers();
			if ((flags&(Event.ALT_MASK|Event.META_MASK|Event.CTRL_MASK))!=0)
				IJ.doCommand("Animation Options...");
			else
				IJ.doCommand("Start Animation [\\]");
		}
		
		public void mouseReleased(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
	
	} // StartStopIcon class

	
}
