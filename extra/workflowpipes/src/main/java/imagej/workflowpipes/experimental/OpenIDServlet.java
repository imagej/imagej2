//
// OpenIDServlet.java
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

package imagej.workflowpipes.experimental;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class OpenIDServlet extends HttpServlet {

	private static final Map<String, String> openIdProviders;
	static {
	
		openIdProviders = new HashMap<String, String>();
		openIdProviders.put("Google", "google.com/accounts/o8/id");
		openIdProviders.put("Yahoo", "yahoo.com");
		openIdProviders.put("MySpace", "myspace.com");
		openIdProviders.put("AOL", "aol.com");
		openIdProviders.put("MyOpenId.com", "myopenid.com");
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		Principal user = request.getUserPrincipal();
		Set<String> attributes = new HashSet<String>();

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		if (user != null) {
			out.println("Hello <i>" + user.getName() + "</i>!");
			out.println("[<a href=\""
					+ userService.createLogoutURL(request.getRequestURI())
					+ "\">sign out</a>]");
		} else {
			out.println("Hello world! Sign in at: ");
			for (String providerName : openIdProviders.keySet()) {
				String providerUrl = openIdProviders.get(providerName);
				String loginUrl = userService.createLoginURL(
						request.getRequestURI(), null, providerUrl, attributes);
				out.println("[<a href=\"" + loginUrl + "\">" + providerName
						+ "</a>] ");
			}
		}
	}

}
