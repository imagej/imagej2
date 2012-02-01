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

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JTextArea;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

	protected EditorFrame textEditor;

	public ExceptionHandler(final EditorFrame textEditor) {
		this.textEditor = textEditor;
	}

	@Override
	public void uncaughtException(final Thread thread, final Throwable t) {
		handle(t, textEditor);
	}

	public static void handle(Throwable t, final EditorFrame editor) {
		final JTextArea screen = editor.errorScreen;
		editor.getTab().showErrors();

		if (t instanceof InvocationTargetException) {
			t = ((InvocationTargetException) t).getTargetException();
		}
		final StackTraceElement[] trace = t.getStackTrace();

		screen.insert(t.getClass().getName() + ": " + t.getMessage() + "\n", screen
			.getDocument().getLength());
		final ErrorHandler handler = new ErrorHandler(screen);
		for (int i = 0; i < trace.length; i++) {
			final String fileName = trace[i].getFileName();
			final int line = trace[i].getLineNumber();
			final String text =
				"\t at " + trace[i].getClassName() + "." + trace[i].getMethodName() +
					"(" + fileName + ":" + line + ")\n";
			final File file = editor.getFileForBasename(fileName);
			handler
				.addError(file == null ? null : file.getAbsolutePath(), line, text);
		}

		editor.errorHandler = handler;
	}

}
