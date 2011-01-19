package imagej.persistence;

import java.util.HashMap;

import imagej.pipesentity.Layout;
import imagej.pipesentity.Type;

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
