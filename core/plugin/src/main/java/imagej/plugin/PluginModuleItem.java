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

import imagej.module.ModuleItem;

import java.lang.reflect.Field;

/**
 * {@link ModuleItem} class for querying metadata of a {@link IPlugin}'s
 * inputs and outputs.
 * 
 * @author Curtis Rueden
 */
public class PluginModuleItem implements ModuleItem {

	private final Field field;
	private final Object defaultValue;

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
	public String getDescription() {
		return getParameter().description();
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
