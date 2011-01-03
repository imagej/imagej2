package experimental;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Demonstrate the URLConnection Object
 * @author rick
 *
 */
public class URLConnectionDemo {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		java.net.URLConnection urlConnection = new URLConnection( new URL("/experimental/sampleDoc.bin") ) 
		{
			
			@Override
			public void connect() throws IOException {
				// TODO Auto-generated method stub
				
			}
		};

	}

}
