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

package imagej.ext.menu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import imagej.ImageJ;
import imagej.ext.MenuPath;
import imagej.ext.module.DefaultModuleInfo;
import imagej.ext.module.ModuleInfo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Tests {@link ShadowMenu}.
 * 
 * @author Curtis Rueden
 */
public class ShadowMenuTest {

	/** Tests {@link ShadowMenu#addAll} and getters. */
	@Test
	public void testStructure() {
		final ShadowMenu root = createShadowMenu();
		checkStructure(root);
	}

	/** Tests {@link ShadowMenu#iterator()}. */
	@Test
	public void testIterator() {
		final ShadowMenu root = createShadowMenu();
		final ShadowMenuIterator iter = root.iterator();
		checkIter(iter, "Copy"); // Edit>Copy
		checkIter(iter, "Cut"); // Edit>Cut
		checkIter(iter, "Paste"); // Edit>Paste
		checkIter(iter, "Exit"); // File>Exit
		checkIter(iter, "Image"); // File>New>Image
		checkIter(iter, "Text Window"); // File>New>Text Window
		checkIter(iter, "Open"); // File>Open
		checkIter(iter, "Save"); // File>Save
		assertFalse(iter.hasNext());
	}

	/** Tests {@link ShadowMenu#add}. */
	@Test
	public void testAdd() {
		final ShadowMenu root = createShadowMenu();
		root.add(createModuleInfo("Edit>Clear"));

		// check Edit>Clear node
		final ShadowMenu edit = root.getChildren().get(0);
		final ShadowMenu editClear = edit.getChildren().get(0);
		checkNode(editClear, "Clear", 1, 1, 0);
	}

	/** Tests {@link ShadowMenu#remove}. */
	@Test
	public void testRemove() {
		final ShadowMenu root = createShadowMenu();

		// check that leaf item is properly removed
		final ModuleInfo createDoc = createModuleInfo("File>New>Document");
		root.add(createDoc);
		root.remove(createDoc);
		checkStructure(root);

		// check that empty submenus are trimmed correctly
		final ModuleInfo importData = createModuleInfo("File>Import>Data");
		root.add(importData);
		root.remove(importData);
		checkStructure(root);
	}

	// -- Helper methods --

	private ShadowMenu createShadowMenu() {
		@SuppressWarnings("unchecked")
		final ImageJ context = ImageJ.createContext(MenuService.class);
		final MenuService menuService = context.getService(MenuService.class);

		final ArrayList<ModuleInfo> modules = new ArrayList<ModuleInfo>();
		modules.add(createModuleInfo("Edit>Copy"));
		modules.add(createModuleInfo("Edit>Cut"));
		modules.add(createModuleInfo("Edit>Paste"));
		modules.add(createModuleInfo("File>Exit"));
		modules.add(createModuleInfo("File>New>Image"));
		modules.add(createModuleInfo("File>New>Text Window"));
		modules.add(createModuleInfo("File>Open"));
		modules.add(createModuleInfo("File>Save"));

		return new ShadowMenu(menuService, modules);
	}

	private ModuleInfo createModuleInfo(final String path) {
		final DefaultModuleInfo info = new DefaultModuleInfo();
		info.setMenuPath(new MenuPath(path));
		return info;
	}

	private void checkStructure(final ShadowMenu root) {
		assertNull(root.getParent());

		final List<ShadowMenu> rootChildren = checkNode(root, null, 8, -1, 2);

		final ShadowMenu edit = rootChildren.get(0);
		final List<ShadowMenu> editChildren = checkNode(edit, "Edit", 3, 0, 3);

		final ShadowMenu file = rootChildren.get(1);
		final List<ShadowMenu> fileChildren = checkNode(file, "File", 5, 0, 4);

		final ShadowMenu editCopy = editChildren.get(0);
		checkNode(editCopy, "Copy", 1, 1, 0);

		final ShadowMenu editCut = editChildren.get(1);
		checkNode(editCut, "Cut", 1, 1, 0);

		final ShadowMenu editPaste = editChildren.get(2);
		checkNode(editPaste, "Paste", 1, 1, 0);

		final ShadowMenu fileExit = fileChildren.get(0);
		checkNode(fileExit, "Exit", 1, 1, 0);

		final ShadowMenu fileNew = fileChildren.get(1);
		final List<ShadowMenu> fileNewChildren =
			checkNode(fileNew, "New", 2, 1, 2);

		final ShadowMenu fileNewImage = fileNewChildren.get(0);
		checkNode(fileNewImage, "Image", 1, 2, 0);

		final ShadowMenu fileNewTextWindow = fileNewChildren.get(1);
		checkNode(fileNewTextWindow, "Text Window", 1, 2, 0);

		final ShadowMenu fileOpen = fileChildren.get(2);
		checkNode(fileOpen, "Open", 1, 1, 0);

		final ShadowMenu fileSave = fileChildren.get(3);
		checkNode(fileSave, "Save", 1, 1, 0);
	}

	private List<ShadowMenu> checkNode(final ShadowMenu node, final String name,
		final int size, final int depth, final int childCount)
	{
		// check name
		if (name == null) assertNull(node.getMenuEntry());
		else assertEquals(name, node.getMenuEntry().getName());

		// check size
		assertEquals(size, node.size());

		// check depth
		assertEquals(depth, node.getMenuDepth());

		// check child count
		final List<ShadowMenu> children = node.getChildren();
		assertNotNull(children);
		assertEquals(childCount, children.size());

		// check leaf status and module info
		final boolean leaf = children.isEmpty();
		assertEquals(leaf, node.isLeaf());
		if (leaf) {
			// leaf nodes retain module info reference
			assertNotNull(node.getModuleInfo());
		}
		else {
			// non-leaf nodes have no associated module info
			assertNull(node.getModuleInfo());
		}

		for (ShadowMenu child : children) {
			assertEquals(node, child.getParent());
		}

		return children;
	}

	private void checkIter(final ShadowMenuIterator iter, final String name) {
		assertTrue(iter.hasNext());
		final ModuleInfo info = iter.next();
		assertEquals(name, info.getMenuPath().getLeaf().getName());
	}

}
