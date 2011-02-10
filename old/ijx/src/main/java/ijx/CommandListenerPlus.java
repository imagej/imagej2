package ijx;

/** Plugins that implement this interface are notified
	when ImageJ's interface issued a command. */
public interface CommandListenerPlus extends CommandListener {

	/**
	 * A successfully run plugin will generate the following sequence:
	 *   CMD_REQUESTED
	 *   CMD_READY
	 *   CMD_STARTED
	 *   CMD_FINISHED
	 *
	 * A successfully run macro will generate the following sequence:
	 *   CMD_REQUESTED
	 *   CMD_READY
	 *   CMD_MACRO
	 *
	 * A successfully run LUT will generate the following sequence:
	 *   CMD_REQUESTED
	 *   CMD_READY
	 *   CMD_LUT
	 */

	static public final int CMD_REQUESTED = 0;
	static public final int CMD_READY = 1;
	static public final int CMD_STARTED = 2;
	static public final int CMD_MACRO = 3;
	static public final int CMD_FINISHED = 4;
	static public final int CMD_CANCELED = 5;
	static public final int CMD_ERROR = 6;
	static public final int CMD_LUT = 7;

	public void stateChanged(Command command, int state);
}
