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

package imagej.ui.swt.widget;

import imagej.ui.AbstractUIInputWidget;
import imagej.ui.UserInterface;
import imagej.ui.swt.SWTUI;
import imagej.widget.WidgetModel;

import org.eclipse.swt.widgets.Composite;

/**
 * Common superclass for SWT-based input widgets.
 * 
 * @author Curtis Rueden
 */
public abstract class SWTInputWidget<T> extends
	AbstractUIInputWidget<T, Composite>
{

	private Composite uiComponent;
	private Composite parent;

	public void setParent(final Composite parent) {
		// CTR FIXME: Temporary hack to inject parent component.
		this.parent = parent;
	}

	public Composite getParent() {
		return parent;
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);
		uiComponent = new Composite(parent, 0);
	}

	// -- UIComponent methods --

	@Override
	public Composite getComponent() {
		return uiComponent;
	}

	@Override
	public Class<Composite> getComponentType() {
		return Composite.class;
	}

	// -- AbstractUIInputWidget methods --

	@Override
	protected UserInterface ui() {
		return ui(SWTUI.NAME);
	}

}
