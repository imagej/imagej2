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

package imagej.core.commands.undo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import imagej.command.Command;
import imagej.command.CommandService;
import imagej.command.InvertableCommand;
import imagej.command.Unrecordable;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.event.DisplayDeletedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.module.Module;
import imagej.module.event.ModuleCanceledEvent;
import imagej.module.event.ModuleFinishedEvent;
import imagej.module.event.ModuleStartedEvent;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

// TODO
// This service is poorly named (recording something service is better)
// This service belongs some place other than ij-commands-data
// More undoable ops: zoom events, pan events, display creations/deletions,
//   setting of options values, dataset dimensions changing, setImgPlus(), etc.
// Also what about legacy plugin run results? (Multiple things hatched)
// Use displays rather than datasets (i.e. handle overlays too and other events)
// Multiple undo histories for different displays etc.
// ThreadLocal code for classToNotRecord. Nope, can't work.
// Reorganizing undo/redo history when in middle and command recorded
// Track display deleted events and delete related undo history 
// Make friendly for multithreaded access.
// Currently made to handle Datasets of ImageDisplays. Should be made to support
//   histories of arbitrary objects. The objects would need to support some
//   duplicate/save/restore interface.

// Later TODOs
// Support tools and gestures
// Grouping of many cammands as one undoable block
//   Create a Command that contains a list of commands to run. Record the undos
//   in one and the redos in another. Then add the uber command to the undo stack.

/**
 * 
 * @author Barry DeZonia
 *
 */
@Plugin(type = Service.class)
public class UndoService extends AbstractService {

	// -- constants --
	
	// TODO we really need max mem for combined histories and not max steps of
	// each history. But it will work as a test bed for now. ANd max should be
	// a settable value in some options plugin
	
	private static final int MAX_STEPS = 5;
	
	/*  tricky attempt to make this code ignore prerecorded commands safely
	private static final String RECORDED_INTERNALLY = "ReallyDontRecordMePlease";
	*/
	
	// -- Parameters --
	
	@Parameter
	private DisplayService displayService;
	
	@Parameter
	private ImageDisplayService imageDisplayService;
	
	@Parameter
	private CommandService commandService;
	
	@Parameter
	private EventService eventService;
	
	// -- working variables --
	
	private Map<Display<?>,History> histories;

	// HACK TO GO AWAY SOON
	private Map<Class<? extends Command>,Boolean> classesToIgnore =
			new ConcurrentHashMap<Class<? extends Command>,Boolean>();
	
	// -- service initialization code --
	
	@Override
	public void initialize() {
		histories = new HashMap<Display<?>,History>() ;
		subscribeToEvents(eventService);
	}

	// -- public api --
	
	/**
	 * Undoes the previous command associated with the given display.
	 * 
	 * @param interestedParty
	 */
	public void undo(Display<?> interestedParty) {
		History history = histories.get(interestedParty);
		if (history != null) history.doUndo();
	}
	
	/**
	 * Redoes the next command associated with the given display.
	 * 
	 * @param interestedParty
	 */
	public void redo(Display<?> interestedParty) {
		History history = histories.get(interestedParty);
		if (history != null) history.doRedo();
	}

	/**
	 * Clears the entire undo/redo cache for given display
	 */
	public void clearHistory(Display<?> interestedParty) {
		History history = histories.get(interestedParty);
		if (history != null) history.clear();
	}
	
	/**
	 * Clears the entire undo/redo cache for all displays
	 */
	public void clearAllHistory() {
		for (History hist : histories.values()) {
			hist.clear();
		}
	}
	
	// -- protected event handlers --
	
	@EventHandler
	protected void onEvent(ModuleStartedEvent evt) {
		Module module = evt.getModule();
		/*  tricky attempt to make this code ignore prerecorded commands safely
		if (module.getInput(RECORDED_INTERNALLY) != null) {
			System.out.println("Skipping the recording of a prerecorded command "+module.getClass().getName());
			return;
		}
		*/
		Object theObject = module.getDelegateObject();
		if (ignoring((Class<? extends Command>)theObject.getClass())) return;
		if (theObject instanceof Unrecordable) return;
		if (theObject instanceof InvertableCommand) return; // record later
		if (theObject instanceof Command) {
			Display<?> display = displayService.getActiveDisplay();
			// FIXME HACK only datasets of imagedisplays supported right now
			if (!(display instanceof ImageDisplay)) return;
			Dataset dataset = imageDisplayService.getActiveDataset((ImageDisplay)display);
			if (dataset == null) return;
			UndoHelperPlugin snapshot = new UndoHelperPlugin();
			snapshot.setContext(getContext());
			snapshot.setSource(dataset);
			snapshot.run();
			Dataset backup = snapshot.getTarget();
			Map<String,Object> inputs = new HashMap<String, Object>();
			inputs.put("source", backup);
			inputs.put("target", dataset);
			findHistory(display).addUndo(UndoHelperPlugin.class, inputs);
		}
	}
	
	@EventHandler
	protected void onEvent(ModuleCanceledEvent evt) {
		Module module = evt.getModule();
		/*  tricky attempt to make this code ignore prerecorded commands safely
		if (module.getInput(RECORDED_INTERNALLY) != null) {
			System.out.println("Skipping the recording of a prerecorded command "+module.getClass().getName());
			return;
		}
		*/
		Object theObject = module.getDelegateObject();
		if (ignoring((Class<? extends Command>)theObject.getClass())) return;
		if (theObject instanceof Unrecordable) return;
		if (theObject instanceof Command) {
			Display<?> display = displayService.getActiveDisplay();
			// FIXME HACK only datasets of imagedisplays supported right now
			if (!(display instanceof ImageDisplay)) return;
			// remove last undo point
			findHistory(display).removeNewestUndo();
		}
	}
	
	@EventHandler
	protected void onEvent(ModuleFinishedEvent evt) {
		Module module = evt.getModule();
		/*  tricky attempt to make this code ignore prerecorded commands safely
		if (module.getInput(RECORDED_INTERNALLY) != null) {
			System.out.println("Skipping the recording of a prerecorded command "+module.getClass().getName());
			return;
		}
		*/
		Object theObject = module.getDelegateObject();
		if (theObject instanceof Unrecordable) return;
		if (theObject instanceof Command) {
			Display<?> display = displayService.getActiveDisplay();
			// FIXME HACK only datasets of imagedisplays supported right now
			if (!(display instanceof ImageDisplay)) return;
			Dataset dataset = imageDisplayService.getActiveDataset((ImageDisplay)display);
			if (dataset == null) return;
			Class<? extends Command> theClass =
					(Class<? extends Command>) theObject.getClass();
			if (!ignoring(theClass)) {
				if (theObject instanceof InvertableCommand) {
					InvertableCommand command = (InvertableCommand) theObject;
					findHistory(display).addUndo(command.getInverseCommand(), command.getInverseInputMap());
				}
				findHistory(display).addRedo(theClass, evt.getModule().getInputs());
			}
			stopIgnoring(theClass);
		}
	}

	// NOTE - what if you use ImageCalc to add two images (so both displays stored
	// as inputs to plugin in undo/redo stack. Then someone deletes one of the
	// displays. Then back to display that may have been a target of the calc.
	// Then undo. Should crash.
	// On a related topic do we store an undo operation on a Display<?>? Or Object?
	// How about a set of inputs like ImageCalc. Then cleanup all undo histories
	// that refer to deleted display?????
	
	@EventHandler
	protected void onEvent(DisplayDeletedEvent evt) {
		History history = histories.get(evt.getObject());
		if (history == null) return;
		history.clear();
		histories.remove(history);
	}
	
	// -- private helpers --

	// HACK TO GO AWAY SOON
	private boolean ignoring(Class<? extends Command> clss) {
		return classesToIgnore.get(clss) != null;
	}

	// HACK TO GO AWAY SOON
	private void stopIgnoring(Class<? extends Command> clss) {
		classesToIgnore.remove(clss);
	}
	
	private History findHistory(Display<?> disp) {
		History h = histories.get(disp);
		if (h == null) {
			h = new History();
			histories.put(disp, h);
		}
		return h;
	}
	
	private class History {
		private LinkedList<Class<? extends Command>> undoableCommands;
		private LinkedList<Class<? extends Command>> redoableCommands;
		private LinkedList<Map<String,Object>> undoableInputs;
		private LinkedList<Map<String,Object>> redoableInputs;
		private int undoPos;
		private int redoPos;

		History() {
			undoableCommands = new LinkedList<Class<? extends Command>>();
			redoableCommands = new LinkedList<Class<? extends Command>>();
			undoableInputs = new LinkedList<Map<String,Object>>();
			redoableInputs = new LinkedList<Map<String,Object>>();
			undoPos = -1;
			redoPos = -1;
		}
		
		void doUndo() {
			if ((undoPos < 0) || (undoPos >= undoableCommands.size())) return;
			Class<? extends Command> command = undoableCommands.get(undoPos);
			Map<String,Object> input = undoableInputs.get(undoPos);
			undoPos--;
			redoPos--;
			commandService.run(command, input);
		}
		
		void doRedo() {
			if ((redoPos < 0) || (redoPos >= redoableCommands.size())) return;
			Class<? extends Command> command = redoableCommands.get(redoPos);
			Map<String,Object> input = redoableInputs.get(redoPos);
			undoPos++;
			redoPos++;
			commandService.run(command, input);
		}
		
		void clear() {
			undoableCommands.clear();
			redoableCommands.clear();
			undoableInputs.clear();
			redoableInputs.clear();
			undoPos = -1;
			redoPos = -1;
		}
		
		// TODO - if not at end clear out some list entries above
		
		void addUndo(Class<? extends Command> command, Map<String,Object> inputs) {
			/*  tricky attempt to make this code ignore prerecorded commands safely
			inputs.put(RECORDED_INTERNALLY, RECORDED_INTERNALLY);
			*/
			undoableCommands.add(command);
			undoableInputs.add(inputs);
			undoPos++;
			if (undoPos > MAX_STEPS) removeOldestUndo();
		}
		
		void addRedo(Class<? extends Command> command, Map<String,Object> inputs) {
			/*  tricky attempt to make this code ignore prerecorded commands safely
			inputs.put(RECORDED_INTERNALLY, RECORDED_INTERNALLY);
			*/
			redoableCommands.add(command);
			redoableInputs.add(inputs);
			redoPos++;
			if (redoPos > MAX_STEPS) removeOldestRedo();
		}
		
		void removeNewestUndo() {
			undoableCommands.removeLast();
			undoableInputs.removeLast();
			undoPos--;
		}

		void removeNewestRedo() {
			redoableCommands.removeLast();
			redoableInputs.removeLast();
			redoPos--;
		}
		
		void removeOldestUndo() {
			undoableCommands.removeFirst();
			undoableInputs.removeFirst();
			undoPos--;
		}

		void removeOldestRedo() {
			redoableCommands.removeFirst();
			redoableInputs.removeFirst();
			redoPos--;
		}

	}
}
