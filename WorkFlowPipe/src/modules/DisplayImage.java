package modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import pipesapi.Module;
import pipesentity.Attr;
import pipesentity.Conf;
import pipesentity.Connector;
import pipesentity.Content;
import pipesentity.Count;
import pipesentity.Description;
import pipesentity.Error;
import pipesentity.ID;
import pipesentity.Item;
import pipesentity.Name;
import pipesentity.Prop;
import pipesentity.Response;
import pipesentity.Tag;
import pipesentity.Terminal;
import pipesentity.Type;
import pipesentity.UI;
import util.DeepCopy;

/**
 * Represents the module type "displayimage"
 * @author rick
 */
public class DisplayImage extends Module implements Serializable {

	public static DisplayImage getDisplayImage() {
		
		DisplayImage fetchPage = new DisplayImage();
		
		fetchPage.id = new ID("");

		fetchPage.terminals.add( Terminal.getOutputTerminal("items") );

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

	@Override
	public void go() 
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
