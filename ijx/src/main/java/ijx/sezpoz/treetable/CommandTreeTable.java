package ijx.sezpoz.treetable;

/*
 * Copyright 1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer. 
 *   
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution. 
 *   
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.  
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,   
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS 
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

import ijx.sezpoz.CommandsManager;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.DateFormat;

/**
 * Assembles the UI. The UI consists of a JTreeTable and a menu.
 * The JTreeTable uses a BookmarksModel to visually represent a bookmarks
 * file stored in the Netscape file format.
 *
 * @author Scott Violet
 */
public class CommandTreeTable {
    /** Number of instances of CommandTreeTable. */
    private static int ttCount;
    /** Used to represent the model. */
    private JTreeTable treeTable;
    /** Frame containing everything. */
    private JFrame frame;
    /** Path created for. */
    private String path;

    /**
     * Creates a CommandTreeTable, loading the bookmarks from the file
     * at <code>path</code>.
     */
    public CommandTreeTable(String path) {
        this.path = path;
        ttCount++;

        frame = createFrame();

        Container cPane = frame.getContentPane();
        JMenuBar mb = createMenuBar();
        TreeTableModel model = createModel(path);

        treeTable = createTreeTable(model);
        JScrollPane sp = new JScrollPane(treeTable);
        sp.getViewport().setBackground(Color.white);
        cPane.add(sp);

        frame.setJMenuBar(mb);
        frame.pack();
        frame.show();
    }

    /**
     * Creates and returns the instanceof JTreeTable that will be used.
     */
    protected JTreeTable createTreeTable(TreeTableModel model) {
        JTreeTable treeTable = new JTreeTable(model);

        treeTable.setDefaultRenderer(Date.class, new DateRenderer());
        treeTable.setDefaultRenderer(Object.class, new StringRenderer());
        return treeTable;
    }

    /**
     * Creates the BookmarksModel for the file at <code>path</code>.
     */
//    protected TreeTableModel createModel(String path) {
//        Bookmarks bookmarks = new Bookmarks(path);
//        return new BookmarksModel(bookmarks.getRoot());
//    }
    protected TreeTableModel createModel(String path) {
        Commands commands = new Commands();
        return new CommandsModel(commands.getRoot());
    }

    /**
     * Creates the JFrame that will contain everything.
     */
    protected JFrame createFrame() {
        JFrame retFrame = new JFrame("Commands -- " + path);

        retFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                frame.dispose();
                if (--ttCount == 0) {
                    System.exit(0);
                }
            }
        });
        return retFrame;
    }

    /**
     * Creates a menu bar.
     */
    protected JMenuBar createMenuBar() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem menuItem;

        menuItem = new JMenuItem("Open");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JFileChooser fc = new JFileChooser(path);
                int result = fc.showOpenDialog(frame);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String newPath = fc.getSelectedFile().getPath();

                    new CommandTreeTable(newPath);
                }
            }
        });
        fileMenu.add(menuItem);
        fileMenu.addSeparator();

        menuItem = new JMenuItem("Exit");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        });
        fileMenu.add(menuItem);


        // Create a menu bar
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(fileMenu);

        // Menu for the look and feels (lafs).
        UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        ButtonGroup lafGroup = new ButtonGroup();

        JMenu optionsMenu = new JMenu("Options");

        menuBar.add(optionsMenu);

        for (int i = 0; i < lafs.length; i++) {
            JRadioButtonMenuItem rb = new JRadioButtonMenuItem(lafs[i].getName());
            optionsMenu.add(rb);
            rb.setSelected(UIManager.getLookAndFeel().getName().equals(lafs[i].getName()));
            rb.putClientProperty("UIKey", lafs[i]);
            rb.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent ae) {
                    JRadioButtonMenuItem rb2 = (JRadioButtonMenuItem) ae.getSource();
                    if (rb2.isSelected()) {
                        UIManager.LookAndFeelInfo info =
                                (UIManager.LookAndFeelInfo) rb2.getClientProperty("UIKey");
                        try {
                            UIManager.setLookAndFeel(info.getClassName());
                            SwingUtilities.updateComponentTreeUI(frame);
                        } catch (Exception e) {
                            System.err.println("unable to set UI "
                                    + e.getMessage());
                        }
                    }
                }
            });
            lafGroup.add(rb);
        }
        return menuBar;
    }

    /**
     * The renderer used for Dates in the TreeTable. The only thing it does,
     * is to format a null date as '---'.
     */
    private static class DateRenderer extends DefaultTableCellRenderer {
        DateFormat formatter;

        public DateRenderer() {
            super();
        }

        public void setValue(Object value) {
            if (formatter == null) {
                formatter = DateFormat.getDateInstance();
            }
            setText((value == null) ? "---" : formatter.format(value));
        }
    }

    /**
     * The renderer used for String in the TreeTable. The only thing it does,
     * is to format a null String as '---'.
     */
    static class StringRenderer extends DefaultTableCellRenderer {
        public StringRenderer() {
            super();
        }

        public void setValue(Object value) {
            setText((value == null) ? "---" : value.toString());
        }
    }



    public static void main(String[] args) {
        if (args.length > 0) {
            // User is specifying the bookmark file to show.
            for (int counter = args.length - 1; counter >= 0; counter--) {
                new CommandTreeTable(args[counter]);
            }
        } else {
            // No file specified, see if the user has one in their home
            // directory.
            String path;

            try {
                path = System.getProperty("user.home");
                if (path != null) {
                    path += File.separator + ".netscape" + File.separator
                            + "bookmarks.html";
                    File file = new File(path);
                    if (!file.exists()) {
                        path = null;
                    }
                }
            } catch (Throwable th) {
                path = null;
            }
            if (path == null) {
                // None available, use a default.
                path = "bookmarks.html";
            }
            new CommandTreeTable(path);

        }
    }
}
