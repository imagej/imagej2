package imagej.core.commands.undo;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
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
public class UndoSaveDataPlugin
	extends ContextCommand
	implements InvertibleCommand
{
	@Parameter
	private UndoService undoService;
	
	@Parameter(type = ItemIO.INPUT)
	private Dataset source;
	
	@Parameter(type = ItemIO.INPUT)
	private PointSet points;
	
	@Parameter(type = ItemIO.OUTPUT)
	private Img<DoubleType> data;
	
	@Override
	public void run() {
		// TODO - change ArrayImgFactory to some small memory ImgFactory made for undo
		undoService.captureData(source, points, new ArrayImgFactory<DoubleType>());
	}
	
	@Override
	public Class<? extends Command> getInverseCommand() {
		return UndoRestoreDataPlugin.class;
	}

	@Override
	public Map<String, Object> getInverseInputMap() {
		HashMap<String, Object> inverseInputs = new HashMap<String, Object>();
		inverseInputs.put("target", source);
		inverseInputs.put("points", points);
		inverseInputs.put("data", data);
		return inverseInputs;
	}
	
	public void setSource(Dataset ds) {
		source = ds;
	}
	
	public Dataset getSource() {
		return source;
	}
	
	public void setPoints(PointSet ps) {
		points = ps;
	}

	public PointSet getPoints() {
		return points;
	}
	
	public Img<DoubleType> getData() {
		return data;
	}
	
	public void setUndoService(UndoService srv) {
		undoService = srv;
	}
}
