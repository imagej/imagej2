package imagej.script.editor;

import java.io.IOException;
import java.net.URL;

public class MacroFunctions {

	public final String MACRO_FUNCTIONS_URL = "http://imagej.net/developer/macro/functions.html";

	private TextEditor editor;

	public MacroFunctions(TextEditor editor) {
		this.editor = editor;
	}

	public void openHelp(String name) throws IOException {
		String url = MACRO_FUNCTIONS_URL;
		if (name != null) {
			String functionName = startsWithIdentifier(name);
			if (functionName != null) {
				url += "#" + functionName;
			}
		}
		editor.platformService.open(new URL(url));
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
