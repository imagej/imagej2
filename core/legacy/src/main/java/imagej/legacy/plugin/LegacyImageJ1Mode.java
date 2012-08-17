package imagej.legacy.plugin;

import imagej.command.ContextCommand;
import imagej.legacy.LegacyService;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menu = { @Menu(label = "Help"), @Menu(label = "Start Legacy ImageJ 1.x Mode") })
public class LegacyImageJ1Mode extends ContextCommand {

	@Parameter
	private LegacyService legacyService;

	@Override
	public void run() {
		legacyService.toggleLegacyMode(true);
	}

}
