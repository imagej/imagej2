package imagej.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import imagej.pipesapi.Module;
import imagej.pipesentity.Attr;
import imagej.pipesentity.Conf;
import imagej.pipesentity.Connector;
import imagej.pipesentity.Content;
import imagej.pipesentity.Count;
import imagej.pipesentity.Description;
import imagej.pipesentity.Error;
import imagej.pipesentity.ID;
import imagej.pipesentity.Item;
import imagej.pipesentity.Message;
import imagej.pipesentity.Name;
import imagej.pipesentity.Prop;
import imagej.pipesentity.Tag;
import imagej.pipesentity.Terminal;
import imagej.pipesentity.Type;
import imagej.pipesentity.UI;

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

	public void go() 
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
