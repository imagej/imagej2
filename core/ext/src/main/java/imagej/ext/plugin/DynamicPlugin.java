//
// DynamicPlugin.java
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

package imagej.ext.plugin;

import imagej.ext.module.AbstractModule;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.module.ModuleItem;

/**
 * A class which can be extended to provide an ImageJ plugin with a variable
 * number of inputs and outputs. This class provides greater configurability,
 * but also greater complexity, than implementing the {@link ImageJPlugin}
 * interface and using the @ {@link Parameter} annotations on instance fields.
 * 
 * @author Curtis Rueden
 */
public abstract class DynamicPlugin extends AbstractModule implements
	ImageJPlugin
{

	private final DynamicPluginInfo info;

	public DynamicPlugin() {
		super(new DynamicPluginInfo());
		info = (DynamicPluginInfo) super.getInfo();
		info.setModule(this);
	}

	// -- DynamicPlugin methods --

	/** Adds an input to the list. */
	public <T> void addInput(final String name, final Class<T> type) {
		addInput(new DefaultModuleItem<T>(this, name, type));
	}

	/** Adds an input to the list. */
	public void addInput(final ModuleItem<?> input) {
		getInfo().addInput(input);
	}

	/** Adds an output to the list. */
	public <T> void addOutput(final String name, final Class<T> type) {
		addOutput(new DefaultModuleItem<T>(this, name, type));
	}

	/** Adds an output to the list. */
	public void addOutput(final ModuleItem<?> output) {
		getInfo().addOutput(output);
	}

	/** Removes an input from the list. */
	public void removeInput(final ModuleItem<?> input) {
		getInfo().removeInput(input);
	}

	/** Removes an output from the list. */
	public void removeOutput(final ModuleItem<?> output) {
		getInfo().removeOutput(output);
	}

	// -- Module methods --

	@Override
	public DynamicPluginInfo getInfo() {
		return info;
	}

}
