//
// ShadowMenuTest.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.ext.menu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

	@Test
	public void testStructure() {
		final ShadowMenu root = createShadowMenu();

		// check root node
		final List<ShadowMenu> rootChildren = checkMenu(root, null, -1, 2);

		// check Edit menu
		final ShadowMenu edit = rootChildren.get(0);
		final List<ShadowMenu> editChildren = checkMenu(edit, "Edit", 0, 3);

		// check File menu
		final ShadowMenu file = rootChildren.get(1);
		final List<ShadowMenu> fileChildren = checkMenu(file, "File", 0, 4);

		// check Edit>Copy menu item
		final ShadowMenu editCopy = editChildren.get(0);
		checkMenu(editCopy, "Copy", 1, 0);

		// check Edit>Cut menu item
		final ShadowMenu editCut = editChildren.get(1);
		checkMenu(editCut, "Cut", 1, 0);

		// check Edit>Paste menu item
		final ShadowMenu editPaste = editChildren.get(2);
		checkMenu(editPaste, "Paste", 1, 0);

		// check File>Exit menu item
		final ShadowMenu fileExit = fileChildren.get(0);
		checkMenu(fileExit, "Exit", 1, 0);

		// check File>New menu
		final ShadowMenu fileNew = fileChildren.get(1);
		final List<ShadowMenu> fileNewChildren = checkMenu(fileNew, "New", 1, 2);

		// check File>New>Image menu item
		final ShadowMenu fileNewImage = fileNewChildren.get(0);
		checkMenu(fileNewImage, "Image", 2, 0);

		// check File>New>Text Window menu item
		final ShadowMenu fileNewTextWindow = fileNewChildren.get(1);
		checkMenu(fileNewTextWindow, "Text Window", 2, 0);

		// check File>Open menu item
		final ShadowMenu fileOpen = fileChildren.get(2);
		checkMenu(fileOpen, "Open", 1, 0);

		// check File>Save menu item
		final ShadowMenu fileSave = fileChildren.get(3);
		checkMenu(fileSave, "Save", 1, 0);
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

	private List<ShadowMenu> checkMenu(final ShadowMenu menu, final String name,
		final int depth, final int childCount)
	{
		if (name == null) assertNull(menu.getMenuEntry());
		else assertEquals(menu.getMenuEntry().getName(), name);
		assertEquals(menu.getMenuDepth(), depth);
		final List<ShadowMenu> children = menu.getChildren();
		assertNotNull(children);
		assertEquals(children.size(), childCount);
		return children;
	}

}
