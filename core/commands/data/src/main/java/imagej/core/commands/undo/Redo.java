package imagej.core.commands.undo;

import imagej.command.Command;
import imagej.data.Dataset;
import imagej.menu.MenuConstants;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menu = {
	@Menu(label = MenuConstants.EDIT_LABEL,
		weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Redo")},
	headless = true)
public class Redo implements Command {

	@Parameter
	private UndoService service;
	
	@Parameter(required = false)
	private Dataset dataset;
	
	@Override
	public void run() {
		service.redo(dataset);
	}

}
