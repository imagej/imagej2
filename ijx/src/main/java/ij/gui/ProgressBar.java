package ij.gui;
import ijx.app.IjxApplication;
import ijx.gui.IjxProgressBar;
import ij.IJ;
import ij.macro.Interpreter;
import ijx.IjxTopComponent;
import implementation.swing.TopComponentSwing;
import java.awt.*;
import java.awt.image.*;

/** This is the progress bar that is displayed in the lower 
	right hand corner of the ImageJ window. Use one of the static 
	IJ.showProgress() methods to display and update the progress bar. */
public class ProgressBar extends Canvas implements IjxProgressBar {

	private int canvasWidth, canvasHeight;
	private int x, y, width, height;
	private double percent;
    private long lastTime = 0;
	private boolean showBar;
	private boolean batchMode;
	
	private Color barColor = Color.gray;
	private Color fillColor = new Color(204,204,255);
	private Color backgroundColor = IJ.backgroundColor;
	private Color frameBrighter = backgroundColor.brighter();
	private Color frameDarker = backgroundColor.darker();

	/** This constructor is called once by ImageJ at startup. */
	public ProgressBar(int canvasWidth, int canvasHeight) {
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		x = 3;
		y = 5;
		width = canvasWidth - 8;
		height = canvasHeight - 7;
	}
		
    void fill3DRect(Graphics g, int x, int y, int width, int height) {
		g.setColor(fillColor);
		g.fillRect(x+1, y+1, width-2, height-2);
		g.setColor(frameDarker);
		g.drawLine(x, y, x, y+height);
		g.drawLine(x+1, y, x+width-1, y);
		g.setColor(frameBrighter);
		g.drawLine(x+1, y+height, x+width, y+height);
		g.drawLine(x+width, y, x+width, y+height-1);
    }
       
 	/**	Updates the progress bar, where percent should run from 0 to 1. */
    public void show(double percent) {
        show(percent, false);
    }
    
	/**	Updates the progress bar, where percent should run from 0 to 1.
	 *  <code>percent = 1.0</code> erases the bar.
     *  The bar is updated only if more than 90 ms have passed since
     *  the last call. Does nothing if the ImageJ window is not present.
     * @param percent   Length of the progress bar to display (0...1)
     * @param showInBatchMode Whether the progress bar should be shown in
     * batch mode.
     */
    public void show(double percent, boolean showInBatchMode) {
        if (!showInBatchMode && (batchMode||Interpreter.isBatchMode())) return;
        if (percent>=1.0) {     //clear the progress bar
			percent = 0.0;
			showBar = false;
			repaint();
            return;
        }
        long time = System.currentTimeMillis();
        if (time - lastTime < 90 && percent != 1.0) return;
        lastTime = time;
        showBar = true;
		this.percent = percent;
        repaint();
    }

 	/**	Updates the progress bar, where the length of the bar is set to
    *  (<code>currentValue+1)/finalValue</code> of the maximum bar length.
    *  The bar is erased if <code>currentValue&gt;=finalValue-1</code>.
    */
    public void show(int currentIndex, int finalIndex) {
        show((currentIndex+1.0)/(double)finalIndex, true);
    }

	public void update(Graphics g) {
		paint(g);
	}

    public void paint(Graphics g) {
    	if (showBar) {
			fill3DRect(g, x-1, y-1, width+1, height+1);
			drawBar(g);
		} else {
			g.setColor(backgroundColor);
			g.fillRect(0, 0, canvasWidth, canvasHeight);
		}
    }

    void drawBar(Graphics g) {
    	if (percent<0.0)
    		percent = 0.0;
    	int barEnd = (int)(width*percent);
//		if (negativeProgress) {
//			g.setColor(fillColor);
//			g.fillRect(barEnd+2, y, width-barEnd, height);
//		} else {
			g.setColor(barColor);
			g.fillRect(x, y, barEnd, height);
//		}
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(canvasWidth, canvasHeight);
    }

    public void setBatchMode(boolean batchMode) {
        this.batchMode = batchMode;
    }

//    public void addKeyListener(IjxApplication ijApp) {
//        this.addKeyListener(ijApp);
//    }
//
//    public void addMouseListener(IjxTopComponent aThis) {
//        this.addMouseListener(aThis);
//    }

}
