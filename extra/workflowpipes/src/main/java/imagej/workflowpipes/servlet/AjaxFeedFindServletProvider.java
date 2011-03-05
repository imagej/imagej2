//
// AjaxFeedFindServletProvider.java
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
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


import imagej.workflowpipes.controller.PipesController;
import imagej.workflowpipes.pipes.ResultFinder;
import imagej.workflowpipes.pipes.Service;
import imagej.workflowpipes.pipesapi.Module;

/**
 * Does a Post with the following parameters:
 * _out (json), query (text), rnd (6730), and .crumb(8C/LW9MBuRW)
 * 
 * @author ipatron returns the top 15 results in JSON with url, name, description, source (for source:"feed")
 * or name, description, type(E.g. type: "pipe:nHNB8TJm3BGumlGA9YS63A"), and source  (for source: "pipe")
 *
 */
public class AjaxFeedFindServletProvider extends HttpServlet {

	private static final long serialVersionUID = 1458365345236667188L;
	private HashMap<Service, Module> modulesServiceHashMap;

	public AjaxFeedFindServletProvider(PipesController pipesController) {
		
		this.modulesServiceHashMap = pipesController.getModulesServiceHashMap();
	
	}

	protected void doPOST( HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException {
		
		//get the query text
		String query = request.getParameter( "query" );
		
		//get the JSONObject
		JSONObject json = ResultFinder.getResultsJSONObject( query, modulesServiceHashMap);
		
		// Add get 1 to array
		json.put("ok", new Integer(1));
		
		//Return the results
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write( json.toString() );
		
	}
	

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		//redirect to doPOST
		doPOST( request, response );
	}

}
