package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class Terminal implements Serializable {
	
	//populate with defaults
	private String typeKey;
	private String typeValue;
	private String directionKey = "name";
	
	// is either "_INPUT" or "_OUTPUT"
	private String directionValue; 
	// TODO fix the variable names to input/output type : "name": instance name
	public static Terminal getInputTerminal( String terminalConnectorType, String connectorName )
	{
		Terminal terminal = new Terminal();
		terminal.directionValue = connectorName;
		terminal.typeKey = "input";
		
		//assign type value
		terminal.typeValue = terminalConnectorType;
		
		return terminal;
	}
	
	public static Terminal getOutputTerminal ( String terminalConnectorType, String connectorName )
	{
		Terminal terminal = new Terminal();
		terminal.directionValue = connectorName;
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
