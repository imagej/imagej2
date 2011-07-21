/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.ui;

/**
 *
 * @author GBH
 */
public interface DialogPrompt {

	/*
	 * Constructor: 
	 * DialogPrompt dialogPrompt(String message, String title, 
			DialogPrompt.MessageType msg, DialogPrompt.OptionType option);
	 * 
	 * Use: 
	 * final UserInterface ui = ImageJ.get(UIService.class).getUI();
	 * DialogPrompt dialog = ui.dialogPrompt("Message", "Title", 
	 *     DialogPrompt.MessageType.QUESTION_MESSAGE, DialogPrompt.OptionType.YES_NO_OPTION);
	 * DialogPrompt.Result result = dialog.prompt();
	 * if(result == DialogPrompt.Result.YES_OPTION) {
	 *	// do something
	 * }
	 * 
	 */
		
	Result prompt();
	
	/*
	 * Types copied from Swing JOptionPane
	 */
	enum MessageType {

		ERROR_MESSAGE,
		INFORMATION_MESSAGE,
		WARNING_MESSAGE,
		QUESTION_MESSAGE,
		PLAIN_MESSAGE
	}

	enum OptionType {

		DEFAULT_OPTION,
		YES_NO_OPTION,
		YES_NO_CANCEL_OPTION,
		OK_CANCEL_OPTION
	}

	enum Result {

		YES_OPTION,
		NO_OPTION,
		CANCEL_OPTION,
		OK_OPTION,
		CLOSED_OPTION
	}
	
}
