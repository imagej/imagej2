
package imagej.core.tools;

import imagej.ImageJ;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleService;
import imagej.ext.plugin.PluginService;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;

@Tool(name = "Keyboard Shortcuts", alwaysActive = true, priority = Integer.MIN_VALUE)
public class AcceleratorHandler extends AbstractTool {

	private final ModuleService moduleService;
	private final PluginService pluginService;
	
	public AcceleratorHandler() {
		moduleService = ImageJ.get(ModuleService.class);
		pluginService = ImageJ.get(PluginService.class);
	}

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		// TODO: ask options service whether the Control modifier should be forced
		final ModuleInfo moduleInfo =
			moduleService.getModuleForAccelerator(evt.getAcceleratorString(true));
		if (moduleInfo == null) return;

		// run via plugin service, so that preprocessors are run
		pluginService.run(moduleInfo);

		// consume event, so that other things do not respond to it
		evt.consume();
	}

}
