package imagej.workflowpipes.pipesentity;

import java.util.ArrayList;

public class Tag {
	
	private String tag;

	public Tag(String tag) {
		//assign the string array
		this.tag = tag;
	}

	/*
	 * returns the quoted values
	 */
	public String getValue() {
		return tag;
	}
	
	@Override
	public String toString()
	{
		return tag;
	}
	
	public static ArrayList<Tag> getTagsArray(Tag ... tags)
	{
		//create array for the tags
		ArrayList<Tag> tagsArray = new ArrayList<Tag>();
	
		//for each tag in the variable length input parameter
		for(Tag tag : tags)
		{
			//add the tag to the array
			tagsArray.add( tag );
		}
		
		//return the results
		return tagsArray;
	}

	public static ArrayList<Tag> getTags( String string ) {
		
		ArrayList<Tag> tags = new ArrayList<Tag>();
		return tags;
	}
}
