/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.data.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.type.numeric.NumericType;

import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * TODO
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class DefaultDataTypeService extends
	AbstractSingletonService<DataType<?>> implements DataTypeService
{

	// -- fields --

	private Map<String, DataType<?>> typesByName;
	private Map<Class<?>, DataType<?>> typesByClass;
	private List<DataType<?>> sortedInstances;

	// -- initializer --

	@Override
	public void initialize() {
		super.initialize();
		typesByName = new HashMap<String, DataType<?>>();
		typesByClass = new HashMap<Class<?>, DataType<?>>();
		for (DataType<?> type : super.getInstances()) {
			typesByName.put(type.longName(), type);
			typesByClass.put(type.getType().getClass(), type);
		}
		sortedInstances = new ArrayList<DataType<?>>();
		sortedInstances.addAll(super.getInstances());
		Collections.sort(sortedInstances, new Comparator<DataType<?>>() {

			@Override
			public int compare(DataType<?> o1, DataType<?> o2) {
				return o1.longName().compareTo(o2.longName());
			}
		});
		sortedInstances = Collections.unmodifiableList(sortedInstances);
	}

	// -- DataTypeService methods --

	@Override
	public List<DataType<?>> getInstances() {
		return sortedInstances;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class<DataType<?>> getPluginType() {
		return (Class<DataType<?>>) (Class) DataType.class;
	}

	@Override
	public DataType<?> getTypeByName(String typeName) {
		return typesByName.get(typeName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends NumericType<T>> DataType<T> getTypeByClass(
		Class<T> typeClass)
	{
		return (DataType<T>) typesByClass.get(typeClass);
	}

	@Override
	public <U extends NumericType<U>, V extends NumericType<V>> void cast(
		DataType<U> inputType, U input, DataType<V> outputType, V output)
	{
		cast(inputType, input, outputType, output, null);
	}

	@Override
	public <U extends NumericType<U>, V extends NumericType<V>> void cast(
		DataType<U> inputType, U input, DataType<V> outputType, V output,
		BigComplex tmp)
	{
		// Only do general casts when data types are unbounded or are outside
		// Double or Long precisions. Otherwise use primitives to avoid tons of
		// Object overhead.

		if (inputType.hasLongRepresentation() && outputType.hasLongRepresentation())
		{
			long val = inputType.asLong(input);
			outputType.setLong(output, val);
		}
		else if (inputType.hasDoubleRepresentation() &&
			outputType.hasDoubleRepresentation())
		{
			double val = inputType.asDouble(input);
			outputType.setDouble(output, val);
		}
		else if (inputType.hasLongRepresentation() &&
			outputType.hasDoubleRepresentation())
		{
			long val = inputType.asLong(input);
			outputType.setDouble(output, val);
		}
		else if (inputType.hasDoubleRepresentation() &&
			outputType.hasLongRepresentation())
		{
			double val = inputType.asDouble(input);
			outputType.setLong(output, (long) val);
		}

		if (tmp == null) {
			throw new IllegalArgumentException("Could not find a suitable cast. "
				+ "Pass a temporary to the alternate version of cast().");
		}

		// fall thru to simplest slowest approach: usually for complex numbers

		inputType.cast(input, tmp);
		outputType.cast(tmp, output);
	}
}
