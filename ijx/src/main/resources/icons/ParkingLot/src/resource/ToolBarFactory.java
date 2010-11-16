/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package resource;

import java.util.*;
import javax.swing.*;

/**
 * This class represents a tool bar factory which builds
 * tool bars from the content of a resource file.<br>
 *
 * The resource entries format is (for a tool bar named 'ToolBar'):<br>
 * <pre>
 *   ToolBar           = Item1 Item2 - Item3 ...
 *   See ButtonFactory.java for details about the items
 *   ...
 * '-' represents a separator
 * </pre>
 * All entries are optional.
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/10/13
 */

public class ToolBarFactory extends ResourceManager {
    // Constants
    //
    private final static String SEPARATOR = "-";

    /**
     * The table which contains the actions
     */
    private ActionMap actions;

    /**
     * The button factory
     */
    private ButtonFactory buttonFactory;

    /**
     * Creates a new tool bar factory
     * @param rb the resource bundle that contains the menu bar
     *           description.
     * @param am the actions to add to menu items
     */
    public ToolBarFactory(ResourceBundle rb, ActionMap am) {
	super(rb);
	actions = am;
	buttonFactory = new ButtonFactory(rb, am);
    }

    /**
     * Creates a tool bar
     * @param name the name of the menu bar in the resource bundle
     * @throws MissingResourceException if one of the keys that compose the tool bar is
     *         missing.
     *         It is not thrown if the action key is missing
     * @throws ResourceFormatException  if a boolean is malformed
     * @throws MissingListenerException if an item action is not found in the action map
     */
    public JToolBar createJToolBar(String name)
	throws MissingResourceException,
               ResourceFormatException,
	       MissingListenerException {
	JToolBar result  = new JToolBar();
        List     buttons = getStringList(name);
        Iterator it      = buttons.iterator();

        while (it.hasNext()) {
	    String s = (String)it.next();
	    if (s.equals(SEPARATOR)) {
		result.addSeparator();
	    } else {
		result.add(createJButton(s));
	    }
        }
	return result;
    }

    /**
     * Creates and returns a new swing button
     * @param name the name of the button in the resource bundle
     * @throws MissingResourceException if key is not the name of a button.
     *         It is not thrown if the mnemonic and the action keys are missing
     * @throws ResourceFormatException if the mnemonic is not a single character
     * @throws MissingListenerException if the button action is not found in the action
     *         map
     */
    public JButton createJButton(String name)
	throws MissingResourceException,
	       ResourceFormatException,
	       MissingListenerException {
	return buttonFactory.createJButton(name);
    }
}
