package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class Description implements Serializable {

	private String description;
	
	public Description( String description ) {
		this.description = description;
	}

	public String getValue() {
		return description;
	}

}
