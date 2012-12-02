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

package imagej.undo;

import imagej.command.Command;
import imagej.command.CommandService;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.event.DisplayDeletedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.module.Module;
import imagej.module.ModuleInfo;
import imagej.module.event.ModuleCanceledEvent;
import imagej.module.event.ModuleFinishedEvent;
import imagej.module.event.ModuleStartedEvent;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.awt.Toolkit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO
// This service is poorly named (recording something service is better)
// More undoable ops: zoom events, pan events, display creations/deletions,
//   setting of options values, dataset dimensions changing, setImgPlus(), etc.
// Also what about legacy plugin run results? (Multiple things hatched)
// Make friendly for multithreaded access.
// Currently command histories are tied to Displays. Should be made to support
//   histories of arbitrary objects. The objects would need to support some
//   duplicate/save/restore interface.
// Note that undoing a noise reducer plugin fails because that command runs two
//   plugins. They need to be grouped as one undoable command. Or break that
//   command into two separate plugins - one a neigh specifier that sets a value
//   of a new Options plugin. Info could show in menu eventually. But for now
//   just a command with 2d rect and n-d radial options. In the long run we want
//   grouping. (Later note: undo of this command seems to work. Redo will throw
//   up the neigh specification dialog again)
// SplitChannelsContext plugin is sometimes getting run and recorded. Is this a
//   problem? Is it putting useless undo markers in the history? If so we can
//   make it Unrecordable.

// Later TODOs
// Support tools and gestures
// Grouping of many cammands as one undoable block
//   Create a Command that contains a list of commands to run. Record the undos
//   in one and the redos in another. Then add the uber command to the undo stack.

// Note: because initially no image exists a redo gets recorded (load image or
// new image) without a corresponding undo. So in general redoPos == undoPos+1.
// (This may no longer be true)
// TODO - currently while no dataset exists nothing gets recorded. So what
// happens when we run a number of nondisplay oriented plugins. They aren't
// undoable. Should undo steps be associated with the app and not the displays?
// Or have a separate undo history for the app (when no dataset loaded). Then
// user can switch between displays and undo a lot of stuff. And they can record
// and undo app related events.

// Note that when a display is deleted it could invalidate other displays' own
// history (maybe). Have img1. Paste from Img2 into Img1. Delete Img2. Go to
// Img1 and do undo. Now try redo. Is last display state of Img2 kept around as
// a reference in Img1's undo history? However note that even if the undo record
// has a handle on a deleted display the command could fail because the display
// layer does not know about it anymore. Perhaps on display deleted events we
// need to walk all undo histories and trim their list to not include any
// reference to the deleted display

// Original NOTE on this same issue: what if you use ImageCalc to add two images
// (so both displays stored as inputs to plugin in undo/redo stack. Then someone
// deletes one of the displays. Then back to display that may have been a target
// of the calc. Then undo. Should crash.
// On a related topic do we store an undo operation on a Display<?>? Or Object?
// How about a set of inputs like ImageCalc. Then cleanup all undo histories
// that refer to deleted display?????

/**
 * Default service for multistep undo/redo.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class DefaultUndoService extends AbstractService implements UndoService {

	// -- constants --

	// TODO we really need max mem for combined histories and max mem of one
	// history. It will work as a test bed for now. Max should be
	// a settable value in some options plugin

	private static final int MAX_BYTES = 20 * 1024 * 1024; // temp: 20 meg

	// -- Parameters --

	@Parameter
	private DisplayService displayService;

	@Parameter
	private CommandService commandService;

	@Parameter
	private EventService eventService;

	// -- working variables --

	private final Map<Display<?>, ModuleHistory> histories =
		new HashMap<Display<?>, ModuleHistory>();

	// HACK TO GO AWAY SOON
	private final Map<ModuleInfo, Boolean> modulesToIgnore =
		new ConcurrentHashMap<ModuleInfo, Boolean>();

	// -- service initialization code --

	@Override
	public void initialize() {
		subscribeToEvents(eventService);
	}

	// -- public api --

	@Override
	public void undo(final Display<?> display) {
		final ModuleHistory history = histories.get(display);
		if (history != null) history.doUndo();
	}

	@Override
	public void redo(final Display<?> display) {
		final ModuleHistory history = histories.get(display);
		if (history != null) history.doRedo();
	}

	@Override
	public void clearHistory(final Display<?> display) {
		final ModuleHistory history = histories.get(display);
		if (history != null) history.clear();
	}

	@Override
	public void clearAllHistory() {
		for (final ModuleHistory hist : histories.values()) {
			hist.clear();
		}
	}

	@Override
	public UndoInfo createFullRestoreModule(
		final SupportsDisplayStates display)
	{
		final DisplayState state = display.getCurrentState();
		final HashMap<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("display", display);
		inputs.put("state", state);
		final long memUsage = state.getMemoryUsage();
		final ModuleInfo displayRestoreState =
			commandService.getCommand(DisplayRestoreState.class);
		return new DefaultUndoInfo(displayRestoreState, inputs, memUsage);
	}

	// -- protected event handlers --

	@EventHandler
	protected void onEvent(final ModuleStartedEvent evt) {
		final Module module = evt.getModule();
		final Object theObject = module.getDelegateObject();
		if (theObject instanceof Unrecordable) return;
		if (theObject instanceof Invertible) return; // record later
		// CTR CHECK
		if (theObject instanceof Command) {
			if (ignoring(module.getInfo())) return;
			final Display<?> display = displayService.getActiveDisplay();
			if (!(display instanceof SupportsDisplayStates)) return;
			final UndoInfo inverse =
				createFullRestoreModule((SupportsDisplayStates) display);
			findHistory(display).addUndo(inverse);
		}
	}

	@EventHandler
	protected void onEvent(final ModuleCanceledEvent evt) {
		final Module module = evt.getModule();
		final Object theObject = module.getDelegateObject();
		if (theObject instanceof Unrecordable) return;
		if (theObject instanceof Invertible) return;
		// CTR CHECK
		if (theObject instanceof Command) {
			if (ignoring(module.getInfo())) return;
			final Display<?> display = displayService.getActiveDisplay();
			if (!(display instanceof SupportsDisplayStates)) return;
			// remove last undo point
			findHistory(display).removeNewestUndo();
		}
	}

	@EventHandler
	protected void onEvent(final ModuleFinishedEvent evt) {
		final Module module = evt.getModule();
		final Object theObject = module.getDelegateObject();
		if (theObject instanceof Unrecordable) return;
		// CTR CHECK
		if (theObject instanceof Command) {
			if (ignoring(module.getInfo())) {
				stopIgnoring(module.getInfo());
				return;
			}
			final Display<?> display = displayService.getActiveDisplay();
			if (!(display instanceof SupportsDisplayStates)) return;
			if (theObject instanceof Invertible) {
				final Invertible invertible = (Invertible) theObject;
				findHistory(display).addUndo(invertible.getInverse());
			}
			final UndoInfo redo =
				new DefaultUndoInfo(module.getInfo(), module.getInputs(), 0);
			findHistory(display).addRedo(redo);
		}
	}

	@EventHandler
	protected void onEvent(final DisplayDeletedEvent evt) {
		final ModuleHistory history = histories.get(evt.getObject());
		if (history == null) return;
		history.clear();
		histories.remove(history);
	}

	// -- private helpers --

	void ignore(final ModuleInfo info) {
		modulesToIgnore.put(info, true);
	}

	private boolean ignoring(final ModuleInfo info) {
		return modulesToIgnore.get(info) != null;
	}

	private void stopIgnoring(final ModuleInfo info) {
		modulesToIgnore.remove(info);
	}

	private ModuleHistory findHistory(final Display<?> disp) {
		ModuleHistory h = histories.get(disp);
		if (h == null) {
			h = new ModuleHistory(MAX_BYTES);
			histories.put(disp, h);
		}
		return h;
	}

	// -- Helper classes --

	/**
	 * Package access class used internally by UndoService to record and undo a
	 * history of commands.
	 * 
	 * @author Barry DeZonia
	 */
	private class ModuleHistory {

		// -- constants --

		private static final int MIN_USAGE = 500;

		// -- instance variables --

		private final long maxMemUsage;
		private final LinkedList<UndoInfo> undoables;
		private final LinkedList<UndoInfo> redoables;
		private final LinkedList<UndoInfo> transitions;

		// -- constructor --

		private ModuleHistory(final long maxMem) {
			maxMemUsage = maxMem;
			undoables = new LinkedList<UndoInfo>();
			redoables = new LinkedList<UndoInfo>();
			transitions = new LinkedList<UndoInfo>();
		}

		// -- api to be used externally --

		private void doUndo() {
			// System.out.println("doUndo() : undoPos = "+undoPos+" redoPos = "+redoPos);
			if (undoables.size() <= 0) {
				// TODO eliminate AWT dependency with a BeepService!
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			UndoInfo command = redoables.removeLast();
			transitions.add(command);
			command = undoables.removeLast();
			ignore(command.getModule());
			commandService.run(command.getModule(), command.getInputs());
		}

		private void doRedo() {
			// System.out.println("doRedo() : undoPos = "+undoPos+" redoPos = "+redoPos);
			if (transitions.size() <= 0) {
				// TODO eliminate AWT dependency with a BeepService!
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			final UndoInfo command = transitions.getLast();
			commandService.run(command.getModule(), command.getInputs());
		}

		private void clear() {
			undoables.clear();
			redoables.clear();
			transitions.clear();
		}

		private void addUndo(final UndoInfo command) {
			final long additionalSpace = MIN_USAGE + command.getMemoryUsage();
			while (((undoables.size() > 0) || (redoables.size() > 0)) &&
				(spaceUsed() + additionalSpace > maxMemUsage))
			{
				if (undoables.size() > 0) removeOldestUndo();
				if (redoables.size() > 0) removeOldestRedo();
				// TODO - what about transitionModules???
			}
			// at this point we have enough space or no history has been stored
			undoables.add(command);
		}

		private void addRedo(final UndoInfo command) {
			final ModuleInfo info = command.getModule();
			final Map<String, Object> input = command.getInputs();
			if (transitions.size() > 0) {
				if (transitions.getLast().getModule().equals(info) &&
					transitions.getLast().getInputs().equals(input))
				{
					transitions.removeLast();
				}
				else {
					transitions.clear();
				}
			}
			final long additionalSpace = MIN_USAGE + command.getMemoryUsage();
			while ((undoables.size() > 0 || redoables.size() > 0) &&
				spaceUsed() + additionalSpace > maxMemUsage)
			{
				if (undoables.size() > 0) removeOldestUndo();
				if (redoables.size() > 0) removeOldestRedo();
				// TODO - what about transitionModules???
			}
			// at this point we have enough space or no history has been stored
			redoables.add(command);
		}

		private void removeNewestUndo() {
			undoables.removeLast();
		}

		private void removeNewestRedo() {
			redoables.removeLast();
		}

		private void removeOldestUndo() {
			undoables.removeFirst();
		}

		private void removeOldestRedo() {
			redoables.removeFirst();
		}

		private long spaceUsed() {
			long used = 0;
			for (final UndoInfo undoable : undoables) {
				used += undoable.getMemoryUsage() + MIN_USAGE;
			}
			for (final UndoInfo redoable : redoables) {
				used += redoable.getMemoryUsage() + MIN_USAGE;
			}
			for (final UndoInfo transition : transitions) {
				used += transition.getMemoryUsage() + MIN_USAGE;
			}
			return used;
		}
	}

}
