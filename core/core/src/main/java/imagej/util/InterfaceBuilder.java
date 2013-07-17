/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper to make an interface from an array of functions.
 * 
 * @author Johannes Schindelin
 */
public class InterfaceBuilder {
	private ConstantPool constantPool = new ConstantPool();
	private byte[] byteCode;

	public InterfaceBuilder(final String name, final Method[] methods) throws UnsupportedEncodingException {
		byteCode = makeInterface(name, methods);
	}

	public byte[] toByteArray() {
		return byteCode;
	}

	private byte[] makeInterface(final String name, final Method[] methods) throws UnsupportedEncodingException {
		return concat(new byte[] {
			// magic: 0xcafebabe
			-54, -2, -70, -66,
			// class file format version (JDK 1.4 = 48.0)
			0, 0, 0, 48,
		},
		// constant pool
		constantPool,
		// class access flags (PUBLIC | SUPER)
		(short)(Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.INTERFACE),
		// this / super class
		classRef(name), classRef(Object.class.getName()),
		// interfaces implemented
		(short)0,
		// field count
		(short)0,
		// method count
		(short)methods.length,
		// the method declarations
		methods(methods),
		// class attribute count
		(short)0
		);
	}

	private class ConstantPool {
		final List<byte[]> elements = new ArrayList<byte[]>();
		final Map<byte[], Short> map = new HashMap<byte[],Short>();

		private int length() {
			int result = 2;
			for (final byte[] element : elements) result += element.length;
			return result;
		}

		private byte[] toByteArray() {
			return concat((short)(elements.size() + 1), concat(elements.toArray()));
		}
	}

	private short constant(final byte[] element) {
		Short s = constantPool.map.get(element);
		if (s == null) {
			constantPool.elements.add(element);
			s = Short.valueOf((short) constantPool.elements.size());
			constantPool.map.put(element, s);
		}
		return s.shortValue();
	}

	private short string(final String string) throws UnsupportedEncodingException {
		final byte[] bytes = string.getBytes("UTF-8");
		return constant(concat((byte)1, (short)bytes.length, bytes));
	}

	private short classRef(final short stringRef) {
		return constant(concat((byte)7, stringRef));
	}

	private short classRef(final String name) throws UnsupportedEncodingException {
		return classRef(string(name.replace('.', '/')));
	}

	private static String typeDescriptor(final Class<?> type) {
		if (type == Void.TYPE) return "V";
		if (type == Boolean.TYPE) return "Z";
		if (type == Byte.TYPE) return "B";
		if (type == Short.TYPE) return "S";
		if (type == Integer.TYPE) return "I";
		if (type == Long.TYPE) return "L";
		if (type == Float.TYPE) return "F";
		if (type == Double.TYPE) return "D";
		return "L" + type.getName().replace('.', '/') + ";";
	}

	private static String signature(final Class<?> returnType, final Class<?>... parameterTypes) {
		final StringBuilder builder = new StringBuilder();
		builder.append("(");
		for (final Class<?> parameterType : parameterTypes) builder.append(typeDescriptor(parameterType));
		builder.append(")").append(typeDescriptor(returnType));
		return builder.toString();
	}

	private byte[] method(final String name, final String descriptor) throws UnsupportedEncodingException {
		return concat((short)(Modifier.PUBLIC | Modifier.ABSTRACT),
			string(name),
			string(descriptor),
			(short)0 /* attribute count */);
	}

	private byte[] method(final Method method) throws UnsupportedEncodingException {
		return method(method.getName(), signature(method.getReturnType(), method.getParameterTypes()));
	}

	private byte[] methods(final Method[] methods) throws UnsupportedEncodingException {
		final Object[] result = new byte[methods.length][];
		for (int i = 0; i < methods.length; i++) {
			result[i] = method(methods[i]);
		}
		return concat(result);
	}

	private byte[] concat(final Object... list) {
		int length = 0;
		for (final Object element : list) {
			if (element == null) continue;
			if (Byte.class.isInstance(element)) length++;
			else if (Short.class.isInstance(element)) length += 2;
			else if (Integer.class.isInstance(element)) length += 4;
			else if (element instanceof byte[]) {
				final byte[] array = (byte[])element;
				length += array.length;
			} else if (element == constantPool) {
				length += constantPool.length();
			} else {
				System.err.println("Cannot handle element of type " + element.getClass());
			}
		}
		final byte[] result = new byte[length];
		int offset =  0;
		for (final Object element : list) {
			if (element == null) continue;
			if (Byte.class.isInstance(element)) {
				result[offset++] = ((Byte)element).byteValue();
			}
			else if (Short.class.isInstance(element)) {
				final short value = ((Short)element).shortValue();
				result[offset++] = (byte)((value & 0xff00) >> 8);
				result[offset++] = (byte)(value & 0xff);
			}
			else if (Integer.class.isInstance(element)) {
				final int value = ((Integer)element).intValue();
				result[offset++] = (byte)((value & 0xff000000l) >> 24);
				result[offset++] = (byte)((value & 0xff0000) >> 16);
				result[offset++] = (byte)((value & 0xff00) >> 8);
				result[offset++] = (byte)(value & 0xff);
			}
			else if (element instanceof byte[] || element == constantPool) {
				final byte[] array = element == constantPool ? constantPool.toByteArray() : (byte[])element;
				System.arraycopy(array, 0, result, offset, array.length);
				offset += array.length;
			}
		}
		return result;
	}

  protected static void hexdump(byte[] bytes) {
    System.err.println("\nhexdump of " + bytes.length + " bytes:");
    for (int offset = 0; offset < bytes.length; offset += 0x10) {
            System.err.printf("%08x  ", offset);
            String suffix = " |";
            for (int i = 0; i < 0x10; i++) {
                    if (offset + i < bytes.length) {
                            int b = bytes[offset + i] & 0xff;
                            System.err.printf("%02x ", b);
                            suffix += (b >= 0x20 && b < 0x80 ? (char)b : '.');
                    } else {
                            System.err.print("   ");
                            suffix += " ";
                    }
                    if (i == 7) System.err.print(" ");
            }
            System.err.println(suffix + "|");
    }
}

	public static void main(final String... args)
		throws UnsupportedEncodingException, SecurityException,
		NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
		InvocationTargetException
	{
		final String name = "imagej/Dancelable";
		final Class<?> i = imagej.Cancelable.class;
		final InterfaceBuilder builder = new InterfaceBuilder(name, i.getDeclaredMethods());
		final ClassLoader loader = builder.getClass().getClassLoader();
		final Method define = ClassLoader.class.getDeclaredMethod("defineClass",
			String.class, byte[].class, int.class, int.class);
		define.setAccessible(true);
		final byte[] byteCode = builder.toByteArray();
		hexdump(byteCode);
		/*
		 * Expect:
00000000  ca fe ba be 00 00 00 30  00 09 07 00 07 07 00 08  |.......0........|
00000010  01 00 0a 69 73 43 61 6e  63 65 6c 65 64 01 00 03  |...isCanceled...|
00000020  28 29 5a 01 00 0f 67 65  74 43 61 6e 63 65 6c 52  |()Z...getCancelR|
00000030  65 61 73 6f 6e 01 00 14  28 29 4c 6a 61 76 61 2f  |eason...()Ljava/|
00000040  6c 61 6e 67 2f 53 74 72  69 6e 67 3b 01 00 11 69  |lang/String;...i|
00000050  6d 61 67 65 6a 2f 43 61  6e 63 65 6c 61 62 6c 65  |magej/Cancelable|
00000060  01 00 10 6a 61 76 61 2f  6c 61 6e 67 2f 4f 62 6a  |...java/lang/Obj|
00000070  65 63 74 06 01 00 01 00  02 00 00 00 00 00 02 04  |ect.............|
00000080  01 00 03 00 04 00 00 04  01 00 05 00 06 00 00 00  |................|
00000090  00                                                |.|
00000091
		 */
		try {
java.io.FileOutputStream out = new java.io.FileOutputStream("/tmp/imagej/Dancelable.class");
out.write(byteCode, 0, byteCode.length);
out.close();
		} catch (java.io.IOException e) { e.printStackTrace(); }
		define.invoke(loader, name.replace('/', '.'), byteCode, 0, byteCode.length);
	}

}
