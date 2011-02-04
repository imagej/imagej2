package imagej.workflowpipes.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import imagej.workflowpipes.controller.PipesController;
import imagej.workflowpipes.pipesapi.Module;

public class AjaxModuleListServletProvider extends HttpServlet {

	private static final long serialVersionUID = 1458365345236667188L;
	private JSONObject json = new JSONObject();

	public AjaxModuleListServletProvider( PipesController pipesController ) 
	{
		JSONArray jsonArrayModule = new JSONArray();

		//add each module to the array
                Module outputPipesModule = null;
		for( Module pipesModule : pipesController.getModulesServiceHashMap().values() )
		{
                    System.out.println("pipesModule " + pipesModule.getID().getValue());
                    if ("_OUTPUT".equals(pipesModule.getID().getValue())) {
                        outputPipesModule = pipesModule;
                    }
                    else {
			//Get the JSONObject representative of the module entity
			JSONObject jsonModule = pipesModule.getJSONObject();

                        System.out.println("jsonModule " + jsonModule.toString());
			
			//put the module in the array
			jsonArrayModule.put( jsonModule );
                    }
		}
                if (null != outputPipesModule) {
                    jsonArrayModule.put( outputPipesModule.getJSONObject() );
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
            System.out.println("doGet " + json.toString());
		
		 
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write(json.toString());
	}

}
