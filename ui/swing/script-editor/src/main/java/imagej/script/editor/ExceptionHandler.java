//
// ExceptionHandler.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.script.editor;

import fiji.scripting.ErrorHandler;
import fiji.scripting.ErrorHandler.Error;

import ij.IJ;

import ij.text.TextWindow;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.PrintWriter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JTextArea;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class ExceptionHandler implements IJ.ExceptionHandler {
	protected Map<ThreadGroup, TextEditor> threadMap =
		new WeakHashMap<ThreadGroup, TextEditor>();

	protected IJ.ExceptionHandler fallBack;

	protected ExceptionHandler(IJ.ExceptionHandler fallBackHandler) {
		fallBack = fallBackHandler;
	}

	public static void addThread(Thread thread, TextEditor editor) {
		addThreadGroup(thread.getThreadGroup(), editor);
	}

	public static void addThreadGroup(ThreadGroup group, TextEditor editor) {
		ExceptionHandler handler = getInstance();
		handler.threadMap.put(group, editor);
	}

	public static ExceptionHandler getInstance() {
		IJ.ExceptionHandler current = null;

		try {
			current = getExceptionHandler();
			if (current instanceof ExceptionHandler)
				return (ExceptionHandler)current;
		}
		catch (Exception e) {
			/* ignore */
		}
		catch (NoSuchMethodError e) {
			/* ignore */
		}

		if (current == null)
			current = new IJ.ExceptionHandler() {
				public void handle(Throwable t) {
					legacyHandle(t);
				}
			};

		ExceptionHandler result = new ExceptionHandler(current);
		IJ.setExceptionHandler(result);
		return result;
	}

	protected static IJ.ExceptionHandler getExceptionHandler() {
		try {
			Field handler = IJ.class.getDeclaredField("exceptionHandler");
			handler.setAccessible(true);
			return (IJ.ExceptionHandler)handler.get(null);
		} catch (Exception e) {
			return null;
		}
	}

	public void handle(Throwable t) {
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		while (group != null) {
			TextEditor editor = threadMap.get(group);
			if (editor != null) {
				handle(t, editor);
				return;
			}
			group = group.getParent();
		}
		fallBack.handle(t);
	}

	public static void legacyHandle(Throwable t) {
		CharArrayWriter writer = new CharArrayWriter();
		t.printStackTrace(new PrintWriter(writer));
		new TextWindow("Exception", writer.toString(), 350, 250);
	}

	public static void handle(Throwable t, TextEditor editor) {
		JTextArea screen = editor.errorScreen;
		editor.getTab().showErrors();

		if (t instanceof InvocationTargetException) {
			t = ((InvocationTargetException)t).getTargetException();
		}
		StackTraceElement[] trace = t.getStackTrace();

		screen.insert(t.getClass().getName() + ": "
				+ t.getMessage() + "\n", screen.getDocument().getLength());
		ErrorHandler handler = new ErrorHandler(screen);
		for (int i = 0; i < trace.length; i++) {
			String fileName = trace[i].getFileName();
			int line = trace[i].getLineNumber();
			String text = "\t at " + trace[i].getClassName()
					+ "." + trace[i].getMethodName()
					+ "(" + fileName + ":" + line + ")\n";
			File file = editor.getFileForBasename(fileName);
			handler.addError(file == null ? null : file.getAbsolutePath(), line, text);
		}

		editor.errorHandler = handler;
	}
}
