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

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * CalculatorService discovers {@link CalculatorOp}s known to the system and
 * allows one to combine {@link Img}s using these operators.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class CalculatorService extends AbstractService {
	
	// -- Parameters --

	@Parameter
	private LogService log;

	@Parameter
	private PluginService pluginService;

	// -- instance variables --

	private Map<String, CalculatorOp<?, ?>> operators;
	private List<String> operatorNames;

	// -- service initializer --

	@Override
	public void initialize() {
		operators = new HashMap<String, CalculatorOp<?, ?>>();
		operatorNames = new ArrayList<String>();
		findOperators();
	}

	// -- OperatorService methods --

	/**
	 * Returns the collection of {@link CalculatorOp}s known to the system. This
	 * collection is a {@link Map} from operator names to operators.
	 */
	public Map<String, CalculatorOp<?, ?>> getOperators() {
		return Collections.unmodifiableMap(operators);
	}

	/**
	 * Returns the collection of {@link CalculatorOp}s known to the system. This
	 * collection is a {@link List} of operator names.
	 */
	public List<String> getOperatorNames() {
		return Collections.unmodifiableList(operatorNames);
	}

	/**
	 * Returns the {@link CalculatorOp} associated with the given name.
	 */
	public CalculatorOp<?, ?> getOperator(String operatorName) {
		return operators.get(operatorName);
	}

	/**
	 * Creates an {@link Img}<DoubleType> from the combination of two input
	 * {@link Img}s. The size of the output {@link Img} matches the region of
	 * overlap between the two input {@link Img}s. The combination is done pixel
	 * by pixel using a given {@link CalculatorOp}.
	 * 
	 * @param img1 data input Img 1
	 * @param img2 data input Img 2
	 * @param op The CalculatorOp algorithm used to combine the two inputs
	 * @return An Img<DoubleType> containing the combined data of the overlapping
	 *         regions of the two input Imgs.
	 */
	public <U extends RealType<U>, V extends RealType<V>> Img<DoubleType>
		combine(Img<U> img1, Img<V> img2, CalculatorOp<U, V> op)
	{
		// TODO - limited by ArrayImg size constraints
		return ImageCombiner.applyOp(op, img1, img2,
			new ArrayImgFactory<DoubleType>(), new DoubleType());
	}

	// -- helpers --

	@SuppressWarnings("rawtypes")
	private void findOperators() {
		final List<PluginInfo<CalculatorOp>> pluginInfos =
			pluginService.getPluginsOfType(CalculatorOp.class);
		for (final PluginInfo<CalculatorOp> info : pluginInfos) {
			final String name = info.getName();
			final CalculatorOp<?, ?> op = pluginService.createInstance(info);
			if (op == null) continue;
			operators.put(name, op);
			operatorNames.add(name);
		}
		Collections.sort(operatorNames);
	}

}
