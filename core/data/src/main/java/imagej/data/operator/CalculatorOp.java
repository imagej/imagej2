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

package imagej.data.operator;

import imagej.ImageJPlugin;
import net.imglib2.ops.operation.real.binary.RealBinaryOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.RichPlugin;
import org.scijava.plugin.SingletonPlugin;

/**
 * {@code CalculatorOp} is a plugin that extends the Image Calculator command.
 * <p>
 * Image Calculator operations discoverable at runtime must implement this
 * interface and be annotated with @{@link Plugin} with attribute
 * {@link Plugin#type()} = {@link CalculatorOp}.class. While it possible to
 * create an operation merely by implementing this interface, it is encouraged
 * to instead extend {@link AbstractCalculatorOp}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 */
public interface CalculatorOp<I1 extends RealType<I1>, I2 extends RealType<I2>>
	extends RealBinaryOperation<I1, I2, DoubleType>, ImageJPlugin, RichPlugin,
	SingletonPlugin
{
	// NB: No implementation needed.
}
