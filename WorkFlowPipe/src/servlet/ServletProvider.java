package servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class ServletProvider extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1458365345236667188L;
	private JSONObject jsonObject = new JSONObject();
	
	public ServletProvider() {
		JSONArray jsonArrayModule = new JSONArray();
		JSONArray jsonArrayTerminals = new JSONArray();
		
		//create a map for the first terminals item
		JSONObject jsonObjectTerminals1 = new JSONObject();
		jsonObjectTerminals1.put("input","number");
		jsonObjectTerminals1.put("name","_INPUT");

		//add first to terminals
		jsonArrayTerminals.put( 0, jsonObjectTerminals1 );
		
		//create a map for the second terminals item
		JSONObject jsonObjectName = new JSONObject();
		jsonObjectName.put("name","_OUTPUT");
		jsonObjectName.put("output","number");
		jsonArrayTerminals.put( 1, jsonObjectName);
		
		
		jsonObjectName = new JSONObject();
		jsonObjectName.put("terminals",jsonArrayTerminals);
		
		//add terminals to module
		jsonArrayModule.put( 1, jsonObjectName );
		
		//add ui key value to the object
		JSONObject jsonObjectUI = new JSONObject();
		String uiString = "class=";
		jsonObjectUI.put("ui", uiString);
		jsonArrayModule.put( 2, jsonObjectUI );
		
		//add name to module
		jsonObjectName.put("name","Simple Math");
		jsonArrayModule.put( 3, jsonObjectName );
		
		//add type to module
		JSONObject jsonObjectType = new JSONObject();
		jsonObjectType.put("type","simplemath");
		jsonArrayModule.put( 4, jsonObjectType );
		
		//add description to module
		JSONObject jsonObjectDescription = new JSONObject();
		jsonObjectDescription.put("description","Simple Math");
		jsonArrayModule.put( 5, jsonObjectDescription );
		
		//add tags to module
		JSONArray jsonArrayTags = new JSONArray();
		jsonArrayTags.put("system:number");
		JSONObject jsonObjectTags = new JSONObject();
		jsonObjectTags.put("tags", jsonArrayTags);
		jsonArrayModule.put(6, jsonObjectTags);
		
		jsonObject = jsonObject.put("module", jsonArrayModule );
		jsonObject.put("ok", new Integer(1) );
		
		System.out.println( jsonObject.toString() );

	}

	public ServletProvider( JSONObject jsonObject ) 
	{
		this.jsonObject = jsonObject;
	}

	protected void doGet( HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException 
			{
		response.setContentType( "text/html" );
		response.setStatus( HttpServletResponse.SC_OK );
		response.getWriter().println( jsonObject.toString() );
		//response.getWriter().println( "session=" + request.getSession(true).getId());
	}
	
	
}
