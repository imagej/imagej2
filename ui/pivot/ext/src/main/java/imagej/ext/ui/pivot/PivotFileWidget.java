
package imagej.ext.ui.pivot;

import imagej.ext.module.ui.FileWidget;
import imagej.ext.module.ui.WidgetModel;
import imagej.ext.module.ui.WidgetStyle;

import java.io.File;

import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.FileBrowserSheet.Mode;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextInput;

/**
 * Pivot implementation of file selector widget.
 * 
 * @author Curtis Rueden
 */
public class PivotFileWidget extends PivotInputWidget
	implements FileWidget, ButtonPressListener
{

	private final TextInput path;
	private final PushButton browse;

	public PivotFileWidget(final WidgetModel model) {
		super(model);

		path = new TextInput();
		add(path);

		browse = new PushButton("Browse");
		browse.getButtonPressListeners().add(this);
		add(browse);

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

	// -- ButtonPressListener methods --

	@Override
	public void buttonPressed(final Button b) {
		File file = new File(path.getText());
		if (!file.isDirectory()) {
			file = file.getParentFile();
		}

		// display file chooser in appropriate mode
		final WidgetStyle style = getModel().getItem().getWidgetStyle();
		final FileBrowserSheet browser;
		if (style == WidgetStyle.FILE_SAVE) {
			browser = new FileBrowserSheet(Mode.SAVE_AS);
		}
		else { // default behavior
			browser = new FileBrowserSheet(Mode.OPEN);
		}
		browser.setSelectedFile(file);
		browser.open(path.getWindow());
		final boolean success = browser.getResult();
		if (!success) return;
		file = browser.getSelectedFile();
		if (file == null) return;

		path.setText(file.getAbsolutePath());
	}

}
