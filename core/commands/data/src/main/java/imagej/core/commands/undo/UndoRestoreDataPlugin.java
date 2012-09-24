package imagej.core.commands.undo;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.img.Img;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.real.DoubleType;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.command.InvertibleCommand;
import imagej.data.Dataset;
import imagej.module.ItemIO;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;


@Plugin
public class UndoRestoreDataPlugin
	extends ContextCommand
	implements InvertibleCommand
{
	@Parameter
	private UndoService undoService;
	
	@Parameter(type = ItemIO.BOTH)
	private Dataset target;
	
	@Parameter(type = ItemIO.INPUT)
	private PointSet points;
	
	@Parameter(type = ItemIO.INPUT)
	private Img<DoubleType> data;
	
	@Override
	public void run() {
		undoService.restoreData(target, points, data);
	}

	@Override
	public Class<? extends Command> getInverseCommand() {
		return UndoSaveDataPlugin.class;
	}

	@Override
	public Map<String, Object> getInverseInputMap() {
		HashMap<String, Object> inverseInputs = new HashMap<String, Object>();
		inverseInputs.put("source", target);
		inverseInputs.put("points", points);
		return inverseInputs;
	}

}
