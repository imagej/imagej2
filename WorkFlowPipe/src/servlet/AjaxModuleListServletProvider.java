package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import pipesentity.Module;
import controller.PipesController;

public class AjaxModuleListServletProvider extends HttpServlet {

	private static final long serialVersionUID = 1458365345236667188L;
	private JSONObject json = new JSONObject();

	public AjaxModuleListServletProvider( PipesController pipesController ) 
	{
		JSONArray jsonArrayModule = new JSONArray();

		//add each module to the array
		for( Module pipesModule : pipesController.getModulesArrayList() )
		{
			//Get the JSONObject representative of the module entity
			JSONObject jsonModule = pipesModule.getJSONObject();
			
			//put the module in the array
			jsonArrayModule.put( jsonModule );
		}
		
		// add module to JSONObject
		json.put("module", jsonArrayModule);
		
		// Add get 1 to array
		json.put( "ok", new Integer(1) );

	}

	/**
	 * input parameters are 	_out=json
	 * 							rnd=5582
	 * 							.crumb=RqBrtsZBJKP
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		 
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write(json.toString());
	}

}
