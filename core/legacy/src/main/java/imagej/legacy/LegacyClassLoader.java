/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.legacy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * Utilities to work with the LegacyService associated with the current thread.
 *
 * @author Johannes Schindelin
 */
public class LegacyClassLoader extends ClassLoader {
	private final ClassPool pool;
	private final Map<String, Class<?>> classes;

	public LegacyClassLoader(final ClassLoader parent) {
		super(parent);
		pool = new ClassPool(ClassPool.getDefault());
		pool.appendClassPath(new LoaderClassPath(this));
		classes = new HashMap<String, Class<?>>();
		Thread.currentThread().setContextClassLoader(this);
	}

	public synchronized Class<?> forceLoadClass(final String name) throws ClassNotFoundException {
		try {
			return forceLoadClass(pool.get(name));
		} catch (NotFoundException e) {
			throw new ClassNotFoundException(name, e);
		}
	}

	public synchronized Class<?> forceLoadClass(final CtClass clazz) throws ClassNotFoundException {
		try {
			Class<?> result = clazz.toClass(this, null);
			classes.put(result.getName(), result);
			return result;
		} catch (CannotCompileException e) {
			throw new ClassNotFoundException(clazz.getName(), e);
		}
	}

	@Override
	public synchronized Class<?> loadClass(final String name) throws ClassNotFoundException {
		Class<?> result = classes.get(name);
		if (result != null) {
			return result;
		}

		if (name.startsWith("java.") || name.equals("imagej.legacy.LegacyService") || name.equals("imagej.legacy.LegacyClassLoader")) {
			result = super.loadClass(name);
			classes.put(name, result);
			return result;
		}

		if (name.startsWith("ij.") || name.startsWith("imagej.legacy.")) {
			return forceLoadClass(name);
		}

		try {
			CtClass clazz = pool.get(name);
			if (hasCommittedReferences(clazz)) {
				return forceLoadClass(clazz);
			}
		} catch (NotFoundException e) {
			throw new ClassNotFoundException(name, e);
		}

		result = super.loadClass(name);
		classes.put(name,  result);
		return result;
	}

	private boolean hasCommittedReferences(CtClass ctclass) {
		return hasCommittedReferences(ctclass, new HashSet<String>());
	}

	private boolean hasCommittedReferences(CtClass ctclass, Set<String> done) {
		for (Object item : ctclass.getRefClasses()) {
			String name = (String)item;
			if (name.startsWith("java.") || done.contains(name)) continue;
			done.add(name);
			Class<?> clazz = classes.get(name);
			if (clazz == null) try {
				CtClass ctclass2 = pool.get(name);
				if (hasCommittedReferences(ctclass2, done)) {
					forceLoadClass(ctclass2);
					return true;
				}
				classes.put(name, super.loadClass(name));
				continue;
			} catch (NotFoundException e) {
				// ignore for now
				continue;
			} catch (ClassNotFoundException e) {
				// ignore for now
				continue;
			}
			if (clazz.getClassLoader() == this) return true;
		}
		return false;
	}

}
