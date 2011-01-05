package modulesapi;

import java.util.ArrayList;

import pipesapi.Module;
import pipesentity.Conf;

public interface IModule {

	public Module getModule();
	public void go( ArrayList<Conf> confs );
	
}
