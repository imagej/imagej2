package imagej.core.plugins.axispos;

//
//AnimateAlongAxis.java
//

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

import net.imglib2.img.Axis;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.event.AxisPositionEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.key.KyTypedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;


/**
 * Partial replacement for animation commands in IJ1.
 *  
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'),
	@Menu(label = "Tools", mnemonic = 't'),
	@Menu(label = "Animate") })
public class AnimateAlongAxis implements ImageJPlugin {

	@Parameter(label = "Speed (0.1 - 1000 fps)", min = "0.1", max = "1000")
	private double fps;

	// TODO - populate min and max values from Dataset
	@Parameter(label = "First frame", min = "1", max = "10000")
	private long oneBasedFirst;
	
	// TODO - populate min and max values from Dataset
	@Parameter(label = "Last frame", min = "1", max = "10000")
	private long oneBasedLast;

	// either wrap or move back and forth
	@Parameter(label = "Loop back and forth")
	private boolean backAndForth;
	
	// -- private instance variables --

	private Display currDisplay;
	private long first;
	private long last;
	private boolean userHasQuit;
	private EventSubscriber<KyTypedEvent> kyTypeSubscriber;
	private EventSubscriber<DisplayDeletedEvent> displaySubscriber;

	// -- public interface --
	
	@Override
	public void run() {
		currDisplay = ImageJ.get(DisplayManager.class).getActiveDisplay();
		if (currDisplay == null) return;
		Dataset ds = (Dataset) currDisplay.getActiveView().getDataObject();
		if (ds == null) return;
		Axis currAxis = AxisUtils.getActiveAxis();
		int axisIndex = ds.getImgPlus().getAxisIndex(currAxis);
		if (axisIndex < 0) return;
		long totalHyperplanes = ds.getImgPlus().dimension(axisIndex);
		subscribeToEvents();
		setFirstAndLast(totalHyperplanes);
		animateAlongAxis(currDisplay, currAxis, totalHyperplanes);
		unsubscribeFromEvents();
	}
	
	// -- private interface --

	private void setFirstAndLast(long totalHyperplanes) {
		first = Math.min(oneBasedFirst, oneBasedLast) - 1;
		last = Math.max(oneBasedFirst, oneBasedLast) - 1;
		if (first < 0) first = 0;
		if (last > totalHyperplanes-1) last = totalHyperplanes-1;
	}
	
	private void animateAlongAxis(Display display, Axis axis, long total) {
		Events.publish(new StatusEvent("Press space bar to terminate animation"));
		Events.publish(
			new AxisPositionEvent(display, axis, first, total, false));
		int increment = 1;
		long currPos = first;
		long delta = 1;
		boolean isRelative = true;
		userHasQuit = false;
		while (!userHasQuit) {
			if (ImageJ.get(DisplayManager.class).getActiveDisplay() != display) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					// do nothing
				}
				continue;
			}
			// reached right end
			if ((increment > 0) && (currPos == last)) {
				if (!backAndForth) {
					isRelative = false;
					delta = first;
					currPos = first;
				}
				else {
					increment = -increment;
					isRelative = true;
					delta = -1;
					currPos--;
				}
			}
			// reached left end
			else if ((increment < 0) && (currPos == first)) {
				if (!backAndForth) {
					isRelative = false;
					delta = last;
					currPos = last;
				}
				else {
					increment = -increment;
					isRelative = true;
					delta = +1;
					currPos++;
				}
			}
			else {  // somewhere in the middle
				isRelative = true;
				if (increment > 0) {
					delta = +1;
					currPos++;
				}
				else {  // increment < 0
					delta = -1;
					currPos--;
				}
			}

			Events.publish(
				new AxisPositionEvent(display, axis, delta, total, isRelative));
			
			try {
				Thread.sleep((long)(1000/fps));
			} catch(Exception e) {
				// do nothing
			}
		}
		Events.publish(new StatusEvent("Animation terminated"));
	}
	
	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {
		kyTypeSubscriber = new EventSubscriber<KyTypedEvent>() {
			@Override
			public void onEvent(KyTypedEvent event) {
				if (event.getCharacter() == ' ')
					userHasQuit = true;
			}
		};
		Events.subscribe(KyTypedEvent.class, kyTypeSubscriber);
		
		displaySubscriber = new EventSubscriber<DisplayDeletedEvent>() {
			@Override
			public void onEvent(DisplayDeletedEvent event) {
				if (event.getObject() == currDisplay)
					userHasQuit = true;
			}
		};
		Events.subscribe(DisplayDeletedEvent.class, displaySubscriber);
	}

	private void unsubscribeFromEvents() {
		Events.unsubscribe(KyTypedEvent.class, kyTypeSubscriber);
		Events.unsubscribe(DisplayDeletedEvent.class, displaySubscriber);
	}
}
