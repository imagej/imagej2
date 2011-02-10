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
import ijx.sezpoz.ActionIjx;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import net.java.sezpoz.IndexItem;

/**
 * Parses the Netscape bookmarks file (NETSCAPE-Bookmark-file-1) into
 * BookmarkEntries's and BookmarkDirectories.
 * <p>
 * For the time being, this is a hack, there needs to be a more generic
 * API to this to allow adding/removing bookmarks and pulling from 
 * different sources other than just Netscape. But for this example, this is
 * plenty good.
 * <p>While a hack, this is interesting in that it shows how you can use
 * the parser provided in the javax.swing.text.html.parser package outside
 * of the HTML package. The netscape format is a pseudo HTML file, pseudo
 * in that there is no head/body. All the bookmarks are presented as
 * DT's in a DL and the name of the directory is a DT. An instance of the
 * parser is created, a callback is registered, and as the parser parses
 * the file, nodes of the correct type are created.
 *
 * @author Scott Violet
 */
public class Commands {
    /** The root node the bookmarks are added to. */
    private CatagoryNode root;
    private CatagoryNode parent;
    private CommandEntry lastCommand;

    /**
     * Creates a new Bookmarks object, with the entries coming from
     * <code>path</code>.
     */
    public Commands() {
        root = new CatagoryNode("MenuBar");
        parent = root;
        lastCommand = new CommandEntry();
    }

    public CatagoryNode getRoot() {
        return root;
    }

    public void createTreeFromItems(Map<String, IndexItem<ActionIjx, ?>> items) {
    }

    public void addCommandEntry(String name) {
        lastCommand.setName(new String(name));
        parent.add(lastCommand);
        lastCommand = new CommandEntry();
    }

    public void addCommandCatagory(String name) {
        CatagoryNode newParent = new CatagoryNode(new String(name));
        //newParent.setCreated(parentDate);
        parent.add(newParent);
        parent = newParent;
    }

    /**
     * CatagoryNode represents a directory containing other
     * CatagoryNode's as well as CommandEntry's. It adds a name
     * and created property to DefaultMutableTreeNode.
     */
    public static class CatagoryNode extends DefaultMutableTreeNode {
        /** Dates created. */
        private Date created;

        public CatagoryNode(String name) {
            super(name);
        }

        public void setName(String name) {
            setUserObject(name);
        }

        public String getName() {
            return (String) getUserObject();
        }
//        public void setCreated(Date date) {
//            this.created = date;
//        }
//
//        public Date getCreated() {
//            return created;
//        }
    }

    /**
     * CommandEntry represents a command.
     */
    public static class CommandEntry extends DefaultMutableTreeNode {
        /** User description of the string. */
        private String name;
        /** The URL the bookmark represents. */
        private String commandKey;
        /** Dates the URL was last visited. */
        private Date lastUpdated;
        /** Date the URL was created. */
        private Date created;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setCommandKey(String commandKey) {
            this.commandKey = commandKey;
        }

        public String getCommandKey() {
            return commandKey;
        }

        public void setLastUpdated(Date date) {
            lastUpdated = date;
        }

        public Date getLastUpdated() {
            return lastUpdated;
        }

        public void setCreated(Date date) {
            this.created = date;
        }

        public Date getCreated() {
            return created;
        }

        public String toString() {
            return getName();
        }
    }
}
