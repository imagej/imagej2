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

package imagej.ui;

import java.lang.reflect.InvocationTargetException;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;
import org.scijava.widget.AbstractInputWidget;
import org.scijava.widget.InputWidget;

/**
 * Abstract superclass of {@link InputWidget} implementations that
 * belong to a specific UI.
 * <p>
 * {@link #refreshWidget()} implementation determines if this widget
 * needs to be refreshed on the EDT, and invokes it as needed if so.
 * </p>
 * <p>
 * Subclasses should implement {@link #doRefresh()} as they would
 * have {@link #refreshWidget()}. A common abstract superclass 
 * for each UserInterface type should be sufficient for 
 * {@link #ui()}, which can delegate to {@link #ui(String)} with
 * the appropriate UI name.
 * </p>
 * 
 * @author Mark Hiner
 * 
 */
public abstract class AbstractUIInputWidget<T, W> extends AbstractInputWidget<T, W> {

	// -- Fields --

	@Parameter
	private ThreadService threadService;

	@Parameter
	private UIService uiService;

	@Parameter
	private LogService log;

	// -- InputWidget methods --
	
	@Override
	public void refreshWidget() {

	  // If this widget requires the EDT, ensure its refresh action takes place
	  // on the EDT.
		if (ui().requiresEDT()) {
			try {
				threadService.invoke(new Runnable() {
					@Override
					public void run() {
						doRefresh();
					}
				});
			}
			catch (InterruptedException e) {
				log.error("Interrupted while refresh widget: " + getClass(), e);
			}
			catch (InvocationTargetException e) {
				log.error("Failed to refresh widget: " + getClass() + " on EDT", e);
			}
		}
		else {
			doRefresh();
		}
	}

	// -- AbstractUIInputWidget methods --
	
	/**
	 * Performs the intended {@link #refreshWidget()} operation.
	 */
	protected abstract void doRefresh();
	
	/**
	 * @return The {@link UserInterface} instance associated
	 * with this InputWidget.
	 */
	protected abstract UserInterface ui();
	
	/**
	 * @return the UserInterface matching the provided String
	 */
	protected UserInterface ui(String uiName) {
		
		return uiService.getUI(uiName);
	}
} 
