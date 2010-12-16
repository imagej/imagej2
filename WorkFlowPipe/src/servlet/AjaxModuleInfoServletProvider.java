package servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import pipes.ModuleSearch;
import pipesentity.Info;
import pipesentity.Module;

public class AjaxModuleInfoServletProvider extends HttpServlet {

	private static final long serialVersionUID = 1458365345236667188L;
	 ArrayList<Module> pipesModuleCollection;

	public AjaxModuleInfoServletProvider( ArrayList<Module> pipesModuleCollection ) {
		
		//hang on the reference
		this.pipesModuleCollection = pipesModuleCollection;
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
			System.out.println("Type parameter is " + typeParameter );
			
			//Search for type 
			pipesModule = ModuleSearch.findfirstModuleOfType( typeParameter, this.pipesModuleCollection );
		
			if(pipesModule.getTypeValue() != typeParameter )
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
