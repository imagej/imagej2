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

package imagej.module;

/**
 * {@link Module} extension allowing manipulation of its metadata.
 * <p>
 * In particular, module inputs and outputs can be added, edited and removed.
 * </p>
 * <p>
 * A {@code MutableModule} always has {@link MutableModuleInfo} attached to it,
 * accessible via the {@link #getInfo()} method.
 * </p>
 * 
 * @author Curtis Rueden
 * @see imagej.command.DynamicCommand
 */
public interface MutableModule extends Module {

	/** Adds an input to the list. */
	<T> MutableModuleItem<T> addInput(String name, Class<T> type);

	/** Adds an input to the list. */
	void addInput(ModuleItem<?> input);

	/** Adds an output to the list. */
	<T> MutableModuleItem<T> addOutput(String name, Class<T> type);

	/** Adds an output to the list. */
	void addOutput(ModuleItem<?> output);

	/** Removes an input from the list. */
	void removeInput(ModuleItem<?> input);

	/** Removes an output from the list. */
	void removeOutput(ModuleItem<?> output);

	// NB: Type narrowing.

	// -- ModuleInfo methods --

	@Override
	MutableModuleInfo getInfo();

}
