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

package imagej.thread;

import imagej.service.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Interface for the thread handling service.
 * 
 * @author Curtis Rueden
 */
public interface ThreadService extends Service, ThreadFactory {

	/**
	 * Asynchronously executes the given code in a new thread, as decided by the
	 * thread service. Typically this means that the service allocates a thread
	 * from its pool, but ultimately the behavior is implementation-dependent.
	 * This method returns immediately.
	 * 
	 * @param code The code to execute.
	 * @return A {@link Future} that will contain the result once the execution
	 *         has finished. Call {@link Future#get()} to access to the return
	 *         value (which will block until execution has completed).
	 */
	<V> Future<V> run(Callable<V> code);

	/**
	 * Asynchronously executes the given code in a new thread, as decided by the
	 * thread service. Typically this means that the service allocates a thread
	 * from its pool, but ultimately the behavior is implementation-dependent.
	 * This method returns immediately.
	 * 
	 * @param code The code to execute.
	 * @return A {@link Future} that can be used to block until the execution has
	 *         finished. Call {@link Future#get()} to do so.
	 */
	Future<?> run(Runnable code);

	/**
	 * Gets whether the current thread is a dispatch thread for use with
	 * {@link #invoke} and {@link #queue}.
	 * <p>
	 * In the case of AWT-based applications (e.g., Java on the desktop), this is
	 * typically the AWT Event Dispatch Thread (EDT). Howveer, ultimately the
	 * behavior is implementation-dependent.
	 * </p>
	 * 
	 * @return True iff the current thread is considered a dispatch thread.
	 */
	boolean isDispatchThread();

	/**
	 * Executes the given code in a special dispatch thread, blocking until
	 * execution is complete.
	 * <p>
	 * In the case of AWT-based applications (e.g., Java on the desktop), this is
	 * typically the AWT Event Dispatch Thread (EDT). Howveer, ultimately the
	 * behavior is implementation-dependent.
	 * </p>
	 * 
	 * @param code The code to execute.
	 * @throws InterruptedException If the code execution is interrupted.
	 * @throws InvocationTargetException If an uncaught exception occurs in the
	 *           code during execution.
	 */
	void invoke(final Runnable code) throws InterruptedException,
		InvocationTargetException;

	/**
	 * Queues the given code for later execution in a special dispatch thread.
	 * <p>
	 * In the case of AWT-based applications (e.g., Java on the desktop), this is
	 * typically the AWT Event Dispatch Thread (EDT). Howveer, ultimately the
	 * behavior is implementation-dependent.
	 * </p>
	 * 
	 * @param code The code to execute.
	 */
	void queue(final Runnable code);

}
