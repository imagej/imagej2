package ijx;

import ijx.plugin.api.PlugIn;

public class Command {
	private long time = System.currentTimeMillis();
	private boolean consumed = false;
	public String command;
	public String className;
	public String arg;
	public Object plugin;
	public int modifiers;

	public Command(String command) {
		this.command = command;
	}

	public void consume() { this.consumed = true; }
	public boolean isConsumed() { return consumed; }

	public String getClassName() { return className; }
	public String getCommand() { return command; }
	public String getArg() { return arg; }
	public int getModifiers() { return modifiers; }

	/** May return null if the plugin is never spawned. */
	public Object getPlugIn() { return plugin; }

	public String toString() {
		return new StringBuffer("command=").append(command).append(", className=").append(className).append(", arg=").append(arg).append(", modifiers=").append(modifiers).append(", consumed=").append(consumed).append(", time=").append(time).toString();
	}
}
