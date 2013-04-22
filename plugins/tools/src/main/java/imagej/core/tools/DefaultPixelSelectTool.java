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

import imagej.core.tools.event.PixelSelectToolEvent;
import imagej.data.display.ImageDisplay;
import imagej.display.Display;
import imagej.display.event.input.MsButtonEvent;
import imagej.display.event.input.MsReleasedEvent;
import imagej.tool.AbstractTool;
import org.scijava.event.EventService;

/**
 * Base implementation for a custom pixel selection tool.
 * 
 * Subclasses should provide an icon image and possibly a new priority.
 * 
 * Clicking on the image initiates a pixel select event.
 * 
 * @author Aivar Grislis
 */
public abstract class DefaultPixelSelectTool extends AbstractTool {
	public static final double PRIORITY = -400;
	
	/** On mouse up, clicked on a pixel. */
	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		if (MsButtonEvent.LEFT_BUTTON == evt.getButton()
				&& 1 == evt.getNumClicks())
		{
			// get event service
			final EventService eventService =
					getContext().getService(EventService.class);
			
			if (null != eventService) {
				// get display
				final Display<?> display = evt.getDisplay();
				if (display instanceof ImageDisplay) {
					final ImageDisplay imageDisplay = (ImageDisplay) display;

					// get location clicked
					final int x = evt.getX();
					final int y = evt.getY();

					// check if within planar bounds
					final long[] dims = imageDisplay.getDims();
					if (x < dims[0] && y < dims[1]) {
						// fill in rest of dimensional positions
						final int[] position = new int[dims.length];
						position[0] = x;
						position[1] = y;
						for (int d = 2; d < dims.length; ++d) {
							position[d] = imageDisplay.getIntPosition(d);
						}

						// propagate pixel select event
						final PixelSelectToolEvent event =
								new PixelSelectToolEvent
										(this, imageDisplay, position);
						eventService.publishLater(event);
					}
				}
			}
		}
		evt.consume();
	}
}