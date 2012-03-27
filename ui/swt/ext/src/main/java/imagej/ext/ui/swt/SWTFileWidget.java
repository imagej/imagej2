
package imagej.ext.ui.swt;

import imagej.ext.module.ui.FileWidget;
import imagej.ext.module.ui.WidgetModel;

import java.io.File;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * SWT implementation of file selector widget.
 *
 * @author Curtis Rueden
 */
public class SWTFileWidget extends SWTInputWidget implements FileWidget {

	private Text path;
	private Button browse;

	public SWTFileWidget(final Composite parent, final WidgetModel model) {
		super(parent, model);

		setLayout(new MigLayout());

		path = new Text(this, 0);
		path.setTextLimit(20);

		browse = new Button(this, 0);
		browse.setText("Browse");

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

}
