package imagej.core.commands.undo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import imagej.command.Command;
import imagej.command.CommandService;
import imagej.command.InvertableCommand;
import imagej.command.Unrecordable;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplayService;
import imagej.event.EventHandler;
import imagej.event.EventService;
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
	
	private static final int MAX_STEPS = 5;
	
	// -- Parameters --
	
	@Parameter
	private ImageDisplayService displayService;
	
	@Parameter
	private CommandService commandService;
	
	@Parameter
	private EventService eventService;
	
	// -- working variables --
	
	private LinkedList<Class<? extends Command>> undoableCommands;
	private LinkedList<Class<? extends Command>> redoableCommands;
	private LinkedList<Map<String,Object>> undoableInputs;
	private LinkedList<Map<String,Object>> redoableInputs;
	private int undoPos;
	private int redoPos;
	private Class<? extends Command> classToNotRecord;
	
	// -- service initialization code --
	
	@Override
	public void initialize() {
		undoableCommands = new LinkedList<Class<? extends Command>>();
		redoableCommands = new LinkedList<Class<? extends Command>>();
		undoableInputs = new LinkedList<Map<String,Object>>();
		redoableInputs = new LinkedList<Map<String,Object>>();
		undoPos = -1;
		redoPos = -1;
		subscribeToEvents(eventService);
	}

	// -- public api --
	
	/**
	 * Undoes the previous command associated with the given object. For instance
	 * the given object might be a Dataset or Display.
	 * 
	 * @param interestedParty
	 */
	public void undo(Object interestedParty) {
		if (interestedParty == null) return;
		if ((undoPos < 0) || (undoPos >= undoableCommands.size())) return;
		Class<? extends Command> command = undoableCommands.get(undoPos);
		Map<String,Object> input = undoableInputs.get(undoPos);
		undoPos--;
		redoPos--;
		classToNotRecord = command;
		commandService.run(command, input);
	}
	
	/**
	 * Redoes the next command associated with the given object (if one exists).
	 * For instance the given object might be a Dataset or Display.
	 * 
	 * @param interestedParty
	 */
	public void redo(Object interestedParty) {
		if (interestedParty == null) return;
		if ((redoPos < 0) || (redoPos >= redoableCommands.size())) return;
		Class<? extends Command> command = redoableCommands.get(redoPos);
		Map<String,Object> input = redoableInputs.get(redoPos);
		System.out.println("About to redo:");
		System.out.println("  command = "+command.getName());
		for (String key : input.keySet()) {
			System.out.println("  input: "+key+" : "+input.get(key));
		}
		undoPos++;
		redoPos++;
		classToNotRecord = command;
		commandService.run(command, input);
	}

	/**
	 * Clears the entire undo/redo cache for all objects
	 */
	public void clearHistory() {
		// TODO - iterate all undo/redo command histories
		undoableCommands.clear();
		redoableCommands.clear();
		undoableInputs.clear();
		redoableInputs.clear();
		undoPos = -1;
		redoPos = -1;
	}
	
	// -- protected event handlers --
	
	@EventHandler
	protected void onEvent(ModuleStartedEvent evt) {
		Object theObject = evt.getModule().getDelegateObject();
		if (theObject instanceof Unrecordable) return;
		// TODO - this could be fraught with multithreaded issues
		if ((classToNotRecord != null) && (classToNotRecord.isInstance(theObject))){
			return;
		}
		if (theObject instanceof InvertableCommand) return; // record later
		if (theObject instanceof Command) {
			Dataset dataset = displayService.getActiveDataset();
			if (dataset == null) return;
			UndoHelperPlugin snapshot = new UndoHelperPlugin();
			snapshot.setContext(getContext());
			snapshot.setSource(dataset);
			snapshot.run();
			Dataset backup = snapshot.getTarget();
			Map<String,Object> inputs = new HashMap<String, Object>();
			inputs.put("source", backup);
			inputs.put("target", dataset);
			addToUndo(dataset, UndoHelperPlugin.class, inputs);
		}
	}
	
	@EventHandler
	protected void onEvent(ModuleCanceledEvent evt) {
		Object theObject = evt.getModule().getDelegateObject();
		if (theObject instanceof Unrecordable) return;
		// TODO - this could be fraught with multithreaded issues
		if ((classToNotRecord != null) && (classToNotRecord.isInstance(theObject))){
			classToNotRecord = null;
			return;
		}
		if (theObject instanceof Command) {
			Dataset dataset = displayService.getActiveDataset();
			if (dataset == null) return;
			// remove last undo point
			undoableCommands.removeLast();
			undoableInputs.removeLast();
			undoPos--;
		}
	}
	
	@EventHandler
	protected void onEvent(ModuleFinishedEvent evt) {
		Object theObject = evt.getModule().getDelegateObject();
		if (theObject instanceof Unrecordable) return;
		// TODO - this could be fraught with multithreaded issues
		if ((classToNotRecord != null) && (classToNotRecord.isInstance(theObject))){
			classToNotRecord = null;
			return;
		}
		if (theObject instanceof Command) {
			Dataset dataset = displayService.getActiveDataset();
			if (dataset == null) return;
			if (theObject instanceof InvertableCommand) {
				InvertableCommand command = (InvertableCommand) theObject;
				addToUndo(dataset, command.getInverseCommand(), command.getInverseInputMap());
			}
			Class<? extends Command> theClass =
					(Class<? extends Command>) theObject.getClass();
			addToRedo(dataset, theClass, evt.getModule().getInputs());
		}
		
		// gobble up old undo/redo commands: limited number of undo steps allowed
		
		if (undoableCommands.size() > MAX_STEPS) {
			while (undoableCommands.size() > MAX_STEPS) undoableCommands.remove(0);
			while (undoableInputs.size() > MAX_STEPS) undoableInputs.remove(0);
			undoPos = undoableCommands.size() - 1;
		}
		
		if (redoableCommands.size() > MAX_STEPS) {
			while (redoableCommands.size() > MAX_STEPS) redoableCommands.remove(0);
			while (redoableInputs.size() > MAX_STEPS) redoableInputs.remove(0);
			redoPos = redoableCommands.size() - 1;
		}
	}

	// -- private helpers --
	
	// TODO - if not at end clear out some list entries above
	
	private void addToUndo(Object interestedParty, Class<? extends Command> command, Map<String,Object> input) {
		if (interestedParty == null) return;
		System.out.println("Adding to undo: "+command.getName());
		undoableCommands.add(command);
		undoableInputs.add(input);
		undoPos++;
	}
	
	private void addToRedo(Object interestedParty, Class<? extends Command> command, Map<String,Object> input) {
		if (interestedParty == null) return;
		System.out.println("Adding to redo: "+command.getName());
		redoableCommands.add(command);
		redoableInputs.add(input);
		redoPos++;
	}
	
}
