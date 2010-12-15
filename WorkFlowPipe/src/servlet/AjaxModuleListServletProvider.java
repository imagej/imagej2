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
import pipesentity.Terminal;

public class AjaxModuleListServletProvider extends HttpServlet {

	/**
	 * 
	 */
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
			String uiStingKey = "class=        \t           \n            <div class=\\\"horizontal\\\"><label>Title:<\\/label> <input type='field' name='title'><\\/div>\n            <div class=\\\"horizontal\\\"><label>Description:<\\/label> <input type='field' name='description'><\\/div>\n            <div class=\\\"horizontal\\\"><label>Link:<\\/label> <input type='field' name='link'><\\/div>\n            <div class=\\\"horizontal\\\"><label>PubDate:<\\/label> <input type='field' name='pubdate'><\\/div>\n            <div class=\\\"horizontal\\\"><label>Author:<\\/label> <input type='field' name='author'><\\/div>\n            <div class=\\\"horizontal\\\"><label>GUID:<\\/label> <input type='field' name='guid'><\\/div>             \n            \n            <div class=\\\"horizontal\\\" style=\\\"margin-top:5px\\\"><label>media:content<\\/label> <img class=\\\"expandme content\\\" width=\\\"12\\\" height=\\\"15\\\" src=\\\"http:\\/\\/l.yimg.com\\/a\\/i\\/space.gif\\\"><\\/div>\n            \n            <div class=\\\"content_holder mediahide\\\">            \n\t            <div class=\\\"horizontal hozindent\\\"><label>url:<\\/label> <input type='field' name='mediaContentURL'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>type:<\\/label> <input type='field' name='mediaContentType'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>width:<\\/label> <input type='field' name='mediaContentWidth'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>height:<\\/label> <input type='field' name='mediaContentHeight'><\\/div>\n\t        <\\/div>\n            \n            <div class=\\\"horizontal\\\" style=\\\"margin-top:5px\\\"><label>media:thumbnail<\\/label> <img class=\\\"expandme thumb\\\" width=\\\"12\\\" height=\\\"15\\\" src=\\\"http:\\/\\/l.yimg.com\\/a\\/i\\/space.gif\\\"><\\/div>\n            \n            <div class=\\\"thumb_holder mediahide\\\" >            \n\t            <div class=\\\"horizontal hozindent\\\"><label>url:<\\/label> <input type='field' name='mediaThumbURL'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>width:<\\/label> <input type='field' name='mediaThumbWidth'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>height:<\\/label> <input type='field' name='mediaThumbHeight'><\\/div>\n\t       <\\/div>\n        ";
			jsonModule.put( "ui", uiStingKey );
	
			// add name to module
			jsonModule.put( "name", "Simple Math" );
		
			// add type to module
			jsonModule.put( "type", "simplemath" );

			// add description to module
			jsonModule.put( "description", "Simple Math");
		
			// add tags to module
			JSONArray jsonArrayTags = new JSONArray();
			jsonArrayTags.put( "system:number" );
			jsonModule.put( "tags", jsonArrayTags );
			jsonArrayModule.put(jsonModule);

			// add module to JSONObject
			json.put("module", jsonArrayModule);
		}
		
		// Add get 1 to array
		json.put("ok", new Integer(1));

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write(json.toString());
	}

}
