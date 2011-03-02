package imagej.plugin.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public abstract class AbstractInputPanel implements InputPanel {

	/** Widget table for numbers. */
	protected Map<String, NumberWidget> numberWidgets =
		new HashMap<String, NumberWidget>();

	/** Widget table for toggles. */
	protected Map<String, ToggleWidget> toggleWidgets =
		new HashMap<String, ToggleWidget>();

	/** Widget table for text fields. */
	protected Map<String, TextFieldWidget> textFieldWidgets =
		new HashMap<String, TextFieldWidget>();

	/** Widget table for choices. */
	protected Map<String, ChoiceWidget> choiceWidgets =
		new HashMap<String, ChoiceWidget>();

	/** Widget table for files. */
	protected Map<String, FileWidget> fileWidgets =
		new HashMap<String, FileWidget>();

	@Override
	public Number getNumber(final String name) {
		return numberWidgets.get(name).getValue();
	}

	@Override
	public boolean getToggle(final String name) {
		return toggleWidgets.get(name).isSelected();
	}

	@Override
	public String getTextField(final String name) {
		return textFieldWidgets.get(name).getText();
	}

	@Override
	public String getChoice(final String name) {
		return choiceWidgets.get(name).getItem();
	}

	@Override
	public int getChoiceIndex(final String name) {
		return choiceWidgets.get(name).getIndex();
	}

	@Override
	public File getFile(final String name) {
		return fileWidgets.get(name).getFile();
	}

	@Override
	public Object getObject(final String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
