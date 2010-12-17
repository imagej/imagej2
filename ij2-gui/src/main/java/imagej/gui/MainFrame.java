//
// MainFrame.java
//

package imagej.gui;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** A simple and dumb Swing-based main window for ImageJ2. */
public class MainFrame {

	/** Creates a new ImageJ frame that runs as an application. */
	public MainFrame() {
		JFrame frame = new JFrame("ImageJ");
		JPanel pane = new JPanel();
		frame.setContentPane(pane);
		pane.setLayout(new BorderLayout());
		pane.add(new JLabel("Hello, ImageJ"));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new MainFrame();
	}

}
