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

package imagej.data.operator;

import imagej.plugin.AbstractSingletonService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.img.ImageCombiner;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default service for managing available {@link CalculatorOp}s.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class DefaultCalculatorService extends
	AbstractSingletonService<CalculatorOp<?, ?>> implements CalculatorService
{

	// -- instance variables --

	private Map<String, CalculatorOp<?, ?>> operators;
	private List<String> operatorNames;

	// -- CalculatorService methods --

	@Override
	public Map<String, CalculatorOp<?, ?>> getOperators() {
		return Collections.unmodifiableMap(operators());
	}

	@Override
	public List<String> getOperatorNames() {
		return Collections.unmodifiableList(operatorNames());
	}

	@Override
	public CalculatorOp<?, ?> getOperator(final String operatorName) {
		return operators().get(operatorName);
	}

	@Override
	public <U extends RealType<U>, V extends RealType<V>> Img<DoubleType>
		combine(final Img<U> img1, final Img<V> img2, final CalculatorOp<U, V> op)
	{
		// TODO - limited by ArrayImg size constraints
		return ImageCombiner.applyOp(op, img1, img2,
			new ArrayImgFactory<DoubleType>(), new DoubleType());
	}

	// -- PTService methods --

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Class<CalculatorOp<?, ?>> getPluginType() {
		return (Class) CalculatorOp.class;
	}

	// -- helpers --

	private void buildDataStructures() {
		operators = new HashMap<String, CalculatorOp<?, ?>>();
		operatorNames = new ArrayList<String>();
		for (final CalculatorOp<?, ?> op : getInstances()) {
			final String name = op.getInfo().getName();
			operators.put(name, op);
			operatorNames.add(name);
		}
	}

	private Map<String, CalculatorOp<?, ?>> operators() {
		if (operators == null) buildDataStructures();
		return operators;
	}

	private List<? extends String> operatorNames() {
		if (operatorNames == null) buildDataStructures();
		return operatorNames;
	}

}
