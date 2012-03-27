/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.updater.util;

import java.util.Iterator;

public class ByteCodeAnalyzer implements Iterable<String> {

	byte[] buffer;
	int[] poolOffsets;
	int endOffset, interfacesOffset, fieldsOffset, methodsOffset,
			attributesOffset;
	Interface[] interfaces;
	Field[] fields;
	Method[] methods;
	Attribute[] attributes;

	public ByteCodeAnalyzer(final byte[] buffer) {
		this(buffer, false);
	}

	public ByteCodeAnalyzer(final byte[] buffer, final boolean readAll) {
		this.buffer = buffer;
		if ((int) getU4(0) != 0xcafebabe) throw new RuntimeException("No class");
		getConstantPoolOffsets();
		if (readAll) {
			getAllInterfaces();
			getAllFields();
			getAllMethods();
			getAllAttributes();
		}
	}

	public String getPathForClass() {
		final int thisOffset = dereferenceOffset(endOffset + 2);
		if (getU1(thisOffset) != 7) throw new RuntimeException("Parse error");
		return getString(dereferenceOffset(thisOffset + 1));
	}

	public String getClassNameConstant(final int index) {
		final int offset = poolOffsets[index - 1];
		if (getU1(offset) != 7) throw new RuntimeException("Constant " + index +
			" does not refer to a class");
		return getStringConstant(getU2(offset + 1)).replace('/', '.');
	}

	public String getStringConstant(final int index) {
		return getString(poolOffsets[index - 1]);
	}

	public boolean containsDebugInfo() {
		for (final Method method : methods)
			if (method.containsDebugInfo()) return true;
		return false;
	}

	int dereferenceOffset(final int offset) {
		final int index = getU2(offset);
		return poolOffsets[index - 1];
	}

	void getConstantPoolOffsets() {
		final int poolCount = getU2(8) - 1;
		poolOffsets = new int[poolCount];
		int offset = 10;
		for (int i = 0; i < poolCount; i++) {
			poolOffsets[i] = offset;
			final int tag = getU1(offset);
			if (tag == 7 || tag == 8) offset += 3;
			else if (tag == 9 || tag == 10 || tag == 11 || tag == 3 || tag == 4 ||
				tag == 12) offset += 5;
			else if (tag == 5 || tag == 6) {
				poolOffsets[++i] = offset;
				offset += 9;
			}
			else if (tag == 1) offset += 3 + getU2(offset + 1);
			else throw new RuntimeException("Unknown tag" + " " + tag);
		}
		endOffset = offset;
	}

	class ClassNameIterator implements Iterator<String> {

		int index;

		ClassNameIterator() {
			index = 0;
			findNext();
		}

		void findNext() {
			while (++index <= poolOffsets.length)
				if (getU1(poolOffsets[index - 1]) == 7) break;
		}

		@Override
		public boolean hasNext() {
			return index <= poolOffsets.length;
		}

		@Override
		public String next() {
			final int current = index;
			findNext();
			return getClassNameConstant(current);
		}

		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Iterator<String> iterator() {
		return new ClassNameIterator();
	}

	class InterfaceIterator implements Iterator<String> {

		int index, count;

		InterfaceIterator() {
			index = 0;
			count = getU2(endOffset + 6);
		}

		@Override
		public boolean hasNext() {
			return index < count;
		}

		@Override
		public String next() {
			return getClassNameConstant(getU2(endOffset + 8 + index++ * 2));
		}

		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

	public Iterable<String> getInterfaces() {
		return new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				return new InterfaceIterator();
			}
		};
	}

	public String getSuperclass() {
		final int index = getU2(endOffset + 4);
		if (index == 0) return null;
		return getClassNameConstant(index);
	}

	public String getSourceFile() {
		for (final Attribute attribute : attributes)
			if (getStringConstant(attribute.nameIndex).equals("SourceFile")) return getStringConstant(getU2(attribute.endOffset - 2));
		return null;
	}

	@Override
	public String toString() {
		String result = "";
		for (int i = 0; i < poolOffsets.length; i++) {
			final int offset = poolOffsets[i];
			result += "index #" + (i + 1) + ": " + format(offset) + "\n";
			final int tag = getU1(offset);
			if (tag == 5 || tag == 6) i++;
		}
		if (interfaces != null) for (int i = 0; i < interfaces.length; i++)
			result += "interface #" + (i + 1) + ": " + interfaces[i] + "\n";
		if (fields != null) for (int i = 0; i < fields.length; i++)
			result += "field #" + (i + 1) + ": " + fields[i] + "\n";
		if (methods != null) for (int i = 0; i < methods.length; i++)
			result += "method #" + (i + 1) + ": " + methods[i] + "\n";
		if (attributes != null) for (int i = 0; i < attributes.length; i++)
			result += "attribute #" + (i + 1) + ": " + attributes[i] + "\n";
		return result;
	}

	int getU1(final int offset) {
		return buffer[offset] & 0xff;
	}

	int getU2(final int offset) {
		return getU1(offset) << 8 | getU1(offset + 1);
	}

	long getU4(final int offset) {
		return ((long) getU2(offset)) << 16 | getU2(offset + 2);
	}

	String getString(final int offset) {
		try {
			return new String(buffer, offset + 3, getU2(offset + 1), "UTF-8");
		}
		catch (final Exception e) {
			return "";
		}
	}

	String format(final int offset) {
		final int tag = getU1(offset);
		final int u2 = getU2(offset + 1);
		final String result = "offset: " + offset + "(" + tag + "), ";
		if (tag == 7) return result + "class #" + u2;
		if (tag == 9) return result + "field #" + u2 + ", #" + getU2(offset + 3);
		if (tag == 10) return result + "method #" + u2 + ", #" + getU2(offset + 3);
		if (tag == 11) return result + "interface method #" + u2 + ", #" +
			getU2(offset + 3);
		if (tag == 8) return result + "string #" + u2;
		if (tag == 3) return result + "integer " + getU4(offset + 1);
		if (tag == 4) return result + "float " + getU4(offset + 1);
		if (tag == 12) return result + "name and type #" + u2 + ", #" +
			getU2(offset + 3);
		if (tag == 5) return result + "long " + getU4(offset + 1) + ", " +
			getU4(offset + 5);
		if (tag == 6) return result + "double " + getU4(offset + 1) + ", " +
			getU4(offset + 5);
		if (tag == 1) return result + "utf8 " + u2 + " " + getString(offset);
		return result + "unknown";
	}

	protected void getAllInterfaces() {
		interfacesOffset = endOffset + 6;
		interfaces = new Interface[getU2(interfacesOffset)];
		for (int i = 0; i < interfaces.length; i++)
			interfaces[i] = new Interface(interfacesOffset + 2 + i * 2);
	}

	protected class Interface {

		int nameIndex;

		public Interface(final int offset) {
			nameIndex = getU2(offset);
		}

		@Override
		public String toString() {
			return getClassNameConstant(nameIndex);
		}
	}

	protected void getAllFields() {
		fieldsOffset = interfacesOffset + 2 + 2 * interfaces.length;
		fields = new Field[getU2(fieldsOffset)];
		for (int i = 0; i < fields.length; i++)
			fields[i] =
				new Field(i == 0 ? fieldsOffset + 2 : fields[i - 1].endOffset);
	}

	protected class Field {

		int accessFlags, nameIndex, descriptorIndex;
		Attribute[] attributes;
		int endOffset;

		public Field(final int offset) {
			accessFlags = getU2(offset);
			nameIndex = getU2(offset + 2);
			descriptorIndex = getU2(offset + 4);
			attributes = getAttributes(offset + 6);
			endOffset =
				attributes.length == 0 ? offset + 8
					: attributes[attributes.length - 1].endOffset;
		}

		@Override
		public String toString() {
			return getStringConstant(nameIndex) +
				ByteCodeAnalyzer.this.toString(attributes);
		}
	}

	protected void getAllMethods() {
		methodsOffset =
			fields.length == 0 ? fieldsOffset + 2
				: fields[fields.length - 1].endOffset;
		methods = new Method[getU2(methodsOffset)];
		for (int i = 0; i < methods.length; i++)
			methods[i] =
				new Method(i == 0 ? methodsOffset + 2 : methods[i - 1].endOffset);
	}

	protected class Method extends Field {

		public Method(final int offset) {
			super(offset);
		}

		public boolean containsDebugInfo() {
			for (final Attribute attribute : attributes)
				if (attribute.containsDebugInfo()) return true;
			return false;
		}
	}

	protected void getAllAttributes() {
		attributesOffset =
			methods.length == 0 ? methodsOffset + 2
				: methods[methods.length - 1].endOffset;
		attributes = getAttributes(attributesOffset);
	}

	protected Attribute[] getAttributes(final int offset) {
		final Attribute[] result = new Attribute[getU2(offset)];
		for (int i = 0; i < result.length; i++)
			result[i] = new Attribute(i == 0 ? offset + 2 : result[i - 1].endOffset);
		return result;
	}

	protected class Attribute {

		int nameIndex;
		byte[] attribute;
		int endOffset;

		public Attribute(final int offset) {
			nameIndex = getU2(offset);
			attribute = new byte[(int) getU4(offset + 2)];
			System.arraycopy(buffer, offset + 6, attribute, 0, attribute.length);
			endOffset = offset + 6 + attribute.length;
		}

		public boolean containsDebugInfo() {
			if (!getStringConstant(nameIndex).equals("Code")) return false;
			for (final Attribute attribute : getAttributes())
				if (getStringConstant(attribute.nameIndex).equals("LocalVariableTable")) return true;
			return false;
		}

		protected Attribute[] getAttributes() {
			final int offset = endOffset - 6 - attribute.length;
			final int codeLength = (int) getU4(offset + 10);
			final int exceptionTableLength = getU2(offset + 14 + codeLength);
			final int attributesOffset =
				offset + 14 + codeLength + 2 + 8 * exceptionTableLength;
			return ByteCodeAnalyzer.this.getAttributes(attributesOffset);
		}

		@Override
		public String toString() {
			if (getStringConstant(nameIndex).equals("Code")) return "Code" +
				ByteCodeAnalyzer.this.toString(getAttributes());
			return getStringConstant(nameIndex) + " (length " + attribute.length +
				")";
		}
	}

	protected String toString(final Attribute[] attributes) {
		String result = "";
		for (final Attribute attribute : attributes)
			result += (result.equals("") ? "(" : ";") + attribute;
		return result.equals("") ? "" : result + ")";
	}
}
