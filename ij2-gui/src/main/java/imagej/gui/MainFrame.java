//
// MainFrame.java
//

package imagej.gui;

import imagej.plugin.PluginEntry;
import imagej.plugin.PluginUtils;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** A simple and dumb Swing-based main window for ImageJ2. */
public class MainFrame {

	private String info = "Hello, ImageJ";

	/** Creates a new ImageJ frame that runs as an application. */
	public MainFrame() {
		final JFrame frame = new JFrame("ImageJ");
		createMenuBar(frame);
		createContentPane(frame);
		frame.pack();
		frame.setVisible(true);
	}

	private void createContentPane(JFrame frame) {
		final JPanel pane = new JPanel();
		frame.setContentPane(pane);
		pane.setLayout(new BorderLayout());
		pane.setBorder(new EmptyBorder(15, 15, 15, 15));
		pane.add(new JLabel(info));
	}

	private void createMenuBar(JFrame frame) {
		final List<PluginEntry> entries = PluginUtils.findPlugins();
		final List<String> menuPath = new ArrayList<String>();
		info = "Discovered " + entries.size() + " plugins";
		final JMenuBar menubar = new MenuBuilder().buildMenuBar(entries);
		frame.setJMenuBar(menubar);
	}

	public static void main(String[] args) {
		new MainFrame();
	}

}
