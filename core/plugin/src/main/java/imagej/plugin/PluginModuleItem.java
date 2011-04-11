//
// PluginModuleItem.java
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

package imagej.plugin;

import java.lang.reflect.Field;

import imagej.module.ModuleItem;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public class PluginModuleItem implements ModuleItem {

	private Field field;
	private Object defaultValue;
	private boolean resolved;

	public PluginModuleItem(final Field field, final Object defaultValue) {
		this.field = field;
		this.defaultValue = defaultValue;
	}

	public Field getField() {
		return field;
	}

	public Parameter getParameter() {
		return field.getAnnotation(Parameter.class);
	}

	/**
	 * Sets the item's resolution status. A "resolved" item is known to have a
	 * final, valid value for use with the module.
	 */
	public void setResolved(final boolean resolved) {
		this.resolved = resolved;
	}

	/**
	 * Gets the item's resolution status. A "resolved" item is known to have a
	 * final, valid value for use with the module.
	 */
	public boolean isResolved() {
		return resolved;
	}

	// -- ModuleItem methods --

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public String getLabel() {
		return getParameter().label();
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

}
