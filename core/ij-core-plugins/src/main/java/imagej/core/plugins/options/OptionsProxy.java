package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Proxy Settings...", weight = 12) })
public class OptionsProxy implements ImageJPlugin{

	@Parameter(label = "Proxy Server")
	private String proxyServer;
	
	@Parameter(label = "Port")
	private int port;

	@Parameter(label = "Authenticate")
	private boolean authenticationRequired;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
