//
// IndexItemInfo.java
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

package imagej.ext;

import imagej.util.StringMaker;

import java.net.URL;

/**
 * A collection of metadata corresponding to a particular index item (e.g.,
 * <code>imagej.ext.plugin.Plugin</code> or <code>imagej.ext.tool.Tool</code>)
 * discovered using SezPoz. This class is a shared data structure for common
 * elements of such objects.
 * 
 * @author Curtis Rueden
 */
public class IndexItemInfo<T> extends AbstractUIDetails implements
	Instantiable<T>
{

	/** Fully qualified class name of this item's object. */
	private String className;

	/** Class object for this item's object. Lazily loaded. */
	private Class<T> classObject;

	// -- IndexItemInfo methods --

	/** Sets the fully qualified name of the {@link Class} of the item objects. */
	public void setClassName(final String className) {
		this.className = className;
	}

	/**
	 * Gets the URL corresponding to the icon resource path.
	 * 
	 * @see #getIconPath()
	 */
	public URL getIconURL() throws InstantiableException {
		final String iconPath = getIconPath();
		if (iconPath == null || iconPath.isEmpty()) return null;
		return loadClass().getResource(iconPath);
	}

	// -- Object methods --

	@Override
	public String toString() {
		final String s = super.toString();
		final StringMaker sm = new StringMaker();
		sm.append("class", className);
		String result = sm.toString();
		if (!s.isEmpty()) result += ", " + s;
		return result;
	}

	// -- Instantiable methods --

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<T> loadClass() throws InstantiableException {
		if (classObject == null) {
			final Class<?> c;
			try {
				c = Class.forName(className);
			}
			catch (final ClassNotFoundException e) {
				throw new InstantiableException("Class not found: " + className, e);
			}
			classObject = (Class<T>) c;
		}
		return classObject;
	}

	@Override
	public T createInstance() throws InstantiableException {
		final Class<T> c = loadClass();

		// instantiate object
		final T instance;
		try {
			instance = c.newInstance();
		}
		catch (final InstantiationException e) {
			throw new InstantiableException(e);
		}
		catch (final IllegalAccessException e) {
			throw new InstantiableException(e);
		}
		return instance;
	}

}
