package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

/**
 * The Layout class maps the UI's javascript components to a Java class structure
 * @author rick
 *
 */
public class Layout implements Serializable {
    
	String layoutID;
	String layoutDefinition;
	String layoutName;
	String layoutDescription;
	String layoutTags;
	
	/**
	 * 
	 * @param layoutID - a unique id used to track a layout
	 * @param layoutDefinition - the JSON string representation of the layout
	 * @param layoutName - the name the user defines for the layout
	 * @param layoutDescription - the text description the user gives for the layout
	 * @param layoutTags - sequence of words given to aid in the discovery of this layout
	 */
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
