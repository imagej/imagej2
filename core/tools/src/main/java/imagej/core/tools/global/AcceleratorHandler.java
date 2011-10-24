
package imagej.core.tools.global;

import imagej.ImageJ;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleService;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.util.Log;

@Tool(name = "Keyboard Shortcuts", global = true, priority = Integer.MIN_VALUE)
public class AcceleratorHandler extends AbstractTool {

	private final ModuleService moduleService;

	public AcceleratorHandler() {
		moduleService = ImageJ.get(ModuleService.class);
	}

	@Override
	public boolean onKeyDown(final KyPressedEvent evt) {
		// TODO: enquire the OptionService whether the Control modifier should be
		// forced
		final ModuleInfo moduleInfo =
			moduleService.getModuleForAccelerator(evt.getAcceleratorString(true));
		if (moduleInfo == null) return false;
		moduleService.run(moduleInfo);
		return true;
	}
}
