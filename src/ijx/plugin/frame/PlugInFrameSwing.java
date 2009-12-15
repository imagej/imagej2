package ijx.plugin.frame;
import ijx.plugin.frame.IjxPluginFrame;
import java.awt.*;
import java.awt.event.*;
import ij.*;


/**  This is a closeable window that plugins can extend. */
public class PlugInFrameSwing extends javax.swing.JFrame implements IjxPluginFrame {

	String title;
	
	public PlugInFrameSwing(String title) {
		super(title);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		this.title = title;
		Frame ij = IJ.getTopComponentFrame();
		addWindowListener(this);
 		addFocusListener(this);
		if (IJ.isLinux()) setBackground(IJ.backgroundColor);
		if (ij!=null) {
			Image img = ij.getIconImage();
			if (img!=null)
				try {setIconImage(img);} catch (Exception e) {}
		}
	}
	
	public void run(String arg) {
	}
	
    public void windowClosing(WindowEvent e) {
    	if (e.getSource()==this)
    		close();
    }
    
    /** Closes this window. */
    public void close() {
		setVisible(false);
		dispose();
		WindowManager.removeWindow(this);
    }
    public boolean canClose() {
		close();
        return true;
    }

    public void windowActivated(WindowEvent e) {
		if (IJ.isMacintosh() && IJ.getInstance()!=null) {
			IJ.wait(10); // may be needed for Java 1.4 on OS X
			setMenuBar(Menus.getMenuBar());
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
}