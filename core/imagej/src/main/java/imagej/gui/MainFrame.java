//
// MainFrame.java
//

package imagej.gui;

import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginUtils;
import imagej.plugin.gui.ShadowMenu;
import imagej.plugin.gui.swing.JMenuBarCreator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

/**
 * A simple and dumb Swing-based main window for ImageJ2.
 *
 * @author Curtis Rueden
 */
public class MainFrame {

	private final JFrame frame;
	private final JToolBar toolBar;
	private final StatusBar statusBar;

	/** Creates a new ImageJ frame that runs as an application. */
	public MainFrame() {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		frame = new JFrame("ImageJ");
		toolBar = new JToolBar();
		statusBar = new StatusBar();

		final JPanel pane = new JPanel();
		frame.setContentPane(pane);
		pane.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		toolBar.setPreferredSize(new Dimension(26 * 21, 26));//TEMP
		pane.add(toolBar, BorderLayout.NORTH);
		pane.add(statusBar, BorderLayout.SOUTH);

		createMenuBar();

		frame.pack();
		frame.setVisible(true);
	}

	private void createMenuBar() {
		final List<PluginEntry<?>> entries = PluginUtils.findPlugins();
		statusBar.setStatus("Discovered " + entries.size() + " plugins");
		final ShadowMenu rootMenu = new ShadowMenu(entries);
		final JMenuBar menuBar = new JMenuBar();
		new JMenuBarCreator().createMenus(rootMenu, menuBar);
		frame.setJMenuBar(menuBar);
	}

	public static void main(String[] args) {
		new MainFrame();
	}

}
