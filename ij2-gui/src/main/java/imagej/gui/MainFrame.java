//
// MainFrame.java
//

package imagej.gui;

import imagej.plugin.gui.ShadowMenu;
import imagej.plugin.gui.swing.JMenuBarCreator;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginUtils;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/** A simple and dumb Swing-based main window for ImageJ2. */
public class MainFrame {

	private JPanel mainPanel;
	private StatusBar statusBar;

	/** Creates a new ImageJ frame that runs as an application. */
	public MainFrame() {
		final JFrame frame = new JFrame("ImageJ");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		createContentPane(frame);
		createMenuBar(frame);
		frame.pack();
		frame.setVisible(true);
	}

	private void createContentPane(JFrame frame) {
		final JPanel pane = new JPanel();
		frame.setContentPane(pane);
		pane.setLayout(new BorderLayout());

		mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		pane.add(mainPanel, BorderLayout.CENTER);

		statusBar = new StatusBar();
		pane.add(statusBar, BorderLayout.SOUTH);
	}

	private void createMenuBar(JFrame frame) {
		final List<PluginEntry> entries = PluginUtils.findPlugins();
		statusBar.setText("Discovered " + entries.size() + " plugins");
		final ShadowMenu rootMenu = new ShadowMenu(entries);
		final JMenuBar menuBar = new JMenuBar();
		new JMenuBarCreator().createMenus(rootMenu, menuBar);
		frame.setJMenuBar(menuBar);
	}

	public static void main(String[] args) {
		new MainFrame();
	}

}
