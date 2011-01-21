package imagej.workflowpipes.servlet;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import imagej.workflowpipes.controller.PipesController;

/**
 * ajax.pipe.save takes _out (json), name (user entered text file name), id(if
 * this is an update action) def (JSON definition of the existing pipe state
 * Layout), rnd (4 digit random integer), and .crumb (a sequence of alpha
 * numerics that is 11 characters).
 * 
 * The call returns ok:1, the action:(update or insert) and id:(32 character hex
 * notation) TODO:Is this a uuid, md5 hash, or something else?
 * 
 * Once a pipe has been saved, the format changes to include the addition of a
 * desc:(99 red ballons), tags:(), and id:(fields)
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

	public AjaxPipeSaveServletProvider(PipesController pipesController) {
		this.pipesController = pipesController;
	}

	protected void doGet( HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException {

		doPost(request, response);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		// get the name
		String name = request.getParameter("name");
		
		// get the desc
		String desc = request.getParameter("desc");
		
		// get the id
		String id = request.getParameter("id");
		
		// get the tags
		String tags = request.getParameter("tags");

		// get the .crumb
		String crumb = request.getParameter(".crumb");

		// get the random number
		String randomNumber = request.getParameter("rnd");

		// get the return format
		String out = request.getParameter("_out");

		// get the definition format
		String def = request.getParameter("def");

		// TODO: add rnd and .crumb checks

		// get the response, a string containing the same id
		json = this.pipesController.savePipe( id, def, name, desc, tags, json, crumb );

		// generate and send the response
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write( json.toString() );

	}

}
