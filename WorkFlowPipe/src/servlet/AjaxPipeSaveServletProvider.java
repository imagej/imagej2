package servlet;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import controller.PipesController;

/**
 * ajax.pipe.save takes _out (json), name (user entered text file name), id(if this is an update action)
 * def (JSON definition of the existing pipe state Layout), rnd (4 digit random integer), and
 *  .crumb (a sequence of alpha numerics that is 11 characters).
 * 
 * The call returns ok:1, the action:(update or insert) and id:(32 character hex notation) 
 * TODO:Is this a uuid, md5 hash, or something else?
 * 
 * Once a pipe has been saved, the format changes to include the addition of a desc:(99 red ballons), tags:(), and id:(fields)
 * 
 * @author ipatron
 *
 */
public class AjaxPipeSaveServletProvider extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1458365345236667188L;
	private JSONObject json = new JSONObject();
	private PipesController pipesController;
	

	public AjaxPipeSaveServletProvider( PipesController pipesController ) {
		this.pipesController = pipesController;
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {
			doPOST( request, response );
		} catch (NoSuchAlgorithmException e) {
			//TODO:Add logging and exception handling
			e.printStackTrace();
		}
	}

	protected void doPOST(HttpServletRequest request, HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
		
		
		
		//get the name
		String name = request.getParameter("name");
		
		//get the .crumb
		String crumb = request.getParameter(".crumb");
		
		//get the random number
		String randomNumber = request.getParameter("rnd");
		
		//get the return format
		String out = request.getParameter("_out");
		
		//get the definition format
		String def = request.getParameter("def");
				
		//TODO: add rnd and .crumb checks
		
		//get the id if this is an update call
		
		try {
			String id = request.getParameter("id");
			
			//this is an update
			json.put("action", "update");
			
			//get the response, a string containing the same id
			this.pipesController.updatePipe( name, def, id, crumb );
			
			json.put( "id", id );
			
			
		} catch (Exception e) {
			//this is not an update
			json.put( "action", "insert" );
			
			//get the response, a string containing the id of the clone
			String insertID = this.pipesController.insertPipe( name, def, crumb );
			
			json.put( "id", insertID );
			
		}
		
		// generate and send the response
		response.setContentType( "application/json" );
		response.setHeader( "Cache-Control", "no-cache" );
		response.getWriter().write( json.toString() );
		
	}

}
