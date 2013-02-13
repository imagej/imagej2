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

package imagej.core.commands.typechange;

import imagej.command.CommandInfo;
import imagej.command.CommandService;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.display.Display;
import imagej.display.event.DisplayActivatedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.module.event.ModulesUpdatedEvent;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.util.List;

/**
 * Service for keeping the Type submenu synced with the active display.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public final class TypeChangeService extends AbstractService {

	@Parameter
	private EventService eventService;

	@Parameter
	private CommandService commandService;

	@Parameter
	private ImageDisplayService imageDisplayService;

	// -- TypeChangeService methods --

	public EventService getEventService() {
		return eventService;
	}

	public CommandService getCommandService() {
		return commandService;
	}

	public ImageDisplayService getImageDisplayService() {
		return imageDisplayService;
	}

	/** Selects the module matching the active dataset's type. */
	public void refreshSelectedType(final ImageDisplay display) {
		final Dataset dataset = imageDisplayService.getActiveDataset(display);
		final String typeLabel =
			dataset == null ? "" : dataset.getTypeLabelShort();
		final String suffix = ".ChangeTo" + typeLabel.toUpperCase();

		final List<CommandInfo> commands =
			commandService.getCommandsOfType(TypeChanger.class);
		for (final CommandInfo info : commands) {
			final boolean selected = info.getDelegateClassName().endsWith(suffix);
			info.setSelected(selected);
		}

		// notify interested parties that the TypeChanger commands have changed
		eventService.publish(new ModulesUpdatedEvent(commands));
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final DisplayActivatedEvent event) {
		final Display<?> display = event.getDisplay();
		if (display instanceof ImageDisplay) {
			refreshSelectedType((ImageDisplay) display);
		}
	}

}
