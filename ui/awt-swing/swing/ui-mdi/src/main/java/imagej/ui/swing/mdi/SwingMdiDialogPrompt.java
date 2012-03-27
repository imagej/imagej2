
package imagej.ui.swing.mdi;

import imagej.ImageJ;
import imagej.ui.DialogPrompt;
import imagej.ui.UserInterface;
import imagej.ui.UIService;
import imagej.ui.swing.SwingApplicationFrame;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * TODO
 * 
 * @author Grant Harris
 */
public class SwingMdiDialogPrompt implements DialogPrompt {

	JInternalFrame dialog;
	JOptionPane pane;

	public SwingMdiDialogPrompt(final String message, final String title,
		final MessageType messageType, final OptionType optionType)
	{
		final UserInterface ui = ImageJ.get(UIService.class).getUI();
		final SwingApplicationFrame appFrame =
			(SwingApplicationFrame) ui.getApplicationFrame();
		final JMDIDesktopPane desk = (JMDIDesktopPane) ui.getDesktop();
		pane =
			new JOptionPane(message, msgMap.get(messageType), optionMap
				.get(optionType));
		dialog = new ModalInternalFrame(title, appFrame.getRootPane(), desk, pane);
	}

	@Override
	public Result prompt() {
		dialog.setVisible(true);
		final Object value = pane.getValue();
		return resultMap.get(value);
	}

	// / Translate DialogPrompt types and results to JOptionPane types and
	// results.
	static final Map<DialogPrompt.MessageType, Integer> msgMap =
		new HashMap<DialogPrompt.MessageType, Integer>();
	static final Map<DialogPrompt.OptionType, Integer> optionMap =
		new HashMap<DialogPrompt.OptionType, Integer>();
	static final Map<Integer, DialogPrompt.Result> resultMap =
		new HashMap<Integer, DialogPrompt.Result>();

	static {
		msgMap.put(DialogPrompt.MessageType.ERROR_MESSAGE,
			JOptionPane.ERROR_MESSAGE);
		msgMap.put(DialogPrompt.MessageType.INFORMATION_MESSAGE,
			JOptionPane.INFORMATION_MESSAGE);
		msgMap.put(DialogPrompt.MessageType.PLAIN_MESSAGE,
			JOptionPane.PLAIN_MESSAGE);
		msgMap.put(DialogPrompt.MessageType.WARNING_MESSAGE,
			JOptionPane.WARNING_MESSAGE);
		msgMap.put(DialogPrompt.MessageType.QUESTION_MESSAGE,
			JOptionPane.QUESTION_MESSAGE);
		//
		optionMap.put(DialogPrompt.OptionType.DEFAULT_OPTION,
			JOptionPane.DEFAULT_OPTION);
		optionMap.put(DialogPrompt.OptionType.OK_CANCEL_OPTION,
			JOptionPane.OK_CANCEL_OPTION);
		optionMap.put(DialogPrompt.OptionType.YES_NO_CANCEL_OPTION,
			JOptionPane.YES_NO_CANCEL_OPTION);
		optionMap.put(DialogPrompt.OptionType.YES_NO_OPTION,
			JOptionPane.YES_NO_OPTION);
		//
		resultMap.put(JOptionPane.CANCEL_OPTION, DialogPrompt.Result.CANCEL_OPTION);
		resultMap.put(JOptionPane.CLOSED_OPTION, DialogPrompt.Result.CLOSED_OPTION);
		resultMap.put(JOptionPane.NO_OPTION, DialogPrompt.Result.NO_OPTION);
		resultMap.put(JOptionPane.OK_OPTION, DialogPrompt.Result.OK_OPTION);
		resultMap.put(JOptionPane.YES_OPTION, DialogPrompt.Result.YES_OPTION);
	}

//	public static JInternalFrame getInternalInputDialog(Component parentComponent, 
//			Object message, String title, int messageType,
//			Icon icon, Object[] selectionValues, Object initialSelectionValue) {
//		JOptionPane pane = new JOptionPane(message, messageType, JOptionPane.OK_CANCEL_OPTION, icon, null, null);
//		Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
//		pane.setWantsInput(true);
//		pane.setSelectionValues(selectionValues);
//		pane.setInitialSelectionValue(initialSelectionValue);
//		JInternalFrame dialog = pane.createInternalFrame(parentComponent, title);
//		pane.selectInitialValue();
//		dialog.setVisible(true);
//		return dialog;
//	}
}
