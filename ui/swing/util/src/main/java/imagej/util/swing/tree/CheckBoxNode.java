/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.util.swing.tree;

import imagej.util.IteratorEnumeration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;

/**
 * A tree node, for use with a {@link JTree}, that can track whether it is
 * selected using an associated check box.
 * <p>
 * Thanks to John Zukowski for the <a
 * href="http://www.java2s.com/Code/Java/Swing-JFC/CheckBoxNodeTreeSample.htm"
 * >sample code</a> upon which this is based.
 * </p>
 * 
 * @author Curtis Rueden
 * @see CheckBoxNodeEditor
 * @see CheckBoxNodeRenderer
 */
public class CheckBoxNode implements List<CheckBoxNode>, TreeNode {

	private String text;
	private Boolean selected;
	private CheckBoxNode parent;
	private final List<CheckBoxNode> children;

	/**
	 * Constructs a new check box node with the given text label and no check box.
	 * 
	 * @param text The text label.
	 */
	public CheckBoxNode(final String text) {
		this(text, null);
	}

	/**
	 * Constructs a new check box node with the given text label and selected
	 * status.
	 * 
	 * @param text The text label.
	 * @param selected The selected status. Use true for checked, false for
	 *          unchecked, and null to hide the checkbox.
	 */
	public CheckBoxNode(final String text, final Boolean selected) {
		this.text = text;
		this.selected = selected;
		this.children = new ArrayList<CheckBoxNode>();
	}

	public Boolean isSelected() {
		return selected;
	}

	public void setSelected(final Boolean newValue) {
		selected = newValue;
	}

	public String getText() {
		return text;
	}

	public void setText(final String newValue) {
		text = newValue;
	}

	// -- Object methods --

	@Override
	public String toString() {
		return getClass().getName() + "[" + text + "/" + selected + "]";
	}

	// -- List methods --

	@Override
	public boolean add(final CheckBoxNode e) {
		linkParent(e);
		return children.add(e);
	}

	@Override
	public void add(final int index, final CheckBoxNode element) {
		linkParent(element);
		children.add(index, element);
	}

	@Override
	public boolean addAll(final Collection<? extends CheckBoxNode> c) {
		linkParents(c);
		return children.addAll(c);
	}

	@Override
	public boolean addAll(final int index,
		final Collection<? extends CheckBoxNode> c)
	{
		linkParents(c);
		return children.addAll(index, c);
	}
	
	@Override
	public void clear() {
		children.clear();
	}

	@Override
	public boolean contains(final Object o) {
		return children.contains(o);
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return children.containsAll(c);
	}

	@Override
	public CheckBoxNode get(final int index) {
		return children.get(index);
	}

	@Override
	public int indexOf(final Object o) {
		return children.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return children.isEmpty();
	}

	@Override
	public Iterator<CheckBoxNode> iterator() {
		return children.iterator();
	}

	@Override
	public int lastIndexOf(final Object o) {
		return children.lastIndexOf(o);
	}

	@Override
	public ListIterator<CheckBoxNode> listIterator() {
		return children.listIterator();
	}

	@Override
	public ListIterator<CheckBoxNode> listIterator(final int index) {
		return children.listIterator(index);
	}

	@Override
	public boolean remove(final Object o) {
		return children.remove(o);
	}

	@Override
	public CheckBoxNode remove(final int index) {
		return children.remove(index);
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		return children.removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		return children.retainAll(c);
	}

	@Override
	public CheckBoxNode set(final int index, final CheckBoxNode element) {
		return children.set(index, element);
	}

	@Override
	public int size() {
		return children.size();
	}

	@Override
	public List<CheckBoxNode> subList(final int fromIndex, final int toIndex) {
		return children.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return children.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		return children.toArray(a);
	}

	// -- TreeNode methods --

	@Override
	public Enumeration<CheckBoxNode> children() {
		return new IteratorEnumeration<CheckBoxNode>(children.iterator());
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public TreeNode getChildAt(final int childIndex) {
		return get(childIndex);
	}

	@Override
	public int getChildCount() {
		return size();
	}

	@Override
	public int getIndex(final TreeNode node) {
		return indexOf(node);
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public boolean isLeaf() {
		return children.isEmpty();
	}

	// -- Helper methods --
	
	private void linkParent(final CheckBoxNode node) {
		checkParent(node);
		node.parent = this;
	}

	private void linkParents(final Collection<? extends CheckBoxNode> c) {
		for (final CheckBoxNode node : c) {
			checkParent(node);
		}
		// all nodes OK
		for (final CheckBoxNode node : c) {
			node.parent = this;
		}
	}

	private void checkParent(final CheckBoxNode node) {
		if (node.parent == null) return;
		throw new IllegalArgumentException("Node already has a parent " +
			"(node = " + node + ", parent = " + node.parent + ")");
	}

}
