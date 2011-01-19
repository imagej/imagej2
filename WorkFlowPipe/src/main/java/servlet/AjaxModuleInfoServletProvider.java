package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import controller.PipesController;

import pipes.ModuleSearch;
import pipes.Service;
import pipesapi.Module;
import pipesentity.Info;

public class AjaxModuleInfoServletProvider extends HttpServlet {

	private static final long serialVersionUID = 1458365345236667188L;
	HashMap<Service, Module> modulesServiceHashMap;

	public AjaxModuleInfoServletProvider( PipesController pipesController ) {
		
		//hang on the reference
		this.modulesServiceHashMap = pipesController.getModulesServiceHashMap();
	}

	/**
	 * Given the output format, type, random variable and crumb
	 * Return the module information
	 */
	protected void doGet( HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException {
	
		//get the parameter
		String typeParameter = request.getParameter("type");
		
		//try to match the requested type
		Module pipesModule = null;
		if( typeParameter != null )
		{
			//Search for type 
			pipesModule = ModuleSearch.findfirstModuleOfType( typeParameter, modulesServiceHashMap );
		
			if( pipesModule.getType().getValue() != typeParameter )
			{
				response.setContentType( "application/json" );
				response.setHeader( "Cache-Control", "no-cache" );
				
				//get JSON Info
				JSONObject jsonObject = new Info( pipesModule ).getJSONObject();
					
				// Set response status
				jsonObject.put( "ok", new Integer(1) );
				
				// generate the response
				response.getWriter().write( jsonObject.toString() );
			}
		}
		
		//TODO: Add error handling
	}
}
