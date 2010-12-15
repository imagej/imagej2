package servlet;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import pipes.PipesModule;
import pipesentity.Tag;
import pipesentity.Terminal;

public class AjaxModuleListServletProvider extends HttpServlet {

	private static final long serialVersionUID = 1458365345236667188L;
	private JSONObject json = new JSONObject();

	public AjaxModuleListServletProvider( Collection<PipesModule> pipesModuleArray ) 
	{
		JSONArray jsonArrayModule = new JSONArray();

		//add each module to the array
		for( PipesModule pipesModule : pipesModuleArray )
		{
			JSONObject jsonModule = new JSONObject();
			JSONArray jsonArrayTerminals = new JSONArray();
			
			// add the terminals
			for( Terminal terminal : pipesModule.getTerminals() )
			{
				JSONObject jsonObjectTerminals = new JSONObject();
				jsonObjectTerminals.put( terminal.getTypeKey(), terminal.getTypeValue() ); 
				jsonObjectTerminals.put( terminal.getDirectionKey(), terminal.getDirectionKeyValue() );
				jsonArrayTerminals.put( jsonObjectTerminals );
			}

			// add terminals array to module
			jsonModule.put("terminals",jsonArrayTerminals);

			// add ui key value to the object
			jsonModule.put( "ui", pipesModule.getUIValue() );
	
			// add name to module
			jsonModule.put( "name", pipesModule.getNameValue() );
		
			// add type to module
			jsonModule.put( "type", pipesModule.getTypeValue() );

			// add description to module
			jsonModule.put( "description", pipesModule.getDescriptionValue() );
		
			JSONArray jsonArrayTags = new JSONArray();
			
			// add tags to array
			for( Tag tag : pipesModule.getTags() )
			{
				jsonArrayTags.put( tag.getValue() );
			}
			
			//add tags array to module
			jsonModule.put( "tags", jsonArrayTags );
			jsonArrayModule.put(jsonModule);
		}
		
		// add module to JSONObject
		json.put("module", jsonArrayModule);
		
		// Add get 1 to array
		json.put( "ok", new Integer(1) );

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write(json.toString());
	}

}
