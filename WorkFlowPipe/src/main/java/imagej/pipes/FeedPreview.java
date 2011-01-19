package imagej.pipes;

import org.json.JSONObject;

public class FeedPreview {

	public static JSONObject getMockFeedPreview( String des )
	{
		JSONObject mockFeedJSONObject = new JSONObject();
		
		return mockFeedJSONObject.put( "favicon", "http://news.google.com/favicon.ico" );
	}
}
