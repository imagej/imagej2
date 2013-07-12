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

package imagej.io;

import java.io.IOException;

import org.scijava.plugin.HandlerService;

/**
 * Interface for data I/O operations: opening and saving data.
 * 
 * @author Curtis Rueden
 */
public interface IOService extends HandlerService<String, IOPlugin<?>> {

	/**
	 * Gets the most appropriate {@link IOPlugin} for opening data from the given
	 * source.
	 */
	IOPlugin<?> getOpener(String source);

	/**
	 * Gets the most appropriate {@link IOPlugin} for saving data to the given
	 * destination.
	 */
	<D> IOPlugin<D> getSaver(D data, String destination);

	/**
	 * Loads data from the given source. For extensibility, the nature of the
	 * source is left intentionally general, but two common examples include file
	 * paths and URLs.
	 * <p>
	 * The opener to use is automatically determined based on available
	 * {@link IOPlugin}s; see {@link #getOpener(String)}.
	 * </p>
	 * 
	 * @param source The source (e.g., file path) from which to data should be
	 *          loaded.
	 * @return An object representing the loaded data, or null if the source is
	 *         not supported.
	 * @throws IOException if something goes wrong loading the data.
	 */
	Object open(String source) throws IOException;

	/**
	 * Saves data to the given destination. The nature of the destination is left
	 * intentionally general, but the most common example is a file path.
	 * <p>
	 * The saver to use is automatically determined based on available
	 * {@link IOPlugin}s; see {@link #getSaver(Object, String)}.
	 * </p>
	 * 
	 * @param data The data to be saved to the destination.
	 * @param destination The destination (e.g., file path) to which data should
	 *          be saved.
	 * @throws IOException if something goes wrong saving the data.
	 */
	void save(Object data, String destination) throws IOException;

}
