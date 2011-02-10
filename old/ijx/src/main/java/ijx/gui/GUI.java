package ijx.gui;
import ijx.IJ;
import java.awt.*;

import ijx.CentralLookup;
import ijx.IjxTopComponent;
import javax.swing.JInternalFrame;

/** This class consists of static GUI utility methods. */
public class GUI {

	/** Positions the specified window in the center of the screen. */
	public static void center(Window w) {
		Dimension screen = IJ.getScreenSize();
		Dimension window = w.getSize();
		if (window.width==0)
			return;
		int left = screen.width/2-window.width/2;
		int top = (screen.height-window.height)/4;
		if (top<0) top = 0;
		w.setLocation(left, top);
	}

	public static void center(JInternalFrame w) {
        IjxTopComponent topC = CentralLookup.getDefault().lookup(IjxTopComponent.class);
		Dimension screen = new Dimension((int)  topC.getSize().getWidth(), (int)topC.getSize().getHeight());
		Dimension window = w.getSize();
		if (window.width==0)
			return;
		int left = screen.width/2-window.width/2;
		int top = (screen.height-window.height)/4;
		if (top<0) top = 0;
		w.setLocation(left, top);
	}
	
    static private Frame frame;
    
    /** Creates a white AWT Image image of the specified size. */
    public static Image createBlankImage(int width, int height) {
        if (width==0 || height==0)
            throw new IllegalArgumentException("");
		if (frame==null) {
			frame = new Frame();
			frame.pack();
			frame.setBackground(Color.white);
		}
        Image img = frame.createImage(width, height);
        return img;
    }
    
}
