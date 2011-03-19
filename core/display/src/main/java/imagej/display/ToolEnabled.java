package imagej.display;

import java.awt.Cursor;

/**
 *
 * @author GBH
 */
public interface ToolEnabled {

	void subscribeToToolEvents();
	/*
		final EventSubscriber<ToolActivatedEvent> toolSubscriber =
			new EventSubscriber<ToolActivatedEvent>()
			{
			@Override
			public void onEvent(final ToolActivatedEvent event) {
				if(getActiveTool()!=null) getActiveTool().onKeyUp(event);
			}
		};
	 	Events.subscribe(ToolActivatedEvent.class, toolSubscriber);
	 *
	 */

	void setCursor(int cursor);

	/* 
	 on JPanel:
	 setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

	 */
}
