//
// HeadlessUI.java
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

package imagej.ui.headless;

import imagej.event.EventSubscriber;
import imagej.event.StatusEvent;
import imagej.ui.DialogPrompt;
import imagej.ui.OutputWindow;
import imagej.ui.StatusBar;
import imagej.ui.ToolBar;
import imagej.ui.UI;
import imagej.ui.UserInterface;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Headless-based user interface for ImageJ.
 * 
 * @author Curtis Rueden
 */
@UI
public class HeadlessUI implements UserInterface, EventSubscriber<StatusEvent>
{

	private ScriptEngine engine;
	private BufferedReader in;
	private PrintStream out;

	public void setLanguage(final String language) {
		final ScriptEngineManager factory = new ScriptEngineManager();
		engine = factory.getEngineByName(language);
	}

	public ScriptEngine getScriptEngine() {
		return engine;
	}

	public BufferedReader getIn() {
		return in;
	}

	public PrintStream getOut() {
		return out;
	}

	public void doInterpreter(final InputStream inStream,
		final PrintStream outStream) throws IOException
	{
		in = new BufferedReader(new InputStreamReader(inStream));
		out = outStream;

		while (true) {
			out.print("> ");
			final String line = in.readLine();
			if (line == null) break;
			try {
				final Object result = engine.eval(line);
				out.println("" + result);
			}
			catch (final ScriptException e) {
				Log.error(e);
			}
		}

		in.close();
		out.close();
	}

	// -- UserInterface methods --

	@Override
	public void initialize() {
		setLanguage("JavaScript");
	}

	@Override
	public void processArgs(final String[] args) {
		String language = null;
		boolean langNext = false;

		for (final String arg : args) {
			if (arg == null) {
				Log.warn("Ignoring null argument");
				continue;
			}
			final String targ = arg.trim();
			if (targ.isEmpty()) {
				Log.warn("Ignoring empty argument");
			}
			else if (langNext) {
				language = arg;
				langNext = false;
			}
			else if (arg.equals("-language")) {
				langNext = true;
			}
			else {
				Log.warn("Ignoring unknown argument: " + arg);
			}
		}

		if (language != null) setLanguage(language);
		try {
			doInterpreter(System.in, System.out);
		}
		catch (final IOException e) {
			Log.error(e);
		}
	}

	@Override
	public ToolBar getToolBar() {
		return null;
	}

	@Override
	public StatusBar getStatusBar() {
		return null;
	}

	@Override
	public OutputWindow newOutputWindow(final String title) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DialogPrompt dialogPrompt(final String message, final String title,
		final MessageType msg, final OptionType option)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	// -- EventSubscriber methods --

	@Override
	public void onEvent(final StatusEvent event) {
		final String message = event.getStatusMessage();
		final int val = event.getProgressValue();
		final int max = event.getProgressMaximum();

		final boolean doMessage = message != null;
		final boolean doProgress = val >= 0 && max > 0;

		final StringBuilder sb = new StringBuilder();

		if (doMessage) {
			sb.append(message);
			if (doProgress) sb.append(" ");
		}
		if (doProgress) sb.append("[" + val + "/" + max + "]");

		Log.info(sb.toString());
	}

}
