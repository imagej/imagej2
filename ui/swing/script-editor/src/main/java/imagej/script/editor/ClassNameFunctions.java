package fiji.scripting;

import fiji.scripting.completion.ClassCompletionProvider;
import fiji.scripting.completion.ClassNames;

import ij.gui.GenericDialog;

import ij.plugin.BrowserLauncher;

import java.awt.Frame;

import java.util.List;

import javax.swing.JOptionPane;

public class ClassNameFunctions {
	ClassNames names;
	Frame parent;

	public ClassNameFunctions(Frame parent, ClassNames names) {
		this.parent = parent;
		this.names = names;
	}

	public ClassNameFunctions(ClassNames names) {
		this(null, names);
	}

	public ClassNameFunctions(ClassCompletionProvider provider) {
		this(provider.getClassNames());
	}

	public ClassNameFunctions(Frame parent,
			ClassCompletionProvider provider) {
		this(parent, provider.getClassNames());
	}

	/**
	 * Return the full name (including the package) for a given class name.
	 *
	 * If there are multiple classes of the specified name, ask the user.
	 * Returns null if the user canceled, or if there was no class of that
	 * name.
	 */
	public String getFullName(String className) {
		if (className.indexOf('.') > 0)
			return className;

		List<String> list = names.getFullClassNames(className);
		if (list.size() == 0) {
			JOptionPane.showMessageDialog(parent, "Class '"
					+ className + "' was not found!");
			return null;
		}
		if (list.size() == 1)
			return list.get(0);
		String[] names = list.toArray(new String[list.size()]);
		GenericDialog gd = new GenericDialog("Choose class", parent);
		gd.addChoice("class", names, names[0]);
		gd.showDialog();
		if (gd.wasCanceled())
			return null;
		return gd.getNextChoice();
	}

	public void openHelpForClass(String className, boolean withFrames) {
		String fullName = getFullName(className);
		if (fullName == null)
			return;
		String urlPrefix;
		if (fullName.startsWith("java.") ||
				fullName.startsWith("javax."))
			urlPrefix = "http://download.oracle.com/javase/6/docs/api/";
		else
			urlPrefix = "http://fiji.sc/javadoc/";
		new BrowserLauncher().run(urlPrefix
				+ (withFrames ? "index.html?" : "")
				+ fullName.replace('.', '/') + ".html");
	}
}
