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

package imagej.plugin;

/**
 * Interface for plugins which "handle" a particular subset of data objects. A
 * handler plugin is a {@link SingletonPlugin} associated with a specific data
 * type (i.e., implementing {@link TypedPlugin}).
 * <p>
 * For a given data object (of type {@code D}), the {@code HandlerPlugin}
 * declares whether it can handle that data object via the {@link #supports}
 * method. The plugin's associated {@link HandlerService#getHandler} method then
 * uses this capability to determine the most appropriate handler for any given
 * data object.
 * </p>
 * <p>
 * Note that there is no single {@code handle(D)} method for actually handling
 * data objects, because it would be rather inflexible; e.g., handlers may have
 * other required inputs, or may provide more than one possible avenue of
 * handling (i.e., more than one "handle"-style method).
 * </p>
 * 
 * @author Curtis Rueden
 * @param <D> Data type associated with the plugin.
 * @see HandlerService
 */
public interface HandlerPlugin<D> extends SingletonPlugin, TypedPlugin<D> {
	// NB: Marker interface.
}
