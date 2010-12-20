package controller;

import java.util.ArrayList;

import persistance.LoadLayouts;
import pipes.ModuleGenerator;
import pipesentity.Module;
import servlet.AjaxFeedFindServletProvider;
import servlet.AjaxFeedPreviewServletProvider;
import servlet.AjaxModuleInfoServletProvider;
import servlet.AjaxModuleListServletProvider;
import servlet.AjaxPipePreviewServletProvider;
import servlet.AjaxPipeSaveServletProvider;
import servlet.OpenIDAuthenticationServlet;


//TODO:add implements run() from plugin
public class Open {
	
	

	static void init( int portNumber ) throws Exception
	{
		//Create a pipes controller
		PipesController pipesController = new PipesController( LoadLayouts.loadLayouts() );
		
		//Create a new JettyServerController
		JettyServerController jettyServerController = new JettyServerController( portNumber, "index.html", "/web", true );
		
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
		
		//add the servlet to handle saving and changing of layouts
		jettyServerController.addServlet("/ajax.pipe.save", new AjaxPipeSaveServletProvider( pipesController )  );
		
        //start the session and launch the default page
		jettyServerController.startAndLaunchBrowser();
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		final int portNumber = 8080;
		
		//start the local services on port 8080
		init( portNumber );
	
	}
}
