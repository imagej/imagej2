package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class ItemCount implements Serializable {
	
	private Integer item_count = 0;
	
	public ItemCount()
	{
		
	}
	
	public String getValue()
	{
		return item_count.toString();
	}
	
	public void incrementCount()
	{
		item_count++;
	}

}
