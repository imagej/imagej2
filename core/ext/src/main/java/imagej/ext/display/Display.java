/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.ext.display;

import imagej.IContext;
import imagej.ImageJ;
import imagej.ext.plugin.IPlugin;

import java.util.List;

/**
 * A display is a particular type of {@link IPlugin} intended to visualize
 * objects somehow. The most common type of display is the
 * <code>imagej.data.display.ImageDisplay</code>, which displays images.
 * However, in principle there are no limits to the sorts of objects that can be
 * handled.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface Display<E> extends List<E>, IPlugin, IContext {

	/**
	 * Set the ImageJ service context for the display. This should
	 * only be done once if at all as part of initialization.
	 * 
	 * @param context
	 */
	void setContext(ImageJ context);
	/**
	 * Tests whether the display is capable of visualizing objects of the given
	 * class.
	 * 
	 * @param c The class to check for visualization capabilities.
	 * @return True if the display can handle certain objects of the given class;
	 *         false if it cannot visualize any objects of that class.
	 */
	boolean canDisplay(Class<?> c);

	/** Tests whether the display is capable of visualizing the given object. */
	boolean canDisplay(Object o);

	/**
	 * Displays the given object in this display.
	 * <p>
	 * This method is essentially the same as {@link #add} except that it accepts
	 * any {@link Object} regardless of type.
	 * </p>
	 * 
	 * @throws IllegalArgumentException if the object cannot be displayed (i.e.,
	 *           if {@link #canDisplay(Object)} returns false).
	 */
	void display(Object o);

	/** Updates and redraws the display onscreen. */
	void update();

	/** Makes this display the active one. */
	void activate();

	/** Closes the display and disposes its resources. */
	void close();

	/** Gets the name of the display. */
	String getName();

	/** Sets the name of the display. */
	void setName(String name);

}
