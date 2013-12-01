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

package imagej.plugins.uis.awt.menu;

import imagej.menu.ShadowMenu;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.PopupMenu;

/**
 * Populates an AWT {@link MenuBar} with menu items from a {@link ShadowMenu}.
 * <p>
 * Unfortunately, the {@link AWTMenuBarCreator}, {@link AWTMenuCreator} and
 * {@link AWTPopupMenuCreator} classes must all exist and replicate some code,
 * because {@link MenuBar}, {@link MenuItem} and {@link PopupMenu} do not share
 * a common interface for operations such as {@link Menu#add}.
 * </p>
 * <p>
 * This class is called {@code AWTMenuBarCreator} rather than simply
 * {@code MenuBarCreator} for consistency with other UI implementations such as
 * {@link AWTMenuCreator}.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class AWTMenuBarCreator extends AbstractAWTMenuCreator<MenuBar> {

	@Override
	protected void addLeafToTop(final ShadowMenu shadow, final MenuBar target) {
		// NB: Ignore top-level leaf.
	}

	@Override
	protected Menu addNonLeafToTop(final ShadowMenu shadow, final MenuBar target)
	{
		final Menu menu = createNonLeaf(shadow);
		target.add(menu);
		return menu;
	}

	@Override
	protected void addSeparatorToTop(final MenuBar target) {
		// NB: Ignore top-level separator.
	}

}
