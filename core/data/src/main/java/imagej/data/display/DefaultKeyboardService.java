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

package imagej.data.display;

import java.util.HashSet;

import imagej.display.event.input.KyPressedEvent;
import imagej.display.event.input.KyReleasedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.input.KeyCode;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

/**
 * Default implementation of {@link KeyboardService}.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class DefaultKeyboardService extends AbstractService implements
	KeyboardService
{

	@Parameter
	private EventService eventService;

	private boolean altDown = false;
	private boolean altGrDown = false;
	private boolean ctrlDown = false;
	private boolean metaDown = false;
	private boolean shiftDown = false;

	private HashSet<KeyCode> pressedKeys = new HashSet<KeyCode>();

	// -- KeyboardService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public boolean isAltDown() {
		return altDown;
	}

	@Override
	public boolean isAltGrDown() {
		return altGrDown;
	}

	@Override
	public boolean isCtrlDown() {
		return ctrlDown;
	}

	@Override
	public boolean isMetaDown() {
		return metaDown;
	}

	@Override
	public boolean isShiftDown() {
		return shiftDown;
	}

	@Override
	public boolean isKeyDown(final KeyCode code) {
		return pressedKeys.contains(code);
	}

	// -- Event handlers --

	@EventHandler
	void onEvent(final KyPressedEvent evt) {
		altDown = evt.getModifiers().isAltDown();
		altGrDown = evt.getModifiers().isAltGrDown();
		ctrlDown = evt.getModifiers().isCtrlDown();
		metaDown = evt.getModifiers().isMetaDown();
		shiftDown = evt.getModifiers().isShiftDown();
		pressedKeys.add(evt.getCode());
	}

	@EventHandler
	void onEvent(final KyReleasedEvent evt) {
		altDown = evt.getModifiers().isAltDown();
		altGrDown = evt.getModifiers().isAltGrDown();
		ctrlDown = evt.getModifiers().isCtrlDown();
		metaDown = evt.getModifiers().isMetaDown();
		shiftDown = evt.getModifiers().isShiftDown();
		pressedKeys.remove(evt.getCode());
	}

}
