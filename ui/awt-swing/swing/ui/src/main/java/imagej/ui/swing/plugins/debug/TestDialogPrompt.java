package imagej.ui.swing.plugins.debug;

import imagej.ImageJ;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Plugin;
import imagej.ui.DialogPrompt;
import imagej.ui.UIService;
import imagej.ui.UserInterface;

/**
 *
 * @author GBH
 */
@Plugin(menuPath = "Plugins>Debug>TestDialogPrompt")
public class TestDialogPrompt implements ImageJPlugin {

	@Override
	public void run() {
		final UserInterface ui = ImageJ.get(UIService.class).getUI();
		DialogPrompt dialog = ui.dialogPrompt("Message", "Title",
				DialogPrompt.MessageType.QUESTION_MESSAGE, DialogPrompt.OptionType.YES_NO_OPTION);
		DialogPrompt.Result result = dialog.prompt();
		if (result == DialogPrompt.Result.YES_OPTION) {
			System.out.println("That's a YES");
		}
	}

}
