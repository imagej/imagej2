/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.uis.swt.menu;

import imagej.menu.AbstractMenuCreator;
import imagej.menu.ShadowMenu;
import imagej.module.ModuleInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Populates an SWT {@link Menu} with menu items from a {@link ShadowMenu}.
 * 
 * @author Curtis Rueden
 */
public class SWTMenuCreator extends AbstractMenuCreator<Menu, Menu> {

	// -- Internal methods --

	@Override
	protected void addLeafToMenu(final ShadowMenu shadow, final Menu target) {
		final MenuItem menuItem = createMenuItem(shadow, target, 0);
		linkAction(shadow, menuItem);
	}

	@Override
	protected void addLeafToTop(final ShadowMenu shadow, final Menu target) {
		addLeafToMenu(shadow, target);
	}

	@Override
	protected Menu addNonLeafToMenu(final ShadowMenu shadow, final Menu target) {
		final Menu menu = new Menu(target);
		final MenuItem menuItem = createMenuItem(shadow, target, SWT.CASCADE);
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

	private MenuItem createMenuItem(final ShadowMenu shadow, final Menu target,
		final int style)
	{
		final MenuItem menuItem = new MenuItem(target, style);
		menuItem.setText(shadow.getMenuEntry().getName());
		assignProperties(shadow, menuItem);
		return menuItem;
	}

	private void
		assignProperties(final ShadowMenu shadow, final MenuItem menuItem)
	{
		final ModuleInfo info = shadow.getModuleInfo();
		if (info != null) menuItem.setEnabled(info.isEnabled());
	}

	private void linkAction(final ShadowMenu shadow, final MenuItem menuItem) {
		menuItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				shadow.run();
			}
		});
	}

}
