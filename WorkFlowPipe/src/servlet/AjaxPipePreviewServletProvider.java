package servlet;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import pipesentity.Def;
import controller.PipesController;

public class AjaxPipePreviewServletProvider extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1458365345236667188L;
	private JSONObject json = new JSONObject();
	private PipesController pipesController;
	
	public AjaxPipePreviewServletProvider( PipesController pipesController  ) 
	{	
		//maintain a reference to the pipes controller
		this.pipesController = pipesController;
	}

	protected void doPost( HttpServletRequest request,
			HttpServletResponse response ) 
	{
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
	
		// Evaluate the inputs
		String definition = request.getParameter( "def" );
		
		//Try to get a Java def
		Def def = null;
		try {
			//get the def object
			def = Def.getDef( definition );
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Have the pipes controller process the results
		JSONObject responseJSON = pipesController.evaluate( def );
			
		// generate and send the response
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		try {
			response.getWriter().write( responseJSON.toString() );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void doGet( HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException {

		doPost( request, response );
	}

}
