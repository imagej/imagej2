package imagej.data.undo;

import imagej.command.Command;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

// TODO - this is poorly named (recording something service is better)
// And it belongs some place other than ij-data

@Plugin(type = Service.class)
public class UndoService extends AbstractService {

	public void add(Object interestedParty, Command command) {
		
	}
	
	public void undo(Object interestedParty) {
		
	}
	
	public void redo(Object interestedParty) {
		
	}
}
