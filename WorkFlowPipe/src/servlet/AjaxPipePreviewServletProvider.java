package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class AjaxPipePreviewServletProvider extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1458365345236667188L;
	private JSONObject json = new JSONObject();

	public AjaxPipePreviewServletProvider() {
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
		String uiString = "class=        \t           \n            <div class=\\\"horizontal\\\"><label>Title:<\\/label> <input type='field' name='title'><\\/div>\n            <div class=\\\"horizontal\\\"><label>Description:<\\/label> <input type='field' name='description'><\\/div>\n            <div class=\\\"horizontal\\\"><label>Link:<\\/label> <input type='field' name='link'><\\/div>\n            <div class=\\\"horizontal\\\"><label>PubDate:<\\/label> <input type='field' name='pubdate'><\\/div>\n            <div class=\\\"horizontal\\\"><label>Author:<\\/label> <input type='field' name='author'><\\/div>\n            <div class=\\\"horizontal\\\"><label>GUID:<\\/label> <input type='field' name='guid'><\\/div>             \n            \n            <div class=\\\"horizontal\\\" style=\\\"margin-top:5px\\\"><label>media:content<\\/label> <img class=\\\"expandme content\\\" width=\\\"12\\\" height=\\\"15\\\" src=\\\"http:\\/\\/l.yimg.com\\/a\\/i\\/space.gif\\\"><\\/div>\n            \n            <div class=\\\"content_holder mediahide\\\">            \n\t            <div class=\\\"horizontal hozindent\\\"><label>url:<\\/label> <input type='field' name='mediaContentURL'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>type:<\\/label> <input type='field' name='mediaContentType'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>width:<\\/label> <input type='field' name='mediaContentWidth'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>height:<\\/label> <input type='field' name='mediaContentHeight'><\\/div>\n\t        <\\/div>\n            \n            <div class=\\\"horizontal\\\" style=\\\"margin-top:5px\\\"><label>media:thumbnail<\\/label> <img class=\\\"expandme thumb\\\" width=\\\"12\\\" height=\\\"15\\\" src=\\\"http:\\/\\/l.yimg.com\\/a\\/i\\/space.gif\\\"><\\/div>\n            \n            <div class=\\\"thumb_holder mediahide\\\" >            \n\t            <div class=\\\"horizontal hozindent\\\"><label>url:<\\/label> <input type='field' name='mediaThumbURL'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>width:<\\/label> <input type='field' name='mediaThumbWidth'><\\/div>\n\t            <div class=\\\"horizontal hozindent\\\"><label>height:<\\/label> <input type='field' name='mediaThumbHeight'><\\/div>\n\t       <\\/div>\n        ";
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
