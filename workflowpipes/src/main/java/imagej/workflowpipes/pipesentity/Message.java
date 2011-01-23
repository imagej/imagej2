package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

/**
 * Represents the pipes error message
 * @author rick
 *
 */
public class Message implements Serializable {

	private String message;
	
	public Message( String message )
	{
		// set the message
		this.message = message;
	}
	
	/**
	 * returns the message value
	 * @return
	 */
	public String getValue()
	{
		return message;
	}
}
