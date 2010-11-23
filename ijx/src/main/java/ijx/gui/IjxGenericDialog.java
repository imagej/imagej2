package ijx.gui;

import ijx.gui.dialog.DialogListener;

import ijx.plugin.filter.IjxPlugInFilterRunner;
import ijx.plugin.api.PlugInFilterRunner;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

/**
 * IjxGenericDialog: interface for a generalized Image Canvas in ImageJ
 * Refactored from ImageJ by Grant B. Harris, November 2008, at ImageJ 2008, Luxembourg
 **/
public interface IjxGenericDialog extends IjxDialog, ActionListener, AdjustmentListener,
        FocusListener, ItemListener, KeyListener, TextListener, WindowListener {
    int MAX_SLIDERS = 25;

    /**
     * Add an Object implementing the DialogListener interface. This object will
     * be notified by its dialogItemChanged method of input to the dialog. The first
     * DialogListener will be also called after the user has typed 'OK' or if the
     * dialog has been invoked by a macro; it should read all input fields of the
     * dialog.
     * For other listeners, the OK button will not cause a call to dialogItemChanged;
     * the CANCEL button will never cause such a call.
     * @param dl the Object that wants to listen.
     */
    void addDialogListener(DialogListener dl);

    /**
     * Displays this dialog box.
     */
    void showDialog();

    void actionPerformed(ActionEvent e);

// <editor-fold defaultstate="collapsed" desc=" Add Fields ">
    /**
     * Adds a checkbox.
     * @param label			the label
     * @param defaultValue	the initial state
     */
    void addCheckbox(String label, boolean defaultValue);

    /**
     * Adds a group of checkboxs using a grid layout.
     * @param rows			the number of rows
     * @param columns		the number of columns
     * @param labels			the labels
     * @param defaultValues	the initial states
     */
    void addCheckboxGroup(int rows, int columns, String[] labels, boolean[] defaultValues);

    /**
     * Adds a popup menu.
     * @param label	the label
     * @param items	the menu items
     * @param defaultItem	the menu item initially selected
     */
    void addChoice(String label, String[] items, String defaultItem);

    /**
     * Adds a numeric field. The first word of the label must be
     * unique or command recording will not work.
     * @param label			the label
     * @param defaultValue	value to be initially displayed
     * @param digits			number of digits to right of decimal point
     */
    void addNumericField(String label, double defaultValue, int digits);

    /**
     * Adds a numeric field. The first word of the label must be
     * unique or command recording will not work.
     * @param label			the label
     * @param defaultValue	value to be initially displayed
     * @param digits			number of digits to right of decimal point
     * @param columns		width of field in characters
     * @param units			a string displayed to the right of the field
     */
    void addNumericField(String label, double defaultValue, int digits, int columns, String units);

    void addSlider(String label, double minValue, double maxValue, double defaultValue);

    /**
     * Adds an 8 column text field.
     * @param label			the label
     * @param defaultText		the text initially displayed
     */
    void addStringField(String label, String defaultText);

    /**
     * Adds a text field.
     * @param label			the label
     * @param defaultText		text initially displayed
     * @param columns			width of the text field
     */
    void addStringField(String label, String defaultText, int columns);

    /**
     * Adds one or two (side by side) text areas.
     * @param text1	initial contents of the first text area
     * @param text2	initial contents of the second text area or null
     * @param rows	the number of rows
     * @param rows	the number of columns
     */
    void addTextAreas(String text1, String text2, int rows, int columns);

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Setup ">
    /**
     * Adds a message consisting of one or more lines of text.
     */
    void addMessage(String text);

    /**
     * Adds a Panel to the dialog.
     */
    void addPanel(Panel panel);

    /**
     * Adds a Panel to the dialog with custom contraint and insets. The
     * defaults are GridBagConstraints.WEST (left justified) and
     * "new Insets(5, 0, 0, 0)" (5 pixels of padding at the top).
     */
    void addPanel(Panel panel, int contraints, Insets insets);

    /**
     * Make this a "Yes No Cancel" dialog.
     */
    void enableYesNoCancel();

    /**
     * Returns the Vector containing the Checkboxes.
     */
    Vector getCheckboxes();

    /**
     * Returns the Vector containing the Choices.
     */
    Vector getChoices();

    /**
     * Returns an error message if getNextNumber was unable to convert a
     * string into a number, otherwise, returns null.
     */
    String getErrorMessage();

    /**
     * Returns a reference to the Label or MultiLineLabel created by the
     * last addMessage() call, or null if addMessage() was not called.
     */
    Component getMessage();

    /**
     * Sets the echo character for the next string field.
     */
    void setEchoChar(char echoChar);

    /**
     * Set the insets (margins), in pixels, that will be
     * used for the next component added to the dialog.
     * <pre>
     * Default insets:
     * addMessage: 0,20,0 (empty string) or 10,20,0
     * addCheckbox: 15,20,0 (first checkbox) or 0,20,0
     * addCheckboxGroup: 10,0,0
     * addNumericField: 5,0,3 (first field) or 0,0,3
     * addStringField: 5,0,5 (first field) or 0,0,5
     * addChoice: 5,0,5 (first field) or 0,0,5
     * </pre>
     */
    void setInsets(int top, int left, int bottom);

    Insets getInsets();

    /**
     * Sets a replacement label for the "OK" button.
     */
    void setOKLabel(String label);

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Get Fields">
    /**
     * Returns the state of the next checkbox.
     */
    boolean getNextBoolean();

    /**
     * Returns the selected item in the next popup menu.
     */
    String getNextChoice();

    /**
     * Returns the index of the selected item in the next popup menu.
     */
    int getNextChoiceIndex();

    /**
     * Returns the contents of the next numeric field.
     */
    double getNextNumber();

    /**
     * Returns the contents of the next text field.
     */
    String getNextString();

    /**
     * Returns the contents of the next textarea.
     */
    String getNextText();

    /**
     * Returns the Vector containing the numeric TextFields.
     */
    Vector getNumericFields();

    /**
     * Returns the sliders (Scrollbars).
     */
    Vector getSliders();

    /**
     * Returns the Vector containing the string TextFields.
     */
    Vector getStringFields();

    /**
     * Returns a reference to textArea1.
     */
    TextArea getTextArea1();

    /**
     * Returns a reference to textArea2.
     */
    TextArea getTextArea2();
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc=" Preview ">
    /**
     * Adds a checkbox labelled "Preview" for "automatic" preview.
     * The reference to this checkbox can be retrieved by getPreviewCheckbox()
     * and it provides the additional method previewRunning for optical
     * feedback while preview is prepared.
     * PlugInFilters can have their "run" method automatically called for
     * preview under the following conditions:
     * - the PlugInFilter must pass a reference to itself (i.e., "this") as an
     * argument to the AddPreviewCheckbox
     * - it must implement the DialogListener interface and set the filter
     * parameters in the dialogItemChanged method.
     * - it must have DIALOG and PREVIEW set in its flags.
     * A previewCheckbox is always off when the filter is started and does not get
     * recorded by the Macro Recorder.
     *
     * @param pfr A reference to the PlugInFilterRunner calling the PlugInFilter
     * if automatic preview is desired, null otherwise.
     */
    void addPreviewCheckbox(PlugInFilterRunner pfr);

    /**
     * Add the preview checkbox with user-defined label; for details see the
     * addPreviewCheckbox method with standard "Preview" label
     * Note that a GenericDialog can have only one PreviewCheckbox
     */
    void addPreviewCheckbox(PlugInFilterRunner pfr, String label);

    /**
     * Returns a reference to the Preview Checkbox.
     */
    Checkbox getPreviewCheckbox();

    /**
     * optical feedback whether preview is running by switching from
     * "Preview" to "wait..."
     */
    void previewRunning(boolean isRunning);
// </editor-fold>

    /**
     * Returns true if one or more of the numeric fields contained an
     * invalid number. Must be called after one or more calls to getNextNumber().
     */
    boolean invalidNumber();

    void paint(Graphics g);

    /**
     * Returns 'true' if any numeric field contained a 'p'.
     */
    boolean unitIsPixel();

    /**
     * Returns true if the user clicked on "Cancel".
     */
    boolean wasCanceled();

    /**
     * Returns true if the user has clicked on "OK" or a macro is running.
     */
    boolean wasOKed();

// <editor-fold defaultstate="collapsed" desc="Event Handling">
    void itemStateChanged(ItemEvent e);

    void keyPressed(KeyEvent e);

    void keyReleased(KeyEvent e);

    void keyTyped(KeyEvent e);

    void adjustmentValueChanged(AdjustmentEvent e);

    void textValueChanged(TextEvent e);

    void focusGained(FocusEvent e);

    void focusLost(FocusEvent e);

    void windowActivated(WindowEvent e);

    void windowClosed(WindowEvent e);

    void windowClosing(WindowEvent e);

    void windowDeactivated(WindowEvent e);

    void windowDeiconified(WindowEvent e);

    void windowIconified(WindowEvent e);

    void windowOpened(WindowEvent e);
// </editor-fold>
}
