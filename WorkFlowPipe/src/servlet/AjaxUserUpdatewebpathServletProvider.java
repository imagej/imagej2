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
 * ajax.user.updatewebpath takes four inputs through the post; path, _rnd, .crumb, and _out
 * and returns a string message such as {"ok":0,"message":"Update failed: 'httppipesyahoocompipespersoninfo' is not allowed"} for failure
 * or {"ok":1,"data":"http:\/\/pipes.yahoo.com\/pipes\/person.info?guid=KLJRE3343"} for success
 * 
 * An example of a successful post is: 
path:/pipes/person.info
_rnd:5021
.crumb:9Dxl9FJn/wN
_out:json
 * 
 */
public class AjaxUserUpdatewebpathServletProvider extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1458365345236667188L;
	private JSONObject json = new JSONObject();
	private PipesController pipesController;

	public AjaxUserUpdatewebpathServletProvider(PipesController pipesController) {
		this.pipesController = pipesController;
	}

	protected void doGet( HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException {

		doPost(request, response);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		// get the path
		String path = request.getParameter("path");
		
		// get the random number
		String rnd = request.getParameter("_rnd");
		
		// get the .crumb
		String crumb = request.getParameter(".crumb");
		
		// get the output format
		String out = request.getParameter("_out");
		
		// TODO: add rnd and .crumb checks

		// get the response, a string containing the same id
		json = this.pipesController.userUpdatewebpath( path, rnd, out, json, crumb );

		// generate and send the response
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write( json.toString() );

	}

}
