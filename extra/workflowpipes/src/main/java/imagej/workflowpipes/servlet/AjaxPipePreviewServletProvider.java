//
// AjaxPipePreviewServletProvider.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.workflowpipes.servlet;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import imagej.workflowpipes.controller.PipesController;

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
