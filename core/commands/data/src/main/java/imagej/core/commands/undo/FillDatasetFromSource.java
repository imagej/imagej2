package imagej.core.commands.undo;

import imagej.command.Command;
import imagej.command.InvertableCommand;
import imagej.data.Dataset;
import imagej.module.ItemIO;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;


@Plugin
public class FillDatasetFromSource implements Command, InvertableCommand {
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
	public Class<FillDatasetFromSource> getInverseCommand() {
		return FillDatasetFromSource.class;
	}

	@Override
	public Object[] getInverseInputMap() {
		return new Object[]{"source",target,"target",source};
	}

}
