package imagej.core.commands.undo;

import java.util.HashMap;
import java.util.Map;

import imagej.command.ContextCommand;
import imagej.command.InvertableCommand;
import imagej.data.Dataset;
import imagej.module.ItemIO;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;


@Plugin
public class UndoHelperPlugin extends ContextCommand implements InvertableCommand {
	@Parameter(type = ItemIO.INPUT)
	private Dataset source;
	
	@Parameter(type = ItemIO.BOTH, required = false)
	private Dataset target;

	@Override
	public void run() {
		if (target == null)
			target = source.duplicate();
		else
			target.setImgPlus(source.getImgPlus());
	}

	@Override
	public Class<UndoHelperPlugin> getInverseCommand() {
		return UndoHelperPlugin.class;
	}

	@Override
	public Map<String,Object> getInverseInputMap() {
		Map<String,Object> inverseInputs = new HashMap<String, Object>();
		inverseInputs.put("source", target);
		inverseInputs.put("target", source);
		return inverseInputs;
	}

	public void setSource(Dataset src) {
		source = src;
	}

	public void setTarget(Dataset targ) {
		target = targ;
	}
	
	public Dataset getTarget() { 
		return target;
	}
}
