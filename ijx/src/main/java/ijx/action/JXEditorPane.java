package ijx.action;
/*
 * $Id: JXEditorPane.java,v 1.3 2005/06/01 23:53:44 rbair Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import java.net.URL;
import java.util.ArrayList;

import java.util.Vector;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Segment;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

/**
 * An extended editor pane which has the following features built in:
 * <ul>
 *   <li>Text search
 *   <li>undo/redo
 *   <li>simple html/plain text editing
 * </ul>
 *
 * @author Mark Davidson
 */
public class JXEditorPane extends JEditorPane implements /*Searchable,*/ Targetable {
    private Matcher matcher;
    private UndoableEditListener undoHandler;
    private UndoManager undoManager;
    private CaretListener caretHandler;
    private JComboBox selector;
    // The ids of supported actions. Perhaps this should be public.
    private final static String ACTION_FIND = "find";
    private final static String ACTION_UNDO = "undo";
    private final static String ACTION_REDO = "redo";
    /*
     * These next 3 actions are part of a *HACK* to get cut/copy/paste
     * support working in the same way as find, undo and redo. in JTextComponent
     * the cut/copy/paste actions are _not_ added to the ActionMap. Instead,
     * a default "transfer handler" system is used, apparently to get the text
     * onto the system clipboard.
     * Since there aren't any CUT/COPY/PASTE actions in the JTextComponent's action
     * map, they cannot be referenced by the action framework the same way that
     * find/undo/redo are. So, I added the actions here. The really hacky part
     * is that by defining an Action to go along with the cut/copy/paste keys,
     * I loose the default handling in the cut/copy/paste routines. So, I have
     * to remove cut/copy/paste from the action map, call the appropriate
     * method (cut, copy, or paste) and then add the action back into the
     * map. Yuck!
     */
    private final static String ACTION_CUT = "cut";
    private final static String ACTION_COPY = "copy";
    private final static String ACTION_PASTE = "paste";
    private TargetableSupport targetSupport = new TargetableSupport(this);

    public JXEditorPane() {
        init();
    }

    public JXEditorPane(String url) throws IOException {
        super(url);
        init();
    }

    public JXEditorPane(String type, String text) {
        super(type, text);
        init();
    }

    public JXEditorPane(URL initialPage) throws IOException {
        super(initialPage);
        init();
    }

    private void init() {
        setEditorKitForContentType("text/html", new SloppyHTMLEditorKit());
        addPropertyChangeListener(new PropertyHandler());
        getDocument().addUndoableEditListener(getUndoableEditListener());
        initActions();
    }

    private class PropertyHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (name.equals("document")) {
                Document doc = (Document) evt.getOldValue();
                if (doc != null) {
                    doc.removeUndoableEditListener(getUndoableEditListener());
                }

                doc = (Document) evt.getNewValue();
                if (doc != null) {
                    doc.addUndoableEditListener(getUndoableEditListener());
                }
            }
        }
    }

    // pp for testing
    CaretListener getCaretListener() {
        return caretHandler;
    }

    // pp for testing
    UndoableEditListener getUndoableEditListener() {
        if (undoHandler == null) {
            undoHandler = new UndoHandler();
            undoManager = new UndoManager();
        }
        return undoHandler;
    }

    /**
     * Overidden to perform document initialization based on type.
     */
    public void setEditorKit(EditorKit kit) {
        super.setEditorKit(kit);

        if (kit instanceof StyledEditorKit) {
            if (caretHandler == null) {
                caretHandler = new CaretHandler();
            }
            addCaretListener(caretHandler);
        }
    }

    /**
     * Register the actions that this class can handle.
     */
    protected void initActions() {
        ActionMap map = getActionMap();
        map.put(ACTION_FIND, new Actions(ACTION_FIND));
        map.put(ACTION_UNDO, new Actions(ACTION_UNDO));
        map.put(ACTION_REDO, new Actions(ACTION_REDO));
        map.put(ACTION_CUT, new Actions(ACTION_CUT));
        map.put(ACTION_COPY, new Actions(ACTION_COPY));
        map.put(ACTION_PASTE, new Actions(ACTION_PASTE));
    }

    // undo/redo implementation
    private class UndoHandler implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent evt) {
            undoManager.addEdit(evt.getEdit());
            updateActionState();
        }
    }

    /**
     * Updates the state of the actions in response to an undo/redo operation.
     */
    private void updateActionState() {
        // Update the state of the undo and redo actions
        Runnable doEnabled = new Runnable() {
            public void run() {
                ActionManager manager = ActionManager.getInstance();
                manager.setEnabled(ACTION_UNDO, undoManager.canUndo());
                manager.setEnabled(ACTION_REDO, undoManager.canRedo());
            }
        };
        SwingUtilities.invokeLater(doEnabled);
    }

    /**
     * A small class which dispatches actions.
     * TODO: Is there a way that we can make this static?
     */
    private class Actions extends UIAction {
        Actions(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent evt) {
            String name = getName();
            if (ACTION_FIND.equals(name)) {
                find();
            } else if (ACTION_UNDO.equals(name)) {
                try {
                    undoManager.undo();
                } catch (CannotUndoException ex) {
                    ex.printStackTrace();
                }
                updateActionState();
            } else if (ACTION_REDO.equals(name)) {
                try {
                    undoManager.redo();
                } catch (CannotRedoException ex) {
                    ex.printStackTrace();
                }
                updateActionState();
            } else if (ACTION_CUT.equals(name)) {
                ActionMap map = getActionMap();
                map.remove(ACTION_CUT);
                cut();
                map.put(ACTION_CUT, this);
            } else if (ACTION_COPY.equals(name)) {
                ActionMap map = getActionMap();
                map.remove(ACTION_COPY);
                copy();
                map.put(ACTION_COPY, this);
            } else if (ACTION_PASTE.equals(name)) {
                ActionMap map = getActionMap();
                map.remove(ACTION_PASTE);
                paste();
                map.put(ACTION_PASTE, this);
            } else {
                System.out.println("ActionHandled: " + name);
            }

        }
    }

    /**
     * Retrieves a component which will be used as the paragraph selector.
     * This can be placed in the toolbar.
     * <p>
     * Note: This is only valid for the HTMLEditorKit
     */
    public JComboBox getParagraphSelector() {
        if (selector == null) {
            selector = new ParagraphSelector();
        }
        return selector;
    }

    /**
     * A control which should be placed in the toolbar to enable
     * paragraph selection.
     */
    private class ParagraphSelector extends JComboBox implements ItemListener {
        private Map itemMap;

        public ParagraphSelector() {

            // The item map is for rendering
            itemMap = new HashMap();
            itemMap.put(HTML.Tag.P, "Paragraph");
            itemMap.put(HTML.Tag.H1, "Heading 1");
            itemMap.put(HTML.Tag.H2, "Heading 2");
            itemMap.put(HTML.Tag.H3, "Heading 3");
            itemMap.put(HTML.Tag.H4, "Heading 4");
            itemMap.put(HTML.Tag.H5, "Heading 5");
            itemMap.put(HTML.Tag.H6, "Heading 6");
            itemMap.put(HTML.Tag.PRE, "Preformatted");

            // The list of items
            Vector items = new Vector();
            items.addElement(HTML.Tag.P);
            items.addElement(HTML.Tag.H1);
            items.addElement(HTML.Tag.H2);
            items.addElement(HTML.Tag.H3);
            items.addElement(HTML.Tag.H4);
            items.addElement(HTML.Tag.H5);
            items.addElement(HTML.Tag.H6);
            items.addElement(HTML.Tag.PRE);

            setModel(new DefaultComboBoxModel(items));
            setRenderer(new ParagraphRenderer());
            addItemListener(this);
            setFocusable(false);
        }

        public void itemStateChanged(ItemEvent evt) {
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                applyTag((HTML.Tag) evt.getItem());
            }
        }

        private class ParagraphRenderer extends DefaultListCellRenderer {
            public ParagraphRenderer() {
                setOpaque(true);
            }

            public Component getListCellRendererComponent(JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);

                setText((String) itemMap.get(value));

                return this;
            }
        }
        // TODO: Should have a rendererer which does stuff like:
        // Paragraph, Heading 1, etc...
    }

    /**
     * Applys the tag to the current selection
     */
    protected void applyTag(HTML.Tag tag) {
        Document doc = getDocument();
        if (!(doc instanceof HTMLDocument)) {
            return;
        }
        HTMLDocument hdoc = (HTMLDocument) doc;
        int start = getSelectionStart();
        int end = getSelectionEnd();

        Element element = hdoc.getParagraphElement(start);
        MutableAttributeSet newAttrs = new SimpleAttributeSet(element.getAttributes());
        newAttrs.addAttribute(StyleConstants.NameAttribute, tag);

        hdoc.setParagraphAttributes(start, end - start, newAttrs, true);
    }

    //private JXFindDialog dialog = null;
    /**
     * The paste method has been overloaded to strip off the <html><body> tags
     * This doesn't really work.
     */
    public void paste() {
        Clipboard clipboard = getToolkit().getSystemClipboard();
        Transferable content = clipboard.getContents(this);
        if (content != null) {
            DataFlavor[] flavors = content.getTransferDataFlavors();
            try {
                for (int i = 0; i < flavors.length; i++) {
                    if (String.class.equals(flavors[i].getRepresentationClass())) {
                        Object data = content.getTransferData(flavors[i]);

                        if (flavors[i].isMimeTypeEqual("text/plain")) {
                            // This works but we lose all the formatting.
                            replaceSelection(data.toString());
                            break;
                        } /*
                        else if (flavors[i].isMimeTypeEqual("text/html")) {
                        // This doesn't really work since we would
                        // have to strip off the <html><body> tags
                        Reader reader = flavors[i].getReaderForText(content);
                        int start = getSelectionStart();
                        int end = getSelectionEnd();
                        int length = end - start;
                        EditorKit kit = getUI().getEditorKit(this);
                        Document doc = getDocument();
                        if (length > 0) {
                        doc.remove(start, length);
                        }
                        kit.read(reader, doc, start);
                        break;
                        } */
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void find() {
//        if (dialog == null) {
//            dialog = new JXFindDialog(this);
//        }
//        dialog.setVisible(true);
    }

    public int search(String searchString) {
        return search(searchString, -1);
    }

    public int search(String searchString, int columnIndex) {
        Pattern pattern = null;
        if (searchString != null) {
            return search(Pattern.compile(searchString, 0), columnIndex);
        }
        return -1;
    }

    public int search(Pattern pattern) {
        return search(pattern, -1);
    }

    public int search(Pattern pattern, int startIndex) {
        return search(pattern, startIndex, false);
    }

    /**
     * @return end position of matching string or -1
     */
    public int search(Pattern pattern, int startIndex, boolean backwards) {
        if (pattern == null) {
            return -1;
        }

        int start = startIndex + 1;
        int end = -1;

        Segment segment = new Segment();
        try {
            Document doc = getDocument();
            doc.getText(start, doc.getLength() - start, segment);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        matcher = pattern.matcher(segment.toString());
        if (matcher.find()) {
            start = matcher.start() + startIndex;
            end = matcher.end() + startIndex;
            select(start + 1, end + 1);
        } else {
            return -1;
        }
        return end;
    }

    public boolean hasCommand(Object command) {
        return targetSupport.hasCommand(command);
    }

    public Object[] getCommands() {
        return targetSupport.getCommands();
    }

    public boolean doCommand(Object command, Object value) {
        return targetSupport.doCommand(command, value);
    }

    /**
     * Listens to the caret placement and adjusts the editing
     * properties as appropriate.
     *
     * Should add more attributes as required.
     */
    private class CaretHandler implements CaretListener {
        public void caretUpdate(CaretEvent evt) {
            StyledDocument document = (StyledDocument) getDocument();
            int dot = evt.getDot();
            Element elem = document.getCharacterElement(dot);
            AttributeSet set = elem.getAttributes();

            ActionManager manager = ActionManager.getInstance();
            manager.setSelected("font-bold", StyleConstants.isBold(set));
            manager.setSelected("font-italic", StyleConstants.isItalic(set));
            manager.setSelected("font-underline", StyleConstants.isUnderline(set));

            elem = document.getParagraphElement(dot);
            set = elem.getAttributes();

            // Update the paragraph selector if applicable.
            if (selector != null) {
                selector.setSelectedItem(set.getAttribute(StyleConstants.NameAttribute));
            }

            switch (StyleConstants.getAlignment(set)) {
                // XXX There is a bug here. the setSelected method
                // should only affect the UI actions rather than propagate
                // down into the action map actions.
                case StyleConstants.ALIGN_LEFT:
                    manager.setSelected("left-justify", true);
                    break;

                case StyleConstants.ALIGN_CENTER:
                    manager.setSelected("center-justify", true);
                    break;

                case StyleConstants.ALIGN_RIGHT:
                    manager.setSelected("right-justify", true);
                    break;
            }
        }
    }

    /**
     * Handles sloppy HTML. This implementation currently only looks for
     * tags that have a / at the end (self-closing tags) and fixes them
     * to work with the version of HTML supported by HTMLEditorKit
     * <p>TODO: Need to break this functionality out so it can take pluggable
     * replacement code blocks, allowing people to write custom replacement
     * routines. The idea is that with some simple modifications a lot more
     * sloppy HTML can be rendered correctly.
     *
     * @author rbair
     */
    private static final class SloppyHTMLEditorKit extends HTMLEditorKit {
        public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
            //read the reader into a String
            StringBuffer buffer = new StringBuffer();
            int length = -1;
            char[] data = new char[1024];
            while ((length = in.read(data)) != -1) {
                buffer.append(data, 0, length);
            }
            //TODO is this regex right?
            StringReader reader = new StringReader(buffer.toString().replaceAll("/>", ">"));
            super.read(reader, doc, pos);
        }
    }

    public static void main(String[] args) {
        JXEditorPane editor = new JXEditorPane();
        //editor.setEditable(false);
        editor.setPreferredSize(new Dimension(600, 400));
        Action[] actions = editor.getActions();
        ActionManager manager = ActionManager.getInstance();
        List<Object> actionNames = new ArrayList<Object>();
        StringBuffer buffer = new StringBuffer("No. of default actions: " + actions.length);
        ActionMap map = editor.getActionMap();
        Object[] keys = map.keys();
        int count = keys != null ? keys.length : 0;
        buffer.append("\n No. of actions in ActionMap: " + count);
        for (int i = 0; i < actions.length; i++) {
            // TODO: are names allowed to be anything else as String?
            // same question in other test methods as well
            Object id = actions[i].getValue(Action.NAME);
            // ?? the id in the actionManager is doc'ed as ACTION_COMMAND?
            // which would imply to be a String (assumption somewhere in core)
            manager.addAction(id, actions[i]);
            actionNames.add(id);
            buffer.append("\n" + actions[i].toString());
        }


        editor.setText(buffer.toString());
        ActionContainerFactory factory = new ActionContainerFactory(manager);

        JToolBar toolbar = factory.createToolBar(actionNames);
        toolbar.setOrientation(JToolBar.VERTICAL);
        editor.setEditable(false);
        editor.setPreferredSize(new Dimension(600, 400));
//      JXList list = new JXList(true);
//      list.setModel(createListModel(actionNames));
        JFrame frame = new JFrame();
        frame.getContentPane().add(toolbar, BorderLayout.WEST);
        frame.getContentPane().add(BorderLayout.CENTER, new JScrollPane(editor));
        frame.pack();
        frame.setLocation(0,0);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
