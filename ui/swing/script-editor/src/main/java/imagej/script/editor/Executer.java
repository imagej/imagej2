//
// Executer.java
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

import imagej.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generic Thread that keeps a starting time stamp, sets the priority to normal
 * and starts itself.
 */
public abstract class Executer extends ThreadGroup {

	/**
	 * 
	 */
	private final EditorFrame editorFrame;
	protected JTextAreaWriter writer, errorWriter;

	@Override
	public void uncaughtException(final Thread thread, final Throwable throwable)
	{
		// TODO!
		throw new RuntimeException("TODO");
	}

	public Executer(final EditorFrame editorFrame, final JTextAreaWriter writer,
		final JTextAreaWriter errorWriter)
	{
		super("Script Editor Run :: " + new Date().toString());
		this.editorFrame = editorFrame;
		this.writer = writer;
		this.errorWriter = errorWriter;
		// Store itself for later
		this.editorFrame.executingTasks.add(this);
		this.editorFrame.setTitle();
		// Enable kill menu
		this.editorFrame.kill.setEnabled(true);
		// Fork a task, as a part of this ThreadGroup
		new Thread(this, getName()) {

			{
				setPriority(Thread.NORM_PRIORITY);
				start();
			}

			@Override
			public void run() {
				try {
					execute();
					// Wait until any children threads die:
					int activeCount = getThreadGroup().activeCount();
					while (activeCount > 1) {
						if (isInterrupted()) break;
						try {
							Thread.sleep(500);
							final List<Thread> ts = getAllThreads();
							activeCount = ts.size();
							if (activeCount <= 1) break;
							Log.debug("Waiting for " + ts.size() + " threads to die");
							int count_zSelector = 0;
							for (final Thread t : ts) {
								if (t.getName().equals("zSelector")) {
									count_zSelector++;
								}
								Log.debug("THREAD: " + t.getName());
							}
							if (activeCount == count_zSelector + 1) {
								// Do not wait on the stack slice selector thread.
								break;
							}
						}
						catch (final InterruptedException ie) {}
					}
				}
				catch (final Throwable t) {
					Executer.this.editorFrame.handleException(t);
				}
				finally {
					Executer.this.editorFrame.executingTasks
						.remove(Executer.this.editorFrame);
					try {
						if (null != writer) writer.close();
						if (null != errorWriter) errorWriter.close();
					}
					catch (final Exception e) {
						Executer.this.editorFrame.handleException(e);
					}
					// Leave kill menu item enabled if other tasks are running
					Executer.this.editorFrame.kill
						.setEnabled(Executer.this.editorFrame.executingTasks.size() > 0);
					Executer.this.editorFrame.setTitle();
				}
			}
		};
	}

	/** The method to extend, that will do the actual work. */
	public abstract void execute();

	/** Fetch a list of all threads from all thread subgroups, recursively. */
	public List<Thread> getAllThreads() {
		final ArrayList<Thread> threads = new ArrayList<Thread>();
		// From all subgroups:
		final ThreadGroup[] tgs = new ThreadGroup[activeGroupCount() * 2 + 100];
		this.enumerate(tgs, true);
		for (final ThreadGroup tg : tgs) {
			if (null == tg) continue;
			final Thread[] ts = new Thread[tg.activeCount() * 2 + 100];
			tg.enumerate(ts);
			for (final Thread t : ts) {
				if (null == t) continue;
				threads.add(t);
			}
		}
		// And from this group:
		final Thread[] ts = new Thread[activeCount() * 2 + 100];
		this.enumerate(ts);
		for (final Thread t : ts) {
			if (null == t) continue;
			threads.add(t);
		}
		return threads;
	}

	/**
	 * Totally destroy/stop all threads in this and all recursive thread
	 * subgroups. Will remove itself from the executingTasks list.
	 */
	@SuppressWarnings("deprecation")
	public void obliterate() {
		try {
			// Stop printing to the screen
			if (null != writer) writer.shutdownNow();
			if (null != errorWriter) errorWriter.shutdownNow();
		}
		catch (final Exception e) {
			Log.error(e);
		}
		for (final Thread thread : getAllThreads()) {
			try {
				thread.interrupt();
				Thread.yield(); // give it a chance
				thread.stop();
			}
			catch (final Throwable t) {
				Log.error(t);
			}
		}
		this.editorFrame.executingTasks.remove(this);
		this.editorFrame.setTitle();
	}
}
