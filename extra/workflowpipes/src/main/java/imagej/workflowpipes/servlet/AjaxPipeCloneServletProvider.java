//
// AjaxPipeCloneServletProvider.java
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

import org.json.JSONObject;

import imagej.workflowpipes.controller.PipesController;

/**
 * ajax.pipe.clone passes four fields; _out:json, id:(optional if saved, 32
 * character hex id of parent), rnd:(4 char number) and .crumb:(11 digit alpha
 * numeric)
 * 
 * The call return two json fields "ok":1 and "new_id":(new 32 character hex id)
 * 
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

	public AjaxPipeCloneServletProvider(PipesController pipesController) {

		this.pipesController = pipesController;

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// call doPOST() to submit the id
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		// get the .crumb
		String crumb = request.getParameter(".crumb");

		// get the random number
		String randomNumber = request.getParameter("rnd");

		// get the return format
		String out = request.getParameter("_out");

		// TODO: add rnd and .crumb checks

		// check for the four required parameters
		String parentID = request.getParameter("id");

		// get the response, a string containing the id of the clone
		json = pipesController.clonePipe( parentID, crumb, json );


		// generate and send the response
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write(json.toString());
	}

}
