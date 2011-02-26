package imagej.plugin.gui.swing;

import imagej.plugin.gui.FileSelector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public class SwingFileSelector extends JPanel
	implements FileSelector, ActionListener
{

	private JTextField path;
	private JButton browse;

	public SwingFileSelector(final File initialValue) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		path = new JTextField(initialValue == null ?
			"" : initialValue.getAbsolutePath(), 20);
		add(path);
		add(Box.createHorizontalStrut(3));
		browse = new JButton("Browse");
		browse.addActionListener(this);
		add(browse);
	}

	@Override
	public File getFile() {
		return new File(path.getText());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		File file = new File(path.getText());
		if (!file.isDirectory()) {
			file = file.getParentFile();
		}
		final JFileChooser chooser = new JFileChooser(file);
		final int rval = chooser.showOpenDialog(this);
		if (rval != JFileChooser.APPROVE_OPTION) return;
		file = chooser.getSelectedFile();
		if (file != null) path.setText(file.getAbsolutePath());
	}

}
