//
// MethodRef.java
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

import imagej.util.ClassUtils;
import imagej.util.Log;

import java.lang.reflect.Method;

/**
 * A reference to a {@link Method}, which can be invoked at will.
 * 
 * @author Curtis Rueden
 */
public class MethodRef {

	private final Method method;
	private final String label;

	public MethodRef(final String className, final String methodName,
		final Class<?>... params)
	{
		method = findMethod(className, methodName, params);
		label = method == null ? null : makeLabel(className, methodName);
	}

	public void execute(final Object obj, final Object... args) {
		if (method == null) return;
		Log.debug("Executing method: " + label);
		try {
			method.invoke(obj, args);
		}
		catch (final Exception e) {
			// NB: Several types of exceptions; simpler to handle them all the same.
			Log.warn("Error executing method: " + label, e);
		}
	}

	private Method findMethod(final String className, final String methodName,
		final Class<?>... params)
	{
		if (methodName == null || methodName.isEmpty()) return null;
		final Class<?> c = ClassUtils.loadClass(className);
		if (c == null) return null;
		try {
			// TODO - support inherited methods
			final Method m = c.getDeclaredMethod(methodName, params);
			m.setAccessible(true);
			return m;
		}
		catch (final Exception e) {
			// NB: Multiple types of exceptions; simpler to handle them all the same.
			Log.warn("Cannot find method: " + makeLabel(c.getName(), methodName), e);
		}
		return null;
	}

	private String makeLabel(final String className, final String methodName) {
		return className + "#" + methodName;
	}
}
