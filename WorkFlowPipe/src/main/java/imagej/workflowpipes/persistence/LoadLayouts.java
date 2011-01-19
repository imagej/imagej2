package imagej.workflowpipes.persistence;

import java.util.HashMap;

import imagej.workflowpipes.pipesentity.Layout;
import imagej.workflowpipes.pipesentity.Type;

public class LoadLayouts {
	
	
	public static HashMap<String, Layout> loadPipes() 
	{
		HashMap<String, Layout>  pipesLayout = new HashMap<String, Layout>();

		return pipesLayout;
	}

	public static HashMap<Type, Layout> loadLayouts() 
	{		
		return new HashMap<Type, Layout>();
	}

}
