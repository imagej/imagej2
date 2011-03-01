package imagej.tool;

import imagej.display.Display;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.DisplayEvent;
import imagej.display.event.mouse.MsEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.ImageJEvent;
import imagej.tool.event.ToolActivatedEvent;
import imagej.tool.event.ToolDeactivatedEvent;
import imagej.tool.event.ToolEvent;

/**
 *
 * @author Grant Harris

 */

// ************ This is not done !!!!!!!!!!!!!!!!

public class ToolManager {

	private Display activeDisplay;
	private static ITool activeTool;



	public class ToolMonitor implements EventSubscriber<ToolEvent> {

		public ToolMonitor() {
			Events.subscribe(ToolEvent.class, this);
		}

		@Override
		public void onEvent(ToolEvent event) {
			if (event instanceof ToolActivatedEvent) {
				deactivateCurrentTool();
				setActiveTool(event.getTool());
			}
			//if (event instanceof ToolDeactivatedEvent) {}
		}

		public ITool getActiveTool() {
			return activeTool;
		}

		public void setActiveTool(ITool _activeTool) {
			activeTool = _activeTool;
			if (activeDisplay != null) {
				activeTool.activate(activeDisplay);
			}
		}

		private void deactivateCurrentTool() {
			if (activeTool != null) {
				activeTool.deactivate();
			}
		}
	}

	class DisplayManager implements EventSubscriber<DisplayEvent> {

		public DisplayManager() {
			Events.subscribe(DisplayEvent.class, this);
		}

		@Override
		public void onEvent(DisplayEvent event) {
			if (event instanceof DisplayActivatedEvent) {
				// deactivate the currently active display
				unsubscribeDisplay(activeDisplay);
				activeDisplay = event.getDisplay();
				if(activeTool != null){
					activeTool.activate(activeDisplay);
					//Events.subscribe(DisplayEvent.class, activeDisplay);
				}
			}
		}

			private void unsubscribeDisplay(Display activeDisplay) {
			 ///Events.unsubscribe(DisplayEvent.class, activeDisplay);
		}

	}
}
