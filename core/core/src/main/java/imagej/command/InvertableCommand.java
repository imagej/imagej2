package imagej.command;

import java.util.Map;

public interface InvertableCommand {
	Class<? extends Command> getInverseCommand();
	Map<String,Object> getInverseInputMap();
}
