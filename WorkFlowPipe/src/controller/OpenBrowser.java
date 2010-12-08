package controller;

import javax.swing.JOptionPane;

public class OpenBrowser {
	public static void openURL(String url) {
		String osName = System.getProperty("os.name");
		try {
			if (osName.startsWith("Windows"))
			{
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + url);
			} else if (osName.startsWith("Mac"))
			{
				Runtime.getRuntime().exec(new String[] { "open", url }).waitFor();
			}
			else {
				String[] browsers = { "firefox", "opera", "konqueror",
						"epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++)
					if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] })
							.waitFor() == 0)
					{
						//System.out.println("Found " + count + " browsers.");
						browser = browsers[count];
					}
				Runtime.getRuntime().exec(new String[] { browser, url });
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error in opening browser"
					+ ":\n" + e.getLocalizedMessage());
		}
	}
}