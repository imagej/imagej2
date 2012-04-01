/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.ext.module;

import imagej.ext.Accelerator;
import imagej.ext.module.process.ModulePostprocessor;
import imagej.ext.module.process.ModulePreprocessor;
import imagej.service.IService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Interface for service that tracks and executes available modules.
 * 
 * @author Curtis Rueden
 */
public interface ModuleService extends IService {

	/** Gets the index of available modules. */
	ModuleIndex getIndex();

	/** Manually registers a module with the module service. */
	void addModule(final ModuleInfo module);

	/** Manually unregisters a module with the module service. */
	void removeModule(final ModuleInfo module);

	/** Manually registers a list of modules with the module service. */
	void addModules(final Collection<? extends ModuleInfo> modules);

	/** Manually unregisters a list of modules with the module service. */
	void removeModules(final Collection<? extends ModuleInfo> modules);

	/** Gets the list of available modules. */
	List<ModuleInfo> getModules();

	/**
	 * Gets the module for a given keyboard shortcut.
	 * 
	 * @param acc the accelerator for which to search.
	 * @return the module info for the corresponding module, or null.
	 */
	ModuleInfo getModuleForAccelerator(final Accelerator acc);

	/**
	 * Executes the given module, without any pre- or postprocessing.
	 * 
	 * @param info The module to instantiate and run.
	 * @param inputValues List of input parameter values, in the same order
	 *          declared by the {@link ModuleInfo}. Passing a number of values
	 *          that differs from the number of input parameters is allowed, but
	 *          will issue a warning. Passing a value of a type incompatible with
	 *          the associated input parameter will issue an error and ignore that
	 *          value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(final ModuleInfo info, final Object... inputValues);

	/**
	 * Executes the given module.
	 * 
	 * @param info The module to instantiate and run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param inputValues List of input parameter values, in the same order
	 *          declared by the {@link ModuleInfo}. Passing a number of values
	 *          that differs from the number of input parameters is allowed, but
	 *          will issue a warning. Passing a value of a type incompatible with
	 *          the associated input parameter will issue an error and ignore that
	 *          value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(final ModuleInfo info,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final Object... inputValues);

	/**
	 * Executes the given module.
	 * 
	 * @param info The module to instantiate and run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          {@link ModuleInfo}'s input parameter names. Passing a value of a
	 *          type incompatible with the associated input parameter will issue
	 *          an error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(final ModuleInfo info,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final Map<String, Object> inputMap);

	/**
	 * Executes the given module, without any pre- or postprocessing.
	 * 
	 * @param module The module to run.
	 * @param inputValues List of input parameter values, in the same order
	 *          declared by the module's {@link ModuleInfo}. Passing a number of
	 *          values that differs from the number of input parameters is
	 *          allowed, but will issue a warning. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(final Module module, final Object... inputValues);

	/**
	 * Executes the given module.
	 * 
	 * @param module The module to run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param inputValues List of input parameter values, in the same order
	 *          declared by the module's {@link ModuleInfo}. Passing a number of
	 *          values that differs from the number of input parameters is
	 *          allowed, but will issue a warning. Passing a value of a type
	 *          incompatible with the associated input parameter will issue an
	 *          error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	Future<Module> run(final Module module,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final Object... inputValues);

	/**
	 * Executes the given module.
	 * 
	 * @param module The module to run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param inputMap Table of input parameter values, with keys matching the
	 *          module's {@link ModuleInfo}'s input parameter names. Passing a
	 *          value of a type incompatible with the associated input parameter
	 *          will issue an error and ignore that value.
	 * @return {@link Future} of the module instance being executed. Calling
	 *         {@link Future#get()} will block until execution is complete.
	 */
	<M extends Module> Future<M> run(final M module,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final Map<String, Object> inputMap);

	/** Blocks until the given module is finished executing. */
	<M extends Module> M waitFor(final Future<M> future);

	/**
	 * Checks the given module for a solitary unresolved input of the given type,
	 * returning the relevant {@link ModuleItem} if found, or null if not exactly
	 * one unresolved input of that type.
	 */
	<T> ModuleItem<T> getSingleInput(final Module module, final Class<T> type);

	/**
	 * Checks the given module for a solitary unresolved output of the given type,
	 * returning the relevant {@link ModuleItem} if found, or null if not exactly
	 * one unresolved output of that type.
	 */
	<T> ModuleItem<T> getSingleOutput(final Module module, final Class<T> type);

}
