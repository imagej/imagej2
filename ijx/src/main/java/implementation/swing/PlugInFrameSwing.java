package implementation.swing;
import ijx.WindowManager;
import ijx.IJ;
import ijx.plugin.frame.IjxPluginFrame;
import java.awt.*;
import java.awt.event.*;

import ijx.app.IjxApplication;
import ijx.plugin.frame.IjxPluginFrame;
import javax.swing.ImageIcon;


/**  This is a closeable window that plugins can extend. */
public class PlugInFrameSwing extends javax.swing.JFrame implements IjxPluginFrame {

	String title;
	
	public PlugInFrameSwing(String title) {
		super(title);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		this.title = title;
		IjxApplication ij = IJ.getInstance();
		addWindowListener(this);
 		addFocusListener(this);
		if (IJ.isLinux()) setBackground(IJ.backgroundColor);
		if (ij!=null) {
			ImageIcon img = ij.getImageIcon();
			if (img!=null)
				try {setIconImage(img.getImage());} catch (Exception e) {}
		}
	}
	
	public void run(String arg) {
	}
	
    public void windowClosing(WindowEvent e) {
    	if (e.getSource()==this)
    		close();
    }
    
    /** Closes this window. */
    public boolean close() {
		setVisible(false);
		dispose();
		WindowManager.removeWindow(this);
        return true;
    }
    public boolean canClose() {
		close();
        return true;
    }

    public void windowActivated(WindowEvent e) {
		if (IJ.isMacintosh() && IJ.getInstance()!=null) {
            // @todo
			//IJ.wait(10); // may be needed for Java 1.4 on OS X
			//setMenuBar(Menus.getMenuBar());
		}
		WindowManager.setWindow(this);
	}

	public void focusGained(FocusEvent e) {
		//IJ.log("PlugInFrame: focusGained");
		WindowManager.setWindow(this);
	}

    public void windowOpened(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
	public void focusLost(FocusEvent e) {}

    public boolean isClosed() {
        return true;
    }

    public ImageIcon getImageIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}