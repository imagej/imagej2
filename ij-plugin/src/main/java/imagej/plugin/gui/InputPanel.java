package imagej.plugin.gui;

import imagej.dataset.Dataset;

import java.io.File;

/**
 * Flexible panel-building interface, for use with
 * UIs that prompt for input values of various types.
 */
public interface InputPanel {

	// TODO - groups of fields

	/** Adds a message consisting of one or more lines of text. */
	void addMessage(String text);

	/**
	 * Adds a numeric field.
	 * 
	 * @param name
	 *          unique name identifying this field
	 * @param label
	 *          the label
	 * @param initialValue
	 *          value to be initially displayed
	 * @param min
	 *          minimum allowed value
	 * @param max
	 *          maximum allowed value
	 * @param stepSize
	 *          
	 */
	void addNumber(String name, String label, Number initialValue,
		Number min, Number max, Number stepSize);

	/**
	 * Adds a checkbox.
	 * 
	 * @param name
	 *          unique name identifying this field
	 * @param label
	 *          the label
	 * @param initialValue
	 *          the initial state
	 */
	void addToggle(String name, String label, boolean initialValue);

	/**
	 * Adds a text field.
	 * 
	 * @param name
	 *          unique name identifying this field
	 * @param label
	 *          the label
	 * @param initialValue
	 *          the text initially displayed
	 * @param columns
	 *          width of field in characters
	 */
	void addTextField(String name, String label, String initialValue,
		int columns);

	/**
	 * Adds a multiple choice text field.
	 * 
	 * @param name
	 *          unique name identifying this field
	 * @param label
	 *          the label
	 * @param initialValue
	 *          the initially selected item
	 * @param items
	 *          the choices
	 */
	void addChoice(String name, String label, String initialValue,
		String[] items);

	/**
	 * Adds a file selector.
	 * 
	 * @param name
	 *          unique name identifying this field
	 * @param label
	 *          the label
	 * @param initialValue
	 *          the initially specified file
	 */
	void addFile(String name, String label, File initialValue);

	/**
	 * Adds a dataset selector.
	 * 
	 * @param name
	 *          unique name identifying this field
	 * @param label
	 *          the label
	 * @param initialValue
	 *          the initially specified dataset
	 */
	void addDataset(String name, String label, Dataset initialValue);

	/**
	 * Returns the contents of the given numeric field.
	 * 
	 * @param name
	 *          unique name identifying this field
	 */
	Number getNumber(String name);

	/**
	 * Returns the state of the given checkbox.
	 * 
	 * @param name
	 *          unique name identifying this field
	 */
	boolean getToggle(String name);

	/**
	 * Returns the contents of the given text field.
	 * 
	 * @param name
	 *          unique name identifying this field
	 */
	String getTextField(String name);

	/**
	 * Returns the selected item in the given choice text field.
	 * 
	 * @param name
	 *          unique name identifying this field
	 */
	String getChoice(String name);

	/**
	 * Returns the index of the selected item in the given choice text field.
	 * 
	 * @param name
	 *          unique name identifying this field
	 */
	int getChoiceIndex(String name);

	/**
	 * Returns the value of the given file selector.
	 * 
	 * @param name
	 *          unique name identifying this field
	 */
	File getFile(String name);

	/** Returns the value of the given dataset selector.
	 * 
	 * @param name
	 *          unique name identifying this field
	 */
	Dataset getDataset(String name);

}
