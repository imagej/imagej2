package imagej.workflowpipes.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import imagej.workflowpipes.controller.PipesController;

/**
 * person.info takes the users guid and generates a page listing all of the
 * user's layouts
 * 
 */
public class PersonInfoServletProvider extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1458365345236667188L;
	private PipesController pipesController;

	public PersonInfoServletProvider(PipesController pipesController) {
		this.pipesController = pipesController;
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		doPost(request, response);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		// get the id
		String guid = request.getParameter("guid");

		// TODO: add guid checks

		
		//Build up the page
		PrintWriter out = response.getWriter();
		
		// TODO: populate with user.info html code or do this a modern way
		out.println("<title>Example</title>" + "<body bgcolor=FFFFFF>");

		out.println( pipesController.getLayoutsHTML(guid) );
		
		out.println("<P>Return to <A HREF=../simpleHTML.html>Form</A>");

		// generate and send the response
		response.setContentType("text/html");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write( out.toString() );

	}

}
