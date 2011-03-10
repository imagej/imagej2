//
// MenuCreator.java
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

package imagej.plugin.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO
 *
 * @author Curtis Rueden
 *
 * @param <M> Top-level menu class to populate (e.g., JMenuBar or JMenu)
 * @param <I> Menu item class (e.g., JMenuItem or MenuItem)
 */
public abstract class AbstractMenuCreator<M, I> implements MenuCreator<M> {

	@Override
	public abstract void createMenus(ShadowMenu root, M target);

	/** Generates a menu item corresponding to this shadow menu node. */
	protected abstract I createMenuItem(ShadowMenu shadow);

	/**
	 * Generates a list of menu items corresponding
	 * to the child menu nodes, sorted by weight.
	 */
	protected List<I> createChildMenuItems(final ShadowMenu shadow) {
		// generate list of ShadowMenu objects, sorted by weight
		final List<ShadowMenu> childMenus =
			new ArrayList<ShadowMenu>(shadow.getChildren().values());
		Collections.sort(childMenus);

		// create menu items corresponding to ShadowMenu objects
		final List<I> menuItems = new ArrayList<I>();
		double lastWeight = Double.NaN;
		for (final ShadowMenu childMenu : childMenus) {
			final double weight = childMenu.getMenuEntry().getWeight();
			final double difference = Math.abs(weight - lastWeight);
			if (difference > 1) menuItems.add(null); // separator
			lastWeight = weight;
			final I item = createMenuItem(childMenu);
			menuItems.add(item);
		}
		return menuItems;
	}

}
