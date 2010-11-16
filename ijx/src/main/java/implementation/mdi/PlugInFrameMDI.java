package implementation.mdi;

import ijx.plugin.frame.IjxPluginFrame;
import java.awt.*;
import java.awt.event.*;
import ij.*;
import ijx.plugin.frame.IjxPluginFrame;
import javax.swing.ImageIcon;

/**  This is a closeable window that plugins can extend. */
public class PlugInFrameMDI extends javax.swing.JInternalFrame implements IjxPluginFrame {
    String title;

    public PlugInFrameMDI(String title) {
        super(title);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        this.title = title;
        Frame ij = IJ.getTopComponentFrame();
        //	addWindowListener(this);
        addFocusListener(this);
        if (IJ.isLinux()) {
            setBackground(IJ.backgroundColor);
        }
//		if (ij!=null) {
//			Image img = ij.getImageIcon();
//			if (img!=null)
//				try {this.setsetIconImage(img);} catch (Exception e) {}
//		}
    }

    public void run(String arg) {
    }

    public void windowClosing(WindowEvent e) {
        if (e.getSource() == this) {
            close();
        }
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
//		if (IJ.isMacintosh() && IJ.getInstance()!=null) {
//			IJ.wait(10); // may be needed for Java 1.4 on OS X
//			setMenuBar(Menus.getMenuBar());
//		}
        WindowManager.setWindow(this);
    }

    public void focusGained(FocusEvent e) {
        //IJ.log("PlugInFrame: focusGained");
        WindowManager.setWindow(this);
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void focusLost(FocusEvent e) {
    }

    public boolean isClosed() {
        return true;
    }

    public ImageIcon getImageIcon() {
        return getImageIcon();
    }
}
