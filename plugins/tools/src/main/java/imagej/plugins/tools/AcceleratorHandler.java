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

package imagej.plugins.tools;

import imagej.command.CommandService;
import imagej.display.event.input.KyPressedEvent;
import imagej.module.ModuleInfo;
import imagej.module.ModuleService;
import imagej.tool.AbstractTool;
import imagej.tool.Tool;

import org.scijava.Priority;
import org.scijava.input.Accelerator;
import org.scijava.input.InputModifiers;
import org.scijava.input.KeyCode;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Handles keyboard accelerator combinations that launch modules.
 * <p>
 * Specifically, we want to handle key presses even if the current UI's built-in
 * event handler would pass them up. For example, with the Swing UI, the menu
 * infrastructure fires a menu item if the associated accelerator is pressed,
 * but we need to fire the linked module regardless of which window is active;
 * i.e., on Windows and Linux platforms, image windows do not have a menu bar
 * attached, so the Swing menu infrastructure does not handle key presses when
 * an image window is active.
 * </p>
 * <p>
 * This tool also handles the case where the accelerator lacks the platform's
 * modifier key; e.g., if you press L (rather than Ctrl+L or Cmd+L) it will take
 * care of launching the Command Finder plugin.
 * </p>
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
@Plugin(type = Tool.class, name = "Keyboard Shortcuts",
	priority = Priority.VERY_LOW_PRIORITY)
public class AcceleratorHandler extends AbstractTool {

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private CommandService commandService;

	@Override
	public boolean isAlwaysActive() {
		return true;
	}

	@Override
	public boolean isActiveInAppFrame() {
		return true;
	}

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		final Accelerator acc = evt.getAccelerator();
		if (acc.getKeyCode() == KeyCode.UNDEFINED) return;

		ModuleInfo moduleInfo = null;

		// look up the module corresponding to this key press
		moduleInfo = moduleService.getModuleForAccelerator(acc);

		// TODO: ask options service whether the default modifier should be forced
		final boolean addModifierAutomatically = true;

		if (moduleInfo == null && addModifierAutomatically) {
			// look up the module corresponding to this key press, plus control
			final KeyCode keyCode = acc.getKeyCode();
			final InputModifiers modifiers = forceDefaultModifier(acc.getModifiers());
			final Accelerator modAcc = new Accelerator(keyCode, modifiers);
			if (!acc.equals(modAcc)) {
				moduleInfo = moduleService.getModuleForAccelerator(modAcc);
			}
		}

		if (moduleInfo == null) return; // no matching module found

		// run via command service, so that preprocessors are run
		moduleService.run(moduleInfo, true, new Object[0]); // FIXME

		// consume event, so that nothing else tries to handle it
		evt.consume();
	}

	// -- Helper methods --

	private InputModifiers forceDefaultModifier(final InputModifiers modifiers) {
		final boolean forceMeta = Accelerator.isCtrlReplacedWithMeta();
		final boolean forceCtrl = !forceMeta;

		final boolean alt = modifiers.isAltDown();
		final boolean altGr = modifiers.isAltGrDown();
		final boolean ctrl = forceCtrl || modifiers.isCtrlDown();
		final boolean meta = forceMeta || modifiers.isMetaDown();
		final boolean shift = modifiers.isShiftDown();

		return new InputModifiers(alt, altGr, ctrl, meta, shift, false, false,
			false);
	}

}
