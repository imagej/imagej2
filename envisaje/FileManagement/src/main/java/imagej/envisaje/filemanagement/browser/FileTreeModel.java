
package imagej.envisaje.filemanagement.browser;

import java.io.File;
import java.util.Arrays;
import javax.swing.tree.TreeModel;

public class FileTreeModel implements TreeModel {

    private File root;

    public FileTreeModel(File root) {
        this.root = root;
    }

    @Override
    public void addTreeModelListener(javax.swing.event.TreeModelListener l) {
        //do nothing
    }

    @Override
    public Object getChild(Object parent, int index) {
        File f = (File) parent;
        return f.listFiles()[index];
    }

    @Override
    public int getChildCount(Object parent) {
        File f = (File) parent;
        if (!f.isDirectory()) {
            return 0;
        } else {
            return f.list().length;
        }
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        File par = (File) parent;
        File ch = (File) child;
        return Arrays.asList(par.listFiles()).indexOf(ch);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public boolean isLeaf(Object node) {
        File f = (File) node;
        return !f.isDirectory();
    }

    @Override
    public void removeTreeModelListener(javax.swing.event.TreeModelListener l) {
        //do nothing
    }

    @Override
    public void valueForPathChanged(javax.swing.tree.TreePath path, Object newValue) {
        //do nothing
    }

}