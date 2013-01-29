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

package imagej.command;

import imagej.InstantiableException;
import imagej.module.Module;
import imagej.module.ModuleException;

/**
 * The default implementation of {@link CommandModuleFactory}, using a
 * {@link CommandModule}.
 * 
 * @author Curtis Rueden
 */
public class DefaultCommandModuleFactory implements CommandModuleFactory {

	@Override
	public Module createModule(final CommandInfo info)
		throws ModuleException
	{
		// if the command implements Module, return a new instance directly
		try {
			final Class<?> commandClass = info.loadClass();
			if (Module.class.isAssignableFrom(commandClass)) {
				return (Module) commandClass.newInstance();
			}
		}
		catch (final InstantiableException e) {
			throw new ModuleException(e);
		}
		catch (final InstantiationException e) {
			throw new ModuleException(e);
		}
		catch (final IllegalAccessException e) {
			throw new ModuleException(e);
		}

		// command does not implement Module; wrap it in a CommandModule instance
		return new CommandModule(info);
	}

	@Override
	public Module createModule(final CommandInfo info, final Command command) {
		// if the command implements Module, return the instance directly
		if (command instanceof Module) {
			return (Module) command;
		}

		// command does not implement Module; wrap it in a CommandModule instance
		return new CommandModule(info, command);
	}

}
