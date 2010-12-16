package controller;

import java.util.ArrayList;

import pipes.ModuleGenerator;
import pipesentity.Module;
import servlet.AjaxModuleInfoServletProvider;
import servlet.AjaxModuleListServletProvider;
import servlet.OpenIDAuthenticationServlet;

//TODO:add implements run() from plugin
public class Open {

	static void init() throws Exception
	{
		//Create a new JettyServerController
		JettyServerController jettyServerController = new JettyServerController( 8080, "index.html", "/web", true );
		
		//Create a sample internal pipes collection
		ArrayList<Module> pipesSampleCollection = ModuleGenerator.getPipeModuleSampleCollection();
		
		//add the OpenID authentication servlet to the instance
		jettyServerController.addServlet("/login.required", new OpenIDAuthenticationServlet() );
		
		//add the Servlets to the controller
		jettyServerController.addServlet("/ajax.module.list", new AjaxModuleListServletProvider( pipesSampleCollection ) );
		
		//add the module info servlet
		jettyServerController.addServlet("/ajax.module.info", new AjaxModuleInfoServletProvider( pipesSampleCollection )  );
				
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
