package ijx.gui;
import ijx.gui.dialog.GenericDialog;
import ijx.util.Tools;
import java.awt.*;
import java.util.Vector;
import java.awt.event.*;



/** Displays a dialog that allows the user to select a color using three sliders. */

// @todo: replace with color chooser from JHotDraw...

public class ColorChooser implements TextListener, AdjustmentListener {
    Vector colors, sliders;
    ColorPanel panel;
    Color initialColor;
    int red, green, blue;
    boolean useHSB;
    String title;
    Frame frame;

    /** Constructs a ColorChooser using the specified title and initial color. */
    public ColorChooser(String title, Color initialColor, boolean useHSB) {
    	this(title, initialColor, useHSB, null);
    }
    
    public ColorChooser(String title, Color initialColor, boolean useHSB, Frame frame) {
    	this.title = title;
    	if (initialColor==null) initialColor = Color.black;
    	this.initialColor = initialColor;
    	red = initialColor.getRed();
    	green = initialColor.getGreen();
    	blue = initialColor.getBlue();
    	this.useHSB = useHSB;
    	this.frame = frame;
    }

    /** Displays a color selection dialog and returns the color selected by the user. */
    public Color getColor() {
        GenericDialog gd = frame!=null?new GenericDialog(title, frame):new GenericDialog(title);
        gd.addSlider("Red:", 0, 255, red);
        gd.addSlider("Green:", 0, 255, green);
        gd.addSlider("Blue:", 0, 255, blue);
        panel = new ColorPanel(initialColor);
        gd.addPanel(panel, GridBagConstraints.CENTER, new Insets(10, 0, 0, 0));
        colors = gd.getNumericFields();
        for (int i=0; i<colors.size(); i++)
            ((TextField)colors.elementAt(i)).addTextListener(this);
        sliders = gd.getSliders();
        for (int i=0; i<sliders.size(); i++)
            ((Scrollbar)sliders.elementAt(i)).addAdjustmentListener(this);
        gd.showDialog();
        if (gd.wasCanceled()) return null;
        int red = (int)gd.getNextNumber();
        int green = (int)gd.getNextNumber();
        int blue = (int)gd.getNextNumber();
        return new Color(red, green, blue);
    }

    public void textValueChanged(TextEvent e) {
        int red = (int)Tools.parseDouble(((TextField)colors.elementAt(0)).getText());
        int green = (int)Tools.parseDouble(((TextField)colors.elementAt(1)).getText());
        int blue = (int)Tools.parseDouble(((TextField)colors.elementAt(2)).getText());
        if (red<0) red=0; if (red>255) red=255;
        if (green<0) green=0; if (green>255) green=255;
        if (blue<0) blue=0; if (blue>255) blue=255;
        panel.setColor(new Color(red, green, blue));
        panel.repaint();
    }

	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		Object source = e.getSource();
		for (int i=0; i<sliders.size(); i++) {
			if (source==sliders.elementAt(i)) {
				Scrollbar sb = (Scrollbar)source;
				TextField tf = (TextField)colors.elementAt(i);
			}
		}
	}

}

class ColorPanel extends Panel {
    static final int WIDTH=100, HEIGHT=50;
    Color c;
     
    ColorPanel(Color c) {
        this.c = c;
    }

    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    void setColor(Color c) {
        this.c = c;
    }

    public Dimension getMinimumSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    public void paint(Graphics g) {
        g.setColor(c);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.black);
        g.drawRect(0, 0, WIDTH-1, HEIGHT-1);
    }

}
