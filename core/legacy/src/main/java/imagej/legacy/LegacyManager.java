//
// LegacyManager.java
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

package imagej.legacy;

import ij.ImagePlus;
import ij.WindowManager;
import imagej.ImageJ;
import imagej.Manager;
import imagej.ManagerComponent;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.event.DisplayActivatedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.OptionsChangedEvent;
import imagej.legacy.patches.FunctionsMethods;
import imagej.legacy.plugin.LegacyPlugin;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager component for working with legacy ImageJ 1.x.
 * <p>
 * The legacy manager overrides the behavior of various IJ1 methods, inserting
 * seams so that (e.g.) the modern UI is aware of IJ1 events as they occur.
 * </p>
 * <p>
 * It also maintains an image map between IJ1 {@link ImagePlus} objects and IJ2
 * {@link Dataset}s.
 * </p>
 * <p>
 * In this fashion, when a legacy plugin is executed on a {@link Dataset}, the
 * manager transparently translates it into an {@link ImagePlus}, and vice
 * versa, enabling backward compatibility with legacy plugins.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Manager(priority = Manager.HIGH_PRIORITY)
public final class LegacyManager implements ManagerComponent {

	static {
		// NB: Override class behavior before class loading gets too far along.
		final CodeHacker hacker = new CodeHacker();

		// override behavior of ij.ImageJ
		hacker.insertMethod("ij.ImageJ",
			"public java.awt.Point getLocationOnScreen()");
		hacker.loadClass("ij.ImageJ");

		// override behavior of ij.IJ
		hacker.insertAfterMethod("ij.IJ",
			"public static void showProgress(double progress)");
		hacker.insertAfterMethod("ij.IJ",
			"public static void showProgress(int currentIndex, int finalIndex)");
		hacker.insertAfterMethod("ij.IJ",
			"public static void showStatus(java.lang.String s)");
		hacker.loadClass("ij.IJ");

		// override behavior of ij.ImagePlus
		hacker.insertAfterMethod("ij.ImagePlus", "public void updateAndDraw()");
		hacker.insertAfterMethod("ij.ImagePlus", "public void repaintWindow()");
		hacker.loadClass("ij.ImagePlus");

		// override behavior of ij.gui.ImageWindow
		hacker.insertMethod("ij.gui.ImageWindow",
			"public void setVisible(boolean vis)");
		hacker.insertMethod("ij.gui.ImageWindow", "public void show()");
		hacker.insertAfterMethod("ij.gui.ImageWindow", "public void close()");
		hacker.loadClass("ij.gui.ImageWindow");

		// override behavior of ij.macro.Functions
		hacker
			.insertBeforeMethod("ij.macro.Functions",
				"void displayBatchModeImage(ij.ImagePlus imp2)",
				"imagej.legacy.patches.FunctionsMethods.displayBatchModeImageBefore(imp2);");
		hacker
			.insertAfterMethod("ij.macro.Functions",
				"void displayBatchModeImage(ij.ImagePlus imp2)",
				"imagej.legacy.patches.FunctionsMethods.displayBatchModeImageAfter(imp2);");
		hacker.loadClass("ij.macro.Functions");

		// override behavior of MacAdapter
		hacker.replaceMethod("MacAdapter",
			"public void run(java.lang.String arg)", ";");
		hacker.loadClass("MacAdapter");
	}

	/** Mapping between modern and legacy image data structures. */
	private LegacyImageMap imageMap;

	/** Method of synchronizing IJ2 & IJ1 options */
	private OptionsSynchronizer optionsSynchronizer;
	
	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	// -- LegacyManager methods --

	public LegacyImageMap getImageMap() {
		return imageMap;
	}

	/**
	 * Indicates to the manager that the given {@link ImagePlus} has changed as
	 * part of a legacy plugin execution.
	 */
	public void legacyImageChanged(final ImagePlus imp) {
		// CTR FIXME rework static InsideBatchDrawing logic?
		if (FunctionsMethods.InsideBatchDrawing > 0) return;
		// record resultant ImagePlus as a legacy plugin output
		LegacyPlugin.getOutputImps().add(imp);
	}

	/**
	 * Ensures that the currently active {@link ImagePlus} matches the currently
	 * active {@link Display}. Does not perform any harmonization.
	 */
	public void syncActiveImage() {
		final DisplayManager displayManager = ImageJ.get(DisplayManager.class);
		final Display activeDisplay = displayManager.getActiveDisplay();
		final ImagePlus activeImagePlus = imageMap.lookupImagePlus(activeDisplay);
		WindowManager.setTempCurrentImage(activeImagePlus);
	}

	// -- ManagerComponent methods --

	@Override
	public void initialize() {
		
		imageMap = new LegacyImageMap();
		optionsSynchronizer = new OptionsSynchronizer();
		
		// initialize legacy ImageJ application
		try {
			new ij.ImageJ(ij.ImageJ.NO_SHOW);
		}
		catch (final Throwable t) {
			Log.warn("Failed to instantiate IJ1.", t);
		}

		// TODO - FIXME
		// call optionsSynchronizer.update() here? Need to determine when the
		// IJ2 settings file has been read/initialized and then call update() once.
		
		subscribeToEvents();
	}

	// -- Helper methods --

	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		// keep the active legacy ImagePlus in sync with the active modern Display
		final EventSubscriber<DisplayActivatedEvent> displayActivatedSubscriber =
			new EventSubscriber<DisplayActivatedEvent>() {

				@Override
				public void onEvent(final DisplayActivatedEvent event) {
					syncActiveImage();
				}
			};
		subscribers.add(displayActivatedSubscriber);
		Events.subscribe(DisplayActivatedEvent.class, displayActivatedSubscriber);
		
		final EventSubscriber<OptionsChangedEvent> optionSubscriber =
			new EventSubscriber<OptionsChangedEvent>() {

				@Override
				public void onEvent(OptionsChangedEvent event) {
					optionsSynchronizer.update();
				}
				
		};
		subscribers.add(optionSubscriber);
		Events.subscribe(OptionsChangedEvent.class, optionSubscriber);
			
	}
}
