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

import java.awt.Event;
import java.net.URL;
import java.util.*;
import javax.swing.*;

/**
 * This class represents a menu factory which builds
 * menubars and menus from the content of a resource file.<br>
 *
 * The resource entries format is (for a menubar named 'MenuBar'):<br>
 * <pre>
 *   MenuBar           = Menu1 Menu2 ...
 *
 *   Menu1.type        = RADIO | CHECK | MENU | ITEM
 *   Menu1             = Item1 Item2 - Item3 ...
 *   Menu1.text        = text 
 *   Menu1.icon        = icon_name 
 *   Menu1.mnemonic    = mnemonic 
 *   Menu1.accelerator = accelerator
 *   Menu1.action      = action_name
 *   Menu1.selected    = true | false
 *   ...
 * mnemonic is a single character
 * accelerator is of the form: mod+mod+...+X
 *   where mod is Shift, Meta, Alt or Ctrl
 * '-' represents a separator
 * </pre>
 * All entries are optional except the '.type' entry
 * Consecutive RADIO items are put in a ButtonGroup
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/04/19
 */
public class MenuFactory extends ResourceManager {
    // Constants
    //
    private final static String TYPE_MENU = "MENU";
    private final static String TYPE_ITEM = "ITEM";
    private final static String TYPE_RADIO = "RADIO";
    private final static String TYPE_CHECK = "CHECK";
    private final static String SEPARATOR = "-";
    private final static String TYPE_SUFFIX = ".type";
    private final static String TEXT_SUFFIX = ".text";
    private final static String MNEMONIC_SUFFIX = ".mnemonic";
    private final static String ACCELERATOR_SUFFIX = ".accelerator";
    private final static String ACTION_SUFFIX = ".action";
    private final static String SELECTED_SUFFIX = ".selected";
    private final static String ICON_SUFFIX = ".icon";
    /**
     * The table which contains the actions
     */
    private ActionMap actions;
    /**
     * The current radio group
     */
    private ButtonGroup buttonGroup;

    /**
     * Creates a new menu factory
     * @param rb the resource bundle that contains the menu bar
     *           description.
     * @param am the actions to add to menu items
     */
    public MenuFactory(ResourceBundle rb, ActionMap am) {
        super(rb);
        actions = am;
        buttonGroup = null;
    }

    /**
     * Creates and returns a swing menu bar
     * @param name the name of the menu bar in the resource bundle
     * @throws MissingResourceException if one of the keys that compose the menu is
     *         missing.
     *         It is not thrown if the mnemonic, the accelerator and the
     *         action keys are missing
     * @throws ResourceFormatException if the mnemonic is not a single character and if
     *         the accelerator is malformed
     * @throws MissingListenerException if an item action is not found in the action map
     */
    public JMenuBar createJMenuBar(String name)
            throws MissingResourceException,
            ResourceFormatException,
            MissingListenerException {
        JMenuBar result = new JMenuBar();
        List menus = getStringList(name);
        Iterator it = menus.iterator();

        while (it.hasNext()) {
            result.add(createJMenuComponent((String) it.next()));
        }
        return result;
    }

    /**
     * Creates and returns a menu item or a separator
     * @param name the name of the menu item or "-" to create a separator
     * @throws MissingResourceException if key is not the name of a menu item.
     *         It is not thrown if the mnemonic, the accelerator and the
     *         action keys are missing
     * @throws ResourceFormatException in case of malformed entry
     * @throws MissingListenerException if an item action is not found in the action map
     */
    protected JComponent createJMenuComponent(String name)
            throws MissingResourceException,
            ResourceFormatException,
            MissingListenerException {
        if (name.equals(SEPARATOR)) {
            buttonGroup = null;
            return new JSeparator();
        }
        String type = getString(name + TYPE_SUFFIX);
        JComponent item = null;

        if (type.equals(TYPE_RADIO)) {
            if (buttonGroup == null) {
                buttonGroup = new ButtonGroup();
            }
        } else {
            buttonGroup = null;
        }

        if (type.equals(TYPE_MENU)) {
            item = createJMenu(name);
        } else if (type.equals(TYPE_ITEM)) {
            item = createJMenuItem(name);
        } else if (type.equals(TYPE_RADIO)) {
            item = createJRadioButtonMenuItem(name);
            buttonGroup.add((AbstractButton) item);
        } else if (type.equals(TYPE_CHECK)) {
            item = createJCheckBoxMenuItem(name);
        } else {
            throw new ResourceFormatException("Malformed resource",
                    bundle.getClass().getName(),
                    name + TYPE_SUFFIX);
        }

        return item;
    }

    /**
     * Creates and returns a new swing menu
     * @param name the name of the menu bar in the resource bundle
     * @throws MissingResourceException if one of the keys that compose the menu is
     *         missing
     *         It is not thrown if the mnemonic, the accelerator and the
     *         action keys are missing
     * @throws ResourceFormatException if the mnemonic is not a single character
     * @throws MissingListenerException if a item action is not found in the action map
     */
    public JMenu createJMenu(String name)
            throws MissingResourceException,
            ResourceFormatException,
            MissingListenerException {
        JMenu result = new JMenu(getString(name + TEXT_SUFFIX));
        initializeJMenuItem(result, name);

        List items = getStringList(name);
        Iterator it = items.iterator();

        while (it.hasNext()) {
            result.add(createJMenuComponent((String) it.next()));
        }
        return result;
    }

    /**
     * Creates and returns a new swing menu item
     * @param name the name of the menu item
     * @throws MissingResourceException if one of the keys that compose the menu item
     *         is missing.
     *         It is not thrown if the mnemonic, the accelerator and the
     *         action keys are missing
     * @throws ResourceFormatException if the mnemonic is not a single character
     * @throws MissingListenerException if then item action is not found in the action
     *         map
     */
    public JMenuItem createJMenuItem(String name)
            throws MissingResourceException,
            ResourceFormatException,
            MissingListenerException {
        JMenuItem result = new JMenuItem(getString(name + TEXT_SUFFIX));
        initializeJMenuItem(result, name);
        return result;
    }

    /**
     * Creates and returns a new swing radio button menu item
     * @param name the name of the menu item
     * @throws MissingResourceException if one of the keys that compose the menu item
     *         is missing.
     *         It is not thrown if the mnemonic, the accelerator and the
     *         action keys are missing
     * @throws ResourceFormatException if the mnemonic is not a single character
     * @throws MissingListenerException if then item action is not found in the action
     *         map
     */
    public JRadioButtonMenuItem createJRadioButtonMenuItem(String name)
            throws MissingResourceException,
            ResourceFormatException,
            MissingListenerException {
        JRadioButtonMenuItem result;
        result = new JRadioButtonMenuItem(getString(name + TEXT_SUFFIX));
        initializeJMenuItem(result, name);

        // is the item selected?
        try {
            result.setSelected(getBoolean(name + SELECTED_SUFFIX));
        } catch (MissingResourceException e) {
        }

        return result;
    }

    /**
     * Creates and returns a new swing check box menu item
     * @param name the name of the menu item
     * @throws MissingResourceException if one of the keys that compose the menu item
     *         is missing.
     *         It is not thrown if the mnemonic, the accelerator and the
     *         action keys are missing
     * @throws ResourceFormatException if the mnemonic is not a single character
     * @throws MissingListenerException if then item action is not found in the action
     *         map
     */
    public JCheckBoxMenuItem createJCheckBoxMenuItem(String name)
            throws MissingResourceException,
            ResourceFormatException,
            MissingListenerException {
        JCheckBoxMenuItem result = new JCheckBoxMenuItem(getString(name + TEXT_SUFFIX));
        initializeJMenuItem(result, name);

        // is the item selected?
        try {
            result.setSelected(getBoolean(name + SELECTED_SUFFIX));
        } catch (MissingResourceException e) {
        }

        return result;
    }

    /**
     * Initializes a swing menu item
     * @param item the menu item to initialize
     * @param name the name of the menu item
     * @throws ResourceFormatException if the mnemonic is not a single character
     * @throws MissingListenerException if then item action is not found in the action
     *         map
     */
    private void initializeJMenuItem(JMenuItem item, String name)
            throws ResourceFormatException,
            MissingListenerException {
        // Icon
        try {
            String s = getString(name + ICON_SUFFIX);
            URL url = actions.getClass().getResource(s);
            if (url != null) {
                item.setIcon(new ImageIcon(url));
            }
        } catch (MissingResourceException e) {
        }

        // Mnemonic
        try {
            String str = getString(name + MNEMONIC_SUFFIX);
            if (str.length() == 1) {
                item.setMnemonic(str.charAt(0));
            } else {
                throw new ResourceFormatException("Malformed mnemonic",
                        bundle.getClass().getName(),
                        name + MNEMONIC_SUFFIX);
            }
        } catch (MissingResourceException e) {
        }

        // Accelerator
        try {
            if (!(item instanceof JMenu)) {
                String str = getString(name + ACCELERATOR_SUFFIX);
                KeyStroke ks = toKeyStroke(str);
                if (ks != null) {
                    item.setAccelerator(ks);
                } else {
                    throw new ResourceFormatException("Malformed accelerator",
                            bundle.getClass().getName(),
                            name + ACCELERATOR_SUFFIX);
                }
            }
        } catch (MissingResourceException e) {
        }

        // Action
        try {
            Action a = actions.getAction(getString(name + ACTION_SUFFIX));
            if (a == null) {
                throw new MissingListenerException("", "Action", name + ACTION_SUFFIX);
            }
            item.addActionListener(a);
            if (a instanceof JComponentModifier) {
                ((JComponentModifier) a).addJComponent(item);
            }
        } catch (MissingResourceException e) {
        }
    }

    /**
     * Translate a string into a key stroke.
     * See the class comment for details
     * @param str a string
     */
    protected KeyStroke toKeyStroke(String str) {
        int state = 0;
        int code = 0;
        int modif = 0;
        int i = 0;

        while (state != 100 && i < str.length()) {
            char curr = Character.toUpperCase(str.charAt(i));

            switch (state) {
                case 0:
                    code = curr;
                    switch (curr) {
                        case 'C':
                            state = 1;
                            break;
                        case 'A':
                            state = 5;
                            break;
                        case 'M':
                            state = 8;
                            break;
                        case 'S':
                            state = 12;
                            break;
                        default:
                            state = 100;
                    }
                    break;

                case 1:
                    state = (curr == 'T') ? 2 : 100;
                    break;
                case 2:
                    state = (curr == 'R') ? 3 : 100;
                    break;
                case 3:
                    state = (curr == 'L') ? 4 : 100;
                    break;
                case 4:
                    state = (curr == '+') ? 0 : 100;
                    if (state == 0) {
                        modif |= Event.CTRL_MASK;
                    }
                    break;
                case 5:
                    state = (curr == 'L') ? 6 : 100;
                    break;
                case 6:
                    state = (curr == 'T') ? 7 : 100;
                    break;
                case 7:
                    state = (curr == '+') ? 0 : 100;
                    if (state == 0) {
                        modif |= Event.ALT_MASK;
                    }
                    break;
                case 8:
                    state = (curr == 'E') ? 9 : 100;
                    break;
                case 9:
                    state = (curr == 'T') ? 10 : 100;
                    break;
                case 10:
                    state = (curr == 'A') ? 11 : 100;
                    break;
                case 11:
                    state = (curr == '+') ? 0 : 100;
                    if (state == 0) {
                        modif |= Event.META_MASK;
                    }
                    break;
                case 12:
                    state = (curr == 'H') ? 13 : 100;
                    break;
                case 13:
                    state = (curr == 'I') ? 14 : 100;
                    break;
                case 14:
                    state = (curr == 'F') ? 15 : 100;
                    break;
                case 15:
                    state = (curr == 'T') ? 16 : 100;
                    break;
                case 16:
                    state = (curr == '+') ? 0 : 100;
                    if (state == 0) {
                        modif |= Event.SHIFT_MASK;
                    }
                    break;
            }
            i++;
        }
        if (code > 0 && modif > 0) {
            return KeyStroke.getKeyStroke(code, modif);
        }
        return null;
    }
}

/*
 *
#
# Definition of the global properties
#

Frame.title   = DynamicJava
Frame.width   = 500
Frame.height  = 570
Frame.divider = 380

Status.lineSymbol                = L
Status.init                      = New file
Status.current                   = Current file:
Status.wrote                     = Wrote:
Status.evaluation.stopped        = Evaluation stopped
Status.interpreter.reinitialized = Interpreter reinitialized

#
# Definition of the main menu bar
#

MenuBar = File Edit Run Help

# File menu #############
File           = Open Save SaveAs - Exit
File.type      = MENU
File.text      = File
File.mnemonic  = F

Open.type        = ITEM
Open.text        = Open ...
Open.icon        = resources/open.gif
Open.mnemonic    = O
Open.action      = OpenAction
Open.accelerator = Ctrl+O

Save.type        = ITEM
Save.text        = Save
Save.icon        = resources/save.gif
Save.mnemonic    = S
Save.action      = SaveAction
Save.accelerator = Ctrl+W

SaveAs.type        = ITEM
SaveAs.text        = Save As ...
SaveAs.mnemonic    = A
SaveAs.action      = SaveAsAction
SaveAs.accelerator = Ctrl+D

Exit.type        = ITEM
Exit.text        = Exit
Exit.mnemonic    = E
Exit.action      = ExitAction
Exit.accelerator = Ctrl+Z

# Edit menu #############
Edit            = Undo Redo - Cut Copy Paste - Clear - Options
Edit.type       = MENU
Edit.text       = Edit
Edit.mnemonic   = E

Undo.type        = ITEM
Undo.text        = Undo
Undo.icon        = resources/undo.gif
Undo.mnemonic    = U
Undo.action      = UndoAction
Undo.accelerator = Ctrl+U

Redo.type        = ITEM
Redo.text        = Redo
Redo.icon        = resources/redo.gif
Redo.mnemonic    = R
Redo.action      = RedoAction
Redo.accelerator = Ctrl+R

Cut.type        = ITEM
Cut.text        = Cut
Cut.icon        = resources/cut.gif
Cut.mnemonic    = t
Cut.action      = CutAction
Cut.accelerator = Ctrl+X

Copy.type        = ITEM
Copy.text        = Copy
Copy.icon        = resources/copy.gif
Copy.mnemonic    = C
Copy.action      = CopyAction
Copy.accelerator = Ctrl+C

Paste.type        = ITEM
Paste.text        = Paste
Paste.icon        = resources/paste.gif
Paste.mnemonic    = P
Paste.action      = PasteAction
Paste.accelerator = Ctrl+V

Clear.type        = ITEM
Clear.text        = Clear The Output Area
Clear.mnemonic    = l
Clear.action      = ClearAction
Clear.accelerator = Ctrl+l

Options.type        = ITEM
Options.text        = Options ...
Options.mnemonic    = O
Options.action      = OptionsAction
Options.accelerator = Ctrl+P

# Run menu #############
Run            = Eval EvalS - Stop - Reinit
Run.type       = MENU
Run.text       = Run
Run.mnemonic   = R

Eval.type        = ITEM
Eval.text        = Evaluate Buffer
Eval.icon        = resources/run.gif
Eval.mnemonic    = E
Eval.action      = EvalAction
Eval.accelerator = Ctrl+E

EvalS.type        = ITEM
EvalS.text        = Evaluate Selection
EvalS.icon        = resources/run_selection.gif
EvalS.mnemonic    = S
EvalS.action      = EvalSAction
EvalS.accelerator = Ctrl+S

Reinit.type        = ITEM
Reinit.text        = Reinitialize The Interpreter
Reinit.mnemonic    = R
Reinit.action      = ReinitAction
Reinit.accelerator = Ctrl+Q

Stop.type        = ITEM
Stop.text        = Stop The Evaluation
Stop.icon        = resources/stop.gif
Stop.mnemonic    = t
Stop.action      = StopAction
Stop.accelerator = Ctrl+T

# Help menu #############
Help            = About
Help.type       = MENU
Help.text       = ?
Help.mnemonic   = ?

About.type        = ITEM
About.text        = About ...
About.mnemonic    = A
About.action      = AboutAction
About.accelerator = Ctrl+A

# About message #########
AboutMessage = DynamicJava - Java Interpreter\n\
\n\
Send comments to Stéphane Hillion\n\
<Stephane.Hillion@koala.inria.fr>

# Initialization Error message
InterpreterInitializationError.title = Initialization Error
InterpreterInitializationError.text  = An error occured during initialization:

#
# Definition of the main tool bar
#

ToolBar         = BOpen BSave - BUndo BRedo - BCut BCopy BPaste - BEval BEvalS - BStop

BOpen.icon      = resources/open.gif
BOpen.action    = OpenAction
BOpen.tooltip   = Open/create a file

BSave.icon      = resources/save.gif
BSave.action    = SaveAction
BSave.tooltip   = Save the buffer

BUndo.icon      = resources/undo.gif
BUndo.action    = UndoAction
BUndo.tooltip   = Undo the last action

BRedo.icon      = resources/redo.gif
BRedo.action    = RedoAction
BRedo.tooltip   = Redo the last cancelled action

BCut.icon       = resources/cut.gif
BCut.action     = CutAction
BCut.tooltip    = Cut the selected text to the clipboard

BCopy.icon      = resources/copy.gif
BCopy.action    = CopyAction
BCopy.tooltip   = Copy the selected text to the clipboard

BPaste.icon     = resources/paste.gif
BPaste.action   = PasteAction
BPaste.tooltip  = Paste text from the clipbard

BEval.icon      = resources/run.gif
BEval.action    = EvalAction
BEval.tooltip   = Evaluate the content of the buffer

BEvalS.icon     = resources/run_selection.gif
BEvalS.action   = EvalSAction
BEvalS.tooltip  = Evaluate the selected text

BStop.icon      = resources/stop.gif
BStop.action    = StopAction
BStop.tooltip   = Stop the evaluation

 */