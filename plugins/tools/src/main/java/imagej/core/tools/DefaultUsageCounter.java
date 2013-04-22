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

package imagej.core.tools;

import imagej.tool.AbstractTool;
import imagej.tool.ToolService;
import imagej.ui.ToolBar;
import imagej.ui.UIService;

/**
 * Hides and shows tool according to usage count.  Mix-in class for tools.
 * 
 * @author Aivar Grislis
 */
public class DefaultUsageCounter implements UsageCounter {
	private final AbstractTool tool;
	private int counter;
	
	public DefaultUsageCounter(AbstractTool tool) {
		this.tool = tool;
		
		// initially hidden
		tool.setHidden(true);
		counter = 0;
	}

	@Override
	public synchronized void show() {
		++counter;
		if (1 == counter) {
			// first user
			setHidden(false);
		}
	}

	@Override
	public synchronized void hide() {
		--counter;
		if (0 == counter) {
			// no more users
			setHidden(true);
		}
	}
	
	private void setHidden(boolean hidden) {
		// set tool hidden state
		final ToolService toolService =
				tool.getContext().getService(ToolService.class);
		toolService.setHiddenTool(tool, hidden);
		
		// refresh toolbar
		final UIService uiService =
				tool.getContext().getService(UIService.class);
		final ToolBar toolBar = uiService.getDefaultUI().getToolBar();
		assert null != toolBar;
		toolBar.refresh();
	}
}
