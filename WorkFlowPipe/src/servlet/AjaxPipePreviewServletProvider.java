package servlet;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import controller.PipesController;

public class AjaxPipePreviewServletProvider extends HttpServlet {

	private static final boolean DEBUG = false;
	private static final long serialVersionUID = -1156366721753088035L;
	private PipesController pipesController;
	
	public AjaxPipePreviewServletProvider( PipesController pipesController  ) 
	{	
		
		if (DEBUG) System.out.println( "Servlet provider constructed");
		//maintain a reference to the pipes controller
		this.pipesController = pipesController;
	}

	protected void doPost( HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException {
		
		System.out.println( "Servlet provider :: do post called");
		
		// Evaluate the inputs
		String definitionString = request.getParameter( "def" );
		
		System.out.println( "Servlet provider :: def is " + definitionString);
		
		JSONObject defJSON = null;
		try {
			defJSON = new JSONObject( definitionString );
		} catch (ParseException e) {
			e.printStackTrace();
			response.setContentType("application/json");
			response.setHeader("Cache-Control", "no-cache");
			response.getWriter().write( request.getParameter( "def" ) );
			return;
		}
		
		// Have the pipes controller process the results
		JSONObject responseJSON = pipesController.evaluate( defJSON );
			
		System.out.println( "Servlet provider :: post evalute JSON is " + responseJSON );
		
		String output =  responseJSON.toString();
		
		// generate and send the response
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write( output );
	}

	protected void doGet( HttpServletRequest request, HttpServletResponse response )
	throws ServletException, IOException {
		doPost( request, response );
	}

}
