package imagej.ui.swing;

import imagej.widget.UIComponent;

import java.awt.Container;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * A helper class that unifies common {@link JFrame} and {@link JDialog} API
 * methods in a single place.
 * 
 * @author Curtis Rueden
 */
public class SwingWindow implements UIComponent<Window> {

	private final JFrame frame;
	private final JDialog dialog;

	public SwingWindow(final JFrame frame) {
		this.frame = frame;
		this.dialog = null;
		// NB: For safety, always dispose the frame when it is closed.
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public SwingWindow(final JDialog dialog) {
		this.frame = null;
		this.dialog = dialog;
		// NB: For safety, always dispose the dialog when it is closed.
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public Container getContentPane() {
		if (frame != null) return frame.getContentPane();
		return dialog.getContentPane();
	}

	public void setContentPane(final Container pane) {
		if (frame != null) frame.setContentPane(pane);
		if (dialog != null) dialog.setContentPane(pane);
	}

	public String getTitle() {
		if (frame != null) return frame.getTitle();
		return dialog.getTitle();
	}

	public void setTitle(final String title) {
		if (frame != null) frame.setTitle(title);
		if (dialog != null) dialog.setTitle(title);
	}

	// -- UIComponent methods --

	@Override
	public Window getComponent() {
		if (frame != null) return frame;
		return dialog;
	}

	@Override
	public Class<Window> getComponentType() {
		return Window.class;
	}

}	
