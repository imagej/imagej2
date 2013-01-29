/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ui.swing.commands.debug;

import imagej.event.EventDetails;
import imagej.event.EventHistory;
import imagej.event.ImageJEvent;
import imagej.log.LogService;
import imagej.util.IteratorPlus;
import imagej.util.swing.tree.CheckBoxNodeData;
import imagej.util.swing.tree.CheckBoxNodeEditor;
import imagej.util.swing.tree.CheckBoxNodeRenderer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Swing-specific window for event watcher plugin.
 * 
 * @author Curtis Rueden
 */
public class WatchEventsFrame extends JFrame implements ActionListener,
	TreeModelListener, TreeSelectionListener
{

	private final EventHistory eventHistory;
	private final LogService log;

	/** Data structure storing event types that should be filtered out. */
	private final HashSet<Class<? extends ImageJEvent>> filtered =
		new HashSet<Class<? extends ImageJEvent>>();

	/** Data structure storing event types that should be highlighted in bold. */
	private final HashSet<Class<? extends ImageJEvent>> selected =
		new HashSet<Class<? extends ImageJEvent>>();

	private final DefaultTreeModel treeModel;
	private final DefaultMutableTreeNode root;
	private final JTree tree;

	private final JTextPane textPane;
	private final HTMLEditorKit kit;
	private final HTMLDocument doc;

	// -- Constructor --

	public WatchEventsFrame(final EventHistory eventHistory, final LogService log)
	{
		super("Event Watcher");
		this.eventHistory = eventHistory;
		this.log = log;

		// create tree
		root = create(ImageJEvent.class);
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setCellRenderer(new CheckBoxNodeRenderer());
		tree.setCellEditor(new CheckBoxNodeEditor(tree));
		tree.setEditable(true);
		tree.setShowsRootHandles(true);
		tree.addTreeSelectionListener(this);
		treeModel.addTreeModelListener(this);

		// create text pane
		textPane = new JTextPane();
		kit = new HTMLEditorKit();
		doc = new HTMLDocument();
		textPane.setEditorKit(kit);
		textPane.setDocument(doc);
		textPane.setEditable(false);

		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setResizeWeight(0.35);
		splitPane.add(new JScrollPane(tree));
		splitPane.add(new JScrollPane(textPane));

		// create clear history button
		final JButton clearHistory = new JButton("Clear History");
		clearHistory.setActionCommand("clearHistory");
		clearHistory.addActionListener(this);

		final JPanel buttonBar = new JPanel();
		buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.X_AXIS));
		buttonBar.add(Box.createHorizontalGlue());
		buttonBar.add(clearHistory);

		final JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);
		contentPane.add(splitPane, BorderLayout.CENTER);
		contentPane.add(buttonBar, BorderLayout.SOUTH);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		setSize(1000, 700);
	}

	// -- WatchEventsFrame methods --

	/** Appends the given event details to the text pane. Efficient. */
	public void append(final EventDetails details) {
		final Class<? extends ImageJEvent> eventType = details.getEventType();
		final DefaultMutableTreeNode node = findOrCreate(eventType);
		if (!isChecked(node)) return; // skip disabled event types
		append(details.toHTML(selected.contains(eventType)));
	}

	/** Refreshes the tree to match the state of its model. */
	public void refreshTree() {
		treeModel.reload();
		// TODO: retain previously expanded nodes only
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	/** Resets the text to match the current event history. Expensive. */
	public void refreshLog() {
		final String html = eventHistory.toHTML(filtered, selected);
		setText(html);
	}

	/** Appends the given HTML text string to the text pane. Efficient. */
	public void append(final String text) {
		try {
			kit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
		}
		catch (final BadLocationException e) {
			log.error(e);
		}
		catch (final IOException e) {
			log.error(e);
		}
		scrollToBottom();
	}

	/** Resets the text pane to the given HTML text. Expensive. */
	public void setText(final String text) {
		textPane.setText(text);
		scrollToBottom();
	}

	/** Clears the text pane. */
	public void clear() {
		textPane.setText("");
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		final String cmd = e.getActionCommand();
		if ("clearHistory".equals(cmd)) {
			eventHistory.clear();
			clear();
		}
	}

	// -- TreeModelListener methods --

	@Override
	public void treeNodesChanged(final TreeModelEvent e) {
		// recursively toggle the subtree to match
		Object[] children = e.getChildren();
		if (children == null) children = new Object[] { root };
		boolean anyChanged = false;
		for (final Object child : children) {
			if (!(child instanceof DefaultMutableTreeNode)) continue;
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) child;
			final CheckBoxNodeData data = getData(node);
			if (data == null) continue;
			final boolean changed = toggle(node, data.isChecked());
			if (changed) anyChanged = true;
		}

		// refresh the tree to match the new state of the model
		if (anyChanged) refreshTree();

		// re-filter the log pane
		syncFiltered();
		refreshLog();
	}

	@Override
	public void treeNodesInserted(final TreeModelEvent e) {
		// NB: No implementation needed.
	}

	@Override
	public void treeNodesRemoved(final TreeModelEvent e) {
		// NB: No implementation needed.
	}

	@Override
	public void treeStructureChanged(final TreeModelEvent e) {
		// NB: No implementation needed.
	}

	// -- TreeSelectionListener methods --

	@Override
	public void valueChanged(final TreeSelectionEvent e) {
		syncSelected();
		refreshLog();
	}

	// -- Helper methods --

	/** Populates the {@link #filtered} set to match the current tree state. */
	private void syncFiltered() {
		filtered.clear();
		syncFiltered(root);
	}

	/** Recursively populates the {@link #filtered} set to match the tree. */
	private void syncFiltered(final DefaultMutableTreeNode node) {
		if (!isChecked(node)) filtered.add(getEventType(node));

		for (final DefaultMutableTreeNode child : children(node)) {
			syncFiltered(child);
		}
	}

	/** Populates the {@link #selected} set to match the current tree state. */
	private void syncSelected() {
		selected.clear();
		final TreePath[] paths = tree.getSelectionPaths();
		if (paths == null) return;
		for (final TreePath path : paths) {
			final DefaultMutableTreeNode node =
				(DefaultMutableTreeNode) path.getLastPathComponent();
			select(node);
		}
	}

	/** Gets a tree node for the given type of event, creating it if necessary. */
	private DefaultMutableTreeNode findOrCreate(
		final Class<? extends ImageJEvent> eventType)
	{
		if (eventType == null) return null;
		if (eventType == ImageJEvent.class) {
			return root;
		}
		@SuppressWarnings("unchecked")
		final Class<? extends ImageJEvent> superclass =
			(Class<? extends ImageJEvent>) eventType.getSuperclass();
		final DefaultMutableTreeNode parentNode = findOrCreate(superclass);

		for (final DefaultMutableTreeNode child : children(parentNode)) {
			if (getEventType(child) == eventType) {
				// found existing event type in the tree
				return child;
			}
		}

		// event type is new; add it to the tree
		final DefaultMutableTreeNode node = create(eventType);
		parentNode.add(node);

		// refresh the tree
		refreshTree();
		tree.scrollPathToVisible(new TreePath(node.getPath()));

		return node;
	}

	/** Creates a tree node for the given type of event. */
	private DefaultMutableTreeNode create(
		final Class<? extends ImageJEvent> eventType)
	{
		final String label = eventType.getName();
		final CheckBoxNodeData data = new CheckBoxNodeData(label, true);
		final DefaultMutableTreeNode node = new DefaultMutableTreeNode(data);
		return node;
	}

	/** Makes sure the last line of text is always visible. */
	private void scrollToBottom() {
		textPane.setCaretPosition(textPane.getDocument().getLength());
	}

	/** Recursively marks the given node, and all its children, as selected. */
	private void select(final DefaultMutableTreeNode node) {
		selected.add(getEventType(node));
		for (final DefaultMutableTreeNode child : children(node)) {
			select(child);
		}
	}

	/** Extracts the event type associated with a given tree node. */
	private Class<? extends ImageJEvent> getEventType(
		final DefaultMutableTreeNode node)
	{
		final CheckBoxNodeData data = getData(node);
		if (data == null) return null;
		final String className = data.getText();
		try {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			final Class<? extends ImageJEvent> eventType =
				(Class) Class.forName(className);
			return eventType;
		}
		catch (final ClassNotFoundException exc) {
			log.error(exc);
			throw new IllegalStateException("Invalid class: " + className);
		}
	}

	/** Extracts the check box state of a given tree node. */
	private boolean isChecked(final DefaultMutableTreeNode node) {
		final CheckBoxNodeData data = getData(node);
		if (data == null) return false;
		return data.isChecked();
	}

	/**
	 * Extracts the check box node data form the given object. Returns null if the
	 * given object is not a tree node, the node is not of the proper type, or the
	 * node has no check box node data.
	 */
	private CheckBoxNodeData getData(final DefaultMutableTreeNode node) {
		final Object userObject = node.getUserObject();
		if (!(userObject instanceof CheckBoxNodeData)) return null;
		return (CheckBoxNodeData) userObject;
	}

	/** Recursively toggles the check box state of the given subtree. */
	private boolean toggle(final DefaultMutableTreeNode node,
		final boolean checked)
	{
		final CheckBoxNodeData data = getData(node);
		boolean anyChanged = false;
		if (data != null) {
			if (data.isChecked() != checked) {
				data.setChecked(checked);
				anyChanged = true;
			}
		}
		for (final DefaultMutableTreeNode child : children(node)) {
			final boolean changed = toggle(child, checked);
			if (changed) anyChanged = true;
		}
		return anyChanged;
	}

	private Iterable<DefaultMutableTreeNode> children(
		final DefaultMutableTreeNode node)
	{
		@SuppressWarnings("unchecked")
		final Enumeration<DefaultMutableTreeNode> en = node.children();
		return new IteratorPlus<DefaultMutableTreeNode>(en);
	}

}
