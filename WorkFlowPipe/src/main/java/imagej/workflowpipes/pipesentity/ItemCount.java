package imagej.workflowpipes.pipesentity;

public class ItemCount {
	
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
