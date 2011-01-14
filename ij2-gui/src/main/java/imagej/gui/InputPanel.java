package imagej.gui;

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
	void addNumber(String label, Number initialValue,
		Number min, Number max, Number stepSize);

	/** Returns the contents of the next numeric field. */
	Number getNextNumber();

	/**
	 * Adds a checkbox.
	 * 
	 * @param label
	 *          the label
	 * @param initialValue
	 *          the initial state
	 */
	void addToggle(String label, boolean initialValue);

	/** Returns the state of the next checkbox. */
	boolean getNextToggle();

	/**
	 * Adds a text field.
	 * 
	 * @param label
	 *          the label
	 * @param initialValue
	 *          the text initially displayed
	 * @param columns
	 *          width of field in characters
	 */
	void addTextField(String label, String initialValue, int columns);

	/** Returns the contents of the next text field. */
	String getNextTextField();

	/**
	 * Adds a multiple choice text field.
	 * 
	 * @param label
	 *          the label
	 * @param items
	 *          the choices
	 * @param initialValue
	 *          the initially selected item
	 */
	void addChoice(String label, String[] items, String initialValue);

	/** Returns the selected item in the next choice text field. */
	String getNextChoice();

	/** Returns the index of the selected item in the next choice text field. */
	int getNextChoiceIndex();

	/**
	 * Adds a file selector.
	 * 
	 * @param label
	 *          the label
	 * @param initialValue
	 *          the initially specified file
	 */
	void addFile(String label, File initialValue);

	/** Returns the value of the next file selector. */
	File getNextFile();

	/**
	 * Adds a dataset selector.
	 * 
	 * @param label
	 *          the label
	 * @param initialValue
	 *          the initially specified dataset
	 */
	void addDataset(String label, Dataset initialValue);

	/** Returns the value of the next dataset selector. */
	Dataset getNextDataset();

}
