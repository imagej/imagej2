/*
 * DropShadowBorderPanel.java
 *
 * Created on April 28, 2005, 10:27 AM
 */
package ijx.action;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

/**
 *
 * @author  rbair
 */
public class ActionDemoPanel_1 extends JFrame {
    private List actions = new ArrayList();
    private List toolbarActions = new ArrayList();
    private ActionManager manager;
    // private DropShadowBorder dsb = new DropShadowBorder();

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //MainWindow.main(new String[]{"-d", "org.jdesktop.demo.swingx.ActionDemoPanel"});
        new ActionDemoPanel_1();
    }

    public void init() {
        java.awt.GridBagConstraints gridBagConstraints;


        menuPanel = new javax.swing.JPanel();
        toolBar = new ActionContainerFactory(manager).createToolBar(toolbarActions);
        jScrollPane1 = new javax.swing.JScrollPane();

        setLayout(new java.awt.GridBagLayout());

        menuPanel.setLayout(new java.awt.BorderLayout());

        toolBar.setBorder(null);
        menuPanel.add(toolBar, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        //add(menuPanel, gridBagConstraints);
        add(menuPanel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        //add(jScrollPane1, gridBagConstraints);


    }

    private final class AntiAliasAction extends AbstractActionExt {
        public AntiAliasAction(String text, String commandId, Icon icon) {
            super(text, commandId, icon);
            super.setStateAction(true);
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            //do nothing
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
//            switch (e.getStateChange()) {
//                case ItemEvent.SELECTED:
//                    editorPane.putClientProperty(SwingUtilities2.AA_TEXT_PROPERTY_KEY, Boolean.TRUE);
//                    editorPane.repaint();
//                    break;
//                case ItemEvent.DESELECTED:
//                    editorPane.putClientProperty(SwingUtilities2.AA_TEXT_PROPERTY_KEY, Boolean.FALSE);
//                    editorPane.repaint();
//                    break;
//            }
        }
    }

    public ActionDemoPanel_1() {

        super.setTitle("Action Demo");

        //just using the same icon for now
        //ImageIcon icon = new ImageIcon(getClass().getResource("resources/gear.png"));

        manager = ActionManager.getInstance();

//        AbstractActionExt[] actions = new AbstractActionExt[14];
//        List<AbstractActionExt> actions = new ArrayList<AbstractActionExt>();
//        actions.add(ActionFactory.createTargetableAction("insert-break", "LF", "F"));
//
//        actions.add(ActionFactory.createTargetableAction("left-justify", "Left", "L", true,
//                                                          "position-group"));
//        actions.add(ActionFactory.createTargetableAction("center-justify", "Center", "C", true,
//                                                          "position-group"));
//        actions.add(ActionFactory.createTargetableAction("right-justify", "Right", "R", true,
//                                                          "position-group"));
//
//        actions.add(ActionFactory.createTargetableAction("font-bold", "Bold", "B", true));
//        actions.add(ActionFactory.createTargetableAction("font-italic", "Italic", "I", true));
//        actions.add(ActionFactory.createTargetableAction("font-underline", "Underline", "U", true));
//
//        actions.add(ActionFactory.createTargetableAction("InsertUnorderedList", "UL", "U", true));
//        actions.add(ActionFactory.createTargetableAction("InsertOrderedList", "OL", "O", true));
//        actions.add(ActionFactory.createTargetableAction("InsertHR", "HR", "H"));
//        ActionManager manager = ActionManager.getInstance();
//
//        List<Object> actionNames = new ArrayList<Object>();
//        for (AbstractActionExt ext : actions) {
//            manager.addAction(ext);
//            actionNames.add(ext.getActionCommand());
//
//        }
//
//        // Populate the toolbar. Must use the ActionContainerFactory to ensure
//        // that toggle actions are supported.
//        ActionContainerFactory factory = new ActionContainerFactory(manager);
//
//        toolbar = factory.createToolBar(actionNames);
//        add(toolbar, BorderLayout.NORTH);

        //manager.addAction(new BoundAction("File", "fileMenu"));
        //manager.addAction(new BoundAction("Text", "textMenu"));
        manager.addAction(new BoundAction("Save", "save"));//, loadIcon("/toolbarButtonGraphics/general/Save16.gif")));
        manager.addAction(new BoundAction("Print", "print"));//, loadIcon("/toolbarButtonGraphics/general/Print16.gif")));
        manager.addAction(new TargetableAction("Cut", "cut"));//, loadIcon("/toolbarButtonGraphics/general/Cut16.gif")));
        manager.addAction(new TargetableAction("Copy", "copy"));//, loadIcon("/toolbarButtonGraphics/general/Copy16.gif")));
        manager.addAction(new TargetableAction("Paste", "paste"));//, loadIcon("/toolbarButtonGraphics/general/Paste16.gif")));
        manager.addAction(new TargetableAction("Undo", "undo"));//, loadIcon("/toolbarButtonGraphics/general/Undo16.gif")));
        manager.addAction(new TargetableAction("Redo", "redo"));//, loadIcon("/toolbarButtonGraphics/general/Redo16.gif")));
        manager.addAction(new TargetableAction("Find", "find"));//, loadIcon("/toolbarButtonGraphics/general/Search16.gif")));
        //manager.addAction(new BoundAction("Align", "alignMenu"));
        manager.addAction(new TargetableAction("Left Align", "left-justify"));//, loadIcon("/toolbarButtonGraphics/text/AlignLeft16.gif")));
        manager.addAction(new TargetableAction("Center Align", "center-justify"));//, loadIcon("/toolbarButtonGraphics/text/AlignCenter16.gif")));
        manager.addAction(new TargetableAction("Right Align", "right-justify"));//, loadIcon("/toolbarButtonGraphics/text/AlignRight16.gif")));
        manager.addAction(new TargetableAction("Bold", "font-bold"));//, loadIcon("/toolbarButtonGraphics/text/Bold16.gif")));
        manager.addAction(new TargetableAction("Italic", "font-italic"));//, loadIcon("/toolbarButtonGraphics/text/Italic16.gif")));
        manager.addAction(new TargetableAction("Normal", "font-normal"));//, loadIcon("/toolbarButtonGraphics/text/Normal16.gif")));
        manager.addAction(new TargetableAction("Underline", "font-underline"));//, loadIcon("/toolbarButtonGraphics/text/Underline16.gif")));
        //manager.addAction(new AntiAliasAction("Anti-Alias", "aa"));//, icon));



        ArrayList fileMenu = addTopLevelMenu("File", "fileMenu");
        fileMenu.add("save");
        fileMenu.add("print");

//        List fileMenu = new ArrayList();
//        fileMenu.add("fileMenu");
//        fileMenu.add("save");
//        fileMenu.add("print");
//        actions.add(fileMenu);
        //
        ArrayList textMenu = addTopLevelMenu("Text", "textMenu");
        textMenu.add("cut");
        //actions.add("cut");
        textMenu.add("copy");
        textMenu.add("paste");
        textMenu.add("undo");
        textMenu.add("redo");
        textMenu.add("find");
        textMenu.add(null);

        ArrayList alignMenu = addSubMenu("Align", "alignMenu", textMenu);
        alignMenu.add("left-justify");
        alignMenu.add("center-justify");
        alignMenu.add("right-justify");
        textMenu.add(null);
        ArrayList styleMenu = addSubMenu("Style", "styleMenu", alignMenu);

        styleMenu.add("font-bold");
        styleMenu.add("font-italic");
        styleMenu.add("font-underline");
        styleMenu.add("font-normal");
        textMenu.add(null);
        textMenu.add("aa");

        ((AbstractActionExt) manager.getAction("left-justify")).setGroup("alignment");
//        ((AbstractActionExt)manager.getAction("left-justify")).setStateAction(true);
        ((AbstractActionExt) manager.getAction("center-justify")).setGroup("alignment");
//        ((AbstractActionExt)manager.getAction("center-justify")).setStateAction(true);
        ((AbstractActionExt) manager.getAction("right-justify")).setGroup("alignment");
//        ((AbstractActionExt)manager.getAction("right-justify")).setStateAction(true);

        ((AbstractActionExt) manager.getAction("font-bold")).setStateAction(true);
        ((AbstractActionExt) manager.getAction("font-italic")).setStateAction(true);
        ((AbstractActionExt) manager.getAction("font-underline")).setStateAction(true);
        ((AbstractActionExt) manager.getAction("font-normal")).setStateAction(true);

        toolbarActions.add("left-justify");
        toolbarActions.add("center-justify");
        toolbarActions.add("right-justify");

        toolbarActions.add(null);
        toolbarActions.add("font-bold");
        toolbarActions.add("font-italic");
        toolbarActions.add("font-underline");
        toolbarActions.add("font-normal");
        //toolbarActions.addAll(actions);
//        toolbarActions.remove(fileMenu);
//        toolbarActions.remove(textMenu);
//        toolbarActions.add(2, null);

//        dsb.setShadowSize(5);
//        dsb.setShowRightShadow(false);


//        menuPanel.setBorder(dsb);
        init();

        //toolBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
        toolBar.setBorderPainted(false);
        toolBar.setMargin(new Insets(0, 0, 0, 0));
        toolBar.setBorder(null);

        TargetManager targetManager = TargetManager.getInstance();
        //targetManager.addTarget(editorPane);

        JMenuBar menuBar = new ActionContainerFactory(manager).createMenuBar(actions);
        menuBar.setBorderPainted(false);
        //menuBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
        menuPanel.add(menuBar, BorderLayout.NORTH);
        this.pack();
        this.setVisible(true);
    }


    ArrayList addTopLevelMenu(String label, String menuKey) {
        manager.addAction(new BoundAction(label, menuKey));
        ArrayList thisMenu = new ArrayList();
        thisMenu.add(menuKey);
        actions.add(thisMenu);
        return thisMenu;
    }
    ArrayList addSubMenu(String label, String menuKey, ArrayList parentMenu) {
        manager.addAction(new BoundAction(label, menuKey));
        ArrayList thisMenu = new ArrayList();
        thisMenu.add(menuKey);
        parentMenu.add(thisMenu);
        return thisMenu;
    }

    // Hold on to the Recently Opened and Windows menus for modification

    private Icon loadIcon(String path) {
        return new ImageIcon(getClass().getResource(path));
    }

    public String getHtmlDescription() {
        return "<html>Demonstrates various uses of the Action framework.</html>";
    }

    public String getName() {
        return "Action Framework";
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel menuPanel;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables
}
