/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.scripting.clojure;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

/**
 * A {@link Bindings} wrapper around Clojure's local variables.
 * 
 * @author Johannes Schindelin
 */
public class ClojureBindings implements Bindings {

	public ClojureBindings() {
		final Var nameSpace = RT.var("clojure.core", "*ns*");
		Var.pushThreadBindings(RT.map(nameSpace, nameSpace.get()));
		RT.var("clojure.core", "in-ns").invoke(Symbol.intern("user"));
		RT.var("clojure.core", "refer").invoke(Symbol.intern("clojure.core"));
	}

	@Override
	public int size() {
		return Var.getThreadBindings().count();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(final Object key) {
		return get(key) != null;
	}

	@Override
	public boolean containsValue(final Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(final Object keyObject) {
		String key = (String) keyObject;
		final int dot = key.lastIndexOf('.');
		final String nameSpace;
		if (dot < 0) {
			nameSpace = "user";
		}
		else {
			nameSpace = key.substring(0, dot);
			key = key.substring(dot + 1);
		}
		try {
			return RT.var(nameSpace, key).get();
		}
		catch (final Error e) {
			return null;
		}
	}

	private Object get(final String nameSpace, final String key) {
		return RT.var(nameSpace, key);
	}

	@Override
	public Object put(final String name, final Object value) {
		final int dot = name.lastIndexOf('.');
		final String nameSpace, key;
		if (dot < 0) {
			nameSpace = "user";
			key = name;
		}
		else {
			nameSpace = name.substring(0, dot);
			key = name.substring(dot + 1);
		}
		final Object result = get(nameSpace, key);
		try {
			final Var var = RT.var(nameSpace, key, null);
			var.setDynamic();
			Var.pushThreadBindings(RT.map(var, value));
		}
		catch (final Error e) {
			// ignore
		}
		return result;
	}

	@Override
	public Object remove(final Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(final Map<? extends String, ? extends Object> toMerge) {
		for (final Entry<? extends String, ? extends Object> entry : toMerge
			.entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Object> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		throw new UnsupportedOperationException();
	}

}
