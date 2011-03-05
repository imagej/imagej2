//
// FetchPage.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import imagej.workflow.debug.PreviewInfo;
import imagej.workflowpipes.pipesapi.Module;
import imagej.workflowpipes.pipesentity.Attr;
import imagej.workflowpipes.pipesentity.Conf;
import imagej.workflowpipes.pipesentity.Connector;
import imagej.workflowpipes.pipesentity.Content;
import imagej.workflowpipes.pipesentity.Count;
import imagej.workflowpipes.pipesentity.Description;
import imagej.workflowpipes.pipesentity.Error;
import imagej.workflowpipes.pipesentity.ID;
import imagej.workflowpipes.pipesentity.Item;
import imagej.workflowpipes.pipesentity.Message;
import imagej.workflowpipes.pipesentity.Name;
import imagej.workflowpipes.pipesentity.Prop;
import imagej.workflowpipes.pipesentity.Tag;
import imagej.workflowpipes.pipesentity.Terminal;
import imagej.workflowpipes.pipesentity.Type;
import imagej.workflowpipes.pipesentity.UI;

/**
 * Represents simple, incomplete module type "fetchpage".  Does not translate relative domains for src resources such as images.
 * @author rick
 */
public class FetchPage extends Module implements Serializable {

	private static final long serialVersionUID = 5622501473362572605L;

	public static FetchPage getFetchPage() {
		
		FetchPage fetchPage = new FetchPage();
		
		fetchPage.id = new ID("");

		fetchPage.terminals.add( Terminal.getOutputTerminal("items","_OUTPUT") );

		fetchPage.ui = new UI("\n\t\t<div class=\"horizontal\">\n\t\t\t<label>URL: </label><input name=\"URL\" type=\"url\" required=\"true\"/>\n\t\t</div> \n\t\t<div class=\"horizontal\">\n\t\t\t<label>Cut content from: </label><input name=\"from\" type=\"text\" required=\"true\"/>\n            <label>to: </label><input name=\"to\" type=\"text\" required=\"true\"/>\n        </div>\n        <div class=\"horizontal\">\n            <label>Split using delimiter: </label><input name=\"token\" type=\"text\" required=\"true\"/>\n            \n\t\t</div> \n\t\t");

		fetchPage.name = new Name("Fetch Page");

		fetchPage.type = new Type("fetchpage");

		fetchPage.description = new Description("Fetch HTML or XHTML documents and emit as a string");

		Tag tag = new Tag("system:sources");

		fetchPage.tags = Tag.getTagsArray(tag);
		
		//TODO this is to be replaced with the implementation
		fetchPage.module = "Yahoo::RSS::FetchPage";
		
		return fetchPage;
	}

	public void go( List<PreviewInfo> previewInfoList )
	{		 
		// call the start method
		start();
		
		Conf urlConf = Conf.getConf( "URL", confs );
		// System.out.println("FetchPage urlConf is " + urlConf.getJSONObject() );
				
		// get the url
		String url = urlConf.getValue().getValue();
		// System.out.println("FetchPage conf value for URL conf is " + url );
		
		String s;
		String contentString = "";
		BufferedReader r = null;
		
		try {
			r = new BufferedReader(new InputStreamReader(new URL("http://" + url).openStream()));
		} catch (MalformedURLException e) {
			// add the error 
			this.errors.add( new Error( new Type( "warning" ), new Message( e.getMessage() ) ) );
		} catch (IOException e) {
			this.errors.add( new Error( new Type( "warning" ), new Message( e.getMessage() ) ) );
		}
		
	    try {
			while ((s = r.readLine()) != null) {
				contentString += s;
			}
		} catch (IOException e) {
			this.errors.add( new Error( new Type( "warning" ), new Message( e.getMessage() ) ) );
		}
	    
		// add the content to prop //TODO: there is likely a much easier way to do this...
		this.props.add( new Prop( new Connector( "_OUTPUT", new Type("item"), new Attr( new Content( new Type("text"), new Count(1) ) ) ) ) );
		
		// System.out.println( "FetchPage results " + contentString );
		
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
