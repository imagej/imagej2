
package imagej.ui.pivot;

import imagej.ImageJ;
import imagej.ext.tool.ToolService;
import imagej.ui.ToolBar;

import org.apache.pivot.wtk.BoxPane;

/**
 * Pivot implementation of {@link ToolBar}.
 *
 * @author Curtis Rueden
 */
public class PivotToolBar extends BoxPane implements ToolBar {

	private ToolService toolService;

	public PivotToolBar() {
		toolService = ImageJ.get(ToolService.class);
		populateToolBar();
	}

	// -- ToolBar methods --

	@Override
	public ToolService getToolService() {
		return toolService;
	}

	// -- Helper methods --

	private void populateToolBar() {
		// TODO
	}

}
