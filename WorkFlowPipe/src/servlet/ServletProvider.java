package servlet;

import java.io.IOException;

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
	private JSONObject json = new JSONObject();
	
	public ServletProvider() {
		JSONArray jsonArrayModule = new JSONArray();
		JSONArray jsonArrayTerminals = new JSONArray();
		
		//create a map for the first terminals item
		JSONObject jsonObjectTerminals1 = new JSONObject();
		jsonObjectTerminals1.put("input","number");
		jsonObjectTerminals1.put("name","_INPUT");
		jsonArrayTerminals.put( 0, jsonObjectTerminals1 );
		
		//create a map for the second terminals item
		JSONObject jsonObjectTerminals2 = new JSONObject();
		jsonObjectTerminals2.put("name","_OUTPUT");
		jsonObjectTerminals2.put("output","number");
		jsonArrayTerminals.put( 1, jsonObjectTerminals2);
		
		//add terminals array to module
		JSONObject jsonObjectNameTerminals = new JSONObject();
		jsonObjectNameTerminals.put("terminals", jsonArrayTerminals);
		jsonArrayModule.put( jsonObjectNameTerminals );
		
		//add ui key value to the object
		JSONObject jsonObjectUI = new JSONObject();
		String uiString = "class=";
		jsonObjectUI.put("ui", uiString);
		jsonArrayModule.put( jsonObjectUI );
		
		//add name to module
		JSONObject jsonObjectName = new JSONObject();
		jsonObjectName.put("name","Simple Math");
		jsonArrayModule.put( jsonObjectName );
		
		//add type to module
		JSONObject jsonObjectType = new JSONObject();
		jsonObjectType.put("type","simplemath");
		jsonArrayModule.put( jsonObjectType );
		
		//add description to module
		JSONObject jsonObjectDescription = new JSONObject();
		jsonObjectDescription.put("description","Simple Math");
		jsonArrayModule.put( jsonObjectDescription );
		
		//add tags to module
		JSONArray jsonArrayTags = new JSONArray();
		jsonArrayTags.put("system:number");
		JSONObject jsonObjectTags = new JSONObject();
		jsonObjectTags.put("tags", jsonArrayTags);
		jsonArrayModule.put( jsonObjectTags );
		
		//add module to JSONObject 
		json.put("module", jsonArrayModule );
		
		//Add get 1 to array
		json.put("ok", new Integer(1) );
		
		System.out.println( json.toString() );

	}

	protected void doGet( HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException 
			{
		response.setContentType( "text/html" );
		response.setStatus( HttpServletResponse.SC_OK );
		response.getWriter().println( json );
		//response.getWriter().println( "session=" + request.getSession(true).getId());
	}
	
	
}
