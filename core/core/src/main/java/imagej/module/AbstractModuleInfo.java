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

package imagej.module;

import imagej.Identifiable;
import imagej.ValidityProblem;
import imagej.module.event.ModulesUpdatedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.AbstractUIDetails;
import org.scijava.event.EventService;
import org.scijava.util.ConversionUtils;

/**
 * Abstract superclass of {@link ModuleInfo} implementation.
 * <p>
 * By default, {@link ModuleItem}s are stored in {@link HashMap}s and
 * {@link ArrayList}s, internally.
 * </p>
 * <p>
 * By default, {@link ModuleItem}s are stored in {@link HashMap}s and
 * {@link ArrayList}s, internally.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractModuleInfo extends AbstractUIDetails implements
	ModuleInfo, Identifiable
{

	/** Table of inputs, keyed on name. */
	protected final Map<String, ModuleItem<?>> inputMap =
		new HashMap<String, ModuleItem<?>>();

	/** Table of outputs, keyed on name. */
	protected final Map<String, ModuleItem<?>> outputMap =
		new HashMap<String, ModuleItem<?>>();

	/** Ordered list of input items. */
	protected final List<ModuleItem<?>> inputList =
		new ArrayList<ModuleItem<?>>();

	/** Ordered list of output items. */
	protected final List<ModuleItem<?>> outputList =
		new ArrayList<ModuleItem<?>>();

	// -- ModuleInfo methods --

	@Override
	public ModuleItem<?> getInput(final String name) {
		return inputMap.get(name);
	}

	@Override
	public <T> ModuleItem<T> getInput(final String name, final Class<T> type) {
		return castItem(getInput(name), type);
	}

	@Override
	public ModuleItem<?> getOutput(final String name) {
		return outputMap.get(name);
	}

	@Override
	public <T> ModuleItem<T> getOutput(final String name, final Class<T> type) {
		return castItem(getOutput(name), type);
	}

	@Override
	public Iterable<ModuleItem<?>> inputs() {
		return Collections.unmodifiableList(inputList);
	}

	@Override
	public Iterable<ModuleItem<?>> outputs() {
		return Collections.unmodifiableList(outputList);
	}

	@Override
	public boolean isInteractive() {
		return false;
	}

	@Override
	public boolean canPreview() {
		return false;
	}

	@Override
	public boolean canCancel() {
		return true;
	}

	@Override
	public boolean canRunHeadless() {
		return false;
	}

	@Override
	public String getInitializer() {
		return null;
	}

	@Override
	public void update(final EventService eventService) {
		eventService.publish(new ModulesUpdatedEvent(this));
	}

	// -- UIDetails methods --

	@Override
	public String getTitle() {
		final String title = super.getTitle();
		if (!title.equals(getClass().getSimpleName())) return title;

		// use delegate class name rather than actual class name
		final String className = getDelegateClassName();
		final int dot = className.lastIndexOf(".");
		return dot < 0 ? className : className.substring(dot + 1);
	}

	// -- Validated methods --

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public List<ValidityProblem> getProblems() {
		return null;
	}

	// -- Identifiable methods --

	@Override
	public String getIdentifier() {
		// NB: By default, we assume that the delegate class name uniquely
		// distinguishes the module from others. If the same delegate class is used
		// for more than one module, though, it may need to override this method to
		// provide more differentiating details.
		return "className = " + getDelegateClassName() + "\n";
	}

	// -- Helper methods --

	private <T> ModuleItem<T> castItem(final ModuleItem<?> item,
		final Class<T> type)
	{
		final Class<?> itemType = item.getType();
		// if (!type.isAssignableFrom(itemType)) {
		final Class<?> saneItemType = ConversionUtils.getNonprimitiveType(itemType);
		if (!ConversionUtils.canCast(type, saneItemType)) {
			throw new IllegalArgumentException("Type " + type.getName() +
				" is incompatible with item of type " + itemType.getName());
		}
		@SuppressWarnings("unchecked")
		ModuleItem<T> typedItem = (ModuleItem<T>) item;
		return typedItem;
	}

}
