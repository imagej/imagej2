package ijx.action;

// from package pathdesign.app;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;


/*
 * From: http://weblogs.java.net/blog/timboudreau/archive/2006/07/index.html
 * 
 Anyway, since I still have some plans for the Imagine project, I was thinking about how
 best to design an image editor, and wanted to do some experimenting that might be useful
 later. What I ended up with is a concept where there are EditingModes, and an EditingMode
 makes available a set of tools that should be shown in a toolbar and a menu. The user can
 select editing modes, and the user can select tools (and also EditingModes have lists of
 actions of their own).

So I've got three things that need basically the same thing - each has a list of objects
 that share a type. For each list of objects, I need a corresponding list of toggle buttons
 and menu items. But the list contents are all different types.

This is where generics come to the rescue - in the past, I probably wouldn't have thought
 to take this approach, simply because all of the casting required would be ugly, and it
 wouldn't be terribly type-safe. What I did is create a single class with a generic type;
 its constructor takes an argument of a 3 method interface which must have the same generic
 type. So all of the logic about manufacturing menu items and such is shared; I just implement
 the Logic interface for each one and pass it to the constructor and I'm done. The Logic
 interface handles fetching the display name of an object from the list, fetching the icon
 for the object, and performing whatever action should be performed when the menu item or
 button is invoked.
 *
 */

public class ActionMapper <T extends Object> {
    private List <T> objects;
    private final Logic logic;
    private T selection;

    public interface Logic <T> {
        void invoke (T t);
        String getDisplayName (T t);
        Icon getIcon (T t);
    }

    public ActionMapper(List <T> objects, Logic<T> invoker, T initialSelection) {
        this.objects = objects == null ? new ArrayList <T> () :
            new ArrayList<T>(objects);
        this.selection = initialSelection == null ? objects.size() > 0 ?
            objects.get(0) : null : initialSelection;

        if (initialSelection == null) {
            throw new NullPointerException ("Can't determine initial selection");
        }
        selection = initialSelection;
        this.logic = invoker;
        if (invoker == null) {
            throw new NullPointerException ("Invoker null"); //NOI18N
        }
    }

    public interface MapperAction <T> extends Action {
        public T getItem();
    }

    public boolean setObjects (List <T> objects, T selection) {
        if (!this.objects.equals(objects)) {
            this.objects = new ArrayList<T>(objects);
            setSelection (selection);
            buttons = null;
            actions = null;
            menuItems = null;
            return true;
        } else {
            return setSelection (selection);
        }
    }

    public List <T> getObjects() {
        return new ArrayList <T> (objects);
    }

    private boolean inSetSelection = false;
    public boolean setSelection (T selection) {
        if (inSetSelection) return false;
        inSetSelection = true;
        try {
            if (!objects.contains(selection)) {
                throw new IllegalArgumentException ("I don't know about "  //NOI18N
                        + selection);
            }
            boolean result = !selection.equals (this.selection);
            if (result) {
                this.selection = selection;
                updateButton(selection);
            }
            return result;
        } finally {
            inSetSelection = false;
        }
    }

    public T getSelection() {
        return selection;
    }

    private List <MapperAction<T>> actions;
    public List <MapperAction<T>> getActions() {
        if (actions == null) {
            actions = createActions();
        }
        return actions;
    }

    private List <JToggleButton> buttons;
    public List <JToggleButton> getButtons() {
        if (buttons == null) {
            buttons = createButtons();
        }
        return buttons;
    }

    public List <JMenuItem> menuItems;
    public List <JMenuItem> getMenuItems() {
        if (menuItems == null) {
            menuItems = createMenuItems();
        }
        return menuItems;
    }

    private List <MapperAction <T>> createActions() {
        List <MapperAction<T>> result = new ArrayList <MapperAction<T>> (objects.size());
        for (T item : objects) {
            MapperAction <T> action = new MapperActionImpl <T>(item);
            action.putValue(Action.NAME, logic.getDisplayName (item));
            result.add (action);
        }
        return result;
    }

    private List <JToggleButton> createButtons() {
        List <JToggleButton> result = new ArrayList <JToggleButton> (objects.size());
        ButtonGroup grp = new ButtonGroup();
        for (MapperAction<T> action : getActions()) {
            JToggleButton button = new JToggleButton();
            button.setAction (action);
            button.setIcon (logic.getIcon (action.getItem()));
            result.add (button);
            if (getSelection() == action.getItem()) {
                button.setSelected(true);
            }
            grp.add (button);
        }
        return result;
    }

    private List <JMenuItem> createMenuItems() {
        List <JMenuItem> result = new ArrayList <JMenuItem> (objects.size());
        ButtonGroup grp = new ButtonGroup();
        for (MapperAction<T> action : getActions()) {
            JMenuItem button = new JMenuItem();
            button.setAction (action);
            button.setIcon (logic.getIcon (action.getItem()));
            result.add (button);
            if (getSelection() == action.getItem()) {
                button.setSelected(true);
            }
            grp.add (button);
        }
        return result;
    }

    private void updateButton (T t) {
        JToggleButton b = buttonFor (t);
        if (b != null) {
            b.setSelected (true);
        }
    }

    private JToggleButton buttonFor (T t) {
        if (buttons != null) {
            for (JToggleButton button : buttons) {
                MapperAction<T> action = (MapperAction<T>) button.getAction();
                if (action.getItem().equals (t))  {
                    return button;
                }
            }
        }
        return null;
    }

    private final class MapperActionImpl <T> extends AbstractAction implements MapperAction <T> {
        private final T item;
        public MapperActionImpl (T item) {
            this.item = item;
        }

        public void actionPerformed(ActionEvent e) {
            logic.invoke(getItem());
        }

        public T getItem() {
            return item;
        }
    }
}
