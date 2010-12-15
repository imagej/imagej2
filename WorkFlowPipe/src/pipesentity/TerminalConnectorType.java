package pipesentity;

import pipesapi.TerminalConnector;

public class TerminalConnectorType implements TerminalConnector {
	public enum inputType{ number, rss, datetime, text }
	public enum outputType{ number, rss, datetime, text }
}
