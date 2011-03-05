//
// AjaxPipeSaveServletProvider.java
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
