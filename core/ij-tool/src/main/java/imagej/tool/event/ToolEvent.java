package imagej.tool.event;

import imagej.event.ImageJEvent;
import imagej.tool.ITool;

/**
 * An event indicating something has happened to a tool.
 *
 * @author Curtis Rueden
 */
public class ToolEvent extends ImageJEvent {

	private ITool tool;

	public ToolEvent(final ITool tool) {
		this.tool = tool;
	}

	public ITool getTool() {
		return tool;
	}

}
