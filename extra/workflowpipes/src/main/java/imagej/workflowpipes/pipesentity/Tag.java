//
// Tag.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.workflowpipes.pipesentity;

import java.io.Serializable;
import java.util.ArrayList;

public class Tag implements Serializable {
	
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
