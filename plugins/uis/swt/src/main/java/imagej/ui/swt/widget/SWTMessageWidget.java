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

import imagej.widget.InputWidget;
import imagej.widget.MessageWidget;
import imagej.widget.WidgetModel;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 * SWT implementation of message widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class, priority = Priority.HIGH_PRIORITY)
public class SWTMessageWidget extends SWTInputWidget<String> implements
	MessageWidget<Composite>
{

	// -- InputWidget methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isMessage();
	}

	@Override
	public void initialize(final WidgetModel model) {
		super.initialize(model);

		final String text = model.getText();

		final Label label = new Label(getComponent(), 0);
		label.setText(text);
		label.setLayoutData("span");
	}

	@Override
	public String getValue() {
		return null;
	}

	@Override
	public void refreshWidget() {
		// NB: No action needed.
	}

	@Override
	public boolean isLabeled() {
		return false;
	}

	@Override
	public boolean isMessage() {
		return true;
	}

}
