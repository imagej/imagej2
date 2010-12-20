package servlet;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import controller.PipesController;

/**
 * ajax.pipe.clone passes four fields; _out:json, id:(32 character hex id of parent), rnd:(4 char number)
 * and .crumb:(11 digit alpha numeric)
 * 
 * The call return two json fields
 *   "ok":1 and "new_id":(new 32 character hex id)
 * @author ipatron
 *
 */
public class AjaxPipeCloneServletProvider extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1458365345236667188L;
	private JSONObject json = new JSONObject();
	private PipesController pipesController;
	
	public AjaxPipeCloneServletProvider( PipesController pipesController ) {
		
		this.pipesController = pipesController;
		json.put( "ok", new Integer(1) );

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		//call doPOST() to submit the id
		try {
			doPOST( request, response );
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	protected void doPOST( HttpServletRequest request, HttpServletResponse response ) throws IOException, NoSuchAlgorithmException {
		
		//check for the four required parameters
		String parentID = request.getParameter("id");
		
		//get the .crumb
		String crumb = request.getParameter(".crumb");
		
		//get the random number
		String randomNumber = request.getParameter("rnd");
		
		//get the return format
		String out = request.getParameter("_out");
				
		//TODO: add rnd and .crumb checks
		
		//get the response, a string containing the id of the clone
		String newId = pipesController.clonePipe( parentID, crumb );
		
		//if output type is JSON
		if(out=="json")
			json.put( "new_id", newId );
		
		// generate and send the response
		response.setContentType( "application/json" );
		response.setHeader( "Cache-Control", "no-cache" );
		response.getWriter().write( json.toString() );
	}

}
