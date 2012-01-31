package fiji.scripting;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.JOptionPane;
import java.io.*;
import javax.swing.filechooser.*;
import javax.swing.colorchooser.*;
import java.awt.image.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
/*for the cell renderer part*/
import java.net.MalformedURLException;
import java.net.URL;
/*cell renderer part ends here*/
import javax.imageio.*;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.*;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.autocomplete.*;

public class CCellRenderer extends CompletionCellRenderer {

	private Icon variableIcon;
	private Icon functionIcon;
	private Icon emptyIcon;


	/**
	 * Constructor.
	 */
	public CCellRenderer() {
		variableIcon = getIcon("images/var.png");
		functionIcon = getIcon("images/function.png");
		emptyIcon = new EmptyIcon(16);
	}


	/**
	 * Returns an icon.
	 *
	 * @param resource The icon to retrieve.  This should either be a file,
	 *        or a resource loadable by the current ClassLoader.
	 * @return The icon.
	 */
	private Icon getIcon(String resource) {
		ClassLoader cl = getClass().getClassLoader();
		URL url = cl.getResource(resource);
		if (url == null) {
			File file = new File(resource);
			try {
				url = file.toURI().toURL();
			} catch (MalformedURLException mue) {
				mue.printStackTrace(); // Never happens
			}
		}
		return url != null ? new ImageIcon(url) : null;
	}


	/**
	 * {@inheritDoc}
	 */
	protected void prepareForOtherCompletion(JList list,
	                Completion c, int index, boolean selected, boolean hasFocus) {
		super.prepareForOtherCompletion(list, c, index, selected, hasFocus);
		setIcon(emptyIcon);
	}


	/**
	 * {@inheritDoc}
	 */
	protected void prepareForVariableCompletion(JList list,
	                VariableCompletion vc, int index, boolean selected,
	                boolean hasFocus) {
		super.prepareForVariableCompletion(list, vc, index, selected,
		                                   hasFocus);
		setIcon(variableIcon);
	}


	/**
	 * {@inheritDoc}
	 */
	protected void prepareForFunctionCompletion(JList list,
	                FunctionCompletion fc, int index, boolean selected,
	                boolean hasFocus) {
		super.prepareForFunctionCompletion(list, fc, index, selected,
		                                   hasFocus);
		setIcon(functionIcon);
	}


	/**
	 * An standard icon that doesn't paint anything.  This can be used to take
	 * up an icon's space when no icon is specified.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private static class EmptyIcon implements Icon, Serializable {

		private int size;

		public EmptyIcon(int size) {
			this.size = size;
		}

		public int getIconHeight() {
			return size;
		}

		public int getIconWidth() {
			return size;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
		}

	}


}


