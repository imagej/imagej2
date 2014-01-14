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

package imagej.module;

import java.util.List;

import org.scijava.ItemIO;
import org.scijava.ItemVisibility;

/**
 * {@link ModuleItem} extension allowing manipulation of its metadata.
 * 
 * @author Curtis Rueden
 * @see imagej.command.DynamicCommand
 */
public interface MutableModuleItem<T> extends ModuleItem<T> {

	void setIOType(ItemIO ioType);

	void setVisibility(ItemVisibility visibility);

	void setRequired(boolean required);

	void setPersisted(boolean persisted);

	void setPersistKey(String persistKey);

	void setInitializer(String initializer);

	void setCallback(String callback);

	void setWidgetStyle(String widgetStyle);

	void setMinimumValue(T minimumValue);

	void setMaximumValue(T maximumValue);

	void setSoftMinimum(T softMinimum);

	void setSoftMaximum(T softMaximum);

	void setStepSize(Number stepSize);

	void setColumnCount(int columnCount);

	void setChoices(List<? extends T> choices);

}
