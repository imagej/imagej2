package imagej.workflowpipes.pipes;

import imagej.workflowpipes.pipesentity.Type;

/**
 * Represents the service entity
 *
 * @author rick
 *
 */
public class Service 
{	
	//TODO: replace with service lookup
	public Type type;
	
	public Service( Type type )
	{
		this.type = type;
	}
	
	public Type getType()
	{
		return type;
	}
}
