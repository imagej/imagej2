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

package imagej.data.autoscale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default service for working with autoscale methods.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@SuppressWarnings("rawtypes")
@Plugin(type = Service.class)
public class DefaultAutoscaleService extends
	AbstractSingletonService<AutoscaleMethod> implements AutoscaleService
{

	// -- instance variables --

	private HashMap<String, AutoscaleMethod> methods;

	private ArrayList<String> methodNames;

	// -- AutoscaleService methods --

	@Override
	public Map<String, AutoscaleMethod> getAutoscaleMethods() {
		return Collections.unmodifiableMap(methods());
	}

	@Override
	public List<String> getAutoscaleMethodNames() {
		return Collections.unmodifiableList(methodNames());
	}

	@Override
	public AutoscaleMethod getAutoscaleMethod(final String name) {
		return methods().get(name);
	}

	@Override
	public AutoscaleMethod getDefaultAutoscaleMethod() {
		return getAutoscaleMethod("Default");
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataRange getDefaultIntervalRange(
		final IterableInterval<? extends RealType<?>> interval)
	{
		return getDefaultAutoscaleMethod().getRange(interval);
	}

	@Override
	public DataRange getDefaultRandomAccessRange(
		final RandomAccessibleInterval<? extends RealType<?>> interval)
	{
		final IterableInterval<? extends RealType<?>> newInterval =
			Views.iterable(interval);
		return getDefaultIntervalRange(newInterval);
	}

	// -- PTService methods --

	@Override
	public Class<AutoscaleMethod> getPluginType() {
		return AutoscaleMethod.class;
	}

	// -- Helper methods - lazy initialization --

	/** Gets {@link #methods}, initializing if needed. */
	private Map<? extends String, ? extends AutoscaleMethod> methods() {
		if (methods == null) initMethods();
		return methods;
	}

	/** Gets {@link #methodNames}, initializing if needed. */
	private List<? extends String> methodNames() {
		if (methodNames == null) initMethodNames();
		return methodNames;
	}

	/** Initializes {@link #methods}. */
	private synchronized void initMethods() {
		if (methods != null) return; // already initialized

		final HashMap<String, AutoscaleMethod> map =
			new HashMap<String, AutoscaleMethod>();
		for (final AutoscaleMethod method : getInstances()) {
			final String name = method.getInfo().getName();
			map.put(name, method);
		}

		methods = map;
	}

	/** Initializes {@link #methodNames}. */
	private synchronized void initMethodNames() {
		if (methodNames != null) return; // already initialized

		final ArrayList<String> list = new ArrayList<String>();
		for (final AutoscaleMethod method : getInstances()) {
			final String name = method.getInfo().getName();
			list.add(name);
		}

		methodNames = list;
	}

}
