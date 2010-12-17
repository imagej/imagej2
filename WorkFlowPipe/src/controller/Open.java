package controller;

import java.util.ArrayList;

import pipes.ModuleGenerator;
import pipesentity.Module;
import servlet.AjaxFeedFindServletProvider;
import servlet.AjaxModuleInfoServletProvider;
import servlet.AjaxModuleListServletProvider;
import servlet.AjaxPipePreviewServletProvider;
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
		
		//add the module listing servlet to the controller
		jettyServerController.addServlet("/ajax.module.list", new AjaxModuleListServletProvider( pipesSampleCollection ) );
		
		//add the module information servlet
		jettyServerController.addServlet("/ajax.module.info", new AjaxModuleInfoServletProvider( pipesSampleCollection )  );
		
		//add the module preview servlet
		jettyServerController.addServlet("/ajax.pipe.preview", new AjaxPipePreviewServletProvider(  )  );
		
		//add the module preview servlet
		jettyServerController.addServlet("/ajax.feed.find", new AjaxFeedFindServletProvider( pipesSampleCollection )  );
		
		
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
