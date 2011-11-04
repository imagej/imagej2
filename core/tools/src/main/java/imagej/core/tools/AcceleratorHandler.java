//
// AcceleratorHandler.java
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

package imagej.core.tools;

import imagej.ImageJ;
import imagej.ext.display.event.input.KyEvent;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleService;
import imagej.ext.plugin.PluginService;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;

/**
 * Handles keyboard accelerator combinations in certain special cases.
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
 * care of launching the Command Finder plugin. This works because the
 * {@link KyEvent#getAcceleratorString()} method takes care to attach the
 * relevant modifier flag when constructing the accelerator string.
 * </p>
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
@Tool(name = "Keyboard Shortcuts", alwaysActive = true,
	activeInAppFrame = true, priority = Integer.MIN_VALUE)
public class AcceleratorHandler extends AbstractTool {

	private final ModuleService moduleService;
	private final PluginService pluginService;
	
	public AcceleratorHandler() {
		moduleService = ImageJ.get(ModuleService.class);
		pluginService = ImageJ.get(PluginService.class);
	}

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		// TODO: ask options service whether the Control modifier should be forced
		final ModuleInfo moduleInfo =
			moduleService.getModuleForAccelerator(evt.getAcceleratorString(true));
		if (moduleInfo == null) return;

		// run via plugin service, so that preprocessors are run
		pluginService.run(moduleInfo);

		// consume event, so that nothing else tries to handle it
		evt.consume();
	}

}
