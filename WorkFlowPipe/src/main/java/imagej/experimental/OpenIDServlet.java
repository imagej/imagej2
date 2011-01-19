package imagej.experimental;

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
