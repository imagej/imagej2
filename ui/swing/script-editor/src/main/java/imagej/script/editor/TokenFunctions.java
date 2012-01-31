package fiji.scripting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;

public class TokenFunctions implements Iterable<Token> {
	RSyntaxTextArea textArea;

	public TokenFunctions(RSyntaxTextArea textArea) {
		this.textArea = textArea;
	}

	public static boolean tokenEquals(Token token, char[] text) {
		if (token.type != token.RESERVED_WORD ||
				token.textCount != text.length)
			return false;
		for (int i = 0; i < text.length; i++)
			if (token.text[token.textOffset + i] != text[i])
				return false;
		return true;
	}

	public static boolean isIdentifier(Token token) {
		if (token.type != token.IDENTIFIER)
			return false;
		if (!Character.isJavaIdentifierStart(token.text[token.textOffset]))
			return false;
		for (int i = 1; i < token.textCount; i++)
			if (!Character.isJavaIdentifierPart(token.text[token.textOffset + i]))
				return false;
		return true;
	}

	public static String getText(Token token) {
		if (token.text == null)
			return "";
		return new String(token.text,
				token.textOffset, token.textCount);
	}

	public void replaceToken(Token token, String text) {
		textArea.replaceRange(text, token.textOffset,
				token.textOffset + token.textCount);
	}

	class TokenIterator implements Iterator<Token> {
		int line = -1;
		Token current, next;

		public boolean hasNext() {
			if (next == null)
				getNextToken();
			return next != null;
		}

		public Token next() {
			current = next;
			next = null;
			return current;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		void getNextToken() {
			if (current != null) {
				next = current.getNextToken();
				if (next != null)
					return;
			}

			while (next == null) {
				if (++line >= textArea.getLineCount())
					return;

				next = textArea.getTokenListForLine(line);
			}
		}
	}

	public Iterator<Token> iterator() {
		return new TokenIterator();
	}

	public static boolean isDot(Token token) {
		return token.type == token.IDENTIFIER && token.textCount == 1
			&& token.text[token.textOffset] == '.';
	}

	/* The following methods are Java-specific */

	public final static char[] classCharacters = {'c', 'l', 'a', 's', 's'};
	public static boolean isClass(Token token) {
		return tokenEquals(token, classCharacters);
	}

	public String getClassName() {
		boolean classSeen = false;
		for (Token token : this)
			if (isClass(token))
				classSeen = true;
			else if (classSeen && isIdentifier(token))
				return getText(token);
		return null;
	}

	public void setClassName(String className) {
		boolean classSeen = false;
		for (Token token : this)
			if (isClass(token))
				classSeen = true;
			else if (classSeen && isIdentifier(token)) {
				replaceToken(token, className);
				return;
			}
	}

	class Import implements Comparable<Import> {
		int startOffset, endOffset;
		String classOrPackage;
		Import(int start, int end, String text) {
			startOffset = start;
			endOffset = end;
			classOrPackage = text;
		}

		String getPackage() {
			int dot = classOrPackage.lastIndexOf('.');
			return dot < 0 ? "" : classOrPackage.substring(0, dot);
		}

		public boolean equals(Import imp) {
			return classOrPackage.equals(imp.classOrPackage);
		}

		public int compareTo(Import imp) {
			return classOrPackage.compareTo(imp.classOrPackage);
		}

		public String toString() {
			return "Import(" + classOrPackage + ","
				+ startOffset + "-" + endOffset + ")";
		}
	}

	Token skipNonCode(TokenIterator iter, Token current) {
		for (;;) {
			switch (current.type) {
				case Token.COMMENT_DOCUMENTATION:
				case Token.COMMENT_EOL:
				case Token.COMMENT_MULTILINE:
				case Token.WHITESPACE:
					break;
				default:
					return current;
			}
			if (!iter.hasNext())
				return null;
			current = iter.next();
		}
	}

	int skipToEOL(TokenIterator iter, Token current) {
		int end = textArea.getDocument().getLength();
		for (;;) {
			if (current.type == current.NULL || !iter.hasNext())
				return end;
			end = current.offset + current.textCount;
			current = iter.next();
		}
	}

	public final char[] importChars = { 'i', 'm', 'p', 'o', 'r', 't' };
	public List<Import> getImports() {
		List<Import> result = new ArrayList<Import>();

		TokenIterator iter = new TokenIterator();
		while (iter.hasNext()) {
			Token token = iter.next();
			int offset = token.offset;
			token = skipNonCode(iter, token);
			if (tokenEquals(token, importChars)) {
				do {
					if (!iter.hasNext())
						return result;
					token = iter.next();
				} while (!isIdentifier(token));
				int start = token.offset, end = start;
				do {
					if (!iter.hasNext())
						return result;
					token = iter.next();
					if (isDot(token) && iter.hasNext())
						token = iter.next();
					end = token.offset + token.textCount;
				} while (isIdentifier(token));
				String identifier = getText(start, end);
				if (identifier.endsWith(";"))
					identifier = identifier.substring(0,
						identifier.length() - 1);
				end = skipToEOL(iter, token);
				result.add(new Import(offset, end, identifier));
			}
		}

		return result;
	}

	public String getText(int start, int end) {
		try {
			return textArea.getText(start, end - start);
		} catch (BadLocationException e) { /* ignore */ }
		return "";
	}

	public boolean emptyLineAt(int offset) {
		return getText(offset, offset + 2).equals("\n\n");
	}

	public boolean eolAt(int offset) {
		return getText(offset, offset + 1).equals("\n");
	}

	void removeImport(Import imp) {
		int start = imp.startOffset, end = imp.endOffset;
		if (emptyLineAt(start - 2) && emptyLineAt(end))
			end += 2;
		else if (eolAt(end))
			end++;
		textArea.replaceRange("", start, end);
	}

	public void removeUnusedImports() {
		Set<String> identifiers = getAllUsedIdentifiers();
		List<Import> imports = getImports();
		for (int i = imports.size() - 1; i >= 0; i--) {
			Import imp = imports.get(i);
			String clazz = imp.classOrPackage;
			if (clazz.endsWith(".*"))
				continue;
			int dot = clazz.lastIndexOf('.');
			if (dot >= 0)
				clazz = clazz.substring(dot + 1);
			if (!identifiers.contains(clazz))
				removeImport(imp);
		}
	}

	public void addImport(String className) {
		List<Import> imports = getImports();

		if (imports.size() == 0) {
			TokenIterator iter = new TokenIterator();
			int offset = 0;
			boolean insertLF = false;
			while (iter.hasNext()) {
				Token token = iter.next();
				if (token.type != token.RESERVED_WORD) {
					insertLF = false;
					continue;
				}
				if (getText(token).equals("package")) {
					skipToEOL(iter, token);
					insertLF = true;
				}
				else {
					offset = token.offset;
					break;
				}
			}
			textArea.insert((insertLF ? "\n" : "") +
				"import " + className + ";\n\n", offset);
			return;
		}

		String string = "import " + className + ";\n";
		Import imp = new Import(0, 0, className);
		int after = -1, startOffset, endOffset;

		for (int i = 0; i < imports.size(); i++) {
			int cmp = imports.get(i).compareTo(imp);
			if (cmp == 0)
				return;
			if (cmp < 0)
				after = i;
		}

		// 'after' is the index of the import after which we
		// want to insert the current import.
		if (after < 0) {
			startOffset = imports.get(0).startOffset;
			if (startOffset > 1 && !getText(startOffset - 2, startOffset).equals("\n\n"))
				string = "\n" + string;
		}
		else {
			startOffset = imports.get(after).endOffset;
			string = "\n" + string;
			if (!imp.getPackage().equals(imports.get(after).getPackage()))
				string = "\n" + string;
		}
		if (after + 1 < imports.size()) {
			endOffset = imports.get(after + 1).startOffset;
			if (!imp.getPackage().equals(imports.get(after + 1).getPackage()))
				string += "\n";
		}
		else {
			if (after < 0)
				endOffset = startOffset;
			else
				endOffset = imports.get(after).endOffset;
			if (endOffset + 1 < textArea.getDocument().getLength() &&
					!getText(endOffset, endOffset + 2).equals("\n\n"))
				string += "\n";
		}
		textArea.replaceRange(string, startOffset, endOffset);
	}

	public Set<String> getAllUsedIdentifiers() {
		Set<String> result = new HashSet<String>();
		boolean classSeen = false;
		String className = null;
		for (Token token : this)
			if (isClass(token))
				classSeen = true;
			else if (classSeen && className == null &&
					isIdentifier(token))
				className = getText(token);
			else if (classSeen && isIdentifier(token))
				result.add(getText(token));
		return result;
	}

	public void sortImports() {
		List<Import> imports = getImports();
		if (imports.size() == 0)
			return;
		int start = imports.get(0).startOffset;
		while (emptyLineAt(start - 2))
			start--;
		int end = imports.get(imports.size() - 1).endOffset;
		while (eolAt(end))
			end++;

		Collections.sort(imports, new Comparator<Import>() {
			public int compare(Import i1, Import i2) {
				return i1.classOrPackage.compareTo(i2.classOrPackage);
			}

			public boolean equals(Object o) {
				return false;
			}
		});

		StringBuffer buffer = new StringBuffer();
		String lastPrefix = null;
		String lastImport = null;
		for (Import imp : imports) {
			if (imp.classOrPackage.equals(lastImport))
				continue;
			lastImport = imp.classOrPackage;

			String prefix = imp.getPackage();
			if (!prefix.equals(lastPrefix)) {
				buffer.append("\n");
				lastPrefix = prefix;
			}
			// TODO: honor comments
			buffer.append(getText(imp.startOffset, imp.endOffset));
			buffer.append("\n");
		}
		buffer.append("\n");

		textArea.replaceRange(buffer.toString(), start, end);
	}

	public void removeTrailingWhitespace() {
		int end = textArea.getDocument().getLength();

		// Turn CR and CRLF into LF
		for (int i = end - 1; i >= 0; i--)
			if (getText(i, i + 1).equals("\r")) {
				boolean isCRLF = i < end - 1 && getText(i + 1, i + 2).equals("\n");
				textArea.replaceRange("\n", i, i + 1 + (isCRLF ? 1 : 0));
				if (isCRLF)
					end--;
			}

		// remove trailing empty lines
		int realEnd = end;
		if (eolAt(end - 1)) {
			while (eolAt(end - 2) ||
					getText(end - 2, end - 1).equals("\r"))
				end--;
			if (end < realEnd)
				textArea.replaceRange("", end - 1, realEnd - 1);
		}

		// remove trailing white space from each line
		for (int i = textArea.getLineCount() - 1; i >= 0; i--) try {
			int start = textArea.getLineStartOffset(i);
			if (eolAt(end - 1))
				end--;
			if (start == end)
				continue;
			String line = getText(start, end);
			realEnd = end;
			while (end - start - 1 >= 0 && Character.isWhitespace(line
					.charAt(end - start - 1)))
				end--;
			if (end < realEnd)
				textArea.replaceRange("", end, realEnd);
			end = start;
		} catch (BadLocationException e) { /* cannot happen */ }
	}
}
