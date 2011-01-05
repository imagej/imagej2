package modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import modulesapi.IModule;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

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
import pipesentity.Preview;
import pipesentity.Prop;
import pipesentity.Tag;
import pipesentity.Terminal;
import pipesentity.TerminalConnectorType;
import pipesentity.Type;
import pipesentity.UI;
import pipesentity.Value;

/**
 * Represents the SW9 module settings
 * @author rick
 */
public class FetchPage extends Module implements IModule {

	public FetchPage( JSONObject jsonObject ) {
		
		super(jsonObject);
	
		id = new ID("");

		terminals = Terminal.getOutTerminal(TerminalConnectorType.outputType.valueOf("items"));

		ui = new UI("\n\t\t<div class=\"horizontal\">\n\t\t\t<label>URL: </label><input name=\"URL\" type=\"url\" required=\"true\"/>\n\t\t</div> \n\t\t<div class=\"horizontal\">\n\t\t\t<label>Cut content from: </label><input name=\"from\" type=\"text\" required=\"true\"/>\n            <label>to: </label><input name=\"to\" type=\"text\" required=\"true\"/>\n        </div>\n        <div class=\"horizontal\">\n            <label>Split using delimiter: </label><input name=\"token\" type=\"text\" required=\"true\"/>\n            \n\t\t</div> \n\t\t");

		name = new Name("Fetch Page");

		type = new Type("fetchpage");

		description = new Description("Fetch HTML or XHTML documents and emit as a string");

		Tag tag = new Tag("system:sources");

		tags = Tag.getTagsArray(tag);
	}

	@Override
	public void go( ArrayList<Conf> confs ) 
	{	
		// call the start method
		start();
		
		this.confs = confs;
		
		Conf urlConf = Conf.getConf( "URL", confs );
		
		// check for null
		if( urlConf == null )
		{
			this.addErrorWarning( "Could not find url" );
			return;
		}
		
		// get the url
		String url = urlConf.getValue().getValue();

		InputStream inputStream = getInputStreamFromUrl( url );
		
		String contentString = convertStreamToString( inputStream );
		
		// add the content to prop //TODO: there is likely a much easier way to do this...
		this.props.add( new Prop( new Connector( "_OUTPUT", new Type("item"), new Attr( new Content( new Type("text"), new Count(1) ) ) ) ) );
		
		// add the items
		this.items.add( new Item( "content", contentString ) );
		
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
			{ this.addErrorWarning( e.getMessage() ); }
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

	@Override
	public Module getModule() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
