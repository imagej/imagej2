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

package imagej.plugin.ui.swt;

import imagej.plugin.PluginEntry;
import imagej.plugin.PluginUtils;
import imagej.plugin.RunnablePlugin;
import imagej.plugin.ui.AbstractMenuCreator;
import imagej.plugin.ui.ShadowMenu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
public class MenuCreator extends AbstractMenuCreator<Menu, Menu> {

	@Override
	protected void addLeafToMenu(final ShadowMenu shadow, final Menu target) {
		final MenuItem menuItem = new MenuItem(target, 0);
		menuItem.setText(shadow.getMenuEntry().getName());
		linkAction(shadow.getPluginEntry(), menuItem);
	}

	@Override
	protected void addLeafToTop(final ShadowMenu shadow, final Menu target) {
		addLeafToMenu(shadow, target);
	}

	@Override
	protected Menu addNonLeafToMenu(final ShadowMenu shadow, final Menu target) {
		final Menu menu = new Menu(target);
		final MenuItem menuItem = new MenuItem(target, SWT.CASCADE);
		menuItem.setText(shadow.getMenuEntry().getName());
		menuItem.setMenu(menu);
		return menu;
	}

	@Override
	protected Menu addNonLeafToTop(final ShadowMenu shadow, final Menu target) {
		return addNonLeafToMenu(shadow, target);
	}

	@Override
	protected void addSeparatorToMenu(final Menu target) {
		new MenuItem(target, SWT.SEPARATOR);
	}

	@Override
	protected void addSeparatorToTop(final Menu target) {
		addSeparatorToMenu(target);
	}

	// -- Helper methods --

	private void linkAction(final PluginEntry<?> entry, final MenuItem menuItem)
	{
		menuItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				// TODO - find better solution for typing here
				@SuppressWarnings("unchecked")
				final PluginEntry<? extends RunnablePlugin> runnableEntry =
					(PluginEntry<? extends RunnablePlugin>) entry;
				PluginUtils.runPlugin(runnableEntry);
			}
		});
	}

}
