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

package imagej.options;

import imagej.command.CommandInfo;
import imagej.plugin.PTService;

import java.util.List;
import java.util.Map;

/**
 * Interface for the options handling service.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @see OptionsPlugin
 */
public interface OptionsService extends PTService<OptionsPlugin> {

	/** Gets a list of all available options. */
	List<OptionsPlugin> getOptions();

	/** Gets options associated with the given options plugin, or null if none. */
	<O extends OptionsPlugin> O getOptions(Class<O> optionsClass);

	/** Gets options associated with the given options plugin, or null if none. */
	OptionsPlugin getOptions(String className);

	/** Gets the option with the given name, from the specified options plugin. */
	<O extends OptionsPlugin> Object
		getOption(Class<O> optionsClass, String name);

	/** Gets the option with the given name, from the specified options plugin. */
	Object getOption(String className, String name);

	/** Gets a map of all options from the given options plugin. */
	<O extends OptionsPlugin> Map<String, Object> getOptionsMap(
		Class<O> optionsClass);

	/** Gets a map of all options from the given options plugin. */
	Map<String, Object> getOptionsMap(String className);

	/**
	 * Sets the option with the given name, from the specified options plugin, to
	 * the given value.
	 */
	<O extends OptionsPlugin> void setOption(Class<O> optionsClass, String name,
		Object value);

	/**
	 * Sets the option with the given name, from the specified options plugin, to
	 * the given value.
	 */
	void setOption(String className, String name, Object value);

	/**
	 * Sets the option with the given name, from the specified options plugin, to
	 * the given value.
	 */
	<O extends OptionsPlugin> void setOption(CommandInfo info, String name,
		Object value);

}
