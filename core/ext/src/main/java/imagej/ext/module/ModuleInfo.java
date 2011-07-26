//
// ModuleInfo.java
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

package imagej.ext.module;

import imagej.ext.UIDetails;
import imagej.ext.module.event.ModuleUpdatedEvent;

/**
 * A ModuleInfo object encapsulates metadata about a particular {@link Module}
 * (but not a specific instance of it). In particular, it can report details on
 * the names and types of inputs and outputs.
 * 
 * @author Aivar Grislis
 * @author Curtis Rueden
 */
public interface ModuleInfo extends UIDetails {

	/** Gets the input item with the given name. */
	ModuleItem<?> getInput(String name);

	/** Gets the output item with the given name. */
	ModuleItem<?> getOutput(String name);

	/** Gets the list of input items. */
	Iterable<ModuleItem<?>> inputs();

	/** Gets the list of output items. */
	Iterable<ModuleItem<?>> outputs();

	/**
	 * Gets the fully qualified name of the class containing the module's actual
	 * implementation. By definition, this is the same value returned by
	 * <code>createModule().getDelegateObject().getClass().getName()</code>, and
	 * hence is also the class containing any callback methods specified by
	 * {@link ModuleItem#getCallback()}.
	 * <p>
	 * The nature of this method is implementation-specific; for example, a
	 * <code>PluginModule</code> will return the class name of its associated
	 * <code>RunnablePlugin</code>. For modules that are not plugins, the result
	 * may be something else.
	 * </p>
	 * <p>
	 * If you are implementing this interface directly, a good rule of thumb is to
	 * return the class name of the associated {@link Module} (i.e., the same
	 * value given by <code>createModule().getClass().getName()</code>).
	 * </p>
	 */
	String getDelegateClassName();

	/** Instantiates the module described by this module info. */
	Module createModule() throws ModuleException;

	/**
	 * Gets whether the module supports previews. A preview is a quick
	 * approximation of the results that would be obtained by actually executing
	 * the module with {@link Module#run()}. If this method returns false, then
	 * calling {@link Module#preview()} will have no effect.
	 */
	boolean canPreview();

	/**
	 * Gets whether the module condones cancellation. Strictly speaking, any
	 * module execution can be canceled during preprocessing, but this flag is a
	 * hint that doing so may be a bad idea, and the UI may want to disallow it.
	 */
	boolean canCancel();

	/**
	 * Notifies interested parties that the module info has been modified. This
	 * mechanism is useful for updating any corresponding user interface such as
	 * menu items that are linked to the module.
	 * <p>
	 * For classes implementing this interface directly, this method should
	 * publish a {@link ModuleUpdatedEvent} to the event bus (see
	 * {@link AbstractModuleInfo#update()} for an example).
	 * </p>
	 */
	void update();

}
