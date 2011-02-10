package imagej.workflowpipes.controller;
/*
import imagej.plugin.PluginEntry;
import imagej.plugin.ij2.IPlugin;
import imagej.plugin.ij2.Ij2PluginFinder;
import imagej.plugin.ij2.Ij2PluginRunner;
import imagej.plugin.ij2.ParameterHandler;
*/
import java.util.HashMap;

import imagej.workflowpipes.experimental.OpenIDAuthenticationServlet;
import imagej.workflowpipes.persistence.LoadLayouts;
import imagej.workflowpipes.pipes.ModuleGenerator;
import imagej.workflowpipes.pipes.Service;
import imagej.workflowpipes.pipesapi.Module;
import imagej.workflowpipes.servlet.AjaxFeedFindServletProvider;
import imagej.workflowpipes.servlet.AjaxFeedPreviewServletProvider;
import imagej.workflowpipes.servlet.AjaxModuleInfoServletProvider;
import imagej.workflowpipes.servlet.AjaxModuleListServletProvider;
import imagej.workflowpipes.servlet.AjaxPipeCloneServletProvider;
import imagej.workflowpipes.servlet.AjaxPipePreviewServletProvider;
import imagej.workflowpipes.servlet.AjaxPipeSaveServletProvider;
import imagej.workflowpipes.servlet.AjaxUserUpdatewebpathServletProvider;
import imagej.workflowpipes.servlet.PipeDeleteServletProvider;


//TODO:add implements run() from plugin
public class Open {
    private static final boolean DEBUG = true;
    
   	static void init( int portNumber ) throws Exception
	{
		/*
		//load the ij2 plugin loader
		Ij2PluginFinder ij2PluginFinder = new Ij2PluginFinder();
		ArrayList<PluginEntry> plugins = new ArrayList<PluginEntry>();
		
		//load the list
		//ij2PluginFinder.findPlugins( plugins );
		
		//add plugin list manually
		plugins.add(new PluginEntry("imagej.gui.ImageFromURL", new ArrayList<String>(), "ImageFromURL", ""));
		plugins.add(new PluginEntry("imagej.gui.GradientImage", new ArrayList<String>(), "GradientImage", ""));
		
		//get a parameter handler
		PluginEntry first = plugins.get(0);
		System.out.println("first plugin is " + first.getLabel());
		//get an instance...
		Ij2PluginRunner ij2PluginRunner = new Ij2PluginRunner();
		IPlugin iplugin = ij2PluginRunner.createInstance( first );
		
		//get the input map
		HashMap<String, Object> inputParameterMap = (HashMap<String, Object>) ParameterHandler.getInputMap( iplugin );
		
		//get the input and output maps
		HashMap<String, Object> outputParameterMap = (HashMap<String, Object>) ParameterHandler.getOutputMap( iplugin );
		
		for( String keyString : inputParameterMap.keySet() )
			System.out.println("inputParameter " + keyString + " is " + inputParameterMap.get( keyString) );
		
		for( String keyString : outputParameterMap.keySet() )
			System.out.println("outputParameter " + keyString + " is " + outputParameterMap.get( keyString) );
		*/
		
		//Get the internal modules
		//HashMap<Service,Module> internalModulesHashMap = ModuleGenerator.getInternalModules();
		
		HashMap<Service,Module> internalModulesHashMap = ModuleGenerator.getInternalModules2();
		
		//Create a pipes controller
		PipesController pipesController = new PipesController( LoadLayouts.loadLayouts(), internalModulesHashMap );
			
		//Create a new JettyServerController
		JettyServerController jettyServerController = new JettyServerController( portNumber, "pipe.edit", "/web", true );
		
		//add the OpenID authentication servlet 
		jettyServerController.addServlet("/login.required", new OpenIDAuthenticationServlet() );
		//jettyServerController.addServlet("/login.required", new OpenIDServlet() );
		
		//add the list servlet
		jettyServerController.addServlet("/ajax.module.list", new AjaxModuleListServletProvider( pipesController ) );
		
		//add the info servlet
		jettyServerController.addServlet("/ajax.module.info", new AjaxModuleInfoServletProvider( pipesController )  );
		
		//add the pipe preview servlet
		jettyServerController.addServlet("/ajax.pipe.preview", new AjaxPipePreviewServletProvider( pipesController )  );
	
		//add the feed preview servlet
		jettyServerController.addServlet("/ajax.feed.preview", new AjaxFeedPreviewServletProvider(  )  );
	
		//add the feed find servlet
		jettyServerController.addServlet("/ajax.feed.find", new AjaxFeedFindServletProvider( pipesController )  );
		
		//add the servlet to handle saving and changing of layouts
		jettyServerController.addServlet("/ajax.pipe.save", new AjaxPipeSaveServletProvider( pipesController )  );
		
		//add the ability to clone layouts
		jettyServerController.addServlet("/ajax.pipe.clone", new AjaxPipeCloneServletProvider( pipesController )  );
       
		//add the ability to update the session path
		jettyServerController.addServlet("/ajax.user.updatewebpath", new AjaxUserUpdatewebpathServletProvider( pipesController )  );

                //add the ability to delete a user created layout
		jettyServerController.addServlet("/pipe.delete", new PipeDeleteServletProvider( pipesController )  );
       
		//add the ability to delete a user created layout
		//jettyServerController.addServlet("/person.info", new PersonInfoServletProvider( pipesController )  );
			
		//start the session and launch the default page
		jettyServerController.startAndLaunchBrowser();

                System.in.read();

                if (DEBUG) System.out.println("Shutting down Jetty");

                //stop the local Jetty ajax services
                jettyServerController.stop();

                //exit to close the threads
                System.exit(1);

	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main( String[] args ) throws Exception {
		
		final int portNumber = 61022;
		
		//start the local Jetty ajax services
		init( portNumber );
	
	}

   
   
}
