package pipesentity;

public class Terminal {
	
	//populate with defaults
	private String typeKey;
	private String typeValue;
	private String directionKey = "name";
	private String directionValue;

	/**
	 * 
	 * @param input If the terminal is of Input Type
	 * @param terminalConnectorType - Enumerated values of supported types
	 */
	public Terminal( boolean isInputTypeOfTerminal, TerminalConnectorType terminalConnectorType )
	{
		if (isInputTypeOfTerminal)
		{
			directionValue = "_INPUT";
			this.typeKey = "input";
		}
		else //output
		{
			directionValue = "_OUTPUT";
			this.typeKey = "output";
		}
		
		//assign type value
		this.typeValue = terminalConnectorType.toString();
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

	public String getDirectionKeyValue() {
		return directionValue;
	}

}
