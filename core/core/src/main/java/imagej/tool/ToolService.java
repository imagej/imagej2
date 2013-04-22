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

package imagej.tool;

import imagej.util.RealCoords;

import java.util.List;

import org.scijava.app.StatusService;
import org.scijava.event.EventService;
import org.scijava.plugin.PluginService;
import org.scijava.service.Service;

/**
 * Interface for service that tracks available tools.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public interface ToolService extends Service {

	/**
	 * Get event service.
	 * 
	 * @return event service
	 */
	EventService getEventService();

	/**
	 * Get status service.
	 *
	 * @return status service
	 */
	StatusService getStatusService();

	/**
	 * Get plugin service.
	 * 
	 * @return plugin service
	 */
	PluginService getPluginService();

	/**
	 * Get a tool given its name.
	 * 
	 * @param name
	 * @return the tool, or null if no such tool
	 */
	Tool getTool(String name);

	/**
	 * Get a tool given its class.
	 * 
	 * @param <T> the tool's type
	 * @param toolClass the class of the tool to fetch
	 * @return the tool, or null if no such tool
	 */
	<T extends Tool> T getTool(Class<T> toolClass);

	/**
	 * Gets list of all (non-hidden) tools.
	 * 
	 * @return list of tools
	 */
	List<Tool> getTools();

	/**
	 * Gets list of tools that don't have to be selected to be active.
	 * 
	 * @return 
	 */
	List<Tool> getAlwaysActiveTools();

	/**
	 * Gets current selected tool.
	 * 
	 * @return 
	 */
	Tool getActiveTool();

	/**
	 * Sets current selected tool.
	 * 
	 * @param activeTool
	 */
	void setActiveTool(Tool activeTool);

	/**
	 * Hides and shows tools.
	 * 
	 * @param tool
	 * @param hide 
	 */
	void setHiddenTool(Tool tool, boolean hide);

	/**
	 * Returns true if the two specified tools should have a separator between
	 * them on the tool bar.
	 * 
	 * @param tool1
	 * @param tool2
	 * @return whether need separator
	 */
	boolean isSeparatorNeeded(Tool tool1, Tool tool2);

	/**
	 * Publishes rectangle dimensions in the status bar.
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h 
	 */
	void reportRectangle(final double x, final double y, final double w,
		final double h);

	/**
	 * Publishes rectangle dimensions in the status bar.
	 * 
	 * @param p1
	 * @param p2 
	 */
	void reportRectangle(final RealCoords p1, final RealCoords p2);

	/**
	 * Publishes line length and angle in the status bar.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2 
	 */
	void reportLine(final double x1, final double y1, final double x2,
		final double y2);

	/**
	 * Publishes line length and angle in the status bar.
	 * 
	 * @param p1
	 * @param p2 
	 */
	void reportLine(final RealCoords p1, final RealCoords p2);

	/**
	 * Publishes point coordinates to the status bar.
	 * 
	 * @param x
	 * @param y 
	 */
	void reportPoint(final double x, final double y);

	/**
	 * Publishes point coordinates to the status bar.
	 * 
	 * @param p 
	 */
	void reportPoint(final RealCoords p);

}
