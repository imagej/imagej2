
package imagej.core.tools.global;

import imagej.ImageJ;
import imagej.ext.display.KeyCode;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleService;
import imagej.ext.plugin.IPlugin;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginService;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;

@Tool(name = "Keyboard Shortcuts", global = true, priority = Integer.MIN_VALUE)
public class AcceleratorHandler extends AbstractTool {

	private final ModuleService moduleService;
	private final PluginService pluginService;
	private ModuleInfo zoomIn;
	private ModuleInfo zoomOut;
	
	public AcceleratorHandler() {
		moduleService = ImageJ.get(ModuleService.class);
		pluginService = ImageJ.get(PluginService.class);
		zoomIn = null;
		zoomOut = null;
	}

	@Override
	public boolean onKeyDown(final KyPressedEvent evt) {
		ModuleInfo moduleInfo = getRunnableModule(evt);
		if (moduleInfo == null)	return false;
		// need to run via pluginService, otherwise no preprocessors are run
		pluginService.run(moduleInfo);
		return true;
	}

	private ModuleInfo getRunnableModule(final KyPressedEvent evt) {
		// workaround code to make sure any use of = or - zooms correctly
		final KeyCode code = evt.getCode();
		if (code == KeyCode.MINUS) {
			return getZoomOut();
		}
		if (code == KeyCode.EQUALS) {
			return getZoomIn();
		}
		final String accelStr = evt.getAcceleratorString(true);
		return moduleService.getModuleForAccelerator(accelStr);
	}
	
	private ModuleInfo getZoomIn() {
		// lazy initialization
		if (zoomIn == null)
			zoomIn = (ModuleInfo)
				pluginService.getPlugin("imagej.core.plugins.zoom.ZoomIn");
		return zoomIn;
	}
	
	private ModuleInfo getZoomOut() {
		// lazy initialization
		if (zoomOut == null)
			zoomOut = (ModuleInfo)
				pluginService.getPlugin("imagej.core.plugins.zoom.ZoomOut");
		return zoomOut;
	}
}
