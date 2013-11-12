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

package imagej.script;

import imagej.module.AbstractModule;
import imagej.module.Module;
import imagej.module.ModuleItem;

import java.io.File;

import javax.script.ScriptException;

import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.service.Service;

/**
 * A {@link Module} which executes a script.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class ScriptModule extends AbstractModule implements Contextual {

	private final ScriptInfo info;

	@Parameter
	private Context context;

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private LogService log;

	public ScriptModule(final ScriptInfo info) {
		this.info = info;
		setContext(info.getContext());
	}

	// -- Module methods --

	@Override
	public ScriptInfo getInfo() {
		return info;
	}

	@Override
	public void initialize() {
		for (final ModuleItem<?> item : info.inputs()) {
			final Class<?> type = item.getType();
			final Object value;
			if (Context.class.isAssignableFrom(type)) {
				value = context;
			}
			else if (Service.class.isAssignableFrom(type)) {
				@SuppressWarnings("unchecked")
				final Class<? extends Service> serviceType =
					(Class<? extends Service>) type;
				value = context.getService(serviceType);
			}
			else continue;
			final String name = item.getName();
			setInput(name, value);
			setResolved(name, true);
		}
	}

	// -- Runnable methods --

	@Override
	public void run() {
		final String path = info.getPath();
		final String fileExtension = FileUtils.getExtension(path);
		final ScriptLanguage language =
			scriptService.getByFileExtension(fileExtension);
		try {
			final Object result =
				language.getScriptEngine().eval(new FileReader(path));
			if (result != null) {
				System.out.println(result.toString());
			}
		}
		catch (final ScriptException e) {
			log.error(e.getCause());
		}
		catch (final Throwable e) {
			log.error(e);
		}
	}

	// -- Contextual methods --

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(Context context) {
		context.inject(this);
	}

}
