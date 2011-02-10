package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

/**
 * Represents the value JSON entity
 * @author rick
 *
 */
public class Value implements Serializable {
	
	private String value;
	
	public Value(String value)
	{
		this.value = value;
	}
	
	public String getValue()
	{
		return value;
	}

}
