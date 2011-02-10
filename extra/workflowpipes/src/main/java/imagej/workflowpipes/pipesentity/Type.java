package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class Type implements Serializable {

	private String typeName;
	
	public Type( String name ) {
		this.typeName = name;
	}

	public String getValue() {
		return typeName;
	}

}
