package imagej.core.commands.undo;

import imagej.command.Command;
import imagej.command.Unrecordable;
import imagej.data.display.ImageDisplay;
import imagej.menu.MenuConstants;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menu = {
	@Menu(label = MenuConstants.EDIT_LABEL,
		weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Clear Undo History")},
	headless = true)
public class ClearUndoHistory implements Command, Unrecordable {

	@Parameter
	private UndoService service;
	
	@Override
	public void run() {
		service.clearHistory();
	}

}
