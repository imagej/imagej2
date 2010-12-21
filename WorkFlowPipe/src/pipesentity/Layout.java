package pipesentity;


public class Layout {

	String layoutID;
	String layoutDefinition;
	String layoutName;
	String layoutDescription;
	String layoutTags;
	
	public Layout( String layoutID, String layoutDefinition, String layoutName, String layoutDescription, String layoutTags )
	{
		this.layoutID = layoutID;
		this.layoutDefinition = layoutDefinition;
		this.layoutName = layoutName;
		this.layoutDescription = layoutDescription;
		this.layoutTags = layoutTags;
	}
	
	public String getLayoutTags() {
		return this.layoutTags;
	}
	
	public String getLayoutDefinition() {
		return layoutDefinition;
	}

	public void setLayoutDefinition(String def) {
		this.layoutDefinition = def;
	}

	public String getLayoutName() {
		return layoutName;
	}

	public void setLayoutName(String name) {
		this.layoutName = name;
	}

	public String getLayoutDescription() {
		return layoutDescription;
	}
	
	public void setLayoutDescription( String layoutDescription ) {
		this.layoutDescription = layoutDescription;
	}

	public String getLayoutID()
	{
		return layoutID;
	}
	
	@Override
	public String toString() {
		return this.layoutName + " " + this.layoutID + " " + this.layoutDescription + this.layoutDefinition;
	}
}
