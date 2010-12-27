package controller;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Controller used for all authentication checks
 *
 * @author rick
 *
 */
//TODO: add oauth per g-data sample at http://code.google.com/p/gdata-java-client/downloads/detail?name=gdata-samples.java-1.42.0.zip&can=2&q=
public class IdentityController {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println( isValidAccount("marinerscovesecretary@gmail.com", "mariners") );

	}

	// credit to Yan Cheng Cheok
	/**
	 * Generic identity check using Google accounts or Google Aps credentials
	 */
	public static boolean isValidAccount(String email, String password)
			throws Exception {
		if (email == null || password == null) {
			return false;
		}

		// URL of target page script.
		final URL url = new URL("https://www.google.com/accounts/ClientLogin");
		final URLConnection urlConn = url.openConnection();
		urlConn.setDoInput(true);
		urlConn.setDoOutput(true);
		urlConn.setUseCaches(false);
		urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		// Send POST output.
		final DataOutputStream cgiInput = new DataOutputStream( urlConn.getOutputStream() );
		
		// http://code.google.com/apis/base/faq_gdata.html#clientlogin
		String content = "accountType=" + URLEncoder.encode("HOSTED_OR_GOOGLE")
				+ "&Email=" + URLEncoder.encode(email) + "&Passwd="
				+ URLEncoder.encode(password) + "&service="
				+ URLEncoder.encode("mail") + "&source="
				+ URLEncoder.encode("JStock-1.05b");

		cgiInput.writeBytes(content);
		cgiInput.flush();
		cgiInput.close();

		//ref: http://code.google.com/apis/accounts/docs/AuthForInstalledApps.html
		//TODO: add responses for 403 and CAPTCHA failures
		
		if (urlConn instanceof HttpURLConnection) {
			// 200 means account is OK
			return ((HttpURLConnection) urlConn).getResponseCode() == 200;
		} else {
			return false;
		}
	}

}
