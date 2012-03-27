
package imagej.ext.ui.awt;

import imagej.ext.module.ui.FileWidget;
import imagej.ext.module.ui.WidgetModel;
import imagej.ext.module.ui.WidgetStyle;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.File;

/**
 * AWT implementation of file selector widget.
 *
 * @author Curtis Rueden
 */
public class AWTFileWidget extends AWTInputWidget
	implements FileWidget, ActionListener, TextListener
{

	private TextField path;
	private Button browse;

	public AWTFileWidget(final WidgetModel model) {
		super(model);

		setLayout(new BorderLayout());

		path = new TextField(20);
		path.addTextListener(this);
		add(path, BorderLayout.CENTER);

		browse = new Button("Browse");
		browse.addActionListener(this);
		add(browse, BorderLayout.EAST);

		refreshWidget();
	}

	// -- FileWidget methods --

	@Override
	public File getValue() {
		return new File(path.getText());
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final File value = (File) getModel().getValue();
		final String text = value == null ? "" : value.toString();
		if (text.equals(path.getText())) return; // no change
		path.setText(text);
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(ActionEvent e) {
		File file = new File(path.getText());
		if (!file.isDirectory()) {
			file = file.getParentFile();
		}

		// display file chooser in appropriate mode
		final WidgetStyle style = getModel().getItem().getWidgetStyle();
		final FileDialog fileDialog = new FileDialog((Frame) null);
		if (style == WidgetStyle.FILE_SAVE) {
			fileDialog.setMode(FileDialog.SAVE);
		}
		else { // default behavior
			fileDialog.setMode(FileDialog.LOAD);
		}
		fileDialog.setDirectory(file.getAbsolutePath());
		fileDialog.setVisible(true);
		final String filename = fileDialog.getFile();
		fileDialog.dispose();
		if (filename == null) return;

		path.setText(filename);
	}

	// -- TextListener methods --

	@Override
	public void textValueChanged(TextEvent e) {
		updateModel();
	}

}
