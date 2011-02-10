/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.sandbox.explorer;

import java.io.File;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

public final class FileNode extends AbstractNode {

    private File file;

    private FileNode(File f) {
        super(new FileKids(f));
        file = f;
        setName(f.getName());
    }

    public static Node files() {
        AbstractNode n = new AbstractNode(new FileKids(null));
        n.setName("Root");
        return n;
    }

    private static final class FileKids extends Children.Keys<File> {
        File file;

        public FileKids(File file) {
            this.file = file;
        }

        @Override
        protected void addNotify() {
            if (file == null) {
                File[] arr = File.listRoots();
                if (arr.length == 1) {
                    arr = arr[0].listFiles();
                }
                setKeys(arr);
            } else {
                File[] arr = file.listFiles();
                if (arr != null) {
                    setKeys(arr);
                }
            }
        }

        @Override
        protected Node[] createNodes(File f) {
            FileNode n = new FileNode(f);
            return new Node[] { n };
        }


    }

}