package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class UI implements Serializable {
	
	public UI( String ui)
	{
		this.uiValue = ui;
	}
	
	private String uiValue;

	public Object getValue() {
		return uiValue;
	}

}
