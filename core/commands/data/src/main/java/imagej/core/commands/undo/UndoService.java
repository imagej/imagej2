package imagej.core.commands.undo;

import java.util.LinkedList;

import imagej.command.Command;
import imagej.command.CommandService;
import imagej.command.InvertableCommand;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplayService;
import imagej.display.event.input.KyTypedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.input.KeyCode;
import imagej.module.event.ModuleFinishedEvent;
import imagej.module.event.ModuleStartedEvent;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

// TODO - this is poorly named (recording something service is better)
// And it belongs some place other than ij-data

// do I lay down a snapshot before each module run? Or after each one? After.

// undoable?: zoom events, pan events, display creations, display deletions,
// setting of options values, dataset dimensions changing, setImgPlus(), etc.
// Also what about legacy plugin run results?


@Plugin(type = Service.class)
public class UndoService extends AbstractService {

	@Parameter
	private ImageDisplayService displayService;
	
	@Parameter
	private CommandService commandService;
	
	@Parameter
	private EventService eventService;
	
	private LinkedList<Class<? extends Command>> commands;
	private LinkedList<Object[]> inputs;
	private int top = -1;
	private boolean dontRecord = false;
	
	@Override
	public void initialize() {
		commands = new LinkedList<Class<? extends Command>>();
		inputs = new LinkedList<Object[]>();
		subscribeToEvents(eventService);
	}

	// TODO - forward and backward commands/inputs needed
	
	public void add(Object interestedParty, Class<? extends Command> command, Object[] input) {
		if (interestedParty == null) return;
		commands.add(command);
		inputs.add(input);
	}
	
	public void undo(Object interestedParty) {
		System.out.println("undo");
		if (interestedParty == null) return;
		dontRecord = true;
		Class<? extends Command> command = commands.removeLast();
		Object[] input = inputs.removeLast();
		commandService.run(command, input);
		dontRecord = false;
	}
	
	public void redo(Object interestedParty) {
		System.out.println("redo");
		if (interestedParty == null) return;
		dontRecord = true;
		// do the work
		dontRecord = false;
	}
	
	@EventHandler
	protected void onEvent(ModuleStartedEvent evt) {
		//System.out.println("A module started: "+evt.getModule().toString());
	}
	
	@EventHandler
	protected void onEvent(ModuleFinishedEvent evt) {
		if (dontRecord) return;
		//System.out.println("A module finished: "+evt.getModule().toString());
		Dataset dataset = displayService.getActiveDataset();
		Object theObject = evt.getModule().getDelegateObject(); 
		if (theObject instanceof Command) {
			if (theObject instanceof InvertableCommand) {
				System.out.println("Adding an undo point that is a single change");
				InvertableCommand command = (InvertableCommand) theObject;
				add(dataset, command.getInverseCommand(), command.getInverseInputMap());
			}
			else {
				System.out.println("Adding a full snapshot undo point");
				add(dataset, FillDatasetFromSource.class, new Object[]{"source",dataset});
			}
		}
	}
	
	private Dataset getActiveDataset() {
		return displayService.getActiveDataset();
	}
}
