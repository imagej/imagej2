/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.paintui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 * Tool for getting a file chooser that remembers the last dir it was invoked
 * against and presents a list of known recent directories.
 *
 * @author Tim Boudreau
 */
public class FileChooserUtils {
    private FileChooserUtils(){}
    
    /**
     * Get a file chooser for the specified context.  Recent files and last
     * selection are specific to the context.
     * 
     * @param dirKey An ad-hoc name for the context.
     * @return A file chooser
     */
    public static JFileChooser getFileChooser (final String dirKey) {
        final A a = new A(dirKey);
        JFileChooser result = new JFileChooser() {

            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                JDialog result = super.createDialog(parent);
                result.getContentPane().add (createRecentFoldersPanel(dirKey, 
                        this, getCurrentDirectory().getPath()), 
                        BorderLayout.NORTH);
                return result;
            }

            @Override
            public int showOpenDialog(Component parent) throws HeadlessException {
                int result = super.showOpenDialog(parent);
                if (result == APPROVE_OPTION) {
                    a.actionPerformed (new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
                }
                return result;
            }
            
            @Override
            public int showSaveDialog(Component parent) throws HeadlessException {
                int result = super.showSaveDialog(parent);
                if (result == APPROVE_OPTION) {
                    a.actionPerformed (new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
                }
                return result;
            }
        };
        String path = NbPreferences.forModule(FileChooserUtils.class).get(dirKey, null);
        if (path != null) {
            File f = new File (path);
            if (f.exists() && f.isDirectory()) {
                result.setCurrentDirectory(f);
            }
        }
        return result;
    }
    
    private static String prune (String[] dirs) {
        Set<String> s = new HashSet<String>(Arrays.asList(dirs));
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> i=s.iterator(); i.hasNext();) {
            String dir = i.next();
            File f = new File (dir);
            if (f.exists() && f.isDirectory()) {
                sb.append (dir);
                if (i.hasNext()) {
                    sb.append (',');
                }
            }
        }
        return sb.toString();
    }
    
    
    private static final class A implements ActionListener {
        private final String dirKey;
        A(String dirKey) {
            this.dirKey = dirKey;
        }
        
        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = (JFileChooser) e.getSource();
            String path = jfc.getCurrentDirectory().getAbsolutePath();
            String s = NbPreferences.forModule(FileChooserUtils.class).get("recentDirs." + dirKey, ""); //NOI18N
            String[] dirs = prune (s.split(File.pathSeparator)).split(File.pathSeparator);
            boolean isWindows = Utilities.isWindows();
            for (String dir : dirs) {
                boolean equal = isWindows ? dir.equalsIgnoreCase(path) : dir.equals(path);
                if (equal) {
                    return;
                }
            }
            if (s.length() > 0) {
                s = s + File.pathSeparatorChar;
            }
            s += path;
            
            NbPreferences.forModule(FileChooserUtils.class).put(dirKey, path);
            NbPreferences.forModule(FileChooserUtils.class).put("recentDirs." + dirKey, s); //NOI18N
        }
    }
    
    private static JComponent createRecentFoldersPanel(final String dirKey, final JFileChooser jfc, String origSelection) {
        String s = NbPreferences.forModule(FileChooserUtils.class).get("recentDirs." + dirKey, ""); //NOI18N
        String[] dirs = s.split(File.pathSeparator);
        Set<String> set = new HashSet<String>(Arrays.asList(dirs));
        dirs = (String[]) set.toArray(new String[set.size()]);
        JPanel result = new JPanel();
        result.setLayout (new FlowLayout());
        result.add (new JLabel (NbBundle.getMessage(FileChooserUtils.class, "LBL_RECENT_DIRS"))); //NOI18N
        final JComboBox box = new JComboBox(dirs);
        box.setSelectedItem(origSelection);
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = (String) box.getSelectedItem();
                if (s != null) {
                    File f = new File (s);
                    if (f.exists() && f.isDirectory()) {
                        jfc.setCurrentDirectory(new File ((String) box.getSelectedItem()));
                    }
                }
            }
        });
        box.setPrototypeDisplayValue("1234512345123451234512345"); //NOI18N
        box.setRenderer(new Ren());
        result.add (box);
        return result;
    }
    
    private static final class Ren extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String s = value == null ? "" : value.toString();
            if (value != null && value.toString().length() > 25) {
                String trunc = "..." + s.substring(s.length() - 23);
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
