package fiji.scripting;

import ij.IJ;
import ij.Menus;

import ij.macro.Interpreter;

import ij.plugin.MacroInstaller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MacroFunctions {

	protected final static String macroFunctionsLabel = "Macro Functions...";

	public void openHelp(String name) {
		if (name != null) {
			name = startsWithIdentifier(name);
			if (name != null) {
				String url = (String)Menus.getCommands().get(macroFunctionsLabel);
				if (url != null && !url.equals("")) {
					Pattern regex = Pattern.compile("^ij.plugin.BrowserLauncher\\(\"(.*)\"\\)$");
					Matcher matcher = regex.matcher(url);
					if (matcher.matches()) {
						IJ.runPlugIn("ij.plugin.BrowserLauncher", matcher.group(1) + "#" + name);
						return;
					}
				}
			}
		}
		IJ.run(macroFunctionsLabel);
	}

	protected String startsWithIdentifier(String text) {
		if (text == null)
			return null;
		char[] array = text.toCharArray();
		int start = 0;
		while (start < array.length && Character.isWhitespace(array[start]))
			start++;
		if (start >= array.length ||
				!Character.isJavaIdentifierStart(array[start]))
			return null;
		int end = start + 1;
		while (end < array.length && Character.isJavaIdentifierPart(array[end]))
			end++;
		return new String(array, start, end - start);
	}
}