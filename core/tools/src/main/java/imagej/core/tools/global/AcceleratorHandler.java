
package imagej.core.tools.global;

import imagej.ImageJ;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleService;
import imagej.ext.plugin.PluginService;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;

@Tool(name = "Keyboard Shortcuts", global = true, priority = Integer.MIN_VALUE)
public class AcceleratorHandler extends AbstractTool {

	private final ModuleService moduleService;
	private final PluginService pluginService;

	public AcceleratorHandler() {
		moduleService = ImageJ.get(ModuleService.class);
		pluginService = ImageJ.get(PluginService.class);
	}

	@Override
	public boolean onKeyDown(final KyPressedEvent evt) {
		// TODO: enquire the OptionService whether the Control modifier should be
		// forced
		final ModuleInfo moduleInfo =
			moduleService.getModuleForAccelerator(evt.getAcceleratorString(true));
		if (moduleInfo == null) return false;
		// need to run via pluginService, otherwise no preprocessors are run
		pluginService.run(moduleInfo);
		return true;
	}
}
