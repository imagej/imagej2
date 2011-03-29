//
//
// DisplayManager.java

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.display;

import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.manager.Manager;
import imagej.manager.ManagerComponent;
import imagej.manager.Managers;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager component for keeping track of active displays.
 *
 * @author Curtis Rueden
 */
@Manager(priority = Managers.LOW_PRIORITY)
public class DisplayManager implements ManagerComponent {

	private final List<Display> displays = new ArrayList<Display>();

	public void addDisplay(final Display display) {
		if (!displays.contains(display)) displays.add(display);
	}

	public void removeDisplay(final Display display) {
		displays.remove(display);
	}

	/** Gets a list of active displays. */
	public List<Display> getDisplays() {
		final List<Display> displayList = new ArrayList<Display>(displays);
		return displayList;
	}

	// -- ManagerComponent methods --

	@Override
	public void initialize() {
		Events.subscribe(DisplayCreatedEvent.class,
			new EventSubscriber<DisplayCreatedEvent>()
		{
			@Override
			public void onEvent(final DisplayCreatedEvent event) {
				addDisplay(event.getDisplay());
			}			
		});
		Events.subscribe(DisplayDeletedEvent.class,
			new EventSubscriber<DisplayDeletedEvent>()
		{
			@Override
			public void onEvent(final DisplayDeletedEvent event) {
				removeDisplay(event.getDisplay());
			}			
		});
	}

}
