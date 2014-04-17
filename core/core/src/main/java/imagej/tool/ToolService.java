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

package imagej.tool;

import imagej.service.ImageJService;

import java.util.List;

import org.scijava.plugin.SingletonService;
import org.scijava.util.RealCoords;

/**
 * Interface for service that tracks available tools.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public interface ToolService extends SingletonService<Tool>, ImageJService {

	Tool getTool(String name);

	/**
	 * Get a tool given its class.
	 * 
	 * @param <T> the tool's type
	 * @param toolClass the class of the tool to fetch
	 * @return the tool, or null if no such tool
	 */
	<T extends Tool> T getTool(Class<T> toolClass);

	List<Tool> getTools();

	List<Tool> getAlwaysActiveTools();

	Tool getActiveTool();

	void setActiveTool(Tool activeTool);

	/**
	 * Returns true if the two specified tools should have a separator between
	 * them on the tool bar.
	 */
	boolean isSeparatorNeeded(Tool tool1, Tool tool2);

	/** Publishes rectangle dimensions in the status bar. */
	void reportRectangle(final double x, final double y, final double w,
		final double h);

	/** Publishes rectangle dimensions in the status bar. */
	void reportRectangle(final RealCoords p1, final RealCoords p2);

	/** Publishes line length and angle in the status bar. */
	void reportLine(final double x1, final double y1, final double x2,
		final double y2);

	/** Publishes line length and angle in the status bar. */
	void reportLine(final RealCoords p1, final RealCoords p2);

	/** Publishes point coordinates to the status bar. */
	void reportPoint(final double x, final double y);

	/** Publishes point coordinates to the status bar. */
	void reportPoint(final RealCoords p);

}
