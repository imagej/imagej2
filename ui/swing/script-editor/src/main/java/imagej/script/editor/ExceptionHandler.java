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

package imagej.script.editor;

import imagej.log.LogService;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JTextArea;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
public class ExceptionHandler {
	private static ExceptionHandler instance;
	private final LogService log;
	private Map<ThreadGroup, TextEditor> threadMap;

	// prevent instantiation from somewhere else
	private ExceptionHandler(final LogService logService) {
		this.log = logService;
		threadMap = new WeakHashMap<ThreadGroup, TextEditor>();
	}

	public static ExceptionHandler getInstance(final LogService logService) {
		if (instance == null) {
			instance = new ExceptionHandler(logService);
		} else if (instance.log != logService) {
			throw new RuntimeException("Cannot have an ExceptionHandler with two different LogServices");
		}
		return instance;
	}

	public static void addThread(Thread thread, TextEditor editor) {
		addThreadGroup(thread.getThreadGroup(), editor);
	}

	public static void addThreadGroup(ThreadGroup group, TextEditor editor) {
		ExceptionHandler handler = getInstance(editor.log);
		handler.threadMap.put(group, editor);
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
		log.error(t);
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
