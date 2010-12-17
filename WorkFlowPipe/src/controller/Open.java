package controller;

import java.util.ArrayList;

import pipes.ModuleGenerator;
import pipesentity.Module;
import servlet.AjaxFeedFindServletProvider;
import servlet.AjaxFeedPreviewServletProvider;
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
		
		//add the OpenID authentication servlet 
		jettyServerController.addServlet("/login.required", new OpenIDAuthenticationServlet() );
		
		//add the list servlet
		jettyServerController.addServlet("/ajax.module.list", new AjaxModuleListServletProvider( pipesSampleCollection ) );
		
		//add the info servlet
		jettyServerController.addServlet("/ajax.module.info", new AjaxModuleInfoServletProvider( pipesSampleCollection )  );
		
		//add the pipe preview servlet
		jettyServerController.addServlet("/ajax.pipe.preview", new AjaxPipePreviewServletProvider(  )  );
	
		//add the feed preview servlet
		jettyServerController.addServlet("/ajax.feed.preview", new AjaxFeedPreviewServletProvider(  )  );
	
		//add the feed find servlet
		jettyServerController.addServlet("/ajax.feed.find", new AjaxFeedFindServletProvider( pipesSampleCollection )  );
		       
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
