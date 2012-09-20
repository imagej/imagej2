package imagej.command;

public interface InvertableCommand {
	Class<? extends Command> getInverseCommand();
	Object[] getInverseInputMap();  // "string", valueObj, "string2" valueObj2, ...
}
