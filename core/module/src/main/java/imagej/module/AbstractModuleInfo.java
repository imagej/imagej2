//
// AbstractModuleInfo.java
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

package imagej.module;

import imagej.event.Events;
import imagej.module.event.ModuleInfoUpdatedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract superclass of {@link ModuleInfo} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractModuleInfo extends AbstractUIDetails implements
	ModuleInfo
{

	/** Table of inputs, keyed on name. */
	private final Map<String, ModuleItem<?>> inputMap =
		new HashMap<String, ModuleItem<?>>();

	/** Table of outputs, keyed on name. */
	private final Map<String, ModuleItem<?>> outputMap =
		new HashMap<String, ModuleItem<?>>();

	/** Ordered list of input items. */
	private final List<ModuleItem<?>> inputList = new ArrayList<ModuleItem<?>>();

	/** Ordered list of output items. */
	private final List<ModuleItem<?>> outputList =
		new ArrayList<ModuleItem<?>>();

	// -- ModuleInfo methods --

	@Override
	public ModuleItem<?> getInput(final String name) {
		return inputMap.get(name);
	}

	@Override
	public ModuleItem<?> getOutput(final String name) {
		return outputMap.get(name);
	}

	@Override
	public Iterable<ModuleItem<?>> inputs() {
		return Collections.unmodifiableList(inputList);
	}

	@Override
	public Iterable<ModuleItem<?>> outputs() {
		return Collections.unmodifiableList(outputList);
	}

	@Override
	public boolean canPreview() {
		return false;
	}

	@Override
	public void update() {
		Events.publish(new ModuleInfoUpdatedEvent(this));
	}

	// -- Internal AbstractModuleInfo methods --

	protected void addInput(final ModuleItem<?> input) {
		inputMap.put(input.getName(), input);
		inputList.add(input);
	}

	protected void addOutput(final ModuleItem<?> output) {
		outputMap.put(output.getName(), output);
		outputList.add(output);
	}

}
