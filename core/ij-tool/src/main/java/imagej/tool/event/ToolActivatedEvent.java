package imagej.tool.event;

import imagej.tool.ITool;

/**
 * An event indicating a tool has been activated.
 *
 * @author Curtis Rueden
 */
public class ToolActivatedEvent extends ToolEvent {

	public ToolActivatedEvent(final ITool tool) {
		super(tool);
	}

}
