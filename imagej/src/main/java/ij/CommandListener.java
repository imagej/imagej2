package ij;

	/** Plugins that implement this interface are notified when ImageJ
		is about to run a menu command. There is an example plugin at
		http://rsb.info.nih.gov/ij/plugins/download/misc/Command_Listener.java
	*/
	public interface CommandListener {

	/*	The method is called when ImageJ is about to run a menu command, 
		where 'command' is the name of the command. Return this string 
		and ImageJ will run the command, return a different command name
		and ImageJ will run that command, or return null to not run a command.
	*/
	public String commandExecuting(String command);

}
