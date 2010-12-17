package servletutils;

import java.text.ParseException;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;

/**
 * Gets a JSONObject from the POST call
 * @author loci
 *
 */
public class ServletRequestHelper {

	public static JSONObject getResponseFiled( String postVariable, HttpServletRequest request) throws ParseException
	{
		//get the parameter
		String typeParameter = request.getParameter( postVariable );
		
/*		
		StringBuffer jb = new StringBuffer();
		
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new JSONObject( jb.toString() );
		*/
		
		return new JSONObject( typeParameter );
	}

}
