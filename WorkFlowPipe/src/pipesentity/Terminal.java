package pipesentity;

public class Terminal {
	
	//populate with defaults
	private String typeKey;
	private String typeValue;
	private String directionKey = "name";
	
	// is either "_INPUT" or "_OUTPUT"
	private String directionValue; 

	public static Terminal getInputTerminal( String terminalConnectorType )
	{
		Terminal terminal = new Terminal();
		terminal.directionValue = "_INPUT";
		terminal.typeKey = "input";
		
		//assign type value
		terminal.typeValue = terminalConnectorType;
		
		return terminal;
	}
	
	public static Terminal getOutputTerminal ( String terminalConnectorType )
	{
		Terminal terminal = new Terminal();
		terminal.directionValue = "_OUTPUT";
		terminal.typeKey = "output";
		
		//assign type value
		terminal.typeValue = terminalConnectorType;
		
		return terminal;
	}
	
	public String getTypeKey() {
		return this.typeKey;
	}
	
	public String getTypeValue() {
		return this.typeValue;
	}

	public String getDirectionKey() {
		return directionKey;
	}

	public String getDirectionValue() {
		return directionValue;
	}
}
