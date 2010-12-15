package controller;

import java.util.Collection;

import pipes.ModuleGenerator;
import servlet.AjaxModuleListServletProvider;

//TODO:add implements run() from plugin
public class Open {

	static void init() throws Exception
	{
		//Create a new JettyServerController
		JettyServerController jettyServerController = new JettyServerController( 8080, "index.html", "/web", true );
		
		//add the Servlets to the controller
		jettyServerController.addServlet("/ajax.module.list", new AjaxModuleListServletProvider( ModuleGenerator.getPipeModuleSampleCollection() ) );
		
		//add more Servlets
		//TODO:add more servlets
       
        //start the session and launch the default page
		jettyServerController.startAndLaunchBrowser();
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		init();
        
	}
}
