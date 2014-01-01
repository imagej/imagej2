/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

package imagej.plugins.commands.debug;

import imagej.command.Command;
import imagej.menu.MenuConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Provides a complete stack dump of all threads.
 * <p>
 * The output is similar to a subset of that given when Ctrl+\ (or Ctrl+Pause on
 * Windows) is pressed from the console.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.PLUGINS_LABEL,
		weight = MenuConstants.PLUGINS_WEIGHT,
		mnemonic = MenuConstants.PLUGINS_MNEMONIC), @Menu(label = "Debug"),
	@Menu(label = "Dump Stack", accelerator = "ctrl back_slash") },
	headless = true)
public class DumpStack implements Command {

	// -- Constants --

	private static final String NL = System.getProperty("line.separator");

	// -- Parameters --

	@Parameter(label = "Stack Dump", type = ItemIO.OUTPUT)
	private String stackDump;

	// -- Runnable methods --

	@Override
	public void run() {
		final StringBuilder sb = new StringBuilder();

		final Map<Thread, StackTraceElement[]> stackTraces =
			Thread.getAllStackTraces();

		// sort list of threads by name
		final ArrayList<Thread> threads =
			new ArrayList<Thread>(stackTraces.keySet());
		Collections.sort(threads, new Comparator<Thread>() {

			@Override
			public int compare(final Thread t1, final Thread t2) {
				return t1.getName().compareTo(t2.getName());
			}
		});

		for (final Thread t : threads) {
			dumpThread(t, stackTraces.get(t), sb);
		}

		stackDump = sb.toString();
	}

	// -- Helper methods --

	private void dumpThread(final Thread t, final StackTraceElement[] trace,
		final StringBuilder sb)
	{
		threadInfo(t, sb);
		for (final StackTraceElement element : trace) {
			sb.append("\tat ");
			sb.append(element);
			sb.append(NL);
		}
		sb.append(NL);
	}

	private void threadInfo(final Thread t, final StringBuilder sb) {
		sb.append("\"");
		sb.append(t.getName());
		sb.append("\"");
		if (!t.isAlive()) sb.append(" DEAD");
		if (t.isInterrupted()) sb.append(" INTERRUPTED");
		if (t.isDaemon()) sb.append(" daemon");
		sb.append(" prio=");
		sb.append(t.getPriority());
		sb.append(" id=");
		sb.append(t.getId());
		sb.append(" group=");
		sb.append(t.getThreadGroup().getName());
		sb.append(NL);
		sb.append("   java.lang.Thread.State: ");
		sb.append(t.getState());
		sb.append(NL);
	}

}
