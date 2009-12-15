/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ijx;

import ij.*;
import ijx.IjxApplication;
import ijx.IjxTopComponent;
import ij.gui.ProgressBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.MenuBar;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.ImageProducer;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JMenuBar;



/**
 *
 * @author GBH
 */
public class TopComponentSwing extends JFrame implements IjxTopComponent {
    
	private ij.gui.Toolbar toolbar;
    private Label statusLine;
	private Panel statusBar;
	private ProgressBar progressBar;
    private boolean windowClosed;
    private IjxApplication ijApp;


    public TopComponentSwing(IjxApplication ijApp) {
        super("ImageJ");
        this.ijApp = ijApp;
        setLayout(new GridLayout(2, 1));
        // Tool bar
		toolbar = new ij.gui.Toolbar();
		toolbar.addKeyListener(ijApp);
		add(toolbar);

		// Status bar
		statusBar = new Panel();
		statusBar.setLayout(new BorderLayout());
		statusBar.setForeground(Color.black);
		statusBar.setBackground(IJ.backgroundColor);
		statusLine = new Label();
		statusLine.setFont(SansSerif12);
		statusLine.addKeyListener(ijApp);
		statusLine.addMouseListener(this);
		statusBar.add("Center", statusLine);
		progressBar = new ProgressBar(120, 20);
		progressBar.addKeyListener(ijApp);
		progressBar.addMouseListener(this);
		statusBar.add("East", progressBar);
		statusBar.setSize(toolbar.getPreferredSize());
		add(statusBar);
        addWindowListener(this);
		setFocusTraversalKeysEnabled(false);
    }
    
    public void finishAndShow() {
        Point loc = getPreferredLocation();
		Dimension tbSize = toolbar.getPreferredSize();
		int ijWidth = tbSize.width+10;
		int ijHeight = 100;
		setCursor(Cursor.getDefaultCursor()); // work-around for JDK 1.1.8 bug
		if (IJ.isWindows()) try {setIcon();} catch(Exception e) {}
		setBounds(loc.x, loc.y, ijWidth, ijHeight); // needed for pack to work
		setLocation(loc.x, loc.y);
		pack();
		// ?? setResizable(!(IJ.isMacintosh() || IJ.isWindows())); // make resizable on Linux
		//if (IJ.isJava15()) {
		//	try {
		//		Method method = Frame.class.getMethod("setAlwaysOnTop", new Class[] {boolean.class});
		//		method.invoke(this, new Object[]{Boolean.TRUE});
		//	} catch(Exception e) {}
		//}
		show();
    }
    
    
    public void setIcon() throws Exception {
		URL url = this.getClass().getResource("/microscope.gif");
		if (url==null) return;
		Image img = createImage((ImageProducer)url.getContent());
		if (img!=null) setIconImage(img);
	}
	
	public Point getPreferredLocation() {
		if (!IJ.isJava14()) return new Point(0, 0);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle maxBounds = ge.getMaximumWindowBounds();
		int ijX = Prefs.getInt(IJ_X,-99);
		int ijY = Prefs.getInt(IJ_Y,-99);
		if (ijX>=0 && ijY>0 && ijX<(maxBounds.x+maxBounds.width-75))
			return new Point(ijX, ijY);
		Dimension tbsize = toolbar.getPreferredSize();
		int ijWidth = tbsize.width+10;
		double percent = maxBounds.width>832?0.8:0.9;
		ijX = (int)(percent*(maxBounds.width-ijWidth));
		if (ijX<10) ijX = 10;
		return new Point(ijX, maxBounds.y);
	}

    public void setMenuBar(Object menu) {
      setJMenuBar((JMenuBar)menu);
    }
	
	public void showStatus(String s) {
        statusLine.setText(s);
	}

	public ProgressBar getProgressBar() {
        return progressBar;
	}

	public Panel getStatusBar() {
        return statusBar;
	}

    
    public void mousePressed(MouseEvent e) {
		Undo.reset();
		IJ.showStatus("Memory: "+IJ.freeMemory());
		if (IJ.debugMode)
			IJ.log("Windows: "+WindowManager.getWindowCount());
	}
	
	public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
    
    public void windowClosing(WindowEvent e) {
		ijApp.doCommand("Quit");
		windowClosed = true;
	}

	public void windowActivated(WindowEvent e) {
		if (IJ.isMacintosh() && !ijApp.quitting()) {
			IJ.wait(10); // may be needed for Java 1.4 on OS X
			setMenuBar(Menus.getMenuBar());
		}
	}
	
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}


    @Override
    public boolean canClose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isClosed() {
        return windowClosed;
    }

}
