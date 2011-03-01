package imagej.tool.event;

import imagej.tool.ITool;

/**
 * An event indicating a tool has been deactivated.
 *
 * @author Curtis Rueden
 */
public class ToolDeactivatedEvent extends ToolEvent {

	public ToolDeactivatedEvent(final ITool tool) {
		super(tool);
	}

}
