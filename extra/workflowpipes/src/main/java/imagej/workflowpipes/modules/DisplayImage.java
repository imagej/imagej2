//
// DisplayImage.java
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

package imagej.workflowpipes.modules;


import java.io.Serializable;
import java.util.List;

import imagej.workflow.debug.PreviewInfo;

import imagej.workflowpipes.pipesapi.Module;
import imagej.workflowpipes.pipesentity.Attr;
import imagej.workflowpipes.pipesentity.Conf;
import imagej.workflowpipes.pipesentity.Connector;
import imagej.workflowpipes.pipesentity.Content;
import imagej.workflowpipes.pipesentity.Count;
import imagej.workflowpipes.pipesentity.Description;
import imagej.workflowpipes.pipesentity.ID;
import imagej.workflowpipes.pipesentity.Item;
import imagej.workflowpipes.pipesentity.Name;
import imagej.workflowpipes.pipesentity.Prop;
import imagej.workflowpipes.pipesentity.Tag;
import imagej.workflowpipes.pipesentity.Terminal;
import imagej.workflowpipes.pipesentity.Type;
import imagej.workflowpipes.pipesentity.UI;

/**
 * Represents the module type "displayimage"
 * @author rick
 */
public class DisplayImage extends Module implements Serializable {

	public static DisplayImage getDisplayImage() {
		
		DisplayImage fetchPage = new DisplayImage();
		
		fetchPage.id = new ID("");

		fetchPage.terminals.add( Terminal.getOutputTerminal("items","_OUTPUT") );

		fetchPage.ui = new UI(
			"\n\t\t<div class=\"horizontal\">\n\t\t\t<label>ImageName: </label><input name=\"ImageName\" " +
			"type=\"imagename\" required=\"true\"/>\n\t\t</div> \n");

		fetchPage.name = new Name("Display Image");

		fetchPage.type = new Type("displayimage");

		fetchPage.description = new Description("Simply returns html for a relative path jpeg image");

		Tag tag = new Tag("system:sources");

		fetchPage.tags = Tag.getTagsArray(tag);
		
		//This is simulated
		fetchPage.module = "Yahoo::RSS::FetchPage";
		
		return fetchPage;
	}

	public void go( List<PreviewInfo> previewInfoList )
	{		 
		// call the start method
		start();
		
		Conf imagenameConf = Conf.getConf( "ImageName", confs );
		// System.out.println("Display image name is " + imagenameConf.getJSONObject() );
				
		// get the image name
		String imageNameString = imagenameConf.getValue().getValue();
		// System.out.println("DisplayImage conf value for ImageName conf is " + imageNameString );
	
		String contentString = "<html><body><img src='" + imageNameString + "'/></html></body>";
	    
		// add the content to prop //TODO: there is likely a much easier way to do this...
		this.props.add( new Prop( new Connector( "_OUTPUT", new Type("item"), new Attr( new Content( new Type("text"), new Count(1) ) ) ) ) );
		
		System.out.println( "FetchPage results " + contentString );
		
		// add the items
		this.items.add( new Item( "content", contentString ) );
		this.item_count.incrementCount();
		this.count.incrementCount();
		
		
		// add self generated stats
		this.response.addStat("CACHE_HIT", new Integer(1) );
		this.response.addStat("CACHE_MISS", new Integer(2) );
		
		// call stop
		stop();	
	}
}
