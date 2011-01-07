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
 * Represents the module type "fetchpage"
 * @author rick
 */
public class FetchPage extends Module implements Serializable {

	private static final long serialVersionUID = 5622501473362572605L;

	public static FetchPage getFetchPage() {
		
		FetchPage fetchPage = new FetchPage();
		
		fetchPage.id = new ID("");

		fetchPage.terminals.add( Terminal.getOutputTerminal("items") );

		fetchPage.ui = new UI("\n\t\t<div class=\"horizontal\">\n\t\t\t<label>URL: </label><input name=\"URL\" type=\"url\" required=\"true\"/>\n\t\t</div> \n\t\t<div class=\"horizontal\">\n\t\t\t<label>Cut content from: </label><input name=\"from\" type=\"text\" required=\"true\"/>\n            <label>to: </label><input name=\"to\" type=\"text\" required=\"true\"/>\n        </div>\n        <div class=\"horizontal\">\n            <label>Split using delimiter: </label><input name=\"token\" type=\"text\" required=\"true\"/>\n            \n\t\t</div> \n\t\t");

		fetchPage.name = new Name("Fetch Page");

		fetchPage.type = new Type("fetchpage");

		fetchPage.description = new Description("Fetch HTML or XHTML documents and emit as a string");

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
		
		Conf urlConf = Conf.getConf( "URL", confs );
		System.out.println("FetchPage urlConf is " + urlConf.getJSONObject() );
				
		// get the url
		String url = urlConf.getValue().getValue();
		System.out.println("FetchPage conf value for URL conf is " + url );
		
		String s;
		String contentString = "";
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(new URL("http://" + url).openStream()));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			while ((s = r.readLine()) != null) {
				contentString += s;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
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
	
	// To converts an InputStream to String 
	private String convertStreamToString( InputStream inputStream ) 
	{
		// check input stream not null
		if ( inputStream != null) 
		{ 
			
			Writer writer = new StringWriter();
			char[] buffer = new char[1024]; 
			
			try {
					Reader reader = new BufferedReader( new InputStreamReader( inputStream, "UTF-8" ));
					int n; 
					// while still reading into buffer
					while ( (n = reader.read(buffer) ) != -1) 
					{
						// output to the stringwriter
						writer.write(buffer, 0, n);
					} 
					
					//return the results if fully read
					return writer.toString();
				} 
			catch ( Exception e) 
			{ 
				this.addErrorWarning( e.getMessage() ); 
			}
			finally // close the steam return partial results
				{
					try {
						inputStream.close();
					} catch (IOException e) {
						this.addErrorWarning( e.getMessage() );
					} 
				}
		}
		
		return "";
	}


	private InputStream getInputStreamFromUrl( String url ) 
	{
		InputStream content = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			HttpClient httpclient = new DefaultHttpClient();
			
			// Execute HTTP Get Request
			HttpResponse response = httpclient.execute(httpGet);
			content = response.getEntity().getContent();
		} 
		catch ( Exception e ) 
		{
			this.addErrorWarning( e.getMessage() );
		}
		
		return content;
	}
}
