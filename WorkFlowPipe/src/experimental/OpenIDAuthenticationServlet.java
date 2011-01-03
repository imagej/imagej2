package experimental;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class OpenIDAuthenticationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
    	UserService userService = UserServiceFactory.getUserService();
    	User user = null;
    	try {
    		user = userService.getCurrentUser();
    	} catch ( Exception e)
    	{
    		user = null;
    	}

    
        if (user != null) {
        	JSONObject json = new JSONObject();
        	json.put( "guid", user.getFederatedIdentity() );
        	response.setContentType("application/json");
    		response.setHeader("Cache-Control", "no-cache");
    		response.getWriter().write(json.toString());
    
        } else {
        	response.sendRedirect(userService.createLoginURL(request.getRequestURI()));
        }
    }
}