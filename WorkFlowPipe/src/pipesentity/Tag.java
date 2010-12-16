package pipesentity;

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
	
	public static Tag[] getTagsArray(Tag ... tags)
	{
		//create array for the tags
		Tag[] tagsArray = new Tag[ tags.length ];
	
		
		//create an index value
		int tagsIndex = 0;
		
		//for each tag in the variable length input parameter
		for(Tag tag : tags)
		{
			//add the tag to the array
			tagsArray[ tagsIndex++ ] = tag;
		}
		
		//return the results
		return tagsArray;
	}
}
