package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class Name implements Serializable {

	private String nameString;
	
	public Name( String name )
	{
		this.nameString = name;
	}
	
	public String getValue() {
		return nameString;
	}

}
