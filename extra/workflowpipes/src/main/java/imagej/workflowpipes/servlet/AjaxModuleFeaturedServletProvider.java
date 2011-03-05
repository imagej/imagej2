//
// AjaxModuleFeaturedServletProvider.java
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class AjaxModuleFeaturedServletProvider extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1458365345236667188L;
	private JSONObject json = new JSONObject();

	public AjaxModuleFeaturedServletProvider() {
		JSONArray jsonArrayModule = new JSONArray();
		JSONArray jsonArrayTerminals = new JSONArray();

		// create a map for the first terminals item
		JSONObject jsonObjectTerminals1 = new JSONObject();
		jsonObjectTerminals1.put("input", "number");
		jsonObjectTerminals1.put("name", "_INPUT");
		jsonArrayTerminals.put(0, jsonObjectTerminals1);

		// create a map for the second terminals item
		JSONObject jsonObjectTerminals2 = new JSONObject();
		jsonObjectTerminals2.put("name", "_OUTPUT");
		jsonObjectTerminals2.put("output", "number");
		jsonArrayTerminals.put(1, jsonObjectTerminals2);

		// add terminals array to module
		JSONObject jsonObjectNameTerminals = new JSONObject();
		jsonObjectNameTerminals.put("terminals", jsonArrayTerminals);
		jsonArrayModule.put(jsonObjectNameTerminals);

		// add ui key value to the object
		JSONObject jsonObjectUI = new JSONObject();
		String uiString = "class=        \t           \n            <div class=\\\"horizontal\\\"><label>Title:<\\/label> <input type='field' name='title'><\\/div>\n            <div class=\\\"horizontal\\\"><label>Description:<\\/label> <input type='field' name='description'><\\/div>\n            <div class=\\\"horizontal\\\"><label>Link:<\\/label> <input type='field' name='link'><\\/div>\n            <div class=\\\"horizontal\\\"><label>PubDate:<\\/label> <input type='field' name='pubdate'><\\/div>\n            <div class=\\\"horizontal\\\"><label>Author:<\\/label> <input type='field' name='author'><\\/div>\n            <div class=\\\"horizontal\\\"><label>GUID:<\\/label> <input type='field' name='guid'><\\/div>             \n            \n            <div class=\\\"horizontal\\\" style=\\\"margin-top:5px\\\"><label>media:content<\\/label> <img class=\\\"expandme content\\\" width=\\\"12\\\" height=\\\"15\\\" src=\\\"space.gif\\\"><\\/div>\n            \n            <div class=\\\"content_holder mediahide\\\">            \n\t            <div class=\\\"horizontal hozindent\\\"><label>url:<\\/label> <input type='field' name='mediaContentURL'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>type:<\\/label> <input type='field' name='mediaContentType'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>width:<\\/label> <input type='field' name='mediaContentWidth'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>height:<\\/label> <input type='field' name='mediaContentHeight'><\\/div>\n\t        <\\/div>\n            \n            <div class=\\\"horizontal\\\" style=\\\"margin-top:5px\\\"><label>media:thumbnail<\\/label> <img class=\\\"expandme thumb\\\" width=\\\"12\\\" height=\\\"15\\\" src=\\\"space.gif\\\"><\\/div>\n            \n            <div class=\\\"thumb_holder mediahide\\\" >            \n\t            <div class=\\\"horizontal hozindent\\\"><label>url:<\\/label> <input type='field' name='mediaThumbURL'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>width:<\\/label> <input type='field' name='mediaThumbWidth'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>height:<\\/label> <input type='field' name='mediaThumbHeight'><\\/div>\n\t       <\\/div>\n        ";
		jsonObjectUI.put("ui", uiString);
		jsonArrayModule.put(jsonObjectUI);

		// add name to module
		JSONObject jsonObjectName = new JSONObject();
		jsonObjectName.put("name", "Simple Math");
		jsonArrayModule.put(jsonObjectName);

		// add type to module
		JSONObject jsonObjectType = new JSONObject();
		jsonObjectType.put("type", "simplemath");
		jsonArrayModule.put(jsonObjectType);

		// add description to module
		JSONObject jsonObjectDescription = new JSONObject();
		jsonObjectDescription.put("description", "Simple Math");
		jsonArrayModule.put(jsonObjectDescription);

		// add tags to module
		JSONArray jsonArrayTags = new JSONArray();
		jsonArrayTags.put("system:number");
		JSONObject jsonObjectTags = new JSONObject();
		jsonObjectTags.put("tags", jsonArrayTags);
		jsonArrayModule.put(jsonObjectTags);

		// add module to JSONObject
		json.put("module", jsonArrayModule);

		// Add get 1 to array
		json.put("ok", new Integer(1));

		// System.out.println( json.toString() );

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		System.out.println(request.getParameter("id"));
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.getWriter().write(json.toString());
		// response.getWriter().println( "session=" +
		// request.getSession(true).getId());
	}

}
