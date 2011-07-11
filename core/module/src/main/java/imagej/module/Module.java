//
// Module.java
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

import java.util.Map;

/**
 * A module is an encapsulated piece of functionality with inputs and outputs.
 * <p>
 * There are several types of modules, including plugins and scripts, as well as
 * workflows, which are directed acyclic graphs consisting of modules whose
 * inputs and outputs are connected.
 * </p>
 * <p>
 * The Module interface represents a specific instance of a module, while the
 * corresponding {@link ModuleInfo} represents metadata about that module,
 * particularly its input and output names and types.
 * </p>
 * 
 * @author Aivar Grislis
 * @author Curtis Rueden
 */
public interface Module {

	/** Executes the module. */
	void run();

	/** Gets metadata about this module. */
	ModuleInfo getInfo();

	/** Gets the value of the input with the given name. */
	Object getInput(String name);

	/** Gets the value of the output with the given name. */
	Object getOutput(String name);

	/** Gets a table of input values. */
	Map<String, Object> getInputs();

	/** Gets a table of output values. */
	Map<String, Object> getOutputs();

	/** Sets the value of the input with the given name. */
	void setInput(String name, Object value);

	/** Sets the value of the output with the given name. */
	void setOutput(String name, Object value);

	/** Sets input values according to the given table. */
	void setInputs(Map<String, Object> inputs);

	/** Sets output values according to the given table. */
	void setOutputs(Map<String, Object> outputs);

	/**
	 * Gets the resolution status of the input with the given name. A "resolved"
	 * input is known to have a final, valid value for use with the module.
	 */
	boolean isResolved(String name);

	/**
	 * Sets the resolution status of the input with the given name. A "resolved"
	 * input is known to have a final, valid value for use with the module.
	 */
	void setResolved(String name, boolean resolved);

}
