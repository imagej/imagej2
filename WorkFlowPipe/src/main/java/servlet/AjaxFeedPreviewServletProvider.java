package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import pipes.FeedFind;
import pipes.FeedPreview;

public class AjaxFeedPreviewServletProvider extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1458365345236667188L;

	public AjaxFeedPreviewServletProvider() {
	

	}
	
	protected void doPost( HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException 
	{
		//get the def
		String defString = request.getParameter( "def" );
		
		JSONObject mockObject = FeedPreview.getMockFeedPreview( defString );
		JSONObject json = new JSONObject();
	
		//add the mock result to the result object
		json.put( "result", mockObject );
		
		// Add get 1 to array
		json.put("ok", new Integer(1));

		//set the types and return the results
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write( json.toString() );
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {

		//call doPOST
		doPost( request, response );
	}

}
