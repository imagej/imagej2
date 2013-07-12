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

package imagej.core.commands.typechange;

import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.types.BigComplex;
import imagej.data.types.DataType;
import imagej.data.types.DataTypeService;
import imagej.data.types.GeneralCast;
import imagej.menu.MenuConstants;
import imagej.module.MutableModuleItem;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * @author Barry DeZonia
 */
@Plugin(type = Command.class,
	menu = {
		@Menu(label = MenuConstants.IMAGE_LABEL,
			weight = MenuConstants.IMAGE_WEIGHT,
			mnemonic = MenuConstants.IMAGE_MNEMONIC),
		@Menu(label = "Type", mnemonic = 't'),
	@Menu(label = "Change...", mnemonic = 'c') },
	headless = true)
public class TypeChanger<U extends RealType<U>, V extends RealType<V> & NativeType<V>>
	extends DynamicCommand
{

	// TODO: expects new type to be RealType and NativeType. The unbounded types
	// defined in the data types package don't support NativeType.

	// TODO: we should be able to make NewImage also work in a similar fashion.

	// -- Parameters --

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private DataTypeService dataTypeService;

	@Parameter(label = "Type", persist = false, initializer = "init")
	private String typeName;

	@Parameter
	private Dataset data;

	// -- Command methods --

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		DataType<U> inType =
			dataTypeService.getTypeByClass(data.getImgPlus().firstElement()
				.getClass());
		DataType<V> outType = (DataType<V>) dataTypeService.getTypeByName(typeName);
		Dataset newData =
			datasetService.create(outType.createVariable(), data.getDims(),
				"Converted Image", data.getAxes());
		Cursor<U> inCursor = (Cursor<U>) data.getImgPlus().cursor();
		RandomAccess<V> outAccessor =
			(RandomAccess<V>) newData.getImgPlus().randomAccess();
		BigComplex tmp = new BigComplex();
		while (inCursor.hasNext()) {
			inCursor.fwd();
			outAccessor.setPosition(inCursor);
			cast(inType, inCursor.get(), outType, outAccessor.get(), tmp);
		}
		// TODO - copy more original metadata? Look at DuplicatorService and/or
		// TypeChanger
		data.setImgPlus(newData.getImgPlus());
	}

	// -- initializers --

	protected void init() {
		MutableModuleItem<String> input =
			getInfo().getMutableInput("typeName", String.class);
		List<String> choices = new ArrayList<String>();
		for (DataType<?> dataType : dataTypeService.getInstances()) {
			choices.add(dataType.longName());
		}
		input.setChoices(choices);
		RealType<?> dataVar = data.getImgPlus().firstElement();
		@SuppressWarnings("unchecked")
		DataType<?> type = dataTypeService.getTypeByClass(dataVar.getClass());
		if (type == null) input.setValue(this, choices.get(0));
		else input.setValue(this, type.longName());
	}

	// TODO - do all the testing outside this method once and call one of three
	// cast methods.

	private void cast(DataType<U> inputType, U i, DataType<V> outputType, V o, BigComplex tmp)
	{
		// TODO
		// only do general cast when data types are unbounded or are outside
		// Double or Long precisions. Otherwise use primitives to avoid tons of
		// Object overhead.
		boolean useLong = false;
		boolean useDouble = false;
		if (useLong) {
			// long val = inputType.asLong(i);
			// outputType.cast(val, o);
		}
		else if (useDouble) {
			// double val = inputType.asDouble(i);
			// outputType.cast(val, o);
		}
		else {
			// simplest slowest approach
			GeneralCast.cast(inputType, i, outputType, o, tmp);
		}
	}
}
