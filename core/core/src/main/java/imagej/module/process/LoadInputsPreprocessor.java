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

package imagej.module.process;

import imagej.module.Module;
import imagej.module.ModuleItem;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.util.ConversionUtils;

/**
 * A preprocessor for loading populated input values from persistent storage.
 * <p>
 * This preprocessor runs late in the chain, to give other preprocessors a
 * chance to populate the inputs first. However, its priority immediately
 * precedes the {@link imagej.widget.InputHarvester}'s, so that user-specified
 * values from last time are populated in the user dialog.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PreprocessorPlugin.class,
	priority = Priority.VERY_LOW_PRIORITY + 1)
public class LoadInputsPreprocessor extends AbstractPreprocessorPlugin {

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		final Iterable<ModuleItem<?>> inputs = module.getInfo().inputs();
		for (final ModuleItem<?> item : inputs) {
			loadValue(module, item);
		}
	}

	// -- Helper methods --

	/** Loads the value of the given module item from persistent storage. */
	private <T> void loadValue(final Module module, final ModuleItem<T> item) {
		// skip input that has already been resolved
		if (module.isResolved(item.getName())) return;

		final Class<T> type = item.getType();
		final T defaultValue = item.getValue(module);
		final T prefValue = item.loadValue();
		final T value = getBestValue(prefValue, defaultValue, type);
		item.setValue(module, value);
	}

	private <T> T getBestValue(final Object prefValue,
		final Object defaultValue, final Class<T> type)
	{
		if (prefValue != null) return ConversionUtils.convert(prefValue, type);
		if (defaultValue != null) {
			return ConversionUtils.convert(defaultValue, type);
		}
		return ConversionUtils.getNullValue(type);
	}

}
