package imagej.workflowpipes.servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


import imagej.workflowpipes.controller.PipesController;
import imagej.workflowpipes.pipes.ResultFinder;
import imagej.workflowpipes.pipes.Service;
import imagej.workflowpipes.pipesapi.Module;

/**
 * Does a Post with the following parameters:
 * _out (json), query (text), rnd (6730), and .crumb(8C/LW9MBuRW)
 * 
 * @author ipatron returns the top 15 results in JSON with url, name, description, source (for source:"feed")
 * or name, description, type(E.g. type: "pipe:nHNB8TJm3BGumlGA9YS63A"), and source  (for source: "pipe")
 *
 */
public class AjaxFeedFindServletProvider extends HttpServlet {

	private static final long serialVersionUID = 1458365345236667188L;
	private HashMap<Service, Module> modulesServiceHashMap;

	public AjaxFeedFindServletProvider(PipesController pipesController) {
		
		this.modulesServiceHashMap = pipesController.getModulesServiceHashMap();
	
	}

	protected void doPOST( HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException {
		
		//get the query text
		String query = request.getParameter( "query" );
		
		//get the JSONObject
		JSONObject json = ResultFinder.getResultsJSONObject( query, modulesServiceHashMap);
		
		// Add get 1 to array
		json.put("ok", new Integer(1));
		
		//Return the results
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write( json.toString() );
		
	}
	

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		//redirect to doPOST
		doPOST( request, response );
	}

}
